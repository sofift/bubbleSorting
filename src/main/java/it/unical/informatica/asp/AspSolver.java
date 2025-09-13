package it.unical.informatica.asp;

import it.unical.informatica.model.*;
import it.unical.mat.embasp.base.*;
import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;
import it.unical.mat.embasp.languages.asp.*;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;


public class AspSolver {
    private static final String DLV2_PATH = "libs/dlv-2.1.2-win64.exe";

    private static final String ASP_RULES_FILE = "src/main/resources/asp/rapidRules.asp";
    private static final int horizon = 15;

    private Handler handler;
    private boolean initialized = false;

    public AspSolver() throws ASPSolverException {
        initializeASP();
    }

    private void initializeASP() throws ASPSolverException {
        try {
            DLV2DesktopService dlvService = new DLV2DesktopService(DLV2_PATH);

            handler = new DesktopHandler(dlvService);

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

    public List<ShowMove> solve(GameState gameState) throws ASPSolverException {
        System.out.println("🤖 Avvio risoluzione ASP (EmbASP object-mode).");
        if (gameState == null) throw new ASPSolverException("GameState nullo");
        if (!initialized) throw new ASPSolverException("Solver non inizializzato");

        try {
            // 1) Programma regole da file (ok tenerlo come path)
            InputProgram rules = new ASPInputProgram();
            rules.addFilesPath(ASP_RULES_FILE);

            // 2) Programma fatti da oggetti mappati
            InputProgram facts = new ASPInputProgram();
            int H = chooseHorizon(gameState);
            addGameFacts(facts, gameState, H);

            // 3) Usa l'handler già inizializzato da initializeASP()
            handler.addProgram(rules);
            handler.addProgram(facts);

            // 4) Avvia il solver
            Output output = handler.startSync();

            // 5) Debug opzionale
            debugPrintAnswerSets(output);

            // 6) Estrai le mosse direttamente dagli atoms mappati (ShowMove)
            List<ShowMove> moves = extractOptimalMovesSorted(output);
            System.out.println("✅ Mosse parsate (object-mode): " + moves.size());
            return moves;

        } catch (Throwable t) {
            t.printStackTrace(System.err);
            throw new ASPSolverException("Errore nella risoluzione: " + t.getMessage(), t);
        }
    }

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
    // AspSolver.java
    private void addGameFacts(final InputProgram facts, final GameState gameState, final int horizon) throws Exception {
        System.out.println("🔧 Generazione fatti ASP (EmbASP mapping).");

        // capacity(4).
        final int capacity = gameState.getLevel().getTubeCapacity();
        facts.addObjectInput(new CapacityFact(capacity));
        System.out.println("   capacity(" + capacity + ").");

        // tube(1..N).
        final int numTubes = gameState.getTubes().size();
        for (int t = 1; t <= numTubes; t++) {
            facts.addObjectInput(new TubeFact(t));
        }
        System.out.println("   tube(1.." + numTubes + ").");

        // pos(0..capacity-1).
        for (int p = 0; p < capacity; p++) {
            facts.addObjectInput(new PosFact(p));
        }
        System.out.println("   pos(0.." + (capacity - 1) + ") espansi.");

        // step(0..H).
        for (int s = 0; s <= horizon; s++) {
            facts.addObjectInput(new StepFact(s));
        }
        System.out.println("   step(0.." + horizon + ") espansi.");

        // opzionali ma utili a loggare/controllare
        facts.addObjectInput(new NumTubesFact(numTubes));
        facts.addObjectInput(new HorizonFact(horizon));

        // init_ball(T,P,C).
        int totalBalls = 0;
        for (int tIndex = 0; tIndex < numTubes; tIndex++) {
            var tube = gameState.getTubes().get(tIndex);
            var balls = tube.getBalls();
            for (int p = 0; p < balls.size(); p++) {
                var b = balls.get(p);
                if (b == null || b.getColor() == null) continue;
                String color = b.getColor().name().toLowerCase();
                facts.addObjectInput(new BallFact(tIndex + 1, p, color));
                totalBalls++;
            }
        }
        System.out.println("   init_ball totali: " + totalBalls);
    }

    private int chooseHorizon(GameState gs) {
        String diff = gs.getLevel().getDisplayName();
        if ("Facile".equals(diff)) return 15;
        if ("Medio".equals(diff)) return 25;
        if ("Difficile".equals(diff)) return 45;
        return 20; // fallback
    }







    /**
     * Processa i risultati del solver ASP
     */


    /** Stampa di debug degli answer set, leggibili */
    /** Stampa di debug degli answer set, leggibili - VERSIONE CORRETTA */
    // AspSolver.java (sostituisci il vecchio debugPrintAnswerSets)
    private static void debugPrintAnswerSets(Output out) {
        if (!(out instanceof AnswerSets)) {
            System.out.println("ℹ️ Output non è AnswerSets. Class: " + (out == null ? "null" : out.getClass().getName()));
            return;
        }
        AnswerSets as = (AnswerSets) out;

        List<AnswerSet> all = as.getAnswersets();
        System.out.println("👉 Answer sets (tot): " + (all == null ? 0 : all.size()));

        try {
            List<AnswerSet> optimal = as.getOptimalAnswerSets();
            if (optimal != null && !optimal.isEmpty()) {
                System.out.println("👉 Optimal answer sets: " + optimal.size());
            } else {
                System.out.println("👉 Nessun optimal set (normale senza ottimizzazione).");
            }
        } catch (Exception e) {
            System.out.println("👉 Optimal non disponibile: " + e.getMessage());
        }
    }




    // AspSolver.java (sostituisci il vecchio extractOptimalMovesSorted)
    private List<ShowMove> extractOptimalMovesSorted(Output output) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        List<ShowMove> moves = new ArrayList<>();
        if (!(output instanceof AnswerSets)) return moves;

        AnswerSets answerSets = (AnswerSets) output;

        // 1) Prova con gli optimal; se non ci sono, fallback a tutti gli answer set
        List<AnswerSet> source;
        try {
            source = answerSets.getOptimalAnswerSets();
            if (source == null || source.isEmpty()) {
                source = answerSets.getAnswersets();
            }
        } catch (Exception e) {
            source = answerSets.getAnswersets();
        }
        if (source == null || source.isEmpty()) return moves;

        // 2) In genere ti basta il PRIMO set (quello ottimo). Se vuoi, puoi unire più set.
        AnswerSet best = source.get(0);

        for (Object atom : best.getAtoms()) {
            if (atom instanceof ShowMove) {
                moves.add((ShowMove) atom);
            }
        }

        // 3) Ordina per step così la GUI può eseguirle in ordine
        moves.sort(Comparator.comparingInt(ShowMove::getStep));
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