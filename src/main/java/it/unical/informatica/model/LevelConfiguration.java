package it.unical.informatica.model;

/**
 * Configurazione per i parametri dei livelli del gioco
 */
public class LevelConfiguration {

    private final GameLevel difficulty;
    private final int numberOfTubes;
    private final int numberOfColors;
    private final int tubeCapacity;
    private final int emptyTubes;

    public LevelConfiguration(GameLevel difficulty) {
        this.difficulty = difficulty;

        // Configurazione parametri in base alla difficoltà
        switch (difficulty) {
            case EASY:
                this.numberOfTubes = 6;
                this.numberOfColors = 4;
                this.tubeCapacity = 4;
                this.emptyTubes = 2;
                break;
            case MEDIUM:
                this.numberOfTubes = 7;
                this.numberOfColors = 5;
                this.tubeCapacity = 4;
                this.emptyTubes = 2;
                break;
            case HARD:
                this.numberOfTubes = 9;
                this.numberOfColors = 7;
                this.tubeCapacity = 4;
                this.emptyTubes = 2;
                break;
            default:
                throw new IllegalArgumentException("Difficoltà non supportata: " + difficulty);
        }
    }

    // Getters
    public GameLevel getDifficulty() {
        return difficulty;
    }

    public int getNumberOfTubes() {
        return numberOfTubes;
    }

    public int getNumberOfColors() {
        return numberOfColors;
    }

    public int getTubeCapacity() {
        return tubeCapacity;
    }

    public int getEmptyTubes() {
        return emptyTubes;
    }

    public int getFilledTubes() {
        return numberOfTubes - emptyTubes;
    }

    public int getTotalBalls() {
        return numberOfColors * tubeCapacity;
    }

    /**
     * Calcola la complessità teorica del livello
     */
    public int getComplexityScore() {
        return numberOfTubes * numberOfColors + (tubeCapacity - 2) * 10;
    }

    /**
     * Verifica se la configurazione è valida
     */
    public boolean isValid() {
        // Deve esserci spazio sufficiente per tutti i colori
        int totalCapacity = numberOfTubes * tubeCapacity;
        int totalBalls = numberOfColors * tubeCapacity;

        return totalCapacity >= totalBalls &&
                emptyTubes >= 1 &&
                emptyTubes < numberOfTubes &&
                numberOfColors > 0 &&
                tubeCapacity >= 2;
    }

    @Override
    public String toString() {
        return String.format("LevelConfig[%s: %d tubi, %d colori, capacità=%d, vuoti=%d]",
                difficulty.getDisplayName(), numberOfTubes, numberOfColors,
                tubeCapacity, emptyTubes);
    }
}