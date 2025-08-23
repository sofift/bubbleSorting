package it.unical.informatica.model;

import java.util.*;

/**
 * Generatore semplice di livelli per il gioco Bubble Sorting
 */
public class LevelGenerator {

    private final Random random;

    public LevelGenerator() {
        this.random = new Random();
    }

    /**
     * Genera un livello deterministico basato su difficoltà e numero livello
     */
    public List<Tube> generateLevel(GameLevel difficulty, int levelNumber) {
        // Seed deterministico per ogni livello
        long seed = (difficulty.name().hashCode() * 31L) + levelNumber;
        random.setSeed(seed);

        // Crea i tubi vuoti
        List<Tube> tubes = new ArrayList<>();
        for (int i = 0; i < difficulty.getNumberOfTubes(); i++) {
            tubes.add(new Tube(i, difficulty.getTubeCapacity()));
        }

        // Crea tutte le palline
        List<Ball> allBalls = createAllBalls(difficulty);

        // Mescola le palline
        Collections.shuffle(allBalls, random);

        // Distribuisce le palline (lascia alcuni tubi vuoti)
        int tubesWithBalls = difficulty.getNumberOfColors();
        distributeBalls(tubes, allBalls, tubesWithBalls, levelNumber);

        return tubes;
    }

    /**
     * Crea tutte le palline necessarie per il livello
     */
    private List<Ball> createAllBalls(GameLevel difficulty) {
        List<Ball> balls = new ArrayList<>();
        Ball.Color[] colors = difficulty.getAvailableColors();
        int ballId = 0;

        // Crea le palline per ogni colore
        for (int colorIndex = 0; colorIndex < difficulty.getNumberOfColors(); colorIndex++) {
            Ball.Color color = colors[colorIndex];
            for (int i = 0; i < difficulty.getBallsPerColor(); i++) {
                balls.add(new Ball(color, ballId++));
            }
        }

        return balls;
    }

    /**
     * Distribuisce le palline nei tubi
     */
    private void distributeBalls(List<Tube> tubes, List<Ball> balls, int tubesWithBalls, int levelNumber) {
        int ballIndex = 0;

        // Calcola la complessità del livello (più alto = più mescolato)
        double complexity = 0.3 + (levelNumber * 0.15); // da 0.45 a 1.05

        for (int tubeIndex = 0; tubeIndex < tubesWithBalls && ballIndex < balls.size(); tubeIndex++) {
            Tube tube = tubes.get(tubeIndex);

            // Determina quante palline mettere in questo tubo
            int ballsForThisTube = calculateBallsForTube(ballIndex, balls.size(),
                    tubesWithBalls - tubeIndex,
                    tube.getCapacity(), complexity);

            // Aggiungi le palline al tubo
            for (int i = 0; i < ballsForThisTube && ballIndex < balls.size(); i++) {
                tube.addBall(balls.get(ballIndex++));
            }
        }
    }

    /**
     * Calcola quante palline mettere in un tubo
     */
    private int calculateBallsForTube(int ballsUsed, int totalBalls, int tubesRemaining,
                                      int tubeCapacity, double complexity) {
        if (tubesRemaining == 1) {
            // Ultimo tubo: metti tutte le palline rimaste
            return Math.min(totalBalls - ballsUsed, tubeCapacity);
        }

        int ballsLeft = totalBalls - ballsUsed;
        int avgBalls = ballsLeft / tubesRemaining;

        // Aggiungi variazione basata sulla complessità
        int variation = Math.max(1, (int)(avgBalls * complexity * 0.5));
        int minBalls = Math.max(1, avgBalls - variation);
        int maxBalls = Math.min(tubeCapacity, avgBalls + variation);

        return minBalls + random.nextInt(Math.max(1, maxBalls - minBalls + 1));
    }

    /**
     * Genera un livello semplice per testing
     */
    public List<Tube> generateSimpleLevel(GameLevel difficulty) {
        List<Tube> tubes = new ArrayList<>();
        for (int i = 0; i < difficulty.getNumberOfTubes(); i++) {
            tubes.add(new Tube(i, difficulty.getTubeCapacity()));
        }

        Ball.Color[] colors = difficulty.getAvailableColors();
        int ballId = 0;

        // Riempi ogni tubo con un colore diverso
        for (int colorIndex = 0; colorIndex < difficulty.getNumberOfColors(); colorIndex++) {
            Tube tube = tubes.get(colorIndex);
            Ball.Color color = colors[colorIndex];

            for (int i = 0; i < difficulty.getBallsPerColor(); i++) {
                tube.addBall(new Ball(color, ballId++));
            }
        }

        // Mescola solo la prima pallina di alcuni tubi per renderlo interessante
        if (tubes.size() > 1) {
            Tube firstTube = tubes.get(0);
            Tube secondTube = tubes.get(1);

            if (!firstTube.isEmpty() && !secondTube.isFull()) {
                Ball ball = firstTube.removeBall();
                secondTube.addBall(ball);
            }
        }

        return tubes;
    }
}