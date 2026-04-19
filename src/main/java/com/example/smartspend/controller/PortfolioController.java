package com.example.smartspend.controller;

import com.example.smartspend.utils.CurrencyFormatter;
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

    // --- THÊM MỚI ---
    @FXML private javafx.scene.control.Label lblNetBalance;
    @FXML private javafx.scene.control.Label lblMonthlyIncome;
    @FXML private javafx.scene.control.Label lblMonthlyExpense;
    @FXML private javafx.scene.layout.VBox   vboxRecentTransactions;

    private final com.example.smartspend.service.TransactionService txService =
            new com.example.smartspend.service.TransactionService();
    private final com.example.smartspend.utils.CurrencyFormatter fmt =
            new com.example.smartspend.utils.CurrencyFormatter();

    @FXML
    private TableView<?> tblActivity;

    @FXML
    void initialize() {
        System.out.println("Giao diện Portfolio đã load thành công!");
        loadDashboardData();
    }

    // --- THÊM MỚI ---
    private void loadDashboardData() {
        try {
            java.util.List<com.example.smartspend.model.Transaction> all =
                    txService.getAllTransactions();

            double totalIncome  = all.stream()
                    .filter(t -> t.getType() == com.example.smartspend.model.enums.TransactionType.INCOME)
                    .mapToDouble(com.example.smartspend.model.Transaction::getAmount).sum();
            double totalExpense = all.stream()
                    .filter(t -> t.getType() == com.example.smartspend.model.enums.TransactionType.EXPENSE)
                    .mapToDouble(com.example.smartspend.model.Transaction::getAmount).sum();

            if (lblNetBalance   != null) lblNetBalance.setText(CurrencyFormatter.format(totalIncome - totalExpense));
            if (lblMonthlyIncome != null) lblMonthlyIncome.setText(CurrencyFormatter.format(totalIncome));
            if (lblMonthlyExpense!= null) lblMonthlyExpense.setText(CurrencyFormatter.format(totalExpense));

            // Recent 5 transactions
            if (vboxRecentTransactions != null) {
                vboxRecentTransactions.getChildren().clear();
                all.stream().limit(5).forEach(t -> {
                    boolean isIncome = t.getType() == com.example.smartspend.model.enums.TransactionType.INCOME;
                    javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(12);
                    row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    row.getStyleClass().add("transaction-item");
                    row.setPadding(new javafx.geometry.Insets(10));

                    javafx.scene.control.Label icon = new javafx.scene.control.Label(isIncome ? "💰" : "💸");
                    icon.getStyleClass().add("transaction-icon");

                    javafx.scene.layout.VBox info = new javafx.scene.layout.VBox(2);
                    javafx.scene.control.Label name = new javafx.scene.control.Label(
                            t.getNote() != null && !t.getNote().isEmpty() ? t.getNote() : "Giao dịch");
                    name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                    javafx.scene.control.Label date = new javafx.scene.control.Label(
                            t.getDate() != null ? t.getDate().toString() : "");
                    date.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                    info.getChildren().addAll(name, date);
                    javafx.scene.layout.HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

                    javafx.scene.control.Label amount = new javafx.scene.control.Label(
                            (isIncome ? "+" : "-") + CurrencyFormatter.format(t.getAmount()));
                    amount.setStyle(isIncome
                            ? "-fx-font-weight: bold; -fx-text-fill: #16a34a;"
                            : "-fx-font-weight: bold; -fx-text-fill: #dc2626;");

                    row.getChildren().addAll(icon, info, amount);
                    vboxRecentTransactions.getChildren().add(row);
                });
            }

            // Animate fade in
            javafx.animation.FadeTransition ft =
                    new javafx.animation.FadeTransition(javafx.util.Duration.millis(400),
                            vboxRecentTransactions != null ? vboxRecentTransactions : new javafx.scene.layout.VBox());
            ft.setFromValue(0); ft.setToValue(1); ft.play();

        } catch (Exception e) {
            System.err.println("Portfolio: không load được data — " + e.getMessage());
        }
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