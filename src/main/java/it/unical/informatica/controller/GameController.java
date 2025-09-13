package it.unical.informatica.controller;

import it.unical.informatica.asp.AspSolver;
import it.unical.informatica.asp.ShowMove;
import it.unical.informatica.model.*;
import it.unical.informatica.view.GameView;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.util.List;

public class GameController {
    private final Stage primaryStage;
    private final MenuController menuController;
    private final GameLevel gameLevel;
    private final int levelNumber;

    private GameState gameState;
    private GameView gameView;
    private AspSolver aspSolver;
    private GamePreferences preferences;

    // stato view
    private boolean isProcessingMove = false;
    private boolean isShowingHint = false;
    private boolean gameInitialized = false;

    // servizi
    private HintService hintService;
    private SolveService solveService;

    public GameController(Stage primaryStage, MenuController menuController,
                          GameLevel gameLevel, int levelNumber) {
        this.primaryStage = primaryStage;
        this.menuController = menuController;
        this.gameLevel = gameLevel;
        this.levelNumber = levelNumber;
        this.preferences = GamePreferences.getInstance();

        initializeComponents();
    }

    private void initializeComponents() {
        try {
            gameState = new GameState(gameLevel, levelNumber);
            gameView = new GameView(gameLevel);
            aspSolver = new AspSolver();
            setupEventHandlers();
            initializeServices();
            updateView();

            gameInitialized = true;

        } catch (Exception e) {
            e.printStackTrace();
            handleInitializationError(e);
        }
    }

    private void initializeServices() {
        hintService = new HintService();
        solveService = new SolveService();

        hintService.setOnSucceeded(e -> {
            ShowMove hint = hintService.getValue();
            handleHintResult(hint);
            gameView.lockTubeInteractions(false);
            gameView.setBusyCursor(false);
        });

        hintService.setOnFailed(e -> {
            Throwable exception = hintService.getException();
            handleHintError(exception);
            gameView.lockTubeInteractions(false);
            gameView.setBusyCursor(false);
        });

        solveService.setOnSucceeded(e -> {
            List<ShowMove> solution = solveService.getValue();
            handleSolutionResult(solution);
            gameView.lockTubeInteractions(false);
            gameView.setBusyCursor(false);
        });

        solveService.setOnFailed(e -> {
            Throwable exception = solveService.getException();
            handleSolutionError(exception);
            gameView.lockTubeInteractions(false);
            gameView.setBusyCursor(false);
        });


    }

    private void setupEventHandlers() {
        if (gameView == null) return;

        gameView.setOnTubeClicked(this::handleTubeClick);

        gameView.setOnRestartRequested(() -> {
            try {
                restartGame();
            } catch (Exception e) {
                showError("Errore nel riavvio", e.getMessage());
            }
        });

        gameView.setOnMenuRequested(() -> {
            try {
                returnToMenu();
            } catch (Exception e) {
                showError("Errore nel menu", e.getMessage());
            }
        });

        gameView.setOnHintRequested(() -> {
            try {
                showHint();
            } catch (Exception e) {
                showError("Errore nel suggerimento", e.getMessage());
            }
        });

        gameView.setOnSolveRequested(() -> {
            try {
                if (gameView.isAutoSolving()) {
                    stopAutoSolving();
                } else {
                    solveAutomatically();
                }
            } catch (Exception e) {
                showError("Errore nella risoluzione", e.getMessage());
            }
        });

        gameView.setOnMoveRequested((fromTubeId, toTubeId) -> {
            try {
                executeMove(fromTubeId, toTubeId);
            } catch (Exception e) {
                showError("Errore nella mossa", e.getMessage());
            }
        });

    }


    private void handleInitializationError(Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore di inizializzazione");
            alert.setHeaderText("Impossibile inizializzare il gioco");
            alert.setContentText("Errore: " + e.getMessage() + "\n\nIl gioco verrà chiuso.");
            alert.showAndWait();
            returnToMenu();
        });
    }


    public void startGame() {
        try {
            if (!gameInitialized) {
                throw new IllegalStateException("Il gioco non è stato inizializzato correttamente");
            }

            if (gameView == null || gameView.getScene() == null) {
                throw new IllegalStateException("La vista del gioco non è disponibile");
            }

            primaryStage.setScene(gameView.getScene());
            primaryStage.setTitle("Bubble Sorting Game - " +
                    gameLevel.getDisplayName() + " - Livello " + levelNumber);

        } catch (Exception e) {
            returnToMenu();
        }
    }


    private void restartGame() {
        if (isProcessingMove) return;

        try {
            stopAutoSolving();

            gameState.reset();
            updateView();

        } catch (Exception e) {
            showError("Errore nel riavvio", e.getMessage());
        }
    }

    private void returnToMenu() {
        try {
            cleanup();

            if (menuController != null) {
                menuController.returnToMenu();
            }

        } catch (Exception e) {
            Platform.exit();
        }
    }

    private void handleTubeClick(int tubeId) {
        if (isProcessingMove || gameState.isGameWon() || gameView.isAutoSolving()) {
            return;
        }

        if (!GameEventHandler.EventValidator.isValidTubeId(tubeId, gameState.getTubes().size())) {
            return;
        }

        try {
            gameView.handleTubeSelection(tubeId, gameState);

        } catch (Exception e) {
            showError("Errore nel click", e.getMessage());
        }
    }


    public boolean executeMove(int fromTubeId, int toTubeId) {
        if (isProcessingMove || gameState.isGameWon()) {
            return false;
        }

        if (!GameEventHandler.EventValidator.isValidMove(fromTubeId, toTubeId, gameState.getTubes().size())) {
            return false;
        }

        try {
            isProcessingMove = true;

            boolean success = gameState.makeMove(fromTubeId, toTubeId);

            if (success) {
                if (gameView != null) {
                    gameView.animateMove(fromTubeId, toTubeId, () -> {
                        updateView();
                        checkWinCondition();
                        isProcessingMove = false;
                    });
                } else {
                    updateView();
                    checkWinCondition();
                    isProcessingMove = false;
                }
            } else {
                isProcessingMove = false;
            }

            return success;

        } catch (Exception e) {
            isProcessingMove = false;
            return false;
        }
    }

    private void showHint() {
        if (isShowingHint || gameState.isGameWon() || gameView.isAutoSolving()) {
            return;
        }

        if (!preferences.isShowHints()) {
            gameView.showMessage("Suggerimenti disabilitati nelle impostazioni");
            return;
        }

        gameView.showMessage("Elaborazione suggerimento...");
        isShowingHint = true;
        hintService.restart();
        gameView.lockTubeInteractions(true);
        gameView.setBusyCursor(true);

    }

    private void handleHintResult(ShowMove hint) {
        Platform.runLater(() -> {
            isShowingHint = false;

            if (hint != null) {
                Move moveHint = new Move(hint.getFrom() - 1, hint.getTo() - 1, null);
                gameView.showHint(moveHint);
                gameView.showMessage("Suggerimento: Sposta dal tubo " + hint.getFrom() + " al tubo " + hint.getTo());
            } else {
                gameView.showMessage("Nessun suggerimento disponibile");
            }
        });
    }


    private void handleHintError(Throwable error) {
        Platform.runLater(() -> {
            isShowingHint = false;
            gameView.showMessage("Errore nel calcolo del suggerimento");
        });
    }


    private void solveAutomatically() {
        if (isProcessingMove || gameState.isGameWon() || gameView.isAutoSolving()) {
            return;
        }

        gameView.showMessage("Calcolo della soluzione in corso...");
        gameView.lockTubeInteractions(true);
        gameView.setBusyCursor(true);
        solveService.restart();
    }


    private void stopAutoSolving() {
        if (gameView != null && gameView.isAutoSolving()) {
           gameView.stopAutoSolving();
        }
    }


    private void handleSolutionResult(List<ShowMove> solution) {
        Platform.runLater(() -> {
            if (solution != null && !solution.isEmpty()) {
                gameView.handleSolution(solution);
            } else {
                gameView.showMessage("Impossibile trovare una soluzione per questo puzzle");
            }
        });
    }


    private void handleSolutionError(Throwable error) {
        Platform.runLater(() -> {
            gameView.showMessage("Errore nel calcolo della soluzione");
        });
    }


    private void checkWinCondition() {
        if (gameState.isGameWon()) {
            handleGameWon();
        }
    }


    private void handleGameWon() {
        try {
            stopAutoSolving();

            preferences.setLevelCompleted(gameLevel, levelNumber, gameState.getMoves());

            Platform.runLater(() -> {
                gameView.showVictoryAnimation();
                gameView.showVictoryDialog(
                        gameState.getMoves(),
                        gameState.getScore(),
                        this::nextLevel,
                        this::restartGame,
                        this::returnToMenu
                );
            });

        } catch (Exception e) {
            System.err.println("Errore nella gestione della vittoria: " + e.getMessage());
        }
    }

    /**
     * Passa al livello successivo
     */
    private void nextLevel() {
        try {
            if (levelNumber < gameLevel.getMaxLevels()) {
               GameController nextController = new GameController(
                        primaryStage, menuController, gameLevel, levelNumber + 1);
                nextController.startGame();
            } else {
                // Ultimo livello della difficoltà
                GameLevel nextDifficulty = gameLevel.getNextLevel();
                if (nextDifficulty != null) {
                    GameController nextController = new GameController(
                            primaryStage, menuController, nextDifficulty, 1);
                    nextController.startGame();
                } else {
                    gameView.showMessage("Congratulazioni! Hai completato tutti i livelli!");
                    returnToMenu();
                }
            }
        } catch (Exception e) {
            returnToMenu();
        }
    }

    private class HintService extends Service<ShowMove> {
        @Override
        protected Task<ShowMove> createTask() {
            return new Task<ShowMove>() {
                @Override
                protected ShowMove call() throws Exception {
                    if (aspSolver == null) {
                        throw new Exception("AspSolver non disponibile");
                    }

                    // Utilizza il metodo getHint del solver ASP
                    return aspSolver.getHint(gameState);
                }
            };
        }
    }


    private class SolveService extends Service<List<ShowMove>> {
        @Override
        protected Task<List<ShowMove>> createTask() {
            return new Task<List<ShowMove>>() {
                @Override
                protected List<ShowMove> call() throws Exception {
                    if (aspSolver == null) {
                        throw new Exception("AspSolver non disponibile");
                    }

                    // Utilizza il metodo solve del solver ASP
                    return aspSolver.solve(gameState);
                }
            };
        }
    }

    private void updateView() {
        if (gameView != null && gameState != null) {
            Platform.runLater(() -> {
                try {
                    gameView.updateGameState(gameState);
                } catch (Exception e) {
                    System.err.println("Errore nell'aggiornamento della vista: " + e.getMessage());
                }
            });
        }
    }


    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Si è verificato un errore");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


    private void cleanup() {
        try {
            stopAutoSolving();

            if (hintService != null && hintService.isRunning()) {
                hintService.cancel();
            }

            if (solveService != null && solveService.isRunning()) {
                solveService.cancel();
            }

            if (aspSolver != null) {
                aspSolver.cleanup();
            }


        } catch (Exception e) {
            System.err.println("Errore nella pulizia: " + e.getMessage());
        }
    }


    public GameView getGameView() {
        return gameView;
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameLevel getGameLevel() {
        return gameLevel;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public boolean isGameInitialized() {
        return gameInitialized;
    }
}