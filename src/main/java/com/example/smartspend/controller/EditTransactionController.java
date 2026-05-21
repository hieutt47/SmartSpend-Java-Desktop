package com.example.smartspend.controller;

import com.example.smartspend.dao.CategoryDAOImpl;
import com.example.smartspend.model.Category;
import com.example.smartspend.model.Transaction;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class EditTransactionController {
    @FXML private DatePicker dpDate;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtAmount;

    private final TransactionService transactionService = new TransactionService();
    private final CategoryDAOImpl categoryDAO = new CategoryDAOImpl();
    private final Map<String, Category> categoryMap = new HashMap<>();
    private Transaction currentTransaction;

    public void initData(Transaction transaction) {
        this.currentTransaction = transaction;
        dpDate.setValue(transaction.getDate());
        txtDescription.setText(transaction.getNote());
        txtAmount.setText(String.valueOf(transaction.getAmount()));
        loadCategories();
    }

    private void loadCategories() {
        cbCategory.getItems().clear();
        categoryMap.clear();
        for (Category category : categoryDAO.getCategoriesByType(currentTransaction.getType())) {
            cbCategory.getItems().add(category.getName());
            categoryMap.put(category.getName(), category);
            if (category.getCategoryId() == currentTransaction.getCategoryId()) {
                cbCategory.setValue(category.getName());
            }
        }
        if (cbCategory.getValue() == null && !cbCategory.getItems().isEmpty()) cbCategory.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleUpdateTransaction() {
        try {
            double amount = Double.parseDouble(txtAmount.getText().trim().replace(",", ""));
            if (dpDate.getValue() == null) throw new IllegalArgumentException("Vui lòng chọn ngày giao dịch.");
            Category category = categoryMap.get(cbCategory.getValue());
            if (category == null) throw new IllegalArgumentException("Danh mục không hợp lệ.");

            currentTransaction.setAmount(amount);
            currentTransaction.setDate(dpDate.getValue());
            currentTransaction.setNote(txtDescription.getText() == null ? "" : txtDescription.getText().trim());
            currentTransaction.setCategoryId(category.getCategoryId());
            transactionService.updateTransaction(currentTransaction);

            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Giao dịch đã được cập nhật.");
            ((Stage) txtAmount.getScene().getWindow()).close();
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Sai số tiền", "Số tiền phải là số hợp lệ.");
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Không thể cập nhật", e.getMessage());
        }
    }
}
