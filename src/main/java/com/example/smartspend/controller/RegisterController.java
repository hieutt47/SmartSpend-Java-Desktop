package com.example.smartspend.controller;

import com.example.smartspend.dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;

    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Label eyeIcon1;
    private boolean isPasswordVisible = false;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private Label eyeIcon2;
    private boolean isConfirmPasswordVisible = false;

    @FXML private CheckBox termsCheckbox;
    @FXML private Button btnRegister;

    @FXML
    public void initialize() {
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(confirmPasswordTextField.textProperty());
    }

    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        isPasswordVisible = !isPasswordVisible;
        passwordField.setVisible(!isPasswordVisible);
        passwordTextField.setVisible(isPasswordVisible);
        eyeIcon1.setText(isPasswordVisible ? "🙈" : "👁");
    }

    @FXML
    private void toggleConfirmPasswordVisibility(MouseEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        confirmPasswordField.setVisible(!isConfirmPasswordVisible);
        confirmPasswordTextField.setVisible(isConfirmPasswordVisible);
        eyeIcon2.setText(isConfirmPasswordVisible ? "🙈" : "👁");
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        switchToLogin(event);
    }

    private void switchToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/LoginView.fxml"));
            Parent loginRoot = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(loginRoot);
        } catch (IOException e) {
            System.out.println("Lỗi không thể tải được file LoginView.fxml");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ các trường!");
            return;
        }

        if (!termsCheckbox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Điều khoản", "Vui lòng đồng ý với các điều khoản!");
            return;
        }

        if (!pass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mật khẩu", "Mật khẩu xác nhận không khớp!");
            return;
        }

        // Gọi DAO để lưu vào MySQL
        UserDAO userDAO = new UserDAO();
        boolean isSuccess = userDAO.registerUser(name, email, pass);

        if (isSuccess) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký thành công! Vui lòng đăng nhập.");
            switchToLogin(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Thất bại", "Đăng ký thất bại. Email này có thể đã tồn tại!");
        }
    }

    // Hàm tiện ích để hiển thị hộp thoại thông báo
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}