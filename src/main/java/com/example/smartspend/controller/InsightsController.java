package com.example.smartspend.controller;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.CurrencyFormatter;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class InsightsController implements Initializable {

    // --- HEADER ---
    @FXML private Label lblSavingsRate;
    @FXML private Label lblLiquidity;
    @FXML private Label lblSyncTime;

    // --- CHART ---
    @FXML private BarChart<String, Number> barChartIncomeExpense;
    @FXML private GridPane gridCategories;

    // --- SNAPSHOT SIDEBAR ---
    @FXML private Label lblSnapshotBadge;
    @FXML private Label lblIncomeForecast;
    @FXML private Label lblProjectedBurn;
    @FXML private Label lblSafeToSpend;
    @FXML private Label lblRecoBody;

    // --- SPLIT BAR ---
    @FXML private Region splitIncome;
    @FXML private Region splitExpense;
    @FXML private Label  lblSplitPct;

    // ---
    private final TransactionService txService = new TransactionService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            List<Transaction> all = txService.getAllTransactions();
            buildChart(all);
            buildSummary(all);
            buildCategoryCards(all);
            buildSplitBar(all);
            buildRecommendation(all);
            animatePageIn();
        } catch (Exception e) {
            System.err.println("InsightsController error: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────
    //  CHART: Income vs Expenses (last 6 months)
    // ────────────────────────────────────────────
    private void buildChart(List<Transaction> all) {
        barChartIncomeExpense.getData().clear();
        barChartIncomeExpense.setLegendVisible(false);
        barChartIncomeExpense.setStyle(
                "-fx-background-color: transparent; -fx-plot-background-color: transparent;");

        XYChart.Series<String, Number> incomeSeries  = new XYChart.Series<>();
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        expenseSeries.setName("Expenses");

        // Get last 6 months
        LocalDate now = LocalDate.now();
        Locale locale = new Locale("vi", "VN");

        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthLabel = month.getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();

            final int m = month.getMonthValue();
            final int y = month.getYear();

            double inc = all.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME
                            && t.getDate() != null
                            && t.getDate().getMonthValue() == m
                            && t.getDate().getYear() == y)
                    .mapToDouble(Transaction::getAmount).sum();

            double exp = all.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE
                            && t.getDate() != null
                            && t.getDate().getMonthValue() == m
                            && t.getDate().getYear() == y)
                    .mapToDouble(Transaction::getAmount).sum();

            incomeSeries.getData().add(new XYChart.Data<>(monthLabel, inc));
            expenseSeries.getData().add(new XYChart.Data<>(monthLabel, exp));
        }

        barChartIncomeExpense.getData().addAll(incomeSeries, expenseSeries);

        // Style bars after rendering
        barChartIncomeExpense.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) styleBarChart();
        });
    }

    private void styleBarChart() {
        barChartIncomeExpense.lookupAll(".data0.chart-bar").forEach(
                n -> n.setStyle("-fx-bar-fill: #2563eb; -fx-background-radius: 4 4 0 0;"));
        barChartIncomeExpense.lookupAll(".data1.chart-bar").forEach(
                n -> n.setStyle("-fx-bar-fill: #ef4444; -fx-background-radius: 4 4 0 0;"));
        barChartIncomeExpense.lookupAll(".chart-plot-background")
                .forEach(n -> n.setStyle("-fx-background-color: transparent;"));
    }

    // ────────────────────────────────────────────
    //  SUMMARY: KPIs + Snapshot sidebar
    // ────────────────────────────────────────────
    private void buildSummary(List<Transaction> all) {
        double totalIncome  = all.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
        double totalExpense = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();

        double net = totalIncome - totalExpense;
        double savingsRate = totalIncome > 0
                ? Math.round((net / totalIncome) * 1000.0) / 10.0 : 0.0;

        lblSavingsRate.setText(savingsRate + "%");
        lblLiquidity.setText(CurrencyFormatter.format(net));
        lblSyncTime.setText("Data synchronized: Today at "
                + java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));

        // Snapshot sidebar
        lblIncomeForecast.setText("+" + CurrencyFormatter.format(totalIncome));
        lblProjectedBurn.setText("-"  + CurrencyFormatter.format(totalExpense));
        lblSafeToSpend.setText(CurrencyFormatter.format(net));

        if (net >= 0) {
            lblSnapshotBadge.setText("SURPLUS");
            lblSnapshotBadge.getStyleClass().setAll("snapshot-badge-surplus");
            lblSafeToSpend.getStyleClass().setAll("snapshot-row-green",
                    "insights-kpi-value");
        } else {
            lblSnapshotBadge.setText("DEFICIT");
            lblSnapshotBadge.getStyleClass().setAll("snapshot-badge-deficit");
            lblSafeToSpend.getStyleClass().setAll("snapshot-row-red",
                    "insights-kpi-value");
        }

        lblFooterSync();
    }

    private void lblFooterSync() {
        // footer sync label synced from buildSummary
    }

    // ────────────────────────────────────────────
    //  CATEGORY CARDS (3-column grid)
    // ────────────────────────────────────────────
    private void buildCategoryCards(List<Transaction> all) {
        gridCategories.getChildren().clear();
        Map<String, Double> expenses = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategoryName() == null || t.getCategoryName().isBlank() ? "Khác" : t.getCategoryName(),
                        Collectors.summingDouble(Transaction::getAmount)));
        if (expenses.isEmpty()) {
            Label empty = new Label("Chưa có dữ liệu chi tiêu để hiển thị phân tích theo danh mục.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-padding: 18;");
            gridCategories.add(empty, 0, 0, 3, 1);
            return;
        }

        double totalExpense = expenses.values().stream().mapToDouble(Double::doubleValue).sum();
        List<Map.Entry<String, Double>> topCategories = expenses.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(6)
                .toList();
        String[] colors = {"#16a34a", "#2563eb", "#dc2626", "#d97706", "#7c3aed", "#0891b2"};
        String[] progressStyles = {"progress-bar-green", "progress-bar-blue", "progress-bar-red", "progress-bar-amber", "progress-bar-blue", "progress-bar-green"};
        int col = 0, row = 0, index = 0;
        for (Map.Entry<String, Double> entry : topCategories) {
            double amount = entry.getValue();
            double share = totalExpense <= 0 ? 0 : amount / totalExpense;
            VBox card = buildCategoryCard(entry.getKey(), amount, totalExpense, share, colors[index], progressStyles[index]);
            gridCategories.add(card, col, row);
            col++;
            if (col == 3) { col = 0; row++; }
            index++;
        }
    }

    private VBox buildCategoryCard(String name, double amount, double totalExpense, double share,
                                   String color, String barStyle) {
        VBox card = new VBox(10);
        card.getStyleClass().add("cat-card");

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("cat-name");
        card.getChildren().add(nameLbl);

        Label amtLbl = new Label(CurrencyFormatter.format(amount));
        amtLbl.getStyleClass().add("cat-amount");
        card.getChildren().add(amtLbl);

        ProgressBar pb = new ProgressBar(share);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(8);
        pb.getStyleClass().addAll("progress-bar", barStyle);
        card.getChildren().add(pb);

        HBox bottom = new HBox(6);
        bottom.setAlignment(Pos.CENTER_LEFT);
        Label shareLbl = new Label(String.format(Locale.US, "%.1f%% tổng chi tiêu", share * 100));
        shareLbl.getStyleClass().add("cat-limit");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label totalLbl = new Label("/ " + CurrencyFormatter.format(totalExpense));
        totalLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        bottom.getChildren().addAll(shareLbl, spacer, totalLbl);
        card.getChildren().add(bottom);
        return card;
    }

    // ────────────────────────────────────────────
    //  SPLIT BAR
    // ────────────────────────────────────────────
    private void buildSplitBar(List<Transaction> all) {
        double inc = all.stream().filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
        double exp = all.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();
        double total = inc + exp;
        if (total == 0) return;

        double incPct = inc / total;
        double expPct = exp / total;

        // Use percent width via HBox grow weights
        HBox.setHgrow(splitIncome,  Priority.ALWAYS);
        HBox.setHgrow(splitExpense, Priority.ALWAYS);
        splitIncome.setPrefWidth(incPct  * 240);
        splitExpense.setPrefWidth(expPct * 240);
        lblSplitPct.setText(
                (int)(incPct * 100) + "% / " + (int)(expPct * 100) + "%");
    }

    // ────────────────────────────────────────────
    //  SMART RECOMMENDATION
    // ────────────────────────────────────────────
    private void buildRecommendation(List<Transaction> all) {
        double totalExp = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();
        double totalInc = all.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();

        String msg;
        if (all.isEmpty()) {
            msg = "Chưa có dữ liệu. Hãy thêm giao dịch đầu tiên để nhận gợi ý cá nhân hóa!";
        } else if (totalExp > totalInc * 0.8) {
            msg = "Chi tiêu đang chiếm hơn 80% thu nhập. Hãy xem lại các khoản không thiết yếu để bảo vệ tỷ lệ tiết kiệm.";
        } else if (totalInc - totalExp > 0) {
            double saved = totalInc - totalExp;
            msg = "Bạn đang giữ lại được " + CurrencyFormatter.format(saved) +
                    ". Hãy ưu tiên quỹ dự phòng hoặc mục tiêu tài chính đã đặt ra.";
        } else {
            msg = "Phân tích dữ liệu của bạn và đề xuất các cơ hội tiết kiệm thông minh hơn.";
        }
        lblRecoBody.setText(msg);
    }

    @FXML
    private void handleOpenCoach() {
        try {
            javafx.scene.Parent coach = javafx.fxml.FXMLLoader.load(getClass().getResource("/layout/SmartCoachView.fxml"));
            javafx.scene.layout.BorderPane mainPane = (javafx.scene.layout.BorderPane) lblRecoBody.getScene().getRoot();
            mainPane.setCenter(coach);
        } catch (Exception e) {
            System.err.println("Không mở được Smart Coach: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────
    //  PAGE ANIMATION
    // ────────────────────────────────────────────
    private void animatePageIn() {
        // Staggered fade + slide for each major section
        List<javafx.scene.Node> nodes = Arrays.asList(
                barChartIncomeExpense, gridCategories);

        int delay = 0;
        for (javafx.scene.Node node : nodes) {
            FadeTransition ft = new FadeTransition(Duration.millis(400), node);
            ft.setFromValue(0); ft.setToValue(1);
            ft.setDelay(Duration.millis(delay));

            TranslateTransition tt = new TranslateTransition(Duration.millis(400), node);
            tt.setFromY(15); tt.setToY(0);
            tt.setDelay(Duration.millis(delay));

            ft.play(); tt.play();
            delay += 100;
        }
    }
}