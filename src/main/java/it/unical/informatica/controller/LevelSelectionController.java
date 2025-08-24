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

    /**
     * Inizializza la vista di selezione livelli
     */
    private void initializeView() {
        try {
            levelSelectionView = new LevelSelectionView(selectedDifficulty);
            setupEventHandlers();
        } catch (Exception e) {
            System.err.println("❌ Errore nell'inizializzazione della vista: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configura gli event handlers ✅ SENZA ECCEZIONI OBBLIGATORIE
     */
    private void setupEventHandlers() {
        if (levelSelectionView == null) return;

        // Handler per la selezione di un livello
        levelSelectionView.setOnLevelSelected((level, levelNumber) -> {
            try {
                this.startSelectedLevel(level, levelNumber);
            } catch (Exception e) {
                System.err.println("❌ Errore nell'avvio del livello: " + e.getMessage());
                e.printStackTrace();
                // ✅ NON lanciare RuntimeException, gestisci l'errore
                showErrorAndReturn("Errore nell'avvio del livello: " + e.getMessage());
            }
        });

        // Handler per tornare al menu
        levelSelectionView.setOnBackToMenu(() -> {
            try {
                this.returnToMenu();
            } catch (Exception e) {
                System.err.println("❌ Errore nel ritorno al menu: " + e.getMessage());
                // ✅ Anche in caso di errore, prova a tornare al menu
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
                System.err.println("❌ Errore nel cambio difficoltà: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * ✅ Mostra errore all'utente e torna al menu
     */
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
            System.err.println("❌ Errore anche nel mostrare l'errore: " + e.getMessage());
            returnToMenu();
        }
    }

    /**
     * Mostra la vista di selezione livelli
     */
    public void show() {
        try {
            if (levelSelectionView == null || levelSelectionView.getScene() == null) {
                System.err.println("❌ LevelSelectionView non inizializzata");
                returnToMenu();
                return;
            }

            primaryStage.setScene(levelSelectionView.getScene());
            primaryStage.setTitle("Selezione Livello - " + selectedDifficulty.getDisplayName());
        } catch (Exception e) {
            System.err.println("❌ Errore nel mostrare la vista: " + e.getMessage());
            returnToMenu();
        }
    }

    /**
     * ✅ Avvia il livello selezionato SENZA ECCEZIONI OBBLIGATORIE
     */
    private void startSelectedLevel(GameLevel level, int levelNumber) {
        try {
            if (GameEventHandler.EventValidator.isValidLevelNumber(levelNumber)) {
                System.out.println("🚀 Avviando livello: " + level.getDisplayName() + " " + levelNumber);

                // ✅ COSTRUTTORE CORRETTO SENZA ECCEZIONI
                GameController gameController = new GameController(
                        primaryStage, menuController, level, levelNumber);
                gameController.startGame();

                System.out.println("✅ Livello avviato correttamente");
            } else {
                System.err.println("❌ Numero livello non valido: " + levelNumber);
                showErrorAndReturn("Numero livello non valido: " + levelNumber);
            }
        } catch (Exception e) {
            System.err.println("❌ ERRORE nell'avvio del livello: " + e.getMessage());
            e.printStackTrace();
            showErrorAndReturn("Errore tecnico nell'avvio del livello");
        }
    }

    /**
     * Overload per compatibilità - avvia il livello con la difficoltà corrente
     */
    private void startSelectedLevel(int levelNumber) {
        startSelectedLevel(selectedDifficulty, levelNumber);
    }

    /**
     * Torna al menu principale
     */
    private void returnToMenu() {
        try {
            if (menuController != null) {
                menuController.returnToMenu();
            } else {
                System.err.println("❌ MenuController è null");
            }
        } catch (Exception e) {
            System.err.println("❌ Errore nel ritorno al menu: " + e.getMessage());
        }
    }

    /**
     * Cambia la difficoltà
     */
    private void changeDifficulty(GameLevel newDifficulty) {
        try {
            LevelSelectionController newController = new LevelSelectionController(
                    primaryStage, menuController, newDifficulty);
            newController.show();
        } catch (Exception e) {
            System.err.println("❌ Errore nel cambio difficoltà: " + e.getMessage());
        }
    }

    /**
     * Ottiene lo stato di completamento per un livello
     */
    public boolean isLevelCompleted(int levelNumber) {
        return false; // TODO: Implementare con GamePreferences
    }

    /**
     * Ottiene il numero di stelle per un livello completato
     */
    public int getLevelStars(int levelNumber) {
        return 0; // TODO: Implementare sistema stelle
    }

    /**
     * Verifica se un livello è sbloccato
     */
    public boolean isLevelUnlocked(int levelNumber) {
        return true; // TODO: Implementare logica sblocco
    }
}