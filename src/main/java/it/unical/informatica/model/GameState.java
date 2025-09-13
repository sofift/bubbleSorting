package it.unical.informatica.model;

import it.unical.informatica.asp.*;
import it.unical.mat.embasp.base.InputProgram;

import java.util.*;

/**
 * Rappresenta lo stato completo del gioco Bubble Sorting.
 * VERSIONE CORRETTA - Carica SEMPRE i livelli dal file JSON, mai casuali!
 */
public class GameState {
    private final GameLevel level;
    private final int levelNumber;
    private final List<Tube> tubes;
    private final Stack<Move> moveHistory;
    private int moves;
    private boolean gameWon;
    private long startTime;
    private long endTime;

    /**
     * Costruttore per creare un nuovo stato del gioco
     * @param level Livello di difficoltà
     * @param levelNumber Numero del livello (1-5)
     */
    public GameState(GameLevel level, int levelNumber) {
        this.level = level;
        this.levelNumber = levelNumber;
        this.tubes = new ArrayList<>();
        this.moveHistory = new Stack<>();
        this.moves = 0;
        this.gameWon = false;
        this.startTime = System.currentTimeMillis();

        initializeTubes();
        loadLevelFromJSON(); // ✅ SEMPRE dal JSON, mai casuale!
    }

    /**
     * Inizializza i tubi vuoti
     */
    private void initializeTubes() {
        for (int i = 0; i < level.getNumberOfTubes(); i++) {
            tubes.add(new Tube(i, level.getTubeCapacity()));
        }
    }

    /**
     * ✅ CORREZIONE: Carica SEMPRE la configurazione dal file JSON
     */
    private void loadLevelFromJSON() {
        System.out.println("📁 Caricamento livello da JSON: " + level.getDisplayName() + " - Livello " + levelNumber);

        LevelLoader levelLoader = new LevelLoader();
        try {
            // ✅ USA SEMPRE IL LEVELLOADER - mai generazione casuale!
            levelLoader.loadLevel(this, level, levelNumber);
            System.out.println("✅ Livello caricato con successo dal JSON");

            // Debug: stampa la configurazione caricata
            printLoadedConfiguration();

        } catch (LevelLoader.LevelLoadException e) {
            System.err.println("❌ ERRORE CRITICO: Impossibile caricare il livello dal JSON: " + e.getMessage());

            // ❌ NON usare fallback casuale - crea configurazione di emergenza deterministica
            createEmergencyLevel();
        }
    }

    /**
     * ✅ Crea una configurazione di emergenza DETERMINISTICA (non casuale)
     * Usata solo se il file JSON è completamente inaccessibile
     */
    private void createEmergencyLevel() {
        System.out.println("🚨 Creazione configurazione di emergenza deterministica...");

        // Pulisce tutti i tubi
        for (Tube tube : tubes) {
            tube.clear();
        }

        // Configurazione di emergenza semplice e deterministica per il livello EASY
        if (level == GameLevel.EASY) {
            createEmergencyEasyLevel();
        } else if (level == GameLevel.MEDIUM) {
            createEmergencyMediumLevel();
        }

        System.out.println("✅ Configurazione di emergenza creata");
    }

    /**
     * Configurazione di emergenza per livello EASY
     */
    private void createEmergencyEasyLevel() {
        // Configurazione fissa per EASY - Livello 1 (simile al JSON ma semplificata)
        BallColor[] colors = {BallColor.RED, BallColor.BLUE, BallColor.GREEN, BallColor.YELLOW};

        // Tubo 1: RED, BLUE, RED, BLUE (dal basso verso l'alto)
        tubes.get(0).addBall(new Ball(colors[0])); // RED
        tubes.get(0).addBall(new Ball(colors[1])); // BLUE
        tubes.get(0).addBall(new Ball(colors[0])); // RED
        tubes.get(0).addBall(new Ball(colors[1])); // BLUE

        // Tubo 2: GREEN, YELLOW, GREEN, YELLOW
        tubes.get(1).addBall(new Ball(colors[2])); // GREEN
        tubes.get(1).addBall(new Ball(colors[3])); // YELLOW
        tubes.get(1).addBall(new Ball(colors[2])); // GREEN
        tubes.get(1).addBall(new Ball(colors[3])); // YELLOW

        // Tubo 3: RED, YELLOW, BLUE, GREEN
        tubes.get(2).addBall(new Ball(colors[0])); // RED
        tubes.get(2).addBall(new Ball(colors[3])); // YELLOW
        tubes.get(2).addBall(new Ball(colors[1])); // BLUE
        tubes.get(2).addBall(new Ball(colors[2])); // GREEN

        // Tubo 4: YELLOW, GREEN, BLUE, RED
        tubes.get(3).addBall(new Ball(colors[3])); // YELLOW
        tubes.get(3).addBall(new Ball(colors[2])); // GREEN
        tubes.get(3).addBall(new Ball(colors[1])); // BLUE
        tubes.get(3).addBall(new Ball(colors[0])); // RED

        // Tubi 5 e 6 rimangono vuoti
    }

    /**
     * Configurazione di emergenza per livello MEDIUM
     */
    private void createEmergencyMediumLevel() {
        BallColor[] colors = {BallColor.RED, BallColor.BLUE, BallColor.GREEN, BallColor.YELLOW, BallColor.ORANGE};

        // Distribuzione semplice per MEDIUM
        for (int tubeIndex = 0; tubeIndex < level.getFilledTubes(); tubeIndex++) {
            for (int ballPos = 0; ballPos < level.getTubeCapacity(); ballPos++) {
                int colorIndex = (tubeIndex + ballPos) % colors.length;
                tubes.get(tubeIndex).addBall(new Ball(colors[colorIndex]));
            }
        }
    }

    /**
     * Configurazione di emergenza per livello HARD
     */
    private void createEmergencyHardLevel() {
        BallColor[] colors = {BallColor.RED, BallColor.BLUE, BallColor.GREEN,
                BallColor.YELLOW, BallColor.ORANGE, BallColor.PURPLE, BallColor.PINK};

        // Distribuzione semplice per HARD
        for (int tubeIndex = 0; tubeIndex < level.getFilledTubes(); tubeIndex++) {
            for (int ballPos = 0; ballPos < level.getTubeCapacity(); ballPos++) {
                int colorIndex = (tubeIndex + ballPos) % colors.length;
                tubes.get(tubeIndex).addBall(new Ball(colors[colorIndex]));
            }
        }
    }

    /**
     * Stampa la configurazione caricata per debug
     */
    private void printLoadedConfiguration() {
        System.out.println("🔍 Configurazione caricata:");
        for (int i = 0; i < tubes.size(); i++) {
            Tube tube = tubes.get(i);
            List<Ball> balls = tube.getBalls();

            System.out.print("   Tubo " + (i + 1) + ": ");
            if (balls.isEmpty()) {
                System.out.println("vuoto");
            } else {
                for (int j = 0; j < balls.size(); j++) {
                    if (j > 0) System.out.print(", ");
                    System.out.print(balls.get(j).getColor().name());
                }
                System.out.println();
            }
        }
    }

    /**
     * Esegue una mossa spostando una pallina da un tubo all'altro
     * @param fromTubeId ID del tubo di origine
     * @param toTubeId ID del tubo di destinazione
     * @return true se la mossa è stata eseguita con successo
     */
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

        // Esegui la mossa
        Ball movedBall = fromTube.removeBall();
        toTube.addBall(movedBall);

        // Registra la mossa nella storia
        Move move = new Move(fromTubeId, toTubeId, movedBall);
        moveHistory.push(move);
        moves++;

        // Controlla se il gioco è vinto
        checkWinCondition();

        return true;
    }

    /**
     * Annulla l'ultima mossa
     * @return true se l'annullamento è riuscito
     */
    public boolean undoMove() {
        if (moveHistory.isEmpty() || gameWon) {
            return false;
        }

        Move lastMove = moveHistory.pop();
        Tube fromTube = tubes.get(lastMove.getFromTubeId());
        Tube toTube = tubes.get(lastMove.getToTubeId());

        // Inverti la mossa
        Ball ballToReturn = toTube.removeBall();
        fromTube.addBall(ballToReturn);

        moves--;
        gameWon = false; // Il gioco non può più essere vinto dopo l'undo

        return true;
    }

    /**
     * Ottiene tutte le mosse possibili dal stato corrente
     * @return Lista delle mosse valide
     */
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

    /**
     * Controlla la condizione di vittoria
     */
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

        // Il gioco è vinto se tutti i tubi pieni sono completati
        // e abbiamo esattamente il numero di colori previsto
        if (allCompleted && completedTubes == level.getNumberOfColors()) {
            gameWon = true;
            endTime = System.currentTimeMillis();
        }
    }

    /**
     * Ottiene il punteggio basato su mosse e tempo
     * @return Punteggio calcolato
     */
    public int getScore() {
        if (!gameWon) return 0;

        long timeInSeconds = (endTime - startTime) / 1000;
        int baseScore = 1000;
        int movesPenalty = moves * 10;
        int timePenalty = (int)(timeInSeconds * 2);

        return Math.max(100, baseScore - movesPenalty - timePenalty);
    }

    /**
     * Ottiene il tempo di gioco in secondi
     * @return Tempo di gioco
     */
    public long getGameTimeSeconds() {
        long currentTime = gameWon ? endTime : System.currentTimeMillis();
        return (currentTime - startTime) / 1000;
    }

    /**
     * Resetta il gioco allo stato iniziale
     */
    public void reset() {
        tubes.forEach(Tube::clear);
        moveHistory.clear();
        moves = 0;
        gameWon = false;
        startTime = System.currentTimeMillis();
        loadLevelFromJSON(); // ✅ Ricarica sempre dal JSON
    }



    // Getters
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