package com.example.smartspend.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
    private void handleRegister(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (!termsCheckbox.isSelected()) {
            System.out.println("Please agree to the terms and conditions!");
            return;
        }

        if (!pass.equals(confirmPass)) {
            System.out.println("Passwords do not match!");
            return;
        }

        System.out.println("Registration successful for: " + name);

        switchToLogin(event);
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
}