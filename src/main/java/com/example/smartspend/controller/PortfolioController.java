package com.example.smartspend.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class PortfolioController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    // Các ID đã được sửa lại khớp hoàn toàn với file FXML của bạn UI
    @FXML
    private Button btnDashboard;

    @FXML
    private Button bthHistory; // Bạn UI gõ nhầm 'bth' thay vì 'btn', thầy giữ nguyên để code không sập

    @FXML
    private Button btnBudget;

    @FXML
    private Button btnInsights;

    @FXML
    private Button btnSetting;

    @FXML
    private Button btnAddTrans;

    @FXML
    private TableView<?> tblActivity;

    @FXML
    void initialize() {
        // Sau này Service sẽ đổ dữ liệu vào đây
        System.out.println("Giao diện Portfolio đã load thành công!");
    }

    @FXML
    void openAddTransactionPopup() {
        try {
            // Gọi đường dẫn đến file FXML sếp vừa tạo ban nãy
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/transaction/AddTransaction.fxml"));
            Parent root = fxmlLoader.load();

            // Tạo một cửa sổ (Stage) mới để hiển thị nó lên
            Stage stage = new Stage();
            stage.setTitle("Thêm Giao Dịch - SmartSpend");
            stage.setScene(new Scene(root));

            // Dùng showAndWait() để người dùng phải xử lý xong form này mới bấm được vùng khác
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi không mở được form Thêm giao dịch: " + e.getMessage());
        }
    }
}