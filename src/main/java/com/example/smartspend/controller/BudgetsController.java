package com.example.smartspend.controller;

import com.example.smartspend.model.Budget;
import com.example.smartspend.service.BudgetService;
import com.example.smartspend.utils.CurrencyFormatter;
import com.example.smartspend.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class BudgetsController {
    @FXML private FlowPane budgetGrid;

    private final BudgetService budgetService = new BudgetService();

    @FXML
    private void initialize() {
        loadBudgetCards();
    }

    private void loadBudgetCards() {
        if (budgetGrid == null) return;
        budgetGrid.getChildren().clear();
        int userId = SessionManager.getCurrentUserId();
        List<Budget> budgets = budgetService.getCurrentMonthBudgets(userId);
        if (budgets.isEmpty()) {
            Label empty = new Label("Chưa có ngân sách tháng này. Tài khoản demo sẽ tự có dữ liệu mẫu; tài khoản mới có thể bắt đầu bằng cách thêm giao dịch chi tiêu.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-padding: 20;");
            budgetGrid.getChildren().add(empty);
            return;
        }
        for (Budget budget : budgets) {
            budgetGrid.getChildren().add(createBudgetCard(budget));
        }
    }

    private VBox createBudgetCard(Budget budget) {
        VBox card = new VBox(10);
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 16, 0, 0, 5);");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label(budget.getProgress() >= 0.9 ? "⚠️" : "💼");
        Label name = new Label(budget.getCategoryName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label pct = new Label((int) Math.round(budget.getProgress() * 100) + "%");
        pct.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (budget.getProgress() >= 0.9 ? "#dc2626" : "#2563eb") + ";");
        header.getChildren().addAll(icon, name, spacer, pct);

        Label spent = new Label(CurrencyFormatter.format(budget.getSpent()) + " / " + CurrencyFormatter.format(budget.getAmount()));
        spent.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        ProgressBar bar = new ProgressBar(budget.getProgress());
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(9);

        Label remaining = new Label("Còn lại: " + CurrencyFormatter.format(budget.getRemaining()));
        remaining.setStyle("-fx-text-fill: " + (budget.getRemaining() < 0 ? "#dc2626" : "#64748b") + ";");
        card.getChildren().addAll(header, spent, bar, remaining);
        return card;
    }
}
