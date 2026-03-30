module com.hasse {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    opens com.hasse to javafx.fxml;
    opens com.hasse.screens to javafx.fxml;
    opens com.hasse.model to javafx.fxml;

    exports com.hasse;
    exports com.hasse.screens;
    exports com.hasse.model;
    exports com.hasse.utils;
    exports com.hasse.components;
}
