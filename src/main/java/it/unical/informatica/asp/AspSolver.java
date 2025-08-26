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
        System.out.println("🤖 Avvio risoluzione ASP (file mode, no stdin)...");
        if (gameState == null) throw new ASPSolverException("GameState nullo");

        // 1) Costruisci i fatti (usa la tua buildFacts o la versione safe)
        final String factsBlock = buildFacts(gameState, horizon);
        System.out.println("===== FACTS (preview) =====\n" + factsBlock + "===========================");

        java.io.File tmpFacts = null;
        try {
            // 2) Scrivi i fatti su file temporaneo .lp
            tmpFacts = java.io.File.createTempFile("asp_facts_", ".lp");
            try (java.io.FileWriter w = new java.io.FileWriter(tmpFacts, java.nio.charset.StandardCharsets.UTF_8)) {
                w.write(factsBlock);
                w.write("\n"); // newline finale
            }
            System.out.println("📝 Fatti scritti in: " + tmpFacts.getAbsolutePath());

            // 3) Crea un handler FRESCO ogni volta (evita accumulo programmi)
            DLV2DesktopService service = new DLV2DesktopService(DLV2_PATH);
            Handler fresh = new DesktopHandler(service);

            // 4) Aggiungi i file: regole + fatti TEMPORANEI
            InputProgram rules = new ASPInputProgram();
            rules.addFilesPath(ASP_RULES_FILE);

            InputProgram facts = new ASPInputProgram();
            facts.addFilesPath(tmpFacts.getAbsolutePath());

            fresh.addProgram(rules);
            fresh.addProgram(facts);

            // ⚠️ Per debug, NIENTE filter (riattivalo dopo)
            // try { fresh.addOption(new OptionDescriptor("--filter=show_move")); } catch (Throwable ignore) {}

            // 5) Esegui
            System.out.println("⚙️ Esecuzione DLV2 (no stdin)...");
            Output output = fresh.startSync();

            // 6) RAW output
            String raw = (output == null) ? "<null Output>" : output.toString();
            System.out.println("===== RAW ASP OUTPUT =====\n" + raw + "\n==========================");

            // 7) Controlli base
            if (raw.toUpperCase().contains("INCOHERENT")) {
                System.out.println("ℹ️ Programma INCOHERENT: nessuna soluzione.");
                return java.util.Collections.emptyList();
            }
            debugPrintAnswerSets(output);

            // 8) Parse robusto

            //List<ShowMove> moves = processResults(output);
            //System.out.println("✅ Mosse parsate: " + moves.size());
            //return moves;

            List<ShowMove> moves = extractOptimalMovesSorted(output);
            System.out.println(moves);
            System.out.println("✅ Mosse parsate: " + moves.size());
            return moves;

        } catch (Throwable t) {
            System.err.println("❌ ERRORE nella risoluzione ASP: " + t);
            t.printStackTrace(System.err);
            throw new ASPSolverException("Errore nella risoluzione: " + t.getMessage(), t);

        } finally {
            // 9) Pulisci il file temporaneo
            if (tmpFacts != null && tmpFacts.exists()) {
                boolean del = tmpFacts.delete();
                if (!del) tmpFacts.deleteOnExit();
            }
        }
    }


    private static String dumpRaw(Output out) {
        if (out == null) return "<null Output>";
        // In EmbASP spesso toString() contiene l'answer set già formattato.
        return out.toString();
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
    /*void addGameFacts(InputProgram facts, GameState gameState, int horizon) throws Exception {
        System.out.println("🔧 Inizio generazione fatti ASP...");

        // 1) capacity(4).
        int capacity = gameState.getLevel().getTubeCapacity(); // dovrebbe essere 4
        facts.addObjectInput(new CapacityFact(capacity));
        System.out.println("   capacity(" + capacity + ").");

        // 2) tube(1). tube(2). ... (espliciti)
        int numTubes = gameState.getTubes().size();
        for (int t = 1; t <= numTubes; t++) {
            facts.addObjectInput(new TubeFact(t));
            System.out.println("   tube(" + t + ").");
        }

        // 3) pos(0). pos(1). pos(2). pos(3).  (espliciti)
        for (int p = 0; p < capacity; p++) {
            facts.addObjectInput(new PosFact(p));
        }
        System.out.println("   pos(0.."+(capacity-1)+") espansi.");

        // 4) step(0). step(1). ... step(horizon).  (espliciti)
        for (int s = 0; s <= horizon; s++) {
            facts.addObjectInput(new StepFact(s));
        }
        System.out.println("   step(0.."+horizon+") espansi.");

        // (facoltativi: se vuoi tenerli per logging/telemetria)
        facts.addObjectInput(new NumTubesFact(numTubes));
        facts.addObjectInput(new HorizonFact(horizon));

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
                facts.addObjectInput(new BallFact(tIndex + 1, p, color));
                totalBalls++;
            }
        }
        System.out.println("   init_ball totali: " + totalBalls);
    }*/
    public static String buildFacts(GameState gs, int horizon) {
        StringBuilder sb = new StringBuilder(1024);

        // Parametri base
        sb.append("capacity(").append(gs.getLevel().getTubeCapacity()).append(").\n");
        sb.append("horizon(").append(horizon).append(").\n");

        // Tubi
        int nTubes = gs.getTubes().size();
        for (int t = 1; t <= nTubes; t++) sb.append("tube(").append(t).append(").\n");

        // Domini espliciti (safe e solver-friendly)
        int cap = gs.getLevel().getTubeCapacity();
        for (int p = 0; p < cap; p++) sb.append("pos(").append(p).append(").\n");
        for (int s = 0; s < horizon; s++) sb.append("step(").append(s).append(").\n");

        // Stato iniziale
        for (int t = 1; t <= nTubes; t++) {
            Tube tube = gs.getTubes().get(t - 1);
            List<Ball> balls = tube.getBalls();

            if (balls == null) continue;                // nessuna pallina
            int size = Math.min(balls.size(), tube.getCapacity()); // non superare la capacity

            for (int p = 0; p < size; p++) {
                Ball b = balls.get(p);
                if (b == null || b.getColor() == null) continue;   // salta eventuali null

                sb.append("init_ball(")
                        .append(t).append(",")
                        .append(p).append(",")
                        .append(b.getColor().name().toLowerCase())
                        .append(").\n");
            }
        }
        return sb.toString();
    }


    /**
     * Processa i risultati del solver ASP
     */
    private static final Pattern MOVE_RE =
            Pattern.compile("show_move\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");

    private List<ShowMove> processResults(Output output) {
        List<ShowMove> moves = new ArrayList<>();

        AnswerSets answerSets = (AnswerSets) output;
        Iterator var6 = answerSets.getAnswersets().iterator();

        while(var6.hasNext()) {
            AnswerSet a = (AnswerSet)var6.next();

            try {
                Iterator var8 = a.getAtoms().iterator();

                while(var8.hasNext()) {
                    Object obj = var8.next();
                    if (obj instanceof ShowMove) {
                        ShowMove move = (ShowMove) obj;
                        moves.add(move);
                        System.out.println(move.getFrom() + move.getTo() + move.getStep());
                    }
                }
            } catch (Exception var12) {
                Exception e = var12;
                e.printStackTrace();
            }
        }


        if (output == null) {
            System.err.println("⚠️ Output nullo dal solver.");
            return moves;
        }

       /* String raw = output.toString();
        if (raw == null) raw = "";
        System.out.println("===== RAW ASP OUTPUT =====\n" + raw + "\n==========================");

        // Caso: DLV2 INCOHERENT / nessun answer set
        if (raw.toUpperCase().contains("INCOHERENT")) {
            System.out.println("ℹ️ Modello INCOERENTE: nessuna soluzione.");
            return moves;
        }

        // Estrai tutti i show_move presenti nel testo (anche se ci sono warning o altro)
        Matcher m = MOVE_RE.matcher(raw);
        while (m.find()) {
            int from = Integer.parseInt(m.group(1));
            int to   = Integer.parseInt(m.group(2));
            int step = Integer.parseInt(m.group(3));
            moves.add(new ShowMove(from, to, step));
        }

        System.out.println("✅ Mosse parsate: " + moves.size());*/
        return moves;
    }


    /** Stampa di debug degli answer set, leggibili */
    private static void debugPrintAnswerSets(Output out) {
        if (!(out instanceof AnswerSets)) {
            System.out.println("⚠️ Output non è AnswerSets. toString(): " + out);
            return;
        }
        AnswerSets as = (AnswerSets) out;

        System.out.println("👉 Answer sets totali: " + as.getAnswersets().size());
        if (as.getOptimalAnswerSets() != null && !as.getOptimalAnswerSets().isEmpty()) {
            System.out.println("👉 Optimal answer sets: " + as.getOptimalAnswerSets().size());
        }

        int idx = 0;
        for (AnswerSet a : as.getAnswersets()) {
            String val = String.valueOf(a.getValue()); // es: "{show_move(2,5,0), show_move(1,3,1), ...}"
            System.out.println("AS[" + (idx++) + "]: " + val);
        }

    }

    private static List<ShowMove> extractOptimalMovesSorted(Output output) {
        List<ShowMove> moves = new ArrayList<>();

        if (!(output instanceof AnswerSets)) {
            // fallback: tenta dal toString (di solito non serve)
            String raw = String.valueOf(output);
            Matcher m = MOVE_RE.matcher(raw);
            while (m.find()) {
                moves.add(new ShowMove(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3))));
            }
            moves.sort(Comparator.comparingInt(ShowMove::getStep));
            return moves;
        }

        AnswerSets as = (AnswerSets) output;

        // Preferisci gli optimal; se vuoti, ripiega su tutti.
        List<AnswerSet> source = as.getOptimalAnswerSets();
        if (source == null || source.isEmpty()) {
            source = as.getAnswersets();
        }
        if (source == null || source.isEmpty()) return moves;

        // Prendi il PRIMO answer set nella lista scelta (di solito l’unico ottimo)
        AnswerSet best = source.get(0);
        String val = String.valueOf(best.getValue()); // es: "{show_move(...), ...}" o "[show_move(...), ...]"
        if (val == null || val.isEmpty()) return moves;

        Matcher m = MOVE_RE.matcher(val);
        while (m.find()) {
            moves.add(new ShowMove(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3))));
        }

        // Ordina per step
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