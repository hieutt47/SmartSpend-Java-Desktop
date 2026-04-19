package com.example.smartspend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;

    @FXML private PasswordField passwordHidden;
    @FXML private TextField passwordText;
    @FXML private Label eyeIcon;
    private boolean isPasswordVisible = false;

    @FXML private Button signInButton;

    @FXML
    public void initialize() {
        passwordHidden.textProperty().bindBidirectional(passwordText.textProperty());
    }
    // --- THÊM MỚI: Navigation state ---
    private static String loggedInUserEmail = "";

    public static String getLoggedInUserEmail() { return loggedInUserEmail; }

    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordHidden.setVisible(false);
            passwordText.setVisible(true);
            eyeIcon.setText("🙈");
        } else {
            passwordHidden.setVisible(true);
            passwordText.setVisible(false);
            eyeIcon.setText("👁");
        }
    }


    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordHidden.getText().trim();

        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            signInButton.setText("⚠ Vui lòng điền đầy đủ!");
            signInButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
            return;
        }

        // TODO: Kết nối UserDAO khi có bảng users
        // Tạm thời: accept any non-empty credentials
        loggedInUserEmail = email;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/MainLayout.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();

            // Fade transition khi chuyển trang
            javafx.animation.FadeTransition ft =
                    new javafx.animation.FadeTransition(javafx.util.Duration.millis(350), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            currentScene.setRoot(root);
        } catch (IOException e) {
            signInButton.setText("❌ Lỗi kết nối!");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/RegisterView.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);
        } catch (IOException e) {
            System.out.println("Error loading RegisterView.fxml");
            e.printStackTrace();
        }
    }
}