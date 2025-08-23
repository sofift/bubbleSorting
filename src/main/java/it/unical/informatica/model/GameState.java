package it.unical.informatica.model;

import java.util.*;

/**
 * Rappresenta lo stato corrente del gioco - VERSIONE COMPLETA AGGIORNATA
 */
public class GameState {

    private GameLevel level;           // Rimosso final per createTestLevel
    private final List<Tube> tubes;
    private int currentLevelNumber;    // Rimosso final per createTestLevel
    private int moves;
    private boolean gameWon;

    // ✅ LEVEL MANAGER STATICO per gestione livelli
    private static LevelManager levelManager;

    // ✅ COSTRUTTORE PRIVATO VUOTO per createTestLevel
    private GameState() {
        this.level = null;
        this.tubes = new ArrayList<>();
        this.currentLevelNumber = 0;
        this.moves = 0;
        this.gameWon = false;
    }

    // ✅ INIZIALIZZAZIONE LAZY per LevelManager
    private static LevelManager getLevelManager() {
        if (levelManager == null) {
            levelManager = new LevelManager();
        }
        return levelManager;
    }

    /**
     * Costruttore principale - utilizza il LevelManager per ottenere livelli validi
     */
    public GameState(GameLevel level, int currentLevelNumber) {
        this.level = level;
        this.currentLevelNumber = currentLevelNumber;
        this.moves = 0;
        this.gameWon = false;
        this.tubes = new ArrayList<>();

        try {
            // ✅ USA IL LEVEL MANAGER per ottenere livelli validi e risolvibili
            GameState generatedState = getLevelManager().createGameState(level, currentLevelNumber);

            // Copia i tubi dal livello generato
            for (Tube tube : generatedState.getTubes()) {
                this.tubes.add(tube.copy());
            }

            System.out.println("✅ Livello caricato con successo: " + level.getDisplayName() +
                    " Livello " + currentLevelNumber);

        } catch (Exception e) {
            System.err.println("⚠️ Errore nel caricamento del livello, uso fallback: " + e.getMessage());
            // ✅ FALLBACK: Inizializzazione manuale semplice
            initializeFallback();
        }

        checkWinCondition();
    }

    /**
     * ✅ FALLBACK: Inizializzazione manuale semplice
     */
    private void initializeFallback() {
        System.out.println("🔄 Inizializzando livello con metodo fallback...");

        // Crea tutti i tubi
        for (int i = 0; i < level.getNumberOfTubes(); i++) {
            tubes.add(new Tube(i, level.getTubeCapacity()));
        }

        // Crea e distribuisce le palline in modo semplice
        List<Ball> allBalls = createAllBalls();
        Collections.shuffle(allBalls);
        distributeBallsSimple(allBalls);
    }

    /**
     * Copy constructor per creare una copia di un GameState
     */
    public GameState(GameState other) {
        this.level = other.level;
        this.currentLevelNumber = other.currentLevelNumber;
        this.moves = other.moves;
        this.gameWon = other.gameWon;
        this.tubes = new ArrayList<>();

        for (Tube tube : other.tubes) {
            this.tubes.add(tube.copy());
        }
    }

    /**
     * Costruttore privato per creare GameState da tubi personalizzati
     */
    private GameState(GameLevel level, int currentLevelNumber, List<Tube> tubes) {
        this.level = level;
        this.currentLevelNumber = currentLevelNumber;
        this.moves = 0;
        this.gameWon = false;
        this.tubes = new ArrayList<>();

        for (Tube tube : tubes) {
            this.tubes.add(tube.copy());
        }

        checkWinCondition();
    }

    /**
     * Crea un GameState con tubi personalizzati (per testing e LevelManager)
     */
    public static GameState createFromTubes(GameLevel level, int levelNumber, List<Tube> customTubes) {
        return new GameState(level, levelNumber, customTubes);
    }

    /**
     * ✅ Crea un GameState deterministico per testing (bypassa il generatore casuale)
     */
    public static GameState createTestLevel(GameLevel level, int levelNumber) {
        System.out.println("🧪 Creando livello di test per " + level.getDisplayName() + " " + levelNumber);

        // Crea GameState con costruttore privato vuoto
        GameState testGameState = new GameState();
        testGameState.level = level;
        testGameState.currentLevelNumber = levelNumber;
        testGameState.moves = 0;
        testGameState.gameWon = false;
        testGameState.tubes.clear();

        // Crea configurazione di test semplice e deterministica
        for (int i = 0; i < level.getNumberOfTubes(); i++) {
            testGameState.tubes.add(new Tube(i, level.getTubeCapacity()));
        }

        // Configurazione di test: ogni colore nel proprio tubo, poi mescola leggermente
        Ball.Color[] colors = level.getAvailableColors();
        int ballId = 0;

        for (int colorIndex = 0; colorIndex < colors.length; colorIndex++) {
            Tube tube = testGameState.tubes.get(colorIndex);
            Ball.Color color = colors[colorIndex];

            for (int i = 0; i < level.getBallsPerColor(); i++) {
                tube.addBall(new Ball(color, ballId++));
            }
        }

        // Mescola solo una pallina per rendere il test interessante
        if (testGameState.tubes.size() > 1 &&
                !testGameState.tubes.get(0).isEmpty() &&
                !testGameState.tubes.get(1).isFull()) {
            Ball ball = testGameState.tubes.get(0).removeBall();
            testGameState.tubes.get(1).addBall(ball);
        }

        testGameState.checkWinCondition();
        System.out.println("✅ Livello di test creato correttamente");
        return testGameState;
    }

    /**
     * ✅ Crea tutte le palline per il livello
     */
    private List<Ball> createAllBalls() {
        List<Ball> balls = new ArrayList<>();
        Ball.Color[] colors = level.getAvailableColors();
        int ballId = 0;

        for (Ball.Color color : colors) {
            for (int i = 0; i < level.getBallsPerColor(); i++) {
                balls.add(new Ball(color, ballId++));
            }
        }

        return balls;
    }

    /**
     * ✅ Distribuisce le palline in modo semplice
     */
    private void distributeBallsSimple(List<Ball> balls) {
        int ballIndex = 0;
        int tubesWithBalls = level.getNumberOfColors(); // Solo i primi N tubi

        for (int tubeIndex = 0; tubeIndex < tubesWithBalls && ballIndex < balls.size(); tubeIndex++) {
            Tube tube = tubes.get(tubeIndex);

            // Riempi il tubo fino alla capacità
            for (int i = 0; i < level.getTubeCapacity() && ballIndex < balls.size(); i++) {
                tube.addBall(balls.get(ballIndex++));
            }
        }
    }

    /**
     * Esegue una mossa spostando una pallina da un tubo all'altro
     */
    public boolean makeMove(int fromTubeId, int toTubeId) {
        if (fromTubeId == toTubeId || gameWon) {
            return false;
        }

        Tube fromTube = getTubeById(fromTubeId);
        Tube toTube = getTubeById(toTubeId);

        if (fromTube == null || toTube == null) {
            return false;
        }

        if (fromTube.canMoveTo(toTube)) {
            Ball ball = fromTube.removeBall();
            toTube.addBall(ball);
            moves++;

            checkWinCondition();
            return true;
        }

        return false;
    }

    /**
     * Verifica se il gioco è stato vinto
     */
    private void checkWinCondition() {
        for (Tube tube : tubes) {
            if (!tube.isEmpty() && !tube.isComplete()) {
                gameWon = false;
                return;
            }
        }
        gameWon = true;
    }

    /**
     * Restituisce il tubo con l'ID specificato
     */
    public Tube getTubeById(int id) {
        return tubes.stream()
                .filter(tube -> tube.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Restituisce il tubo all'indice specificato
     */
    public Tube getTube(int index) {
        if (index >= 0 && index < tubes.size()) {
            return tubes.get(index);
        }
        return null;
    }

    /**
     * Restituisce una lista di tutte le mosse possibili nello stato corrente
     */
    public List<Move> getPossibleMoves() {
        List<Move> possibleMoves = new ArrayList<>();

        for (Tube fromTube : tubes) {
            if (fromTube.isEmpty()) continue;

            for (Tube toTube : tubes) {
                if (fromTube.getId() == toTube.getId()) continue;

                if (fromTube.canMoveTo(toTube)) {
                    possibleMoves.add(new Move(fromTube.getId(), toTube.getId()));
                }
            }
        }

        return possibleMoves;
    }

    /**
     * Crea una copia profonda dello stato del gioco
     */
    public GameState copy() {
        return new GameState(this);
    }

    // === METODI AGGIUNTIVI PER ASP SOLVER ===

    /**
     * Restituisce il numero di tubi
     */
    public int getNumberOfTubes() {
        return tubes.size();
    }

    /**
     * Restituisce il numero di colori nel gioco
     */
    public int getNumberOfColors() {
        return level.getNumberOfColors();
    }

    /**
     * Restituisce la capacità dei tubi
     */
    public int getTubeCapacity() {
        return level.getTubeCapacity();
    }

    /**
     * ✅ METODO AGGIUNTO per ASP - Restituisce la pallina in una posizione specifica
     */
    public String getBall(int tubeIndex, int position) {
        if (tubeIndex < 0 || tubeIndex >= tubes.size()) {
            return null;
        }

        Tube tube = tubes.get(tubeIndex);
        List<Ball> balls = tube.getBalls();

        if (position < 0 || position >= balls.size()) {
            return null;
        }

        return balls.get(position).getColor().name();
    }

    // === GETTERS ===

    public GameLevel getLevel() {
        return level;
    }

    public List<Tube> getTubes() {
        return new ArrayList<>(tubes);
    }

    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }

    public int getMoves() {
        return moves;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    // === METODI STATICI DI UTILITÀ ===

    /**
     * Metodo per ottenere statistiche del level manager
     */
    public static String getLevelStats() {
        try {
            return getLevelManager().getCacheStats();
        } catch (Exception e) {
            return "Statistiche non disponibili: " + e.getMessage();
        }
    }

    /**
     * Pre-genera tutti i livelli per una difficoltà (per prestazioni migliori)
     */
    public static void preGenerateLevels(GameLevel difficulty) {
        try {
            getLevelManager().preGenerateLevels(difficulty);
        } catch (Exception e) {
            System.err.println("Errore nella pre-generazione: " + e.getMessage());
        }
    }

    /**
     * Pulisce la cache dei livelli
     */
    public static void clearLevelCache() {
        try {
            if (levelManager != null) {
                levelManager.clearCache();
            }
        } catch (Exception e) {
            System.err.println("Errore nella pulizia: " + e.getMessage());
        }
    }

    /**
     * Verifica se il sistema ASP è funzionante
     */
    public static boolean isAspWorking() {
        try {
            return getLevelManager().isAspWorking();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ottiene informazioni diagnostiche del sistema
     */
    public static String getDiagnosticInfo() {
        try {
            return getLevelManager().getDiagnosticInfo();
        } catch (Exception e) {
            return "Errore nell'ottenere info diagnostiche: " + e.getMessage();
        }
    }

    /**
     * Test rapido del sistema
     */
    public static boolean runSystemTest() {
        try {
            return getLevelManager().runSystemTest();
        } catch (Exception e) {
            System.err.println("Test del sistema fallito: " + e.getMessage());
            return false;
        }
    }

    /**
     * Pulizia delle risorse statiche
     */
    public static void cleanup() {
        try {
            if (levelManager != null) {
                levelManager.clearCache();
                levelManager = null;
            }
        } catch (Exception e) {
            System.err.println("Errore nella pulizia: " + e.getMessage());
        }
    }
    public static GameState fromTubes(List<Tube> tubeList) {
        GameState gs = new GameState();   // usa il costruttore privato vuoto già presente
        gs.level = null;                  // nessuna info di livello (coerente con LevelGenerator)
        gs.currentLevelNumber = 0;
        gs.moves = 0;
        gs.gameWon = false;

        gs.tubes.clear();
        if (tubeList != null) {
            for (Tube t : tubeList) {
                gs.tubes.add(t.copy());   // copia difensiva
            }
        }

        gs.checkWinCondition();
        return gs;
    }

    /**
     * Classe interna per rappresentare una mossa
     */
    public static class Move {
        private final int fromTubeId;
        private final int toTubeId;

        public Move(int fromTubeId, int toTubeId) {
            this.fromTubeId = fromTubeId;
            this.toTubeId = toTubeId;
        }

        public int getFromTubeId() {
            return fromTubeId;
        }

        public int getToTubeId() {
            return toTubeId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Move move = (Move) obj;
            return fromTubeId == move.fromTubeId && toTubeId == move.toTubeId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fromTubeId, toTubeId);
        }

        @Override
        public String toString() {
            return String.format("Move{from=%d, to=%d}", fromTubeId, toTubeId);
        }
    }
}