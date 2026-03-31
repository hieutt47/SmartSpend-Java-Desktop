module com.example.smartspend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens com.example.smartspend to javafx.fxml;
    exports com.example.smartspend;
}