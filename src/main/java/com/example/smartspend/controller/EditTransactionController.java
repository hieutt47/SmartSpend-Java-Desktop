package com.example.smartspend.controller;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditTransactionController {

    @FXML private DatePicker dpDate;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtAmount;

    private TransactionService transactionService = new TransactionService();

    // Transaction đang được edit — được inject từ ngoài vào
    private Transaction currentTransaction;

    /**
     * Được gọi từ TransactionController sau khi load FXML.
     * Nhận vào transaction cần sửa và đổ dữ liệu lên form.
     */
    public void initData(Transaction transaction) {
        this.currentTransaction = transaction;

        // Đổ dữ liệu cũ lên các trường
        dpDate.setValue(transaction.getDate());
        txtDescription.setText(transaction.getNote());
        txtAmount.setText(String.valueOf(transaction.getAmount()));

        // Populate ComboBox danh mục (tạm thời hardcode như AddTransactionController)
        cbCategory.getItems().setAll("Dining & Food", "Shopping", "Entertainment", "Income", "Transport", "Other");
        cbCategory.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleUpdateTransaction() {
        try {
            // Parse dữ liệu từ form
            double amount = Double.parseDouble(txtAmount.getText().trim());
            String description = txtDescription.getText();

            if (dpDate.getValue() == null) {
                throw new IllegalArgumentException("Vui lòng chọn ngày giao dịch!");
            }

            // Cập nhật object currentTransaction với dữ liệu mới từ form
            currentTransaction.setAmount(amount);
            currentTransaction.setDate(dpDate.getValue());
            currentTransaction.setNote(description);
            // categoryId giữ nguyên từ currentTransaction (chưa có CategoryDAO thực tế)

            // Gọi Service để validate + lưu xuống DB
            transactionService.updateTransaction(currentTransaction);

            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Giao dịch đã được cập nhật!");

            // Đóng popup sau khi update thành công
            Stage stage = (Stage) txtAmount.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền phải là chữ số hợp lệ!");
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Cảnh báo", e.getMessage());
        }
    }
}