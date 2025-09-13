package it.unical.informatica;

import it.unical.informatica.controller.MenuController;
import javafx.application.Application;
import javafx.stage.Stage;

public class BubbleSortingMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bubble Sorting Game - AI Project");
        primaryStage.setResizable(true);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(600);

        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        // Inizializza il controller del menu
        MenuController menuController = new MenuController(primaryStage);
        menuController.showMenu();

        primaryStage.show();
    }

}


