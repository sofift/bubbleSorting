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
    private static final int horizon = 40;

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
            ASPMapper.getInstance().registerClass(ShowMove.class);
            ASPMapper.getInstance().registerClass(TubeFact.class);
            ASPMapper.getInstance().registerClass(CapacityFact.class);
            ASPMapper.getInstance().registerClass(NumTubesFact.class);
            ASPMapper.getInstance().registerClass(HorizonFact.class);
            ASPMapper.getInstance().registerClass(PosFact.class);
            ASPMapper.getInstance().registerClass(StepFact.class);


            initialized = true;
            System.out.println("ASP Solver inizializzato correttamente");

        } catch (Exception e) {
            throw new ASPSolverException("Errore nell'inizializzazione del solver ASP: " + e.getMessage(), e);
        }
    }

    /**
     * Ottiene un suggerimento (prossima mossa migliore)
     */
    public ShowMove getHint(GameState gameState) throws ASPSolverException {
        if (!initialized) {
            throw new ASPSolverException("Solver non inizializzato");
        }

        try {
            List<ShowMove> solution = solve(gameState); // Orizzonte breve per suggerimenti rapidi
            return solution.isEmpty() ? null : solution.get(0);

        } catch (Exception e) {
            throw new ASPSolverException("Errore nel calcolo del suggerimento: " + e.getMessage(), e);
        }
    }

    /**
     * Risolve completamente il puzzle
     */

    /**
     * Risolve il puzzle con un orizzonte temporale specifico
     */
    public List<ShowMove> solve(GameState gameState) throws ASPSolverException {
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
            List<ShowMove> result = processResults(output);

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
            List<ShowMove> solution = solve(gameState); // Test rapido con orizzonte breve
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
    private List<ShowMove> processResults(Output output) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        List<ShowMove> moves = new ArrayList<>();
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
        Map<Integer, ShowMove> byStep = new TreeMap<>();
        for (Object atom : best.getAtoms()) {
            if (atom instanceof ShowMove) {
                ShowMove sm = (ShowMove) atom;
                // se in ASP i tubi sono 1-based, qui converti a 0-based per il tuo engine
                ShowMove m = new ShowMove(sm.getFrom() - 1, sm.getTo() - 1, sm.getStep());
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