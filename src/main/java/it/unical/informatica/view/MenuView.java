package it.unical.informatica.view;

import it.unical.informatica.model.GameLevel;
import it.unical.mat.embasp.languages.IllegalAnnotationException;
import it.unical.mat.embasp.languages.ObjectNotValidException;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import it.unical.informatica.controller.GameEventHandler;

/**
 * Vista del menu principale con design moderno ispirato a CrazyGames
 */
public class MenuView {

    private Scene scene;
    private VBox mainContainer;

    // Event handlers
    private GameEventHandler.NewGameHandler onNewGameSelected;
    private GameEventHandler.ActionHandler onExitSelected;
    private GameEventHandler.ActionHandler onRulesSelected;
    private GameEventHandler.ActionHandler onAboutSelected;
    private GameEventHandler.ActionHandler onSettingsSelected;

    public MenuView() {
        createMenuScene();
    }

    private void createMenuScene() {
        // Container principale con layout migliorato
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("menu-container");
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setSpacing(30);
        mainContainer.setPadding(new Insets(30));

        // Sezioni del menu
        createTitle();
        createMenuButtons();
        // Crea la scena con dimensioni moderne
        scene = new Scene(mainContainer, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    }

    private void createTitle() {
        VBox titleSection = new VBox();
        titleSection.setAlignment(Pos.CENTER);
        titleSection.setSpacing(15);

        // Titolo principale con effetto moderno
        Text title = new Text("Bubble Sorting Game");
        title.getStyleClass().add("menu-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 52));
        title.setFill(Color.WHITE);

        // Effetto ombra per il titolo
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setRadius(10);
        shadow.setOffsetY(4);
        title.setEffect(shadow);

        // Sottotitolo elegante
        Text subtitle = new Text("Ordina le palline colorate con l'intelligenza artificiale");
        subtitle.getStyleClass().add("menu-subtitle");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 18));
        subtitle.setFill(Color.rgb(255, 255, 255, 0.9));

        titleSection.getChildren().addAll(title, subtitle);
        mainContainer.getChildren().add(titleSection);
    }

    private void createMenuButtons() {
        VBox buttonSection = new VBox();
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.setSpacing(20);
        buttonSection.setPadding(new Insets(30, 0, 30, 0));

        // Pulsante principale - Nuova Partita
        Button newGameButton = createPrimaryButton("🎮 Nuova Partita");
        newGameButton.setOnAction(e -> showDifficultySelection());

        // Pulsanti secondari
        Button rulesButton = createSecondaryButton("📖 Regole del Gioco");
        rulesButton.setOnAction(e -> {
            if (onRulesSelected != null) {
                try {
                    onRulesSelected.onAction();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button settingsButton = createSecondaryButton("⚙️ Impostazioni");
        settingsButton.setOnAction(e -> {
            if (onSettingsSelected != null) {
                try {
                    onSettingsSelected.onAction();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button aboutButton = createSecondaryButton("ℹ️ Info Progetto");
        aboutButton.setOnAction(e -> {
            if (onAboutSelected != null) {
                try {
                    onAboutSelected.onAction();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Pulsante uscita con stile diverso
        Button exitButton = createDangerButton("🚪 Esci");
        exitButton.setOnAction(e -> {
            if (onExitSelected != null) {
                try {
                    onExitSelected.onAction();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        buttonSection.getChildren().addAll(
                newGameButton, rulesButton, settingsButton, aboutButton, exitButton
        );

        mainContainer.getChildren().add(buttonSection);
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("menu-button", "primary-button");
        button.setFont(Font.font("System", FontWeight.BOLD, 20));
        button.setPrefWidth(350);
        button.setPrefHeight(60);
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("menu-button", "secondary-button");
        button.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        button.setPrefWidth(320);
        button.setPrefHeight(50);
        return button;
    }

    private Button createDangerButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("menu-button", "danger-button");
        button.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        button.setPrefWidth(280);
        button.setPrefHeight(45);
        return button;
    }


    private void showDifficultySelection() {
        Dialog<GameLevel> dialog = new Dialog<>();
        dialog.setTitle("Seleziona Difficoltà");
        dialog.setHeaderText("Scegli il livello di difficoltà per iniziare a giocare");

        // Container del dialog con design moderno
        VBox content = new VBox();
        content.setSpacing(25);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("dialog-content");

        ToggleGroup difficultyGroup = new ToggleGroup();

        for (GameLevel level : GameLevel.values()) {
            VBox levelCard = createDifficultyCard(level, difficultyGroup);
            content.getChildren().add(levelCard);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        // Pulsanti con stile moderno
        ButtonType playButtonType = new ButtonType("🎮 Gioca!", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("❌ Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(playButtonType, cancelButtonType);

        // Logica dei pulsanti
        Button playButton = (Button) dialog.getDialogPane().lookupButton(playButtonType);
        playButton.setDisable(true);
        playButton.getStyleClass().add("dialog-button-primary");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("dialog-button-secondary");

        difficultyGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            playButton.setDisable(newToggle == null);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == playButtonType && difficultyGroup.getSelectedToggle() != null) {
                return (GameLevel) difficultyGroup.getSelectedToggle().getUserData();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(level -> {
            if (onNewGameSelected != null) {
                onNewGameSelected.onNewGame(level, 1);
            }
        });
    }

    private VBox createDifficultyCard(GameLevel level, ToggleGroup group) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(12);
        card.setPrefSize(280, 120);
        card.getStyleClass().add("difficulty-card");

        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(group);
        radioButton.setUserData(level);
        radioButton.getStyleClass().add("difficulty-radio");

        Text levelName = new Text(level.getDisplayName());
        levelName.setFont(Font.font("System", FontWeight.BOLD, 22));
        levelName.getStyleClass().add("difficulty-name");

        Text levelDetails = new Text(String.format("%d tubi • %d colori",
                level.getNumberOfTubes(), level.getNumberOfColors()));
        levelDetails.setFont(Font.font("System", FontWeight.NORMAL, 14));
        levelDetails.getStyleClass().add("difficulty-details");

        Text levelDescription = new Text(getDifficultyDescription(level));
        levelDescription.setFont(Font.font("System", FontWeight.LIGHT, 12));
        levelDescription.getStyleClass().add("difficulty-description");
        levelDescription.setWrappingWidth(250);

        card.getChildren().addAll(radioButton, levelName, levelDetails, levelDescription);

        // Rendi cliccabile tutta la card
        card.setOnMouseClicked(e -> radioButton.setSelected(true));

        return card;
    }

    private String getDifficultyDescription(GameLevel level) {
        return switch (level) {
            case EASY -> "Perfetto per iniziare e imparare le basi";
            case MEDIUM -> "Sfida equilibrata per giocatori esperti";
        };
    }

    public void showRulesDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Regole del Gioco");
        alert.setHeaderText("Come giocare a Bubble Sorting Game");

        String rulesText = """
            OBIETTIVO:
            Ordinare tutte le palline colorate in modo che ogni tubo contenga solo palline dello stesso colore.
            
            COME GIOCARE:
            • Clicca su un tubo per selezionare la pallina in cima
            • Clicca su un altro tubo per spostare la pallina
            • Puoi spostare una pallina solo se:
              - Il tubo di destinazione ha spazio
              - Il tubo di destinazione è vuoto OPPURE la pallina in cima è dello stesso colore
            
            MODALITÀ:
            • Facile: 6 tubi, 4 colori
            • Medio: 7 tubi, 5 colori
            
            VALUTAZIONE:
            Meno mosse usi, più stelle ottieni!
            
            SUGGERIMENTI:
            • Usa i tubi vuoti per organizzare le palline
            • Pianifica le mosse in anticipo
            • Usa i pulsanti AI se sei bloccato:
              - Suggerimento: Una singola mossa consigliata
              - Risoluzione Ottima: Soluzione migliore (più lenta)
              - Risoluzione Rapida: Soluzione veloce (meno ottimale)
            """;

        alert.setContentText(rulesText);
        alert.getDialogPane().setPrefWidth(550);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        alert.showAndWait();
    }

    public void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Bubble Sorting Game - Progetto di Intelligenza Artificiale");

        String aboutText = """
            PROGETTO UNIVERSITARIO
            Università della Calabria
            Corso di Laurea in Informatica
            Corso: Intelligenza Artificiale
            
            TECNOLOGIE UTILIZZATE:
            • Java 17+ con JavaFX per l'interfaccia grafica
            • Answer Set Programming (ASP) per la logica AI
            • Solver: DLV, DLV2, Clingo
            • Pattern architetturale: Model-View-Controller (MVC)
            
            INTELLIGENZA ARTIFICIALE:
            Il gioco utilizza ASP (Answer Set Programming) per:
            • Fornire suggerimenti intelligenti
            • Risolvere automaticamente i puzzle
            • Verificare la risolvibilità dei livelli
            • Due modalità di risoluzione: ottima e rapida
            
            CARATTERISTICHE:
            • 3 livelli di difficoltà 
            • 15 livelli totali (5 per difficoltà)
            • Sistema di salvataggio progresso
            • Animazioni fluide e interfaccia moderna
            • Design ispirato ai giochi web moderni
            
            SVILUPPATO CON DEDIZIONE
            """;

        alert.setContentText(aboutText);
        alert.getDialogPane().setPrefWidth(600);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        alert.showAndWait();
    }

    // Getters e Setters per i gestori di eventi

    public Scene getScene() {
        return scene;
    }

    public void setOnNewGameSelected(GameEventHandler.NewGameHandler handler) {
        this.onNewGameSelected = handler;
    }

    public void setOnExitSelected(GameEventHandler.ActionHandler handler) {
        this.onExitSelected = handler;
    }

    public void setOnRulesSelected(GameEventHandler.ActionHandler handler) {
        this.onRulesSelected = handler;
    }

    public void setOnAboutSelected(GameEventHandler.ActionHandler handler) {
        this.onAboutSelected = handler;
    }

    public void setOnSettingsSelected(GameEventHandler.ActionHandler handler) {
        this.onSettingsSelected = handler;
    }
}