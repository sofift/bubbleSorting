package it.unical.informatica.controller;

import it.unical.informatica.model.GameLevel;
import it.unical.informatica.model.GameState;
import it.unical.informatica.view.GameView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Controller principale del gioco - versione semplificata con Task/Service.
 * - Tutte le operazioni pesanti (ASP) girano in background con Service.
 * - La UI viene aggiornata SOLO in callback di successo/fallimento (FX thread).
 */
public class GameController {

    private final Stage primaryStage;
    private final MenuController menuController;
    private final GameLevel gameLevel;
    private final int levelNumber;

    private GameState gameState;
    private GameView gameView;
    private AspSolver aspSolver;

    // Stato del controller
    private boolean isProcessingMove = false;
    private boolean isShowingHint = false;

    // Services (riusabili)
    private HintService hintService;
    private SolveService solveService;

    // -------------------- COSTRUTTORE --------------------
    public GameController(Stage primaryStage, MenuController menuController,
                          GameLevel gameLevel, int levelNumber) {
        this.primaryStage = primaryStage;
        this.menuController = menuController;
        this.gameLevel = gameLevel;
        this.levelNumber = levelNumber;
        initializeComponents();
    }

    // -------------------- INIT --------------------
    private void initializeComponents() {
        try {
            gameState = new GameState(gameLevel, levelNumber);
            gameView = new GameView(gameLevel);
            aspSolver = new AspSolver();

            setupEventHandlers();
            updateView();

            // Inizializza i servizi
            hintService = new HintService();
            solveService = new SolveService();

        } catch (Exception e) {
            e.printStackTrace();
            createMinimalFallback();
        }
    }

    private void createMinimalFallback() {
        try {
            if (gameState == null) gameState = GameState.createTestLevel(gameLevel, levelNumber);
            if (gameView == null)  gameView  = new GameView(gameLevel);
            if (aspSolver == null) aspSolver = new AspSolver();

            setupEventHandlers();
            updateView();

            hintService = new HintService();
            solveService = new SolveService();

        } catch (Exception ex) {
            showCriticalError(ex);
        }
    }

    private void showCriticalError(Exception e) {
        Platform.runLater(() -> {
            var alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Errore Critico");
            alert.setHeaderText("Impossibile inizializzare il gioco");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            if (menuController != null) menuController.returnToMenu();
        });
    }

    // -------------------- EVENTI UI --------------------
    private void setupEventHandlers() {
        if (gameView == null) return;

        gameView.setOnTubeClicked(this::handleTubeClick);
        gameView.setOnRestartRequested(this::restartGame);
        gameView.setOnMenuRequested(this::returnToMenu);
        gameView.setOnHintRequested(this::showHint);
        gameView.setOnSolveRequested(this::solveAutomatically);
        gameView.setOnUndoRequested(this::undoLastMove);
    }

    public void startGame() {
        try {
            if (gameView == null || gameView.getScene() == null) {
                returnToMenu();
                return;
            }
            primaryStage.setScene(gameView.getScene());
            primaryStage.setTitle("Bubble Sorting Game - " + gameLevel.getDisplayName() +
                    " Livello " + levelNumber);
        } catch (Exception e) {
            returnToMenu();
        }
    }

    private void handleTubeClick(int tubeId) {
        if (isProcessingMove || gameState.isGameWon()) return;
        gameView.handleTubeSelection(tubeId, gameState);
    }

    private void updateView() {
        if (gameView != null && gameState != null) gameView.updateGameState(gameState);
    }

    private void nextLevel() {
        try {
            if (levelNumber < 5) {
                GameController next = new GameController(primaryStage, menuController, gameLevel, levelNumber + 1);
                next.startGame();
            } else {
                gameView.showCompletionDialog(this::returnToMenu);
            }
        } catch (Exception ignored) {}
    }

    private void restartGame() {
        try {
            gameState = new GameState(gameLevel, levelNumber);
            gameView.resetView();
            updateView();
        } catch (Exception e) {
            returnToMenu();
        }
    }

    private void returnToMenu() {
        try {
            menuController.returnToMenu();
        } catch (Exception ignored) {}
    }

    // -------------------- HINT (Service) --------------------
    private void showHint() {
        if (isShowingHint || gameState.isGameWon() || aspSolver == null) return;

        // configura il service sullo stato corrente
        hintService.restartWith(gameState);

        // UI: loading on
        isShowingHint = true;
        gameView.showLoadingHint(true);

        // callback successo/errore (girano su FX thread)
        hintService.setOnSucceeded(ev -> {
            GameState.Move hint = hintService.getValue();
            gameView.showLoadingHint(false);
            if (hint != null) gameView.highlightHint(hint.getFromTubeId(), hint.getToTubeId());
            else gameView.showNoHintAvailable();
            isShowingHint = false;
        });

        hintService.setOnFailed(ev -> {
            gameView.showLoadingHint(false);
            gameView.showHintError();
            isShowingHint = false;
        });

        hintService.start();
    }

    // -------------------- SOLVE (Service) --------------------
    private void solveAutomatically() {
        if (isProcessingMove || gameState.isGameWon() || aspSolver == null) return;

        solveService.restartWith(gameState);

        gameView.showLoadingSolution(true);

        solveService.setOnSucceeded(ev -> {
            List<GameState.Move> plan = solveService.getValue();
            gameView.showLoadingSolution(false);
            if (plan != null && !plan.isEmpty()) executeAutomaticSolution(plan);
            else gameView.showNoSolutionFound();
        });

        solveService.setOnFailed(ev -> {
            gameView.showLoadingSolution(false);
            gameView.showSolutionError();
        });

        solveService.start();
    }

    // -------------------- ESECUZIONE PIANO --------------------
    private void executeAutomaticSolution(List<GameState.Move> moves) {
        if (moves == null || moves.isEmpty()) return;
        isProcessingMove = true;
        executeMovesSequentially(moves, 0);
    }

    private void executeMovesSequentially(List<GameState.Move> moves, int index) {
        if (index >= moves.size()) { isProcessingMove = false; return; }

        GameState.Move move = moves.get(index);

        // aggiorna modello
        gameState.makeMove(move.getFromTubeId(), move.getToTubeId());

        // anima e poi continua
        gameView.animateMove(move.getFromTubeId(), move.getToTubeId(), () -> {
            updateView();
            if (index == moves.size() - 1) {
                if (gameState.isGameWon()) {
                    gameView.showWinAnimation(() ->
                            gameView.showWinDialog(gameState.getMoves(), this::nextLevel, this::restartGame)
                    );
                }
                isProcessingMove = false;
            } else {
                // pausa senza bloccare il thread FX
                PauseTransition pause = new PauseTransition(Duration.millis(400));
                pause.setOnFinished(e -> executeMovesSequentially(moves, index + 1));
                pause.play();
            }
        });
    }

    private void undoLastMove() {
        gameView.showUndoNotAvailable();
    }

    // -------------------- SERVICES --------------------
    /**
     * Calcolo del suggerimento (una singola mossa) in background.
     */
    private class HintService extends Service<GameState.Move> {
        private GameState source;

        void restartWith(GameState gs) {
            this.source = gs;
            if (isRunning()) cancel();
            reset();
        }

        @Override
        protected Task<GameState.Move> createTask() {
            GameState snapshot = source; // catturo riferimento
            return new Task<>() {
                @Override
                protected GameState.Move call() throws Exception {
                    return aspSolver.findBestMove(snapshot);
                }
            };
        }
    }

    /**
     * Calcolo del piano completo in background.
     */
    private class SolveService extends Service<List<GameState.Move>> {
        private GameState source;

        void restartWith(GameState gs) {
            this.source = gs;
            if (isRunning()) cancel();
            reset();
        }

        @Override
        protected Task<List<GameState.Move>> createTask() {
            GameState snapshot = source; // catturo riferimento
            return new Task<>() {
                @Override
                protected List<GameState.Move> call() throws Exception {
                    return aspSolver.solveGame(snapshot);
                }
            };
        }
    }

    // -------------------- GETTER (se servono ai test) --------------------
    public GameState getGameState() { return gameState; }
    public GameView getGameView() { return gameView; }
}
