package com.hasse;

import com.hasse.screens.SplashScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        stage.setTitle("Hasse Diagram Visualizer");
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.setWidth(1280);
        stage.setHeight(800);

        SplashScreen splash = new SplashScreen();
        Scene scene = new Scene(splash.getRoot(), 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/com/hasse/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
