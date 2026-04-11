package com.example.smartspend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class LoginController {

    @FXML private TextField emailField;

    @FXML private PasswordField passwordHidden;
    @FXML private TextField passwordText;
    @FXML private Label eyeIcon;

    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        passwordHidden.textProperty().bindBidirectional(passwordText.textProperty());
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
}