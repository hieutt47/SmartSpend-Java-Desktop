package com.example.smartspend.controller;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.EmailService;
import com.example.smartspend.service.FinancialAdvisorService;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.AlertHelper;
import com.example.smartspend.utils.CurrencyFormatter;
import com.example.smartspend.utils.HostInfo;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SmartCoachController {
    @FXML private Label lblHost;
    @FXML private Label lblNetBalance;
    @FXML private Label lblSavingsRate;
    @FXML private Label lblRiskLevel;
    @FXML private Label lblCoachSummary;
    @FXML private VBox adviceList;
    @FXML private VBox categoryList;
    @FXML private TextArea txtQuestion;
    @FXML private Label lblCoachAnswer;
    @FXML private Label lblProviderStatus;

    private final TransactionService transactionService = new TransactionService();
    private final FinancialAdvisorService advisorService = new FinancialAdvisorService();
    private List<Transaction> currentTransactions = List.of();

    @FXML
    private void initialize() {
        lblHost.setText("Host: " + HostInfo.HOST_NAME + " · " + HostInfo.HOST_EMAIL);
        updateProviderStatus();
        refreshData();
    }

    @FXML
    private void handleRefresh() {
        refreshData();
        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Smart Coach", "Đã cập nhật phân tích từ dữ liệu mới nhất.");
    }

    @FXML
    private void handleAskCoach() {
        String question = txtQuestion == null ? "" : txtQuestion.getText();
        String answer = advisorService.answerQuestion(question, currentTransactions);
        lblCoachAnswer.setText(answer);
        updateProviderStatus();
    }

    @FXML
    private void handleEmailReport() {
        String to = LoginController.getLoggedInUserEmail();
        if (to == null || to.isBlank()) to = HostInfo.HOST_EMAIL;
        String report = advisorService.buildEmailReport(currentTransactions);
        boolean sent = new EmailService().sendPlainText(to, "SmartSpend AI Coach Report", report);
        if (sent) {
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Đã gửi report", "SmartSpend đã gửi AI Coach report tới: " + to);
        } else {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Chưa gửi được email", "SMTP chưa cấu hình hoặc gửi thất bại. Vào Email notifications để cấu hình Gmail App Password.\n\nPreview report:\n" + report);
        }
    }

    private void refreshData() {
        currentTransactions = transactionService.getAllTransactions();
        render(currentTransactions);
        updateProviderStatus();
    }

    private void render(List<Transaction> transactions) {
        FinancialAdvisorService.Snapshot snapshot = advisorService.buildSnapshot(transactions);
        lblNetBalance.setText(CurrencyFormatter.format(snapshot.net()));
        lblSavingsRate.setText(String.format(java.util.Locale.US, "%.1f%%", snapshot.savingsRate()));
        lblRiskLevel.setText(riskLabel(snapshot));
        lblCoachSummary.setText(advisorService.buildSummary(transactions));
        if (lblCoachAnswer != null) {
            lblCoachAnswer.setText("Hỏi Smart Coach một câu cụ thể để nhận tư vấn theo dữ liệu hiện tại. Ví dụ: 'Tháng này tôi nên tiết kiệm bao nhiêu?'");
        }

        adviceList.getChildren().clear();
        categoryList.getChildren().clear();
        advisorService.buildActionPlan(transactions).forEach(text -> adviceList.getChildren().add(adviceCard(text)));
        buildCategoryBreakdown(snapshot.expenseByCategory());
    }


    private void updateProviderStatus() {
        if (lblProviderStatus != null) {
            lblProviderStatus.setText(advisorService.providerStatus());
        }
    }

    private String riskLabel(FinancialAdvisorService.Snapshot s) {
        if (s.income() <= 0 && s.expense() > 0) return "High risk";
        if (s.expense() > s.income()) return "Overspending";
        if (s.savingsRate() >= 30) return "Excellent";
        if (s.savingsRate() >= 15) return "Healthy";
        return "Needs attention";
    }

    private VBox adviceCard(String text) {
        VBox card = new VBox(6);
        card.getStyleClass().add("advice-card");
        Label title = new Label("Action item");
        title.getStyleClass().add("advice-title");
        Label body = new Label(text);
        body.getStyleClass().add("advice-body");
        body.setWrapText(true);
        card.getChildren().addAll(title, body);
        return card;
    }

    private void buildCategoryBreakdown(Map<String, Double> expenseByCategory) {
        double totalExpense = expenseByCategory.values().stream().mapToDouble(Double::doubleValue).sum();
        if (expenseByCategory.isEmpty()) {
            Label empty = new Label("Chưa có dữ liệu chi tiêu để phân tích danh mục.");
            empty.getStyleClass().add("muted-text");
            categoryList.getChildren().add(empty);
            return;
        }
        expenseByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(6)
                .forEach(entry -> categoryList.getChildren().add(categoryRow(entry.getKey(), entry.getValue(), totalExpense)));
    }

    private HBox categoryRow(String category, double amount, double total) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("category-row");

        VBox text = new VBox(4);
        Label name = new Label(category);
        name.getStyleClass().add("category-name");
        ProgressBar bar = new ProgressBar(total <= 0 ? 0 : amount / total);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add("coach-progress");
        text.getChildren().addAll(name, bar);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label value = new Label(CurrencyFormatter.format(amount));
        value.getStyleClass().add("category-value");
        row.getChildren().addAll(text, value);
        return row;
    }
}
