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

    private javafx.scene.control.Button currentActiveBtn = null;

    private static final String STYLE_ACTIVE =
            "-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; " +
                    "-fx-font-weight: bold; -fx-alignment: BASELINE_LEFT; -fx-background-radius: 8;";

    private static final String STYLE_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #64748b; " +
                    "-fx-font-weight: bold; -fx-alignment: BASELINE_LEFT; -fx-background-radius: 8;";

    @FXML
    public void initialize() {
        System.out.println("MainLayout khởi tạo thành công!");
        showPortfolio(); // Mặc định hiển thị Portfolio
        if (btnPortfolio != null) {
            btnPortfolio.setStyle(STYLE_ACTIVE);
            currentActiveBtn = btnPortfolio;
        }
    }

    @FXML public void showPortfolio()     { navigateTo("/auth/PortfolioView.fxml",        btnPortfolio); }
    @FXML public void showTransactions()  { navigateTo("/transaction/TransactionsView.fxml", btnTransactions); }
    @FXML public void showInsights()      { navigateTo("/layout/InsightsView.fxml",        btnInsights); }
    @FXML public void showBudgets()       { navigateTo("/layout/BudgetsView.fxml", btnBudgets); }

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

            Stage stage = new Stage();
            stage.setTitle("Add New Transaction");
            stage.setScene(new Scene(root));

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.show();
            System.out.println("=> Popup đã hiện hình!");

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
    void handleSupport(javafx.event.ActionEvent event) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Support");
        alert.setHeaderText("SmartSpend Support Center");
        alert.setContentText("Vui lòng liên hệ qua email: support@atelier.com để được hỗ trợ.");
        alert.showAndWait();
    }
}