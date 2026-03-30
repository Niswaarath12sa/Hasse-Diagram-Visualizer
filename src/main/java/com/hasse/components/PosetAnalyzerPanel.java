package com.hasse.components;

import com.hasse.utils.PosetAnalyzer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class PosetAnalyzerPanel {

    private final PosetAnalyzer analyzer;
    private VBox root;

    public PosetAnalyzerPanel(PosetAnalyzer analyzer) {
        this.analyzer = analyzer;
        build();
    }

    private void build() {
        root = new VBox(20);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color: transparent;");
        root.getChildren().addAll(buildPropertiesCard(), buildMeetJoinTabs());
    }

    private VBox buildPropertiesCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:#181828; -fx-background-radius:14; " +
                "-fx-border-color:#2a2a55; -fx-border-radius:14; -fx-border-width:1;");

        Label title = new Label("⚙  Poset Properties");
        title.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00e5ff; -fx-font-family:'Segoe UI';");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#2a2a45;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);

        String minimal = "{" + String.join(", ", analyzer.getMinimalElements()) + "}";
        String maximal = "{" + String.join(", ", analyzer.getMaximalElements()) + "}";
        String least = analyzer.getLeastElement() != null ? analyzer.getLeastElement() : "none";
        String greatest = analyzer.getGreatestElement() != null ? analyzer.getGreatestElement() : "none";
        boolean lattice = analyzer.isLattice();

        addRow(grid, 0, "Minimal Elements:", minimal, "#a0d0ff");
        addRow(grid, 1, "Maximal Elements:", maximal, "#a0d0ff");
        addRow(grid, 2, "Least Element:", least, least.equals("none") ? "#ff7777" : "#90ee90");
        addRow(grid, 3, "Greatest Element:", greatest, greatest.equals("none") ? "#ff7777" : "#90ee90");

        HBox badge = new HBox(10);
        badge.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label("Is Lattice:");
        lbl.setStyle("-fx-font-size:13px; -fx-text-fill:#8888bb; -fx-font-family:'Segoe UI';");
        Label val = new Label(lattice ? "✓  Yes" : "✗  No");
        val.setPadding(new Insets(4, 14, 4, 14));
        val.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-background-radius:20; -fx-font-family:'Segoe UI'; " +
                (lattice ? "-fx-background-color:#0d3325; -fx-text-fill:#50fa7b; -fx-border-color:#50fa7b33; -fx-border-width:1; -fx-border-radius:20;"
                         : "-fx-background-color:#320d0d; -fx-text-fill:#ff6b6b; -fx-border-color:#ff6b6b33; -fx-border-width:1; -fx-border-radius:20;"));
        badge.getChildren().addAll(lbl, val);
        GridPane.setColumnSpan(badge, 2);
        grid.add(badge, 0, 4);

        card.getChildren().addAll(title, sep, grid);
        return card;
    }

    private void addRow(GridPane grid, int row, String labelText, String value, String color) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size:13px; -fx-text-fill:#8888bb; -fx-font-family:'Segoe UI';");
        lbl.setMinWidth(160);
        Label val = new Label(value);
        val.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-font-family:'Consolas',monospace; -fx-text-fill:" + color + ";");
        val.setWrapText(true);
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private TabPane buildMeetJoinTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color:#181828; -fx-tab-min-height:38px;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab meetTab = new Tab("  Meet Table  (∧)  ");
        meetTab.setContent(buildTable(analyzer.getMeetTable(), "∧"));

        Tab joinTab = new Tab("  Join Table  (∨)  ");
        joinTab.setContent(buildTable(analyzer.getJoinTable(), "∨"));

        tabPane.getTabs().addAll(meetTab, joinTab);
        tabPane.setMinHeight(260);
        return tabPane;
    }

    private ScrollPane buildTable(Map<String, Map<String, String>> table, String op) {
        List<String> elements = analyzer.getElements();
        int n = elements.size();

        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPadding(new Insets(16));
        grid.setStyle("-fx-background-color:#111122;");

        grid.add(cell(op, "#00e5ff", "#0d1330", true), 0, 0);
        for (int c = 0; c < n; c++) grid.add(cell(elements.get(c), "#00e5ff", "#131335", true), c + 1, 0);

        for (int r = 0; r < n; r++) {
            String a = elements.get(r);
            grid.add(cell(a, "#00e5ff", "#131335", true), 0, r + 1);
            Map<String, String> rowData = table.get(a);
            for (int c = 0; c < n; c++) {
                String b = elements.get(c);
                String val = rowData != null ? rowData.getOrDefault(b, "—") : "—";
                boolean diag = r == c;
                boolean invalid = val.equals("—");
                String bg = diag ? "#1a2540" : invalid ? "#2a0d0d" : "#181828";
                String fg = diag ? "#7ec8e3" : invalid ? "#ff6b6b" : "#e0e0ff";
                Label cl = cell(val, fg, bg, false);
                Tooltip tip = new Tooltip(a + " " + op + " " + b + " = " + val);
                tip.setStyle("-fx-background-color:#1e1e3a; -fx-text-fill:#ccccff; -fx-font-size:12px;");
                Tooltip.install(cl, tip);
                String bgF = bg, fgF = fg;
                cl.setOnMouseEntered(e -> cl.setStyle(cellStyle(fgF, "#2a2a60", diag)));
                cl.setOnMouseExited(e -> cl.setStyle(cellStyle(fgF, bgF, diag)));
                grid.add(cl, c + 1, r + 1);
            }
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(false);
        scroll.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-border-color:transparent;");
        return scroll;
    }

    private Label cell(String text, String fg, String bg, boolean bold) {
        Label l = new Label(text);
        l.setPrefWidth(52);
        l.setPrefHeight(36);
        l.setAlignment(Pos.CENTER);
        l.setStyle(cellStyle(fg, bg, false) + (bold ? " -fx-font-weight:bold;" : ""));
        return l;
    }

    private String cellStyle(String fg, String bg, boolean diag) {
        return "-fx-font-size:13px; -fx-font-family:'Consolas',monospace; -fx-text-fill:" + fg +
                "; -fx-background-color:" + bg + "; -fx-background-radius:6; -fx-border-color:" +
                (diag ? "#00e5ff44" : "#2a2a45") + "; -fx-border-radius:6; -fx-border-width:1; -fx-alignment:center;";
    }

    public VBox getRoot() { return root; }
}
