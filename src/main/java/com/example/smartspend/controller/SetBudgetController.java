package com.example.smartspend.controller;

import com.example.smartspend.dao.CategoryDAOImpl;
import com.example.smartspend.model.Category;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.BudgetService;
import com.example.smartspend.utils.AlertHelper;
import com.example.smartspend.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class SetBudgetController {
    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtAmount;

    private final CategoryDAOImpl categoryDAO = new CategoryDAOImpl();
    private final BudgetService budgetService = new BudgetService();
    private final Map<String, Category> categories = new HashMap<>();

    @FXML
    private void initialize() {
        categoryDAO.getCategoriesByType(TransactionType.EXPENSE).forEach(category -> {
            categories.put(category.getName(), category);
            cbCategory.getItems().add(category.getName());
        });
        if (!cbCategory.getItems().isEmpty()) cbCategory.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSave() {
        try {
            Category category = categories.get(cbCategory.getValue());
            if (category == null) throw new IllegalArgumentException("Vui lòng chọn danh mục chi tiêu.");
            double amount = Double.parseDouble(txtAmount.getText().trim().replace(",", ""));
            YearMonth month = YearMonth.now();
            boolean success = budgetService.saveBudget(SessionManager.getCurrentUserId(), category.getCategoryId(), amount,
                    month.getMonthValue(), month.getYear());
            if (!success) throw new IllegalStateException("Không lưu được ngân sách.");
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Đã lưu", "Ngân sách tháng này đã được cập nhật.");
            ((Stage) txtAmount.getScene().getWindow()).close();
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Số tiền không hợp lệ", "Nhập số tiền hợp lệ, ví dụ: 2500000.");
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Không thể lưu ngân sách", e.getMessage());
        }
    }
}
