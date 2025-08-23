package it.unical.informatica.model;

/**
 * Rappresenta una pallina colorata nel gioco
 */
public class Ball {



    /**
     * Enumerazione dei colori disponibili per le palline
     */
    public enum Color {
        RED("Rosso"),
        BLUE("Blu"),
        GREEN("Verde"),
        YELLOW("Giallo"),
        ORANGE("Arancione"),
        PURPLE("Viola"),
        PINK("Rosa");

        private final String displayName;

        Color(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Color color;
    private final int id;

    /**
     * Costruttore
     * @param color Colore della pallina
     * @param id Identificativo univoco della pallina
     */
    public Ball(Color color, int id) {
        this.color = color;
        this.id = id;
    }

    /**
     * Restituisce il colore della pallina
     */
    public Color getColor() {
        return color;
    }

    /**
     * Restituisce l'ID della pallina
     */
    public int getId() {
        return id;
    }

    /**
     * Verifica se due palline hanno lo stesso colore
     */
    public boolean hasSameColor(Ball other) {
        return this.color == other.color;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ball ball = (Ball) obj;
        return id == ball.id && color == ball.color;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(color, id);
    }

    @Override
    public String toString() {
        return String.format("Ball{id=%d, color=%s}", id, color);
    }
}