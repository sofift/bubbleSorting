package it.unical.informatica.view;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Dialog per mostrare il progresso di operazioni lunghe
 */
public class ProgressDialog {

    private Stage stage;
    private ProgressBar progressBar;
    private Label messageLabel;
    private Label detailLabel;
    private Button cancelButton;
    private boolean cancellable = false;
    private Task<?> boundTask;

    public ProgressDialog() {
        createDialog();
    }

    public ProgressDialog(boolean cancellable) {
        this.cancellable = cancellable;
        createDialog();
    }

    private void createDialog() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(350);

        // Messaggio principale
        messageLabel = new Label("Elaborazione in corso...");
        messageLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Barra di progresso
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setProgress(0);

        // Dettagli aggiuntivi
        detailLabel = new Label("");
        detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        detailLabel.setWrapText(true);
        detailLabel.setPrefWidth(300);

        root.getChildren().addAll(messageLabel, progressBar, detailLabel);

        // Pulsante di cancellazione (se abilitato)
        if (cancellable) {
            cancelButton = new Button("Annulla");
            cancelButton.setOnAction(e -> {
                if (boundTask != null) {
                    boundTask.cancel();
                }
                close();
            });
            root.getChildren().add(cancelButton);
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setScene(scene);
    }

    /**
     * Collega il dialog a un Task per aggiornamenti automatici
     */
    public void bindToTask(Task<?> task) {
        this.boundTask = task;

        // Collega il progresso
        progressBar.progressProperty().bind(task.progressProperty());

        // Collega il messaggio
        messageLabel.textProperty().bind(task.messageProperty());

        // Collega i dettagli (se disponibili)
        task.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            if (newMessage != null && !newMessage.isEmpty()) {
                detailLabel.setText(newMessage);
            }
        });

        // Chiude automaticamente al completamento
        task.setOnSucceeded(e -> {
            if (stage.isShowing()) {
                close();
            }
        });

        task.setOnFailed(e -> {
            if (stage.isShowing()) {
                close();
            }
        });

        task.setOnCancelled(e -> {
            if (stage.isShowing()) {
                close();
            }
        });
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.close();
    }

    public void setTitle(String title) {
        stage.setTitle(title);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setDetail(String detail) {
        detailLabel.setText(detail);
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public void setIndeterminate(boolean indeterminate) {
        if (indeterminate) {
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        } else {
            progressBar.setProgress(0);
        }
    }

    public boolean isShowing() {
        return stage.isShowing();
    }

    public Stage getStage() {
        return stage;
    }
}