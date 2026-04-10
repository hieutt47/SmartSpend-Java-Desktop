module com.example.smartspend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;

    exports com.example.smartspend.test;
    opens com.example.smartspend.test to javafx.fxml;

    exports com.example.smartspend.controller;
    opens com.example.smartspend.controller to javafx.fxml;
}