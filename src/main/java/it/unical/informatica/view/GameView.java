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

public class GameView {
    private static final double TUBE_WIDTH = 70;
    private static final double TUBE_HEIGHT = 150;
    private static final double BALL_RADIUS = 15;
    private static final double TUBE_SPACING = 20;
    private static final double ANIMATION_DURATION = 200; // millisecondi
    private static final double AUTO_SOLVE_DELAY = 500; // Pausa tra le mosse nella risoluzione automatica

    private final GameLevel gameLevel;
    private Scene scene;
    private VBox mainContainer;
    private HBox gameArea;
    private VBox controlPanel;
    private VBox statusPanel;

    private GameState currentGameState;
    private List<TubeView> tubeViews;
    private int selectedTubeId = -1;
    private boolean animationsEnabled;
    private boolean hintsEnabled;

    private boolean isAutoSolving = false;
    private List<ShowMove> solutionMoves = new ArrayList<>();
    private int currentSolutionStep = 0;
    private Timeline autoSolveTimeline;
    private boolean interactionsLocked = false;


   private Text movesText;
    private Text timeText;
    private Text messageText;
    private Button restartButton;
    private Button menuButton;
    private Button hintButton;
    private Button solveButton;

    private GameEventHandler.TubeClickHandler onTubeClicked;
    private GameEventHandler.ActionHandler onRestartRequested;
    private GameEventHandler.ActionHandler onMenuRequested;
    private GameEventHandler.ActionHandler onHintRequested;
    private GameEventHandler.ActionHandler onSolveRequested;
    private GameEventHandler.MoveHandler onMoveRequested;


    public GameView(GameLevel gameLevel) {
        this.gameLevel = gameLevel;
        this.tubeViews = new ArrayList<>();

        GamePreferences preferences = GamePreferences.getInstance();
        this.animationsEnabled = preferences.isAnimationsEnabled();
        this.hintsEnabled = preferences.isShowHints();

        initializeView();
    }


    private void initializeView() {
        try {
            createMainLayout();
            createGameArea();
            createControlPanel();
            createStatusPanel();
            createScene();


        } catch (Exception e) {
            System.err.println("Errore nell'inizializzazione della vista: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void createMainLayout() {
        mainContainer = new VBox();
        mainContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        mainContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        mainContainer.getStyleClass().add("game-container");
        mainContainer.setSpacing(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);
    }


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

    private void createControlPanel() {
        controlPanel = new VBox();
        controlPanel.getStyleClass().add("control-panel");
        controlPanel.setSpacing(15);
        controlPanel.setAlignment(Pos.CENTER);

        // Riga superiore : pulsanti principali
        HBox mainButtons = new HBox();
        mainButtons.setSpacing(10);
        mainButtons.setAlignment(Pos.CENTER);

        restartButton = createControlButton("Riavvia", "control-button");
        menuButton = createControlButton("Menu", "control-button secondary-button");
        mainButtons.getChildren().addAll(restartButton, menuButton);

        // Riga inferiore : ris. auto
        HBox aiButtons = new HBox();
        aiButtons.setSpacing(10);
        aiButtons.setAlignment(Pos.CENTER);

        hintButton = createControlButton("Suggerimento", "control-button ai-button");
        solveButton = createControlButton("Risolvi", "control-button ai-button");

        aiButtons.getChildren().addAll(hintButton, solveButton);

        // Event handlers
        setupControlButtonHandlers();

        controlPanel.getChildren().addAll(mainButtons, aiButtons);
        mainContainer.getChildren().add(controlPanel);
    }


    private void createStatusPanel() {
        statusPanel = new VBox();
        statusPanel.getStyleClass().add("status-panel");
        statusPanel.setSpacing(10);
        statusPanel.setAlignment(Pos.CENTER);
        statusPanel.setPadding(new Insets(10));

        // info del livello
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

        statusPanel.getChildren().addAll(levelInfo, messageText);
        mainContainer.getChildren().add(statusPanel);
    }


    private void createScene() {
        scene = new Scene(mainContainer, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    }


    private Button createControlButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll(styleClass.split(" "));
        return button;
    }


    private void setupControlButtonHandlers() {
        restartButton.setOnAction(e -> {
            if (onRestartRequested != null) {
                try {
                    stopAutoSolving(); // Ferma la risoluzione automatica se in corso
                    onRestartRequested.onAction();
                } catch (Exception ex) {
                    System.err.println("Errore nel restart: " + ex.getMessage());
                }
            }
        });

        menuButton.setOnAction(e -> {
            if (onMenuRequested != null) {
                try {
                    stopAutoSolving(); // Ferma la risoluzione automatica se in corso
                    onMenuRequested.onAction();
                } catch (Exception ex) {
                    System.err.println("Errore nel menu: " + ex.getMessage());
                }
            }
        });


        hintButton.setOnAction(e -> {
            hintButton.setDisable(true);
            if (onHintRequested != null && !isAutoSolving) {
                try {
                    onHintRequested.onAction();
                } catch (Exception ex) {
                    System.err.println("Errore nel hint: " + ex.getMessage());
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
                    System.err.println("Errore nella risoluzione: " + ex.getMessage());
                }
            }
        });
    }


    public void updateGameState(GameState gameState) {
        if (gameState == null) return;

        try {
            this.currentGameState = gameState;

            updateTubes(gameState.getTubes());

            updateStats(gameState);

            updateButtonStates(gameState);


        } catch (Exception e) {
            System.err.println("Errore nell'aggiornamento della vista: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void updateTubes(List<Tube> tubes) {
        for (int i = 0; i < tubes.size() && i < tubeViews.size(); i++) {
            tubeViews.get(i).updateTube(tubes.get(i));
        }
    }


    private void updateStats(GameState gameState) {
        Platform.runLater(() -> {
            movesText.setText("Mosse: " + gameState.getMoves());

            long seconds = gameState.getGameTimeSeconds();
            int minutes = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            timeText.setText(String.format("Tempo: %02d:%02d", minutes, secs));
        });
    }


    private void updateButtonStates(GameState gameState) {
        Platform.runLater(() -> {
            if (gameState.isGameWon()) {
                hintButton.setDisable(true);
                solveButton.setDisable(true);
            } else {
                hintButton.setDisable(!hintsEnabled || isAutoSolving);

                if (isAutoSolving) {
                    solveButton.setText("Stop");
                    solveButton.getStyleClass().removeAll("ai-button");
                    solveButton.getStyleClass().add("danger-button");
                } else {
                    solveButton.setText("Risolvi");
                    solveButton.getStyleClass().removeAll("danger-button");
                    solveButton.getStyleClass().add("ai-button");
                    solveButton.setDisable(false);
                }
            }
        });
    }


    public void handleTubeSelection(int tubeId, GameState gameState) {
        if (isAutoSolving) {
            showMessage("Risoluzione automatica in corso...", "auto-solving");
            return;
        }

        if (selectedTubeId == -1) {
            selectTube(tubeId, gameState);
        } else if (selectedTubeId == tubeId) {
            deselectTube();
        } else {
            attemptMove(selectedTubeId, tubeId, gameState);
        }
    }


    private void selectTube(int tubeId, GameState gameState) {
        Tube tube = gameState.getTube(tubeId);
        if (tube == null || tube.isEmpty()) {
            showMessage("Il tubo è vuoto!");
            return;
        }

        selectedTubeId = tubeId;
        tubeViews.get(tubeId).setSelected(true);
        showMessage("Tubo " + (tubeId + 1) + " selezionato. Clicca su un altro tubo per spostare.", "info");
        // Evidenzia i tubi validi per la mossa
        highlightValidMoves(tubeId, gameState);
    }


    private void deselectTube() {
        if (selectedTubeId != -1) {
            tubeViews.get(selectedTubeId).setSelected(false);
            selectedTubeId = -1;
            clearHighlights();
            showMessage("");
        }
    }


    private void attemptMove(int fromTubeId, int toTubeId, GameState gameState) {
        Tube fromTube = gameState.getTube(fromTubeId);
        Tube toTube = gameState.getTube(toTubeId);

        if (fromTube == null || toTube == null || fromTube.isEmpty()) {
            showMessage("Mossa non valida!", "error");deselectTube();
            return;
        }

        Ball ballToMove = fromTube.getTopBall();
        if (!toTube.canAddBall(ballToMove)) {
            showMessage("Impossibile spostare la pallina qui!", "error");
            deselectTube();
            return;
        }

        if (onMoveRequested != null) {
            onMoveRequested.onMove(fromTubeId, toTubeId);
        }

        deselectTube();
        showMessage("");
    }


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


    private void clearHighlights() {
        for (TubeView tubeView : tubeViews) {
            tubeView.setSelected(false);
            tubeView.setHighlighted(false);
        }
    }

    public void setTubesInteractive(boolean enabled) {
        interactionsLocked = !enabled;
        if (gameArea != null) {
            gameArea.setDisable(!enabled);
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


    public void animateMove(int fromTubeId, int toTubeId, Runnable onComplete) {
        if (!animationsEnabled) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        try {

            VBox fromContainer = tubeViews.get(fromTubeId).getContainer();
            VBox toContainer = tubeViews.get(toTubeId).getContainer();

            createBallMoveAnimation(fromContainer, toContainer, onComplete);

        } catch (Exception e) {
            System.err.println("Errore nell'animazione: " + e.getMessage());
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }


    private void createBallMoveAnimation(VBox fromContainer, VBox toContainer, Runnable onComplete) {
        Timeline timeline = new Timeline();

        KeyFrame highlight = new KeyFrame(Duration.millis(50), e -> {
            fromContainer.getStyleClass().add("moving-from");
            toContainer.getStyleClass().add("moving-to");
        });

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


    public void showVictoryAnimation() {
        if (!animationsEnabled) return;

        try {
            for (TubeView tubeView : tubeViews) {
                tubeView.playVictoryAnimation();
            }
        } catch (Exception e) {
            System.err.println("Errore nell'animazione di vittoria: " + e.getMessage());
        }
    }

    public void startAutoSolve(List<ShowMove> solution) {
        if (solution == null || solution.isEmpty()) {
            showMessage("Nessuna soluzione trovata!", "error");
            return;
        }

        if (isAutoSolving) {
            showMessage("Risoluzione automatica già in corso...", "auto-solving");
            return;
        }


        this.solutionMoves = new ArrayList<>(solution);
        this.currentSolutionStep = 0;
        this.isAutoSolving = true;

        disableUserInteraction();

        showMessage("Risoluzione automatica in corso... Mossa 1/" + solution.size(), "auto-solving");

        executeNextSolutionMove();
    }


    private void executeNextSolutionMove() {
        if (!isAutoSolving || currentSolutionStep >= solutionMoves.size()) {
            finishAutoSolve();
            return;
        }

        ShowMove move = solutionMoves.get(currentSolutionStep);

        int fromTube = move.getFrom() - 1;
        int toTube = move.getTo() - 1;


        highlightAutoSolveMove(fromTube, toTube);

        if (onMoveRequested != null) {
            onMoveRequested.onMove(fromTube, toTube);
        }

        currentSolutionStep++;

        if (currentSolutionStep < solutionMoves.size()) {
            showMessage("Risoluzione automatica in corso... Mossa " +
                    (currentSolutionStep + 1) + "/" + solutionMoves.size(), "auto-solving");
        }

        scheduleNextMove();
    }


    private void scheduleNextMove() {
        if (!isAutoSolving) return;

        autoSolveTimeline = new Timeline(new KeyFrame(
                Duration.millis(AUTO_SOLVE_DELAY),
                e -> executeNextSolutionMove()
        ));
        autoSolveTimeline.play();
    }


    private void highlightAutoSolveMove(int fromTube, int toTube) {
        Platform.runLater(() -> {
            clearHighlights();

            if (fromTube >= 0 && fromTube < tubeViews.size()) {
                tubeViews.get(fromTube).setAutoSolveHighlight(true);
            }
            if (toTube >= 0 && toTube < tubeViews.size()) {
                tubeViews.get(toTube).setAutoSolveHighlight(true);
            }

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


    public void stopAutoSolving() {
        if (!isAutoSolving) return;


        isAutoSolving = false;
        solutionMoves.clear();
        currentSolutionStep = 0;

         if (autoSolveTimeline != null) {
            autoSolveTimeline.stop();
            autoSolveTimeline = null;
        }

        enableUserInteraction();

        clearHighlights();
        clearAutoSolveHighlights();

        updateButtonStates(currentGameState);

        showMessage("Risoluzione automatica interrotta", "info");
    }

    private void finishAutoSolve() {

        isAutoSolving = false;
        solutionMoves.clear();
        currentSolutionStep = 0;

        enableUserInteraction();

         clearHighlights();
         clearAutoSolveHighlights();

        updateButtonStates(currentGameState);

        showMessage("Puzzle risolto automaticamente!", "success");

        // Mostra animazione di vittoria se il gioco è completato
        if (currentGameState != null && currentGameState.isGameWon()) {
            showVictoryAnimation();
        }
    }


    private void disableUserInteraction() {
        Platform.runLater(() -> {
            for (TubeView tubeView : tubeViews) {
                tubeView.setInteractionEnabled(false);
            }

            updateButtonStates(currentGameState);
        });
    }


    private void enableUserInteraction() {
        Platform.runLater(() -> {
            for (TubeView tubeView : tubeViews) {
                tubeView.setInteractionEnabled(true);
            }
        });
    }


    private void clearAutoSolveHighlights() {
        for (TubeView tubeView : tubeViews) {
            tubeView.setAutoSolveHighlight(false);
        }
    }

    public void showHint(Move hint) {
        if (hint == null) return;

        try {
            Platform.runLater(() -> {
                // evidenzia la mossa suggerita
                tubeViews.get(hint.getFromTubeId()).setHinted(true);
                tubeViews.get(hint.getToTubeId()).setHinted(true);

                // rimuovi l'hint dopo qualche secondo
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> clearHints())
                );
                timeline.play();
            });

        } catch (Exception e) {
            System.err.println("Errore nella visualizzazione del hint: " + e.getMessage());
        }
    }


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


    private void clearHints() {
        for (TubeView tubeView : tubeViews) {
            tubeView.setHinted(false);
        }
    }


    public void showMessage(String message, String messageType) {
        Platform.runLater(() -> {
            if (messageText != null) {
                // Rimuovi tutte le classi di stile precedenti
                messageText.getStyleClass().removeAll("auto-solving", "success", "error", "info", "pulsing");

                messageText.setText(message);

                // Applica lo stile specifico
                if (messageType != null && !messageType.isEmpty()) {
                    messageText.getStyleClass().add(messageType);
                }

                if (!message.isEmpty() && !"auto-solving".equals(messageType)) {
                    Timeline fadeOut = new Timeline(
                            new KeyFrame(Duration.seconds(10), e -> {
                                if (messageText.getText().equals(message)) {
                                    messageText.setText("");
                                    messageText.getStyleClass().removeAll("auto-solving", "success", "error", "info", "pulsing");
                                }
                            })
                    );
                    fadeOut.play();
                }
            }
        });
    }

    public void showMessage(String message) {
        showMessage(message, null);
    }

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

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );
            dialogPane.getStyleClass().add("dialog-pane");

            ButtonType nextButton = new ButtonType("Livello Successivo", ButtonBar.ButtonData.OK_DONE);
            ButtonType restartButton = new ButtonType("Ricomincia", ButtonBar.ButtonData.OTHER);
            ButtonType menuButton = new ButtonType("Menu Principale", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(nextButton, restartButton, menuButton);

            Button btnNext = (Button) dialogPane.lookupButton(nextButton);
            btnNext.getStyleClass().add("dialog-button-primary");

            Button btnRestart = (Button) dialogPane.lookupButton(restartButton);
            btnRestart.getStyleClass().add("dialog-button-secondary");

            Button btnMenu = (Button) dialogPane.lookupButton(menuButton);
            btnMenu.getStyleClass().add("dialog-button-secondary");

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



    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }


    public void setHintsEnabled(boolean enabled) {
        this.hintsEnabled = enabled;
        hintButton.setDisable(!enabled);
    }


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

            Text tubeNumber = new Text(String.valueOf(tubeId + 1));
            tubeNumber.getStyleClass().add("tube-number");

            ballContainer = new VBox();
            ballContainer.setAlignment(Pos.BOTTOM_CENTER);
            ballContainer.setPrefHeight(TUBE_HEIGHT);
            ballContainer.setMaxHeight(TUBE_HEIGHT);

            // Base del tubo
            tubeBase = new Rectangle(TUBE_WIDTH, 10);
            tubeBase.getStyleClass().add("tube-base");
            tubeBase.setFill(Color.DARKGRAY);

            container.getChildren().addAll(tubeNumber, ballContainer, tubeBase);

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
                ballContainer.getChildren().add(0, ballCircle);
            }

            updateVisualState();
        }

        private Circle createBallCircle(Ball ball) {
            Circle circle = new Circle(BALL_RADIUS);
            circle.getStyleClass().add("ball");

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

    public Scene getScene() {
        return scene;
    }

    public GameLevel getGameLevel() {
        return gameLevel;
    }

    public boolean isAutoSolving() {
        return isAutoSolving;
    }

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

    public void setOnMoveRequested(GameEventHandler.MoveHandler handler) {
        this.onMoveRequested = handler;
    }
}