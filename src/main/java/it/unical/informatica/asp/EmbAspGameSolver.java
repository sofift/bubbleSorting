package it.unical.informatica.asp;

import it.unical.informatica.model.GameState;
import it.unical.informatica.model.Tube;
import it.unical.informatica.model.Ball;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.OptionDescriptor;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.IllegalAnnotationException;
import it.unical.mat.embasp.languages.ObjectNotValidException;
import it.unical.mat.embasp.languages.Param;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.languages.asp.ASPMapper;
import it.unical.mat.embasp.languages.asp.AnswerSet;
import it.unical.mat.embasp.languages.asp.AnswerSets;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Classe per risolvere automaticamente il gioco usando embASP con DLV2 - VERSIONE CORRETTA
 */
public class EmbAspGameSolver {

    private static final String DLV2_PATH = "libs/dlv-2.1.2-win64.exe";
    private static final String GAME_SOLVER_PROGRAM = "src/main/resources/asp/game_solver.lp";

    private final Handler handler;
    private final ASPMapper mapper;

    public EmbAspGameSolver() {
        // Inizializza handler per DLV2
        this.handler = new DesktopHandler(new DLV2DesktopService(DLV2_PATH));

        // Inizializza mapper usando getInstance()
        this.mapper = ASPMapper.getInstance();
        setupMapper();
    }

    /**
     * Configura il mapper per il mapping automatico
     */
    private void setupMapper() {
        try {
            // Registra le classi per il mapping
            mapper.registerClass(EmbAspLevelChecker.TubeFact.class);
            mapper.registerClass(EmbAspLevelChecker.BallFact.class);
            mapper.registerClass(EmbAspLevelChecker.ColorFact.class);
            mapper.registerClass(MoveFact.class);
        } catch (ObjectNotValidException | IllegalAnnotationException e) {
            System.err.println("Errore nel setup mapper solver: " + e.getMessage());
        }
    }

    /**
     * Risolve il gioco e restituisce la sequenza di mosse ottimali
     */
    public List<GameState.Move> solve(GameState gameState) {
        try {
            // Crea il programma ASP
            InputProgram program = new ASPInputProgram();
            program.addFilesPath(GAME_SOLVER_PROGRAM);

            // Converti GameState in fatti ASP e aggiungili al programma
            convertAndAddGameStateFacts(program, gameState);

            // Aggiungi opzioni per DLV2
            handler.addOption(new OptionDescriptor("--filter=move"));
            handler.addOption(new OptionDescriptor("--models=1"));

            // Aggiungi il programma al handler
            handler.addProgram(program);

            // ✅ CORREZIONE: startSync() senza parametri
            Output output = handler.startSync();

            // Converte l'output in AnswerSets
            AnswerSets answerSets = (AnswerSets) output;

            // Converte i risultati in mosse
            return extractMoves(answerSets);

        } catch (Exception e) {
            System.err.println("Errore nella risoluzione con embASP: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Converte GameState in fatti ASP (riusa la logica del checker)
     */
    private void convertAndAddGameStateFacts(InputProgram program, GameState gameState) throws Exception {
        // Parametri del gioco
        program.addObjectInput(new EmbAspLevelChecker.NumTubesFact(gameState.getNumberOfTubes()));
        program.addObjectInput(new EmbAspLevelChecker.TubeCapacityFact(gameState.getTubeCapacity()));
        program.addObjectInput(new EmbAspLevelChecker.NumColorsFact(gameState.getNumberOfColors()));

        // Colori utilizzati
        Set<String> usedColors = new HashSet<>();

        // Stato dei tubi
        for (int tubeIndex = 0; tubeIndex < gameState.getNumberOfTubes(); tubeIndex++) {
            Tube tube = gameState.getTube(tubeIndex);
            program.addObjectInput(new EmbAspLevelChecker.TubeFact(tubeIndex));

            List<Ball> balls = tube.getBalls();
            for (int pos = 0; pos < balls.size(); pos++) {
                Ball ball = balls.get(pos);
                String colorName = ball.getColor().name().toLowerCase();

                program.addObjectInput(new EmbAspLevelChecker.BallFact(tubeIndex, pos, colorName));
                usedColors.add(colorName);
            }
        }

        // Aggiungi i colori utilizzati
        for (String color : usedColors) {
            program.addObjectInput(new EmbAspLevelChecker.ColorFact(color));
        }
    }

    /**
     * Estrae le mosse dagli AnswerSets
     */
    private List<GameState.Move> extractMoves(AnswerSets answerSets) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if (answerSets.getAnswersets().isEmpty()) {
            return new ArrayList<>();
        }

        // Prendi il primo (e migliore) answer set
        AnswerSet bestAnswer = answerSets.getAnswersets().get(0);

        // Raccogli tutti i fatti di tipo "move"
        Map<Integer, GameState.Move> movesByStep = new TreeMap<>();

        for (Object atom : bestAnswer.getAtoms()) {
            if (atom instanceof MoveFact) {
                MoveFact moveFact = (MoveFact) atom;
                GameState.Move move = new GameState.Move(
                        moveFact.getFromTube(),
                        moveFact.getToTube()
                );
                movesByStep.put(moveFact.getStep(), move);
            }
        }

        // Restituisce le mosse ordinate per step
        return new ArrayList<>(movesByStep.values());
    }

    /**
     * Trova solo la prossima mossa migliore
     */
    public GameState.Move findNextMove(GameState gameState) {
        List<GameState.Move> solution = solve(gameState);
        return solution.isEmpty() ? null : solution.get(0);
    }

    /**
     * Stima la difficoltà del livello (numero di mosse necessarie)
     */
    public int estimateDifficulty(GameState gameState) {
        return solve(gameState).size();
    }

    /**
     * Verifica se una mossa è ottimale
     */
    public boolean isMoveOptimal(GameState gameState, GameState.Move candidateMove) {
        GameState.Move bestMove = findNextMove(gameState);
        return bestMove != null && bestMove.equals(candidateMove);
    }

    /**
     * Risoluzione con statistiche dettagliate
     */
    public SolveResult solveWithStats(GameState gameState) {
        long startTime = System.currentTimeMillis();

        try {
            List<GameState.Move> moves = solve(gameState);
            long duration = System.currentTimeMillis() - startTime;

            return new SolveResult(
                    !moves.isEmpty(),
                    moves,
                    duration,
                    "SUCCESS",
                    "embASP with DLV2"
            );

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new SolveResult(
                    false,
                    new ArrayList<>(),
                    duration,
                    "ERROR",
                    e.getMessage()
            );
        }
    }

    // ============================================================================
    // CLASSI per il MAPPING AUTOMATICO embASP - SOLVER - VERSIONE CORRETTA
    // ============================================================================

    /**
     * ✅ MAPPING CORRETTO: Rappresenta una mossa in ASP
     */
    @Id("move")
    public static class MoveFact {
        @Param(0)
        private int fromTube;

        @Param(1)
        private int toTube;

        @Param(2)
        private int step;

        public MoveFact(int fromTube, int toTube, int step) {
            this.fromTube = fromTube;
            this.toTube = toTube;
            this.step = step;
        }

        public int getFromTube() { return fromTube; }
        public int getToTube() { return toTube; }
        public int getStep() { return step; }
    }

    /**
     * Risultato della risoluzione con statistiche
     */
    public static class SolveResult {
        private final boolean solved;
        private final List<GameState.Move> moves;
        private final long duration;
        private final String status;
        private final String details;

        public SolveResult(boolean solved, List<GameState.Move> moves, long duration,
                           String status, String details) {
            this.solved = solved;
            this.moves = moves;
            this.duration = duration;
            this.status = status;
            this.details = details;
        }

        // Getters
        public boolean isSolved() { return solved; }
        public List<GameState.Move> getMoves() { return moves; }
        public long getDuration() { return duration; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
        public int getMoveCount() { return moves.size(); }

        @Override
        public String toString() {
            return String.format("SolveResult[solved=%s, moves=%d, duration=%dms, status=%s, details=%s]",
                    solved, moves.size(), duration, status, details);
        }
    }
}