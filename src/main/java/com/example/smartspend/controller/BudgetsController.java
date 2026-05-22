package com.example.smartspend.controller;

import com.example.smartspend.model.Budget;
import com.example.smartspend.service.BudgetService;
import com.example.smartspend.utils.AlertHelper;
import com.example.smartspend.utils.CurrencyFormatter;
import com.example.smartspend.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BudgetsController {
    @FXML private FlowPane budgetGrid;
    @FXML private Label lblBudgetMonth;
    @FXML private Label lblTotalAllocation;
    @FXML private Label lblSpent;
    @FXML private Label lblRemaining;
    @FXML private Label lblBudgetProgress;
    @FXML private Label lblBudgetInsight;
    @FXML private ProgressBar overallProgress;

    private final BudgetService budgetService = new BudgetService();

    @FXML
    private void initialize() {
        YearMonth currentMonth = YearMonth.now();
        lblBudgetMonth.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));
        loadBudgets();
    }

    @FXML
    private void handleSetBudget() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/SetBudgetView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Set Monthly Budget");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadBudgets();
        } catch (Exception e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Budget", "Không mở được form thiết lập ngân sách: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshBudgets() {
        loadBudgets();
    }

    private void loadBudgets() {
        if (budgetGrid == null) return;
        budgetGrid.getChildren().clear();
        int userId = SessionManager.getCurrentUserId();
        List<Budget> budgets = budgetService.getCurrentMonthBudgets(userId);
        double allocation = budgets.stream().mapToDouble(Budget::getAmount).sum();
        double spent = budgets.stream().mapToDouble(Budget::getSpent).sum();
        double remaining = allocation - spent;
        double progress = allocation <= 0 ? 0 : Math.min(1, spent / allocation);

        lblTotalAllocation.setText(CurrencyFormatter.format(allocation));
        lblSpent.setText(CurrencyFormatter.format(spent) + " đã chi");
        lblRemaining.setText(CurrencyFormatter.format(remaining));
        lblBudgetProgress.setText("Tiến độ ngân sách (" + Math.round(progress * 100) + "%)");
        overallProgress.setProgress(progress);
        lblBudgetInsight.setText(buildInsight(progress, remaining, budgets.isEmpty()));

        if (budgets.isEmpty()) {
            Label empty = new Label("Bạn chưa thiết lập ngân sách cho tháng này. Bấm 'Set budget' để bắt đầu theo dõi giới hạn chi tiêu.");
            empty.getStyleClass().add("empty-state");
            empty.setWrapText(true);
            budgetGrid.getChildren().add(empty);
            return;
        }
        budgets.forEach(budget -> budgetGrid.getChildren().add(createBudgetCard(budget)));
    }

    private String buildInsight(double progress, double remaining, boolean empty) {
        if (empty) return "Tạo ngân sách cho từng danh mục để SmartSpend theo dõi mức chi còn an toàn.";
        if (remaining < 0) return "Bạn đã vượt tổng ngân sách. Nên rà lại các khoản chi linh hoạt ngay hôm nay.";
        if (progress >= 0.8) return "Bạn đã dùng trên 80% ngân sách. Ưu tiên các khoản thiết yếu trong phần còn lại của tháng.";
        return "Ngân sách đang trong vùng an toàn. Tiếp tục duy trì thói quen theo dõi chi tiêu.";
    }

    private VBox createBudgetCard(Budget budget) {
        VBox card = new VBox(10);
        card.setPrefWidth(255);
        card.getStyleClass().add("budget-card");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(budget.getCategoryName());
        name.getStyleClass().add("budget-category-name");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        int percent = (int) Math.round(budget.getProgress() * 100);
        Label pct = new Label(percent + "%");
        pct.getStyleClass().add(budget.getProgress() >= 0.9 ? "budget-pct-danger" : "budget-pct-safe");
        header.getChildren().addAll(name, spacer, pct);

        Label spent = new Label(CurrencyFormatter.format(budget.getSpent()) + " / " + CurrencyFormatter.format(budget.getAmount()));
        spent.getStyleClass().add("budget-spent");
        ProgressBar bar = new ProgressBar(budget.getProgress());
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add(budget.getProgress() >= 0.9 ? "budget-bar-danger" : "budget-bar-safe");
        Label remaining = new Label("Còn lại: " + CurrencyFormatter.format(budget.getRemaining()));
        remaining.getStyleClass().add(budget.getRemaining() < 0 ? "budget-remaining-danger" : "budget-remaining");
        card.getChildren().addAll(header, spent, bar, remaining);
        return card;
    }
}
