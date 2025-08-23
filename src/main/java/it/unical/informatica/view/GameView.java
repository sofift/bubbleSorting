package it.unical.informatica.view;

import it.unical.mat.embasp.languages.IllegalAnnotationException;
import it.unical.mat.embasp.languages.ObjectNotValidException;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.application.Platform;
import it.unical.informatica.model.*;
import it.unical.informatica.controller.GameEventHandler;
import java.util.*;

/**
 * Vista principale del gioco con tubi e palline
 * Gestisce l'interfaccia grafica, le animazioni e l'interazione utente
 */
public class GameView {

    // Costanti per il layout
    private static final double BALL_SIZE = 25;
    private static final double TUBE_WIDTH = 70;
    private static final double TUBE_SPACING = 15;
    private static final double ANIMATION_DURATION = 0.6;

    // Componenti principali
    private Scene scene;
    private StackPane rootPane;
    private BorderPane mainContainer;
    private VBox gameHeader;
    private HBox gameArea;
    private VBox loadingOverlay;

    // Componenti UI
    private Text levelText;
    private Text movesText;
    private Button hintButton;
    private Button solveButton;
    private Button undoButton;
    private ProgressIndicator loadingIndicator;
    private Text loadingText;

    // Gestione tubi e gioco
    private final GameLevel gameLevel;
    private final List<TubeView> tubeViews;
    private GameState currentGameState;
    private TubeView selectedTube;

    // Impostazioni
    private boolean animationsEnabled = true;
    private boolean soundEnabled = true;
    private boolean hintsEnabled = true;

    // Event handlers
    private GameEventHandler.TubeClickHandler onTubeClicked;
    private GameEventHandler.ActionHandler onRestartRequested;
    private GameEventHandler.ActionHandler onMenuRequested;
    private GameEventHandler.ActionHandler onHintRequested;
    private GameEventHandler.ActionHandler onSolveRequested;
    private GameEventHandler.ActionHandler onUndoRequested;

    /**
     * Costruttore
     */
    public GameView(GameLevel gameLevel) {
        this.gameLevel = gameLevel;
        this.tubeViews = new ArrayList<>();

        initializeComponents();
        createGameScene();
        setupStyles();
    }

    /**
     * Inizializza tutti i componenti
     */
    private void initializeComponents() {
        // Crea i tubi
        for (int i = 0; i < gameLevel.getNumberOfTubes(); i++) {
            TubeView tubeView = new TubeView(i, gameLevel.getTubeCapacity());
            tubeViews.add(tubeView);
        }
    }

    /**
     * Crea la scena del gioco
     */
    private void createGameScene() {
        // Container principale
        mainContainer = new BorderPane();
        mainContainer.getStyleClass().add("game-container");

        // Header del gioco
        createGameHeader();
        mainContainer.setTop(gameHeader);

        // Area di gioco con i tubi
        createGameArea();
        mainContainer.setCenter(gameArea);

        // Overlay per loading
        createLoadingOverlay();

        // Root pane con overlay
        rootPane = new StackPane();
        rootPane.getChildren().addAll(mainContainer, loadingOverlay);

        // Scena finale
        scene = new Scene(rootPane, 1000, 700);
    }

    /**
     * Crea l'header del gioco con informazioni e controlli
     */
    private void createGameHeader() {
        gameHeader = new VBox();
        gameHeader.setSpacing(15);
        gameHeader.setPadding(new Insets(20));
        gameHeader.getStyleClass().add("game-header");

        // Riga informazioni
        HBox infoRow = createInfoRow();

        // Riga controlli
        HBox controlRow = createControlRow();

        gameHeader.getChildren().addAll(infoRow, controlRow);
    }

    /**
     * Crea la riga con le informazioni del gioco
     */
    private HBox createInfoRow() {
        HBox infoRow = new HBox();
        infoRow.setAlignment(Pos.CENTER);
        infoRow.setSpacing(30);

        levelText = new Text();
        levelText.getStyleClass().add("game-info");

        movesText = new Text("Mosse: 0");
        movesText.getStyleClass().add("game-info");

        infoRow.getChildren().addAll(levelText, movesText);
        return infoRow;
    }

    /**
     * Crea la riga con i controlli del gioco
     */
    private HBox createControlRow() {
        HBox controlRow = new HBox();
        controlRow.setAlignment(Pos.CENTER);
        controlRow.setSpacing(15);

        // Pulsanti di navigazione
        Button menuButton = createControlButton("🏠 Menu", "control-button");
        menuButton.setOnAction(e -> {
            if (onMenuRequested != null) {
                try {
                    onMenuRequested.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        Button restartButton = createControlButton("🔄 Restart", "control-button");
        restartButton.setOnAction(e -> {
            if (onRestartRequested != null) {
                try {
                    onRestartRequested.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Pulsanti di aiuto
        undoButton = createControlButton("↶ Undo", "control-button");
        undoButton.setOnAction(e -> {
            if (onUndoRequested != null) {
                try {
                    onUndoRequested.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        hintButton = createControlButton("💡 Suggerimento", "control-button");
        hintButton.setOnAction(e -> {
            if (onHintRequested != null) {
                try {
                    onHintRequested.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        solveButton = createControlButton("🤖 Risolvi", "control-button");
        solveButton.setOnAction(e -> {
            if (onSolveRequested != null) {
                try {
                    onSolveRequested.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        controlRow.getChildren().addAll(
                menuButton, restartButton, undoButton, hintButton, solveButton
        );

        return controlRow;
    }

    /**
     * Crea un pulsante di controllo
     */
    private Button createControlButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    /**
     * Crea l'area di gioco con i tubi
     */
    private void createGameArea() {
        gameArea = new HBox();
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setSpacing(TUBE_SPACING);
        gameArea.setPadding(new Insets(30));

        // Aggiungi tutti i tubi
        for (TubeView tubeView : tubeViews) {
            gameArea.getChildren().add(tubeView.getContainer());

            // Configura il click handler per ogni tubo
            tubeView.setOnClick(() -> {
                if (onTubeClicked != null) {
                    onTubeClicked.onTubeClicked(tubeView.getTubeId());
                }
            });
        }
    }

    /**
     * Crea l'overlay per il loading
     */
    private void createLoadingOverlay() {
        loadingOverlay = new VBox();
        loadingOverlay.getStyleClass().add("loading-indicator");
        loadingOverlay.setAlignment(Pos.CENTER);
        loadingOverlay.setSpacing(20);
        loadingOverlay.setVisible(false);

        VBox loadingContent = new VBox();
        loadingContent.getStyleClass().add("loading-content");
        loadingContent.setAlignment(Pos.CENTER);
        loadingContent.setSpacing(15);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);

        loadingText = new Text("Calcolando...");
        loadingText.getStyleClass().add("loading-text");

        loadingContent.getChildren().addAll(loadingIndicator, loadingText);
        loadingOverlay.getChildren().add(loadingContent);
    }

    /**
     * Applica gli stili CSS
     */
    private void setupStyles() {
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    }

    /**
     * Aggiorna la vista con un nuovo stato del gioco
     */
    public void updateGameState(GameState gameState) {
        this.currentGameState = gameState;

        // Aggiorna le informazioni nell'header
        updateGameInfo();

        // Aggiorna i tubi
        updateTubes();

        // Reset selezione
        clearSelection();
    }

    /**
     * Aggiorna le informazioni del gioco
     */
    private void updateGameInfo() {
        levelText.setText(String.format("%s - Livello %d",
                currentGameState.getLevel().getDisplayName(),
                currentGameState.getCurrentLevelNumber()));
        movesText.setText("Mosse: " + currentGameState.getMoves());
    }

    /**
     * Aggiorna tutti i tubi
     */
    private void updateTubes() {
        List<Tube> tubes = currentGameState.getTubes();
        for (int i = 0; i < tubes.size() && i < tubeViews.size(); i++) {
            tubeViews.get(i).updateTube(tubes.get(i));
        }
    }

    /**
     * Gestisce la selezione di un tubo
     */
    public void handleTubeSelection(int tubeId, GameState gameState) {
        TubeView clickedTube = tubeViews.get(tubeId);
        Tube tube = gameState.getTubeById(tubeId);

        if (selectedTube == null) {
            // Prima selezione
            if (!tube.isEmpty()) {
                selectTube(clickedTube);
            }
        } else if (selectedTube == clickedTube) {
            // Deseleziona lo stesso tubo
            clearSelection();
        } else {
            // Seconda selezione - esegui mossa
            executeMoveFromSelection(gameState, selectedTube.getTubeId(), clickedTube.getTubeId());
        }
    }

    /**
     * Seleziona un tubo
     */
    private void selectTube(TubeView tubeView) {
        selectedTube = tubeView;
        tubeView.setSelected(true);
    }

    /**
     * Pulisce la selezione corrente
     */
    private void clearSelection() {
        if (selectedTube != null) {
            selectedTube.setSelected(false);
            selectedTube = null;
        }
        clearAllHints();
    }

    /**
     * Esegue una mossa dalla selezione
     */
    private void executeMoveFromSelection(GameState gameState, int fromTubeId, int toTubeId) {
        Tube fromTube = gameState.getTubeById(fromTubeId);
        Tube toTube = gameState.getTubeById(toTubeId);

        if (fromTube == null || toTube == null || !fromTube.canMoveTo(toTube)) {
            showInvalidMoveAnimation(fromTubeId, toTubeId);
            clearSelection();
            return;
        }

        // Esegui la mossa nel modello
        boolean success = gameState.makeMove(fromTubeId, toTubeId);

        if (success) {
            // Anima la mossa
            animateMove(fromTubeId, toTubeId, () -> {
                updateGameState(gameState);
                // Usa la versione semplice del check per le mosse manuali
                checkWinCondition();
            });
        } else {
            showInvalidMoveAnimation(fromTubeId, toTubeId);
            clearSelection();
        }
    }

    /**
     * Controlla se il gioco è stato vinto (versione semplice per uso interno)
     */
    private void checkWinCondition() {
        if (currentGameState.isGameWon()) {
            showWinAnimation(() -> {
                showWinDialog(currentGameState.getMoves(), null, null);
            });
        }
    }

    /**
     * Controlla se il gioco è stato vinto e mostra il dialog appropriato
     */
    public void checkWinConditionWithHandlers(GameEventHandler.ActionHandler onNextLevel,
                                              GameEventHandler.ActionHandler onRestart) {
        if (currentGameState.isGameWon()) {
            showWinAnimation(() -> {
                showWinDialog(currentGameState.getMoves(), onNextLevel, onRestart);
            });
        }
    }

    /**
     * Anima una mossa tra due tubi
     */
    public void animateMove(int fromTubeId, int toTubeId, GameEventHandler.AnimationCompleteHandler onComplete) {
        if (!animationsEnabled) {
            Platform.runLater(onComplete::onAnimationComplete);
            return;
        }

        TubeView fromTube = tubeViews.get(fromTubeId);
        TubeView toTube = tubeViews.get(toTubeId);

        Circle ballToMove = fromTube.getTopBall();
        if (ballToMove == null) {
            Platform.runLater(onComplete::onAnimationComplete);
            return;
        }

        // Crea pallina animata
        Circle animatedBall = createAnimatedBall(ballToMove);

        // Calcola posizioni
        double[] positions = calculateAnimationPositions(fromTube, toTube);
        double startX = positions[0], startY = positions[1];
        double endX = positions[2], endY = positions[3];

        // Imposta posizione iniziale
        animatedBall.setCenterX(startX);
        animatedBall.setCenterY(startY);

        // Aggiungi alla scena
        rootPane.getChildren().add(animatedBall);
        ballToMove.setVisible(false);

        // Crea e avvia animazione
        Timeline animation = createMoveAnimation(animatedBall, startX, startY, endX, endY);
        animation.setOnFinished(e -> {
            rootPane.getChildren().remove(animatedBall);
            Platform.runLater(onComplete::onAnimationComplete);
        });
        animation.play();

        clearSelection();
    }

    /**
     * Crea una pallina per l'animazione
     */
    private Circle createAnimatedBall(Circle original) {
        Circle animatedBall = new Circle(original.getRadius());
        animatedBall.setFill(original.getFill());
        animatedBall.setStroke(original.getStroke());
        animatedBall.setStrokeWidth(original.getStrokeWidth());
        animatedBall.setEffect(original.getEffect());
        animatedBall.getStyleClass().addAll(original.getStyleClass());
        animatedBall.getStyleClass().add("moving");
        return animatedBall;
    }

    /**
     * Calcola le posizioni per l'animazione
     */
    private double[] calculateAnimationPositions(TubeView fromTube, TubeView toTube) {
        double startX = fromTube.getCenterX();
        double startY = fromTube.getTopBallY();
        double endX = toTube.getCenterX();
        double endY = toTube.getNextBallY();

        return new double[]{startX, startY, endX, endY};
    }

    /**
     * Crea l'animazione di movimento
     */
    private Timeline createMoveAnimation(Circle ball, double startX, double startY, double endX, double endY) {
        Timeline timeline = new Timeline();

        // Movimento verso l'alto
        KeyFrame upFrame = new KeyFrame(
                Duration.seconds(ANIMATION_DURATION * 0.3),
                new KeyValue(ball.centerYProperty(), startY - 50, Interpolator.EASE_OUT)
        );

        // Movimento orizzontale
        KeyFrame horizontalFrame = new KeyFrame(
                Duration.seconds(ANIMATION_DURATION * 0.7),
                new KeyValue(ball.centerXProperty(), endX, Interpolator.EASE_BOTH)
        );

        // Movimento verso il basso
        KeyFrame downFrame = new KeyFrame(
                Duration.seconds(ANIMATION_DURATION),
                new KeyValue(ball.centerYProperty(), endY, Interpolator.EASE_IN)
        );

        timeline.getKeyFrames().addAll(upFrame, horizontalFrame, downFrame);
        return timeline;
    }

    /**
     * Mostra animazione per mossa non valida
     */
    public void showInvalidMoveAnimation(int fromTubeId, int toTubeId) {
        TubeView fromTube = tubeViews.get(fromTubeId);
        TubeView toTube = tubeViews.get(toTubeId);

        // Animazione di shake
        for (TubeView tube : Arrays.asList(fromTube, toTube)) {
            VBox container = tube.getContainer();

            Timeline shake = new Timeline();
            double originalX = container.getTranslateX();

            KeyFrame[] frames = {
                    new KeyFrame(Duration.seconds(0.1), new KeyValue(container.translateXProperty(), originalX + 5)),
                    new KeyFrame(Duration.seconds(0.2), new KeyValue(container.translateXProperty(), originalX - 5)),
                    new KeyFrame(Duration.seconds(0.3), new KeyValue(container.translateXProperty(), originalX + 3)),
                    new KeyFrame(Duration.seconds(0.4), new KeyValue(container.translateXProperty(), originalX - 3)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(container.translateXProperty(), originalX))
            };

            shake.getKeyFrames().addAll(Arrays.asList(frames));
            shake.play();
        }
    }

    /**
     * Evidenzia un suggerimento
     */
    public void highlightHint(int fromTubeId, int toTubeId) {
        clearAllHints();

        tubeViews.get(fromTubeId).setHintSource(true);
        tubeViews.get(toTubeId).setHintTarget(true);

        // Rimuovi hint dopo 3 secondi
        Timeline removeHint = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> clearAllHints())
        );
        removeHint.play();
    }

    /**
     * Rimuove tutti i suggerimenti
     */
    private void clearAllHints() {
        tubeViews.forEach(tube -> {
            tube.setHintSource(false);
            tube.setHintTarget(false);
        });
    }

    /**
     * Mostra animazione di vittoria
     */
    public void showWinAnimation(GameEventHandler.AnimationCompleteHandler onComplete) {
        Timeline celebration = new Timeline();

        for (int i = 0; i < tubeViews.size(); i++) {
            TubeView tube = tubeViews.get(i);
            VBox container = tube.getContainer();

            KeyFrame bounceUp = new KeyFrame(
                    Duration.seconds(0.2 + i * 0.1),
                    new KeyValue(container.scaleYProperty(), 1.1, Interpolator.EASE_OUT)
            );
            KeyFrame bounceDown = new KeyFrame(
                    Duration.seconds(0.4 + i * 0.1),
                    new KeyValue(container.scaleYProperty(), 1.0, Interpolator.EASE_IN)
            );

            celebration.getKeyFrames().addAll(bounceUp, bounceDown);
        }

        celebration.setOnFinished(e -> Platform.runLater(onComplete::onAnimationComplete));
        celebration.play();
    }

    /**
     * Mostra dialog di vittoria con opzioni
     */
    public void showWinDialog(int moves, GameEventHandler.ActionHandler onNextLevel,
                              GameEventHandler.ActionHandler onRestart) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Livello Completato!");
        alert.setHeaderText("🎉 Congratulazioni! 🎉");

        int stars = calculateStars(moves);
        String starsText = "⭐".repeat(stars) + "☆".repeat(3 - stars);

        alert.setContentText(String.format(
                "Hai completato il livello in %d mosse!\n\n" +
                        "Valutazione: %s (%d/3 stelle)\n\n" +
                        "Cosa vuoi fare ora?",
                moves, starsText, stars));

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        // Pulsanti personalizzati
        ButtonType nextLevelButton = new ButtonType("Livello Successivo", ButtonBar.ButtonData.OK_DONE);
        ButtonType restartButton = new ButtonType("Rigioca", ButtonBar.ButtonData.OTHER);
        ButtonType menuButton = new ButtonType("Menu", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getDialogPane().getButtonTypes().setAll(nextLevelButton, restartButton, menuButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == nextLevelButton && onNextLevel != null) {
                try {
                    onNextLevel.onAction();
                } catch (ObjectNotValidException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAnnotationException e) {
                    throw new RuntimeException(e);
                }
            } else if (response == restartButton && onRestart != null) {
                try {
                    onRestart.onAction();
                } catch (ObjectNotValidException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAnnotationException e) {
                    throw new RuntimeException(e);
                }
            } else if (response == menuButton && onMenuRequested != null) {
                try {
                    onMenuRequested.onAction();
                } catch (ObjectNotValidException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAnnotationException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Mostra il dialog di completamento di tutti i livelli
     */
    public void showCompletionDialog(GameEventHandler.ActionHandler onBackToMenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Modalità Completata!");
        alert.setHeaderText("🏆 Hai completato tutti i livelli! 🏆");
        alert.setContentText(String.format(
                "Complimenti! Hai completato tutti i 5 livelli della modalità %s!\n\n" +
                        "Prova le altre modalità per una sfida ancora maggiore!",
                gameLevel.getDisplayName()));

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        // Pulsanti
        ButtonType newDifficultyButton = new ButtonType("Nuova Difficoltà", ButtonBar.ButtonData.OK_DONE);
        ButtonType menuButton = new ButtonType("Menu Principale", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getDialogPane().getButtonTypes().setAll(newDifficultyButton, menuButton);

        alert.showAndWait().ifPresent(response -> {
            if (onBackToMenu != null) {
                try {
                    onBackToMenu.onAction();
                } catch (ObjectNotValidException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAnnotationException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Calcola le stelle in base al numero di mosse
     */
    private int calculateStars(int moves) {
        int optimalMoves = switch (gameLevel) {
            case EASY -> 20;
            case MEDIUM -> 30;
            case HARD -> 45;
        };

        if (moves <= optimalMoves) return 3;
        if (moves <= optimalMoves * 1.5) return 2;
        if (moves <= optimalMoves * 2) return 1;
        return 0;
    }

    // Metodi per gestire loading e messaggi

    public void showLoadingHint(boolean show) {
        loadingOverlay.setVisible(show);
        loadingText.setText(show ? "Calcolando suggerimento..." : "");
    }

    public void showLoadingSolution(boolean show) {
        loadingOverlay.setVisible(show);
        loadingText.setText(show ? "Trovando soluzione..." : "");
    }

    public void showNoHintAvailable() {
        showTemporaryMessage("Nessun suggerimento disponibile", Alert.AlertType.INFORMATION);
    }

    public void showHintError() {
        showTemporaryMessage("Errore nel calcolo del suggerimento", Alert.AlertType.WARNING);
    }

    public void showNoSolutionFound() {
        showTemporaryMessage("Nessuna soluzione trovata", Alert.AlertType.INFORMATION);
    }

    public void showSolutionError() {
        showTemporaryMessage("Errore nella risoluzione automatica", Alert.AlertType.ERROR);
    }

    public void showUndoNotAvailable() {
        showTemporaryMessage("Undo non disponibile", Alert.AlertType.INFORMATION);
    }

    /**
     * Mostra un messaggio temporaneo
     */
    private void showTemporaryMessage(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());
        alert.show();

        // Chiudi automaticamente dopo 2 secondi
        Timeline autoClose = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> alert.close())
        );
        autoClose.play();
    }

    /**
     * Resetta la vista
     */
    public void resetView() {
        clearSelection();
        tubeViews.forEach(TubeView::reset);
    }

    // Getters e Setters

    public Scene getScene() {
        return scene;
    }

    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setHintsEnabled(boolean enabled) {
        this.hintsEnabled = enabled;
        hintButton.setDisable(!enabled);
    }

    // Event handlers

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

    /**
     * Classe interna per rappresentare un tubo nella vista
     */
    private static class TubeView {
        private final int tubeId;
        private final int capacity;
        private final VBox container;
        private final List<Circle> balls;
        private final double tubeHeight;

        private Runnable onClick;

        public TubeView(int tubeId, int capacity) {
            this.tubeId = tubeId;
            this.capacity = capacity;
            this.balls = new ArrayList<>();
            this.tubeHeight = capacity * (BALL_SIZE * 2 + 5) + 20;

            // Crea container
            container = new VBox();
            container.getStyleClass().add("tube");
            container.setAlignment(Pos.BOTTOM_CENTER);
            container.setPrefSize(TUBE_WIDTH, tubeHeight);
            container.setSpacing(2);
            container.setPadding(new Insets(5));

            // Event handler
            container.setOnMouseClicked(e -> {
                if (onClick != null) onClick.run();
            });
        }

        /**
         * Aggiorna il tubo con un nuovo stato
         */
        public void updateTube(Tube tube) {
            container.getChildren().clear();
            balls.clear();

            List<Ball> tubeBalls = tube.getBalls();
            for (Ball ball : tubeBalls) {
                Circle ballCircle = createBallCircle(ball);
                balls.add(ballCircle);
                container.getChildren().add(0, ballCircle);
            }
        }

        /**
         * Crea un cerchio per rappresentare una pallina
         */
        private Circle createBallCircle(Ball ball) {
            Circle circle = new Circle(BALL_SIZE);

            // Applica colore con gradiente
            javafx.scene.paint.Paint ballPaint = getBallColor(ball.getColor());
            circle.setFill(ballPaint);

            // Bordo e ombra
            circle.setStroke(javafx.scene.paint.Color.web("#333333"));
            circle.setStrokeWidth(1.5);

            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setColor(javafx.scene.paint.Color.web("#00000080"));
            shadow.setRadius(4);
            shadow.setOffsetX(2);
            shadow.setOffsetY(2);
            circle.setEffect(shadow);

            circle.getStyleClass().addAll("ball", ball.getColor().name().toLowerCase());

            return circle;
        }

        /**
         * Ottiene il colore JavaFX per una pallina
         */
        private javafx.scene.paint.Paint getBallColor(Ball.Color ballColor) {
            javafx.scene.paint.Color centerColor, edgeColor;

            switch (ballColor) {
                case RED -> {
                    centerColor = javafx.scene.paint.Color.web("#FF6B6B");
                    edgeColor = javafx.scene.paint.Color.web("#E53E3E");
                }
                case BLUE -> {
                    centerColor = javafx.scene.paint.Color.web("#4DABF7");
                    edgeColor = javafx.scene.paint.Color.web("#2B77E5");
                }
                case GREEN -> {
                    centerColor = javafx.scene.paint.Color.web("#51CF66");
                    edgeColor = javafx.scene.paint.Color.web("#37B24D");
                }
                case YELLOW -> {
                    centerColor = javafx.scene.paint.Color.web("#FFD43B");
                    edgeColor = javafx.scene.paint.Color.web("#FAB005");
                }
                case ORANGE -> {
                    centerColor = javafx.scene.paint.Color.web("#FF8A65");
                    edgeColor = javafx.scene.paint.Color.web("#FF7043");
                }
                case PURPLE -> {
                    centerColor = javafx.scene.paint.Color.web("#9C88FF");
                    edgeColor = javafx.scene.paint.Color.web("#7C3AED");
                }
                case PINK -> {
                    centerColor = javafx.scene.paint.Color.web("#FFB3D9");
                    edgeColor = javafx.scene.paint.Color.web("#FF8CC8");
                }
                default -> {
                    centerColor = javafx.scene.paint.Color.GRAY;
                    edgeColor = javafx.scene.paint.Color.DARKGRAY;
                }
            }

            return new javafx.scene.paint.RadialGradient(
                    0, 0, 0.3, 0.3, 0.7, true,
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, centerColor),
                    new javafx.scene.paint.Stop(1, edgeColor)
            );
        }

        // Metodi di utilità

        public Circle getTopBall() {
            return balls.isEmpty() ? null : balls.get(balls.size() - 1);
        }

        public double getCenterX() {
            return container.getLayoutX() + TUBE_WIDTH / 2;
        }

        public double getTopBallY() {
            if (balls.isEmpty()) return container.getLayoutY() + tubeHeight - BALL_SIZE;
            return container.getLayoutY() + container.getHeight() - (balls.size() * (BALL_SIZE * 2 + 5));
        }

        public double getNextBallY() {
            return container.getLayoutY() + container.getHeight() - ((balls.size() + 1) * (BALL_SIZE * 2 + 5));
        }

        public void setSelected(boolean selected) {
            if (selected) {
                container.getStyleClass().add("selected");
            } else {
                container.getStyleClass().remove("selected");
            }
        }

        public void setHintSource(boolean isSource) {
            if (isSource) {
                container.getStyleClass().add("hint-source");
            } else {
                container.getStyleClass().remove("hint-source");
            }
        }

        public void setHintTarget(boolean isTarget) {
            if (isTarget) {
                container.getStyleClass().add("hint-target");
            } else {
                container.getStyleClass().remove("hint-target");
            }
        }

        public void reset() {
            container.getChildren().clear();
            balls.clear();
            container.getStyleClass().removeAll("selected", "hint-source", "hint-target");
        }

        // Getters

        public int getTubeId() {
            return tubeId;
        }

        public VBox getContainer() {
            return container;
        }

        public void setOnClick(Runnable onClick) {
            this.onClick = onClick;
        }
    }
}