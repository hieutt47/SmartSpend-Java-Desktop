package com.example.smartspend.controller;

import com.example.smartspend.dao.CategoryDAOImpl;
import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AddTransactionController {
    @FXML private TextField txtAmount;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtNote;

    private TransactionService transactionService = new TransactionService();
    private final Map<String, Integer> categoryMap = new HashMap<>();
    private final CategoryDAOImpl categoryDAO = new CategoryDAOImpl();

    @FXML
    public void initialize() {
        // 1. Seed nếu bảng categories chưa có data
        categoryDAO.seedDefaultCategoriesIfEmpty();

        // 2. Load danh mục từ DB lên ComboBox
        List<com.example.smartspend.model.Category> categories = categoryDAO.getAllCategories();

        cbCategory.getItems().clear();
        categoryMap.clear();

        // NẾU MẤT KẾT NỐI DB THÌ BÁO LỖI LÊN GIAO DIỆN CHỨ KHÔNG VĂNG APP
        if (categories == null || categories.isEmpty()) {
            cbCategory.getItems().add("Lỗi kết nối DB");
            cbCategory.getSelectionModel().selectFirst();
            cbCategory.setDisable(true); // Khóa luôn ô này lại
            return;
        }

        for (com.example.smartspend.model.Category cat : categories) {
            cbCategory.getItems().add(cat.getName());
            categoryMap.put(cat.getName(), cat.getCategoryId());
        }

        if (!cbCategory.getItems().isEmpty()) {
            cbCategory.getSelectionModel().selectFirst();
            cbCategory.setDisable(false); // Mở lại nếu có mạng
        }
    }

    @FXML void handleAddIncome() { processTransaction(TransactionType.INCOME); }

    @FXML void handleAddExpense() { processTransaction(TransactionType.EXPENSE); }

    private void processTransaction(TransactionType type) {
        try {
            double amount = Double.parseDouble(txtAmount.getText());
            LocalDate date = dpDate.getValue();
            if (date == null) throw new IllegalArgumentException("Vui lòng chọn ngày!");

            // Kiểm tra xem có lấy được Category không
            if (categoryMap.isEmpty()) {
                throw new IllegalArgumentException("Không có dữ liệu Danh mục. Vui lòng kiểm tra lại kết nối mạng/Database!");
            }

            Transaction newTrans = new Transaction();
            newTrans.setAmount(amount);
            newTrans.setDate(date);
            newTrans.setNote(txtNote.getText());
            int selectedCategoryId = categoryMap.getOrDefault(cbCategory.getValue(), 1);
            newTrans.setCategoryId(selectedCategoryId);
            newTrans.setType(type);

            transactionService.addTransaction(newTrans);

            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Giao dịch đã được ghi nhận!");

            // Xóa form sau khi thêm
            txtAmount.clear();
            txtNote.clear();
            dpDate.setValue(null);

        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền phải là chữ số hợp lệ!");
        } catch (IllegalArgumentException e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Cảnh báo", e.getMessage());
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }
}