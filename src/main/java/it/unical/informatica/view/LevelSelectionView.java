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
    private VBox mainContainer;
    private final GameLevel currentDifficulty;
    private final GamePreferences preferences;

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
        mainContainer.setSpacing(30);
        mainContainer.setPadding(new Insets(30));

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

        // Crea la scena
        scene = new Scene(mainContainer, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    }

    /**
     * Crea l'header con titolo
     */
    private void createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Text title = new Text("Selezione Livello");
        title.getStyleClass().add("level-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text difficultyText = new Text(currentDifficulty.getDisplayName());
        difficultyText.getStyleClass().add("level-title");
        difficultyText.setStyle("-fx-fill: #FF5722; -fx-font-size: 24px;");

        header.getChildren().addAll(title, spacer, difficultyText);
        mainContainer.getChildren().add(header);
    }

    /**
     * Crea il selettore di difficoltà
     */
    private void createDifficultySelector() {
        VBox difficultySection = new VBox();
        difficultySection.setSpacing(15);

        Text sectionTitle = new Text("Cambia Difficoltà:");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #1976D2;");

        HBox difficultyButtons = new HBox();
        difficultyButtons.setSpacing(15);
        difficultyButtons.setAlignment(Pos.CENTER);

        for (GameLevel level : GameLevel.values()) {
            Button diffButton = createDifficultyButton(level);
            difficultyButtons.getChildren().add(diffButton);
        }

        difficultySection.getChildren().addAll(sectionTitle, difficultyButtons);
        mainContainer.getChildren().add(difficultySection);
    }

    /**
     * Crea un pulsante per la difficoltà
     */
    private Button createDifficultyButton(GameLevel level) {
        Button button = new Button(level.getDisplayName());
        button.getStyleClass().add("control-button");

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
        gridSection.setSpacing(20);

        Text sectionTitle = new Text("Seleziona Livello:");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #1976D2;");

        GridPane levelGrid = new GridPane();
        levelGrid.setAlignment(Pos.CENTER);
        levelGrid.setHgap(20);
        levelGrid.setVgap(20);
        levelGrid.setPadding(new Insets(20));

        // Crea le card per ogni livello
        for (int level = 1; level <= 5; level++) {
            VBox levelCard = createLevelCard(level);
            levelGrid.add(levelCard, (level - 1) % 5, (level - 1) / 5);
        }

        gridSection.getChildren().addAll(sectionTitle, levelGrid);
        mainContainer.getChildren().add(gridSection);
    }

    /**
     * Crea una card per un livello
     */
    private VBox createLevelCard(int levelNumber) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10);
        card.setPrefSize(150, 150);

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

        // Numero del livello
        Text levelText = new Text(String.valueOf(levelNumber));
        levelText.getStyleClass().add("level-number");

        // Icona di stato
        Text statusIcon = new Text();
        if (!isUnlocked) {
            statusIcon.setText("🔒");
            statusIcon.setStyle("-fx-font-size: 24px;");
        } else if (isCompleted) {
            statusIcon.setText("✅");
            statusIcon.setStyle("-fx-font-size: 24px;");
        } else {
            statusIcon.setText("▶️");
            statusIcon.setStyle("-fx-font-size: 24px;");
        }

        // Stelle (se completato)
        HBox starsBox = new HBox();
        starsBox.setAlignment(Pos.CENTER);
        starsBox.setSpacing(3);
        starsBox.getStyleClass().add("level-stars");

        if (isCompleted) {
            int stars = preferences.getStars(currentDifficulty, levelNumber);
            for (int i = 0; i < 3; i++) {
                Text star = new Text("⭐");
                star.getStyleClass().add(i < stars ? "star" : "star empty");
                starsBox.getChildren().add(star);
            }

            // Mostra il miglior punteggio
            int bestMoves = preferences.getBestMoves(currentDifficulty, levelNumber);
            Text bestScore = new Text("Best: " + bestMoves + " mosse");
            bestScore.setStyle("-fx-font-size: 10px; -fx-fill: #666666;");
            card.getChildren().add(bestScore);
        }

        card.getChildren().addAll(levelText, statusIcon, starsBox);

        // Event handler per il click
        if (isUnlocked) {
            card.setOnMouseClicked(e -> {
                if (onLevelSelected != null) {
                    onLevelSelected.onLevelSelected(currentDifficulty, levelNumber);
                }
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
        progressSection.setSpacing(10);
        progressSection.setPadding(new Insets(20));
        progressSection.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10px;");

        Text progressTitle = new Text("Progresso " + currentDifficulty.getDisplayName());
        progressTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #1976D2;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.getStyleClass().add("progress-bar");
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(20);

        double progress = preferences.getProgressForDifficulty(currentDifficulty) / 100.0;
        progressBar.setProgress(progress);

        Text progressText = new Text(String.format("%.0f%% completato (%d/5 livelli)",
                progress * 100, countCompletedLevels()));
        progressText.setStyle("-fx-font-size: 14px; -fx-fill: #666666;");

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
        HBox controlBox = new HBox();
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setSpacing(20);
        controlBox.setPadding(new Insets(20, 0, 0, 0));

        Button backButton = new Button("🔙 Torna al Menu");
        backButton.getStyleClass().addAll("control-button", "secondary-button");
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
        mainContainer.getChildren().clear();
        createLevelSelectionScene();
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