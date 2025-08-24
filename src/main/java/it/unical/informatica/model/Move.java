package it.unical.informatica.model;

import java.util.List;
import java.util.Objects;

/**
 * Rappresenta una mossa nel gioco Bubble Sorting.
 * Una mossa consiste nello spostare una pallina da un tubo a un altro.
 */
public class Move {
    private final int fromTubeId;
    private final int toTubeId;
    private final Ball ball;
    private final long timestamp;

    /**
     * Costruttore per creare una nuova mossa
     * @param fromTubeId ID del tubo di origine
     * @param toTubeId ID del tubo di destinazione
     * @param ball Pallina che viene spostata
     */
    public Move(int fromTubeId, int toTubeId, Ball ball) {
        this.fromTubeId = fromTubeId;
        this.toTubeId = toTubeId;
        this.ball = ball;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Costruttore semplificato senza pallina specifica
     * @param fromTubeId ID del tubo di origine
     * @param toTubeId ID del tubo di destinazione
     */
    public Move(int fromTubeId, int toTubeId) {
        this(fromTubeId, toTubeId, null);
    }

    // Getters
    public int getFromTubeId() {
        return fromTubeId;
    }

    public int getToTubeId() {
        return toTubeId;
    }

    public Ball getBall() {
        return ball;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Controlla se la mossa è valida (IDs diversi e non negativi)
     * @return true se la mossa è valida
     */
    public boolean isValid() {
        return fromTubeId >= 0 && toTubeId >= 0 && fromTubeId != toTubeId;
    }

    /**
     * Ottiene la mossa inversa (per l'undo)
     * @return Nuova mossa che inverte questa
     */
    public Move getReverse() {
        return new Move(toTubeId, fromTubeId, ball);
    }

    /**
     * Converte la mossa in formato ASP
     * @return Stringa ASP che rappresenta la mossa
     */
    public String toASP() {
        return String.format("move(%d,%d)", fromTubeId, toTubeId);
    }

    /**
     * Converte la mossa in formato per il display
     * @return Stringa leggibile della mossa
     */
    public String getDisplayString() {
        String ballInfo = (ball != null) ?
                " (" + ball.getColor().getDisplayName() + ")" : "";
        return String.format("Tubo %d → Tubo %d%s", fromTubeId + 1, toTubeId + 1, ballInfo);
    }

    /**
     * Calcola un punteggio per questa mossa (per l'AI)
     * Mosse che completano tubi o creano spazio hanno punteggi più alti
     * @param gameState Stato corrente del gioco
     * @return Punteggio della mossa
     */
    public int calculateScore(GameState gameState) {
        if (!isValid()) return -1000;

        Tube fromTube = gameState.getTube(fromTubeId);
        Tube toTube = gameState.getTube(toTubeId);

        if (fromTube == null || toTube == null) return -1000;

        int score = 0;

        // Penalizza se il tubo di origine ha una sola pallina
        if (fromTube.getCurrentSize() == 1) {
            score -= 50;
        }

        // Bonus se stiamo spostando su un tubo vuoto
        if (toTube.isEmpty()) {
            score += 30;
        }

        // Bonus se stiamo completando un colore
        if (!toTube.isEmpty() && toTube.getCurrentSize() == 3) {
            Ball topBall = fromTube.getTopBall();
            if (topBall != null && topBall.sameColor(toTube.getTopBall())) {
                score += 100; // Mossa che completa un tubo
            }
        }

        // Bonus se stiamo spostando palline dello stesso colore consecutive
        int consecutiveCount = fromTube.getTopSameColorCount();
        if (!toTube.isEmpty()) {
            Ball fromBall = fromTube.getTopBall();
            Ball toBall = toTube.getTopBall();
            if (fromBall != null && toBall != null && fromBall.sameColor(toBall)) {
                score += consecutiveCount * 20;
            }
        }

        // Penalizza mosse che bloccano colori diversi sotto
        if (fromTube.getCurrentSize() > 1) {
            List<Ball> balls = fromTube.getBallsFromTop();
            if (balls.size() >= 2) {
                Ball top = balls.get(0);
                Ball second = balls.get(1);
                if (top != null && second != null && !top.sameColor(second)) {
                    score += 40; // Bonus per liberare un colore bloccato
                }
            }
        }

        return score;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return fromTubeId == move.fromTubeId &&
                toTubeId == move.toTubeId &&
                Objects.equals(ball, move.ball);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromTubeId, toTubeId, ball);
    }

    @Override
    public String toString() {
        return String.format("Move[%d→%d, ball=%s]",
                fromTubeId, toTubeId,
                ball != null ? ball.getColor() : "?");
    }
}