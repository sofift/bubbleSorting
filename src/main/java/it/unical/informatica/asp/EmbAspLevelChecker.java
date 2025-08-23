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
import jdk.jfr.Registered;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Classe per verificare la risolubilità usando embASP con DLV2 - VERSIONE CORRETTA
 */
public class EmbAspLevelChecker {

    private static final String DLV2_PATH = "libs/dlv-2.1.2-win64.exe";
    private static final String LEVEL_CHECKER_PROGRAM = "src/main/resources/asp/level_checker.lp";

    private final Handler handler;
    private final ASPMapper mapper;

    public EmbAspLevelChecker() {
        // Inizializza handler per DLV2
        this.handler = new DesktopHandler(new DLV2DesktopService(DLV2_PATH));

        // Inizializza mapper usando getInstance()
        this.mapper = ASPMapper.getInstance();
        setupMapper();
    }

    /**
     * Configura il mapper per la conversione automatica Java ↔ ASP
     */
    private void setupMapper() {
        try {
            // Registra le classi del dominio per il mapping automatico
            mapper.registerClass(TubeFact.class);
            mapper.registerClass(BallFact.class);
            mapper.registerClass(ColorFact.class);
            mapper.registerClass(SolvableFact.class);
        } catch (ObjectNotValidException | IllegalAnnotationException e) {
            System.err.println("Errore nel setup mapper: " + e.getMessage());
        }
    }

    /**
     * Verifica se un livello è risolvibile usando embASP
     */
    public boolean isLevelSolvable(GameState gameState) {
        try {
            // Crea il programma ASP
            InputProgram program = new ASPInputProgram();
            program.addFilesPath(LEVEL_CHECKER_PROGRAM);

            // Converti GameState in fatti ASP e aggiungili al programma
            convertAndAddGameStateFacts(program, gameState);

            // Aggiungi il programma al handler
            handler.addProgram(program);

            // ✅ CORREZIONE: startSync() senza parametri
            Output output = handler.startSync();

            // Converte l'output in AnswerSets
            AnswerSets answerSets = (AnswerSets) output;

            // Verifica se esiste una soluzione
            return containsSolvableFact(answerSets);

        } catch (Exception e) {
            System.err.println("Errore nella verifica con embASP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Converte GameState in fatti ASP e li aggiunge al programma
     */
    private void convertAndAddGameStateFacts(InputProgram program, GameState gameState) throws Exception {
        // Parametri del gioco
        program.addObjectInput(new NumTubesFact(gameState.getNumberOfTubes()));
        program.addObjectInput(new TubeCapacityFact(gameState.getTubeCapacity()));
        program.addObjectInput(new NumColorsFact(gameState.getNumberOfColors()));

        // Colori utilizzati
        Set<String> usedColors = new HashSet<>();

        // Stato dei tubi
        for (int tubeIndex = 0; tubeIndex < gameState.getNumberOfTubes(); tubeIndex++) {
            Tube tube = gameState.getTube(tubeIndex);
            program.addObjectInput(new TubeFact(tubeIndex));

            List<Ball> balls = tube.getBalls();
            for (int pos = 0; pos < balls.size(); pos++) {
                Ball ball = balls.get(pos);
                String colorName = ball.getColor().name().toLowerCase();

                program.addObjectInput(new BallFact(tubeIndex, pos, colorName));
                usedColors.add(colorName);
            }
        }

        // Aggiungi i colori utilizzati
        for (String color : usedColors) {
            program.addObjectInput(new ColorFact(color));
        }
    }

    /**
     * Verifica se gli AnswerSets contengono il fatto "solvable"
     */
    private boolean containsSolvableFact(AnswerSets answerSets) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
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
    // CLASSI per il MAPPING AUTOMATICO embASP - VERSIONE CORRETTA
    // ============================================================================

    /**
     * ✅ MAPPING CORRETTO: Id + Param
     */
    @Id("num_tubes")
    public static class NumTubesFact {
        @Param(0)
        private int value;

        public NumTubesFact(int value) {
            this.value = value;
        }

        public int getValue() { return value; }
    }

    @Id("tube_capacity")
    public static class TubeCapacityFact {
        @Param(0)
        private int value;

        public TubeCapacityFact(int value) {
            this.value = value;
        }
        public int getValue() { return value; }
    }

    @Id("num_colors")
    public static class NumColorsFact {
       @Param(0)
        private int value;

        public NumColorsFact(int value) {
            this.value = value;
        }

        public int getValue() { return value; }
    }

    /**
     * Rappresenta un tubo in ASP
     */
    @Id("tube")
    public static class TubeFact {

        @Param(0)
        private int tubeId;

        public TubeFact(int tubeId) {
            this.tubeId = tubeId;
        }

        public int getTubeId() { return tubeId; }
    }

    /**
     * Rappresenta una pallina in ASP
     */
    @Id("initial_ball")
    public static class BallFact {
        @Param(0)
        private int tubeId;

        @Param(1)
        private int position;

        @Param(2)
        private String color;

        public BallFact(int tubeId, int position, String color) {
            this.tubeId = tubeId;
            this.position = position;
            this.color = color;
        }

        public int getTubeId() { return tubeId; }
        public int getPosition() { return position; }
        public String getColor() { return color; }
    }

    /**
     * Rappresenta un colore disponibile in ASP
     */
    @Id("color")
    public static class ColorFact {


        @it.unical.mat.embasp.languages.Param(0)
        private String color;

        public ColorFact(String color) {
            this.color = color;
        }

        public String getColor() { return color; }
    }

    /**
     * Rappresenta il risultato "solvable" da ASP
     */
    @Id("solvable")
    public static class SolvableFact {

        public SolvableFact() {}

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