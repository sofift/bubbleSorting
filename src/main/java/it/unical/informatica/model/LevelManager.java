package it.unical.informatica.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestisce la creazione e validazione dei livelli del gioco - VERSIONE embASP
 * Coerente con LevelGenerator.generate(Difficolta) che ritorna un GameState.
 */
public class LevelManager {

    private final EmbAspLevelChecker checker;
    private final Map<String, List<Tube>> cachedLevels;

    public LevelManager() {
        this.checker = new EmbAspLevelChecker();
        this.cachedLevels = new HashMap<>();
        System.out.println("🎯 LevelManager inizializzato con embASP + DLV2 (coerente con LevelGenerator statico)");
    }

    /** Crea un nuovo GameState per il livello specificato (con cache). */
    public GameState createGameState(GameLevel difficulty, int levelNumber) {
        String cacheKey = difficulty.name() + "_" + levelNumber;

        // Cache hit
        if (cachedLevels.containsKey(cacheKey)) {
            List<Tube> cachedTubes = cachedLevels.get(cacheKey);
            return GameState.createFromTubes(difficulty, levelNumber, cachedTubes);
        }

        // Genera e valida
        List<Tube> tubes = generateValidLevel(difficulty, levelNumber);

        // Cache
        cachedLevels.put(cacheKey, copyTubes(tubes));

        return GameState.createFromTubes(difficulty, levelNumber, tubes);
    }

    /** Genera un livello valido e risolvibile, con fallback semplice. */
    private List<Tube> generateValidLevel(GameLevel difficulty, int levelNumber) {
        final int maxAttempts = 10;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                // Usa il LevelGenerator: Difficolta -> GameState -> estraggo tubi
                Difficolta diff = toDifficolta(difficulty);
                GameState generated = LevelGenerator.generate(diff); // <-- static
                List<Tube> tubes = copyTubes(generated.getTubes());   // difensivo

                if (checker.isSolverAvailable()) {
                    GameState testState = GameState.createFromTubes(difficulty, levelNumber, tubes);
                    if (checker.isLevelSolvable(testState)) {
                        System.out.println("✅ Livello valido generato: " + difficulty.getDisplayName() +
                                " L" + levelNumber + " (tentativo " + attempt + ")");
                        return tubes;
                    } else {
                        System.out.println("❌ Livello non risolvibile, rigenerazione… (tentativo " + attempt + ")");
                    }
                } else {
                    System.out.println("⚠️ ASP non disponibile, accetto livello generato");
                    return tubes;
                }
            } catch (Exception e) {
                System.err.println("Errore nella generazione/validazione (tentativo " + attempt + "): " + e.getMessage());
            }
        }

        // Fallback: livello semplice coerente con GameLevel
        System.out.println("🔄 Fallback: genera livello semplice per " + difficulty.getDisplayName());
        return generateSimpleFallback(difficulty);
    }

    /** Livello semplice: primi N tubi riempiti per colore, altri vuoti. */
    private List<Tube> generateSimpleFallback(GameLevel difficulty) {
        int tubeCapacity   = difficulty.getTubeCapacity();
        int numberOfTubes  = difficulty.getNumberOfTubes();
        int ballsPerColor  = difficulty.getBallsPerColor();
        Ball.Color[] colors = difficulty.getAvailableColors();

        List<Tube> tubes = new ArrayList<>(numberOfTubes);
        for (int i = 0; i < numberOfTubes; i++) {
            // id 1-based per coerenza con UI/ASP
            tubes.add(new Tube(i + 1, tubeCapacity));
        }

        int nextBallId = 1;
        // Riempi i primi "colors.length" tubi con le relative palline
        for (int c = 0; c < colors.length && c < numberOfTubes; c++) {
            Tube t = tubes.get(c);
            for (int k = 0; k < ballsPerColor && k < tubeCapacity; k++) {
                t.addBall(new Ball(colors[c], nextBallId++));
            }
        }
        // Gli altri tubi restano vuoti (spazio di manovra)

        return tubes;
    }

    /** Copia profonda della lista di tubi. */
    private List<Tube> copyTubes(List<Tube> original) {
        if (original == null) return List.of();
        return original.stream().map(Tube::copy).collect(Collectors.toList());
    }

    /** Pre-genera tutti i livelli (1..5) per una difficoltà. */
    public void preGenerateLevels(GameLevel difficulty) {
        System.out.println("🚀 Pre-generazione livelli per " + difficulty.getDisplayName());
        for (int level = 1; level <= 5; level++) {
            String cacheKey = difficulty.name() + "_" + level;
            if (!cachedLevels.containsKey(cacheKey)) {
                List<Tube> tubes = generateValidLevel(difficulty, level);
                cachedLevels.put(cacheKey, copyTubes(tubes));
                System.out.println("✅ Pre-generato livello " + level);
            }
        }
        System.out.println("🎯 Pre-generazione completata per " + difficulty.getDisplayName());
    }

    /** Pulisce la cache dei livelli. */
    public void clearCache() {
        cachedLevels.clear();
        System.out.println("🗑️ Cache livelli pulita");
    }

    /** Statistiche sui livelli in cache. */
    public String getCacheStats() {
        int totalLevels = cachedLevels.size();
        Map<GameLevel, Integer> byDiff = new HashMap<>();

        for (String key : cachedLevels.keySet()) {
            String difficultyName = key.split("_")[0];
            GameLevel difficulty = GameLevel.valueOf(difficultyName);
            byDiff.put(difficulty, byDiff.getOrDefault(difficulty, 0) + 1);
        }

        StringBuilder stats = new StringBuilder();
        stats.append("📊 Statistiche Cache Livelli:\n");
        stats.append("Totale livelli in cache: ").append(totalLevels).append("\n");

        for (Map.Entry<GameLevel, Integer> e : byDiff.entrySet()) {
            stats.append("- ").append(e.getKey().getDisplayName())
                    .append(": ").append(e.getValue()).append("/5 livelli\n");
        }
        return stats.toString();
    }

    /** Verifica se il sistema ASP è funzionante. */
    public boolean isAspWorking() {
        return checker.isSolverAvailable();
    }

    /** Info diagnostiche sintetiche. */
    public String getDiagnosticInfo() {
        StringBuilder info = new StringBuilder();
        info.append("🔧 Informazioni Diagnostiche LevelManager:\n\n");

        info.append("ASP Solver Status: ");
        if (checker.isSolverAvailable()) {
            info.append("✅ Disponibile\n");
            info.append("Solver Info: ").append(checker.getSolverInfo()).append("\n");
        } else {
            info.append("❌ Non disponibile\n");
        }

        info.append("\n").append(getCacheStats());
        return info.toString();
    }

    /** Test rapido del sistema. */
    public boolean runSystemTest() {
        try {
            System.out.println("🧪 Eseguendo test del sistema LevelManager...");

            // Generazione tramite LevelGenerator (statico)
            List<Tube> testTubes = copyTubes(LevelGenerator.generate(toDifficolta(GameLevel.EASY)).getTubes());
            if (testTubes.isEmpty()) {
                System.err.println("❌ Test fallito: generazione livello");
                return false;
            }

            // Creazione GameState
            GameState testState = GameState.createFromTubes(GameLevel.EASY, 1, testTubes);
            if (testState == null) {
                System.err.println("❌ Test fallito: creazione GameState");
                return false;
            }

            // ASP (se disponibile)
            if (checker.isSolverAvailable()) {
                boolean solvable = checker.isLevelSolvable(testState);
                System.out.println("🔍 Test ASP: livello " + (solvable ? "risolvibile" : "non risolvibile"));
            }

            System.out.println("✅ Test del sistema completato con successo");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Test del sistema fallito: " + e.getMessage());
            return false;
        }
    }

    /* ===================== UTILITIES ===================== */

    /** Mapping robusto GameLevel → Difficolta (per LevelGenerator). */
    private Difficolta toDifficolta(GameLevel gl) {
        if (gl == null) return Difficolta.FACILE;

        // prova col name()
        String n = gl.name().toLowerCase();
        if (n.contains("easy") || n.contains("facile")) return Difficolta.FACILE;
        if (n.contains("med")  || n.contains("medio"))  return Difficolta.MEDIO;
        if (n.contains("hard") || n.contains("diff"))   return Difficolta.DIFFICILE;

        // prova col display name
        String dn = gl.getDisplayName() != null ? gl.getDisplayName().toLowerCase() : "";
        if (dn.contains("facile") || dn.contains("easy")) return Difficolta.FACILE;
        if (dn.contains("medio")  || dn.contains("medium")) return Difficolta.MEDIO;
        if (dn.contains("difficile") || dn.contains("hard")) return Difficolta.DIFFICILE;

        // fallback
        return Difficolta.FACILE;
    }
}
