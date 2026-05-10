package com.example.smartspend.controller;

import com.example.smartspend.dao.CategoryDAOImpl;
import com.example.smartspend.model.Category;
import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.service.BudgetService;
import com.example.smartspend.service.TransactionService;
import com.example.smartspend.utils.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

public class BudgetsController implements Initializable {

    // --- FXML bindings (sẽ thêm fx:id vào FXML ở bước 6) ---
    @FXML private javafx.scene.layout.FlowPane budgetGrid;
    @FXML private Label lblTotalBudget;
    @FXML private Label lblBudgetProgressText;
    @FXML private Label lblSpent;
    @FXML private ProgressBar heroProgressBar;
    @FXML private Label lblRemaining;
    @FXML private Label lblInsightText;
    @FXML private Label lblMonthLabel;

    // --- Services ---
    private final BudgetService    budgetService      = new BudgetService();
    private final TransactionService transactionService = new TransactionService();
    private final CategoryDAOImpl  categoryDAO        = new CategoryDAOImpl();

    // --- State ---
    private int currentMonth;
    private int currentYear;

    // --- Icon & màu theo tên danh mục ---
    private static final Map<String, String> CAT_ICONS = new HashMap<>() {{
        put("Ăn uống",          "🍽️");
        put("Mua sắm",          "🛍️");
        put("Nhà ở & Hóa đơn",  "🏠");
        put("Di chuyển",        "🚗");
        put("Sức khỏe",         "❤️");
        put("Giải trí",         "🎬");
        put("Giáo dục",         "📚");
        put("Chi tiêu khác",    "💳");
    }};

    private static final Map<String, String> CAT_COLORS = new HashMap<>() {{
        put("Ăn uống",          "#16a34a");
        put("Mua sắm",          "#2563eb");
        put("Nhà ở & Hóa đơn",  "#dc2626");
        put("Di chuyển",        "#d97706");
        put("Sức khỏe",         "#16a34a");
        put("Giải trí",         "#7c3aed");
        put("Giáo dục",         "#0891b2");
        put("Chi tiêu khác",    "#64748b");
    }};

    // ─────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LocalDate now = LocalDate.now();
        currentMonth = now.getMonthValue();
        currentYear  = now.getYear();
        loadData();
    }

    // ─────────────────────────────────────────────────
    //  LOAD ALL DATA
    // ─────────────────────────────────────────────────
    private void loadData() {
        try {
            // Cập nhật label tháng/năm
            String monthName = Month.of(currentMonth)
                    .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("vi-VN"));
            if (lblMonthLabel != null)
                lblMonthLabel.setText(monthName + " " + currentYear);

            List<Transaction> allTx = transactionService.getAllTransactions();

            // Tổng chi tiêu trong tháng
            double totalSpent = allTx.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE
                            && t.getDate() != null
                            && t.getDate().getMonthValue() == currentMonth
                            && t.getDate().getYear() == currentYear)
                    .mapToDouble(Transaction::getAmount).sum();

            // Lấy ngân sách các danh mục trong tháng
            Map<Integer, Double> budgetMap = budgetService.getBudgetsForMonth(currentMonth, currentYear);
            double totalBudget = budgetMap.values().stream().mapToDouble(Double::doubleValue).sum();

            updateHeroCard(totalBudget, totalSpent);
            buildBudgetCards(allTx, budgetMap);
            updateInsight(totalBudget, totalSpent);

        } catch (Exception e) {
            System.err.println("BudgetsController.loadData error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────
    //  CẬP NHẬT HERO CARD
    // ─────────────────────────────────────────────────
    private void updateHeroCard(double totalBudget, double totalSpent) {
        double remaining = totalBudget - totalSpent;
        double progress  = totalBudget > 0 ? Math.min(totalSpent / totalBudget, 1.0) : 0.0;
        int    pct       = (int)(progress * 100);

        if (lblTotalBudget       != null) lblTotalBudget.setText(CurrencyFormatter.format(totalBudget));
        if (lblBudgetProgressText!= null) lblBudgetProgressText.setText("Budget Progress (" + pct + "%)");
        if (lblSpent             != null) lblSpent.setText(CurrencyFormatter.format(totalSpent) + " Spent");
        if (heroProgressBar      != null) heroProgressBar.setProgress(progress);
        if (lblRemaining         != null) lblRemaining.setText(CurrencyFormatter.format(remaining));
    }

    // ─────────────────────────────────────────────────
    //  BUILD BUDGET CARDS (FlowPane)
    // ─────────────────────────────────────────────────
    private void buildBudgetCards(List<Transaction> allTx, Map<Integer, Double> budgetMap) {
        if (budgetGrid == null) return;
        budgetGrid.getChildren().clear();

        List<Category> categories = categoryDAO.getAllCategories();

        categories.stream()
                .filter(c -> c.getType() == TransactionType.EXPENSE)
                .forEach(cat -> {
                    double spent = allTx.stream()
                            .filter(t -> t.getType() == TransactionType.EXPENSE
                                    && t.getId() == cat.getId()
                                    && t.getDate() != null
                                    && t.getDate().getMonthValue() == currentMonth
                                    && t.getDate().getYear() == currentYear)
                            .mapToDouble(Transaction::getAmount).sum();

                    double budget   = budgetMap.getOrDefault(cat.getId(), 0.0);
                    double progress = budget > 0
                            ? Math.min(spent / budget, 1.0)
                            : (spent > 0 ? 1.0 : 0.0);

                    VBox card = buildCategoryCard(cat, spent, budget, progress);
                    budgetGrid.getChildren().add(card);
                });
    }

    private VBox buildCategoryCard(Category cat, double spent, double budget, double progress) {
        VBox card = new VBox(12);
        card.setPrefWidth(270);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 8, 0, 0, 2); " +
                        "-fx-padding: 18;");

        String icon  = CAT_ICONS.getOrDefault(cat.getName(), "💰");
        String color = CAT_COLORS.getOrDefault(cat.getName(), "#2563eb");

        // Header: icon + tên
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px; -fx-background-color: " + color + "22; " +
                "-fx-background-radius: 8; -fx-padding: 6 8;");
        Label nameLbl = new Label(cat.getName());
        nameLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        header.getChildren().addAll(iconLbl, nameLbl);

        // Số tiền đã chi
        Label spentLbl = new Label(CurrencyFormatter.format(spent));
        spentLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: " + color + ";");

        // Progress bar — đỏ nếu vượt ngân sách, vàng nếu >80%, xanh nếu bình thường
        ProgressBar pb = new ProgressBar(progress);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(8);
        String barAccent = progress >= 1.0 ? "#ef4444" : (progress >= 0.8 ? "#f59e0b" : "#16a34a");
        pb.setStyle("-fx-accent: " + barAccent + "; " +
                "-fx-background-color: #f1f5f9; -fx-background-radius: 4; -fx-border-radius: 4;");

        // Footer: thông tin hạn mức + nút edit
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);

        String budgetInfo = budget > 0
                ? (int)(progress * 100) + "% của " + CurrencyFormatter.format(budget)
                : "Chưa đặt ngân sách";
        Label limitLbl = new Label(budgetInfo);
        limitLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button(budget > 0 ? "✏ Sửa" : "+ Đặt");
        editBtn.setStyle(
                "-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; -fx-font-size: 11px; " +
                        "-fx-font-weight: 700; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openSetBudgetDialog(cat, budget));

        footer.getChildren().addAll(limitLbl, spacer, editBtn);

        card.getChildren().addAll(header, spentLbl, pb, footer);
        return card;
    }

    // ─────────────────────────────────────────────────
    //  DIALOG ĐẶT NGÂN SÁCH
    // ─────────────────────────────────────────────────
    private void openSetBudgetDialog(Category cat, double currentBudget) {
        TextInputDialog dialog = new TextInputDialog(
                currentBudget > 0 ? String.valueOf((long) currentBudget) : "");
        dialog.setTitle("Đặt ngân sách");
        dialog.setHeaderText("📋 Danh mục: " + cat.getName());
        dialog.setContentText("Nhập số tiền ngân sách:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                double amount = Double.parseDouble(input.trim().replace(",", ""));
                if (amount < 0) throw new NumberFormatException("Âm");
                budgetService.setCategoryBudget(cat.getId(), amount, currentMonth, currentYear);
                loadData(); // Reload để cập nhật UI
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Vui lòng nhập số tiền hợp lệ (VD: 500000)", ButtonType.OK)
                        .showAndWait();
            }
        });
    }

    // ─────────────────────────────────────────────────
    //  INSIGHT TEXT
    // ─────────────────────────────────────────────────
    private void updateInsight(double totalBudget, double totalSpent) {
        if (lblInsightText == null) return;
        if (totalBudget == 0) {
            lblInsightText.setText(
                    "Chưa có ngân sách nào được đặt. Bấm \"+ Đặt\" trên từng danh mục để SmartSpend " +
                            "theo dõi và tối ưu chi tiêu cho bạn.");
            return;
        }
        double remaining = totalBudget - totalSpent;
        double pct = (totalSpent / totalBudget) * 100;
        if (pct >= 100) {
            lblInsightText.setText("⚠️ Bạn đã vượt ngân sách " + String.format("%.0f", pct - 100) +
                    "%! Hãy xem xét cắt giảm chi tiêu các tháng tiếp theo.");
        } else if (pct >= 80) {
            lblInsightText.setText("⚡ Bạn đã dùng " + String.format("%.0f", pct) +
                    "% ngân sách. Còn " + CurrencyFormatter.format(remaining) + " — chi tiêu cẩn thận nhé!");
        } else {
            lblInsightText.setText("✅ Tốt lắm! Bạn còn " + CurrencyFormatter.format(remaining) +
                    " trong ngân sách tháng này. Đang pacing " + String.format("%.0f", 100 - pct) +
                    "% tốt hơn kế hoạch.");
        }
    }

    // ─────────────────────────────────────────────────
    //  FXML BUTTON HANDLERS
    // ─────────────────────────────────────────────────

    @FXML
    private void handlePrevMonth() {
        currentMonth--;
        if (currentMonth < 1) { currentMonth = 12; currentYear--; }
        loadData();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth++;
        if (currentMonth > 12) { currentMonth = 1; currentYear++; }
        loadData();
    }

    @FXML
    private void handleCreateCategory() {
        List<Category> cats = categoryDAO.getAllCategories();
        List<String> catNames = new ArrayList<>();
        Map<String, Category> nameTocat = new HashMap<>();

        cats.stream()
                .filter(c -> c.getType() == TransactionType.EXPENSE)
                .forEach(c -> { catNames.add(c.getName()); nameTocat.put(c.getName(), c); });

        if (catNames.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Chưa có danh mục nào trong database!", ButtonType.OK)
                    .showAndWait();
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(catNames.get(0), catNames);
        dialog.setTitle("Đặt ngân sách mới");
        dialog.setHeaderText("Chọn danh mục để đặt ngân sách tháng " + currentMonth + "/" + currentYear);
        dialog.setContentText("Danh mục:");

        dialog.showAndWait().ifPresent(name -> {
            Category cat = nameTocat.get(name);
            if (cat != null) openSetBudgetDialog(cat, 0);
        });
    }
}