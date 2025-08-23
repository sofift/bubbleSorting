package it.unical.informatica.model;

import java.util.*;

/**
 * Rappresenta un tubo che contiene le palline
 */
public class Tube {

    private final int id;
    private final int capacity;
    private final Stack<Ball> balls;

    public Tube(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.balls = new Stack<>();
    }

    /**
     * Aggiunge una pallina in cima al tubo
     */
    public boolean addBall(Ball ball) {
        if (isFull()) {
            return false;
        }
        balls.push(ball);
        return true;
    }

    /**
     * Rimuove e restituisce la pallina in cima al tubo
     */
    public Ball removeBall() {
        if (isEmpty()) {
            return null;
        }
        return balls.pop();
    }

    /**
     * Restituisce la pallina in cima senza rimuoverla
     */
    public Ball getTopBall() {
        if (isEmpty()) {
            return null;
        }
        return balls.peek();
    }

    /**
     * Verifica se il tubo è vuoto
     */
    public boolean isEmpty() {
        return balls.isEmpty();
    }

    /**
     * Verifica se il tubo è pieno
     */
    public boolean isFull() {
        return balls.size() >= capacity;
    }

    /**
     * Verifica se tutte le palline nel tubo sono dello stesso colore
     */
    public boolean isMonochromatic() {
        if (isEmpty()) {
            return true;
        }

        Ball.Color firstColor = balls.get(0).getColor();
        return balls.stream().allMatch(ball -> ball.getColor() == firstColor);
    }

    /**
     * Verifica se il tubo è completo (pieno e monocromatico)
     */
    public boolean isComplete() {
        return isFull() && isMonochromatic();
    }

    /**
     * Verifica se è possibile spostare una pallina da questo tubo a quello di destinazione
     */
    public boolean canMoveTo(Tube destination) {
        if (this.isEmpty() || destination.isFull()) {
            return false;
        }

        // Se il tubo di destinazione è vuoto, è sempre possibile spostare
        if (destination.isEmpty()) {
            return true;
        }

        // Altrimenti, la pallina in cima deve essere dello stesso colore
        return this.getTopBall().getColor() == destination.getTopBall().getColor();
    }

    public int getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCurrentSize() {
        return balls.size();
    }

    public List<Ball> getBalls() {
        return new ArrayList<>(balls);
    }

    /**
     * Crea una copia del tubo
     */
    public Tube copy() {
        Tube copy = new Tube(this.id, this.capacity);
        for (Ball ball : this.balls) {
            copy.addBall(new Ball(ball.getColor(), ball.getId()));
        }
        return copy;
    }

    @Override
    public String toString() {
        return String.format("Tube{id=%d, capacity=%d, balls=%s}",
                id, capacity, balls);
    }
}