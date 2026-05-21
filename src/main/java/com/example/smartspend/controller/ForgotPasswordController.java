package com.example.smartspend.controller;

import com.example.smartspend.dao.UserDAO;
import com.example.smartspend.service.EmailService;
import com.example.smartspend.utils.AlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button sendCodeButton;
    @FXML private Button resetButton;
    @FXML private Label statusLabel;

    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final SecureRandom RANDOM = new SecureRandom();

    @FXML
    private void initialize() {
        codeField.setDisable(true);
        newPasswordField.setDisable(true);
        confirmPasswordField.setDisable(true);
        resetButton.setDisable(true);
    }

    @FXML
    private void handleSendCode() {
        String email = normalize(emailField.getText());
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            setStatus("Email chưa đúng định dạng.", true);
            return;
        }
        if (!userDAO.isEmailExists(email)) {
            setStatus("Email này chưa tồn tại trong hệ thống.", true);
            return;
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        boolean saved = userDAO.savePasswordResetCode(email, code, LocalDateTime.now().plusMinutes(10));
        if (!saved) {
            setStatus("Không tạo được mã reset. Hãy thử lại.", true);
            return;
        }

        boolean sent = emailService.sendPasswordResetCode(email, code);
        codeField.setDisable(false);
        newPasswordField.setDisable(false);
        confirmPasswordField.setDisable(false);
        resetButton.setDisable(false);

        if (sent) {
            setStatus("Đã gửi mã reset về email. Mã có hiệu lực 10 phút.", false);
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Đã gửi email", "SmartSpend đã gửi mã reset tới: " + email + "\nHãy kiểm tra Inbox/Spam.");
        } else {
            setStatus("SMTP chưa cấu hình nên app đang dùng mã demo nội bộ.", false);
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Mã reset demo", "Muốn gửi email thật: vào Email notifications trong sidebar và cấu hình Gmail App Password.\n\nMã demo tạm thời: " + code);
        }
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String email = normalize(emailField.getText());
        String code = normalize(codeField.getText());
        String newPassword = newPasswordField.getText() == null ? "" : newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (code.length() != 6) {
            setStatus("Mã reset phải gồm 6 chữ số.", true);
            return;
        }
        if (newPassword.length() < 6) {
            setStatus("Mật khẩu mới cần ít nhất 6 ký tự.", true);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            setStatus("Mật khẩu xác nhận không khớp.", true);
            return;
        }

        boolean ok = userDAO.resetPasswordWithCode(email, code, newPassword);
        if (!ok) {
            setStatus("Mã reset sai hoặc đã hết hạn.", true);
            return;
        }
        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Mật khẩu đã được đổi. Hãy đăng nhập lại.");
        goToLogin(event);
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/auth/LoginView.fxml"));
            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);
        } catch (IOException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không mở được màn hình đăng nhập: " + e.getMessage());
        }
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (error ? "#dc2626" : "#16a34a") + "; -fx-font-weight: 700;");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
