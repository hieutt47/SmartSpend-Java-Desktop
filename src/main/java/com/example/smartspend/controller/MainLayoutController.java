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

    @FXML
    public void initialize() {
        System.out.println("MainLayout khởi tạo thành công!");
        showPortfolio(); // Mặc định hiển thị Portfolio
    }

    // --- CÁC HÀM CHUYỂN TRANG CHÍNH ---
    @FXML
    public void showPortfolio() {
        System.out.println("-> Đang bốc file Portfolio...");
        loadView("/auth/PortfolioView.fxml");
    }

    @FXML
    public void showTransactions() {
        System.out.println("-> Đang bốc file Transactions...");
        loadView("/transaction/TransactionsView.fxml");
    }

    // --- CÁC HÀM DỰ PHÒNG (TRÁNH LỖI THIẾU HÀM) ---
    @FXML
    public void showInsights() {
        System.out.println("-> Bấm nút Insights (Tính năng này các bạn khác sẽ code sau)");
    }

    @FXML
    public void showBudgets() {
        System.out.println("-> Bấm nút Budgets (Tính năng này các bạn khác sẽ code sau)");
    }

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