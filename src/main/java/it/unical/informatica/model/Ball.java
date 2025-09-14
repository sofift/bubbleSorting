package it.unical.informatica.model;

import java.util.Objects;

public class Ball {
    private final int id;
    private final BallColor color;


    public Ball(int id, BallColor color) {
        this.id = id;
        this.color = color;
    }


    public Ball(BallColor color) {
        this(generateId(), color);
    }

    private static int idCounter = 0;
    private static synchronized int generateId() {
        return ++idCounter;
    }

    public int getId() {
        return id;
    }

    public BallColor getColor() {
        return color;
    }

    public String getColorName() {
        return color.name();
    }


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


    public Ball copy() {
        return new Ball(this.id, this.color);
    }
}