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
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InputScreen {

    private BorderPane root;
    private AppState state;
    private TextField elementsField;
    private TextArea customRelField;
    private Label errorLabel;
    private ToggleGroup typeGroup;
    private VBox customRelBox;

    public InputScreen(AppState state) {
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

        VBox center = new VBox(28);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(40, 60, 40, 60));
        center.setMaxWidth(680);

        Label heading = new Label("Define Your Set");
        heading.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:white; -fx-font-family:'Segoe UI';");

        Label subHeading = new Label("Enter elements and choose a relation type to generate the Hasse diagram.");
        subHeading.setStyle("-fx-font-size:14px; -fx-text-fill:#7777aa; -fx-font-family:'Segoe UI';");
        subHeading.setWrapText(true);

        // elements input card
        VBox elemCard = buildCard();
        Label elemLabel = buildFieldLabel("Set Elements");
        Label elemHint = new Label("Enter comma-separated values  (e.g. 1,2,3,4 or A,B,C)");
        elemHint.setStyle("-fx-font-size:12px; -fx-text-fill:#555577;");
        elementsField = new TextField();
        elementsField.setPromptText("e.g.  1, 2, 3, 4, 6, 12");
        elementsField.getStyleClass().add("input-field");
        elementsField.setPrefHeight(44);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#ff5577;");
        errorLabel.setVisible(false);

        elemCard.getChildren().addAll(elemLabel, elemHint, elementsField, errorLabel);

        // relation type card
        VBox typeCard = buildCard();
        Label typeLabel = buildFieldLabel("Relation Type");

        typeGroup = new ToggleGroup();
        HBox radioRow = new HBox(20);
        radioRow.setAlignment(Pos.CENTER_LEFT);

        RadioButton divides  = buildRadio("÷  Divides", "divides");
        RadioButton lessThan = buildRadio("≤  Less Than / Equal", "lessthan");
        RadioButton custom   = buildRadio("✎  Custom", "custom");

        divides.setSelected(true);
        radioRow.getChildren().addAll(divides, lessThan, custom);

        customRelBox = new VBox(8);
        customRelBox.setVisible(false);
        customRelBox.setManaged(false);
        Label customLabel = buildFieldLabel("Custom Relations");
        Label customHint  = new Label("Format: (1,2),(2,3),(1,4)");
        customHint.setStyle("-fx-font-size:12px; -fx-text-fill:#555577;");
        customRelField = new TextArea();
        customRelField.setPromptText("e.g. (1,2),(2,4),(2,3)");
        customRelField.getStyleClass().add("input-field");
        customRelField.setPrefHeight(90);
        customRelField.setWrapText(true);
        customRelBox.getChildren().addAll(customLabel, customHint, customRelField);

        custom.selectedProperty().addListener((obs, ov, nv) -> {
            customRelBox.setVisible(nv);
            customRelBox.setManaged(nv);
        });

        typeCard.getChildren().addAll(typeLabel, radioRow, customRelBox);

        HBox buttonRow = new HBox(16);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button nextBtn = new Button("Next  →");
        nextBtn.getStyleClass().add("btn-primary");
        nextBtn.setPrefWidth(160);
        nextBtn.setPrefHeight(44);
        nextBtn.setOnAction(e -> handleNext());

        Button exampleBtn = new Button("Try Example");
        exampleBtn.getStyleClass().add("btn-secondary");
        exampleBtn.setPrefHeight(44);
        exampleBtn.setOnAction(e -> loadExample());

        buttonRow.getChildren().addAll(nextBtn, exampleBtn);

        center.getChildren().addAll(heading, subHeading, elemCard, typeCard, buttonRow);

        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getChildren().add(center);
        scroll.setContent(wrapper);
        root.setCenter(scroll);
    }

    private void handleNext() {
        String raw = elementsField.getText().trim();
        if (raw.isEmpty()) { showError("Enter elements separated by commas", true); return; }

        List<String> elements = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (elements.isEmpty()) { showError("Enter elements separated by commas", true); return; }

        boolean allNums  = elements.stream().allMatch(e -> e.matches("\\d+"));
        boolean allAlpha = elements.stream().allMatch(e -> e.matches("[A-Za-z]+"));

        if (!allNums && !allAlpha) { showError("Use only numbers or alphabets (not mixed)", true); return; }

        hideError();
        state.setElements(elements);

        String relType = (String) typeGroup.getSelectedToggle().getUserData();
        state.setRelationType(relType);

        Set<String[]> rels;

        if (relType.equals("custom")) {
            String customText = customRelField.getText().trim();
            if (customText.isEmpty()) { showError("Enter custom relations in format (1,2),(2,3)", false); return; }
            try {
                rels = DMATEngine.parseCustomRelation(customText);
            } catch (Exception ex) {
                showError("Invalid relation format", false);
                return;
            }
        } else if (relType.equals("divides")) {
            if (!allNums) { showError("Divides relation requires numeric elements", true); return; }
            rels = DMATEngine.buildDividesRelation(elements);
        } else {
            rels = DMATEngine.buildLessThanRelation(elements);
        }

        state.setRelations(rels);

        // check all 4 properties using DMATEngine
        state.setReflexive(DMATEngine.isReflexive(elements, rels));
        state.setSymmetric(DMATEngine.isSymmetric(rels));
        state.setAntisymmetric(DMATEngine.isAntisymmetric(rels));
        state.setTransitive(DMATEngine.isTransitive(rels));

        ScreenNavigator.navigateTo(new RelationAnalysisScreen(state).getRoot());
    }

    private void loadExample() {
        elementsField.setText("1, 2, 3, 4, 6, 12");
        typeGroup.getToggles().forEach(t -> {
            if ("divides".equals(t.getUserData())) typeGroup.selectToggle(t);
        });
        hideError();
        shake(elementsField, true);
    }

    private void showError(String msg, boolean shakeField) {
        errorLabel.setText("⚠  " + msg);
        errorLabel.setVisible(true);
        elementsField.setStyle("-fx-background-color:#1e1e2f; -fx-text-fill:white; " +
                "-fx-border-color:#ff4466; -fx-border-width:1.5; -fx-border-radius:8; " +
                "-fx-background-radius:8; -fx-font-size:14px; -fx-padding:0 12;");
        if (shakeField) shake(elementsField, false);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        elementsField.setStyle(null);
    }

    private void shake(javafx.scene.Node node, boolean gentle) {
        TranslateTransition t = new TranslateTransition(Duration.millis(60), node);
        t.setCycleCount(6);
        t.setAutoReverse(true);
        t.setByX(gentle ? 3 : 7);
        t.play();
    }

    private VBox buildTopBar() {
        VBox topBox = new VBox(0);
        topBox.setStyle("-fx-background-color:#12121f; -fx-border-color:#1e1e3a; -fx-border-width:0 0 1 0;");
        HBox appBar = new HBox();
        appBar.setPadding(new Insets(14, 24, 14, 24));
        appBar.setAlignment(Pos.CENTER_LEFT);
        Label appName = new Label("⬡  Hasse Diagram Visualizer");
        appName.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00e5ff; -fx-font-family:'Segoe UI';");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        appBar.getChildren().addAll(appName, spacer);
        topBox.getChildren().addAll(appBar, new StepProgressBar(0));
        return topBox;
    }

    private VBox buildCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color:#181828; -fx-background-radius:12; " +
                "-fx-border-color:#2a2a45; -fx-border-radius:12; -fx-border-width:1;");
        return card;
    }

    private Label buildFieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#ccccee; -fx-font-family:'Segoe UI';");
        return lbl;
    }

    private RadioButton buildRadio(String label, String userData) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(typeGroup);
        rb.setUserData(userData);
        rb.setStyle("-fx-text-fill:#aaaacc; -fx-font-size:13px; -fx-font-family:'Segoe UI';");
        return rb;
    }

    public Parent getRoot() { return root; }
}
