package com.example.smartspend.controller;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.AlertHelper;
import com.example.smartspend.utils.CurrencyFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionsController implements Initializable {

    // --- FXML BINDINGS ---
    @FXML private TextField txtSearch;

    @FXML private Label lblTotalSpent;
    @FXML private Label lblSpentBadge;
    @FXML private Label lblTopCategory;
    @FXML private Label lblTopCategoryAmount;
    @FXML private Label lblTopCategoryPct;

    @FXML private ComboBox<String> cbFilterCategory;
    @FXML private ComboBox<String> cbFilterAccount;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private ComboBox<Integer> cbPageSize;

    @FXML private Label lblSelectedCount;
    @FXML private Label lblRowCount;
    @FXML private CheckBox chkSelectAll;

    @FXML private TableView<Transaction>         tblTransactions;
    @FXML private TableColumn<Transaction, LocalDate>      colDate;
    @FXML private TableColumn<Transaction, String>         colDescription;
    @FXML private TableColumn<Transaction, Integer>        colCategory;
    @FXML private TableColumn<Transaction, String>         colMethod;
    @FXML private TableColumn<Transaction, String>         colStatus;
    @FXML private TableColumn<Transaction, Double>         colAmount;

    // --- STATE ---
    private final TransactionService transactionService = new TransactionService();
    private ObservableList<Transaction> masterList = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ──────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupComboBoxes();
        loadTransactions();
    }

    // ──────────────────────────────────────────────────────────────
    //  TABLE SETUP
    // ──────────────────────────────────────────────────────────────
    private void setupTableColumns() {
        // Bind các cột đơn giản
        colDescription.setCellValueFactory(new PropertyValueFactory<>("note"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryId"));

        // Cột Date — format hiển thị "Oct 24, 2023"
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(DATE_FMT));
            }
        });

        // Cột Amount — màu xanh cho Income, màu đỏ cho Expense
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) { setText(null); setStyle(""); return; }
                Transaction t = getTableView().getItems().get(getIndex());
                boolean isIncome = t.getType() == TransactionType.INCOME;
                setText((isIncome ? "+" : "-") + CurrencyFormatter.format(amount));
                setStyle(isIncome
                        ? "-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-font-size: 13px;"
                        : "-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");
            }
        });

        // Cột Method — hiển thị placeholder (chưa có data thật)
        colMethod.setCellValueFactory(new PropertyValueFactory<>("note")); // tạm dùng note
        colMethod.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : "Direct Deposit");
                setStyle("-fx-text-fill: #64748b;");
            }
        });

        // Cột Status — placeholder "Completed"
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                setText("● Completed");
                setStyle("-fx-text-fill: #16a34a; -fx-font-weight: 600;");
            }
        });

        // Cột Category — hiển thị type thay vì categoryId
        colCategory.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer catId, boolean empty) {
                super.updateItem(catId, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                Transaction t = getTableView().getItems().get(getIndex());
                setText(t.getType().name());
                setStyle(t.getType() == TransactionType.INCOME
                        ? "-fx-text-fill: #15803d; -fx-font-weight: 700;"
                        : "-fx-text-fill: #b91c1c; -fx-font-weight: 700;");
            }
        });

        // Chiều rộng tự động cho Description
        colDescription.setPrefWidth(220);
        tblTransactions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupComboBoxes() {
        cbFilterCategory.getItems().addAll("All Categories", "Income", "Expense", "Dining", "Shopping", "Transport");
        cbFilterCategory.setValue("All Categories");

        cbFilterStatus.getItems().addAll("All Status", "Completed", "Pending");
        cbFilterStatus.setValue("All Status");

        cbPageSize.getItems().addAll(10, 25, 50, 100);
        cbPageSize.setValue(25);
    }

    // ──────────────────────────────────────────────────────────────
    //  DATA LOADING
    // ──────────────────────────────────────────────────────────────
    private void loadTransactions() {
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();
            masterList = FXCollections.observableArrayList(transactions);
            tblTransactions.setItems(masterList);

            updateSummaryCards(transactions);
            updateRowCountLabel(transactions.size());

        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Lỗi tải dữ liệu",
                    "Không thể kết nối database: " + e.getMessage());
        }
    }

    private void updateSummaryCards(List<Transaction> transactions) {
        // Tổng chi tiêu
        double totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();
        lblTotalSpent.setText(CurrencyFormatter.format(totalExpense));

        // Top category (dùng type vì CategoryDAO chưa implement)
        long expenseCount = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE).count();
        long incomeCount  = transactions.size() - expenseCount;
        String topCat = expenseCount >= incomeCount ? "Expense" : "Income";
        double topAmt = expenseCount >= incomeCount ? totalExpense
                : transactions.stream().filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
        int pct = transactions.isEmpty() ? 0 : (int)((expenseCount * 100.0) / transactions.size());

        lblTopCategory.setText(topCat);
        lblTopCategoryAmount.setText(CurrencyFormatter.format(topAmt));
        lblTopCategoryPct.setText(pct + "% of total");
    }

    private void updateRowCountLabel(int count) {
        lblRowCount.setText("Showing " + count + " transaction" + (count != 1 ? "s" : ""));
    }

    // ──────────────────────────────────────────────────────────────
    //  FXML EVENT HANDLERS
    // ──────────────────────────────────────────────────────────────
    @FXML
    private void handleFilter() {
        // Placeholder — sẽ implement khi CategoryDAO hoàn thiện
        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Bộ lọc",
                "Tính năng lọc sẽ hoàn thiện sau khi CategoryDAO được implement.");
    }

    @FXML
    private void handleClearFilters() {
        cbFilterCategory.setValue("All Categories");
        cbFilterStatus.setValue("All Status");
        tblTransactions.setItems(masterList);
        updateRowCountLabel(masterList.size());
    }

    @FXML
    private void handleDeleteSelected() {
        Transaction selected = tblTransactions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một giao dịch để xóa.");
            return;
        }
        // TODO: Gọi transactionService.deleteTransaction(selected.getTransactionId()) khi có
        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Thông báo",
                "Tính năng xóa sẽ được thêm ở task tiếp theo.");
    }

    @FXML
    private void handlePrevPage()   { /* TODO: implement paging */ }

    @FXML
    private void handleNextPage()   { /* TODO: implement paging */ }

    @FXML
    private void handlePageChange() { /* TODO: implement paging */ }

    /**
     * Được gọi từ bên ngoài (ví dụ sau khi Add/Edit thành công)
     * để reload lại bảng.
     */
    public void refreshTable() {
        loadTransactions();
    }

}