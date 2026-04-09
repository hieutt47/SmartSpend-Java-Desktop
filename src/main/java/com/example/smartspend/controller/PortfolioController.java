package com.example.smartspend.controller;

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
}