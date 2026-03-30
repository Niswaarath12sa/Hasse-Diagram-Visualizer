package com.hasse.screens;

import com.hasse.components.StepProgressBar;
import com.hasse.model.AppState;
import com.hasse.utils.DMATEngine;
import com.hasse.utils.ScreenNavigator;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RelationAnalysisScreen {

    private BorderPane root;
    private AppState state;

    public RelationAnalysisScreen(AppState state) {
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

        Label heading = new Label("Relation Analysis");
        heading.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:white; -fx-font-family:'Segoe UI';");

        Label sub = new Label("Exploring the structure of your partial order relation.");
        sub.setStyle("-fx-font-size:14px; -fx-text-fill:#7777aa;");

        // relation set display
        VBox relCard = buildCard("Generated Relation Set");
        Set<String[]> rels = state.getRelations();
        String relStr = "{" + rels.stream()
                .map(r -> "(" + r[0] + "," + r[1] + ")")
                .collect(Collectors.joining(", ")) + "}";
        Label relLabel = new Label(relStr);
        relLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#aaeeff; -fx-font-family:'JetBrains Mono','Consolas',monospace;");
        relLabel.setWrapText(true);
        relCard.getChildren().add(relLabel);

        // relation properties
        VBox propsCard = buildCard("Relation Properties");
        Label propsInfo = new Label("Analysis based on " + state.getElements().size() + " elements:");
        propsInfo.setStyle("-fx-font-size:12px; -fx-text-fill:#555577;");
        propsCard.getChildren().add(propsInfo);
        HBox propsRow = new HBox(16);
        propsRow.setAlignment(Pos.CENTER_LEFT);
        propsRow.getChildren().addAll(
                buildPropertyChip("Reflexive",     state.isReflexive()),
                buildPropertyChip("Symmetric",     state.isSymmetric()),
                buildPropertyChip("Antisymmetric", state.isAntisymmetric()),
                buildPropertyChip("Transitive",    state.isTransitive())
        );
        propsCard.getChildren().add(propsRow);

        // order classification
        VBox orderCard = buildCard("Order Classification");
        String orderDesc = DMATEngine.classifyOrder(
                state.isReflexive(), state.isSymmetric(),
                state.isAntisymmetric(), state.isTransitive());
        Label orderLabel = new Label(orderDesc);
        orderLabel.setStyle("-fx-font-size:14px; -fx-text-fill:#aaddff; -fx-font-family:'Segoe UI';");
        orderLabel.setWrapText(true);
        orderCard.getChildren().add(orderLabel);

        // stats
        VBox statsCard = buildCard("Statistics");
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
                buildStatBox("Elements",  String.valueOf(state.getElements().size())),
                buildStatBox("Relations", String.valueOf(rels.size())),
                buildStatBox("Self-loops", String.valueOf(rels.stream().filter(r -> r[0].equals(r[1])).count()))
        );
        statsCard.getChildren().add(statsRow);

        // buttons
        HBox btnRow = new HBox(16);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setPrefHeight(44);
        backBtn.setOnAction(e -> ScreenNavigator.navigateTo(new InputScreen(state).getRoot()));

        Button nextBtn = new Button("Show Reduction  →");
        nextBtn.getStyleClass().add("btn-primary");
        nextBtn.setPrefHeight(44);
        nextBtn.setOnAction(e -> {
            // run transitive reduction through DMATEngine
            List<String> steps = new ArrayList<>();
            List<String[]> reduced = DMATEngine.computeTransitiveReduction(state.getRelations(), steps);
            state.setReductionSteps(steps);
            state.setReducedRelations(new java.util.LinkedHashSet<>(reduced));

            // run full poset analysis right here using DMATEngine
            List<String> elems = state.getElements();
            java.util.Map<String, Set<String>> above = DMATEngine.computeAbove(elems, reduced);
            java.util.Map<String, Set<String>> below = DMATEngine.computeBelow(elems, reduced);
            state.setAbove(above);
            state.setBelow(below);
            state.setMinimalElements(DMATEngine.findMinimalElements(elems, reduced));
            state.setMaximalElements(DMATEngine.findMaximalElements(elems, reduced));
            state.setLeastElement(DMATEngine.findLeastElement(elems, above));
            state.setGreatestElement(DMATEngine.findGreatestElement(elems, below));
            state.setLattice(DMATEngine.isLattice(elems, above, below));
            state.setMeetTable(DMATEngine.buildMeetTable(elems, below, above));
            state.setJoinTable(DMATEngine.buildJoinTable(elems, above, below));

            ScreenNavigator.navigateTo(new ReductionScreen(state).getRoot());
        });

        btnRow.getChildren().addAll(backBtn, nextBtn);

        center.getChildren().addAll(heading, sub, relCard, propsCard, orderCard, statsCard, btnRow);

        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getChildren().add(center);
        scroll.setContent(wrapper);
        root.setCenter(scroll);

        animateIn(relCard,   0);
        animateIn(propsCard, 100);
        animateIn(orderCard, 200);
        animateIn(statsCard, 300);
    }

    private VBox buildTopBar() {
        VBox topBox = new VBox(0);
        topBox.setStyle("-fx-background-color:#12121f; -fx-border-color:#1e1e3a; -fx-border-width:0 0 1 0;");
        HBox appBar = new HBox();
        appBar.setPadding(new Insets(14, 24, 14, 24));
        Label appName = new Label("⬡  Hasse Diagram Visualizer");
        appName.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00e5ff; -fx-font-family:'Segoe UI';");
        appBar.getChildren().add(appName);
        topBox.getChildren().addAll(appBar, new StepProgressBar(1));
        return topBox;
    }

    private VBox buildCard(String title) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color:#181828; -fx-background-radius:12; " +
                "-fx-border-color:#2a2a45; -fx-border-radius:12; -fx-border-width:1;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#8888bb; -fx-font-family:'Segoe UI';");
        card.getChildren().add(titleLbl);
        javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 0, 100, 0);
        line.setStroke(Color.web("#2a2a45"));
        card.getChildren().add(line);
        return card;
    }

    private HBox buildPropertyChip(String name, boolean positive) {
        HBox chip = new HBox(8);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(8, 16, 8, 12));
        chip.setStyle("-fx-background-color:" + (positive ? "#1a3a2a" : "#3a1a24") +
                "; -fx-background-radius:20; -fx-border-color:" +
                (positive ? "#00cc66" : "#ff4466") + "; -fx-border-radius:20; -fx-border-width:1;");
        Circle dot = new Circle(5);
        dot.setFill(Color.web(positive ? "#00cc66" : "#ff4466"));
        Label lbl = new Label((positive ? "✔ " : "✘ ") + name);
        lbl.setStyle("-fx-text-fill:" + (positive ? "#00cc66" : "#ff4466") + "; -fx-font-size:13px; -fx-font-family:'Segoe UI';");
        chip.getChildren().addAll(dot, lbl);
        return chip;
    }

    private VBox buildStatBox(String label, String value) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(14, 24, 14, 24));
        box.setStyle("-fx-background-color:#1e1e35; -fx-background-radius:10; -fx-border-color:#2e2e50; -fx-border-radius:10; -fx-border-width:1;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size:24px; -fx-font-weight:bold; -fx-text-fill:#00e5ff;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#666688;");
        box.getChildren().addAll(val, lbl);
        return box;
    }

    private void animateIn(javafx.scene.Node node, int delayMs) {
        node.setOpacity(0);
        node.setTranslateY(20);
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
