package com.example.smartspend.test;

import com.example.smartspend.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void init() {
        DatabaseConnection.initializeDatabase();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/layout/MainLayout.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 650);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setTitle("SmartSpend - Personal Finance Manager");
        stage.setScene(scene);
        stage.show();
    }
}
