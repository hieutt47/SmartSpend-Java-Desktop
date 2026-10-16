package com.example.smartspend.test;

import com.example.smartspend.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void init() {
        DatabaseConnection.initializeDatabase();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/auth/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setTitle("SmartSpend - Personal Finance Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
