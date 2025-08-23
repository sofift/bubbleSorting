package it.unical.informatica.asp;

import it.unical.informatica.model.GameState;
import it.unical.informatica.model.Tube;
import it.unical.informatica.model.Ball;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.OptionDescriptor;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.languages.asp.ASPMapper;
import it.unical.mat.embasp.languages.asp.AnswerSet;
import it.unical.mat.embasp.languages.asp.AnswerSets;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.util.*;

/**
 * Classe per verificare la risolubilità usando embASP con DLV2
 */
public class EmbAspLevelChecker {

    private static final String DLV2_PATH = "libs/dlv-2.1.2-win64.exe";
    private static final String LEVEL_CHECKER_PROGRAM = "src/main/resources/asp/level_checker.lp";

    private final Handler handler;
    private final ASPMapper mapper;

    public EmbAspLevelChecker() {
        // Inizializza handler per DLV2
        this.handler = new DesktopHandler(new DLV2DesktopService(DLV2_PATH));

        // Inizializza mapper per la conversione automatica
        this.mapper = new ASPMapper();
        setupMapper();
    }

    /**
     * Configura il mapper per la conversione automatica Java ↔ ASP
     */
    private void setupMapper() {
        // Registra le classi del dominio per il mapping automatico
        mapper.registerClass(GameStateFact.class);
        mapper.registerClass(TubeFact.class);
        mapper.registerClass(BallFact.class);
        mapper.registerClass(ColorFact.class);
        mapper.registerClass(SolvableFact.class);
    }

    /**
     * Verifica se un livello è risolvibile usando embASP
     */
    public boolean isLevelSolvable(GameState gameState) {
        try {
            // Crea il programma ASP
            InputProgram program = new ASPInputProgram();
            program.addFilesPath(LEVEL_CHECKER_PROGRAM);

            // Converti GameState in fatti ASP
            List<Object> facts = convertGameStateToFacts(gameState);

            // Aggiungi i fatti al programma
            program.addObjectInput(facts);

            // Configura opzioni per DLV2
            handler.addOption(new OptionDescriptor("--filter=solvable"));
            handler.addOption(new OptionDescriptor("--models=1"));

            // Esegue il solver
            AnswerSets answerSets = (AnswerSets) handler.startSync(program);

            // Verifica se esiste una soluzione
            return !answerSets.getAnswersets().isEmpty() &&
                    containsSolvableFact(answerSets);

        } catch (Exception e) {
            System.err.println("Errore nella verifica con embASP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Converte GameState in lista di fatti ASP
     */
    private List<Object> convertGameStateToFacts(GameState gameState) {
        List<Object> facts = new ArrayList<>();

        // Parametri del gioco
        facts.add(new GameParameterFact("num_tubes", gameState.getNumberOfTubes()));
        facts.add(new GameParameterFact("tube_capacity", gameState.getTubeCapacity()));
        facts.add(new GameParameterFact("num_colors", gameState.getNumberOfColors()));

        // Colori utilizzati
        Set<String> usedColors = new HashSet<>();

        // Stato dei tubi
        for (int tubeIndex = 0; tubeIndex < gameState.getNumberOfTubes(); tubeIndex++) {
            Tube tube = gameState.getTube(tubeIndex);
            facts.add(new TubeFact(tubeIndex));

            List<Ball> balls = tube.getBalls();
            for (int pos = 0; pos < balls.size(); pos++) {
                Ball ball = balls.get(pos);
                String colorName = ball.getColor().name().toLowerCase();

                facts.add(new BallFact(tubeIndex, pos, colorName));
                usedColors.add(colorName);
            }
        }

        // Aggiungi i colori utilizzati
        for (String color : usedColors) {
            facts.add(new ColorFact(color));
        }

        return facts;
    }

    /**
     * Verifica se gli AnswerSets contengono il fatto "solvable"
     */
    private boolean containsSolvableFact(AnswerSets answerSets) {
        for (AnswerSet answerSet : answerSets.getAnswersets()) {
            for (Object atom : answerSet.getAtoms()) {
                if (atom instanceof SolvableFact) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica la risolubilità con dettagli
     */
    public CheckResult checkLevelWithDetails(GameState gameState) {
        long startTime = System.currentTimeMillis();

        try {
            boolean solvable = isLevelSolvable(gameState);
            long duration = System.currentTimeMillis() - startTime;

            String status = solvable ? "SATISFIABLE" : "UNSATISFIABLE";
            return new CheckResult(solvable, duration, status, "embASP with DLV2");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new CheckResult(false, duration, "ERROR", e.getMessage());
        }
    }

    /**
     * Verifica se DLV2 è disponibile
     */
    public boolean isSolverAvailable() {
        try {
            // Test semplice per verificare che DLV2 sia accessibile
            ProcessBuilder pb = new ProcessBuilder(DLV2_PATH, "--help");
            Process process = pb.start();
            process.waitFor();
            return process.exitValue() == 0;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Informazioni sul solver
     */
    public String getSolverInfo() {
        if (isSolverAvailable()) {
            return "DLV2 disponibile in: " + DLV2_PATH + "\nUsando embASP per l'integrazione Java-ASP";
        } else {
            return "DLV2 non disponibile. Controlla il path: " + DLV2_PATH;
        }
    }

    // ============================================================================
    // CLASSI per il MAPPING AUTOMATICO embASP
    // ============================================================================

    /**
     * Rappresenta parametri del gioco in ASP
     */
    @it.unical.mat.embasp.languages.asp.Term
    public static class GameParameterFact {
        private String parameter;
        private int value;

        public GameParameterFact(String parameter, int value) {
            this.parameter = parameter;
            this.value = value;
        }

        // Getters necessari per embASP
        public String getParameter() { return parameter; }
        public int getValue() { return value; }

        @Override
        public String toString() {
            return parameter + "(" + value + ")";
        }
    }

    /**
     * Rappresenta un tubo in ASP
     */
    @it.unical.mat.embasp.languages.asp.Term
    public static class TubeFact {
        private int tubeId;

        public TubeFact(int tubeId) {
            this.tubeId = tubeId;
        }

        public int getTubeId() { return tubeId; }

        @Override
        public String toString() {
            return "tube(" + tubeId + ")";
        }
    }

    /**
     * Rappresenta una pallina in ASP
     */
    @it.unical.mat.embasp.languages.asp.Term
    public static class BallFact {
        private int tubeId;
        private int position;
        private String color;

        public BallFact(int tubeId, int position, String color) {
            this.tubeId = tubeId;
            this.position = position;
            this.color = color;
        }

        public int getTubeId() { return tubeId; }
        public int getPosition() { return position; }
        public String getColor() { return color; }

        @Override
        public String toString() {
            return "initial_ball(" + tubeId + ", " + position + ", " + color + ")";
        }
    }

    /**
     * Rappresenta un colore disponibile in ASP
     */
    @it.unical.mat.embasp.languages.asp.Term
    public static class ColorFact {
        private String color;

        public ColorFact(String color) {
            this.color = color;
        }

        public String getColor() { return color; }

        @Override
        public String toString() {
            return "color(" + color + ")";
        }
    }

    /**
     * Rappresenta il risultato "solvable" da ASP
     */
    @it.unical.mat.embasp.languages.asp.Term
    public static class SolvableFact {
        public SolvableFact() {}

        @Override
        public String toString() {
            return "solvable";
        }
    }

    /**
     * Risultato dettagliato del controllo
     */
    public static class CheckResult {
        private final boolean solvable;
        private final long duration;
        private final String status;
        private final String details;

        public CheckResult(boolean solvable, long duration, String status, String details) {
            this.solvable = solvable;
            this.duration = duration;
            this.status = status;
            this.details = details;
        }

        public boolean isSolvable() { return solvable; }
        public long getDuration() { return duration; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }

        @Override
        public String toString() {
            return String.format("CheckResult[solvable=%s, duration=%dms, status=%s, details=%s]",
                    solvable, duration, status, details);
        }
    }
}