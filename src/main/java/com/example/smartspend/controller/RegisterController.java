package com.example.smartspend.controller;

import com.example.smartspend.dao.UserDAO;
import com.example.smartspend.service.EmailService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.Optional;

public class RegisterController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

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
        eyeIcon1.setText(isPasswordVisible ? "Hide" : "Show");
    }

    @FXML
    private void toggleConfirmPasswordVisibility(MouseEvent event) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        confirmPasswordField.setVisible(!isConfirmPasswordVisible);
        confirmPasswordTextField.setVisible(isConfirmPasswordVisible);
        eyeIcon2.setText(isConfirmPasswordVisible ? "Hide" : "Show");
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
    private void handleGoogleLogin(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(emailField.getText() == null ? "" : emailField.getText().trim());
        dialog.setTitle("Google Registration");
        dialog.setHeaderText("Google Sign-In demo mode");
        dialog.setContentText("Nhập Gmail để tạo/mở tài khoản demo.");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String email = result.get().trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.WARNING, "Email không hợp lệ", "Vui lòng nhập email đúng định dạng.");
            return;
        }

        String name = email.contains("@") ? email.substring(0, email.indexOf('@')).replace('.', ' ') : "Google User";
        int userId = new UserDAO().getOrCreateExternalUser(name, email, "GOOGLE");
        if (userId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Google Registration", "Không thể tạo tài khoản Google demo.");
            return;
        }
        new EmailService().sendWelcomeEmail(email, name);
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo/mở tài khoản Google demo. Nếu SMTP đã cấu hình, SmartSpend cũng đã gửi email chào mừng.");
        switchToLogin(event);
    }

    @FXML
    private void handleAppleLogin(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Apple ID", "Apple ID đang ở trạng thái demo. Dùng Google hoặc email/password để tiếp tục.");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim().toLowerCase();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();
        String confirmPass = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ các trường!");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.WARNING, "Email không hợp lệ", "Vui lòng nhập email đúng định dạng, ví dụ: name@example.com");
            return;
        }

        if (pass.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mật khẩu yếu", "Mật khẩu nên có ít nhất 6 ký tự.");
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

        UserDAO userDAO = new UserDAO();

        if (userDAO.isEmailExists(email)) {
            showAlert(Alert.AlertType.WARNING, "Email đã tồn tại", "Email này đã được sử dụng. Vui lòng dùng email khác hoặc Đăng nhập!");
            return; // Dừng lại, không cho đăng ký tiếp
        }

        // 2. Nếu email chưa ai dùng thì tiến hành lưu vào DB
        boolean isSuccess = userDAO.registerUser(name, email, pass);

        if (isSuccess) {
            boolean sent = new EmailService().sendWelcomeEmail(email, name);
            String mailNote = sent
                    ? "\nSmartSpend đã gửi email chào mừng tới: " + email
                    : "\nEmail chào mừng chưa được gửi vì SMTP chưa cấu hình. Vào Email notifications để bật gửi mail thật.";
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký thành công! Vui lòng đăng nhập." + mailNote);
            switchToLogin(event); // Tự động chuyển về trang Login
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra sự cố máy chủ. Vui lòng thử lại sau.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}