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

    @FXML
    private BorderPane mainPane;
    // --- THÊM MỚI: Tracking sidebar active state ---
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

    // --- CÁC HÀM CHUYỂN TRANG CHÍNH ---
    // --- THAY THẾ (additive override) ---
    @FXML public void showPortfolio()     { navigateTo("/auth/PortfolioView.fxml",        btnPortfolio); }
    @FXML public void showTransactions()  { navigateTo("/transaction/TransactionsView.fxml", btnTransactions); }
    @FXML public void showInsights()      { navigateTo("/layout/InsightsView.fxml",        btnInsights); }
    @FXML public void showBudgets()       { System.out.println("BudgetsView — teammate đang làm."); }

    @FXML
    public void showTaxCentral() {
        System.out.println("-> Bấm nút Tax Central (Tính năng này các bạn khác sẽ code sau)");
    }

    // --- HÀM MỞ POPUP ADD TRANSACTION ---


    // --- LÕI ĐỔI RUỘT (DYNAMIC ROUTING) ---
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
    // --- THÊM MỚI: Chuyển view với fade animation + active state ---
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

            // Update sidebar active state
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
            // 1. Tìm đường dẫn đến file FXML của cái Popup
            // Lưu ý: Đường dẫn này phải trùng khớp 100% với cây thư mục của sếp
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/transaction/AddTransaction.fxml"));
            Parent root = loader.load();

            // 2. Tạo một cái "Sân khấu" (Stage) mới cho Popup
            Stage stage = new Stage();
            stage.setTitle("Add New Transaction");
            stage.setScene(new Scene(root));

            // 3. Khóa màn hình chính lại (Phải xong Popup mới được bấm tiếp màn chính)
            stage.initModality(Modality.APPLICATION_MODAL);

            // 4. Bật đèn lên!
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
}