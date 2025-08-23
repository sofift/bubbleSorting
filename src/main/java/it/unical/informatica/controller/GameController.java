package it.unical.informatica.controller;

import it.unical.informatica.asp.AspSolver;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import it.unical.informatica.model.*;
import it.unical.informatica.view.GameView;

import java.util.List;

/**
 * Controller principale del gioco - VERSIONE CORRETTA
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
    private boolean isProcessingMove;
    private boolean isShowingHint;

    /**
     * ✅ COSTRUTTORE SENZA ECCEZIONI OBBLIGATORIE
     */
    public GameController(Stage primaryStage, MenuController menuController,
                          GameLevel gameLevel, int levelNumber) {
        this.primaryStage = primaryStage;
        this.menuController = menuController;
        this.gameLevel = gameLevel;
        this.levelNumber = levelNumber;
        this.isProcessingMove = false;
        this.isShowingHint = false;

        // ✅ Inizializza i componenti con gestione errori
        initializeComponents();
    }

    /**
     * ✅ Inizializza i componenti del gioco con gestione errori robusta
     */
    private void initializeComponents() {
        try {
            System.out.println("🚀 Inizializzando GameController...");

            // Crea il nuovo stato del gioco
            System.out.println("📝 Creando GameState...");
            gameState = new GameState(gameLevel, levelNumber);
            System.out.println("✅ GameState creato correttamente");

            // Crea la vista del gioco
            System.out.println("🎨 Creando GameView...");
            gameView = new GameView(gameLevel);
            System.out.println("✅ GameView creata correttamente");

            // Inizializza il solver ASP (con gestione errori)
            System.out.println("🧠 Inizializzando AspSolver...");
            aspSolver = new AspSolver(); // ✅ NON lancia eccezioni
            System.out.println("✅ AspSolver inizializzato");

            // Configura gli event handlers
            System.out.println("🔧 Configurando event handlers...");
            setupEventHandlers();
            System.out.println("✅ Event handlers configurati");

            // Aggiorna la vista con lo stato iniziale
            System.out.println("🔄 Aggiornando vista iniziale...");
            updateView();
            System.out.println("✅ GameController inizializzato correttamente");

        } catch (Exception e) {
            System.err.println("❌ ERRORE nell'inizializzazione del GameController: " + e.getMessage());
            e.printStackTrace();

            // ✅ FALLBACK: Crea componenti minimi per evitare crash
            createMinimalFallback();
        }
    }

    /**
     * ✅ Crea componenti minimi in caso di errore
     */
    private void createMinimalFallback() {
        try {
            System.out.println("🆘 Attivando modalità fallback...");

            if (gameState == null) {
                // Crea GameState semplice senza generatore
                gameState = GameState.createTestLevel(gameLevel, levelNumber);
            }

            if (gameView == null) {
                gameView = new GameView(gameLevel);
            }

            if (aspSolver == null) {
                aspSolver = new AspSolver();
            }

            setupEventHandlers();
            updateView();

            System.out.println("✅ Modalità fallback attivata");

        } catch (Exception fallbackError) {
            System.err.println("💥 ERRORE CRITICO nel fallback: " + fallbackError.getMessage());
            // In questo caso estremo, mostra un messaggio di errore
            showCriticalError(fallbackError);
        }
    }

    /**
     * ✅ Mostra errore critico all'utente
     */
    private void showCriticalError(Exception e) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Errore Critico");
            alert.setHeaderText("Impossibile inizializzare il gioco");
            alert.setContentText("Errore: " + e.getMessage() + "\n\nTornando al menu principale...");
            alert.showAndWait();

            // Torna al menu
            if (menuController != null) {
                menuController.returnToMenu();
            }
        });
    }

    /**
     * Configura gli event handlers per la vista
     */
    private void setupEventHandlers() {
        if (gameView == null) return;

        // Handler per i click sui tubi
        gameView.setOnTubeClicked(this::handleTubeClick);

        // Handler per il pulsante restart
        gameView.setOnRestartRequested(() -> {
            try {
                restartGame();
            } catch (Exception e) {
                System.err.println("Errore nel restart: " + e.getMessage());
            }
        });

        // Handler per il pulsante menu
        gameView.setOnMenuRequested(() -> {
            try {
                returnToMenu();
            } catch (Exception e) {
                System.err.println("Errore nel ritorno al menu: " + e.getMessage());
            }
        });

        // Handler per il pulsante hint
        gameView.setOnHintRequested(() -> {
            try {
                showHint();
            } catch (Exception e) {
                System.err.println("Errore nel suggerimento: " + e.getMessage());
            }
        });

        // Handler per il pulsante solve
        gameView.setOnSolveRequested(() -> {
            try {
                solveAutomatically();
            } catch (Exception e) {
                System.err.println("Errore nella risoluzione automatica: " + e.getMessage());
            }
        });

        // Handler per undo
        gameView.setOnUndoRequested(() -> {
            try {
                undoLastMove();
            } catch (Exception e) {
                System.err.println("Errore nell'undo: " + e.getMessage());
            }
        });
    }

    /**
     * Avvia il gioco
     */
    public void startGame() {
        try {
            if (gameView == null || gameView.getScene() == null) {
                System.err.println("❌ GameView non inizializzata correttamente");
                returnToMenu();
                return;
            }

            primaryStage.setScene(gameView.getScene());
            primaryStage.setTitle("Bubble Sorting Game - " + gameLevel.getDisplayName() +
                    " Livello " + levelNumber);

            System.out.println("✅ Gioco avviato correttamente");
        } catch (Exception e) {
            System.err.println("❌ Errore nell'avvio del gioco: " + e.getMessage());
            returnToMenu();
        }
    }

    /**
     * Gestisce il click su un tubo
     */
    private void handleTubeClick(int tubeId) {
        try {
            if (isProcessingMove || gameState.isGameWon()) {
                return;
            }

            System.out.println("Click su tubo: " + tubeId);
            gameView.handleTubeSelection(tubeId, gameState);
        } catch (Exception e) {
            System.err.println("Errore nel click del tubo: " + e.getMessage());
        }
    }

    /**
     * Aggiorna la vista con lo stato corrente del gioco
     */
    private void updateView() {
        try {
            if (gameView != null && gameState != null) {
                gameView.updateGameState(gameState);
            }
        } catch (Exception e) {
            System.err.println("Errore nell'aggiornamento della vista: " + e.getMessage());
        }
    }

    /**
     * Passa al livello successivo
     */
    private void nextLevel() {
        try {
            if (levelNumber < 5) {
                GameController nextController = new GameController(
                        primaryStage, menuController, gameLevel, levelNumber + 1);
                nextController.startGame();
            } else {
                gameView.showCompletionDialog(() -> returnToMenu());
            }
        } catch (Exception e) {
            System.err.println("Errore nel passaggio al livello successivo: " + e.getMessage());
        }
    }

    /**
     * Riavvia il livello corrente
     */
    private void restartGame() {
        try {
            gameState = new GameState(gameLevel, levelNumber);
            gameView.resetView();
            updateView();
        } catch (Exception e) {
            System.err.println("Errore nel restart del gioco: " + e.getMessage());
            // In caso di errore, torna al menu
            returnToMenu();
        }
    }

    /**
     * Torna al menu principale
     */
    private void returnToMenu() {
        try {
            menuController.returnToMenu();
        } catch (Exception e) {
            System.err.println("Errore nel ritorno al menu: " + e.getMessage());
        }
    }

    /**
     * Mostra un hint per la prossima mossa
     */
    private void showHint() {
        if (isShowingHint || gameState.isGameWon() || aspSolver == null) {
            return;
        }

        isShowingHint = true;
        gameView.showLoadingHint(true);

        // Esegue il calcolo dell'hint in background
        Task<GameState.Move> hintTask = new Task<GameState.Move>() {
            @Override
            protected GameState.Move call() throws Exception {
                return aspSolver.findBestMove(gameState);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    try {
                        GameState.Move hint = getValue();
                        gameView.showLoadingHint(false);

                        if (hint != null) {
                            gameView.highlightHint(hint.getFromTubeId(), hint.getToTubeId());
                        } else {
                            gameView.showNoHintAvailable();
                        }

                        isShowingHint = false;
                    } catch (Exception e) {
                        System.err.println("Errore nella gestione dell'hint: " + e.getMessage());
                        isShowingHint = false;
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    gameView.showLoadingHint(false);
                    gameView.showHintError();
                    isShowingHint = false;
                });
            }
        };

        Thread hintThread = new Thread(hintTask);
        hintThread.setDaemon(true);
        hintThread.start();
    }

    /**
     * Risolve automaticamente il gioco
     */
    private void solveAutomatically() {
        if (isProcessingMove || gameState.isGameWon() || aspSolver == null) {
            return;
        }

        gameView.showLoadingSolution(true);

        Task<List<GameState.Move>> solveTask = new Task<List<GameState.Move>>() {
            @Override
            protected List<GameState.Move> call() throws Exception {
                return aspSolver.solveGame(gameState);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    try {
                        List<GameState.Move> solution = getValue();
                        gameView.showLoadingSolution(false);

                        if (solution != null && !solution.isEmpty()) {
                            executeAutomaticSolution(solution);
                        } else {
                            gameView.showNoSolutionFound();
                        }
                    } catch (Exception e) {
                        System.err.println("Errore nella gestione della soluzione: " + e.getMessage());
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    gameView.showLoadingSolution(false);
                    gameView.showSolutionError();
                });
            }
        };

        Thread solveThread = new Thread(solveTask);
        solveThread.setDaemon(true);
        solveThread.start();
    }

    /**
     * Esegue automaticamente una sequenza di mosse
     */
    private void executeAutomaticSolution(List<GameState.Move> moves) {
        if (moves.isEmpty()) {
            return;
        }

        isProcessingMove = true;
        executeMovesSequentially(moves, 0);
    }

    /**
     * Esegue le mosse in sequenza con animazioni
     */
    private void executeMovesSequentially(List<GameState.Move> moves, int index) {
        try {
            if (index >= moves.size()) {
                isProcessingMove = false;
                return;
            }

            GameState.Move move = moves.get(index);

            // Esegui la mossa nel modello
            gameState.makeMove(move.getFromTubeId(), move.getToTubeId());

            // Anima la mossa
            gameView.animateMove(move.getFromTubeId(), move.getToTubeId(), () -> {
                updateView();

                if (index == moves.size() - 1) {
                    // Ultima mossa
                    if (gameState.isGameWon()) {
                        gameView.showWinAnimation(() -> {
                            gameView.showWinDialog(gameState.getMoves(), this::nextLevel, this::restartGame);
                        });
                    }
                    isProcessingMove = false;
                } else {
                    // Continua con la prossima mossa
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        executeMovesSequentially(moves, index + 1);
                    });
                }
            });
        } catch (Exception e) {
            System.err.println("Errore nell'esecuzione delle mosse: " + e.getMessage());
            isProcessingMove = false;
        }
    }

    /**
     * Annulla l'ultima mossa (funzionalità bonus)
     */
    private void undoLastMove() {
        gameView.showUndoNotAvailable();
    }

    // Getters per testing
    public GameState getGameState() {
        return gameState;
    }

    public GameView getGameView() {
        return gameView;
    }
}