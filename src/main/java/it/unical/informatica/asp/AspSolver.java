package it.unical.informatica.asp;

import it.unical.informatica.model.*;
import it.unical.mat.embasp.base.*;
import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;
import it.unical.mat.embasp.languages.asp.*;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe per l'integrazione con ASP usando EmbASP.
 * Gestisce la comunicazione con DLV2 per fornire suggerimenti e soluzioni.
 */
public class AspSolver {
    private static final String DLV2_PATH = "libs/dlv-2.1.2-win64.exe";
    private static final String ASP_RULES_FILE = "src/main/resources/asp/rules.asp";
    private static final int DEFAULT_HORIZON = 15;

    private Handler handler;
    private boolean initialized = false;

    public AspSolver() throws ASPSolverException {
        initializeASP();
    }

    /**
     * Inizializza il sistema ASP con EmbASP
     */
    private void initializeASP() throws ASPSolverException {
        try {
            // Crea il service DLV2 con il path dell'eseguibile
            DLV2DesktopService dlvService = new DLV2DesktopService(DLV2_PATH);

            // Crea l'handler
            handler = new DesktopHandler(dlvService);

            // Registra le classi per il mapping ASP
            ASPMapper.getInstance().registerClass(BallFact.class);
            ASPMapper.getInstance().registerClass(MoveFact.class);
            ASPMapper.getInstance().registerClass(TubeFact.class);
            ASPMapper.getInstance().registerClass(CapacityFact.class);
            ASPMapper.getInstance().registerClass(NumTubesFact.class);
            ASPMapper.getInstance().registerClass(HorizonFact.class);

            initialized = true;
            System.out.println("ASP Solver inizializzato correttamente");

        } catch (Exception e) {
            throw new ASPSolverException("Errore nell'inizializzazione del solver ASP: " + e.getMessage(), e);
        }
    }

    /**
     * Ottiene un suggerimento (prossima mossa migliore)
     */
    public Move getHint(GameState gameState) throws ASPSolverException {
        if (!initialized) {
            throw new ASPSolverException("Solver non inizializzato");
        }

        try {
            List<Move> solution = solve(gameState, 3); // Orizzonte breve per suggerimenti rapidi
            return solution.isEmpty() ? null : solution.get(0);

        } catch (Exception e) {
            throw new ASPSolverException("Errore nel calcolo del suggerimento: " + e.getMessage(), e);
        }
    }

    /**
     * Risolve completamente il puzzle
     */
    public List<Move> solve(GameState gameState) throws ASPSolverException {
        return solve(gameState, DEFAULT_HORIZON);
    }

    /**
     * Risolve il puzzle con un orizzonte temporale specifico
     */
    public List<Move> solve(GameState gameState, int horizon) throws ASPSolverException {
        if (!initialized) {
            throw new ASPSolverException("Solver non inizializzato");
        }

        try {
            System.out.println("🤖 Avvio risoluzione ASP...");

            // Crea l'InputProgram
            InputProgram inputProgram = new ASPInputProgram();

            // ✅ AGGIUNGE DEBUG: Verifica che il file esista
            System.out.println("📁 Caricamento file: " + ASP_RULES_FILE);

            // Aggiunge il file delle regole ASP
            inputProgram.addFilesPath(ASP_RULES_FILE);
            System.out.println("✅ File ASP aggiunto");

            // ✅ AGGIUNGE DEBUG: Stampa i fatti che verranno aggiunti
            System.out.println("🔧 Generazione fatti di gioco...");

            // Aggiunge i fatti del gioco
            addGameFacts(inputProgram, gameState, horizon);
            System.out.println("✅ Fatti del gioco aggiunti");

            // ✅ AGGIUNGE DEBUG: Mostra stato InputProgram
            System.out.println("📋 InputProgram pronto, avvio DLV2...");

            // Aggiunge il programma all'handler
            handler.addProgram(inputProgram);

            // Esegue il solver
            System.out.println("⚙️ Esecuzione DLV2...");
            Output output = handler.startSync();
            System.out.println("✅ DLV2 completato");

            // Processa i risultati
            System.out.println("📊 Processamento risultati...");
            List<Move> result = processResults(output);

            if (result.isEmpty()) {
                System.out.println("❌ Nessuna soluzione trovata");
            } else {
                System.out.println("✅ Trovate " + result.size() + " mosse");
            }

            return result;

        } catch (Exception e) {
            System.err.println("❌ ERRORE nella risoluzione ASP: " + e.getMessage());
            e.printStackTrace();
            throw new ASPSolverException("Errore nella risoluzione: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se il puzzle è risolvibile
     */
    public boolean isSolvable(GameState gameState) throws ASPSolverException {
        try {
            List<Move> solution = solve(gameState, 5); // Test rapido con orizzonte breve
            return !solution.isEmpty();
        } catch (Exception e) {
            return false; // Se c'è un errore, assumiamo non risolvibile
        }
    }

    /**
     * Aggiunge i fatti del gioco all'InputProgram
     */
    private void addGameFacts(InputProgram program, GameState gameState, int horizon) throws Exception {
        System.out.println("🔧 Inizio generazione fatti ASP...");

        // Numero di tubi
        NumTubesFact numTubesFact = new NumTubesFact(gameState.getTubes().size());
        program.addObjectInput(numTubesFact);
        System.out.println("   " + numTubesFact);

        // Capacità dei tubi
        CapacityFact capacityFact = new CapacityFact(gameState.getLevel().getTubeCapacity());
        program.addObjectInput(capacityFact);
        System.out.println("   " + capacityFact);

        // Orizzonte temporale
        HorizonFact horizonFact = new HorizonFact(horizon);
        program.addObjectInput(horizonFact);
        System.out.println("   " + horizonFact);

        // Fatti sui tubi e palline
        List<Tube> tubes = gameState.getTubes();
        int totalFacts = 0;

        for (int tubeIndex = 0; tubeIndex < tubes.size(); tubeIndex++) {
            Tube tube = tubes.get(tubeIndex);

            // Tubo (numerazione da 1 per ASP)
            TubeFact tubeFact = new TubeFact(tubeIndex + 1);
            program.addObjectInput(tubeFact);
            System.out.println("   " + tubeFact);
            totalFacts++;

            // Palline nel tubo
            List<Ball> balls = tube.getBalls();
            System.out.println("   Tubo " + (tubeIndex + 1) + " ha " + balls.size() + " palline:");

            for (int position = 0; position < balls.size(); position++) {
                Ball ball = balls.get(position);
                BallFact ballFact = new BallFact(
                        tubeIndex + 1, // tubo (1-indexed)
                        position,      // posizione (0-indexed)
                        ball.getColor().name().toLowerCase()
                );
                program.addObjectInput(ballFact);
                System.out.println("     " + ballFact);
                totalFacts++;
            }
        }

        System.out.println("✅ Aggiunti " + totalFacts + " fatti ASP totali");
    }

    /**
     * Processa i risultati del solver ASP
     */
    private List<Move> processResults(Output output) throws Exception {
        List<Move> moves = new ArrayList<>();

        System.out.println("📊 Tipo di output: " + output.getClass().getName());

        if (!(output instanceof AnswerSets)) {
            System.out.println("❌ Output non è AnswerSets");
            return moves;
        }

        AnswerSets answerSets = (AnswerSets) output;
        System.out.println("📋 Numero di AnswerSets: " + answerSets.getAnswersets().size());

        if (answerSets.getAnswersets().isEmpty()) {
            System.out.println("📋 Nessun AnswerSet trovato - puzzle non risolvibile");
            return moves;
        }

        // Prende il primo answer set (soluzione ottimale)
        AnswerSet bestAnswerSet = answerSets.getAnswersets().get(0);
        System.out.println("🔍 Atomi nell'AnswerSet: " + bestAnswerSet.getAtoms().size());

        // ✅ STAMPA TUTTI GLI ATOMI per debug
        System.out.println("🔍 Atomi trovati:");
        for (Object atom : bestAnswerSet.getAtoms()) {
            System.out.println("   " + atom + " (tipo: " + atom.getClass().getName() + ")");
        }

        // Mappa per ordinare le mosse per step
        Map<Integer, Move> stepMoves = new TreeMap<>();

        // Estrae le mosse dal set di atomi
        int movesFound = 0;
        for (Object atom : bestAnswerSet.getAtoms()) {
            if (atom instanceof MoveFact) {
                MoveFact moveFact = (MoveFact) atom;
                Move move = new Move(
                        moveFact.getFromTube() - 1, // Converti da 1-indexed a 0-indexed
                        moveFact.getToTube() - 1,   // Converti da 1-indexed a 0-indexed
                        null // Ball sarà determinato durante l'esecuzione
                );
                stepMoves.put(moveFact.getStep(), move);
                System.out.println("   ✅ Mossa trovata: " + moveFact);
                movesFound++;
            }
        }

        System.out.println("📊 Mosse totali estratte: " + movesFound);

        moves.addAll(stepMoves.values());
        return moves;
    }


    /**
     * Pulisce le risorse
     */
    public void cleanup() {
        if (handler != null) {
            // EmbASP gestisce automaticamente la pulizia
            handler = null;
        }
        initialized = false;
    }

    // ===============================
    // CLASSI PER IL MAPPING ASP
    // ===============================

    /**
     * Rappresenta init_ball(T, P, C)
     */
    @Id("init_ball")
    public static class BallFact {
        @Param(0)
        private int tube;
        @Param(1)
        private int position;
        @Param(2)
        private String color;

        public BallFact() {}

        public BallFact(int tube, int position, String color) {
            this.tube = tube;
            this.position = position;
            this.color = color;
        }

        // Getters e setters necessari per EmbASP
        public int getTube() { return tube; }
        public void setTube(int tube) { this.tube = tube; }
        public int getPosition() { return position; }
        public void setPosition(int position) { this.position = position; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        @Override
        public String toString() {
            return String.format("init_ball(%d,%d,%s)", tube, position, color);
        }
    }

    /**
     * Rappresenta move(F, T, S)
     */
    @Id("move")
    public static class MoveFact {
        @Param(0)
        private int fromTube;
        @Param(1)
        private int toTube;
        @Param(2)
        private int step;

        public MoveFact() {}

        public MoveFact(int fromTube, int toTube, int step) {
            this.fromTube = fromTube;
            this.toTube = toTube;
            this.step = step;
        }

        public int getFromTube() { return fromTube; }
        public void setFromTube(int fromTube) { this.fromTube = fromTube; }
        public int getToTube() { return toTube; }
        public void setToTube(int toTube) { this.toTube = toTube; }
        public int getStep() { return step; }
        public void setStep(int step) { this.step = step; }

        @Override
        public String toString() {
            return String.format("move(%d,%d,%d)", fromTube, toTube, step);
        }
    }

    /**
     * Rappresenta tube(T)
     */
    @Id("tube")
    public static class TubeFact {
        @Param(0)
        private int tube;

        public TubeFact() {}

        public TubeFact(int tube) {
            this.tube = tube;
        }

        public int getTube() { return tube; }
        public void setTube(int tube) { this.tube = tube; }

        @Override
        public String toString() {
            return String.format("tube(%d)", tube);
        }
    }

    /**
     * Rappresenta capacity(C)
     */
    @Id("capacity")
    public static class CapacityFact {
        @Param(0)
        private int capacity;

        public CapacityFact() {}

        public CapacityFact(int capacity) {
            this.capacity = capacity;
        }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        @Override
        public String toString() {
            return String.format("capacity(%d)", capacity);
        }
    }

    /**
     * Rappresenta num_tubes(N)
     */
    @Id("num_tubes")
    public static class NumTubesFact {
        @Param(0)
        private int numTubes;

        public NumTubesFact() {}

        public NumTubesFact(int numTubes) {
            this.numTubes = numTubes;
        }

        public int getNumTubes() { return numTubes; }
        public void setNumTubes(int numTubes) { this.numTubes = numTubes; }

        @Override
        public String toString() {
            return String.format("num_tubes(%d)", numTubes);
        }
    }

    /**
     * Rappresenta horizon(H)
     */
    @Id("horizon")
    public static class HorizonFact {
        @Param(0)
        private int horizon;

        public HorizonFact() {}

        public HorizonFact(int horizon) {
            this.horizon = horizon;
        }

        public int getHorizon() { return horizon; }
        public void setHorizon(int horizon) { this.horizon = horizon; }

        @Override
        public String toString() {
            return String.format("horizon(%d)", horizon);
        }
    }

    /**
     * Eccezione per errori del solver ASP
     */
    public static class ASPSolverException extends Exception {
        public ASPSolverException(String message) {
            super(message);
        }

        public ASPSolverException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}