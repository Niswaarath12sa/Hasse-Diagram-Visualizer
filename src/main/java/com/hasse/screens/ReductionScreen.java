package com.hasse.screens;

import com.hasse.components.StepProgressBar;
import com.hasse.model.AppState;
import com.hasse.utils.ScreenNavigator;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReductionScreen {

    private BorderPane root;
    private AppState state;

    public ReductionScreen(AppState state) {
        this.state = state;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0d1117;");
        buildUI();
    }

    private void buildUI() {
        root.setTop(buildTopBar());

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-border-color:transparent;");

        VBox center = new VBox(24);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(40, 60, 40, 60));
        center.setMaxWidth(760);

        Label heading = new Label("Transitive Reduction");
        heading.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:white; -fx-font-family:'Segoe UI';");

        Label sub = new Label("Removing redundant edges to find the Hasse cover relations.");
        sub.setStyle("-fx-font-size:14px; -fx-text-fill:#7777aa;");

        // Before/After comparison card
        HBox compareRow = new HBox(16);
        compareRow.setAlignment(Pos.CENTER);

        Set<String[]> original = state.getRelations();
        Set<String[]> reduced = state.getReducedRelations();

        VBox beforeCard = buildRelCard("Before Reduction",
                original.stream().filter(r -> !r[0].equals(r[1]))
                        .map(r -> "(" + r[0] + "," + r[1] + ")")
                        .collect(Collectors.joining(", ")),
                "#3a1a24", "#ff4466");

        VBox afterCard = buildRelCard("After Reduction (Cover Relations)",
                reduced.stream()
                        .map(r -> "(" + r[0] + "," + r[1] + ")")
                        .collect(Collectors.joining(", ")),
                "#1a3a2a", "#00cc66");

        HBox.setHgrow(beforeCard, Priority.ALWAYS);
        HBox.setHgrow(afterCard, Priority.ALWAYS);
        compareRow.getChildren().addAll(beforeCard, afterCard);

        // Steps card
        VBox stepsCard = buildCard("Step-by-Step Removal");
        List<String> steps = state.getReductionSteps();

        if (steps.isEmpty()) {
            Label noSteps = new Label("✓  No redundant edges found. The relation is already in its minimal form.");
            noSteps.setStyle("-fx-font-size:13px; -fx-text-fill:#00cc66; -fx-font-family:'Segoe UI';");
            noSteps.setWrapText(true);
            stepsCard.getChildren().add(noSteps);
        } else {
            Label stepsHint = new Label("The following edges were removed because indirect paths exist:");
            stepsHint.setStyle("-fx-font-size:12px; -fx-text-fill:#666688;");
            stepsCard.getChildren().add(stepsHint);

            for (int i = 0; i < steps.size(); i++) {
                String step = steps.get(i);
                HBox stepRow = buildStepRow(i + 1, step);
                stepsCard.getChildren().add(stepRow);
                final int delay = i * 120;
                stepRow.setOpacity(0);
                PauseTransition p = new PauseTransition(Duration.millis(400 + delay));
                p.setOnFinished(e -> {
                    FadeTransition ft = new FadeTransition(Duration.millis(300), stepRow);
                    ft.setToValue(1);
                    ft.play();
                });
                p.play();
            }
        }

        // Summary card
        VBox summaryCard = buildCard("Reduction Summary");
        int removedCount = steps.size();
        HBox summRow = new HBox(20);
        summRow.getChildren().addAll(
                buildStatBox("Original Edges", String.valueOf(original.stream().filter(r -> !r[0].equals(r[1])).count())),
                buildStatBox("Removed", String.valueOf(removedCount), "#ff7799"),
                buildStatBox("Cover Relations", String.valueOf(reduced.size()), "#00cc66")
        );
        summaryCard.getChildren().add(summRow);

        // Buttons
        HBox btnRow = new HBox(16);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setPrefHeight(44);
        backBtn.setOnAction(e -> {
            RelationAnalysisScreen screen = new RelationAnalysisScreen(state);
            ScreenNavigator.navigateTo(screen.getRoot());
        });

        Button nextBtn = new Button("Generate Hasse Diagram  →");
        nextBtn.getStyleClass().add("btn-primary");
        nextBtn.setPrefHeight(44);
        nextBtn.setOnAction(e -> {
            HasseDiagramScreen screen = new HasseDiagramScreen(state);
            ScreenNavigator.navigateTo(screen.getRoot());
        });

        btnRow.getChildren().addAll(backBtn, nextBtn);

        center.getChildren().addAll(heading, sub, compareRow, stepsCard, summaryCard, btnRow);

        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getChildren().add(center);
        scroll.setContent(wrapper);
        root.setCenter(scroll);

        animateIn(compareRow, 0);
        animateIn(stepsCard, 100);
        animateIn(summaryCard, 200);
    }

    private VBox buildTopBar() {
        VBox topBox = new VBox(0);
        topBox.setStyle("-fx-background-color:#12121f; -fx-border-color:#1e1e3a; -fx-border-width:0 0 1 0;");
        HBox appBar = new HBox();
        appBar.setPadding(new Insets(14, 24, 14, 24));
        Label appName = new Label("⬡  Hasse Diagram Visualizer");
        appName.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00e5ff; -fx-font-family:'Segoe UI';");
        appBar.getChildren().add(appName);
        StepProgressBar progress = new StepProgressBar(2);
        topBox.getChildren().addAll(appBar, progress);
        return topBox;
    }

    private VBox buildCard(String title) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color:#181828; -fx-background-radius:12; " +
                "-fx-border-color:#2a2a45; -fx-border-radius:12; -fx-border-width:1;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#8888bb; " +
                "-fx-font-family:'Segoe UI';");
        card.getChildren().add(titleLbl);
        javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 0, 100, 0);
        line.setStroke(Color.web("#2a2a45"));
        card.getChildren().add(line);
        return card;
    }

    private VBox buildRelCard(String title, String content, String bgColor, String borderColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:" + bgColor + "; -fx-background-radius:10; " +
                "-fx-border-color:" + borderColor + "; -fx-border-radius:10; -fx-border-width:1;");
        Label t = new Label(title);
        t.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + borderColor + "; -fx-font-family:'Segoe UI';");
        Label c = new Label("{" + content + "}");
        c.setStyle("-fx-font-size:12px; -fx-text-fill:#ddddee; -fx-font-family:'JetBrains Mono','Consolas',monospace;");
        c.setWrapText(true);
        card.getChildren().addAll(t, c);
        return card;
    }

    private HBox buildStepRow(int num, String step) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color:#1e1828; -fx-background-radius:8;");

        Label numLbl = new Label(String.valueOf(num));
        numLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#555577; -fx-min-width:22; -fx-alignment:CENTER;");

        javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, 0, 16);
        sep.setStroke(Color.web("#333355"));

        Label stepLbl = new Label("⊖  " + step);
        stepLbl.setStyle("-fx-font-size:13px; -fx-text-fill:#ffaa88; -fx-font-family:'JetBrains Mono','Consolas',monospace;");
        stepLbl.setWrapText(true);

        row.getChildren().addAll(numLbl, sep, stepLbl);
        return row;
    }

    private VBox buildStatBox(String label, String value) {
        return buildStatBox(label, value, "#00e5ff");
    }

    private VBox buildStatBox(String label, String value, String color) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(14, 24, 14, 24));
        box.setStyle("-fx-background-color:#1e1e35; -fx-background-radius:10; " +
                "-fx-border-color:#2e2e50; -fx-border-radius:10; -fx-border-width:1;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#666688;");
        box.getChildren().addAll(val, lbl);
        return box;
    }

    private void animateIn(javafx.scene.Node node, int delayMs) {
        node.setOpacity(0);
        node.setTranslateY(16);
        PauseTransition delay = new PauseTransition(Duration.millis(delayMs));
        delay.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(400), node);
            ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), node);
            tt.setToY(0);
            new ParallelTransition(ft, tt).play();
        });
        delay.play();
    }

    public Parent getRoot() { return root; }
}
