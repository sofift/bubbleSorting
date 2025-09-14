package it.unical.informatica.controller;

import it.unical.informatica.model.GameLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Gestisce le preferenze del gioco e il progresso del giocatore
 */
public class GamePreferences {

    private static final String LEVEL_COMPLETED_PREFIX = "level_completed_";
    private static final String LEVEL_MOVES_PREFIX = "level_moves_";
    private static final String LEVEL_STARS_PREFIX = "level_stars_";
    private static final String SOUND_ENABLED = "sound_enabled";
    private static final String ANIMATIONS_ENABLED = "animations_enabled";
    private static final String SHOW_HINTS = "show_hints";

    private final Preferences preferences;
    private static GamePreferences instance;

    // Cache per migliorare le prestazioni
    private final Map<String, Boolean> completionCache;
    private final Map<String, Integer> movesCache;
    private final Map<String, Integer> starsCache;

    private GamePreferences() {
        preferences = Preferences.userNodeForPackage(GamePreferences.class);
        completionCache = new HashMap<>();
        movesCache = new HashMap<>();
        starsCache = new HashMap<>();

        loadCache();
    }

    /**
     * Ottiene l'istanza singleton
     */
    public static GamePreferences getInstance() {
        if (instance == null) {
            instance = new GamePreferences();
        }
        return instance;
    }

    /**
     * Carica i dati nella cache
     */
    private void loadCache() {
        for (GameLevel level : GameLevel.values()) {
            for (int levelNumber = 1; levelNumber <= 5; levelNumber++) {
                String key = getLevelKey(level, levelNumber);

                completionCache.put(key,
                        preferences.getBoolean(LEVEL_COMPLETED_PREFIX + key, false));
                movesCache.put(key,
                        preferences.getInt(LEVEL_MOVES_PREFIX + key, Integer.MAX_VALUE));
                starsCache.put(key,
                        preferences.getInt(LEVEL_STARS_PREFIX + key, 0));
            }
        }
    }

    /**
     * Genera una chiave unica per un livello
     */
    private String getLevelKey(GameLevel level, int levelNumber) {
        return level.name().toLowerCase() + "_" + levelNumber;
    }

    /**
     * Segna un livello come completato
     */
    public void setLevelCompleted(GameLevel level, int levelNumber, int moves) {
        String key = getLevelKey(level, levelNumber);

        // Aggiorna solo se è un miglioramento
        int currentBestMoves = getBestMoves(level, levelNumber);
        if (moves < currentBestMoves) {
            completionCache.put(key, true);
            movesCache.put(key, moves);

            // Calcola le stelle basate sul numero di mosse
            int stars = calculateStars(level, moves);
            starsCache.put(key, Math.max(stars, getStars(level, levelNumber)));

            // Salva nelle preferenze
            preferences.putBoolean(LEVEL_COMPLETED_PREFIX + key, true);
            preferences.putInt(LEVEL_MOVES_PREFIX + key, moves);
            preferences.putInt(LEVEL_STARS_PREFIX + key, getStars(level, levelNumber));
        }
    }

    /**
     * Verifica se un livello è stato completato
     */
    public boolean isLevelCompleted(GameLevel level, int levelNumber) {
        String key = getLevelKey(level, levelNumber);
        return completionCache.getOrDefault(key, false);
    }

    /**
     * Ottiene il miglior numero di mosse per un livello
     */
    public int getBestMoves(GameLevel level, int levelNumber) {
        String key = getLevelKey(level, levelNumber);
        return movesCache.getOrDefault(key, Integer.MAX_VALUE);
    }

    /**
     * Ottiene il numero di stelle per un livello
     */
    public int getStars(GameLevel level, int levelNumber) {
        String key = getLevelKey(level, levelNumber);
        return starsCache.getOrDefault(key, 0);
    }

    /**
     * Calcola il numero di stelle basato sul numero di mosse
     */
    private int calculateStars(GameLevel level, int moves) {
        // Calcolo basato sulla difficoltà e numero ottimale di mosse
        int optimalMoves = getOptimalMoves(level);

        if (moves <= optimalMoves) {
            return 3; // Perfetto
        } else if (moves <= optimalMoves * 1.5) {
            return 2; // Buono
        } else if (moves <= optimalMoves * 2) {
            return 1; // Sufficiente
        } else {
            return 0; // Completato ma non efficiente
        }
    }

    /**
     * Ottiene il numero ottimale di mosse per un livello (stima)
     */
    private int getOptimalMoves(GameLevel level) {
        return switch (level) {
            case EASY -> 15;
            case MEDIUM -> 25;
        };
    }

    /**
     * Verifica se un livello è sbloccato
     */
    public boolean isLevelUnlocked(GameLevel level, int levelNumber) {
        // Il primo livello è sempre sbloccato
        if (levelNumber == 1) {
            return true;
        }

        // Gli altri livelli si sbloccano completando il precedente
        return isLevelCompleted(level, levelNumber - 1);
    }

    /**
     * Calcola il progresso totale per una difficoltà
     */
    public double getProgressForDifficulty(GameLevel level) {
        int completedLevels = 0;
        for (int i = 1; i <= 5; i++) {
            if (isLevelCompleted(level, i)) {
                completedLevels++;
            }
        }
        return (double) completedLevels / 5.0 * 100.0;
    }

    /**
     * Calcola il progresso totale del gioco
     */
    public double getTotalProgress() {
        int totalCompleted = 0;
        int totalLevels = GameLevel.values().length * 5;

        for (GameLevel level : GameLevel.values()) {
            for (int i = 1; i <= 5; i++) {
                if (isLevelCompleted(level, i)) {
                    totalCompleted++;
                }
            }
        }

        return (double) totalCompleted / totalLevels * 100.0;
    }

    // Metodi per le preferenze dell'interfaccia

    public boolean isSoundEnabled() {
        return preferences.getBoolean(SOUND_ENABLED, true);
    }

    public void setSoundEnabled(boolean enabled) {
        preferences.putBoolean(SOUND_ENABLED, enabled);
    }

    public boolean isAnimationsEnabled() {
        return preferences.getBoolean(ANIMATIONS_ENABLED, true);
    }

    public void setAnimationsEnabled(boolean enabled) {
        preferences.putBoolean(ANIMATIONS_ENABLED, enabled);
    }

    public boolean isShowHints() {
        return preferences.getBoolean(SHOW_HINTS, true);
    }

    public void setShowHints(boolean show) {
        preferences.putBoolean(SHOW_HINTS, show);
    }

    /**
     * Resetta tutto il progresso del gioco
     */
    public void resetAllProgress() {
        try {
            preferences.clear();
            completionCache.clear();
            movesCache.clear();
            starsCache.clear();
            loadCache();
        } catch (Exception e) {
            System.err.println("Errore nel reset del progresso: " + e.getMessage());
        }
    }

    /**
     * Resetta il progresso per una specifica difficoltà
     */
    public void resetProgress(GameLevel level) {
        for (int i = 1; i <= 5; i++) {
            String key = getLevelKey(level, i);

            preferences.remove(LEVEL_COMPLETED_PREFIX + key);
            preferences.remove(LEVEL_MOVES_PREFIX + key);
            preferences.remove(LEVEL_STARS_PREFIX + key);

            completionCache.put(key, false);
            movesCache.put(key, Integer.MAX_VALUE);
            starsCache.put(key, 0);
        }
    }

    /**
     * Ottiene statistiche dettagliate per il debug
     */
    public Map<String, Object> getDebugInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("total_progress", getTotalProgress());
        info.put("sound_enabled", isSoundEnabled());
        info.put("animations_enabled", isAnimationsEnabled());
        info.put("show_hints", isShowHints());

        for (GameLevel level : GameLevel.values()) {
            info.put(level.name().toLowerCase() + "_progress", getProgressForDifficulty(level));
        }

        return info;
    }
}