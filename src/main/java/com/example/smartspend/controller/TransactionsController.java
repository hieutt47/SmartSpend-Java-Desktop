package com.example.smartspend.controller;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.AlertHelper;
import com.example.smartspend.utils.CurrencyFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.net.URL;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionsController implements Initializable {
    @FXML private TextField txtSearch;
    @FXML private Label lblTotalSpent;
    @FXML private Label lblSpentBadge;
    @FXML private Label lblTopCategory;
    @FXML private Label lblTopCategoryAmount;
    @FXML private Label lblTopCategoryPct;
    @FXML private Label lblRecordCount;
    @FXML private ComboBox<String> cbFilterCategory;
    @FXML private ComboBox<String> cbFilterAccount;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private Label lblSelectedCount;
    @FXML private Label lblRowCount;
    @FXML private CheckBox chkSelectAll;
    @FXML private TableView<Transaction> tblTransactions;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colMethod;
    @FXML private TableColumn<Transaction, String> colStatus;
    @FXML private TableColumn<Transaction, Double> colAmount;

    private final TransactionService transactionService = new TransactionService();
    private ObservableList<Transaction> masterList = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        loadTransactions();
    }

    private void setupTableColumns() {
        colDescription.setCellValueFactory(new PropertyValueFactory<>("note"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colMethod.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType() == TransactionType.INCOME ? "Income" : "Expense"));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty("Completed"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        colDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(DATE_FMT));
            }
        });
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) { setText(null); setStyle(""); return; }
                Transaction t = getTableView().getItems().get(getIndex());
                boolean income = t.getType() == TransactionType.INCOME;
                setText((income ? "+" : "-") + CurrencyFormatter.format(amount));
                setStyle("-fx-text-fill: " + (income ? "#16a34a" : "#dc2626") + "; -fx-font-weight: bold;");
            }
        });
        colMethod.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                Transaction t = getTableView().getItems().get(getIndex());
                setText(t.getType() == TransactionType.INCOME ? "Income" : "Expense");
            }
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : "● Completed");
                setStyle(empty ? "" : "-fx-text-fill: #16a34a; -fx-font-weight: 600;");
            }
        });
        tblTransactions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblTransactions.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblTransactions.getSelectionModel().getSelectedItem() != null) {
                openEditDialog(tblTransactions.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void setupComboBoxes() {
        cbFilterCategory.getItems().setAll("All Types", "Income", "Expense");
        cbFilterCategory.setValue("All Types");
        if (cbFilterAccount != null) {
            cbFilterAccount.getItems().setAll("All Accounts", "Cash", "Bank");
            cbFilterAccount.setValue("All Accounts");
        }
        if (cbFilterStatus != null) {
            cbFilterStatus.getItems().setAll("All Status", "Completed");
            cbFilterStatus.setValue("All Status");
        }
    }

    private void setupListeners() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbFilterCategory.setOnAction(e -> applyFilters());
        tblTransactions.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (lblSelectedCount != null) lblSelectedCount.setText(selected == null ? "No transaction selected" : "1 transaction selected");
        });
    }

    private void loadTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        masterList = FXCollections.observableArrayList(transactions);
        applyFilters();
        updateSummaryCards(transactions);
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        String filter = cbFilterCategory.getValue();
        ObservableList<Transaction> filtered = masterList.filtered(t -> {
            String note = t.getNote() == null ? "" : t.getNote().toLowerCase();
            String category = t.getCategoryName() == null ? "" : t.getCategoryName().toLowerCase();
            boolean matchesKeyword = keyword.isBlank()
                    || note.contains(keyword)
                    || category.contains(keyword)
                    || t.getType().name().toLowerCase().contains(keyword)
                    || (t.getDate() != null && t.getDate().toString().contains(keyword));
            boolean matchesType = filter == null || filter.equals("All Types")
                    || (filter.equals("Income") && t.getType() == TransactionType.INCOME)
                    || (filter.equals("Expense") && t.getType() == TransactionType.EXPENSE);
            return matchesKeyword && matchesType;
        });
        tblTransactions.setItems(filtered);
        updateRowCountLabel(filtered.size());
    }

    private void updateSummaryCards(List<Transaction> transactions) {
        double totalExpense = transactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE).mapToDouble(Transaction::getAmount).sum();
        double totalIncome = transactions.stream().filter(t -> t.getType() == TransactionType.INCOME).mapToDouble(Transaction::getAmount).sum();
        lblTotalSpent.setText(CurrencyFormatter.format(totalExpense));
        if (lblSpentBadge != null) lblSpentBadge.setText("Net: " + CurrencyFormatter.format(totalIncome - totalExpense));
        java.util.Map<String, Double> categoryTotals = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getCategoryName() == null || t.getCategoryName().isBlank() ? "Khác" : t.getCategoryName(),
                        java.util.stream.Collectors.summingDouble(Transaction::getAmount)));
        java.util.Map.Entry<String, Double> top = categoryTotals.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue()).orElse(null);
        lblTopCategory.setText(top == null ? "No expenses" : top.getKey());
        lblTopCategoryAmount.setText(top == null ? CurrencyFormatter.format(0) : CurrencyFormatter.format(top.getValue()));
        int pct = top == null || totalExpense <= 0 ? 0 : (int) Math.round(top.getValue() * 100 / totalExpense);
        lblTopCategoryPct.setText(pct + "% of expenses");
        if (lblRecordCount != null) lblRecordCount.setText(String.valueOf(transactions.size()));
    }

    private void updateRowCountLabel(int count) {
        lblRowCount.setText("Showing " + count + " transaction" + (count != 1 ? "s" : ""));
    }

    @FXML private void handleFilter() { applyFilters(); }

    @FXML private void handleClearFilters() {
        txtSearch.clear();
        cbFilterCategory.setValue("All Types");
        if (cbFilterStatus != null) cbFilterStatus.setValue("All Status");
        applyFilters();
    }

    @FXML
    private void handleDeleteSelected() {
        Transaction selected = tblTransactions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một giao dịch để xóa.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa giao dịch này?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                try {
                    transactionService.deleteTransaction(selected.getTransactionId());
                    loadTransactions();
                } catch (Exception e) {
                    AlertHelper.showAlert(Alert.AlertType.ERROR, "Không thể xóa", e.getMessage());
                }
            }
        });
    }

    private void openEditDialog(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/transaction/EditTransaction.fxml"));
            Parent root = loader.load();
            EditTransactionController controller = loader.getController();
            controller.initData(transaction);
            Stage stage = new Stage();
            stage.setTitle("Sửa giao dịch");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadTransactions();
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Không mở được form sửa", e.getMessage());
        }
    }

    @FXML
    private void handleExportCsv() {
        exportTransactions("smartspend-transactions.csv", false);
    }

    @FXML
    private void handleExportReport() {
        exportTransactions("smartspend-report.csv", true);
    }

    private void exportTransactions(String defaultName, boolean includeSummary) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export SmartSpend CSV");
        chooser.setInitialFileName(defaultName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(tblTransactions.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            if (includeSummary) {
                double income = masterList.stream().filter(t -> t.getType() == TransactionType.INCOME).mapToDouble(Transaction::getAmount).sum();
                double expense = masterList.stream().filter(t -> t.getType() == TransactionType.EXPENSE).mapToDouble(Transaction::getAmount).sum();
                out.println("SmartSpend Report");
                out.println("Host,Trần Trung Hiếu");
                out.println("Email,trantrunghieu30032006@gmail.com");
                out.println("Total Income," + income);
                out.println("Total Expense," + expense);
                out.println("Net Balance," + (income - expense));
                out.println();
            }
            out.println("Date,Type,Category,Note,Amount");
            for (Transaction t : tblTransactions.getItems()) {
                out.printf("%s,%s,%s,%s,%.2f%n",
                        t.getDate(),
                        t.getType(),
                        csv(t.getCategoryName()),
                        csv(t.getNote()),
                        t.getAmount());
            }
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Export thành công", "Đã xuất file: " + file.getAbsolutePath());
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Export thất bại", e.getMessage());
        }
    }

    private String csv(String text) {
        String safe = text == null ? "" : text.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    @FXML private void handlePrevPage() { }
    @FXML private void handleNextPage() { }
    @FXML private void handlePageChange() { }
    public void refreshTable() { loadTransactions(); }
}
