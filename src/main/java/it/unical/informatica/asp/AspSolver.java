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
    private static final int DEFAULT_HORIZON = 40;

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
            ASPMapper.getInstance().registerClass(PosFact.class);
            ASPMapper.getInstance().registerClass(StepFact.class);
            ASPMapper.getInstance().registerClass(ShowMove.class);


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
        if (!initialized) throw new ASPSolverException("Solver non inizializzato");
        System.out.println("🤖 Avvio risoluzione ASP...");


        try {
            // 1) Prepara InputProgram
            InputProgram input = new ASPInputProgram();
            System.out.println("📁 Caricamento file: " + ASP_RULES_FILE);
            input.addFilesPath(ASP_RULES_FILE);
            System.out.println("✅ File ASP aggiunto");

            System.out.println("🔧 Generazione fatti di gioco...");
            addGameFacts(input, gameState, horizon);
            System.out.println("✅ Fatti del gioco aggiunti");

            // (facoltativo) limita l’output alle sole mosse
            try { handler.addOption(new OptionDescriptor("--filter=show_move")); } catch (Throwable ignore) {}

            // 2) Esegui
            handler.addProgram(input);
            System.out.println("⚙️ Esecuzione DLV2...");
            Output output = handler.startSync();
            System.out.println("✅ DLV2 completato");

            // 3) Parsifica risultati in un unico punto
            System.out.println("📊 Processamento risultati...");
            List<Move> result = processResults(output);

            if (result.isEmpty()) System.out.println("❌ Nessuna soluzione trovata");
            else System.out.println("✅ Trovate " + result.size() + " mosse");
            return result;

        } catch (Exception e) {
            System.err.println("❌ ERRORE nella risoluzione ASP: " + e.getMessage());
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
    void addGameFacts(InputProgram program, GameState gameState, int horizon) throws Exception {
        System.out.println("🔧 Inizio generazione fatti ASP...");

        // 1) capacity(4).
        int capacity = gameState.getLevel().getTubeCapacity(); // dovrebbe essere 4
        program.addObjectInput(new CapacityFact(capacity));
        System.out.println("   capacity(" + capacity + ").");

        // 2) tube(1). tube(2). ... (espliciti)
        int numTubes = gameState.getTubes().size();
        for (int t = 1; t <= numTubes; t++) {
            program.addObjectInput(new TubeFact(t));
            System.out.println("   tube(" + t + ").");
        }

        // 3) pos(0). pos(1). pos(2). pos(3).  (espliciti)
        for (int p = 0; p < capacity; p++) {
            program.addObjectInput(new PosFact(p));
        }
        System.out.println("   pos(0.."+(capacity-1)+") espansi.");

        // 4) step(0). step(1). ... step(horizon).  (espliciti)
        for (int s = 0; s <= horizon; s++) {
            program.addObjectInput(new StepFact(s));
        }
        System.out.println("   step(0.."+horizon+") espansi.");

        // (facoltativi: se vuoi tenerli per logging/telemetria)
        program.addObjectInput(new NumTubesFact(numTubes));
        program.addObjectInput(new HorizonFact(horizon));

        // 5) init_ball(T,P,C): ATTENZIONE a indici e colori
        int totalBalls = 0;
        for (int tIndex = 0; tIndex < numTubes; tIndex++) {
            Tube tube = gameState.getTubes().get(tIndex);
            List<Ball> balls = tube.getBalls();

            // Assunzione: P=0 è il fondo e cresce verso la cima (coerente col debug).
            // Se nel tuo modello l’array è "cima→fondo", usa: int pos = capacity - 1 - p;
            for (int p = 0; p < balls.size(); p++) {
                Ball b = balls.get(p);
                if (b == null || b.getColor() == null) continue; // salta spazi vuoti
                String color = b.getColor().name().toLowerCase(); // RED -> "red"
                program.addObjectInput(new BallFact(tIndex + 1, p, color));
                totalBalls++;
            }
        }
        System.out.println("   init_ball totali: " + totalBalls);
    }


    /**
     * Processa i risultati del solver ASP
     */
    private List<Move> processResults(Output output) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        List<Move> moves = new ArrayList<>();
        if (!(output instanceof AnswerSets)) return moves;

        AnswerSets as = (AnswerSets) output;

        // Se presente, preferisci i set ottimali (vincoli deboli)
        List<AnswerSet> models;
        try {
            models = as.getOptimalAnswerSets();  // EmbASP 7.x spesso ce l’ha
            if (models == null || models.isEmpty()) models = as.getAnswersets();
        } catch (Throwable t) {
            models = as.getAnswersets();
        }

        if (models.isEmpty()) return moves;

        // Prendi il primo (ottimale)
        AnswerSet best = models.get(0);

        // Estrai show_move(F,T,S) mappato su ShowMove (ricorda: registra la classe!)
        Map<Integer, Move> byStep = new TreeMap<>();
        for (Object atom : best.getAtoms()) {
            if (atom instanceof ShowMove) {
                ShowMove sm = (ShowMove) atom;
                // se in ASP i tubi sono 1-based, qui converti a 0-based per il tuo engine
                Move m = new Move(sm.getFrom() - 1, sm.getTo() - 1, null);
                byStep.put(sm.getStep(), m);
            }
        }
        moves.addAll(byStep.values());
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

    @Id("pos")
    public static class PosFact {
        @Param(0) private int p;
        public PosFact() {}
        public PosFact(int p) { this.p = p; }
        public int getP() { return p; }
        public void setP(int p) { this.p = p; }
        @Override public String toString(){ return "pos(" + p + ")"; }
    }

    @Id("step")
    public static class StepFact {
        @Param(0) private int s;
        public StepFact() {}
        public StepFact(int s) { this.s = s; }
        public int getS() { return s; }
        public void setS(int s) { this.s = s; }
        @Override public String toString(){ return "step(" + s + ")"; }
    }

    @Id("show_move")
    public class ShowMove {
        @Param(1) private int from;
        @Param(2) private int to;
        @Param(3) private int step;

        public ShowMove() {}
        public ShowMove(int from, int to, int step) { this.from=from; this.to=to; this.step=step; }

        public int getFrom(){ return from; }
        public int getTo(){ return to; }
        public int getStep(){ return step; }
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