package com.example.smartspend.controller;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.AdvisorMode;
import com.example.smartspend.service.EmailService;
import com.example.smartspend.service.FinancialAdvisorService;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.AlertHelper;
import com.example.smartspend.utils.CurrencyFormatter;
import com.example.smartspend.utils.HostInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SmartCoachController {
    @FXML private Label lblHost;
    @FXML private Label lblProviderStatus;
    @FXML private Label lblNetBalance;
    @FXML private Label lblSavingsRate;
    @FXML private Label lblRiskLevel;
    @FXML private Label lblCoachSummary;
    @FXML private Label lblCoachAnswer;
    @FXML private Label lblTaskStatus;
    @FXML private TextArea txtQuestion;
    @FXML private VBox adviceList;
    @FXML private VBox categoryList;
    @FXML private ComboBox<AdvisorMode> cbMode;
    @FXML private ComboBox<String> cbModel;
    @FXML private CheckBox chkUseLocalAi;
    @FXML private ProgressIndicator piThinking;
    @FXML private Button btnAsk;

    private final TransactionService transactionService = new TransactionService();
    private final FinancialAdvisorService advisorService = new FinancialAdvisorService();
    private List<Transaction> currentTransactions = List.of();

    @FXML
    private void initialize() {
        lblHost.setText("Host: " + HostInfo.HOST_NAME + " · " + HostInfo.HOST_EMAIL);
        cbMode.getItems().setAll(AdvisorMode.values());
        cbMode.setValue(AdvisorMode.COACH);
        cbMode.setOnAction(event -> {
            AdvisorMode mode = cbMode.getValue();
            if (mode != null && (txtQuestion.getText() == null || txtQuestion.getText().isBlank())) {
                txtQuestion.setText(mode.getDefaultQuestion());
            }
        });
        chkUseLocalAi.setSelected(advisorService.isLocalAiEnabled());
        chkUseLocalAi.setOnAction(event -> {
            advisorService.setLocalAiEnabled(chkUseLocalAi.isSelected());
            refreshRuntimeAsync();
        });
        cbModel.setOnAction(event -> {
            if (cbModel.getValue() != null && !cbModel.getValue().isBlank()) {
                advisorService.chooseModel(cbModel.getValue());
                lblProviderStatus.setText("Đã chọn model: " + cbModel.getValue());
            }
        });
        txtQuestion.setText(AdvisorMode.COACH.getDefaultQuestion());
        refreshData();
        refreshRuntimeAsync();
    }

    @FXML
    private void handleRefresh() {
        refreshData();
        refreshRuntimeAsync();
    }

    @FXML
    private void handleRefreshAi() {
        refreshRuntimeAsync();
    }

    @FXML
    private void handleAskCoach() {
        AdvisorMode mode = cbMode.getValue() == null ? AdvisorMode.COACH : cbMode.getValue();
        String question = txtQuestion.getText() == null ? "" : txtQuestion.getText().trim();
        runAdviceTask(mode, question);
    }

    @FXML private void handleQuickSaving() {
        cbMode.setValue(AdvisorMode.SAVINGS_PLANNER);
        txtQuestion.setText(AdvisorMode.SAVINGS_PLANNER.getDefaultQuestion());
        handleAskCoach();
    }

    @FXML private void handleQuickBudget() {
        cbMode.setValue(AdvisorMode.BUDGET_PLANNER);
        txtQuestion.setText(AdvisorMode.BUDGET_PLANNER.getDefaultQuestion());
        handleAskCoach();
    }

    @FXML private void handleQuickRisk() {
        cbMode.setValue(AdvisorMode.RISK_GUARD);
        txtQuestion.setText(AdvisorMode.RISK_GUARD.getDefaultQuestion());
        handleAskCoach();
    }

    @FXML private void handleFullReview() {
        cbMode.setValue(AdvisorMode.ANALYST);
        txtQuestion.setText(AdvisorMode.ANALYST.getDefaultQuestion());
        handleAskCoach();
    }

    @FXML
    private void handleEmailReport() {
        String to = LoginController.getLoggedInUserEmail();
        if (to == null || to.isBlank()) to = HostInfo.HOST_EMAIL;
        String report = advisorService.buildEmailReport(currentTransactions);
        boolean sent = new EmailService().sendPlainText(to, "SmartSpend Financial Coach Report", report);
        if (sent) {
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Đã gửi report", "Báo cáo đã được gửi tới: " + to);
        } else {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Chưa gửi được email",
                    "SMTP chưa được cấu hình hoặc gửi thất bại. Hãy mở Email notifications để kiểm tra cấu hình Gmail App Password.\n\nPreview report:\n" + report);
        }
    }

    private void runAdviceTask(AdvisorMode mode, String question) {
        setBusy(true, "Đang phân tích dữ liệu và tạo tư vấn...");
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return advisorService.answerQuestion(mode, question, currentTransactions);
            }
        };
        task.setOnSucceeded(event -> {
            lblCoachAnswer.setText(task.getValue());
            setBusy(false, "Hoàn tất phân tích.");
            refreshRuntimeAsync();
        });
        task.setOnFailed(event -> {
            lblCoachAnswer.setText("Không thể tạo phản hồi AI lúc này. Smart Coach vẫn có thể dùng các thẻ phân tích offline ở bên dưới.");
            setBusy(false, "Không thể kết nối AI local.");
        });
        Thread worker = new Thread(task, "smartspend-ai-advice");
        worker.setDaemon(true);
        worker.start();
    }

    private void refreshRuntimeAsync() {
        lblProviderStatus.setText("Đang kiểm tra AI local...");
        Task<FinancialAdvisorService.RuntimeInfo> task = new Task<>() {
            @Override
            protected FinancialAdvisorService.RuntimeInfo call() {
                return advisorService.discoverRuntime();
            }
        };
        task.setOnSucceeded(event -> {
            FinancialAdvisorService.RuntimeInfo info = task.getValue();
            lblProviderStatus.setText(info.statusText());
            String previouslySelected = cbModel.getValue();
            cbModel.getItems().setAll(info.models());
            if (info.aiAvailable()) {
                cbModel.setDisable(false);
                cbModel.setValue(info.models().contains(previouslySelected) ? previouslySelected : info.selectedModel());
            } else {
                cbModel.setDisable(true);
                cbModel.setPromptText("Chưa có model Ollama");
            }
        });
        task.setOnFailed(event -> lblProviderStatus.setText("Smart Coach offline đang bật · Không thể kiểm tra Ollama."));
        Thread worker = new Thread(task, "smartspend-ai-discovery");
        worker.setDaemon(true);
        worker.start();
    }

    private void setBusy(boolean busy, String status) {
        piThinking.setVisible(busy);
        piThinking.setManaged(busy);
        btnAsk.setDisable(busy);
        txtQuestion.setDisable(busy);
        lblTaskStatus.setText(status);
    }

    private void refreshData() {
        currentTransactions = transactionService.getAllTransactions();
        render(currentTransactions);
    }

    private void render(List<Transaction> transactions) {
        FinancialAdvisorService.Snapshot snapshot = advisorService.buildSnapshot(transactions);
        lblNetBalance.setText(CurrencyFormatter.format(snapshot.net()));
        lblSavingsRate.setText(String.format(java.util.Locale.US, "%.1f%%", snapshot.savingsRate()));
        lblRiskLevel.setText(riskLabel(snapshot));
        lblCoachSummary.setText(advisorService.buildSummary(transactions));
        lblCoachAnswer.setText("Chọn chuyên gia và đặt câu hỏi. Smart Coach dùng dữ liệu giao dịch hiện tại của bạn để tư vấn.");
        adviceList.getChildren().clear();
        categoryList.getChildren().clear();
        advisorService.buildActionPlan(transactions).forEach(text -> adviceList.getChildren().add(adviceCard(text)));
        buildCategoryBreakdown(snapshot.expenseByCategory());
    }

    private String riskLabel(FinancialAdvisorService.Snapshot snapshot) {
        if (snapshot.income() <= 0 && snapshot.expense() > 0) return "Cao";
        if (snapshot.expense() > snapshot.income()) return "Vượt thu";
        if (snapshot.savingsRate() >= 30) return "Tốt";
        if (snapshot.savingsRate() >= 15) return "Ổn định";
        return "Cần chú ý";
    }

    private VBox adviceCard(String text) {
        VBox card = new VBox(6);
        card.getStyleClass().add("advice-card");
        Label title = new Label("Hành động đề xuất");
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
