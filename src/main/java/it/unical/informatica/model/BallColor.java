package it.unical.informatica.model;

/**
 * Enumerazione che rappresenta i colori disponibili per le palline
 * nel gioco Bubble Sorting.
 */
public enum BallColor {
    RED("#FF4444", "Rosso"),
    BLUE("#4444FF", "Blu"),
    GREEN("#44FF44", "Verde"),
    YELLOW("#FFFF44", "Giallo"),
    ORANGE("#FF8844", "Arancione"),
    PURPLE("#8844FF", "Viola"),
    PINK("#FF44FF", "Rosa");

    private final String hexColor;
    private final String displayName;

    BallColor(String hexColor, String displayName) {
        this.hexColor = hexColor;
        this.displayName = displayName;
    }

    /**
     * Ottiene il codice colore esadecimale
     * @return Codice colore in formato #RRGGBB
     */
    public String getHexColor() {
        return hexColor;
    }

    /**
     * Ottiene il nome visualizzato del colore
     * @return Nome del colore in italiano
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converte una stringa in BallColor
     * @param colorName Nome del colore (case insensitive)
     * @return BallColor corrispondente
     * @throws IllegalArgumentException se il colore non esiste
     */
    public static BallColor fromString(String colorName) {
        if (colorName == null || colorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome colore non può essere null o vuoto");
        }

        try {
            return BallColor.valueOf(colorName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Colore non valido: " + colorName);
        }
    }

    /**
     * Ottiene tutti i colori disponibili per un determinato livello di difficoltà
     * @param difficulty Livello di difficoltà
     * @return Array di colori disponibili
     */
    public static BallColor[] getColorsForDifficulty(GameLevel difficulty) {
        switch (difficulty) {
            case EASY:
                return new BallColor[]{RED, BLUE, GREEN, YELLOW};
            case MEDIUM:
                return new BallColor[]{RED, BLUE, GREEN, YELLOW, ORANGE};
            case HARD:
                return new BallColor[]{RED, BLUE, GREEN, YELLOW, ORANGE, PURPLE, PINK};
            default:
                throw new IllegalArgumentException("Difficoltà non supportata: " + difficulty);
        }
    }

    /**
     * Controlla se il colore è disponibile per la difficoltà specificata
     * @param difficulty Livello di difficoltà
     * @return true se il colore è disponibile
     */
    public boolean isAvailableForDifficulty(GameLevel difficulty) {
        BallColor[] availableColors = getColorsForDifficulty(difficulty);
        for (BallColor color : availableColors) {
            if (color == this) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return displayName;
    }
}