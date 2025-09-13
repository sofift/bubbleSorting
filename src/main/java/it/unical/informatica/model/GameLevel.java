package it.unical.informatica.model;

/**
 * Enumerazione che rappresenta i livelli di difficoltà del gioco.
 * Ogni difficoltà ha specifiche configurazioni per numero di tubi,
 * colori e tubi vuoti.
 */
public enum GameLevel {
    EASY("Facile", 6, 4, 2, 5),
    MEDIUM("Medio", 7, 5, 2, 5);

    private final String displayName;
    private final int numberOfTubes;
    private final int numberOfColors;
    private final int emptyTubes;
    private final int maxLevels;

    GameLevel(String displayName, int numberOfTubes, int numberOfColors, int emptyTubes, int maxLevels) {
        this.displayName = displayName;
        this.numberOfTubes = numberOfTubes;
        this.numberOfColors = numberOfColors;
        this.emptyTubes = emptyTubes;
        this.maxLevels = maxLevels;
    }

    /**
     * Ottiene il nome visualizzato della difficoltà
     * @return Nome da mostrare all'utente
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Ottiene il numero totale di tubi per questa difficoltà
     * @return Numero di tubi
     */
    public int getNumberOfTubes() {
        return numberOfTubes;
    }

    /**
     * Ottiene il numero di colori diversi per questa difficoltà
     * @return Numero di colori
     */
    public int getNumberOfColors() {
        return numberOfColors;
    }

    /**
     * Ottiene il numero di tubi vuoti all'inizio del gioco
     * @return Numero di tubi vuoti
     */
    public int getEmptyTubes() {
        return emptyTubes;
    }

    /**
     * Ottiene il numero massimo di livelli per questa difficoltà
     * @return Numero massimo di livelli
     */
    public int getMaxLevels() {
        return maxLevels;
    }

    /**
     * Ottiene il numero di tubi con palline all'inizio
     * @return Numero di tubi pieni
     */
    public int getFilledTubes() {
        return numberOfTubes - emptyTubes;
    }

    /**
     * Ottiene la capacità di ogni tubo (numero di palline per tubo)
     * @return Capacità del tubo
     */
    public int getTubeCapacity() {
        return 4; // Standard per tutti i livelli
    }

    /**
     * Ottiene il numero totale di palline nel gioco
     * @return Numero totale di palline
     */
    public int getTotalBalls() {
        return numberOfColors * getTubeCapacity();
    }

    /**
     * Controlla se un numero di livello è valido per questa difficoltà
     * @param levelNumber Numero del livello da controllare
     * @return true se il livello è valido
     */
    public boolean isValidLevelNumber(int levelNumber) {
        return levelNumber >= 1 && levelNumber <= maxLevels;
    }

    /**
     * Ottiene la difficoltà successiva
     * @return Difficoltà successiva o null se è già la più alta
     */
    public GameLevel getNextLevel() {
        switch (this) {
            case EASY: return MEDIUM;
            case MEDIUM: return null;
            default: return null;
        }
    }

    /**
     * Ottiene la difficoltà precedente
     * @return Difficoltà precedente o null se è già la più bassa
     */
    public GameLevel getPreviousLevel() {
        switch (this) {
            case MEDIUM: return EASY;
            case EASY: return null; // Nessun livello precedente
            default: return null;
        }
    }

    /**
     * Converte una stringa in GameLevel
     * @param levelName Nome della difficoltà
     * @return GameLevel corrispondente
     * @throws IllegalArgumentException se la difficoltà non esiste
     */
    public static GameLevel fromString(String levelName) {
        if (levelName == null || levelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome difficoltà non può essere null o vuoto");
        }

        try {
            return GameLevel.valueOf(levelName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Prova con il display name
            for (GameLevel level : values()) {
                if (level.displayName.equalsIgnoreCase(levelName.trim())) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Difficoltà non valida: " + levelName);
        }
    }

    @Override
    public String toString() {
        return displayName + " (" + numberOfTubes + " tubi, " + numberOfColors + " colori)";
    }
}