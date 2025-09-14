package it.unical.informatica.controller;

import it.unical.informatica.model.GameLevel;
import javafx.stage.Stage;
import it.unical.informatica.view.MenuView;

public class MenuController {

    private final Stage primaryStage;
    private MenuView menuView;

    public MenuController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.menuView = new MenuView();

        setupEventHandlers();
    }


    private void setupEventHandlers() {
        // Handler per iniziare una nuova partita
        menuView.setOnNewGameSelected(this::startNewGame);

        // Handler per uscire dal gioco
        menuView.setOnExitSelected(this::exitGame);

        // Handler per le regole del gioco
        menuView.setOnRulesSelected(this::showRules);

    }


    public void showMenu() {
        primaryStage.setScene(menuView.getScene());
    }


    private void startNewGame(GameLevel level, int levelNumber) {
        System.out.println("Avvio selezione livello: " + level.getDisplayName());

        LevelSelectionController levelController = new LevelSelectionController(
                primaryStage, this, level);
        levelController.show();
    }


    private void showRules() {
        menuView.showRulesDialog();
    }


    private void exitGame() {
        primaryStage.close();
    }


    public void returnToMenu() {
        showMenu();
    }
}