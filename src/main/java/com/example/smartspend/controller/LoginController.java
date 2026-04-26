package com.example.smartspend.controller;

import com.example.smartspend.dao.UserDAO;
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

        if (email.isEmpty() || password.isEmpty()) {
            signInButton.setText("⚠ Vui lòng điền đầy đủ!");
            signInButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
            return;
        }

        // Gọi DAO để kiểm tra trong MySQL
        UserDAO userDAO = new UserDAO();
        boolean isValidUser = userDAO.validateLogin(email, password);

        if (isValidUser) {
            loggedInUserEmail = email; // Lưu state như bạn đã code
            System.out.println("Đăng nhập thành công với: " + email);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/MainLayout.fxml"));
                Parent root = loader.load();
                Scene currentScene = ((Node) event.getSource()).getScene();

                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(350), root);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();

                currentScene.setRoot(root);
            } catch (IOException e) {
                signInButton.setText("❌ Lỗi chuyển trang!");
                e.printStackTrace();
            }
        } else {
            // Sai email hoặc pass
            signInButton.setText("❌ Sai Email hoặc Mật khẩu!");
            signInButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
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