package it.unical.informatica.model;

import java.util.Objects;

/**
 * Rappresenta una pallina colorata nel gioco Bubble Sorting.
 * Ogni pallina ha un colore e un ID univoco.
 */
public class Ball {
    private final int id;
    private final BallColor color;

    /**
     * Costruttore per creare una nuova pallina
     * @param id ID univoco della pallina
     * @param color Colore della pallina
     */
    public Ball(int id, BallColor color) {
        this.id = id;
        this.color = color;
    }

    /**
     * Costruttore con solo il colore (ID generato automaticamente)
     * @param color Colore della pallina
     */
    public Ball(BallColor color) {
        this(generateId(), color);
    }

    private static int idCounter = 0;
    private static synchronized int generateId() {
        return ++idCounter;
    }

    // Getters
    public int getId() {
        return id;
    }

    public BallColor getColor() {
        return color;
    }

    public String getColorName() {
        return color.name();
    }

    /**
     * Controlla se due palline hanno lo stesso colore
     * @param other Altra pallina da confrontare
     * @return true se hanno lo stesso colore
     */
    public boolean sameColor(Ball other) {
        return other != null && this.color == other.color;
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
        return Objects.hash(id, color);
    }

    @Override
    public String toString() {
        return String.format("Ball[id=%d, color=%s]", id, color);
    }

    /**
     * Crea una copia della pallina
     * @return Nuova istanza di Ball con gli stessi attributi
     */
    public Ball copy() {
        return new Ball(this.id, this.color);
    }
}