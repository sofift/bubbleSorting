package it.unical.informatica.view;

import it.unical.informatica.model.GameLevel;
import it.unical.mat.embasp.languages.IllegalAnnotationException;
import it.unical.mat.embasp.languages.ObjectNotValidException;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import it.unical.informatica.controller.GameEventHandler;
import it.unical.informatica.controller.GamePreferences;

/**
 * Vista per la selezione dei livelli
 */
public class LevelSelectionView {

    private Scene scene;
    private ScrollPane scrollPane;
    private VBox mainContainer;
    private final GameLevel currentDifficulty;
    private final GamePreferences preferences;

    // Riferimenti agli elementi per il ridimensionamento
    private Text mainTitle;
    private Text difficultyTitle;
    private Text difficultySelectionTitle;
    private Text levelSelectionTitle;
    private Text progressTitle;
    private Text progressText;

    // Event handlers
    private GameEventHandler.LevelSelectionHandler onLevelSelected;
    private GameEventHandler.ActionHandler onBackToMenu;
    private GameEventHandler.LevelSelectionHandler onDifficultyChanged;

    public LevelSelectionView(GameLevel difficulty) {
        this.currentDifficulty = difficulty;
        this.preferences = GamePreferences.getInstance();
        createLevelSelectionScene();
    }

    /**
     * Crea la scena di selezione livelli
     */
    private void createLevelSelectionScene() {
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("level-container");
        mainContainer.setSpacing(20);
        mainContainer.setPadding(new Insets(15));

        // Header con titolo e controlli
        createHeader();

        // Selezione difficoltà
        createDifficultySelector();

        // Griglia dei livelli
        createLevelGrid();

        // Progress bar
        createProgressSection();

        // Pulsanti di controllo
        createControlButtons();

        // Wrappa tutto in uno ScrollPane
        scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("scroll-pane");

        // Crea la scena
        scene = new Scene(scrollPane, 600, 500);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Aggiungi listener per il ridimensionamento
        addResponsiveListeners();
    }

    /**
     * Aggiunge listener per rendere il layout responsive
     */
    private void addResponsiveListeners() {
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            updateLayoutForWidth(width);
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double height = newVal.doubleValue();
            updateLayoutForHeight(height);
        });

        // Imposta le dimensioni iniziali
        updateLayoutForWidth(scene.getWidth());
        updateLayoutForHeight(scene.getHeight());
    }

    /**
     * Aggiorna il layout in base alla larghezza
     */
    private void updateLayoutForWidth(double width) {
        // Aggiusta il padding in base alla larghezza
        double padding = Math.max(10, Math.min(30, width * 0.03));
        mainContainer.setPadding(new Insets(padding));

        // Aggiorna le dimensioni dei font
        updateFontSizes(width);
    }

    /**
     * Aggiorna il layout in base all'altezza
     */
    private void updateLayoutForHeight(double height) {
        // Aggiusta lo spacing in base all'altezza
        double spacing = Math.max(10, Math.min(25, height * 0.035));
        mainContainer.setSpacing(spacing);
    }

    /**
     * Aggiorna le dimensioni dei font in base alla larghezza
     */
    private void updateFontSizes(double width) {
        // Calcola le dimensioni dei font responsive
        double titleSize = Math.max(24, Math.min(32, width * 0.04));
        double subtitleSize = Math.max(18, Math.min(24, width * 0.035));
        double normalSize = Math.max(14, Math.min(18, width * 0.025));
        double smallSize = Math.max(12, Math.min(16, width * 0.02));
        double tinySize = Math.max(10, Math.min(14, width * 0.015));

        // Applica i font ai titoli principali
        if (mainTitle != null) {
            mainTitle.setStyle("-fx-font-size: " + titleSize + "px;");
        }
        if (difficultyTitle != null) {
            difficultyTitle.setStyle("-fx-fill: #FF5722; -fx-font-size: " + subtitleSize + "px;");
        }
        if (difficultySelectionTitle != null) {
            difficultySelectionTitle.setStyle("-fx-font-weight: bold; -fx-fill: #1976D2; -fx-font-size: " + smallSize + "px;");
        }
        if (levelSelectionTitle != null) {
            levelSelectionTitle.setStyle("-fx-font-weight: bold; -fx-fill: #1976D2; -fx-font-size: " + normalSize + "px;");
        }
        if (progressTitle != null) {
            progressTitle.setStyle("-fx-font-weight: bold; -fx-fill: #1976D2; -fx-font-size: " + smallSize + "px;");
        }
        if (progressText != null) {
            progressText.setStyle("-fx-fill: #666666; -fx-font-size: " + tinySize + "px;");
        }
    }

    /**
     * Crea l'header con titolo
     */
    private void createHeader() {
        VBox headerContainer = new VBox();
        headerContainer.setAlignment(Pos.CENTER);
        headerContainer.setSpacing(10);

        mainTitle = new Text("Selezione Livello");
        mainTitle.getStyleClass().add("level-title");

        difficultyTitle = new Text(currentDifficulty.getDisplayName());
        difficultyTitle.getStyleClass().add("level-title");

        headerContainer.getChildren().addAll(mainTitle, difficultyTitle);
        mainContainer.getChildren().add(headerContainer);
    }

    /**
     * Crea il selettore di difficoltà
     */
    private void createDifficultySelector() {
        VBox difficultySection = new VBox();
        difficultySection.setSpacing(10);

        difficultySelectionTitle = new Text("Cambia Difficoltà:");

        FlowPane difficultyButtons = new FlowPane();
        difficultyButtons.setHgap(10);
        difficultyButtons.setVgap(10);
        difficultyButtons.setAlignment(Pos.CENTER);

        for (GameLevel level : GameLevel.values()) {
            Button diffButton = createDifficultyButton(level);
            difficultyButtons.getChildren().add(diffButton);
        }

        difficultySection.getChildren().addAll(difficultySelectionTitle, difficultyButtons);
        mainContainer.getChildren().add(difficultySection);
    }

    /**
     * Crea un pulsante per la difficoltà
     */
    private Button createDifficultyButton(GameLevel level) {
        Button button = new Button(level.getDisplayName());
        button.getStyleClass().add("control-button");
        button.setPrefWidth(120);
        button.setPrefHeight(60);

        if (level == currentDifficulty) {
            button.setStyle("-fx-background-color: #FF5722; -fx-border-color: #D84315; -fx-border-width: 2px;");
        }

        // Aggiungi statistiche
        double progress = preferences.getProgressForDifficulty(level);
        String progressText = String.format("%.0f%%", progress);
        button.setText(level.getDisplayName() + "\n" + progressText);

        button.setOnAction(e -> {
            if (onDifficultyChanged != null && level != currentDifficulty) {
                onDifficultyChanged.onLevelSelected(level, 1);
            }
        });

        return button;
    }

    /**
     * Crea la griglia dei livelli
     */
    private void createLevelGrid() {
        VBox gridSection = new VBox();
        gridSection.setSpacing(15);

        levelSelectionTitle = new Text("Seleziona Livello:");

        FlowPane levelGrid = new FlowPane();
        levelGrid.setAlignment(Pos.CENTER);
        levelGrid.setHgap(15);
        levelGrid.setVgap(15);
        levelGrid.setPadding(new Insets(10));

        // Crea le card per ogni livello
        for (int level = 1; level <= 5; level++) {
            VBox levelCard = createLevelCard(level);
            levelGrid.getChildren().add(levelCard);
        }

        gridSection.getChildren().addAll(levelSelectionTitle, levelGrid);
        mainContainer.getChildren().add(gridSection);
    }

    /**
     * Crea una card per un livello
     */
    private VBox createLevelCard(int levelNumber) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(5);

        // Dimensioni più grandi e proporzionate
        card.setPrefSize(140, 160);
        card.setMinSize(120, 140);
        card.setMaxSize(160, 180);

        boolean isCompleted = preferences.isLevelCompleted(currentDifficulty, levelNumber);
        boolean isUnlocked = preferences.isLevelUnlocked(currentDifficulty, levelNumber);

        // Applica stili in base allo stato
        if (isCompleted) {
            card.getStyleClass().addAll("level-card", "completed");
        } else if (!isUnlocked) {
            card.getStyleClass().addAll("level-card", "locked");
        } else {
            card.getStyleClass().add("level-card");
        }

        // Container per numero e icona nella parte superiore
        VBox topSection = new VBox();
        topSection.setAlignment(Pos.CENTER);
        topSection.setSpacing(8);

        // Numero del livello più grande e ben visibile
        Text levelText = new Text(String.valueOf(levelNumber));
        levelText.getStyleClass().add("level-number");
        levelText.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        if (!isUnlocked) {
            Text statusIcon = new Text();
            topSection.getChildren().addAll(levelText, statusIcon);
            statusIcon.setText("🔒");
            statusIcon.setStyle("-fx-font-size: 24px;");
        } else {
            topSection.getChildren().addAll(levelText);
        }



        // Sezione centrale per le stelle (solo se completato)
        HBox starsSection = new HBox();
        starsSection.setAlignment(Pos.CENTER);
        starsSection.setSpacing(3);
        starsSection.setPadding(new Insets(5, 0, 5, 0));

        if (isCompleted) {
            int stars = preferences.getStars(currentDifficulty, levelNumber);
            for (int i = 0; i < 3; i++) {
                Text star = new Text("⭐");
                star.setStyle("-fx-font-size: 16px;");
                if (i < stars) {
                    star.getStyleClass().add("star");
                } else {
                    star.getStyleClass().add("star");
                    star.setStyle(star.getStyle() + "-fx-opacity: 0.3;");
                }
                starsSection.getChildren().add(star);
            }
        } else {
            // Placeholder per mantenere l'altezza uniforme
            Region spacer = new Region();
            spacer.setPrefHeight(20);
            starsSection.getChildren().add(spacer);
        }

        // Sezione inferiore per il punteggio
        VBox bottomSection = new VBox();
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setSpacing(2);

        if (isCompleted) {
            int bestMoves = preferences.getBestMoves(currentDifficulty, levelNumber);

            // Etichetta "BEST"
            Text bestLabel = new Text("BEST");
            bestLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-fill: #7B8A8B;");

            // Punteggio
            Text bestScore = new Text(String.valueOf(bestMoves));
            bestScore.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2E86C1;");

            bottomSection.getChildren().addAll(bestLabel, bestScore);
        } else if (!isUnlocked) {
            Text lockedText = new Text("BLOCCATO");
            lockedText.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-fill: #7B8A8B; -fx-opacity: 0.7;");
            bottomSection.getChildren().add(lockedText);
        } else {
            Text playText = new Text("GIOCA");
            playText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: #2E86C1;");
            bottomSection.getChildren().add(playText);
        }

        // Assembla tutti i componenti
        card.getChildren().addAll(topSection, starsSection, bottomSection);

        // Event handler per il click
        if (isUnlocked) {
            card.setOnMouseClicked(e -> {
                if (onLevelSelected != null) {
                    onLevelSelected.onLevelSelected(currentDifficulty, levelNumber);
                }
            });

            // Effetto hover
            card.setOnMouseEntered(e -> {
                card.setStyle(card.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
            });

            card.setOnMouseExited(e -> {
                card.setStyle(card.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", ""));
            });

            // Tooltip con informazioni
            Tooltip tooltip = new Tooltip();
            if (isCompleted) {
                tooltip.setText(String.format("Livello %d - Completato!\nMiglior punteggio: %d mosse\nStelle: %d/3",
                        levelNumber,
                        preferences.getBestMoves(currentDifficulty, levelNumber),
                        preferences.getStars(currentDifficulty, levelNumber)));
            } else {
                tooltip.setText(String.format("Livello %d - %s\nClicca per giocare!",
                        levelNumber, currentDifficulty.getDisplayName()));
            }
            Tooltip.install(card, tooltip);
        } else {
            // Tooltip per livelli bloccati
            Tooltip tooltip = new Tooltip("Completa il livello precedente per sbloccare questo!");
            Tooltip.install(card, tooltip);
        }

        return card;
    }

    /**
     * Crea la sezione del progresso
     */
    private void createProgressSection() {
        VBox progressSection = new VBox();
        progressSection.setSpacing(8);
        progressSection.setPadding(new Insets(15));
        progressSection.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10px;");

        progressTitle = new Text("Progresso " + currentDifficulty.getDisplayName());

        ProgressBar progressBar = new ProgressBar();
        progressBar.getStyleClass().add("progress-bar");
        progressBar.setPrefHeight(15);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        double progress = preferences.getProgressForDifficulty(currentDifficulty) / 100.0;
        progressBar.setProgress(progress);

        progressText = new Text(String.format("%.0f%% completato (%d/5 livelli)",
                progress * 100, countCompletedLevels()));

        progressSection.getChildren().addAll(progressTitle, progressBar, progressText);
        mainContainer.getChildren().add(progressSection);
    }

    /**
     * Conta i livelli completati per la difficoltà corrente
     */
    private int countCompletedLevels() {
        int count = 0;
        for (int i = 1; i <= 5; i++) {
            if (preferences.isLevelCompleted(currentDifficulty, i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Crea i pulsanti di controllo
     */
    private void createControlButtons() {
        FlowPane controlBox = new FlowPane();
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setHgap(15);
        controlBox.setVgap(10);
        controlBox.setPadding(new Insets(15, 0, 0, 0));

        Button backButton = new Button("🔙 Torna al Menu");
        backButton.getStyleClass().addAll("control-button", "secondary-button");
        backButton.setPrefWidth(140);
        backButton.setOnAction(e -> {
            if (onBackToMenu != null) {
                try {
                    onBackToMenu.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        Button resetButton = new Button("🔄 Reset Progresso");
        resetButton.getStyleClass().addAll("control-button", "danger-button");
        resetButton.setPrefWidth(140);
        resetButton.setOnAction(e -> showResetConfirmation());

        controlBox.getChildren().addAll(backButton, resetButton);
        mainContainer.getChildren().add(controlBox);
    }

    /**
     * Mostra la conferma per il reset del progresso
     */
    private void showResetConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Progresso");
        alert.setHeaderText("Conferma Reset");
        alert.setContentText(String.format(
                "Sei sicuro di voler resettare tutto il progresso per la difficoltà %s?\n" +
                        "Questa azione non può essere annullata!",
                currentDifficulty.getDisplayName()));

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                preferences.resetProgress(currentDifficulty);
                // Ricarica la vista
                recreateScene();
            }
        });
    }

    /**
     * Ricrea la scena dopo modifiche
     */
    private void recreateScene() {
        // Salva i riferimenti dei listener prima di ricreare
        mainContainer.getChildren().clear();

        // Resetta i riferimenti degli elementi
        mainTitle = null;
        difficultyTitle = null;
        difficultySelectionTitle = null;
        levelSelectionTitle = null;
        progressTitle = null;
        progressText = null;

        // Ricrea tutti i componenti
        createHeader();
        createDifficultySelector();
        createLevelGrid();
        createProgressSection();
        createControlButtons();

        // Riapplica il responsive design
        updateLayoutForWidth(scene.getWidth());
        updateLayoutForHeight(scene.getHeight());
    }

    // Getters e Setters per gli event handlers

    public Scene getScene() {
        return scene;
    }

    public void setOnLevelSelected(GameEventHandler.LevelSelectionHandler handler) {
        this.onLevelSelected = handler;
    }

    public void setOnBackToMenu(GameEventHandler.ActionHandler handler) {
        this.onBackToMenu = handler;
    }

    public void setOnDifficultyChanged(GameEventHandler.LevelSelectionHandler handler) {
        this.onDifficultyChanged = handler;
    }
}