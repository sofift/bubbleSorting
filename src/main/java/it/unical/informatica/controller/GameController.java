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

/**
 * Controller principale del gioco Bubble Sorting.
 * Gestisce la logica di gioco, l'interazione con ASP e la comunicazione con la vista.
 * Segue il pattern MVC e gestisce tutte le operazioni ASP in background.
 */
public class GameController {

    // ===== DIPENDENZE =====
    private final Stage primaryStage;
    private final MenuController menuController;
    private final GameLevel gameLevel;
    private final int levelNumber;

    // ===== COMPONENTI CORE =====
    private GameState gameState;
    private GameView gameView;
    private AspSolver aspSolver;
    private GamePreferences preferences;

    // ===== STATO DEL CONTROLLER =====
    private boolean isProcessingMove = false;
    private boolean isShowingHint = false;
    private boolean gameInitialized = false;

    // ===== SERVIZI BACKGROUND =====
    private HintService hintService;
    private SolveService solveService;

    // ===============================
    // COSTRUTTORE E INIZIALIZZAZIONE
    // ===============================

    /**
     * Costruttore del GameController
     * @param primaryStage Stage principale dell'applicazione
     * @param menuController Controller del menu per il ritorno
     * @param gameLevel Livello di difficoltà
     * @param levelNumber Numero del livello (1-5)
     */
    public GameController(Stage primaryStage, MenuController menuController,
                          GameLevel gameLevel, int levelNumber) {
        this.primaryStage = primaryStage;
        this.menuController = menuController;
        this.gameLevel = gameLevel;
        this.levelNumber = levelNumber;
        this.preferences = GamePreferences.getInstance();

        initializeComponents();
    }

    /**
     * Inizializza tutti i componenti del controller
     */
    private void initializeComponents() {
        try {
            System.out.println("🎮 Inizializzazione GameController...");

            // Crea lo stato del gioco
            gameState = new GameState(gameLevel, levelNumber);
            System.out.println("✅ GameState creato");

            // Crea la vista del gioco
            gameView = new GameView(gameLevel);
            System.out.println("✅ GameView creata");

            // Inizializza il solver ASP
            aspSolver = new AspSolver();
            System.out.println("✅ AspSolver inizializzato");

            // Configura gli event handlers
            setupEventHandlers();

            // Inizializza i servizi background
            initializeServices();

            // Aggiorna la vista con lo stato iniziale
            updateView();

            gameInitialized = true;
            System.out.println("✅ GameController inizializzato correttamente");

        } catch (Exception e) {
            System.err.println("❌ Errore nell'inizializzazione del GameController: " + e.getMessage());
            e.printStackTrace();
            handleInitializationError(e);
        }
    }

    /**
     * Inizializza i servizi per operazioni in background
     */
    private void initializeServices() {
        hintService = new HintService();
        solveService = new SolveService();
        System.out.println("✅ Servizi background inizializzati");
    }

    /**
     * Configura tutti gli event handlers della vista
     */
    private void setupEventHandlers() {
        if (gameView == null) return;

        // Handler per il click sui tubi
        gameView.setOnTubeClicked(this::handleTubeClick);

        // Handler per i pulsanti di controllo
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
                solveAutomatically();
            } catch (Exception e) {
                showError("Errore nella risoluzione", e.getMessage());
            }
        });

        gameView.setOnUndoRequested(() -> {
            try {
                undoLastMove();
            } catch (Exception e) {
                showError("Errore nell'annullamento", e.getMessage());
            }
        });

        // ✅ AGGIUNTO: Handler per l'esecuzione delle mosse
        gameView.setOnMoveRequested((fromTubeId, toTubeId) -> {
            try {
                executeMove(fromTubeId, toTubeId);
            } catch (Exception e) {
                showError("Errore nella mossa", e.getMessage());
            }
        });

        System.out.println("✅ Event handlers configurati");
    }

    /**
     * Gestisce gli errori di inizializzazione
     */
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

    // ===============================
    // AVVIO E CONTROLLO DEL GIOCO
    // ===============================

    /**
     * Avvia il gioco mostrando la vista
     */
    public void startGame() {
        try {
            if (!gameInitialized) {
                throw new IllegalStateException("Il gioco non è stato inizializzato correttamente");
            }

            if (gameView == null || gameView.getScene() == null) {
                throw new IllegalStateException("La vista del gioco non è disponibile");
            }

            // Mostra la scena del gioco
            primaryStage.setScene(gameView.getScene());
            primaryStage.setTitle("Bubble Sorting Game - " +
                    gameLevel.getDisplayName() + " - Livello " + levelNumber);

            System.out.println("🚀 Gioco avviato: " + gameLevel.getDisplayName() + " - Livello " + levelNumber);

        } catch (Exception e) {
            System.err.println("❌ Errore nell'avvio del gioco: " + e.getMessage());
            returnToMenu();
        }
    }

    /**
     * Riavvia il gioco allo stato iniziale
     */
    private void restartGame() {
        if (isProcessingMove) return;

        try {
            System.out.println("🔄 Riavvio del gioco...");

            gameState.reset();
            updateView();

            System.out.println("✅ Gioco riavviato");

        } catch (Exception e) {
            System.err.println("❌ Errore nel riavvio: " + e.getMessage());
            showError("Errore nel riavvio", e.getMessage());
        }
    }

    /**
     * Torna al menu principale
     */
    private void returnToMenu() {
        try {
            System.out.println("🏠 Ritorno al menu principale...");

            // Cleanup delle risorse
            cleanup();

            if (menuController != null) {
                menuController.returnToMenu();
            } else {
                System.err.println("❌ MenuController non disponibile");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore nel ritorno al menu: " + e.getMessage());
            // Forza la chiusura in caso di errore
            Platform.exit();
        }
    }

    // ===============================
    // GESTIONE DELLE MOSSE
    // ===============================

    /**
     * Gestisce il click su un tubo
     * @param tubeId ID del tubo cliccato
     */
    private void handleTubeClick(int tubeId) {
        if (isProcessingMove || gameState.isGameWon()) {
            System.out.println("⏸️ Click ignorato: gioco in elaborazione o già vinto");
            return;
        }

        if (!GameEventHandler.EventValidator.isValidTubeId(tubeId, gameState.getTubes().size())) {
            System.err.println("❌ ID tubo non valido: " + tubeId);
            return;
        }

        try {
            System.out.println("👆 Click su tubo: " + tubeId);
            gameView.handleTubeSelection(tubeId, gameState);

        } catch (Exception e) {
            System.err.println("❌ Errore nella gestione del click: " + e.getMessage());
            showError("Errore nel click", e.getMessage());
        }
    }

    /**
     * Esegue una mossa nel gioco
     * @param fromTubeId Tubo di origine
     * @param toTubeId Tubo di destinazione
     * @return true se la mossa è stata eseguita con successo
     */
    public boolean executeMove(int fromTubeId, int toTubeId) {
        if (isProcessingMove || gameState.isGameWon()) {
            return false;
        }

        if (!GameEventHandler.EventValidator.isValidMove(fromTubeId, toTubeId, gameState.getTubes().size())) {
            System.err.println("❌ Mossa non valida: " + fromTubeId + " -> " + toTubeId);
            return false;
        }

        try {
            isProcessingMove = true;

            System.out.println("🎯 Esecuzione mossa: " + fromTubeId + " -> " + toTubeId);

            boolean success = gameState.makeMove(fromTubeId, toTubeId);

            if (success) {
                System.out.println("✅ Mossa eseguita con successo");

                // ✅ ANIMAZIONE + AGGIORNAMENTO VISTA
                if (gameView != null) {
                    gameView.animateMove(fromTubeId, toTubeId, () -> {
                        // Callback dopo l'animazione
                        updateView();
                        checkWinCondition();
                        isProcessingMove = false;
                    });
                } else {
                    // Senza animazione
                    updateView();
                    checkWinCondition();
                    isProcessingMove = false;
                }
            } else {
                System.out.println("❌ Mossa fallita");
                isProcessingMove = false;
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Errore nell'esecuzione della mossa: " + e.getMessage());
            isProcessingMove = false;
            return false;
        }
    }

    /**
     * Annulla l'ultima mossa
     */
    private void undoLastMove() {
        if (isProcessingMove || !gameState.canUndo()) {
            System.out.println("⏸️ Impossibile annullare la mossa");
            return;
        }

        try {
            System.out.println("⏪ Annullamento ultima mossa...");

            boolean success = gameState.undoMove();

            if (success) {
                System.out.println("✅ Mossa annullata");
                updateView();
            } else {
                System.out.println("❌ Impossibile annullare la mossa");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore nell'annullamento: " + e.getMessage());
            showError("Errore nell'annullamento", e.getMessage());
        }
    }

    // ===============================
    // INTEGRAZIONE ASP
    // ===============================

    /**
     * Mostra un suggerimento usando ASP
     */
    private void showHint() {
        if (isShowingHint || gameState.isGameWon()) {
            System.out.println("⏸️ Suggerimento già in corso o gioco terminato");
            return;
        }

        if (!preferences.isShowHints()) {
            System.out.println("💡 Suggerimenti disabilitati nelle impostazioni");
            gameView.showMessage("Suggerimenti disabilitati nelle impostazioni");
            return;
        }

        System.out.println("💡 Richiesta suggerimento...");
        gameView.showMessage("Elaborazione suggerimento...");

        hintService.restart();
    }

    /**
     * Risolve automaticamente il puzzle usando ASP
     */
    private void solveAutomatically() {
        if (isProcessingMove || gameState.isGameWon()) {
            System.out.println("⏸️ Risoluzione automatica non disponibile");
            return;
        }

        System.out.println("🤖 Risoluzione automatica...");
        gameView.showMessage("Risoluzione puzzle in corso...");

        solveService.restart();
    }

    // ===============================
    // GESTIONE VITTORIA
    // ===============================

    /**
     * Controlla se il gioco è stato vinto e gestisce la vittoria
     */
    private void checkWinCondition() {
        if (gameState.isGameWon()) {
            handleGameWon();
        }
    }

    /**
     * Gestisce la vittoria del gioco
     */
    private void handleGameWon() {
        System.out.println("🎉 GIOCO VINTO!");

        try {
            // Salva il progresso
            preferences.setLevelCompleted(gameLevel, levelNumber, gameState.getMoves());

            // Mostra il messaggio di vittoria
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
            System.err.println("❌ Errore nella gestione della vittoria: " + e.getMessage());
        }
    }

    /**
     * Passa al livello successivo
     */
    private void nextLevel() {
        try {
            if (levelNumber < gameLevel.getMaxLevels()) {
                // Livello successivo nella stessa difficoltà
                System.out.println("➡️ Passaggio al livello successivo...");
                GameController nextController = new GameController(
                        primaryStage, menuController, gameLevel, levelNumber + 1);
                nextController.startGame();
            } else {
                // Ultimo livello della difficoltà
                GameLevel nextDifficulty = gameLevel.getNextLevel();
                if (nextDifficulty != null) {
                    System.out.println("⬆️ Passaggio alla difficoltà successiva...");
                    GameController nextController = new GameController(
                            primaryStage, menuController, nextDifficulty, 1);
                    nextController.startGame();
                } else {
                    System.out.println("🏆 Tutti i livelli completati!");
                    gameView.showMessage("Congratulazioni! Hai completato tutti i livelli!");
                    returnToMenu();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nel passaggio al livello successivo: " + e.getMessage());
            returnToMenu();
        }
    }

    // ===============================
    // SERVIZI BACKGROUND (ASP)
    // ===============================

    /**
     * Servizio per calcolare suggerimenti in background
     */
    private class HintService extends Service<ShowMove> {
        @Override
        protected Task<ShowMove> createTask() {
            return new Task<ShowMove>() {
                @Override
                protected ShowMove call() throws Exception {
                    // ✅ CORREZIONE: Usa il metodo corretto
                    //return aspSolver.getHint(gameState);
                    return null;
                }
            };
        }
    }

    /**
     * Servizio per la risoluzione automatica in background
     */
    private class SolveService extends Service<List<ShowMove>> {
        @Override
        protected Task<List<ShowMove>> createTask() {
            return new Task<List<ShowMove>>() {
                @Override
                protected List<ShowMove> call() throws Exception {
                    // ✅ CORREZIONE: Usa il metodo corretto
                    return aspSolver.solve(gameState);
                }
            };
        }
    }

    // ===============================
    // UTILITY E CLEANUP
    // ===============================

    /**
     * Aggiorna la vista con lo stato corrente del gioco
     */
    private void updateView() {
        if (gameView != null && gameState != null) {
            Platform.runLater(() -> {
                try {
                    gameView.updateGameState(gameState);
                } catch (Exception e) {
                    System.err.println("❌ Errore nell'aggiornamento della vista: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Mostra un messaggio di errore all'utente
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Si è verificato un errore");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Pulisce le risorse del controller
     */
    private void cleanup() {
        try {
            System.out.println("🧹 Pulizia risorse GameController...");

            if (hintService != null && hintService.isRunning()) {
                hintService.cancel();
            }

            if (solveService != null && solveService.isRunning()) {
                solveService.cancel();
            }

            if (aspSolver != null) {
                //aspSolver.cleanup();
            }

            System.out.println("✅ Risorse pulite");

        } catch (Exception e) {
            System.err.println("❌ Errore nella pulizia: " + e.getMessage());
        }
    }

    // ===============================
    // GETTERS PUBBLICI
    // ===============================

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