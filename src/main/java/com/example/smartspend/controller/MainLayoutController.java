package com.example.smartspend.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MainLayoutController {

    @FXML private BorderPane mainPane;
    @FXML private javafx.scene.control.Button btnPortfolio;
    @FXML private javafx.scene.control.Button btnTransactions;
    @FXML private javafx.scene.control.Button btnInsights;
    @FXML private javafx.scene.control.Button btnBudgets;
    @FXML private javafx.scene.control.Button btnCoach;

    private javafx.scene.control.Button currentActiveBtn = null;

    private static final String STYLE_ACTIVE =
            "-fx-background-color: linear-gradient(to right, #2563eb, #1d4ed8); -fx-text-fill: white; " +
                    "-fx-font-weight: 800; -fx-alignment: CENTER_LEFT; -fx-background-radius: 14; " +
                    "-fx-padding: 14 18 14 18; -fx-cursor: hand;";

    private static final String STYLE_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #cbd5e1; " +
                    "-fx-font-weight: 700; -fx-alignment: CENTER_LEFT; -fx-background-radius: 14; " +
                    "-fx-padding: 14 18 14 18; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        System.out.println("MainLayout khởi tạo thành công!");
        if (btnPortfolio != null) btnPortfolio.setStyle(STYLE_INACTIVE);
        if (btnTransactions != null) btnTransactions.setStyle(STYLE_INACTIVE);
        if (btnInsights != null) btnInsights.setStyle(STYLE_INACTIVE);
        if (btnBudgets != null) btnBudgets.setStyle(STYLE_INACTIVE);
        if (btnCoach != null) btnCoach.setStyle(STYLE_INACTIVE);
        showPortfolio();
    }

    @FXML public void showPortfolio()     { navigateTo("/auth/PortfolioView.fxml",        btnPortfolio); }
    @FXML public void showTransactions()  { navigateTo("/transaction/TransactionsView.fxml", btnTransactions); }
    @FXML public void showInsights()      { navigateTo("/layout/InsightsView.fxml",        btnInsights); }
    @FXML public void showBudgets()       { navigateTo("/layout/BudgetsView.fxml", btnBudgets); }
    @FXML public void showCoach()         { navigateTo("/layout/SmartCoachView.fxml", btnCoach); }

    @FXML
    public void showTaxCentral() {
        System.out.println("-> Bấm nút Tax Central (Tính năng này các bạn khác sẽ code sau)");
    }

    private void loadView(String fxmlPath) {
        try {
            URL xmlUrl = getClass().getResource(fxmlPath);
            if (xmlUrl == null) {
                System.err.println("LỖI CỰC NẶNG: Không tìm thấy file tại " + fxmlPath);
                return;
            }
            Parent view = FXMLLoader.load(xmlUrl);
            mainPane.setCenter(view);
        } catch (IOException e) {
            System.err.println("Lỗi khi nạp file FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath, javafx.scene.control.Button activeBtn) {
        try {
            java.net.URL xmlUrl = getClass().getResource(fxmlPath);
            if (xmlUrl == null) {
                System.err.println("Không tìm thấy: " + fxmlPath);
                return;
            }
            javafx.scene.Parent view = javafx.fxml.FXMLLoader.load(xmlUrl);

            // Fade out → swap → fade in
            if (mainPane.getCenter() != null) {
                javafx.animation.FadeTransition fadeOut =
                        new javafx.animation.FadeTransition(javafx.util.Duration.millis(120), mainPane.getCenter());
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    mainPane.setCenter(view);
                    javafx.animation.FadeTransition fadeIn =
                            new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), view);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                });
                fadeOut.play();
            } else {
                mainPane.setCenter(view);
            }

            if (currentActiveBtn != null) currentActiveBtn.setStyle(STYLE_INACTIVE);
            if (activeBtn != null) {
                activeBtn.setStyle(STYLE_ACTIVE);
                currentActiveBtn = activeBtn;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void openAddTransaction() {
        System.out.println("-> Đang mở cửa sổ Thêm giao dịch...");
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/transaction/AddTransaction.fxml"));
            Parent root = loader.load();
            AddTransactionController controller = loader.getController();
            if (controller != null) controller.setDefaultType(com.example.smartspend.model.enums.TransactionType.EXPENSE);

            Stage stage = new Stage();
            stage.setTitle("Add New Transaction");
            stage.setScene(new Scene(root));

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();
            if (currentActiveBtn == btnPortfolio) showPortfolio();
            else if (currentActiveBtn == btnTransactions) showTransactions();
            else if (currentActiveBtn == btnInsights) showInsights();
            else if (currentActiveBtn == btnBudgets) showBudgets();
            else if (currentActiveBtn == btnCoach) showCoach();
            System.out.println("=> Popup đã đóng!");

        } catch (IOException e) {
            System.err.println("LỖI: Không tìm thấy file AddTransaction.fxml tại /transaction/");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi mở Popup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleSignOut(javafx.event.ActionEvent event) {
        try {
            com.example.smartspend.utils.SessionManager.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth/LoginView.fxml"));
            Parent root = loader.load();

            // Lấy Scene hiện tại thay vì tạo Scene mới
            javafx.scene.Scene currentScene = ((javafx.scene.Node) event.getSource()).getScene();

            currentScene.setRoot(root);

            System.out.println("Đã đăng xuất thành công và giữ nguyên kích thước màn hình!");

        } catch (java.io.IOException e) {
            System.err.println("Lỗi chuyển trang Đăng xuất: Không tìm thấy file /auth/LoginView.fxml");
            e.printStackTrace();
        }
    }

    @FXML
    public void openEmailSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/EmailSettingsView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("SmartSpend Email Notifications");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Email Settings");
            alert.setHeaderText(null);
            alert.setContentText("Không mở được cấu hình email: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void handleSupport(javafx.event.ActionEvent event) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Support");
        alert.setHeaderText("SmartSpend Support Center");
        alert.setContentText("Host: Trần Trung Hiếu\nEmail: trantrunghieu30032006@gmail.com\n\n" +
                "SmartSpend is ready for demo, study, and portfolio presentation.\n\n" +
                "Open Email notifications in the sidebar to configure SMTP and send real email alerts.");
        alert.showAndWait();
    }
}
