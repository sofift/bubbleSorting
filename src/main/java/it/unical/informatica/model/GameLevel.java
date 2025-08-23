package it.unical.informatica.model;

/**
 * Enumeration che definisce i diversi livelli di difficoltà del gioco
 */
public enum GameLevel {

    EASY(1, "Facile", 6, 4, 4),      // 6 tubi, 4 colori, 4 palline per colore
    MEDIUM(2, "Medio", 7, 5, 4),     // 7 tubi, 5 colori, 4 palline per colore
    HARD(3, "Difficile", 9, 7, 4);   // 9 tubi, 7 colori, 4 palline per colore

    private final int levelNumber;
    private final String displayName;
    private final int numberOfTubes;
    private final int numberOfColors;
    private final int ballsPerColor;
    private final int tubeCapacity;

    GameLevel(int levelNumber, String displayName, int numberOfTubes,
              int numberOfColors, int ballsPerColor) {
        this.levelNumber = levelNumber;
        this.displayName = displayName;
        this.numberOfTubes = numberOfTubes;
        this.numberOfColors = numberOfColors;
        this.ballsPerColor = ballsPerColor;
        this.tubeCapacity = ballsPerColor; // Capacità uguale al numero di palline per colore
    }

    public int getLevelNumber() {
        return levelNumber;
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

    public int getBallsPerColor() {
        return ballsPerColor;
    }

    public int getTubeCapacity() {
        return tubeCapacity;
    }

    /**
     * Calcola il numero di tubi vuoti (per i movimenti)
     */
    public int getEmptyTubes() {
        return numberOfTubes - numberOfColors;
    }

    /**
     * Calcola il numero totale di palline nel gioco
     */
    public int getTotalBalls() {
        return numberOfColors * ballsPerColor;
    }

    /**
     * Restituisce i colori disponibili per questo livello
     */
    public Ball.Color[] getAvailableColors() {
        Ball.Color[] allColors = Ball.Color.values();
        Ball.Color[] levelColors = new Ball.Color[numberOfColors];
        System.arraycopy(allColors, 0, levelColors, 0, numberOfColors);
        return levelColors;
    }

    @Override
    public String toString() {
        return String.format("%s (%d tubi, %d colori)",
                displayName, numberOfTubes, numberOfColors);
    }
}