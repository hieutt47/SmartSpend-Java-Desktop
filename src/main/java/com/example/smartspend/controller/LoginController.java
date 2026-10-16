package com.example.smartspend.controller;

import com.example.smartspend.dao.UserDAO;
import com.example.smartspend.service.EmailService;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.prefs.Preferences;
import java.util.Optional;
import java.util.regex.Pattern;

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
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    public static String getLoggedInUserEmail() { return loggedInUserEmail; }

    @FXML
    public void initialize() {
        passwordHidden.textProperty().bindBidirectional(passwordText.textProperty());

        prefs = Preferences.userNodeForPackage(LoginController.class);

        String savedEmail = prefs.get("email", "");
        if (!savedEmail.isEmpty()) {
            emailField.setText(savedEmail);
            keepLoggedInCheckBox.setSelected(true);
        }
    }

    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordHidden.setVisible(false);
            passwordText.setVisible(true);
            eyeIcon.setText("Hide");
        } else {
            passwordHidden.setVisible(true);
            passwordText.setVisible(false);
            eyeIcon.setText("Show");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText() == null ? "" : emailField.getText().trim().toLowerCase();
        String password = passwordHidden.getText() == null ? "" : passwordHidden.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            signInButton.setText("Vui lòng điền đầy đủ");
            signInButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
            return;
        }

        UserDAO userDAO = new UserDAO();
        int loggedInUserId = userDAO.validateLogin(email, password);

        if (loggedInUserId > 0) {
            if (keepLoggedInCheckBox.isSelected()) {
                prefs.put("email", email);
            } else {
                prefs.remove("email");
            }
            new EmailService().sendLoginNotice(email);
            openMainLayout(event, loggedInUserId, email);
        } else {
            // Sai email hoặc pass
            signInButton.setText("Sai Email hoặc Mật khẩu");
            signInButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(emailField.getText() == null ? "" : emailField.getText().trim());
        dialog.setTitle("Gmail quick access");
        dialog.setHeaderText("Gmail quick access (local)");
        dialog.setContentText("Nhập Gmail để tiếp tục. Khi cấu hình OAuth Client ID thật, nút này có thể nối với Google OAuth.");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String email = result.get().trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Gmail quick access", "Email không hợp lệ. Vui lòng nhập dạng name@example.com");
            return;
        }

        String name = deriveNameFromEmail(email);
        UserDAO userDAO = new UserDAO();
        int userId = userDAO.getOrCreateExternalUser(name, email, "GOOGLE");
        if (userId <= 0) {
            showError("Gmail quick access", "Không thể tạo hoặc mở tài khoản Gmail local.");
            return;
        }

        prefs.put("email", email);
        keepLoggedInCheckBox.setSelected(true);
        new EmailService().sendLoginNotice(email);
        openMainLayout(event, userId, email);
    }

    private void openMainLayout(ActionEvent event, int userId, String email) {
        com.example.smartspend.utils.SessionManager.setCurrentUserId(userId);
        loggedInUserEmail = email;
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
            showError("Navigation", "Lỗi chuyển trang: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String deriveNameFromEmail(String email) {
        String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        if (local.isBlank()) return "Google User";
        return Character.toUpperCase(local.charAt(0)) + local.substring(1).replace('.', ' ');
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleForgotAccess(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/ForgotPasswordView.fxml"));
            Parent root = loader.load();
            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Forgot Access");
            alert.setHeaderText("Không mở được màn hình quên mật khẩu");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
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