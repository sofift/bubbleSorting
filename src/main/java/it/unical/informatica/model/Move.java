package it.unical.informatica.model;

import java.util.List;
import java.util.Objects;


public class Move {
    private final int fromTubeId;
    private final int toTubeId;
    private final Ball ball;
    private final long timestamp;


    public Move(int fromTubeId, int toTubeId, Ball ball) {
        this.fromTubeId = fromTubeId;
        this.toTubeId = toTubeId;
        this.ball = ball;
        this.timestamp = System.currentTimeMillis();
    }


    public Move(int fromTubeId, int toTubeId) {
        this(fromTubeId, toTubeId, null);
    }

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


    public boolean isValid() {
        return fromTubeId >= 0 && toTubeId >= 0 && fromTubeId != toTubeId;
    }


    public Move getReverse() {
        return new Move(toTubeId, fromTubeId, ball);
    }


    public String toASP() {
        return String.format("move(%d,%d)", fromTubeId, toTubeId);
    }


    public String getDisplayString() {
        String ballInfo = (ball != null) ?
                " (" + ball.getColor().getDisplayName() + ")" : "";
        return String.format("Tubo %d → Tubo %d%s", fromTubeId + 1, toTubeId + 1, ballInfo);
    }

    public int calculateScore(GameState gameState) {
        if (!isValid()) return -1000;

        Tube fromTube = gameState.getTube(fromTubeId);
        Tube toTube = gameState.getTube(toTubeId);

        if (fromTube == null || toTube == null) return -1000;

        int score = 0;

        if (fromTube.getCurrentSize() == 1) {
            score -= 50;
        }

        if (toTube.isEmpty()) {
            score += 30;
        }

        if (!toTube.isEmpty() && toTube.getCurrentSize() == 3) {
            Ball topBall = fromTube.getTopBall();
            if (topBall != null && topBall.sameColor(toTube.getTopBall())) {
                score += 100; // Mossa che completa un tubo
            }
        }

        int consecutiveCount = fromTube.getTopSameColorCount();
        if (!toTube.isEmpty()) {
            Ball fromBall = fromTube.getTopBall();
            Ball toBall = toTube.getTopBall();
            if (fromBall != null && toBall != null && fromBall.sameColor(toBall)) {
                score += consecutiveCount * 20;
            }
        }

        if (fromTube.getCurrentSize() > 1) {
            List<Ball> balls = fromTube.getBallsFromTop();
            if (balls.size() >= 2) {
                Ball top = balls.get(0);
                Ball second = balls.get(1);
                if (top != null && second != null && !top.sameColor(second)) {
                    score += 40;
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