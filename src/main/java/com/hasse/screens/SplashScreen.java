package com.hasse.screens;

import com.hasse.model.AppState;
import com.hasse.utils.ScreenNavigator;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class SplashScreen {

    private StackPane root;
    private Canvas graphCanvas;
    private double[] nodeX = {300, 200, 400, 300};
    private double[] nodeY = {80, 200, 200, 320};
    private double[] glowPhase = {0, 0.5, 1.0, 1.5};
    private AnimationTimer glowTimer;

    public SplashScreen() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #0d1117;");
        buildUI();
    }

    private void buildUI() {
        // Background gradient overlay
        Pane bgGradient = new Pane();
        bgGradient.setStyle("-fx-background-color: transparent;");
        RadialGradient rg = new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1a1a3e", 0.9)),
                new Stop(1, Color.web("#0d1117", 1.0)));
        bgGradient.setBackground(new Background(new BackgroundFill(rg, CornerRadii.EMPTY, Insets.EMPTY)));

        // Center card
        VBox centerContent = new VBox(24);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(50, 60, 50, 60));
        centerContent.setMaxWidth(540);

        // Graph canvas (animated nodes)
        graphCanvas = new Canvas(600, 380);
        drawStaticGraph();
        startGlowAnimation();

        // Title
        Label title = new Label("Hasse Diagram Visualizer");
        title.setStyle("-fx-font-size:36px; -fx-font-weight:bold; -fx-text-fill:white; " +
                "-fx-font-family:'Segoe UI';");
        title.setOpacity(0);

        Label tagline = new Label("From Relations to Visualization");
        tagline.setStyle("-fx-font-size:16px; -fx-text-fill:#8888aa; -fx-font-family:'Segoe UI';");
        tagline.setOpacity(0);

        // Decorative separator
        HBox sep = new HBox();
        sep.setAlignment(Pos.CENTER);
        Line sepLine = new Line(0, 0, 200, 0);
        sepLine.setStroke(Color.web("#00e5ff", 0.4));
        sepLine.setStrokeWidth(1);
        sep.getChildren().add(sepLine);
        sep.setOpacity(0);

        Button getStarted = new Button("Get Started  →");
        getStarted.getStyleClass().add("btn-primary");
        getStarted.setOpacity(0);
        getStarted.setPrefWidth(200);
        getStarted.setPrefHeight(46);

        getStarted.setOnAction(e -> {
            stopGlowAnimation();
            InputScreen inputScreen = new InputScreen(new AppState());
            ScreenNavigator.navigateTo(inputScreen.getRoot());
        });

        centerContent.getChildren().addAll(graphCanvas, title, tagline, sep, getStarted);

        StackPane.setAlignment(centerContent, Pos.CENTER);
        root.getChildren().addAll(bgGradient, centerContent);

        // Fade-in animations
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> {
            fadeIn(title, 600, 0);
            fadeIn(tagline, 600, 300);
            fadeIn(sep, 600, 500);
            fadeIn(getStarted, 700, 700);
        });
        pause.play();
    }

    private void drawStaticGraph() {
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 600, 380);

        double cx = 300;
        // Node positions: diamond-ish Hasse shape
        double[][] nodes = {
                {cx, 60},       // top
                {cx - 100, 160}, // mid-left
                {cx + 100, 160}, // mid-right
                {cx, 265},      // mid-center
                {cx, 330}        // bottom (not always visible)
        };

        // Edges
        int[][] edges = {{4, 1}, {4, 2}, {1, 0}, {2, 0}, {3, 1}, {3, 2}};
        gc.setStroke(Color.web("#00e5ff", 0.35));
        gc.setLineWidth(2);
        for (int[] e : edges) {
            gc.strokeLine(nodes[e[0]][0], nodes[e[0]][1], nodes[e[1]][0], nodes[e[1]][1]);
        }

        // Nodes
        for (double[] n : nodes) {
            // Glow
            gc.setFill(Color.web("#00e5ff", 0.12));
            gc.fillOval(n[0] - 20, n[1] - 20, 40, 40);
            gc.setFill(Color.web("#00e5ff", 0.18));
            gc.fillOval(n[0] - 14, n[1] - 14, 28, 28);
            gc.setFill(Color.web("#00e5ff"));
            gc.fillOval(n[0] - 9, n[1] - 9, 18, 18);
        }
    }

    private void startGlowAnimation() {
        glowTimer = new AnimationTimer() {
            long last = 0;
            double t = 0;
            @Override
            public void handle(long now) {
                if (last == 0) last = now;
                t += (now - last) / 1_000_000_000.0;
                last = now;
                animateGraph(t);
            }
        };
        glowTimer.start();
    }

    private void animateGraph(double t) {
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 600, 380);

        double cx = 300;
        double[][] nodes = {
                {cx, 60},
                {cx - 100, 160},
                {cx + 100, 160},
                {cx, 265},
                {cx, 330}
        };
        int[][] edges = {{4, 1}, {4, 2}, {1, 0}, {2, 0}, {3, 1}, {3, 2}};

        // Animated edges
        for (int[] e : edges) {
            double phase = (t * 1.2 + e[0] * 0.3) % (Math.PI * 2);
            double alpha = 0.2 + 0.2 * Math.sin(phase);
            gc.setStroke(Color.web("#00e5ff", alpha));
            gc.setLineWidth(1.8);
            gc.strokeLine(nodes[e[0]][0], nodes[e[0]][1], nodes[e[1]][0], nodes[e[1]][1]);
        }

        // Animated nodes
        for (int i = 0; i < nodes.length; i++) {
            double[] n = nodes[i];
            double phase = (t * 1.5 + i * 0.8) % (Math.PI * 2);
            double glow = 0.08 + 0.12 * (0.5 + 0.5 * Math.sin(phase));

            gc.setFill(Color.web("#00e5ff", glow * 1.5));
            gc.fillOval(n[0] - 22, n[1] - 22, 44, 44);
            gc.setFill(Color.web("#00e5ff", glow * 2.5));
            gc.fillOval(n[0] - 15, n[1] - 15, 30, 30);
            gc.setFill(Color.web("#1a3a4a"));
            gc.fillOval(n[0] - 9, n[1] - 9, 18, 18);
            gc.setFill(Color.web("#00e5ff"));
            gc.fillOval(n[0] - 7, n[1] - 7, 14, 14);
        }
    }

    private void stopGlowAnimation() {
        if (glowTimer != null) glowTimer.stop();
    }

    private void fadeIn(javafx.scene.Node node, int ms, int delayMs) {
        PauseTransition delay = new PauseTransition(Duration.millis(delayMs));
        delay.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });
        delay.play();
    }

    public Parent getRoot() {
        return root;
    }
}
