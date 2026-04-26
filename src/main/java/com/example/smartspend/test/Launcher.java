package com.example.smartspend.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
public class Launcher extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Đường dẫn chuẩn theo cấu trúc 3.0
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/auth/LoginView.fxml"));
//        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/layout/MainLayout.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("SmartSpend - The Fiscal Atelier");
        stage.setScene(scene);
        stage.show();
    }
}

