package com.example.smartspend.controller;

import com.example.smartspend.dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.prefs.Preferences; // Added to save local credentials

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordHidden;
    @FXML private TextField passwordText;
    @FXML private Label eyeIcon;
    @FXML private Button signInButton;
    @FXML private CheckBox keepLoggedInCheckBox; // Injected CheckBox

    private boolean isPasswordVisible = false;
    private Preferences prefs; // Preferences object

    private static String loggedInUserEmail = "";

    public static String getLoggedInUserEmail() { return loggedInUserEmail; }

    @FXML
    public void initialize() {
        passwordHidden.textProperty().bindBidirectional(passwordText.textProperty());

        prefs = Preferences.userNodeForPackage(LoginController.class);

        String savedEmail = prefs.get("email", "");
        String savedPassword = prefs.get("password", "");

        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            emailField.setText(savedEmail);
            passwordHidden.setText(savedPassword);
            keepLoggedInCheckBox.setSelected(true);
        }
    }

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

        UserDAO userDAO = new UserDAO();
        int loggedInUserId = userDAO.validateLogin(email, password);

        if (loggedInUserId > 0) {

            // --- SAVE OR CLEAR CREDENTIALS ---
            if (keepLoggedInCheckBox.isSelected()) {
                prefs.put("email", email);
                prefs.put("password", password);
            } else {
                prefs.remove("email");
                prefs.remove("password");
            }

            com.example.smartspend.utils.SessionManager.setCurrentUserId(loggedInUserId);
            loggedInUserEmail = email;

            System.out.println("Đăng nhập thành công với ID: " + loggedInUserId);

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
    private void handleForgotAccess(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Access");
        alert.setHeaderText("Password Reset Requested");
        alert.setContentText("Please contact the system administrator or check your registered email to reset your credentials.");
        alert.showAndWait();
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