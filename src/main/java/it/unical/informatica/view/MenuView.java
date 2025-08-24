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

/**
 * Vista del menu principale del gioco
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

    /**
     * Crea la scena del menu principale
     */
    private void createMenuScene() {
        mainContainer = new VBox();
        mainContainer.getStyleClass().add("menu-container");
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setSpacing(30);

        // Titolo del gioco
        createTitle();

        // Pulsanti del menu
        createMenuButtons();

        // Informazioni del progetto
        createProjectInfo();

        // Crea la scena
        scene = new Scene(mainContainer, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    }

    /**
     * Crea il titolo del gioco
     */
    private void createTitle() {
        VBox titleBox = new VBox();
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setSpacing(10);

        Text title = new Text("Bubble Sorting Game");
        title.getStyleClass().add("menu-title");

        Text subtitle = new Text("Ordina le palline colorate - Progetto di Intelligenza Artificiale");
        subtitle.getStyleClass().add("menu-subtitle");

        titleBox.getChildren().addAll(title, subtitle);
        mainContainer.getChildren().add(titleBox);
    }

    /**
     * Crea i pulsanti del menu
     */
    private void createMenuButtons() {
        VBox buttonBox = new VBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(15);
        buttonBox.setPadding(new Insets(20, 0, 20, 0));

        // Pulsante Nuova Partita
        Button newGameButton = createMenuButton("🎮 Nuova Partita", "menu-button");
        newGameButton.setOnAction(e -> showDifficultySelection());

        // Pulsante Regole
        Button rulesButton = createMenuButton("📖 Regole del Gioco", "menu-button secondary-button");
        rulesButton.setOnAction(e -> {
            if (onRulesSelected != null) {
                try {
                    onRulesSelected.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Pulsante Impostazioni
        Button settingsButton = createMenuButton("⚙️ Impostazioni", "menu-button secondary-button");
        settingsButton.setOnAction(e -> {
            if (onSettingsSelected != null) {
                try {
                    onSettingsSelected.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Pulsante Info
        Button aboutButton = createMenuButton("ℹ️ Info Progetto", "menu-button secondary-button");
        aboutButton.setOnAction(e -> {
            if (onAboutSelected != null) {
                try {
                    onAboutSelected.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Pulsante Esci
        Button exitButton = createMenuButton("🚪 Esci", "menu-button danger-button");
        exitButton.setOnAction(e -> {
            if (onExitSelected != null) {
                try {
                    onExitSelected.onAction();
                } catch (ObjectNotValidException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAnnotationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        buttonBox.getChildren().addAll(
                newGameButton, rulesButton, settingsButton, aboutButton, exitButton
        );

        mainContainer.getChildren().add(buttonBox);
    }

    /**
     * Crea un pulsante del menu con stile
     */
    private Button createMenuButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll(styleClass.split(" "));
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    /**
     * Crea le informazioni del progetto in basso
     */
    private void createProjectInfo() {
        VBox infoBox = new VBox();
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(5);

        Text projectInfo = new Text("Università della Calabria - Corso di Intelligenza Artificiale");
        projectInfo.getStyleClass().add("menu-subtitle");
        projectInfo.setStyle("-fx-font-size: 12px;");

        Text techInfo = new Text("Java + Answer Set Programming (ASP) + JavaFX");
        techInfo.getStyleClass().add("menu-subtitle");
        techInfo.setStyle("-fx-font-size: 10px; -fx-opacity: 0.8;");

        infoBox.getChildren().addAll(projectInfo, techInfo);

        // Posiziona in basso
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        mainContainer.getChildren().addAll(spacer, infoBox);
    }

    /**
     * Mostra la selezione della difficoltà
     */
    private void showDifficultySelection() {
        Dialog<GameLevel> dialog = new Dialog<>();
        dialog.setTitle("Seleziona Difficoltà");
        dialog.setHeaderText("Scegli il livello di difficoltà per iniziare a giocare");

        // Crea il contenuto del dialog
        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        ToggleGroup difficultyGroup = new ToggleGroup();

        for (GameLevel level : GameLevel.values()) {
            VBox levelBox = createDifficultyOption(level, difficultyGroup);
            content.getChildren().add(levelBox);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        // Pulsanti
        ButtonType playButtonType = new ButtonType("Gioca!", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(playButtonType, cancelButtonType);

        // Disabilita il pulsante Gioca finché non si seleziona una difficoltà
        Button playButton = (Button) dialog.getDialogPane().lookupButton(playButtonType);
        playButton.setDisable(true);

        difficultyGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            playButton.setDisable(newToggle == null);
        });

        // Converter per il risultato
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == playButtonType && difficultyGroup.getSelectedToggle() != null) {
                return (GameLevel) difficultyGroup.getSelectedToggle().getUserData();
            }
            return null;
        });

        // Mostra il dialog e gestisci il risultato
        dialog.showAndWait().ifPresent(level -> {
            if (onNewGameSelected != null) {
                onNewGameSelected.onNewGame(level, 1); // Inizia dal livello 1
            }
        });
    }

    /**
     * Crea un'opzione di difficoltà per il dialog
     */
    private VBox createDifficultyOption(GameLevel level, ToggleGroup group) {
        VBox optionBox = new VBox();
        optionBox.setAlignment(Pos.CENTER);
        optionBox.setSpacing(10);
        optionBox.setPadding(new Insets(15));
        optionBox.getStyleClass().add("level-card");

        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(group);
        radioButton.setUserData(level);

        Text levelName = new Text(level.getDisplayName());
        levelName.getStyleClass().add("level-number");
        levelName.setStyle("-fx-font-size: 20px;");

        Text levelInfo = new Text(String.format("%d tubi, %d colori",
                level.getNumberOfTubes(), level.getNumberOfColors()));
        levelInfo.setStyle("-fx-font-size: 12px; -fx-fill: #666666;");

        optionBox.getChildren().addAll(radioButton, levelName, levelInfo);

        // Rendi cliccabile tutta la card
        optionBox.setOnMouseClicked(e -> radioButton.setSelected(true));

        return optionBox;
    }

    /**
     * Mostra il dialog delle regole
     */
    public void showRulesDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Regole del Gioco");
        alert.setHeaderText("Come giocare a Bubble Sorting Game");

        String rulesText = """
            🎯 OBIETTIVO:
            Ordinare tutte le palline colorate in modo che ogni tubo contenga solo palline dello stesso colore.
            
            🎮 COME GIOCARE:
            • Clicca su un tubo per selezionare la pallina in cima
            • Clicca su un altro tubo per spostare la pallina
            • Puoi spostare una pallina solo se:
              - Il tubo di destinazione ha spazio
              - Il tubo di destinazione è vuoto OPPURE la pallina in cima è dello stesso colore
            
            📊 MODALITÀ:
            • Facile: 6 tubi, 4 colori
            • Medio: 7 tubi, 5 colori  
            • Difficile: 9 tubi, 7 colori
            
            ⭐ VALUTAZIONE:
            Meno mosse usi, più stelle ottieni!
            
            💡 SUGGERIMENTI:
            • Usa i tubi vuoti per organizzare le palline
            • Pianifica le mosse in anticipo
            • Usa il pulsante "Suggerimento" se sei bloccato
            """;

        alert.setContentText(rulesText);
        alert.getDialogPane().setPrefWidth(500);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        alert.showAndWait();
    }

    /**
     * Mostra il dialog delle informazioni
     */
    public void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazioni sul Progetto");
        alert.setHeaderText("Bubble Sorting Game - Progetto di Intelligenza Artificiale");

        String aboutText = """
            🎓 PROGETTO UNIVERSITARIO
            Università della Calabria
            Corso di Laurea in Informatica
            Corso: Intelligenza Artificiale
            
            💻 TECNOLOGIE UTILIZZATE:
            • Java 17+ con JavaFX per l'interfaccia grafica
            • Answer Set Programming (ASP) per la logica AI
            • Solver: DLV, DLV2, Clingo
            • Pattern architetturale: Model-View-Controller (MVC)
            
            🧠 INTELLIGENZA ARTIFICIALE:
            Il gioco utilizza ASP (Answer Set Programming) per:
            • Fornire suggerimenti intelligenti
            • Risolvere automaticamente i puzzle
            • Verificare la risolvibilità dei livelli
            
            🎮 CARATTERISTICHE:
            • 3 livelli di difficoltà
            • 15 livelli totali (5 per difficoltà)
            • Sistema di salvataggio progresso
            • Animazioni fluide e interfaccia moderna
            
            📧 SVILUPPATO CON ❤️
            """;

        alert.setContentText(aboutText);
        alert.getDialogPane().setPrefWidth(550);
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