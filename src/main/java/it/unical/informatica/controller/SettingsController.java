package it.unical.informatica.controller;

import javafx.stage.Stage;
import it.unical.informatica.view.SettingsView;

/**
 * Controller per la gestione delle impostazioni del gioco
 */
public class SettingsController {

    private final Stage primaryStage;
    private final MenuController menuController;
    private SettingsView settingsView;
    private final GamePreferences preferences;

    public SettingsController(Stage primaryStage, MenuController menuController) {
        this.primaryStage = primaryStage;
        this.menuController = menuController;
        this.preferences = GamePreferences.getInstance();

        initializeView();
    }

    /**
     * Inizializza la vista delle impostazioni
     */
    private void initializeView() {
        settingsView = new SettingsView();
        setupEventHandlers();
        loadCurrentSettings();
    }

    /**
     * Configura gli event handlers
     */
    private void setupEventHandlers() {
        // Handler per il cambio delle impostazioni audio
        settingsView.setOnSoundToggled(this::toggleSound);

        // Handler per il cambio delle animazioni
        settingsView.setOnAnimationsToggled(this::toggleAnimations);

        // Handler per il cambio dei suggerimenti
        settingsView.setOnHintsToggled(this::toggleHints);

        // Handler per il reset del progresso
        settingsView.setOnResetProgressRequested(this::showResetConfirmation);

        // Handler per tornare al menu
        settingsView.setOnBackToMenu(this::returnToMenu);

        // Handler per le informazioni di debug
        settingsView.setOnDebugInfoRequested(this::showDebugInfo);
    }

    /**
     * Carica le impostazioni correnti nella vista
     */
    private void loadCurrentSettings() {
        settingsView.setSoundEnabled(preferences.isSoundEnabled());
        settingsView.setAnimationsEnabled(preferences.isAnimationsEnabled());
        settingsView.setHintsEnabled(preferences.isShowHints());
        settingsView.setTotalProgress(preferences.getTotalProgress());
    }

    /**
     * Mostra la vista delle impostazioni
     */
    public void show() {
        primaryStage.setScene(settingsView.getScene());
        primaryStage.setTitle("Impostazioni - Bubble Sorting Game");
    }

    /**
     * Gestisce il toggle del suono
     */
    private void toggleSound(boolean enabled) {
        preferences.setSoundEnabled(enabled);
        settingsView.showSoundFeedback(enabled);
    }

    /**
     * Gestisce il toggle delle animazioni
     */
    private void toggleAnimations(boolean enabled) {
        preferences.setAnimationsEnabled(enabled);
        settingsView.showAnimationsFeedback(enabled);
    }

    /**
     * Gestisce il toggle dei suggerimenti
     */
    private void toggleHints(boolean enabled) {
        preferences.setShowHints(enabled);
        settingsView.showHintsFeedback(enabled);
    }

    /**
     * Mostra la conferma per il reset del progresso
     */
    private void showResetConfirmation() {
        settingsView.showResetConfirmationDialog(
                this::resetAllProgress,
                this::resetSpecificDifficulty
        );
    }

    /**
     * Resetta tutto il progresso
     */
    private void resetAllProgress() {
        preferences.resetAllProgress();
        settingsView.setTotalProgress(0.0);
        settingsView.showResetSuccessMessage("Tutto il progresso è stato resettato!");
    }

    /**
     * Resetta il progresso per una difficoltà specifica
     */
    private void resetSpecificDifficulty() {
        settingsView.showDifficultySelectionDialog((level) -> {
            preferences.resetProgress(level);
            double newProgress = preferences.getTotalProgress();
            settingsView.setTotalProgress(newProgress);
            settingsView.showResetSuccessMessage(
                    "Progresso resettato per la difficoltà: " + level.getDisplayName());
        });
    }

    /**
     * Mostra le informazioni di debug
     */
    private void showDebugInfo() {
        var debugInfo = preferences.getDebugInfo();
        settingsView.showDebugInfoDialog(debugInfo);
    }

    /**
     * Torna al menu principale
     */
    private void returnToMenu() {
        menuController.returnToMenu();
    }

    /**
     * Applica le impostazioni a un controller di gioco
     */
    public void applySettingsToGame(GameController gameController) {
        // Applica le impostazioni al gioco corrente
        if (gameController != null && gameController.getGameView() != null) {
            gameController.getGameView().setAnimationsEnabled(preferences.isAnimationsEnabled());
            gameController.getGameView().setSoundEnabled(preferences.isSoundEnabled());
            gameController.getGameView().setHintsEnabled(preferences.isShowHints());
        }
    }

    /**
     * Verifica se ci sono impostazioni che richiedono il riavvio
     */
    public boolean requiresRestart() {
        // Per ora nessuna impostazione richiede il riavvio
        return false;
    }

    /**
     * Esporta le impostazioni per il backup
     */
    public String exportSettings() {
        StringBuilder export = new StringBuilder();
        export.append("# Bubble Sorting Game - Backup Impostazioni\n");
        export.append("# Generato il: ").append(java.time.LocalDateTime.now()).append("\n\n");

        var debugInfo = preferences.getDebugInfo();
        for (var entry : debugInfo.entrySet()) {
            export.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        return export.toString();
    }
}