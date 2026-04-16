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
        String email = emailField.getText();
        String password = passwordHidden.getText();

        System.out.println("Attempting login for: " + email);
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