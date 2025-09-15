package it.unical.informatica.model;

import java.util.*;

public class Tube {
    private final int id;
    private final int capacity;
    private final Stack<Ball> balls;


    public Tube(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.balls = new Stack<>();
    }


    public Tube(int id) {
        this(id, 4);
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

    public int getEmptySpaces() {
        return capacity - balls.size();
    }

    public boolean isEmpty() {
        return balls.isEmpty();
    }


    public boolean isFull() {
        return balls.size() >= capacity;
    }


    public Ball getTopBall() {
        return isEmpty() ? null : balls.peek();
    }


    public List<Ball> getBalls() {
        return new ArrayList<>(balls);
    }


    public List<Ball> getBallsFromTop() {
        List<Ball> result = new ArrayList<>(balls);
        Collections.reverse(result);
        return result;
    }


    public boolean canAddBall(Ball ball) {
        if (ball == null || isFull()) {
            return false;
        }

        if (isEmpty()) {
            return true;
        }

        Ball topBall = getTopBall();
        return topBall.sameColor(ball);
    }


    public boolean addBallForLoading(Ball ball) {
        if (ball == null || isFull()) {
            return false;
        }

        balls.push(ball);
        return true;
    }


    public boolean addBall(Ball ball) {
        if (!canAddBall(ball)) {
            return false;
        }

        balls.push(ball);
        return true;
    }


    public Ball removeBall() {
        return isEmpty() ? null : balls.pop();
    }


    public boolean isCompleted() {
        if (isEmpty()) {
            return true;
        }

        if (balls.size() != capacity) {
            return false;
        }

        BallColor firstColor = balls.firstElement().getColor();
        return balls.stream().allMatch(ball -> ball.getColor() == firstColor);
    }


    public BallColor getDominantColor() {
        if (isEmpty()) {
            return null;
        }

        Map<BallColor, Integer> colorCount = new HashMap<>();
        for (Ball ball : balls) {
            colorCount.put(ball.getColor(), colorCount.getOrDefault(ball.getColor(), 0) + 1);
        }

        return colorCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }


    public int getTopSameColorCount() {
        if (isEmpty()) {
            return 0;
        }

        BallColor topColor = getTopBall().getColor();
        int count = 0;

        List<Ball> ballsFromTop = getBallsFromTop();
        for (Ball ball : ballsFromTop) {
            if (ball.getColor() == topColor) {
                count++;
            } else {
                break;
            }
        }

        return count;
    }


    public void clear() {
        balls.clear();
    }


    public Tube copy() {
        Tube copyTube = new Tube(this.id, this.capacity);
        for (Ball ball : this.balls) {
            copyTube.balls.push(ball.copy());
        }
        return copyTube;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tube tube = (Tube) obj;
        return id == tube.id && capacity == tube.capacity && Objects.equals(balls, tube.balls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, capacity, balls);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tube[id=").append(id)
                .append(", capacity=").append(capacity)
                .append(", size=").append(getCurrentSize())
                .append(", balls=[");

        for (int i = 0; i < balls.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(balls.get(i).getColor());
        }
        sb.append("]]");
        return sb.toString();
    }
}