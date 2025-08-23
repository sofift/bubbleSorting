package it.unical.informatica;

import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;

import java.util.*;

public class LevelGeneratorAndCheck {

    // Parametri livello
    private int numTubi;
    private int height;
    private int numColori;
    private int minEmpty;

    public LevelGeneratorAndCheck(int numTubi, int height, int numColori, int minEmpty) {
        this.numTubi = numTubi;
        this.height = height;
        this.numColori = numColori;
        this.minEmpty = minEmpty;
    }

    public List<List<Integer>> generaLivello() {
        // Crea lista palline
        List<Integer> balls = new ArrayList<>();
        for (int c = 1; c <= numColori; c++) {
            for (int i = 0; i < height; i++) {
                balls.add(c);
            }
        }

        // Mischia
        Collections.shuffle(balls);

        // Distribuzione nei tubi
        List<List<Integer>> tubes = new ArrayList<>();
        int idx = 0;
        for (int t = 1; t <= numTubi; t++) {
            List<Integer> tubo = new ArrayList<>();
            if (t <= numTubi - minEmpty) {
                for (int p = 0; p < height; p++) {
                    tubo.add(balls.get(idx++));
                }
            }
            tubes.add(tubo);
        }
        return tubes;
    }

    public String toAspFacts(List<List<Integer>> tubes) {
        StringBuilder sb = new StringBuilder();
        sb.append("tube(1..").append(numTubi).append(").\n");
        sb.append("height(").append(height).append(").\n");
        sb.append("min_empty(").append(minEmpty).append(").\n");
        sb.append("color(1..").append(numColori).append(").\n");

        for (int t = 0; t < tubes.size(); t++) {
            List<Integer> tubo = tubes.get(t);
            for (int p = 0; p < tubo.size(); p++) {
                sb.append("ball(")
                  .append(t + 1).append(",") // tubo
                  .append(p + 1).append(",") // posizione
                  .append(tubo.get(p))       // colore
                  .append(").\n");
            }
        }
        return sb.toString();
    }

    public void checkConSolver(String aspFacts, String aspProgramPath) throws Exception {
        // Path al binario di DLV2
        String dlvPath = "libs/dlv-2.1.2-win64.exe"; // Modifica se diverso

        Handler handler = new DesktopHandler(
                new DLV2DesktopService(dlvPath));

        // Carica il programma logico di check
        InputProgram facts = new it.unical.mat.embasp.base.InputProgram();
        facts.addProgram(aspFacts);

        InputProgram checkProgram = new it.unical.mat.embasp.base.InputProgram();
        checkProgram.addFilesPath(aspProgramPath);

        handler.addProgram(facts);
        handler.addProgram(checkProgram);

        Output o = handler.startSync();
        System.out.println("=== RISULTATO SOLVER ===");
        System.out.println(o.getOutput());
    }

    public static void main(String[] args) throws Exception {
        LevelGeneratorAndCheck gen = new LevelGeneratorAndCheck(
                5, // numTubi
                4, // height
                3, // numColori
                2  // minEmpty
        );

        // 1. Genera livello
        List<List<Integer>> livello = gen.generaLivello();

        // 2. Converti in fatti ASP
        String facts = gen.toAspFacts(livello);
        System.out.println("=== LIVELLO GENERATO ===");
        System.out.println(facts);

        // 3. Passa al solver con il programma di check
        String bubbleSortProgram = "src/main/resources/asp/bubble_sort.lp";
        gen.checkConSolver(facts, bubbleSortProgram);
    }
}
