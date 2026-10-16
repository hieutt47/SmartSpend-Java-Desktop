package com.example.smartspend.controller;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.FinancialAdvisorService;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.CurrencyFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class PortfolioController {

    @FXML private Label lblNetBalance;
    @FXML private Label lblHeroSummary;
    @FXML private Label lblCashFlowBadge;
    @FXML private Label lblLastUpdated;
    @FXML private Label lblSavingsRate;
    @FXML private Label lblSavingsDelta;
    @FXML private Label lblRunway;
    @FXML private Label lblProjectedIncome;
    @FXML private Label lblCommittedExpenses;
    @FXML private Label lblSafeToSpend;
    @FXML private Label lblSavingsDiscipline;
    @FXML private Label lblSavingsComment;
    @FXML private Label lblNextPriority;
    @FXML private Label lblPriorityDesc;
    @FXML private Label lblHealthScore;
    @FXML private Label lblHealthComment;
    @FXML private Button btnAddIncome;
    @FXML private Button btnAddExpense;
    @FXML private Button btnViewAll;
    @FXML private Button btnDetails;
    @FXML private VBox vboxRecentTransactions;
    @FXML private BarChart<String, Number> portfolioChart;

    private final TransactionService txService = new TransactionService();
    private final FinancialAdvisorService advisorService = new FinancialAdvisorService();

    @FXML
    private void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        List<Transaction> transactions = txService.getAllTransactions();
        if (lblLastUpdated != null) {
            lblLastUpdated.setText("Last updated: Today at " +
                    java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        updateMetrics(transactions);
        buildMonthlyChart(transactions);
        buildRecentTransactions(transactions);
    }

    private void updateMetrics(List<Transaction> transactions) {
        FinancialAdvisorService.Snapshot snapshot = advisorService.buildSnapshot(transactions);
        YearMonth now = YearMonth.now();
        double monthIncome = transactions.stream()
                .filter(t -> t.getDate() != null && t.getType() == TransactionType.INCOME && YearMonth.from(t.getDate()).equals(now))
                .mapToDouble(Transaction::getAmount).sum();
        double monthExpense = transactions.stream()
                .filter(t -> t.getDate() != null && t.getType() == TransactionType.EXPENSE && YearMonth.from(t.getDate()).equals(now))
                .mapToDouble(Transaction::getAmount).sum();
        double safeToSpend = Math.max(0, monthIncome - monthExpense) * 0.5;
        double runway = monthExpense <= 0 ? 0 : Math.max(0, snapshot.net()) / monthExpense;
        int score = calculateHealthScore(snapshot);

        setText(lblNetBalance, CurrencyFormatter.format(snapshot.net()));
        setText(lblHeroSummary, advisorService.buildSummary(transactions));
        setText(lblCashFlowBadge, snapshot.net() >= 0 ? "Positive cash flow" : "Needs attention");
        setText(lblSavingsRate, String.format(Locale.US, "%.1f%%", snapshot.savingsRate()));
        setText(lblSavingsDelta, snapshot.savingsRate() >= 20 ? "Good savings discipline" : "Improve savings rate");
        setText(lblRunway, monthExpense <= 0 ? "—" : String.format(Locale.US, "%.1f mo", runway));
        setText(lblProjectedIncome, CurrencyFormatter.format(monthIncome));
        setText(lblCommittedExpenses, CurrencyFormatter.format(monthExpense));
        setText(lblSafeToSpend, CurrencyFormatter.format(safeToSpend));
        setText(lblSavingsDiscipline, String.format(Locale.US, "%.1f%%", snapshot.savingsRate()));
        setText(lblSavingsComment, snapshot.savingsRate() >= 20 ? "You are protecting cash flow well." : "Try to reach at least 15–20% savings.");
        setText(lblNextPriority, snapshot.net() < 0 ? "Reduce spend" : "Build reserve");
        setText(lblPriorityDesc, snapshot.net() < 0
                ? "Cut flexible expenses until net balance returns to positive."
                : "Move part of the surplus into emergency savings or learning funds.");
        setText(lblHealthScore, String.valueOf(score));
        setText(lblHealthComment, score >= 80
                ? "Strong financial posture. Keep budgets reviewed weekly."
                : score >= 55 ? "Moderate position. Focus on reducing the largest spending category."
                : "Risk level is high. Prioritize cash-flow recovery this month.");
    }

    private int calculateHealthScore(FinancialAdvisorService.Snapshot s) {
        int score = 50;
        if (s.net() >= 0) score += 20; else score -= 20;
        if (s.savingsRate() >= 30) score += 25;
        else if (s.savingsRate() >= 15) score += 15;
        else if (s.savingsRate() > 0) score += 5;
        if (s.expense() > 0 && s.topCategoryAmount() / s.expense() > 0.5) score -= 10;
        return Math.max(0, Math.min(100, score));
    }

    private void setText(Label label, String value) {
        if (label != null) label.setText(value);
    }

    private void buildMonthlyChart(List<Transaction> transactions) {
        if (portfolioChart == null) return;

        portfolioChart.getData().clear();
        portfolioChart.setAnimated(false);
        portfolioChart.setLegendVisible(true);
        portfolioChart.setCategoryGap(26);
        portfolioChart.setBarGap(6);

        if (portfolioChart.getXAxis() instanceof CategoryAxis axis) {
            axis.setLabel(null);
            axis.setAnimated(false);
        }
        if (portfolioChart.getYAxis() instanceof NumberAxis axis) {
            axis.setLabel(null);
            axis.setForceZeroInRange(true);
            axis.setAutoRanging(true);
            axis.setAnimated(false);
        }

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expense");
        XYChart.Series<String, Number> netSeries = new XYChart.Series<>();
        netSeries.setName("Net");

        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String label = month.getMonth().toString().substring(0, 3);
            double income = transactions.stream()
                    .filter(t -> t.getDate() != null && t.getType() == TransactionType.INCOME
                            && t.getDate().getMonthValue() == month.getMonthValue()
                            && t.getDate().getYear() == month.getYear())
                    .mapToDouble(Transaction::getAmount).sum();
            double expense = transactions.stream()
                    .filter(t -> t.getDate() != null && t.getType() == TransactionType.EXPENSE
                            && t.getDate().getMonthValue() == month.getMonthValue()
                            && t.getDate().getYear() == month.getYear())
                    .mapToDouble(Transaction::getAmount).sum();

            incomeSeries.getData().add(new XYChart.Data<>(label, income));
            expenseSeries.getData().add(new XYChart.Data<>(label, expense));
            netSeries.getData().add(new XYChart.Data<>(label, Math.max(income - expense, 0)));
        }

        portfolioChart.getData().addAll(incomeSeries, expenseSeries, netSeries);
        Platform.runLater(() -> {
            styleSeries(incomeSeries, "#16a34a");
            styleSeries(expenseSeries, "#ef4444");
            styleSeries(netSeries, "#2563eb");
            styleChartLegend();
        });
    }

    private void styleChartLegend() {
        if (portfolioChart == null) return;
        String[] colors = {"#16a34a", "#ef4444", "#2563eb"};
        int index = 0;
        for (javafx.scene.Node symbol : portfolioChart.lookupAll(".chart-legend-item-symbol")) {
            if (index >= colors.length) break;
            symbol.setStyle("-fx-background-color: " + colors[index] + "; -fx-background-radius: 6; -fx-padding: 5;");
            index++;
        }
    }

    private void styleSeries(XYChart.Series<String, Number> series, String color) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: " + color + "; -fx-background-radius: 8 8 0 0;");
            }
        }
    }

    private void buildRecentTransactions(List<Transaction> transactions) {
        if (vboxRecentTransactions == null) return;
        vboxRecentTransactions.getChildren().clear();
        if (transactions.isEmpty()) {
            Label empty = new Label("Chưa có giao dịch. Bấm Add Income hoặc Add Expense để bắt đầu.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-wrap-text: true;");
            vboxRecentTransactions.getChildren().add(empty);
            return;
        }
        transactions.stream().limit(6).forEach(t -> vboxRecentTransactions.getChildren().add(transactionItem(t)));
    }

    private HBox transactionItem(Transaction t) {
        HBox row = new HBox(12);
        row.getStyleClass().add("transaction-item");
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-padding: 10;");
        Label icon = new Label(t.getType() == TransactionType.INCOME ? "IN" : "OUT");
        icon.setStyle("-fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: " + (t.getType() == TransactionType.INCOME ? "#16a34a" : "#ef4444") + ";");
        VBox text = new VBox(2);
        Label note = new Label(t.getNote() == null || t.getNote().isBlank() ? t.getCategoryName() : t.getNote());
        note.setStyle("-fx-font-weight: bold;");
        Label category = new Label(((t.getCategoryName() == null || t.getCategoryName().isBlank()) ? t.getType().name() : t.getCategoryName()) + " • " + t.getDate());
        category.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        text.getChildren().addAll(note, category);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label amount = new Label((t.getType() == TransactionType.INCOME ? "+" : "-") + CurrencyFormatter.format(t.getAmount()));
        amount.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (t.getType() == TransactionType.INCOME ? "#16a34a" : "#ef4444") + ";");
        row.getChildren().addAll(icon, text, amount);
        return row;
    }

    @FXML private void handleAddIncome() { openAddTransactionPopup(TransactionType.INCOME); }
    @FXML private void handleAddExpense() { openAddTransactionPopup(TransactionType.EXPENSE); }

    @FXML
    private void openAddTransactionPopup() {
        openAddTransactionPopup(TransactionType.EXPENSE);
    }

    private void openAddTransactionPopup(TransactionType defaultType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/transaction/AddTransaction.fxml"));
            Parent root = loader.load();
            AddTransactionController controller = loader.getController();
            if (controller != null) controller.setDefaultType(defaultType);
            Stage stage = new Stage();
            stage.setTitle("Thêm giao dịch - SmartSpend");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadDashboardData();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Không mở được form giao dịch: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleViewAll() {
        try {
            Parent transactionsView = FXMLLoader.load(getClass().getResource("/transaction/TransactionsView.fxml"));
            BorderPane mainPane = (BorderPane) btnViewAll.getScene().getRoot();
            mainPane.setCenter(transactionsView);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Không mở được Transactions: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleDetails() {
        List<Transaction> transactions = txService.getAllTransactions();
        FinancialAdvisorService.Snapshot s = advisorService.buildSnapshot(transactions);
        Map<TransactionType, Long> counts = transactions.stream().collect(Collectors.groupingBy(Transaction::getType, Collectors.counting()));
        String msg = "Thu nhập: " + CurrencyFormatter.format(s.income()) + "\n"
                + "Chi tiêu: " + CurrencyFormatter.format(s.expense()) + "\n"
                + "Số giao dịch thu: " + counts.getOrDefault(TransactionType.INCOME, 0L) + "\n"
                + "Số giao dịch chi: " + counts.getOrDefault(TransactionType.EXPENSE, 0L) + "\n"
                + "Số dư ròng: " + CurrencyFormatter.format(s.net()) + "\n"
                + "Danh mục chi lớn nhất: " + s.topCategory();
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
