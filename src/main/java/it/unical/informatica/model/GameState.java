package it.unical.informatica.model;


import java.util.*;


public class GameState {
    private final GameLevel level;
    private final int levelNumber;
    private final List<Tube> tubes;
    private final Stack<Move> moveHistory;
    private int moves;
    private boolean gameWon;
    private long startTime;
    private long endTime;


    public GameState(GameLevel level, int levelNumber) {
        this.level = level;
        this.levelNumber = levelNumber;
        this.tubes = new ArrayList<>();
        this.moveHistory = new Stack<>();
        this.moves = 0;
        this.gameWon = false;
        this.startTime = System.currentTimeMillis();

        initializeTubes();
        loadLevelFromJSON();
    }


    private void initializeTubes() {
        for (int i = 0; i < level.getNumberOfTubes(); i++) {
            tubes.add(new Tube(i, level.getTubeCapacity()));
        }
    }


    private void loadLevelFromJSON() {

        LevelLoader levelLoader = new LevelLoader();
        try {
            levelLoader.loadLevel(this, level, levelNumber);
            System.out.println("Livello caricato con successo dal JSON");


        } catch (LevelLoader.LevelLoadException e) {
            System.err.println("ERRORE CRITICO: Impossibile caricare il livello dal JSON: " + e.getMessage());
        }
    }




    public boolean makeMove(int fromTubeId, int toTubeId) {
        if (gameWon || fromTubeId == toTubeId) {
            return false;
        }

        if (fromTubeId < 0 || fromTubeId >= tubes.size() ||
                toTubeId < 0 || toTubeId >= tubes.size()) {
            return false;
        }

        Tube fromTube = tubes.get(fromTubeId);
        Tube toTube = tubes.get(toTubeId);

        if (fromTube.isEmpty()) {
            return false;
        }

        Ball ballToMove = fromTube.getTopBall();
        if (!toTube.canAddBall(ballToMove)) {
            return false;
        }

        Ball movedBall = fromTube.removeBall();
        toTube.addBall(movedBall);

        Move move = new Move(fromTubeId, toTubeId, movedBall);
        moveHistory.push(move);
        moves++;

        checkWinCondition();

        return true;
    }


    public List<Move> getPossibleMoves() {
        List<Move> possibleMoves = new ArrayList<>();

        for (int fromId = 0; fromId < tubes.size(); fromId++) {
            Tube fromTube = tubes.get(fromId);
            if (fromTube.isEmpty()) continue;

            Ball topBall = fromTube.getTopBall();

            for (int toId = 0; toId < tubes.size(); toId++) {
                if (fromId == toId) continue;

                Tube toTube = tubes.get(toId);
                if (toTube.canAddBall(topBall)) {
                    possibleMoves.add(new Move(fromId, toId, topBall));
                }
            }
        }

        return possibleMoves;
    }

    private void checkWinCondition() {
        boolean allCompleted = true;
        int completedTubes = 0;

        for (Tube tube : tubes) {
            if (!tube.isEmpty()) {
                if (tube.isCompleted()) {
                    completedTubes++;
                } else {
                    allCompleted = false;
                    break;
                }
            }
        }

        // il gioco è vinto se tutti i tubi pieni sono completati
        // e abbiamo esattamente il numero di colori previsto
        if (allCompleted && completedTubes == level.getNumberOfColors()) {
            gameWon = true;
            endTime = System.currentTimeMillis();
        }
    }


    public int getScore() {
        if (!gameWon) return 0;

        long timeInSeconds = (endTime - startTime) / 1000;
        int baseScore = 1000;
        int movesPenalty = moves * 10;
        int timePenalty = (int)(timeInSeconds * 2);

        return Math.max(100, baseScore - movesPenalty - timePenalty);
    }


    public long getGameTimeSeconds() {
        long currentTime = gameWon ? endTime : System.currentTimeMillis();
        return (currentTime - startTime) / 1000;
    }


    public void reset() {
        tubes.forEach(Tube::clear);
        moveHistory.clear();
        moves = 0;
        gameWon = false;
        startTime = System.currentTimeMillis();
        loadLevelFromJSON();
    }



    public GameLevel getLevel() { return level; }
    public int getLevelNumber() { return levelNumber; }
    public List<Tube> getTubes() { return new ArrayList<>(tubes); }
    public Tube getTube(int id) { return id >= 0 && id < tubes.size() ? tubes.get(id) : null; }
    public int getMoves() { return moves; }
    public boolean isGameWon() { return gameWon; }
    public Stack<Move> getMoveHistory() { return new Stack<Move>() {{ addAll(moveHistory); }}; }
    public boolean canUndo() { return !moveHistory.isEmpty() && !gameWon; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GameState[level=").append(level)
                .append(", levelNumber=").append(levelNumber)
                .append(", moves=").append(moves)
                .append(", won=").append(gameWon)
                .append("]\nTubes:\n");

        for (int i = 0; i < tubes.size(); i++) {
            sb.append("  ").append(i + 1).append(": ");
            Tube tube = tubes.get(i);
            if (tube.isEmpty()) {
                sb.append("vuoto");
            } else {
                List<Ball> balls = tube.getBalls();
                for (int j = 0; j < balls.size(); j++) {
                    if (j > 0) sb.append(", ");
                    sb.append(balls.get(j).getColor().name());
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}