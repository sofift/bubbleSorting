package it.unical.informatica.controller;

import it.unical.informatica.model.GameLevel;
import javafx.stage.Stage;
import it.unical.informatica.view.LevelSelectionView;

/**
 * Controller per la selezione dei livelli - VERSIONE CORRETTA
 */
public class LevelSelectionController {

    private final Stage primaryStage;
    private final MenuController menuController;
    private final GameLevel selectedDifficulty;
    private LevelSelectionView levelSelectionView;

    public LevelSelectionController(Stage primaryStage, MenuController menuController,
                                    GameLevel selectedDifficulty) {
        this.primaryStage = primaryStage;
        this.menuController = menuController;
        this.selectedDifficulty = selectedDifficulty;

        initializeView();
    }

    private void initializeView() {
        try {
            levelSelectionView = new LevelSelectionView(selectedDifficulty);
            setupEventHandlers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupEventHandlers() {
        if (levelSelectionView == null) return;

        // Handler per la selezione di un livello
        levelSelectionView.setOnLevelSelected((level, levelNumber) -> {
            try {
                this.startSelectedLevel(level, levelNumber);
            } catch (Exception e) {
                System.err.println("Errore nell'avvio del livello: " + e.getMessage());
                e.printStackTrace();
                showErrorAndReturn("Errore nell'avvio del livello: " + e.getMessage());
            }
        });

        // Handler per tornare al menu
        levelSelectionView.setOnBackToMenu(() -> {
            try {
                this.returnToMenu();
            } catch (Exception e) {
                System.err.println("Errore nel ritorno al menu: " + e.getMessage());
                if (menuController != null) {
                    menuController.returnToMenu();
                }
            }
        });

        // Handler per la selezione di una difficoltà diversa
        levelSelectionView.setOnDifficultyChanged((level, levelNumber) -> {
            try {
                this.changeDifficulty(level);
            } catch (Exception e) {
                System.err.println("Errore nel cambio difficoltà: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    private void showErrorAndReturn(String message) {
        try {
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText("Impossibile avviare il livello");
                alert.setContentText(message + "\n\nTornando al menu principale...");
                alert.showAndWait();

                returnToMenu();
            });
        } catch (Exception e) {
            System.err.println("Errore anche nel mostrare l'errore: " + e.getMessage());
            returnToMenu();
        }
    }


    public void show() {
        try {
            if (levelSelectionView == null || levelSelectionView.getScene() == null) {
                returnToMenu();
                return;
            }

            primaryStage.setScene(levelSelectionView.getScene());
            primaryStage.setTitle("Selezione Livello - " + selectedDifficulty.getDisplayName());
        } catch (Exception e) {
            System.err.println("Errore nel mostrare la vista: " + e.getMessage());
            returnToMenu();
        }
    }


    private void startSelectedLevel(GameLevel level, int levelNumber) {
        try {
            if (GameEventHandler.EventValidator.isValidLevelNumber(levelNumber)) {
                GameController gameController = new GameController(
                        primaryStage, menuController, level, levelNumber);
                gameController.startGame();


            } else {
                showErrorAndReturn("Numero livello non valido: " + levelNumber);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAndReturn("Errore tecnico nell'avvio del livello");
        }
    }


    private void startSelectedLevel(int levelNumber) {
        startSelectedLevel(selectedDifficulty, levelNumber);
    }


    private void returnToMenu() {
        try {
            if (menuController != null) {
                menuController.returnToMenu();
            }
        } catch (Exception e) {
            System.err.println("Errore nel ritorno al menu: " + e.getMessage());
        }
    }


    private void changeDifficulty(GameLevel newDifficulty) {
        try {
            LevelSelectionController newController = new LevelSelectionController(
                    primaryStage, menuController, newDifficulty);
            newController.show();
        } catch (Exception e) {
            System.err.println("Errore nel cambio difficoltà: " + e.getMessage());
        }
    }


    public boolean isLevelCompleted(int levelNumber) {
        return false; // TODO: Implementare con GamePreferences
    }


    public int getLevelStars(int levelNumber) {
        return 0; // TODO: Implementare sistema stelle
    }


    public boolean isLevelUnlocked(int levelNumber) {
        return true; // TODO: Implementare logica sblocco
    }
}