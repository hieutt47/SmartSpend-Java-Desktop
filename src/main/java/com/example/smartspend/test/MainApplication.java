package com.example.smartspend.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Đường dẫn chuẩn theo cấu trúc 3.0
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/auth/PortfolioView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 600);
        stage.setTitle("SmartSpend - The Fiscal Atelier");
        stage.setScene(scene);
        stage.show();
    }
}