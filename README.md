# 📊 Hasse Diagram Visualizer

### *Making Discrete Mathematics actually make sense.*

A desktop app that takes your set and relation, analyzes its properties, and draws you a clean Hasse Diagram — all in a few clicks.


- **Custom Input** — Enter your own set and define any relation type; the app handles the rest
- **Relation Property Analysis** — Automatically checks if your relation is reflexive, symmetric, antisymmetric, and transitive
- **Hasse Diagram Generation** — Applies transitive reduction and draws a clean, simplified diagram
- **Poset Insights** — Displays minimal and maximal elements of the partially ordered set
- **Lattice Detection** — Checks if the poset forms a lattice, and finds the meet and join if they exist
- **Step-by-Step Flow** — Walks you through each stage with a visual progress bar so nothing feels like a black box

---
## 📸 Screenshots
![screen1](screenshots/screen1.png)

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java |
| UI Framework | JavaFX |
| Algorithms | Discrete Mathematics (Transitive Reduction, Poset Analysis) |
| Build Tool | Maven (`pom.xml`) |

---

## ⚙️ How It Works

Here's the full pipeline from your input to the final diagram:

```
1. You enter a set and choose a relation type
        ↓
2. The app generates all relation pairs
        ↓
3. Relation properties are analyzed (reflexive, symmetric, antisymmetric, transitive)
        ↓
4. Transitive reduction is applied to simplify the graph
        ↓
5. Hasse Diagram is rendered visually
        ↓
6. Minimal/maximal elements are computed + lattice check is performed
```

The core logic lives in `RelationEngine.java`, which handles all the math. The UI is split across five clean screens, each focused on one step of the process.

---

## 📂 Project Structure

```
HasseDiagramVisualizer/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java
        │   └── com/hasse/
        │       ├── MainApp.java                     ← Entry point
        │       ├── model/
        │       │   └── AppState.java                ← Shared state across screens
        │       ├── utils/
        │       │   ├── RelationEngine.java           ← Core relation logic & reduction
        │       │   └── ScreenNavigator.java          ← Handles screen transitions
        │       ├── components/
        │       │   └── StepProgressBar.java          ← Reusable step indicator UI
        │       └── screens/
        │           ├── SplashScreen.java             ← Screen 1: Animated intro
        │           ├── InputScreen.java              ← Screen 2: User input
        │           ├── RelationAnalysisScreen.java   ← Screen 3: Property results
        │           ├── ReductionScreen.java          ← Screen 4: Transitive reduction
        │           └── HasseDiagramScreen.java       ← Screen 5: Final diagram
        └── resources/
            └── com/hasse/
                └── styles.css                        ← Global dark theme styles
```

---

## ▶️ Installation & Running

### Prerequisites

- **Java 17+** (or whichever version you're targeting)
- **JavaFX SDK** — [Download here](https://gluonhq.com/products/javafx/)
- **Maven** (optional but recommended)

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/yourusername/HasseDiagramVisualizer.git
cd HasseDiagramVisualizer
```

**2. Set up JavaFX**

Download the JavaFX SDK and note the path to its `lib` folder. You'll need it below.

**3. Compile**
```bash
javac --module-path /path/to/javafx-sdk/lib \
      --add-modules javafx.controls,javafx.fxml \
      -d out \
      src/main/java/com/hasse/*.java
```

**4. Run**
```bash
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -cp out com.hasse.MainApp
```

> 💡 **Using Maven?** Just run `mvn javafx:run` if the JavaFX plugin is configured in `pom.xml`.

---

## 💡 Use Cases

- **Students** learning Posets, Lattices, and Hasse Diagrams in a Discrete Math course
- **Teachers** who want a quick visual aid for demonstrating relation properties in class
- **Anyone** who needs to verify or visualize a relation without doing it all on paper

---

## 🔮 Future Improvements

A few things we'd love to add someday:

- [ ] Export diagram as PNG or PDF
- [ ] Support for larger sets with auto-scaling layout
- [ ] Interactive diagram (click nodes to explore)
- [ ] Dark/light theme toggle
- [ ] Web version so no Java setup is needed

---

## 🤝 Contributing

Contributions are super welcome! Whether it's a bug fix, a new feature, or just improving the UI — feel free to open a PR.

## 🌟 Show Your Support

If this project helped you understand Hasse Diagrams even a little bit better, consider giving it a ⭐ on GitHub — it genuinely means a lot to a team of students who built this from scratch!

## 🙌 Final Note

Discrete Math can feel abstract and confusing, especially when you're staring at a page full of relation pairs trying to figure out what the Hasse Diagram looks like. We built this tool because we needed it ourselves — and we hope it makes things a little clearer for you too.

If you run into any bugs or have suggestions, don't hesitate to open an issue. We'd love to hear from you.


If you'd like to discuss ideas first or need help getting started, **feel free to DM me** — happy to collaborate anytime.
