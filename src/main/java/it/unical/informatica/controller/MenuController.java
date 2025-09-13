package it.unical.informatica.controller;

import it.unical.informatica.model.GameLevel;
import javafx.stage.Stage;
import it.unical.informatica.view.MenuView;

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