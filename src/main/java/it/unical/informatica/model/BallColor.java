package it.unical.informatica.model;


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

    public String getHexColor() {
        return hexColor;
    }


    public String getDisplayName() {
        return displayName;
    }

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


    public static BallColor[] getColorsForDifficulty(GameLevel difficulty) {
        switch (difficulty) {
            case EASY:
                return new BallColor[]{RED, BLUE, GREEN, YELLOW};
            case MEDIUM:
                return new BallColor[]{RED, BLUE, GREEN, YELLOW, ORANGE};
            default:
                throw new IllegalArgumentException("Difficoltà non supportata: " + difficulty);
        }
    }


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