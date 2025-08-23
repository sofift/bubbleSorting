package it.unical.informatica.model;

import it.unical.informatica.asp.EmbAspLevelChecker;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestisce la creazione e validazione dei livelli del gioco - VERSIONE embASP
 */
public class LevelManager {

    private final LevelGenerator generator;
    private final EmbAspLevelChecker checker;
    private final Map<String, List<Tube>> cachedLevels;

    public LevelManager() {
        this.generator = new LevelGenerator();
        this.checker = new EmbAspLevelChecker();
        this.cachedLevels = new HashMap<>();

        System.out.println("🎯 LevelManager inizializzato con embASP + DLV2");
    }

    /**
     * Crea un nuovo GameState per il livello specificato
     */
    public GameState createGameState(GameLevel difficulty, int levelNumber) {
        String cacheKey = difficulty.name() + "_" + levelNumber;

        // Controlla se il livello è già in cache
        if (cachedLevels.containsKey(cacheKey)) {
            List<Tube> cachedTubes = cachedLevels.get(cacheKey);
            return GameState.createFromTubes(difficulty, levelNumber, cachedTubes);
        }

        // Genera un nuovo livello
        List<Tube> tubes = generateValidLevel(difficulty, levelNumber);

        // Cache il livello generato
        cachedLevels.put(cacheKey, copyTubes(tubes));

        return GameState.createFromTubes(difficulty, levelNumber, tubes);
    }

    /**
     * Genera un livello valido e risolvibile
     */
    private List<Tube> generateValidLevel(GameLevel difficulty, int levelNumber) {
        int maxAttempts = 10;
        int attempts = 0;

        while (attempts < maxAttempts) {
            try {
                // Genera il livello
                List<Tube> tubes = generator.generateLevel(difficulty, levelNumber);

                // Verifica se è risolvibile (solo se ASP è disponibile)
                if (checker.isSolverAvailable()) {
                    GameState testState = GameState.createFromTubes(difficulty, levelNumber, tubes);

                    if (checker.isLevelSolvable(testState)) {
                        System.out.println("✅ Livello valido generato: " + difficulty.getDisplayName() +
                                " Livello " + levelNumber + " (tentativo " + (attempts + 1) + ")");
                        return tubes;
                    } else {
                        System.out.println("❌ Livello non risolvibile, rigenerazione...");
                    }
                } else {
                    // Se ASP non è disponibile, accetta il livello generato
                    System.out.println("⚠️ ASP non disponibile, accetto livello generato");
                    return tubes;
                }

            } catch (Exception e) {
                System.err.println("Errore nella generazione del livello: " + e.getMessage());
            }

            attempts++;
        }

        // Fallback: genera un livello semplice
        System.out.println("🔄 Usando livello semplice come fallback");
        return generator.generateSimpleLevel(difficulty);
    }

    /**
     * Crea una copia profonda della lista di tubi
     */
    private List<Tube> copyTubes(List<Tube> original) {
        return original.stream()
                .map(Tube::copy)
                .toList();
    }

    /**
     * Pre-genera tutti i livelli per una difficoltà
     */
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

    /**
     * Pulisce la cache dei livelli
     */
    public void clearCache() {
        cachedLevels.clear();
        System.out.println("🗑️ Cache livelli pulita");
    }

    /**
     * Ottiene statistiche sui livelli in cache
     */
    public String getCacheStats() {
        int totalLevels = cachedLevels.size();
        Map<GameLevel, Integer> levelsByDifficulty = new HashMap<>();

        for (String key : cachedLevels.keySet()) {
            String difficultyName = key.split("_")[0];
            GameLevel difficulty = GameLevel.valueOf(difficultyName);
            levelsByDifficulty.put(difficulty, levelsByDifficulty.getOrDefault(difficulty, 0) + 1);
        }

        StringBuilder stats = new StringBuilder();
        stats.append("📊 Statistiche Cache Livelli:\n");
        stats.append("Totale livelli in cache: ").append(totalLevels).append("\n");

        for (Map.Entry<GameLevel, Integer> entry : levelsByDifficulty.entrySet()) {
            stats.append("- ").append(entry.getKey().getDisplayName())
                    .append(": ").append(entry.getValue()).append("/5 livelli\n");
        }

        return stats.toString();
    }

    /**
     * Verifica se il sistema ASP è funzionante
     */
    public boolean isAspWorking() {
        return checker.isSolverAvailable();
    }

    /**
     * Ottiene informazioni diagnostiche
     */
    public String getDiagnosticInfo() {
        StringBuilder info = new StringBuilder();
        info.append("🔧 Informazioni Diagnostiche LevelManager:\n\n");

        // Informazioni ASP
        info.append("ASP Solver Status: ");
        if (checker.isSolverAvailable()) {
            info.append("✅ Disponibile\n");
            info.append("Solver Info: ").append(checker.getSolverInfo()).append("\n");
        } else {
            info.append("❌ Non disponibile\n");
        }

        // Statistiche cache
        info.append("\n").append(getCacheStats());

        return info.toString();
    }

    /**
     * Test rapido del sistema
     */
    public boolean runSystemTest() {
        try {
            System.out.println("🧪 Eseguendo test del sistema LevelManager...");

            // Test generazione livello
            List<Tube> testTubes = generator.generateLevel(GameLevel.EASY, 1);
            if (testTubes == null || testTubes.isEmpty()) {
                System.err.println("❌ Test fallito: generazione livello");
                return false;
            }

            // Test creazione GameState
            GameState testState = GameState.createFromTubes(GameLevel.EASY, 1, testTubes);
            if (testState == null) {
                System.err.println("❌ Test fallito: creazione GameState");
                return false;
            }

            // Test ASP (se disponibile)
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
}