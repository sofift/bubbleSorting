package it.unical.informatica.view;

import it.unical.informatica.controller.GameEventHandler;
import it.unical.informatica.controller.GamePreferences;
import it.unical.informatica.model.*;
import it.unical.informatica.asp.ShowMove;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Vista principale del gioco Bubble Sorting.
 * Gestisce la visualizzazione dell'interfaccia grafica, le animazioni
 * e l'interazione con l'utente seguendo il pattern MVC.
 */
public class GameView {

    // ===== CONFIGURAZIONE =====
    private static final double TUBE_WIDTH = 70;
    private static final double TUBE_HEIGHT = 150;
    private static final double BALL_RADIUS = 15;
    private static final double TUBE_SPACING = 20;
    private static final double ANIMATION_DURATION = 300; // millisecondi
    private static final double AUTO_SOLVE_DELAY = 700; // Pausa tra le mosse nella risoluzione automatica

    // ===== COMPONENTI CORE =====
    private final GameLevel gameLevel;
    private Scene scene;
    private VBox mainContainer;
    private HBox gameArea;
    private VBox controlPanel;
    private VBox statusPanel;

    // ===== STATO DELLA VISTA =====
    private GameState currentGameState;
    private List<TubeView> tubeViews;
    private int selectedTubeId = -1;
    private boolean animationsEnabled;
    private boolean hintsEnabled;

    // ===== STATO RISOLUZIONE AUTOMATICA =====
    private boolean isAutoSolving = false;
    private List<ShowMove> solutionMoves = new ArrayList<>();
    private int currentSolutionStep = 0;
    private Timeline autoSolveTimeline;
    private boolean interactionsLocked = false;


    // ===== COMPONENTI UI =====
    private Text movesText;
    private Text timeText;
    private Text messageText;
    private Button restartButton;
    private Button menuButton;
    private Button hintButton;
    private Button solveButton;
    private ProgressDialog progressDialog;

    // ===== EVENT HANDLERS =====
    private GameEventHandler.TubeClickHandler onTubeClicked;
    private GameEventHandler.ActionHandler onRestartRequested;
    private GameEventHandler.ActionHandler onMenuRequested;
    private GameEventHandler.ActionHandler onHintRequested;
    private GameEventHandler.ActionHandler onSolveRequested;
    private GameEventHandler.ActionHandler onUndoRequested;
    private GameEventHandler.MoveHandler onMoveRequested;

    // ===============================
    // COSTRUTTORE E INIZIALIZZAZIONE
    // ===============================

    /**
     * Costruttore della GameView
     * @param gameLevel Livello di difficoltà del gioco
     */
    public GameView(GameLevel gameLevel) {
        this.gameLevel = gameLevel;
        this.tubeViews = new ArrayList<>();

        // Carica le preferenze
        GamePreferences preferences = GamePreferences.getInstance();
        this.animationsEnabled = preferences.isAnimationsEnabled();
        this.hintsEnabled = preferences.isShowHints();

        initializeView();
        System.out.println("✅ GameView inizializzata per " + gameLevel.getDisplayName());
    }

    /**
     * Inizializza tutti i componenti della vista
     */
    private void initializeView() {
        try {
            createMainLayout();
            createGameArea();
            createControlPanel();
            createStatusPanel();
            createScene();

            System.out.println("✅ Layout della vista creato");

        } catch (Exception e) {
            System.err.println("❌ Errore nell'inizializzazione della vista: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea il layout principale
     */
    private void createMainLayout() {
        mainContainer = new VBox();
        mainContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        mainContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        mainContainer.getStyleClass().add("game-container");
        mainContainer.setSpacing(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);
    }

    /**
     * Crea l'area di gioco con i tubi
     */
    private void createGameArea() {
        gameArea = new HBox();
        gameArea.getStyleClass().add("game-area");
        gameArea.setSpacing(TUBE_SPACING);
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setPadding(new Insets(20));
        gameArea.setFillHeight(false);
        gameArea.setPrefWidth(Region.USE_COMPUTED_SIZE);
        gameArea.setMinWidth(Region.USE_COMPUTED_SIZE);
        gameArea.setMaxWidth(Region.USE_PREF_SIZE);


        // Crea i tubi vuoti inizialmente
        for (int i = 0; i < gameLevel.getNumberOfTubes(); i++) {
            TubeView tubeView = new TubeView(i);
            tubeViews.add(tubeView);
            gameArea.getChildren().add(tubeView.getContainer());
        }

        mainContainer.getChildren().add(gameArea);
    }

    /**
     * Crea il pannello di controllo
     */
    private void createControlPanel() {
        controlPanel = new VBox();
        controlPanel.getStyleClass().add("control-panel");
        controlPanel.setSpacing(15);
        controlPanel.setAlignment(Pos.CENTER);

        // Riga superiore - pulsanti principali
        HBox mainButtons = new HBox();
        mainButtons.setSpacing(10);
        mainButtons.setAlignment(Pos.CENTER);

        restartButton = createControlButton("🔄 Riavvia", "control-button");
        menuButton = createControlButton("🏠 Menu", "control-button secondary-button");
        mainButtons.getChildren().addAll(restartButton, menuButton);

        // Riga inferiore - pulsanti AI
        HBox aiButtons = new HBox();
        aiButtons.setSpacing(10);
        aiButtons.setAlignment(Pos.CENTER);

        hintButton = createControlButton("💡 Suggerimento", "control-button ai-button");
        solveButton = createControlButton("🤖 Risolvi", "control-button ai-button");

        aiButtons.getChildren().addAll(hintButton, solveButton);

        // Event handlers
        setupControlButtonHandlers();

        controlPanel.getChildren().addAll(mainButtons, aiButtons);
        mainContainer.getChildren().add(controlPanel);
    }

    /**
     * Crea il pannello di stato
     */
    private void createStatusPanel() {
        statusPanel = new VBox();
        statusPanel.getStyleClass().add("status-panel");
        statusPanel.setSpacing(10);
        statusPanel.setAlignment(Pos.CENTER);
        statusPanel.setPadding(new Insets(10));

        // Informazioni del livello
        Text levelInfo = new Text(gameLevel.getDisplayName());
        levelInfo.getStyleClass().add("level-info");

        // Statistiche di gioco
        HBox statsBox = new HBox();
        statsBox.setSpacing(20);
        statsBox.setAlignment(Pos.CENTER);

        movesText = new Text("Mosse: 0");
        movesText.getStyleClass().add("stat-text");

        timeText = new Text("Tempo: 00:00");
        timeText.getStyleClass().add("stat-text");

        statsBox.getChildren().addAll(movesText, timeText);

        // Area messaggi
        messageText = new Text("");
        messageText.getStyleClass().add("message-text");
        messageText.setWrappingWidth(400);

        statusPanel.getChildren().addAll(levelInfo, statsBox, messageText);
        mainContainer.getChildren().add(statusPanel);
    }

    /**
     * Crea la scena principale
     */
    private void createScene() {
        scene = new Scene(mainContainer, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    }

    /**
     * Crea un pulsante di controllo con stile
     */
    private Button createControlButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll(styleClass.split(" "));
        return button;
    }

    /**
     * Configura gli event handlers per i pulsanti di controllo
     */
    private void setupControlButtonHandlers() {
        restartButton.setOnAction(e -> {
            if (onRestartRequested != null) {
                try {
                    stopAutoSolving(); // Ferma la risoluzione automatica se in corso
                    onRestartRequested.onAction();
                } catch (Exception ex) {
                    System.err.println("❌ Errore nel restart: " + ex.getMessage());
                }
            }
        });

        menuButton.setOnAction(e -> {
            if (onMenuRequested != null) {
                try {
                    stopAutoSolving(); // Ferma la risoluzione automatica se in corso
                    onMenuRequested.onAction();
                } catch (Exception ex) {
                    System.err.println("❌ Errore nel menu: " + ex.getMessage());
                }
            }
        });


        hintButton.setOnAction(e -> {
            if (onHintRequested != null && !isAutoSolving) {
                try {
                    onHintRequested.onAction();
                } catch (Exception ex) {
                    System.err.println("❌ Errore nel hint: " + ex.getMessage());
                }
            }
        });

        solveButton.setOnAction(e -> {
            if (onSolveRequested != null) {
                try {
                    if (isAutoSolving) {
                        stopAutoSolving();
                    } else {
                        onSolveRequested.onAction();
                    }
                } catch (Exception ex) {
                    System.err.println("❌ Errore nella risoluzione: " + ex.getMessage());
                }
            }
        });
    }

    // ===============================
    // AGGIORNAMENTO STATO DI GIOCO
    // ===============================

    /**
     * Aggiorna la vista con un nuovo stato di gioco
     * @param gameState Nuovo stato del gioco
     */
    public void updateGameState(GameState gameState) {
        if (gameState == null) return;

        try {
            this.currentGameState = gameState;

            // Aggiorna i tubi
            updateTubes(gameState.getTubes());

            // Aggiorna le statistiche
            updateStats(gameState);

            // Aggiorna lo stato dei pulsanti
            updateButtonStates(gameState);

            System.out.println("✅ Vista aggiornata - Mosse: " + gameState.getMoves());

        } catch (Exception e) {
            System.err.println("❌ Errore nell'aggiornamento della vista: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Aggiorna la visualizzazione dei tubi
     */
    private void updateTubes(List<Tube> tubes) {
        for (int i = 0; i < tubes.size() && i < tubeViews.size(); i++) {
            tubeViews.get(i).updateTube(tubes.get(i));
        }
    }

    /**
     * Aggiorna le statistiche visualizzate
     */
    private void updateStats(GameState gameState) {
        Platform.runLater(() -> {
            movesText.setText("Mosse: " + gameState.getMoves());

            long seconds = gameState.getGameTimeSeconds();
            int minutes = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            timeText.setText(String.format("Tempo: %02d:%02d", minutes, secs));
        });
    }

    /**
     * Aggiorna lo stato dei pulsanti in base al gioco
     */
    private void updateButtonStates(GameState gameState) {
        Platform.runLater(() -> {
            if (gameState.isGameWon()) {
                hintButton.setDisable(true);
                solveButton.setDisable(true);
            } else {
                hintButton.setDisable(!hintsEnabled || isAutoSolving);

                if (isAutoSolving) {
                    solveButton.setText("⏹ Stop");
                    solveButton.getStyleClass().removeAll("ai-button");
                    solveButton.getStyleClass().add("danger-button");
                } else {
                    solveButton.setText("🤖 Risolvi");
                    solveButton.getStyleClass().removeAll("danger-button");
                    solveButton.getStyleClass().add("ai-button");
                    solveButton.setDisable(false);
                }
            }
        });
    }

    // ===============================
    // GESTIONE SELEZIONE TUBI
    // ===============================

    /**
     * Gestisce la selezione di un tubo
     * @param tubeId ID del tubo selezionato
     * @param gameState Stato corrente del gioco
     */
    public void handleTubeSelection(int tubeId, GameState gameState) {
        if (isAutoSolving) {
            showMessage("Risoluzione automatica in corso...");
            return;
        }

        if (selectedTubeId == -1) {
            // Prima selezione
            selectTube(tubeId, gameState);
        } else if (selectedTubeId == tubeId) {
            // Deseleziona lo stesso tubo
            deselectTube();
        } else {
            // Seconda selezione - tenta la mossa
            attemptMove(selectedTubeId, tubeId, gameState);
        }
    }

    /**
     * Seleziona un tubo
     */
    private void selectTube(int tubeId, GameState gameState) {
        Tube tube = gameState.getTube(tubeId);
        if (tube == null || tube.isEmpty()) {
            showMessage("Il tubo è vuoto!");
            return;
        }

        selectedTubeId = tubeId;
        tubeViews.get(tubeId).setSelected(true);
        showMessage("Tubo " + (tubeId + 1) + " selezionato. Clicca su un altro tubo per spostare.");

        // Evidenzia i tubi validi per la mossa
        highlightValidMoves(tubeId, gameState);
    }

    /**
     * Deseleziona il tubo corrente
     */
    private void deselectTube() {
        if (selectedTubeId != -1) {
            tubeViews.get(selectedTubeId).setSelected(false);
            selectedTubeId = -1;
            clearHighlights();
            showMessage("");
        }
    }

    /**
     * Tenta di eseguire una mossa
     */
    private void attemptMove(int fromTubeId, int toTubeId, GameState gameState) {
        // Verifica se la mossa è valida
        Tube fromTube = gameState.getTube(fromTubeId);
        Tube toTube = gameState.getTube(toTubeId);

        if (fromTube == null || toTube == null || fromTube.isEmpty()) {
            showMessage("Mossa non valida!");
            deselectTube();
            return;
        }

        Ball ballToMove = fromTube.getTopBall();
        if (!toTube.canAddBall(ballToMove)) {
            showMessage("Impossibile spostare la pallina qui!");
            deselectTube();
            return;
        }

        // Notifica al GameController di eseguire la mossa
        if (onMoveRequested != null) {
            onMoveRequested.onMove(fromTubeId, toTubeId);
        }

        deselectTube();
        showMessage(""); // Pulisce il messaggio
    }

    /**
     * Evidenzia i tubi dove è possibile spostare la pallina
     */
    private void highlightValidMoves(int fromTubeId, GameState gameState) {
        Tube fromTube = gameState.getTube(fromTubeId);
        if (fromTube == null || fromTube.isEmpty()) return;

        Ball ballToMove = fromTube.getTopBall();

        for (int i = 0; i < gameState.getTubes().size(); i++) {
            if (i == fromTubeId) continue;

            Tube targetTube = gameState.getTube(i);
            if (targetTube != null && targetTube.canAddBall(ballToMove)) {
                tubeViews.get(i).setHighlighted(true);
            }
        }
    }

    /**
     * Rimuove tutti gli highlight dai tubi
     */
    private void clearHighlights() {
        for (TubeView tubeView : tubeViews) {
            tubeView.setSelected(false);
            tubeView.setHighlighted(false);
        }
    }

    public void setTubesInteractive(boolean enabled) {
        interactionsLocked = !enabled;
        if (gameArea != null) {
            gameArea.setDisable(!enabled); // disabilita tutti i figli (tubi) in cascata
        }
    }
    public void lockTubeInteractions(boolean lock) {
        setTubesInteractive(!lock);
    }
    public void setBusyCursor(boolean busy) {
        if (scene != null) {
            scene.setCursor(busy ? javafx.scene.Cursor.WAIT : javafx.scene.Cursor.DEFAULT);
        }
    }

    // ===============================
    // ANIMAZIONI
    // ===============================

    /**
     * Anima il movimento di una pallina da un tubo all'altro
     */
    public void animateMove(int fromTubeId, int toTubeId, Runnable onComplete) {
        if (!animationsEnabled) {
            // Senza animazione, esegui subito il callback
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        try {
            System.out.println("🎬 Avvio animazione: " + fromTubeId + " → " + toTubeId);

            // Ottieni i contenitori dei tubi
            VBox fromContainer = tubeViews.get(fromTubeId).getContainer();
            VBox toContainer = tubeViews.get(toTubeId).getContainer();

            // Animazione migliorata con movimento reale della pallina
            createBallMoveAnimation(fromContainer, toContainer, onComplete);

        } catch (Exception e) {
            System.err.println("❌ Errore nell'animazione: " + e.getMessage());
            // In caso di errore, esegui comunque il callback
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * Crea un'animazione realistica del movimento della pallina
     */
    private void createBallMoveAnimation(VBox fromContainer, VBox toContainer, Runnable onComplete) {
        // Per ora manteniamo l'animazione semplice, ma funzionale
        Timeline timeline = new Timeline();

        // Animazione di evidenziazione dei tubi coinvolti
        KeyFrame highlight = new KeyFrame(Duration.millis(50), e -> {
            fromContainer.getStyleClass().add("moving-from");
            toContainer.getStyleClass().add("moving-to");
        });

        // Rimozione dell'evidenziazione e callback
        KeyFrame finish = new KeyFrame(Duration.millis(ANIMATION_DURATION), e -> {
            fromContainer.getStyleClass().removeAll("moving-from");
            toContainer.getStyleClass().removeAll("moving-to");
            if (onComplete != null) {
                onComplete.run();
            }
        });

        timeline.getKeyFrames().addAll(highlight, finish);
        timeline.play();
    }

    /**
     * Mostra l'animazione di vittoria
     */
    public void showVictoryAnimation() {
        if (!animationsEnabled) return;

        try {
            // Animazione semplice di celebrazione
            for (TubeView tubeView : tubeViews) {
                tubeView.playVictoryAnimation();
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nell'animazione di vittoria: " + e.getMessage());
        }
    }

    // ===============================
    // RISOLUZIONE AUTOMATICA - NUOVE FUNZIONALITÀ
    // ===============================

    /**
     * Avvia la risoluzione automatica animata del puzzle
     * @param solution Lista delle mosse da eseguire
     */
    public void startAutoSolve(List<ShowMove> solution) {
        if (solution == null || solution.isEmpty()) {
            showMessage("Nessuna soluzione trovata!");
            return;
        }

        if (isAutoSolving) {
            showMessage("Risoluzione automatica già in corso...");
            return;
        }

        System.out.println("🤖 Avvio risoluzione automatica con " + solution.size() + " mosse");

        this.solutionMoves = new ArrayList<>(solution);
        this.currentSolutionStep = 0;
        this.isAutoSolving = true;

        // Disabilita l'interazione dell'utente
        disableUserInteraction();

        // Mostra messaggio informativo
        showMessage("🤖 Risoluzione automatica in corso... Mossa 1/" + solution.size());

        // Avvia l'esecuzione delle mosse
        executeNextSolutionMove();
    }

    /**
     * Esegue la prossima mossa della soluzione
     */
    private void executeNextSolutionMove() {
        if (!isAutoSolving || currentSolutionStep >= solutionMoves.size()) {
            // Soluzione completata
            finishAutoSolve();
            return;
        }

        ShowMove move = solutionMoves.get(currentSolutionStep);

        // Converti gli ID da ASP (1-based) a Java (0-based)
        int fromTube = move.getFrom() - 1;
        int toTube = move.getTo() - 1;

        System.out.println("🎯 Esecuzione mossa automatica " + (currentSolutionStep + 1) + "/" +
                solutionMoves.size() + ": " + (fromTube + 1) + " → " + (toTube + 1));

        // Evidenzia i tubi coinvolti nella mossa
        highlightAutoSolveMove(fromTube, toTube);

        // Esegui la mossa tramite il controller
        if (onMoveRequested != null) {
            onMoveRequested.onMove(fromTube, toTube);
        }

        currentSolutionStep++;

        // Aggiorna il messaggio
        if (currentSolutionStep < solutionMoves.size()) {
            showMessage("🤖 Risoluzione automatica in corso... Mossa " +
                    (currentSolutionStep + 1) + "/" + solutionMoves.size());
        }

        // Programma la prossima mossa dopo un delay
        scheduleNextMove();
    }

    /**
     * Programma l'esecuzione della prossima mossa
     */
    private void scheduleNextMove() {
        if (!isAutoSolving) return;

        autoSolveTimeline = new Timeline(new KeyFrame(
                Duration.millis(AUTO_SOLVE_DELAY),
                e -> executeNextSolutionMove()
        ));
        autoSolveTimeline.play();
    }

    /**
     * Evidenzia i tubi coinvolti nella mossa automatica
     */
    private void highlightAutoSolveMove(int fromTube, int toTube) {
        Platform.runLater(() -> {
            // Pulisce tutti gli highlight precedenti
            clearHighlights();

            // Evidenzia i tubi della mossa corrente
            if (fromTube >= 0 && fromTube < tubeViews.size()) {
                tubeViews.get(fromTube).setAutoSolveHighlight(true);
            }
            if (toTube >= 0 && toTube < tubeViews.size()) {
                tubeViews.get(toTube).setAutoSolveHighlight(true);
            }

            // Rimuovi l'evidenziazione dopo un breve periodo
            Timeline clearHighlight = new Timeline(new KeyFrame(
                    Duration.millis(AUTO_SOLVE_DELAY * 0.7),
                    e -> {
                        if (fromTube >= 0 && fromTube < tubeViews.size()) {
                            tubeViews.get(fromTube).setAutoSolveHighlight(false);
                        }
                        if (toTube >= 0 && toTube < tubeViews.size()) {
                            tubeViews.get(toTube).setAutoSolveHighlight(false);
                        }
                    }
            ));
            clearHighlight.play();
        });
    }

    /**
     * Ferma la risoluzione automatica
     */
    public void stopAutoSolving() {
        if (!isAutoSolving) return;

        System.out.println("⏹ Interruzione risoluzione automatica");

        isAutoSolving = false;
        solutionMoves.clear();
        currentSolutionStep = 0;

        // Ferma il timer se attivo
        if (autoSolveTimeline != null) {
            autoSolveTimeline.stop();
            autoSolveTimeline = null;
        }

        // Riabilita l'interazione dell'utente
        enableUserInteraction();

        // Pulisce gli highlight
        clearHighlights();
        clearAutoSolveHighlights();

        // Aggiorna i pulsanti
        updateButtonStates(currentGameState);

        showMessage("Risoluzione automatica interrotta");
    }

    /**
     * Completa la risoluzione automatica
     */
    private void finishAutoSolve() {
        System.out.println("✅ Risoluzione automatica completata!");

        isAutoSolving = false;
        solutionMoves.clear();
        currentSolutionStep = 0;

        // Riabilita l'interazione dell'utente
        enableUserInteraction();

        // Pulisce gli highlight
        clearHighlights();
        clearAutoSolveHighlights();

        // Aggiorna i pulsanti
        updateButtonStates(currentGameState);

        showMessage("🎉 Puzzle risolto automaticamente!");

        // Mostra animazione di vittoria se il gioco è completato
        if (currentGameState != null && currentGameState.isGameWon()) {
            showVictoryAnimation();
        }
    }

    /**
     * Disabilita l'interazione dell'utente durante la risoluzione automatica
     */
    private void disableUserInteraction() {
        Platform.runLater(() -> {
            // Disabilita i click sui tubi
            for (TubeView tubeView : tubeViews) {
                tubeView.setInteractionEnabled(false);
            }

            // Aggiorna lo stato dei pulsanti
            updateButtonStates(currentGameState);
        });
    }

    /**
     * Riabilita l'interazione dell'utente
     */
    private void enableUserInteraction() {
        Platform.runLater(() -> {
            // Riabilita i click sui tubi
            for (TubeView tubeView : tubeViews) {
                tubeView.setInteractionEnabled(true);
            }
        });
    }

    /**
     * Pulisce tutti gli highlight della risoluzione automatica
     */
    private void clearAutoSolveHighlights() {
        for (TubeView tubeView : tubeViews) {
            tubeView.setAutoSolveHighlight(false);
        }
    }

    // ===============================
    // FUNZIONALITÀ ASP
    // ===============================

    /**
     * Mostra un suggerimento visualmente
     */
    public void showHint(Move hint) {
        if (hint == null) return;

        try {
            Platform.runLater(() -> {
                // Evidenzia la mossa suggerita
                tubeViews.get(hint.getFromTubeId()).setHinted(true);
                tubeViews.get(hint.getToTubeId()).setHinted(true);

                // Rimuovi l'hint dopo qualche secondo
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> clearHints())
                );
                timeline.play();
            });

        } catch (Exception e) {
            System.err.println("❌ Errore nella visualizzazione del hint: " + e.getMessage());
        }
    }

    /**
     * Gestisce la soluzione ASP - METODO PRINCIPALE PER LA RISOLUZIONE AUTOMATICA
     */
    public void handleSolution(List<ShowMove> solution) {
        if (solution == null || solution.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Nessuna soluzione");
                alert.setHeaderText("Puzzle non risolvibile");
                alert.setContentText("Non è stato possibile trovare una soluzione per questo puzzle.");
                alert.showAndWait();
            });
            return;
        }

        // Avvia la risoluzione automatica animata
        startAutoSolve(solution);
    }

    /**
     * Mostra la soluzione completa (modalità precedente - ora deprecata)
     */
    @Deprecated
    public void showSolution(List<Move> solution) {
        if (solution == null || solution.isEmpty()) return;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Soluzione trovata");
            alert.setHeaderText("Soluzione in " + solution.size() + " mosse");

            StringBuilder content = new StringBuilder();
            for (int i = 0; i < Math.min(solution.size(), 10); i++) {
                Move move = solution.get(i);
                content.append(String.format("%d. Tubo %d → Tubo %d\n",
                        i + 1, move.getFromTubeId() + 1, move.getToTubeId() + 1));
            }

            if (solution.size() > 10) {
                content.append("... e altre ").append(solution.size() - 10).append(" mosse");
            }

            alert.setContentText(content.toString());
            alert.showAndWait();
        });
    }

    /**
     * Rimuove tutti i suggerimenti visivi
     */
    private void clearHints() {
        for (TubeView tubeView : tubeViews) {
            tubeView.setHinted(false);
        }
    }

    // ===============================
    // DIALOGHI E MESSAGGI
    // ===============================

    /**
     * Mostra un messaggio all'utente
     */
    public void showMessage(String message) {
        Platform.runLater(() -> {
            if (messageText != null) {
                messageText.setText(message);

                // Fade out automatico dopo 3 secondi
                if (!message.isEmpty()) {
                    Timeline fadeOut = new Timeline(
                            new KeyFrame(Duration.seconds(3), e -> {
                                if (messageText.getText().equals(message)) {
                                    messageText.setText("");
                                }
                            })
                    );
                    fadeOut.play();
                }
            }
        });
    }

    /**
     * Mostra il dialogo di vittoria
     */
    public void showVictoryDialog(int moves, int score, Runnable onNextLevel,
                                  Runnable onRestart, Runnable onMenu) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Livello Completato!");
            alert.setHeaderText("Congratulazioni!");

            String content = String.format(
                    "Hai completato il livello!\n\n" +
                            "Statistiche:\n" +
                            "• Mosse utilizzate: %d\n" +
                            "• Punteggio: %d punti\n" +
                            "• Livello: %s\n\n" +
                            "Cosa vuoi fare ora?",
                    moves, score, gameLevel.getDisplayName()
            );

            alert.setContentText(content);

            // Pulsanti personalizzati
            ButtonType nextButton = new ButtonType("Livello Successivo");
            ButtonType restartButton = new ButtonType("Ricomincia");
            ButtonType menuButton = new ButtonType("Menu Principale");

            alert.getButtonTypes().setAll(nextButton, restartButton, menuButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == nextButton && onNextLevel != null) {
                    onNextLevel.run();
                } else if (response == restartButton && onRestart != null) {
                    onRestart.run();
                } else if (onMenu != null) {
                    onMenu.run();
                }
            });
        });
    }

    // ===============================
    // IMPOSTAZIONI
    // ===============================

    /**
     * Abilita o disabilita le animazioni
     */
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
        System.out.println("Animazioni " + (enabled ? "abilitate" : "disabilitate"));
    }

    /**
     * Abilita o disabilita i suggerimenti
     */
    public void setHintsEnabled(boolean enabled) {
        this.hintsEnabled = enabled;
        hintButton.setDisable(!enabled);
        System.out.println("Suggerimenti " + (enabled ? "abilitati" : "disabilitati"));
    }

    // ===============================
    // CLASSE INTERNA TUBEVIEW - VERSIONE ESTESA
    // ===============================

    /**
     * Classe per rappresentare visualmente un singolo tubo
     */
    private class TubeView {
        private final int tubeId;
        private VBox container;
        private Rectangle tubeBase;
        private VBox ballContainer;
        private List<Circle> ballCircles;
        private boolean selected = false;
        private boolean highlighted = false;
        private boolean hinted = false;
        private boolean autoSolveHighlighted = false;
        private boolean interactionEnabled = true;

        public TubeView(int tubeId) {
            this.tubeId = tubeId;
            this.ballCircles = new ArrayList<>();
            initializeTube();
        }

        private void initializeTube() {
            container = new VBox();
            container.setAlignment(Pos.BOTTOM_CENTER);
            container.setSpacing(5);
            container.setPrefWidth(TUBE_WIDTH);
            container.getStyleClass().add("tube-container");

            // Numero del tubo
            Text tubeNumber = new Text(String.valueOf(tubeId + 1));
            tubeNumber.getStyleClass().add("tube-number");

            // Container per le palline
            ballContainer = new VBox();
            ballContainer.setAlignment(Pos.BOTTOM_CENTER);
            ballContainer.setPrefHeight(TUBE_HEIGHT);
            ballContainer.setMaxHeight(TUBE_HEIGHT);

            // Base del tubo
            tubeBase = new Rectangle(TUBE_WIDTH, 10);
            tubeBase.getStyleClass().add("tube-base");
            tubeBase.setFill(Color.DARKGRAY);

            container.getChildren().addAll(tubeNumber, ballContainer, tubeBase);

            // Click handler
            container.setOnMouseClicked(e -> {
                if (onTubeClicked != null && interactionEnabled) {
                    onTubeClicked.onTubeClicked(tubeId);
                }
            });
        }

        public void updateTube(Tube tube) {
            ballContainer.getChildren().clear();
            ballCircles.clear();

            for (Ball ball : tube.getBalls()) {
                Circle ballCircle = createBallCircle(ball);
                ballCircles.add(ballCircle);
                ballContainer.getChildren().add(0, ballCircle); // Aggiungi in cima
            }

            updateVisualState();
        }

        private Circle createBallCircle(Ball ball) {
            Circle circle = new Circle(BALL_RADIUS);
            circle.getStyleClass().add("ball");

            // Imposta il colore
            String colorHex = ball.getColor().getHexColor();
            circle.setFill(Color.web(colorHex));
            circle.setStroke(Color.DARKGRAY);
            circle.setStrokeWidth(1);

            return circle;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            updateVisualState();
        }

        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
            updateVisualState();
        }

        public void setHinted(boolean hinted) {
            this.hinted = hinted;
            updateVisualState();
        }

        public void setAutoSolveHighlight(boolean highlighted) {
            this.autoSolveHighlighted = highlighted;
            updateVisualState();
        }

        public void setInteractionEnabled(boolean enabled) {
            this.interactionEnabled = enabled;
            container.setDisable(!enabled);
        }

        private void updateVisualState() {
            container.getStyleClass().removeAll("selected", "highlighted", "hinted", "auto-solve-highlight");

            if (selected) {
                container.getStyleClass().add("selected");
            }
            if (highlighted) {
                container.getStyleClass().add("highlighted");
            }
            if (hinted) {
                container.getStyleClass().add("hinted");
            }
            if (autoSolveHighlighted) {
                container.getStyleClass().add("auto-solve-highlight");
            }
        }

        public void playVictoryAnimation() {
            if (!animationsEnabled) return;

            RotateTransition rotation = new RotateTransition(Duration.seconds(0.5), container);
            rotation.setByAngle(360);
            rotation.setCycleCount(2);
            rotation.play();
        }

        public VBox getContainer() {
            return container;
        }
    }

    // ===============================
    // GETTERS E SETTERS
    // ===============================

    public Scene getScene() {
        return scene;
    }

    public GameLevel getGameLevel() {
        return gameLevel;
    }

    public boolean isAutoSolving() {
        return isAutoSolving;
    }

    // Event handlers setters
    public void setOnTubeClicked(GameEventHandler.TubeClickHandler handler) {
        this.onTubeClicked = handler;
    }

    public void setOnRestartRequested(GameEventHandler.ActionHandler handler) {
        this.onRestartRequested = handler;
    }

    public void setOnMenuRequested(GameEventHandler.ActionHandler handler) {
        this.onMenuRequested = handler;
    }

    public void setOnHintRequested(GameEventHandler.ActionHandler handler) {
        this.onHintRequested = handler;
    }

    public void setOnSolveRequested(GameEventHandler.ActionHandler handler) {
        this.onSolveRequested = handler;
    }

    public void setOnUndoRequested(GameEventHandler.ActionHandler handler) {
        this.onUndoRequested = handler;
    }

    public void setOnMoveRequested(GameEventHandler.MoveHandler handler) {
        this.onMoveRequested = handler;
    }
}