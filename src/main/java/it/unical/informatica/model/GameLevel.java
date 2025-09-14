package it.unical.informatica.model;


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


    public String getDisplayName() {
        return displayName;
    }


    public int getNumberOfTubes() {
        return numberOfTubes;
    }


    public int getNumberOfColors() {
        return numberOfColors;
    }


    public int getEmptyTubes() {
        return emptyTubes;
    }


    public int getMaxLevels() {
        return maxLevels;
    }


    public int getFilledTubes() {
        return numberOfTubes - emptyTubes;
    }

    public int getTubeCapacity() {
        return 4; // Standard per tutti i livelli
    }

    public int getTotalBalls() {
        return numberOfColors * getTubeCapacity();
    }


    public boolean isValidLevelNumber(int levelNumber) {
        return levelNumber >= 1 && levelNumber <= maxLevels;
    }


    public GameLevel getNextLevel() {
        switch (this) {
            case EASY: return MEDIUM;
            case MEDIUM: return null;
            default: return null;
        }
    }


    public GameLevel getPreviousLevel() {
        switch (this) {
            case MEDIUM: return EASY;
            case EASY: return null; // Nessun livello precedente
            default: return null;
        }
    }


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