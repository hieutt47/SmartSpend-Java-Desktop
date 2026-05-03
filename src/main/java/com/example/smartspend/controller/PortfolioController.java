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

    @FXML
    private Button btnDashboard;

    @FXML
    private Button bthHistory;

    @FXML
    private Button btnBudget;

    @FXML
    private Button btnInsights;

    @FXML
    private Button btnSetting;

    @FXML
    private Button btnAddTrans;

    @FXML private Button btnAddIncome;
    @FXML private Button btnAddExpense;
    @FXML private Button btnDetails;

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

    private void loadDashboardData() {
        try {
            if (lblLastUpdated != null) {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm a");
                lblLastUpdated.setText("Last updated: Today at " + java.time.LocalTime.now().format(dtf));
            }

            if (portfolioChart != null) {
                portfolioChart.getData().clear();

                javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
                series.setName("Asset Growth");

                series.getData().add(new javafx.scene.chart.XYChart.Data<>("Dec", 15000));
                series.getData().add(new javafx.scene.chart.XYChart.Data<>("Jan", 22000));
                series.getData().add(new javafx.scene.chart.XYChart.Data<>("Feb", 18500));
                series.getData().add(new javafx.scene.chart.XYChart.Data<>("Mar", 28000));
                series.getData().add(new javafx.scene.chart.XYChart.Data<>("Apr", 35000));
                series.getData().add(new javafx.scene.chart.XYChart.Data<>("May", 42000));

                portfolioChart.getData().add(series);

                for (javafx.scene.chart.XYChart.Series<String, Number> s : portfolioChart.getData()) {
                    for (javafx.scene.chart.XYChart.Data<String, Number> data : s.getData()) {
                        data.getNode().setStyle("-fx-bar-fill: #0d52c6;");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Portfolio: không load được data — " + e.getMessage());
        }
    }

    @FXML
    void openAddTransactionPopup() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/transaction/AddTransaction.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Thêm Giao Dịch - SmartSpend");
            stage.setScene(new Scene(root));

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi không mở được form Thêm giao dịch: " + e.getMessage());
        }
    }

    @FXML
    void handleAddIncome(javafx.event.ActionEvent event) {
        openAddTransactionPopup();
    }

    @FXML
    void handleAddExpense(javafx.event.ActionEvent event) {
        openAddTransactionPopup();
    }

    @FXML
    void handleDetails(javafx.event.ActionEvent event) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Financial Health Details");
        alert.setHeaderText("Phân tích Sức khỏe Tài chính");
        alert.setContentText("Tính năng phân tích chuyên sâu đang được phát triển. Sẽ sớm ra mắt!");
        alert.showAndWait();
    }

    @FXML private Button btnViewAll;
    @FXML private javafx.scene.control.Label lblLastUpdated;
    @FXML private javafx.scene.chart.BarChart<String, Number> portfolioChart;

    @FXML
    void handleViewAll(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.scene.Scene scene = source.getScene();

            javafx.scene.layout.BorderPane mainPane = (javafx.scene.layout.BorderPane) scene.getRoot();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/transaction/TransactionsView.fxml"));
            Parent transactionsView = loader.load();

            mainPane.setCenter(transactionsView);

            System.out.println("Đã điều hướng sang trang Transactions!");

        } catch (java.io.IOException e) {
            System.err.println("Lỗi chuyển trang: Không tìm thấy file TransactionsView.fxml");
            e.printStackTrace();
        }
    }
}