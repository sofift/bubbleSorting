package it.unical.informatica.asp;

import it.unical.informatica.model.*;
import it.unical.mat.embasp.base.*;
import it.unical.mat.embasp.languages.asp.*;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class AspSolver {
    private static final String DLV2_PATH = "libs/dlv-2.1.2-win64.exe";

    private static final String ASP_RULES_FILE = "src/main/resources/asp/rapidRules.asp";

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

    private Handler newHandler() {
        return new DesktopHandler(new DLV2DesktopService(DLV2_PATH));
    }

    public List<ShowMove> solve(GameState gameState) throws ASPSolverException {
        System.out.println("avvio risoluzione ASP.");
        if (gameState == null) throw new ASPSolverException("GameState nullo");
        if (!initialized) throw new ASPSolverException("Solver non inizializzato");

        try {
            handler = newHandler();
            InputProgram rules = new ASPInputProgram();
            rules.addFilesPath(ASP_RULES_FILE);

            InputProgram facts = new ASPInputProgram();
            int H = chooseHorizon(gameState);
            addGameFacts(facts, gameState, H);

            handler.addProgram(rules);
            handler.addProgram(facts);

            Output output = handler.startSync();

            List<ShowMove> moves = extractOptimalMovesSorted(output);
            return moves;

        } catch (Throwable t) {
            t.printStackTrace(System.err);
            throw new ASPSolverException("Errore nella risoluzione: " + t.getMessage(), t);
        }
    }

    public boolean isSolvable(GameState gameState) throws ASPSolverException {
        try {
            List<ShowMove> solution = solve(gameState);
            return !solution.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void addGameFacts(final InputProgram facts, final GameState gameState, final int horizon) throws Exception {
        // capacity(4).
        final int capacity = gameState.getLevel().getTubeCapacity();
        facts.addObjectInput(new CapacityFact(capacity));

        // tube(1..N).
        final int numTubes = gameState.getTubes().size();
        for (int t = 1; t <= numTubes; t++) {
            facts.addObjectInput(new TubeFact(t));
        }

        // pos(0..capacity-1).
        for (int p = 0; p < capacity; p++) {
            facts.addObjectInput(new PosFact(p));
        }

        // step(0..H).
        for (int s = 0; s <= horizon; s++) {
            facts.addObjectInput(new StepFact(s));
        }

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
    }

    private int chooseHorizon(GameState gs) {
        String diff = gs.getLevel().getDisplayName();
        if ("Facile".equals(diff)) return 15;
        if ("Medio".equals(diff)) return 25;
        return 20; // fallback
    }



    private List<ShowMove> extractOptimalMovesSorted(Output output) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        List<ShowMove> moves = new ArrayList<>();
        if (!(output instanceof AnswerSets)) return moves;

        AnswerSets answerSets = (AnswerSets) output;

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

        AnswerSet best = source.get(0);

        for (Object atom : best.getAtoms()) {
            if (atom instanceof ShowMove) {
                moves.add((ShowMove) atom);
            }
        }

        moves.sort(Comparator.comparingInt(ShowMove::getStep));
        return moves;
    }

    public void cleanup() {
        if (handler != null) {
            // EmbASP gestisce automaticamente la pulizia
            handler = null;
        }
        initialized = false;
    }

    public static class ASPSolverException extends Exception {
        public ASPSolverException(String message) {
            super(message);
        }

        public ASPSolverException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}