package com.hasse.screens;

import com.hasse.components.PosetAnalyzerPanel;
import com.hasse.components.StepProgressBar;
import com.hasse.model.AppState;
import com.hasse.utils.PosetAnalyzer;
import com.hasse.utils.ScreenNavigator;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

public class HasseDiagramScreen {

    private BorderPane root;
    private AppState state;
    private Canvas canvas;
    private double canvasW = 760;
    private double canvasH = 500;

    private List<String> elements;
    private Map<String, double[]> nodePositions = new LinkedHashMap<>();
    private Map<String, Integer> levels = new HashMap<>();
    private List<String[]> edges;
    private List<String> animatedNodes = new ArrayList<>();
    private List<String[]> animatedEdges = new ArrayList<>();
    private boolean animationComplete = false;
    private String hoveredNode = null;
    private PosetAnalyzer posetAnalyzer;

    public HasseDiagramScreen(AppState state) {
        this.state = state;
        this.elements = state.getElements();
        this.edges = new ArrayList<>(state.getReducedRelations());
        computeLevels();
        computePositions();
        this.posetAnalyzer = new PosetAnalyzer(elements, edges);
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0d1117;");
        buildUI();
    }

    private void computeLevels() {
        Map<String, Set<String>> successors = new HashMap<>();
        Map<String, Set<String>> predecessors = new HashMap<>();
        for (String e : elements) {
            successors.put(e, new HashSet<>());
            predecessors.put(e, new HashSet<>());
        }
        for (String[] edge : edges) {
            successors.get(edge[0]).add(edge[1]);
            predecessors.get(edge[1]).add(edge[0]);
        }
        Queue<String> queue = new LinkedList<>();
        for (String e : elements) {
            if (predecessors.get(e).isEmpty()) { levels.put(e, 0); queue.add(e); }
        }
        while (!queue.isEmpty()) {
            String curr = queue.poll();
            for (String succ : successors.get(curr)) {
                int newLevel = levels.get(curr) + 1;
                if (!levels.containsKey(succ) || levels.get(succ) < newLevel) {
                    levels.put(succ, newLevel); queue.add(succ);
                }
            }
        }
        for (String e : elements) if (!levels.containsKey(e)) levels.put(e, 0);
    }

    private void computePositions() {
        Map<Integer, List<String>> byLevel = new TreeMap<>();
        for (String e : elements) byLevel.computeIfAbsent(levels.get(e), k -> new ArrayList<>()).add(e);
        int maxLevel = byLevel.keySet().stream().mapToInt(i -> i).max().orElse(0);
        double levelHeight = canvasH / (maxLevel + 2);
        for (Map.Entry<Integer, List<String>> entry : byLevel.entrySet()) {
            List<String> nodes = entry.getValue();
            double y = canvasH - levelHeight * (entry.getKey() + 1);
            double xSpacing = canvasW / (nodes.size() + 1);
            for (int i = 0; i < nodes.size(); i++)
                nodePositions.put(nodes.get(i), new double[]{xSpacing * (i + 1), y});
        }
    }

    private void buildUI() {
        root.setTop(buildTopBar());

        StackPane canvasContainer = new StackPane();
        canvasContainer.setStyle("-fx-background-color:#13131f; -fx-border-color:#1e1e3a; -fx-border-width:0 1 0 0;");
        canvasContainer.setPadding(new Insets(20));
        canvas = new Canvas(canvasW, canvasH);
        canvasContainer.getChildren().add(canvas);
        StackPane.setAlignment(canvas, Pos.CENTER);

        canvas.setOnMouseMoved(e -> {
            String prev = hoveredNode;
            hoveredNode = null;
            for (Map.Entry<String, double[]> entry : nodePositions.entrySet()) {
                double[] pos = entry.getValue();
                if (Math.sqrt(Math.pow(e.getX()-pos[0],2)+Math.pow(e.getY()-pos[1],2)) <= 22) {
                    hoveredNode = entry.getKey(); break;
                }
            }
            if (!Objects.equals(prev, hoveredNode) && animationComplete) drawDiagram(1.0);
        });

        ScrollPane sideScroll = new ScrollPane();
        sideScroll.setFitToWidth(true);
        sideScroll.setStyle("-fx-background:transparent; -fx-background-color:#111120; -fx-border-color:transparent;");

        VBox sidePanel = new VBox(16);
        sidePanel.setPadding(new Insets(24, 20, 24, 20));
        sidePanel.setPrefWidth(310);
        sidePanel.setStyle("-fx-background-color:#111120;");

        Label panelTitle = new Label("Hasse Diagram");
        panelTitle.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:white; -fx-font-family:'Segoe UI';");
        Label panelSub = new Label("Final visualization after transitive reduction");
        panelSub.setStyle("-fx-font-size:12px; -fx-text-fill:#555577; -fx-font-family:'Segoe UI';");
        panelSub.setWrapText(true);

        // Legend
        VBox legendCard = new VBox(10);
        legendCard.setPadding(new Insets(16));
        legendCard.setStyle("-fx-background-color:#181828; -fx-background-radius:10; -fx-border-color:#2a2a45; -fx-border-radius:10; -fx-border-width:1;");
        Label legendTitle = new Label("Legend");
        legendTitle.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#8888bb;");
        HBox nodeItem = new HBox(10); nodeItem.setAlignment(Pos.CENTER_LEFT);
        Circle nodeDot = new Circle(10); nodeDot.setFill(Color.web("#0d1117")); nodeDot.setStroke(Color.web("#00e5ff")); nodeDot.setStrokeWidth(2);
        Label nodeItemLbl = new Label("Node = Element"); nodeItemLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#aaaacc;");
        nodeItem.getChildren().addAll(nodeDot, nodeItemLbl);
        HBox edgeItem = new HBox(10); edgeItem.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Line edgeIcon = new javafx.scene.shape.Line(0, 0, 20, 0);
        edgeIcon.setStroke(Color.web("#00e5ff", 0.7)); edgeIcon.setStrokeWidth(2);
        Label edgeItemLbl = new Label("Edge = Cover Relation"); edgeItemLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#aaaacc;");
        edgeItem.getChildren().addAll(edgeIcon, edgeItemLbl);
        legendCard.getChildren().addAll(legendTitle, nodeItem, edgeItem);

        // Elements card
        VBox elemCard = new VBox(8);
        elemCard.setPadding(new Insets(16));
        elemCard.setStyle("-fx-background-color:#181828; -fx-background-radius:10; -fx-border-color:#2a2a45; -fx-border-radius:10; -fx-border-width:1;");
        Label elemTitle = new Label("Elements (" + elements.size() + ")");
        elemTitle.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#8888bb;");
        elemCard.getChildren().add(elemTitle);
        FlowPane elemFlow = new FlowPane(6, 6);
        for (String el : elements) {
            boolean isMin = posetAnalyzer.getMinimalElements().contains(el);
            boolean isMax = posetAnalyzer.getMaximalElements().contains(el);
            String bg = (isMin && isMax) ? "#2a3a1a" : isMin ? "#1a2a1a" : isMax ? "#2a2010" : "#1e2a3a";
            String fg = (isMin && isMax) ? "#90ee90" : isMin ? "#7ecf7e" : isMax ? "#ffcc66" : "#7ecfdf";
            Label chip = new Label(el);
            chip.setPadding(new Insets(4, 10, 4, 10));
            chip.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:12; -fx-text-fill:" + fg + "; -fx-font-size:12px; -fx-font-family:'Consolas',monospace;");
            String role = (isMin && isMax) ? "Minimal & Maximal" : isMin ? "Minimal element" : isMax ? "Maximal element" : "Element";
            Tooltip tip = new Tooltip(role);
            tip.setStyle("-fx-background-color:#1e1e3a; -fx-text-fill:#ccccff; -fx-font-size:11px;");
            Tooltip.install(chip, tip);
            elemFlow.getChildren().add(chip);
        }
        elemCard.getChildren().add(elemFlow);
        HBox chipLegend = new HBox(12); chipLegend.setAlignment(Pos.CENTER_LEFT); chipLegend.setPadding(new Insets(6,0,0,0));
        addLegendDot(chipLegend, "#7ecf7e", "Minimal");
        addLegendDot(chipLegend, "#ffcc66", "Maximal");
        elemCard.getChildren().add(chipLegend);

        // Poset Analyzer section
        Separator sep = new Separator(); sep.setStyle("-fx-background-color:#2a2a45;");
        Label analyzerLbl = new Label("Poset Analysis");
        analyzerLbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#00e5ff; -fx-font-family:'Segoe UI';");
        VBox analyzerHeader = new VBox(6, sep, analyzerLbl);
        PosetAnalyzerPanel analyzerPanel = new PosetAnalyzerPanel(posetAnalyzer);

        // Controls
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(8, 0, 0, 0));

        Button replayBtn = new Button("▶  Replay Animation");
        replayBtn.getStyleClass().add("btn-primary");
        replayBtn.setPrefWidth(270); replayBtn.setPrefHeight(40);
        replayBtn.setOnAction(e -> replayAnimation());

        Button resetBtn = new Button("↺  Reset");
        resetBtn.getStyleClass().add("btn-secondary");
        resetBtn.setPrefWidth(270); resetBtn.setPrefHeight(40);
        resetBtn.setOnAction(e -> {
            AppState fresh = new AppState();
            InputScreen screen = new InputScreen(fresh);
            ScreenNavigator.navigateTo(screen.getRoot());
        });

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("btn-ghost");
        backBtn.setPrefWidth(270); backBtn.setPrefHeight(36);
        backBtn.setOnAction(e -> {
            ReductionScreen screen = new ReductionScreen(state);
            ScreenNavigator.navigateTo(screen.getRoot());
        });

        controls.getChildren().addAll(replayBtn, resetBtn, backBtn);

        sidePanel.getChildren().addAll(panelTitle, panelSub, legendCard, elemCard, analyzerHeader, analyzerPanel.getRoot(), controls);
        sideScroll.setContent(sidePanel);

        BorderPane mainArea = new BorderPane();
        mainArea.setStyle("-fx-background-color:#0d1117;");
        mainArea.setCenter(canvasContainer);
        mainArea.setRight(sideScroll);
        root.setCenter(mainArea);

        PauseTransition startDelay = new PauseTransition(Duration.millis(400));
        startDelay.setOnFinished(e -> startAnimation());
        startDelay.play();
    }

    private void addLegendDot(HBox box, String color, String label) {
        Label dot = new Label("●"); dot.setStyle("-fx-font-size:10px; -fx-text-fill:" + color + ";");
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size:10px; -fx-text-fill:#555577;");
        HBox item = new HBox(4, dot, lbl); item.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(item);
    }

    private VBox buildTopBar() {
        VBox topBox = new VBox(0);
        topBox.setStyle("-fx-background-color:#12121f; -fx-border-color:#1e1e3a; -fx-border-width:0 0 1 0;");
        HBox appBar = new HBox();
        appBar.setPadding(new Insets(14, 24, 14, 24));
        Label appName = new Label("⬡  Hasse Diagram Visualizer");
        appName.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00e5ff; -fx-font-family:'Segoe UI';");
        appBar.getChildren().add(appName);
        StepProgressBar progress = new StepProgressBar(3);
        topBox.getChildren().addAll(appBar, progress);
        return topBox;
    }

    private void startAnimation() {
        animatedNodes.clear();
        animatedEdges.clear();
        animationComplete = false;
        drawDiagram(0);

        List<String> sorted = elements.stream()
                .sorted(Comparator.comparingInt(e -> levels.getOrDefault(e, 0)))
                .collect(Collectors.toList());

        // ── Node animation ──
        Timeline nodeTimeline = new Timeline();
        if (sorted.isEmpty()) {
            // No nodes — skip straight to done
            animationComplete = true;
            drawDiagram(1.0);
            return;
        }
        for (int i = 0; i < sorted.size(); i++) {
            final String el = sorted.get(i);
            nodeTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(200 + i * 200), e -> {
                    animatedNodes.add(el);
                    drawDiagram(1.0);
                })
            );
        }

        nodeTimeline.setOnFinished(e -> {
            // ── Edge animation ──
            if (edges.isEmpty()) {
                // No edges — mark done immediately
                animationComplete = true;
                drawDiagram(1.0);
                return;
            }
            Timeline edgeTimeline = new Timeline();
            for (int i = 0; i < edges.size(); i++) {
                final String[] edge = edges.get(i);
                edgeTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(200 + i * 220), ev -> {
                        animatedEdges.add(edge);
                        drawDiagram(1.0);
                    })
                );
            }
            edgeTimeline.setOnFinished(ev -> {
                animationComplete = true;
                drawDiagram(1.0);
            });
            edgeTimeline.play();
        });

        nodeTimeline.play();
    }

    private void replayAnimation() { startAnimation(); }

    private void drawDiagram(double alpha) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, canvasH);
        gc.setFill(Color.web("#ffffff", 0.03));
        for (int x = 20; x < canvasW; x += 30)
            for (int y = 20; y < canvasH; y += 30)
                gc.fillOval(x-1, y-1, 2, 2);
        for (String[] edge : animatedEdges) drawEdge(gc, edge[0], edge[1], false);
        for (String el : animatedNodes) drawNode(gc, el, el.equals(hoveredNode));
        if (hoveredNode != null && animationComplete) {
            drawTooltip(gc, hoveredNode);
            for (String[] edge : animatedEdges)
                if (edge[0].equals(hoveredNode) || edge[1].equals(hoveredNode))
                    drawEdge(gc, edge[0], edge[1], true);
        }
    }

    private void drawEdge(GraphicsContext gc, String from, String to, boolean highlight) {
        double[] p1 = nodePositions.get(from), p2 = nodePositions.get(to);
        if (p1 == null || p2 == null) return;
        double dx = p2[0]-p1[0], dy = p2[1]-p1[1], len = Math.sqrt(dx*dx+dy*dy);
        if (len == 0) return;
        double r = 20;
        double sx = p1[0]+(dx/len)*r, sy = p1[1]+(dy/len)*r, ex = p2[0]-(dx/len)*r, ey = p2[1]-(dy/len)*r;
        gc.setStroke(highlight ? Color.web("#00ffcc",0.9) : Color.web("#00e5ff",0.45));
        gc.setLineWidth(highlight ? 2.5 : 1.8);
        gc.strokeLine(sx, sy, ex, ey);
        double aLen=8, angle=Math.atan2(ey-sy,ex-sx), aAng=Math.toRadians(25);
        gc.setLineWidth(1.5);
        gc.strokeLine(ex,ey,ex-aLen*Math.cos(angle-aAng),ey-aLen*Math.sin(angle-aAng));
        gc.strokeLine(ex,ey,ex-aLen*Math.cos(angle+aAng),ey-aLen*Math.sin(angle+aAng));
    }

    private void drawNode(GraphicsContext gc, String el, boolean hovered) {
        double[] pos = nodePositions.get(el); if (pos == null) return;
        double x=pos[0], y=pos[1], r=hovered?22:20;
        boolean isMin=posetAnalyzer.getMinimalElements().contains(el), isMax=posetAnalyzer.getMaximalElements().contains(el);
        String ring=(isMin&&isMax)?"#90ee90":isMin?"#7ecf7e":isMax?"#ffcc66":"#00e5ff";
        gc.setFill(Color.web(ring,hovered?0.15:0.06)); gc.fillOval(x-r-8,y-r-8,(r+8)*2,(r+8)*2);
        gc.setFill(Color.web(ring,hovered?0.20:0.10)); gc.fillOval(x-r-3,y-r-3,(r+3)*2,(r+3)*2);
        gc.setFill(Color.web(hovered?"#0d2535":"#111825")); gc.fillOval(x-r,y-r,r*2,r*2);
        gc.setStroke(Color.web(hovered?"#00ffcc":ring)); gc.setLineWidth(hovered?2.5:2.0); gc.strokeOval(x-r,y-r,r*2,r*2);
        gc.setFill(Color.web(hovered?"#00ffcc":"#ffffff"));
        gc.setFont(javafx.scene.text.Font.font("Segoe UI",javafx.scene.text.FontWeight.BOLD,13));
        javafx.scene.text.Text t = new javafx.scene.text.Text(el);
        gc.fillText(el, x-t.getBoundsInLocal().getWidth()/2, y+5);
    }

    private void drawTooltip(GraphicsContext gc, String el) {
        double[] pos = nodePositions.get(el); if (pos == null) return;
        long out=edges.stream().filter(e->e[0].equals(el)).count(), in=edges.stream().filter(e->e[1].equals(el)).count();
        boolean isMin=posetAnalyzer.getMinimalElements().contains(el), isMax=posetAnalyzer.getMaximalElements().contains(el);
        String role=(isMin&&isMax)?"Minimal & Maximal":isMin?"Minimal":isMax?"Maximal":"Internal";
        String[] lines={"Element: "+el,"Role: "+role,"Level: "+levels.getOrDefault(el,0),"In: "+in+"  Out: "+out};
        double tw=175, th=90, tx=pos[0]+28, ty=pos[1]-50;
        if (tx+tw>canvasW) tx=pos[0]-tw-10; if (ty<0) ty=10;
        gc.setFill(Color.web("#1a1a35",0.95)); gc.fillRoundRect(tx,ty,tw,th,8,8);
        gc.setStroke(Color.web("#00e5ff",0.5)); gc.setLineWidth(1); gc.strokeRoundRect(tx,ty,tw,th,8,8);
        gc.setFont(javafx.scene.text.Font.font("Segoe UI",12));
        for (int i=0;i<lines.length;i++) {
            gc.setFill(i==0?Color.web("#00e5ff"):i==1?Color.web("#ffcc66"):Color.web("#aaaacc"));
            gc.fillText(lines[i],tx+10,ty+18+i*18);
        }
    }

    public Parent getRoot() { return root; }
}
