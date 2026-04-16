package com.example.smartspend.controller;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.AlertHelper; // Khai thác file AlertHelper có sẵn của team
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import java.time.LocalDate;

public class AddTransactionController {
    @FXML private TextField txtAmount;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtNote;

    private TransactionService transactionService = new TransactionService();

    @FXML void handleAddIncome() { processTransaction(TransactionType.INCOME); }
    @FXML void handleAddExpense() { processTransaction(TransactionType.EXPENSE); }

    

    private void processTransaction(TransactionType type) {
        try {
            double amount = Double.parseDouble(txtAmount.getText());
            LocalDate date = dpDate.getValue();
            if (date == null) throw new IllegalArgumentException("Vui lòng chọn ngày!");

            Transaction newTrans = new Transaction();
            newTrans.setAmount(amount);
            newTrans.setDate(date);
            newTrans.setNote(txtNote.getText());
            newTrans.setCategoryId(1); // Mặc định ID 1 cho dễ test
            newTrans.setType(type);

            transactionService.addTransaction(newTrans);

            // Dùng hàm có sẵn trong file AlertHelper của team
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Giao dịch đã được ghi nhận!");

            // Xóa form sau khi thêm
            txtAmount.clear();
            txtNote.clear();
            dpDate.setValue(null);

        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền phải là chữ số hợp lệ!");
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Cảnh báo", e.getMessage());
        }
    }
}