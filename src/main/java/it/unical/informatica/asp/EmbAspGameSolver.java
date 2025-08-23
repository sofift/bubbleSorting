package it.unical.informatica.asp;

import it.unical.informatica.model.GameState;
import it.unical.informatica.model.Tube;
import it.unical.informatica.model.Ball;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.OptionDescriptor;
import it.unical.mat.embasp.languages.IllegalAnnotationException;
import it.unical.mat.embasp.languages.ObjectNotValidException;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.languages.asp.ASPMapper;
import it.unical.mat.embasp.languages.asp.AnswerSet;
import it.unical.mat.embasp.languages.asp.AnswerSets;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.util.*;

/**
 * Classe per risolvere automaticamente il gioco usando embASP con DLV2
 */
public class EmbAspGameSolver {

    private static final String DLV2_PATH = "libs/dlv-2.1.2-win64.exe";
    private static final String GAME_SOLVER_PROGRAM = "src/main/resources/asp/game_solver.lp";

    private final Handler handler;
    private final ASPMapper mapper;

    public EmbAspGameSolver() throws ObjectNotValidException, IllegalAnnotationException {
        // Inizializza handler per DLV2
        this.handler = new DesktopHandler(new DLV2DesktopService(DLV2_PATH));

        // Inizializza mapper
        this.mapper = ASPMapper.getInstance();
        setupMapper();
    }

    /**
     * Configura il mapper per il mapping automatico
     */
    private void setupMapper() throws ObjectNotValidException, IllegalAnnotationException {
        // Registra le classi per il mapping
        mapper.registerClass(EmbAspLevelChecker.GameParameterFact.class);
        mapper.registerClass(EmbAspLevelChecker.TubeFact.class);
        mapper.registerClass(EmbAspLevelChecker.BallFact.class);
        mapper.registerClass(EmbAspLevelChecker.ColorFact.class);
        mapper.registerClass(MoveFact.class);
        mapper.registerClass(SolutionStepFact.class);
    }

    /**
     * Risolve il gioco e restituisce la sequenza di mosse ottimali
     */
    public List<GameState.Move> solve(GameState gameState) {
        try {
            // Crea il programma ASP
            InputProgram program = new ASPInputProgram();
            program.addFilesPath(GAME_SOLVER_PROGRAM);

            // Converti GameState in fatti ASP
            List<Object> facts = convertGameStateToFacts(gameState);
            program.addObjectInput(facts);

            // Configura DLV2 per trovare soluzioni ottimali
            handler.addOption(new OptionDescriptor("--filter=move"));
            handler.addOption(new OptionDescriptor("--models=1"));
            handler.addOption(new OptionDescriptor("--opt-mode=optN")); // Ottimizzazione

            // Esegue il solver
            AnswerSets answerSets = (AnswerSets) handler.startSync(program);

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
    private List<Object> convertGameStateToFacts(GameState gameState) {
        List<Object> facts = new ArrayList<>();

        // Parametri del gioco
        facts.add(new EmbAspLevelChecker.GameParameterFact("num_tubes", gameState.getNumberOfTubes()));
        facts.add(new EmbAspLevelChecker.GameParameterFact("tube_capacity", gameState.getTubeCapacity()));
        facts.add(new EmbAspLevelChecker.GameParameterFact("num_colors", gameState.getNumberOfColors()));

        // Colori utilizzati
        Set<String> usedColors = new HashSet<>();

        // Stato dei tubi
        for (int tubeIndex = 0; tubeIndex < gameState.getNumberOfTubes(); tubeIndex++) {
            Tube tube = gameState.getTube(tubeIndex);
            facts.add(new EmbAspLevelChecker.TubeFact(tubeIndex));

            List<Ball> balls = tube.getBalls();
            for (int pos = 0; pos < balls.size(); pos++) {
                Ball ball = balls.get(pos);
                String colorName = ball.getColor().name().toLowerCase();

                facts.add(new EmbAspLevelChecker.BallFact(tubeIndex, pos, colorName));
                usedColors.add(colorName);
            }
        }

        // Aggiungi i colori utilizzati
        for (String color : usedColors) {
            facts.add(new EmbAspLevelChecker.ColorFact(color));
        }

        return facts;
    }

    /**
     * Estrae le mosse dagli AnswerSets
     */
    private List<GameState.Move> extractMoves(AnswerSets answerSets) {
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
    // CLASSI per il MAPPING AUTOMATICO embASP - SOLVER
    // ============================================================================

    /**
     * Rappresenta una mossa in ASP
     */
    @it.unical.mat.embasp.languages.asp.Term
    public static class MoveFact {
        private int fromTube;
        private int toTube;
        private int step;

        public MoveFact(int fromTube, int toTube, int step) {
            this.fromTube = fromTube;
            this.toTube = toTube;
            this.step = step;
        }

        public int getFromTube() { return fromTube; }
        public int getToTube() { return toTube; }
        public int getStep() { return step; }

        @Override
        public String toString() {
            return "move(" + fromTube + ", " + toTube + ", " + step + ")";
        }
    }

    /**
     * Rappresenta un passo della soluzione
     */
    @it.unical.mat.embasp.languages.asp.Term
    public static class SolutionStepFact {
        private int step;

        public SolutionStepFact(int step) {
            this.step = step;
        }

        public int getStep() { return step; }

        @Override
        public String toString() {
            return "solution_step(" + step + ")";
        }
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