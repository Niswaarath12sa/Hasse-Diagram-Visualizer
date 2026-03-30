package com.hasse.utils;

import com.hasse.MainApp;
import javafx.scene.Parent;

public class ScreenNavigator {
    public static void navigateTo(Parent root) {
        MainApp.primaryStage.getScene().setRoot(root);
    }
}
