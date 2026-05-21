module com.example.smartspend {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.prefs;
    requires java.desktop;
    requires java.net.http;
    requires com.h2database;

    exports com.example.smartspend.test;
    opens com.example.smartspend.test to javafx.fxml;

    exports com.example.smartspend.controller;
    opens com.example.smartspend.controller to javafx.fxml;
    opens com.example.smartspend.model to javafx.base;
    opens com.example.smartspend.model.enums to javafx.base;
}
