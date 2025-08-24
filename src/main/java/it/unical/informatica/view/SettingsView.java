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
import java.util.Map;
import java.util.function.Consumer;

/**
 * Vista per le impostazioni del gioco
 */
public class SettingsView {

    private Scene scene;
    private VBox mainContainer;

    // Components
    private CheckBox soundCheckBox;
    private CheckBox animationsCheckBox;
    private CheckBox hintsCheckBox;
    private ProgressBar totalProgressBar;
    private Text progressText;

    // Event handlers
    private Consumer<Boolean> onSoundToggled;
    private Consumer<Boolean> onAnimationsToggled;
    private Consumer<Boolean> onHintsToggled;
    private GameEventHandler.ActionHandler onResetProgressRequested;
    private GameEventHandler.ActionHandler onBackToMenu;
    private GameEventHandler.ActionHandler onDebugInfoRequested;

    public SettingsView() {
        createSettingsScene();
    }

    /**
     * Crea la scena delle impostazioni
     */
    private void createSettingsScene() {
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("settings-container");
        mainContainer.setSpacing(25);
        mainContainer.setPadding(new Insets(30));

        // Titolo
        createTitle();

        // Sezione Audio e Grafica
        createAudioVideoSection();

        // Sezione Gameplay
        createGameplaySection();

        // Sezione Progresso
        createProgressSection();

        // Sezione Debug (nascosta di default)
        createDebugSection();

        // Pulsanti di controllo
        createControlButtons();

        // Crea la scena
        scene = new Scene(new ScrollPane(mainContainer), 700, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    }

    /**
     * Crea il titolo delle impostazioni
     */
    private void createTitle() {
        Text title = new Text("Impostazioni");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-fill: #1976D2;");

        VBox titleBox = new VBox(title);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 20, 0));

        mainContainer.getChildren().add(titleBox);
    }

    /**
     * Crea la sezione audio e video
     */
    private void createAudioVideoSection() {
        VBox section = new VBox();
        section.getStyleClass().add("settings-section");
        section.setSpacing(15);

        Text sectionTitle = new Text("🔊 Audio e Grafica");
        sectionTitle.getStyleClass().add("settings-title");

        // Controllo suono
        HBox soundRow = createSettingRow("Effetti sonori",
                "Abilita o disabilita gli effetti sonori del gioco");
        soundCheckBox = new CheckBox();
        soundCheckBox.setSelected(true);
        soundCheckBox.setOnAction(e -> {
            if (onSoundToggled != null) {
                onSoundToggled.accept(soundCheckBox.isSelected());
            }
        });
        soundRow.getChildren().add(soundCheckBox);

        // Controllo animazioni
        HBox animationsRow = createSettingRow("Animazioni fluide",
                "Abilita o disabilita le animazioni durante il gioco");
        animationsCheckBox = new CheckBox();
        animationsCheckBox.setSelected(true);
        animationsCheckBox.setOnAction(e -> {
            if (onAnimationsToggled != null) {
                onAnimationsToggled.accept(animationsCheckBox.isSelected());
            }
        });
        animationsRow.getChildren().add(animationsCheckBox);

        section.getChildren().addAll(sectionTitle, soundRow, animationsRow);
        mainContainer.getChildren().add(section);
    }

    /**
     * Crea la sezione gameplay
     */
    private void createGameplaySection() {
        VBox section = new VBox();
        section.getStyleClass().add("settings-section");
        section.setSpacing(15);

        Text sectionTitle = new Text("🎮 Gameplay");
        sectionTitle.getStyleClass().add("settings-title");

        // Controllo suggerimenti
        HBox hintsRow = createSettingRow("Suggerimenti AI",
                "Abilita o disabilita i suggerimenti basati su Answer Set Programming");
        hintsCheckBox = new CheckBox();
        hintsCheckBox.setSelected(true);
        hintsCheckBox.setOnAction(e -> {
            if (onHintsToggled != null) {
                onHintsToggled.accept(hintsCheckBox.isSelected());
            }
        });
        hintsRow.getChildren().add(hintsCheckBox);

        section.getChildren().addAll(sectionTitle, hintsRow);
        mainContainer.getChildren().add(section);
    }

    /**
     * Crea la sezione progresso
     */
    private void createProgressSection() {
        VBox section = new VBox();
        section.getStyleClass().add("settings-section");
        section.setSpacing(15);

        Text sectionTitle = new Text("📊 Progresso");
        sectionTitle.getStyleClass().add("settings-title");

        // Progress bar totale
        VBox progressBox = new VBox();
        progressBox.setSpacing(10);

        Text progressLabel = new Text("Progresso totale del gioco:");
        progressLabel.getStyleClass().add("settings-label");

        totalProgressBar = new ProgressBar();
        totalProgressBar.getStyleClass().add("progress-bar");
        totalProgressBar.setPrefWidth(300);
        totalProgressBar.setProgress(0.0);

        progressText = new Text("0% completato");
        progressText.setStyle("-fx-font-size: 14px; -fx-fill: #666666;");

        progressBox.getChildren().addAll(progressLabel, totalProgressBar, progressText);

        // Pulsante reset
        Button resetButton = new Button("🗑️ Reset Progresso");
        resetButton.getStyleClass().addAll("control-button", "danger-button");
        resetButton.setOnAction(e -> {
            if (onResetProgressRequested != null) {
                try {
                    onResetProgressRequested.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        section.getChildren().addAll(sectionTitle, progressBox, resetButton);
        mainContainer.getChildren().add(section);
    }

    /**
     * Crea la sezione debug
     */
    private void createDebugSection() {
        VBox section = new VBox();
        section.getStyleClass().add("settings-section");
        section.setSpacing(15);

        Text sectionTitle = new Text("🔧 Sviluppatori");
        sectionTitle.getStyleClass().add("settings-title");

        Button debugButton = new Button("📋 Mostra Info Debug");
        debugButton.getStyleClass().add("control-button");
        debugButton.setOnAction(e -> {
            if (onDebugInfoRequested != null) {
                try {
                    onDebugInfoRequested.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        Text debugInfo = new Text("Informazioni tecniche e statistiche dettagliate del gioco");
        debugInfo.getStyleClass().add("settings-label");
        debugInfo.setStyle("-fx-font-size: 12px; -fx-fill: #999999;");

        section.getChildren().addAll(sectionTitle, debugButton, debugInfo);
        mainContainer.getChildren().add(section);
    }

    /**
     * Crea i pulsanti di controllo
     */
    private void createControlButtons() {
        HBox controlBox = new HBox();
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setSpacing(20);
        controlBox.setPadding(new Insets(30, 0, 0, 0));

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

        controlBox.getChildren().add(backButton);
        mainContainer.getChildren().add(controlBox);
    }

    /**
     * Crea una riga di impostazione
     */
    private HBox createSettingRow(String title, String description) {
        HBox row = new HBox();
        row.getStyleClass().add("settings-item");
        row.setSpacing(15);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox();
        textBox.setSpacing(5);

        Text titleText = new Text(title);
        titleText.getStyleClass().add("settings-label");
        titleText.setStyle("-fx-font-weight: bold;");

        Text descText = new Text(description);
        descText.setStyle("-fx-font-size: 12px; -fx-fill: #666666;");
        descText.setWrappingWidth(300);

        textBox.getChildren().addAll(titleText, descText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(textBox, spacer);

        return row;
    }

    /**
     * Mostra il dialog di conferma reset
     */
    public void showResetConfirmationDialog(GameEventHandler.ActionHandler onResetAll,
                                            GameEventHandler.ActionHandler onResetSpecific) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Progresso");
        alert.setHeaderText("Scegli il tipo di reset");
        alert.setContentText("Cosa vuoi resettare?");

        ButtonType resetAllButton = new ButtonType("Reset Tutto", ButtonBar.ButtonData.OK_DONE);
        ButtonType resetSpecificButton = new ButtonType("Reset Difficoltà", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getDialogPane().getButtonTypes().setAll(resetAllButton, resetSpecificButton, cancelButton);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        alert.showAndWait().ifPresent(response -> {
            if (response == resetAllButton && onResetAll != null) {
                try {
                    onResetAll.onAction();
                } catch (ObjectNotValidException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAnnotationException e) {
                    throw new RuntimeException(e);
                }
            } else if (response == resetSpecificButton && onResetSpecific != null) {
                try {
                    onResetSpecific.onAction();
                } catch (ObjectNotValidException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAnnotationException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Mostra il dialog per selezionare la difficoltà da resettare
     */
    public void showDifficultySelectionDialog(Consumer<GameLevel> onDifficultySelected) {
        ChoiceDialog<GameLevel> dialog = new ChoiceDialog<>(GameLevel.EASY, GameLevel.values());
        dialog.setTitle("Seleziona Difficoltà");
        dialog.setHeaderText("Reset Progresso Difficoltà");
        dialog.setContentText("Scegli quale difficoltà resettare:");

        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        dialog.showAndWait().ifPresent(onDifficultySelected);
    }

    /**
     * Mostra il dialog con le informazioni di debug
     */
    public void showDebugInfoDialog(Map<String, Object> debugInfo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazioni Debug");
        alert.setHeaderText("Statistiche Tecniche");

        StringBuilder content = new StringBuilder();
        content.append("📊 STATISTICHE GENERALI:\n");
        content.append(String.format("Progresso totale: %.1f%%\n", debugInfo.get("total_progress")));
        content.append(String.format("Suono abilitato: %s\n", debugInfo.get("sound_enabled")));
        content.append(String.format("Animazioni abilitate: %s\n", debugInfo.get("animations_enabled")));
        content.append(String.format("Suggerimenti abilitati: %s\n\n", debugInfo.get("show_hints")));

        content.append("📈 PROGRESSO PER DIFFICOLTÀ:\n");
        for (GameLevel level : GameLevel.values()) {
            String key = level.name().toLowerCase() + "_progress";
            content.append(String.format("%s: %.1f%%\n",
                    level.getDisplayName(), debugInfo.get(key)));
        }

        content.append("\n🛠️ INFORMAZIONI TECNICHE:\n");
        content.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        content.append("JavaFX Version: ").append(System.getProperty("javafx.version", "N/A")).append("\n");
        content.append("OS: ").append(System.getProperty("os.name")).append("\n");
        content.append("Memoria utilizzata: ").append(
                        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024)
                .append(" MB\n");

        alert.setContentText(content.toString());
        alert.getDialogPane().setPrefWidth(500);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        alert.showAndWait();
    }

    /**
     * Mostra un messaggio di successo per il reset
     */
    public void showResetSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reset Completato");
        alert.setHeaderText("✅ Operazione Completata");
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());
        alert.showAndWait();
    }

    /**
     * Mostra feedback per il cambio di impostazione
     */
    public void showSoundFeedback(boolean enabled) {
        showSettingFeedback("Suono " + (enabled ? "abilitato" : "disabilitato"));
    }

    public void showAnimationsFeedback(boolean enabled) {
        showSettingFeedback("Animazioni " + (enabled ? "abilitate" : "disabilitate"));
    }

    public void showHintsFeedback(boolean enabled) {
        showSettingFeedback("Suggerimenti " + (enabled ? "abilitati" : "disabilitati"));
    }

    /**
     * Mostra un feedback temporaneo per le impostazioni
     */
    private void showSettingFeedback(String message) {
        // Tooltip temporaneo o notifica discreta
        System.out.println("Settings: " + message); // Per ora stampa in console
    }

    // Getters e Setters

    public Scene getScene() {
        return scene;
    }

    public void setSoundEnabled(boolean enabled) {
        soundCheckBox.setSelected(enabled);
    }

    public void setAnimationsEnabled(boolean enabled) {
        animationsCheckBox.setSelected(enabled);
    }

    public void setHintsEnabled(boolean enabled) {
        hintsCheckBox.setSelected(enabled);
    }

    public void setTotalProgress(double progress) {
        totalProgressBar.setProgress(progress / 100.0);
        progressText.setText(String.format("%.0f%% completato", progress));
    }

    // Event handlers

    public void setOnSoundToggled(Consumer<Boolean> handler) {
        this.onSoundToggled = handler;
    }

    public void setOnAnimationsToggled(Consumer<Boolean> handler) {
        this.onAnimationsToggled = handler;
    }

    public void setOnHintsToggled(Consumer<Boolean> handler) {
        this.onHintsToggled = handler;
    }

    public void setOnResetProgressRequested(GameEventHandler.ActionHandler handler) {
        this.onResetProgressRequested = handler;
    }

    public void setOnBackToMenu(GameEventHandler.ActionHandler handler) {
        this.onBackToMenu = handler;
    }

    public void setOnDebugInfoRequested(GameEventHandler.ActionHandler handler) {
        this.onDebugInfoRequested = handler;
    }
}