package com.hasse.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class StepProgressBar extends HBox {

    private static final String[] STEP_NAMES = {"Input", "Analysis", "Reduction", "Diagram"};

    public StepProgressBar(int currentStep) {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10, 40, 10, 40));
        setSpacing(0);
        setStyle("-fx-background-color:transparent;");

        for (int i = 0; i < STEP_NAMES.length; i++) {
            boolean done    = i < currentStep;
            boolean active  = i == currentStep;

            // ── Dot ──
            Circle dot = new Circle(10);
            if (done) {
                dot.setFill(Color.web("#00e5ff"));
                dot.setStroke(Color.web("#00e5ff"));
                dot.setStrokeWidth(2);
            } else if (active) {
                dot.setFill(Color.web("#0d1117"));
                dot.setStroke(Color.web("#00e5ff"));
                dot.setStrokeWidth(2.5);
            } else {
                dot.setFill(Color.web("#1e1e3a"));
                dot.setStroke(Color.web("#2a2a55"));
                dot.setStrokeWidth(1.5);
            }

            // Checkmark label inside dot for done steps
            Label check = new Label(done ? "✓" : String.valueOf(i + 1));
            check.setStyle("-fx-font-size:9px; -fx-font-weight:bold; -fx-text-fill:" +
                    (done ? "#0d1117" : active ? "#00e5ff" : "#2a2a55") + ";");

            StackPane dotStack = new StackPane(dot, check);

            // ── Step name ──
            Label name = new Label(STEP_NAMES[i]);
            name.setStyle("-fx-font-size:10px; -fx-font-family:'Segoe UI'; " +
                    "-fx-font-weight:" + (active ? "bold" : "normal") + "; " +
                    "-fx-text-fill:" + (active ? "#00e5ff" : done ? "#7ecfdf" : "#333366") + ";");

            VBox stepBox = new VBox(5, dotStack, name);
            stepBox.setAlignment(Pos.CENTER);
            stepBox.setMinWidth(70);
            getChildren().add(stepBox);

            // ── Connector line between steps ──
            if (i < STEP_NAMES.length - 1) {
                VBox lineWrapper = new VBox();
                lineWrapper.setAlignment(Pos.CENTER);
                lineWrapper.setMinWidth(60);
                lineWrapper.setPadding(new Insets(0, 0, 18, 0)); // push up to dot level

                Region line = new Region();
                line.setPrefWidth(60);
                line.setPrefHeight(2);
                line.setStyle("-fx-background-color:" +
                        (done ? "#00e5ff" : "#1e1e3a") + "; -fx-background-radius:2;");

                lineWrapper.getChildren().add(line);
                getChildren().add(lineWrapper);
            }
        }
    }
}
