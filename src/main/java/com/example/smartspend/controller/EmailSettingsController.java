package com.example.smartspend.controller;

import com.example.smartspend.service.EmailService;
import com.example.smartspend.service.SmtpSettingsService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EmailSettingsController {
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField fromField;
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private TextField testEmailField;
    @FXML private CheckBox sslCheckBox;
    @FXML private Label statusLabel;

    private final SmtpSettingsService settings = new SmtpSettingsService();
    private final EmailService emailService = new EmailService();

    @FXML
    private void initialize() {
        hostField.setText(settings.getHost().isBlank() ? "smtp.gmail.com" : settings.getHost());
        portField.setText(settings.getPort().isBlank() ? "465" : settings.getPort());
        fromField.setText(settings.getFrom());
        userField.setText(settings.getUser());
        passwordField.setText(settings.getPassword());
        sslCheckBox.setSelected(settings.isSsl());
        testEmailField.setText(settings.getFrom());
        refreshStatus();
    }

    @FXML
    private void handleSave() {
        if (saveSettings(true)) {
            show(Alert.AlertType.INFORMATION, "Đã lưu", "Cấu hình email đã được lưu trên máy này.");
        }
    }

    @FXML
    private void handleTestEmail() {
        if (!saveSettings(true)) return;
        String to = value(testEmailField);
        if (to.isBlank()) to = settings.getFrom();
        if (!to.contains("@") || !to.contains(".")) {
            show(Alert.AlertType.WARNING, "Email nhận test không hợp lệ", "Hãy nhập email nhận test đúng định dạng.");
            return;
        }
        boolean sent = emailService.sendTestEmail(to);
        if (sent) {
            show(Alert.AlertType.INFORMATION, "Gửi thành công", "Đã gửi email test tới: " + to);
            refreshStatus();
        } else {
            show(Alert.AlertType.ERROR, "Gửi thất bại", "Không gửi được email. Hãy kiểm tra Gmail App Password, mạng, host/port hoặc quyền SMTP.");
        }
    }


    private boolean saveSettings(boolean showValidationAlerts) {
        String host = value(hostField);
        String port = value(portField);
        String from = value(fromField);
        String user = value(userField);
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (host.isBlank() || port.isBlank() || from.isBlank() || user.isBlank()) {
            if (showValidationAlerts) show(Alert.AlertType.WARNING, "Thiếu thông tin", "Hãy nhập SMTP host, port, from email và username.");
            return false;
        }
        if (!from.contains("@") || !user.contains("@")) {
            if (showValidationAlerts) show(Alert.AlertType.WARNING, "Email SMTP không hợp lệ", "From email và username nên là địa chỉ email hợp lệ.");
            return false;
        }
        if (password.isBlank()) {
            if (showValidationAlerts) show(Alert.AlertType.WARNING, "Thiếu App Password", "Với Gmail, hãy tạo App Password rồi dán vào ô mật khẩu SMTP.");
            return false;
        }
        try {
            int numericPort = Integer.parseInt(port);
            if (numericPort <= 0 || numericPort > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            if (showValidationAlerts) show(Alert.AlertType.WARNING, "Sai port", "SMTP port phải là số hợp lệ, ví dụ Gmail SSL là 465.");
            return false;
        }

        settings.save(host, port, from, user, password, sslCheckBox.isSelected());
        refreshStatus();
        return true;
    }
    @FXML
    private void handleClose() {
        Stage stage = (Stage) hostField.getScene().getWindow();
        stage.close();
    }

    private void refreshStatus() {
        statusLabel.setText(settings.describeStatus());
        statusLabel.setStyle("-fx-text-fill: " + (settings.isConfigured() ? "#16a34a" : "#dc2626") + "; -fx-font-weight: 700;");
    }

    private String value(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
