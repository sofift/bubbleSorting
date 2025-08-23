package it.unical.informatica.view;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import it.unical.informatica.model.Ball;

/**
 * Utilità per la creazione di componenti UI
 */
public class UIUtils {

    /**
     * Crea un pulsante stilizzato
     */
    public static Button createStyledButton(String text, String... styleClasses) {
        Button button = new Button(text);
        button.getStyleClass().addAll(styleClasses);
        return button;
    }

    /**
     * Crea un container con padding e spacing standard
     */
    public static VBox createContainer(double spacing, Insets padding) {
        VBox container = new VBox();
        container.setSpacing(spacing);
        container.setPadding(padding);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    /**
     * Crea un container orizzontale
     */
    public static HBox createHorizontalContainer(double spacing, Pos alignment) {
        HBox container = new HBox();
        container.setSpacing(spacing);
        container.setAlignment(alignment);
        return container;
    }

    /**
     * Crea un testo stilizzato
     */
    public static Text createStyledText(String content, String... styleClasses) {
        Text text = new Text(content);
        text.getStyleClass().addAll(styleClasses);
        return text;
    }

    /**
     * Crea un separatore visivo
     */
    public static Region createSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #E0E0E0;");
        return separator;
    }

    /**
     * Crea un spacer che si espande
     */
    public static Region createExpandingSpacer() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /**
     * Applica un'animazione di fade in
     */
    public static void fadeIn(javafx.scene.Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Applica un'animazione di fade out
     */
    public static void fadeOut(javafx.scene.Node node, Duration duration, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        fade.play();
    }

    /**
     * Applica un'animazione di scale (ingrandimento/rimpicciolimento)
     */
    public static void scaleAnimation(javafx.scene.Node node, double fromScale, double toScale, Duration duration) {
        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(fromScale);
        scale.setFromY(fromScale);
        scale.setToX(toScale);
        scale.setToY(toScale);
        scale.play();
    }

    /**
     * Applica un'animazione di shake (scuotimento)
     */
    public static void shakeAnimation(javafx.scene.Node node, double intensity, Duration duration) {
        double originalX = node.getTranslateX();

        Timeline shake = new Timeline();

        // Crea i keyframes per l'animazione di shake
        for (int i = 0; i <= 10; i++) {
            double progress = (double) i / 10;
            double offset = Math.sin(progress * Math.PI * 4) * intensity * (1 - progress);

            KeyFrame frame = new KeyFrame(
                    Duration.millis(duration.toMillis() * progress),
                    new KeyValue(node.translateXProperty(), originalX + offset)
            );
            shake.getKeyFrames().add(frame);
        }

        shake.play();
    }

    /**
     * Applica un'animazione di bounce (rimbalzo)
     */
    public static void bounceAnimation(javafx.scene.Node node, Duration duration) {
        Timeline bounce = new Timeline();

        KeyFrame frame1 = new KeyFrame(Duration.ZERO, new KeyValue(node.scaleYProperty(), 1.0));
        KeyFrame frame2 = new KeyFrame(duration.multiply(0.4), new KeyValue(node.scaleYProperty(), 1.2, Interpolator.EASE_OUT));
        KeyFrame frame3 = new KeyFrame(duration.multiply(0.6), new KeyValue(node.scaleYProperty(), 0.9, Interpolator.EASE_IN));
        KeyFrame frame4 = new KeyFrame(duration, new KeyValue(node.scaleYProperty(), 1.0, Interpolator.EASE_OUT));

        bounce.getKeyFrames().addAll(frame1, frame2, frame3, frame4);
        bounce.play();
    }

    /**
     * Crea un effetto di glow
     */
    public static void addGlowEffect(javafx.scene.Node node, Color color, double radius) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(radius);
        glow.setSpread(0.6);
        node.setEffect(glow);
    }

    /**
     * Rimuove gli effetti da un nodo
     */
    public static void removeEffects(javafx.scene.Node node) {
        node.setEffect(null);
    }

    /**
     * Crea un tooltip informativo
     */
    public static Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(500));
        tooltip.setHideDelay(Duration.millis(100));
        return tooltip;
    }

    /**
     * Crea un loading spinner
     */
    public static ProgressIndicator createLoadingSpinner(double size) {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(size, size);
        spinner.setProgress(-1); // Animazione infinita
        return spinner;
    }

    /**
     * Crea una card con ombra
     */
    public static VBox createCard(double spacing, Insets padding) {
        VBox card = new VBox();
        card.setSpacing(spacing);
        card.setPadding(padding);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);
            """);
        return card;
    }

    /**
     * Ottiene il colore JavaFX per una pallina
     */
    public static Color getBallColor(Ball.Color ballColor) {
        return switch (ballColor) {
            case RED -> Color.web("#FF6B6B");
            case BLUE -> Color.web("#4DABF7");
            case GREEN -> Color.web("#51CF66");
            case YELLOW -> Color.web("#FFD43B");
            case ORANGE -> Color.web("#FF8A65");
            case PURPLE -> Color.web("#9C88FF");
            case PINK -> Color.web("#FFB3D9");
        };
    }

    /**
     * Crea un'animazione di pulsazione
     */
    public static Animation createPulseAnimation(javafx.scene.Node node) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.5), node);
        scaleUp.setToX(1.1);
        scaleUp.setToY(1.1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.5), node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        SequentialTransition pulse = new SequentialTransition(scaleUp, scaleDown);
        pulse.setCycleCount(Animation.INDEFINITE);

        return pulse;
    }

    /**
     * Mostra un toast message (notifica temporanea)
     */
    public static void showToast(String message, javafx.scene.Node parent) {
        Label toast = new Label(message);
        toast.setStyle("""
            -fx-background-color: rgba(0, 0, 0, 0.8);
            -fx-text-fill: white;
            -fx-padding: 10px 15px;
            -fx-background-radius: 5px;
            -fx-font-size: 14px;
            """);

        // Posiziona il toast
        if (parent instanceof Pane) {
            Pane pane = (Pane) parent;
            toast.setLayoutX(pane.getWidth() / 2 - 100);
            toast.setLayoutY(pane.getHeight() - 100);

            pane.getChildren().add(toast);

            // Animazione di apparizione e scomparsa
            fadeIn(toast, Duration.millis(300));

            Timeline removeToast = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                fadeOut(toast, Duration.millis(300), () -> pane.getChildren().remove(toast));
            }));
            removeToast.play();
        }
    }

    /**
     * Crea un'animazione di rotazione
     */
    public static RotateTransition createRotateAnimation(javafx.scene.Node node, double angle, Duration duration) {
        RotateTransition rotate = new RotateTransition(duration, node);
        rotate.setByAngle(angle);
        return rotate;
    }

    /**
     * Crea un'animazione di trascinamento delle palline
     */
    public static ParallelTransition createBallMoveAnimation(javafx.scene.Node ball,
                                                             double fromX, double fromY,
                                                             double toX, double toY,
                                                             Duration duration) {
        TranslateTransition moveX = new TranslateTransition(duration, ball);
        moveX.setFromX(fromX);
        moveX.setToX(toX);

        TranslateTransition moveY = new TranslateTransition(duration, ball);
        moveY.setFromY(fromY);
        moveY.setToY(toY);

        // Aggiungi un effetto di arco per rendere il movimento più naturale
        Timeline arcEffect = new Timeline();
        double peakY = Math.min(fromY, toY) - 30; // Punto più alto dell'arco

        for (int i = 0; i <= 10; i++) {
            double progress = (double) i / 10;
            double currentY = fromY + (toY - fromY) * progress;

            // Aggiungi l'effetto arco
            double arcOffset = Math.sin(progress * Math.PI) * Math.abs(peakY - currentY);
            currentY -= arcOffset;

            KeyFrame frame = new KeyFrame(
                    Duration.millis(duration.toMillis() * progress),
                    new KeyValue(ball.translateYProperty(), currentY, Interpolator.EASE_BOTH)
            );
            arcEffect.getKeyFrames().add(frame);
        }

        ParallelTransition movement = new ParallelTransition(moveX, arcEffect);
        return movement;
    }

    /**
     * Crea un gradiente per i background
     */
    public static String createGradientBackground(String color1, String color2, String direction) {
        return String.format("-fx-background-color: linear-gradient(%s, %s, %s);",
                direction, color1, color2);
    }

    /**
     * Applica un effetto hover a un pulsante
     */
    public static void addHoverEffect(Button button) {
        button.setOnMouseEntered(e -> scaleAnimation(button, 1.0, 1.05, Duration.millis(100)));
        button.setOnMouseExited(e -> scaleAnimation(button, 1.05, 1.0, Duration.millis(100)));
    }

    /**
     * Crea un indicatore di caricamento personalizzato
     */
    public static VBox createCustomLoadingIndicator(String message) {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(15);

        ProgressIndicator spinner = createLoadingSpinner(40);
        Text loadingText = createStyledText(message, "loading-text");

        container.getChildren().addAll(spinner, loadingText);

        return container;
    }
}