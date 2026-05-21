package com.example.smartspend.controller;

import com.example.smartspend.dao.CategoryDAOImpl;
import com.example.smartspend.model.Category;
import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.AlertHelper;
import com.example.smartspend.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTransactionController {
    @FXML private TextField txtAmount;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbCategory;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField txtNote;

    private final TransactionService transactionService = new TransactionService();
    private final CategoryDAOImpl categoryDAO = new CategoryDAOImpl();
    private final Map<String, Category> categoryMap = new HashMap<>();

    @FXML
    public void initialize() {
        dpDate.setValue(LocalDate.now());
        if (cbType != null) {
            cbType.getItems().setAll("Expense", "Income");
            cbType.setValue("Expense");
            cbType.setOnAction(e -> loadCategories(currentType()));
        }
        loadCategories(currentType());
    }

    public void setDefaultType(TransactionType type) {
        if (type == null) return;
        if (cbType != null) {
            cbType.setValue(type == TransactionType.INCOME ? "Income" : "Expense");
        }
        loadCategories(type);
    }

    private TransactionType currentType() {
        if (cbType == null || cbType.getValue() == null) return TransactionType.EXPENSE;
        return "Income".equalsIgnoreCase(cbType.getValue()) ? TransactionType.INCOME : TransactionType.EXPENSE;
    }

    private void loadCategories(TransactionType type) {
        List<Category> categories = categoryDAO.getCategoriesByType(type);
        cbCategory.getItems().clear();
        categoryMap.clear();
        for (Category category : categories) {
            cbCategory.getItems().add(category.getName());
            categoryMap.put(category.getName(), category);
        }
        if (!cbCategory.getItems().isEmpty()) cbCategory.getSelectionModel().selectFirst();
    }

    @FXML private void handleAddIncome() {
        if (cbType != null) cbType.setValue("Income");
        loadCategories(TransactionType.INCOME);
        processTransaction(TransactionType.INCOME);
    }

    @FXML private void handleAddExpense() {
        if (cbType != null) cbType.setValue("Expense");
        loadCategories(TransactionType.EXPENSE);
        processTransaction(TransactionType.EXPENSE);
    }

    @FXML private void handleSaveTransaction() { processTransaction(currentType()); }

    private void processTransaction(TransactionType type) {
        try {
            if (categoryMap.isEmpty() || cbCategory.getValue() == null) loadCategories(type);
            String amountText = txtAmount.getText() == null ? "" : txtAmount.getText().trim().replace(",", "");
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) throw new IllegalArgumentException("Số tiền phải lớn hơn 0.");
            LocalDate date = dpDate.getValue();
            if (date == null) throw new IllegalArgumentException("Vui lòng chọn ngày.");

            int currentUserId = SessionManager.getCurrentUserId();
            if (currentUserId <= 0) throw new IllegalArgumentException("Phiên đăng nhập không hợp lệ. Hãy đăng nhập lại.");

            Category selectedCategory = categoryMap.get(cbCategory.getValue());
            if (selectedCategory == null) throw new IllegalArgumentException("Vui lòng chọn danh mục hợp lệ.");

            Transaction transaction = new Transaction();
            transaction.setUserId(currentUserId);
            transaction.setAmount(amount);
            transaction.setDate(date);
            transaction.setNote(txtNote.getText() == null ? "" : txtNote.getText().trim());
            transaction.setType(type);
            transaction.setCategoryId(selectedCategory.getCategoryId());
            transactionService.addTransaction(transaction);

            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm giao dịch.");
            Stage stage = (Stage) txtAmount.getScene().getWindow();
            stage.close();
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Sai số tiền", "Số tiền phải là số hợp lệ, ví dụ: 120000.");
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Không thể thêm giao dịch", e.getMessage());
        }
    }
}
