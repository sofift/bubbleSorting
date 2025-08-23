package it.unical.informatica.controller;

import javafx.stage.Stage;
import it.unical.informatica.model.GameLevel;
import it.unical.informatica.view.MenuView;
import it.unical.informatica.view.GameView;

/**
 * Controller per il menu principale del gioco
 */
public class MenuController {

    private final Stage primaryStage;
    private MenuView menuView;

    public MenuController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.menuView = new MenuView();

        // Configura gli event handlers
        setupEventHandlers();
    }

    /**
     * Configura gli event handlers per il menu
     */
    private void setupEventHandlers() {
        // Handler per iniziare una nuova partita
        menuView.setOnNewGameSelected(this::startNewGame);

        // Handler per uscire dal gioco
        menuView.setOnExitSelected(this::exitGame);

        // Handler per le regole del gioco
        menuView.setOnRulesSelected(this::showRules);

        // Handler per le impostazioni
        menuView.setOnSettingsSelected(this::showSettings);
    }

    /**
     * Mostra il menu principale
     */
    public void showMenu() {
        primaryStage.setScene(menuView.getScene());
    }

    /**
     * Avvia una nuova partita con il livello selezionato
     */
    private void startNewGame(GameLevel level, int levelNumber) {
        System.out.println("Avvio selezione livello: " + level.getDisplayName());

        // Mostra la schermata di selezione livelli
        LevelSelectionController levelController = new LevelSelectionController(
                primaryStage, this, level);
        levelController.show();
    }

    /**
     * Mostra le regole del gioco
     */
    private void showRules() {
        menuView.showRulesDialog();
    }

    /**
     * Mostra le impostazioni del gioco
     */
    private void showSettings() {
        SettingsController settingsController = new SettingsController(primaryStage, this);
        settingsController.show();
    }

    /**
     * Mostra informazioni sul progetto
     */
    private void showAbout() {
        menuView.showAboutDialog();
    }

    /**
     * Esce dal gioco
     */
    private void exitGame() {
        primaryStage.close();
    }

    /**
     * Ritorna al menu principale (chiamato dal GameController)
     */
    public void returnToMenu() {
        showMenu();
    }
}