package com.example.smartspend.service;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.AdvisorMode;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.utils.CurrencyFormatter;
import com.example.smartspend.utils.HostInfo;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Offline-first financial intelligence engine.
 *
 * <p>The deterministic analysis is always available. When an Ollama model is
 * installed locally, SmartSpend automatically upgrades conversational answers
 * through Ollama without sending personal transactions to a cloud service.</p>
 */
public class FinancialAdvisorService {
    private static final String OLLAMA_BASE = "http://localhost:11434/api";
    private static final int DISCOVERY_TIMEOUT_SECONDS = 2;
    private static final int CHAT_TIMEOUT_SECONDS = 45;
    private static final List<String> MODEL_PRIORITY = List.of(
            "llama3.2", "llama3.1", "qwen2.5", "gemma3", "gemma2", "mistral", "phi3"
    );
    private static volatile boolean attemptedOllamaStart;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(DISCOVERY_TIMEOUT_SECONDS))
            .build();
    private final AiPreferencesService preferences = new AiPreferencesService();

    public Snapshot buildSnapshot(List<Transaction> transactions) {
        List<Transaction> safe = transactions == null ? List.of() : transactions;
        double income = total(safe, TransactionType.INCOME);
        double expense = total(safe, TransactionType.EXPENSE);
        double net = income - expense;
        double savingsRate = income <= 0 ? 0 : net * 100.0 / income;
        Map<String, Double> expenseByCategory = expenseByCategory(safe);
        String topCategory = expenseByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Chưa có dữ liệu");
        double topCategoryAmount = expenseByCategory.getOrDefault(topCategory, 0.0);
        return new Snapshot(income, expense, net, savingsRate, topCategory, topCategoryAmount,
                expenseByCategory, safe.size());
    }

    public String buildSummary(List<Transaction> transactions) {
        Snapshot s = buildSnapshot(transactions);
        if (s.transactionCount() == 0) {
            return "Bạn chưa có dữ liệu giao dịch. Hãy thêm khoản thu và một vài khoản chi để Smart Coach phân tích chính xác hơn.";
        }
        if (s.income() <= 0 && s.expense() > 0) {
            return "Hiện chưa ghi nhận thu nhập nhưng đã có chi tiêu " + CurrencyFormatter.format(s.expense())
                    + ". Hãy bổ sung nguồn thu hoặc siết lại khoản chi chưa cần thiết.";
        }
        if (s.net() < 0) {
            return "Dòng tiền đang âm " + CurrencyFormatter.format(Math.abs(s.net()))
                    + ". Rủi ro chính đến từ " + s.topCategory() + "; nên ưu tiên đưa ngân sách về mức hòa vốn.";
        }
        return "Dòng tiền đang dương " + CurrencyFormatter.format(s.net()) + " với tỷ lệ tiết kiệm "
                + pct(s.savingsRate()) + ". Danh mục chi lớn nhất hiện là " + s.topCategory() + ".";
    }

    public List<String> buildActionPlan(List<Transaction> transactions) {
        Snapshot s = buildSnapshot(transactions);
        List<String> plan = new ArrayList<>();
        if (s.transactionCount() == 0) {
            plan.add("Nhập khoản thu nhập đầu tiên và ít nhất ba giao dịch chi tiêu để bắt đầu phân tích.");
            plan.add("Tạo ngân sách cho Ăn uống, Di chuyển và Nhà ở & Hóa đơn.");
            plan.add("Quay lại Smart Coach sau một tuần để nhận đánh giá dòng tiền thực tế.");
            return plan;
        }
        if (s.net() < 0) {
            plan.add("Cắt tối thiểu " + CurrencyFormatter.format(Math.abs(s.net())) + " để đưa dòng tiền về hòa vốn.");
        } else {
            double emergencyFund = Math.max(0, s.net() * 0.5);
            plan.add("Chuyển " + CurrencyFormatter.format(emergencyFund) + " vào quỹ dự phòng hoặc mục tiêu tiết kiệm ngay trong tháng này.");
        }
        if (s.savingsRate() < 15) {
            plan.add("Đặt mục tiêu tỷ lệ tiết kiệm 15% trước; ưu tiên giảm các khoản mua sắm linh hoạt.");
        } else if (s.savingsRate() < 30) {
            plan.add("Tỷ lệ tiết kiệm ổn; thử nâng lên 30% trong ba tháng bằng ngân sách theo danh mục.");
        } else {
            plan.add("Tỷ lệ tiết kiệm tốt; phân bổ phần dư cho quỹ khẩn cấp, học tập và mục tiêu dài hạn.");
        }
        if (s.topCategoryAmount() > 0 && s.expense() > 0) {
            double share = s.topCategoryAmount() * 100 / s.expense();
            plan.add("Theo dõi " + s.topCategory() + " vì đang chiếm " + pct(share) + " tổng chi tiêu; đặt giới hạn cảnh báo ở mức 80% budget.");
        }
        return plan;
    }

    public String answerQuestion(AdvisorMode mode, String question, List<Transaction> transactions) {
        AdvisorMode safeMode = mode == null ? AdvisorMode.COACH : mode;
        String normalized = question == null ? "" : question.trim();
        if (normalized.isBlank()) normalized = safeMode.getDefaultQuestion();
        if (preferences.isLocalAiEnabled()) {
            Optional<String> generated = queryOllama(safeMode, normalized, transactions);
            if (generated.isPresent()) return generated.get();
        }
        return offlineAnswer(safeMode, normalized, transactions);
    }

    public String answerQuestion(String question, List<Transaction> transactions) {
        return answerQuestion(AdvisorMode.COACH, question, transactions);
    }

    public String buildBudgetSuggestion(List<Transaction> transactions) {
        Snapshot s = buildSnapshot(transactions);
        if (s.expenseByCategory().isEmpty()) {
            return "Chưa đủ dữ liệu chi tiêu để lập ngân sách. Hãy thêm các khoản expense trước.";
        }
        StringBuilder sb = new StringBuilder("Ngân sách đề xuất cho tháng tới:\n");
        s.expenseByCategory().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(8)
                .forEach(entry -> {
                    double suggested = Math.max(50, entry.getValue() * 0.95);
                    sb.append("- ").append(entry.getKey()).append(": ")
                            .append(CurrencyFormatter.format(suggested)).append("\n");
                });
        return sb.append("\nMục tiêu: giữ tổng chi thấp hơn thu nhập và bảo vệ khoản tiết kiệm.").toString().trim();
    }

    public String buildEmailReport(List<Transaction> transactions) {
        Snapshot s = buildSnapshot(transactions);
        return "SmartSpend Financial Coach Report\n"
                + "Host: " + HostInfo.HOST_NAME + " <" + HostInfo.HOST_EMAIL + ">\n\n"
                + "Tổng thu: " + CurrencyFormatter.format(s.income()) + "\n"
                + "Tổng chi: " + CurrencyFormatter.format(s.expense()) + "\n"
                + "Số dư ròng: " + CurrencyFormatter.format(s.net()) + "\n"
                + "Tỷ lệ tiết kiệm: " + pct(s.savingsRate()) + "\n"
                + "Chi tiêu lớn nhất: " + s.topCategory() + " (" + CurrencyFormatter.format(s.topCategoryAmount()) + ")\n\n"
                + buildSummary(transactions) + "\n\nKế hoạch hành động:\n- " + String.join("\n- ", buildActionPlan(transactions));
    }

    public RuntimeInfo discoverRuntime() {
        if (!preferences.isLocalAiEnabled()) {
            return new RuntimeInfo(false, "", List.of(), "Smart Coach offline đang bật · AI local đã tắt trong cài đặt.");
        }
        List<String> models = queryInstalledModels();
        if (models.isEmpty()) {
            tryStartOllama();
            models = queryInstalledModels();
        }
        if (models.isEmpty()) {
            return new RuntimeInfo(false, "", List.of(), "Smart Coach offline đang bật · Cài Ollama + model để có phản hồi AI tự nhiên hơn.");
        }
        String selected = resolveSelectedModel(models);
        return new RuntimeInfo(true, selected, models, "Ollama local đang hoạt động · Model: " + selected);
    }

    public String providerStatus() {
        return discoverRuntime().statusText();
    }

    public List<String> availableModels() {
        return discoverRuntime().models();
    }

    public void chooseModel(String model) {
        preferences.setPreferredModel(model);
    }

    public boolean isLocalAiEnabled() {
        return preferences.isLocalAiEnabled();
    }

    public void setLocalAiEnabled(boolean enabled) {
        preferences.setLocalAiEnabled(enabled);
    }

    private String offlineAnswer(AdvisorMode mode, String question, List<Transaction> transactions) {
        Snapshot s = buildSnapshot(transactions);
        return switch (mode) {
            case ANALYST -> buildSummary(transactions) + "\n\nPhân tích chi tiêu:\n- "
                    + categoryHighlights(s) + "\n- Tổng số giao dịch đã phân tích: " + s.transactionCount() + ".";
            case BUDGET_PLANNER -> buildBudgetSuggestion(transactions);
            case RISK_GUARD -> riskAssessment(s);
            case SAVINGS_PLANNER -> savingsPlan(s);
            case COACH -> contextualOfflineAnswer(question, transactions, s);
        };
    }

    private String contextualOfflineAnswer(String question, List<Transaction> transactions, Snapshot s) {
        String lower = question.toLowerCase(Locale.ROOT);
        if (lower.contains("ngân sách") || lower.contains("budget")) return buildBudgetSuggestion(transactions);
        if (lower.contains("rủi ro") || lower.contains("nguy") || lower.contains("vượt")) return riskAssessment(s);
        if (lower.contains("tiết kiệm") || lower.contains("save")) return savingsPlan(s);
        return buildSummary(transactions) + "\n\nƯu tiên đề xuất:\n- " + String.join("\n- ", buildActionPlan(transactions));
    }

    private String riskAssessment(Snapshot s) {
        if (s.transactionCount() == 0) return "Chưa đủ dữ liệu để đánh giá rủi ro.";
        if (s.net() < 0) {
            return "Mức rủi ro: CAO. Dòng tiền đang âm " + CurrencyFormatter.format(Math.abs(s.net()))
                    + ". Hãy giảm " + s.topCategory() + " và hoãn chi tiêu không thiết yếu.";
        }
        double share = s.expense() <= 0 ? 0 : s.topCategoryAmount() * 100 / s.expense();
        if (share >= 45) {
            return "Mức rủi ro: CẦN THEO DÕI. " + s.topCategory() + " chiếm " + pct(share)
                    + " tổng chi. Hãy đặt budget và cảnh báo trước khi vượt mức.";
        }
        return "Mức rủi ro: THẤP. Dòng tiền vẫn dương; tiếp tục kiểm tra budget và quỹ dự phòng định kỳ.";
    }

    private String savingsPlan(Snapshot s) {
        if (s.income() <= 0) return "Chưa có dữ liệu thu nhập để lập kế hoạch tiết kiệm.";
        double conservative = Math.max(0, s.net() * 0.4);
        double target = Math.max(0, s.net() * 0.6);
        return "Kế hoạch tiết kiệm gợi ý:\n- Mức tối thiểu an toàn: " + CurrencyFormatter.format(conservative)
                + "/tháng.\n- Mục tiêu tốt: " + CurrencyFormatter.format(target)
                + "/tháng.\n- Ưu tiên hoàn thiện quỹ khẩn cấp trước khi tăng chi tiêu linh hoạt.";
    }

    private String categoryHighlights(Snapshot s) {
        if (s.expenseByCategory().isEmpty()) return "chưa có danh mục chi tiêu";
        return s.expenseByCategory().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(entry -> entry.getKey() + " " + CurrencyFormatter.format(entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private Optional<String> queryOllama(AdvisorMode mode, String question, List<Transaction> transactions) {
        RuntimeInfo runtime = discoverRuntime();
        if (!runtime.aiAvailable()) return Optional.empty();
        Snapshot s = buildSnapshot(transactions);
        String system = "Bạn là SmartSpend AI, trợ lý tài chính cá nhân. Trả lời bằng tiếng Việt, rõ ràng, ngắn gọn, "
                + "dùng số liệu được cung cấp, không bịa dữ liệu, không đưa lời khuyên đầu tư rủi ro. "
                + "Vai trò hiện tại: " + mode.getLabel() + ".";
        String user = "Dữ liệu tài chính: tổng thu=" + CurrencyFormatter.format(s.income())
                + ", tổng chi=" + CurrencyFormatter.format(s.expense())
                + ", số dư=" + CurrencyFormatter.format(s.net())
                + ", tỷ lệ tiết kiệm=" + pct(s.savingsRate())
                + ", nhóm chi lớn nhất=" + s.topCategory() + " (" + CurrencyFormatter.format(s.topCategoryAmount()) + ").\n"
                + "Top categories: " + categoryHighlights(s) + ".\nCâu hỏi: " + question;
        String json = "{\"model\":\"" + escapeJson(runtime.selectedModel()) + "\",\"stream\":false,\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(system) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(user) + "\"}]}";
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(OLLAMA_BASE + "/chat"))
                    .timeout(Duration.ofSeconds(CHAT_TIMEOUT_SECONDS))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) return Optional.empty();
            String content = extractJsonString(response.body(), "content");
            return content == null || content.isBlank() ? Optional.empty() : Optional.of(content.trim());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private List<String> queryInstalledModels() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(OLLAMA_BASE + "/tags"))
                    .timeout(Duration.ofSeconds(DISCOVERY_TIMEOUT_SECONDS))
                    .GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) return List.of();
            return extractModelNames(response.body());
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String resolveSelectedModel(List<String> installed) {
        String preferred = preferences.getPreferredModel();
        if (!preferred.isBlank() && installed.contains(preferred)) return preferred;
        for (String candidate : MODEL_PRIORITY) {
            for (String installedModel : installed) {
                if (installedModel.equalsIgnoreCase(candidate) || installedModel.toLowerCase(Locale.ROOT).startsWith(candidate + ":")) {
                    return installedModel;
                }
            }
        }
        return installed.get(0);
    }

    private void tryStartOllama() {
        if (attemptedOllamaStart) return;
        attemptedOllamaStart = true;
        String executable = locateOllamaExecutable();
        if (executable == null) return;
        try {
            new ProcessBuilder(executable, "serve")
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            Thread.sleep(1200);
        } catch (Exception ignored) {
            // Ollama is optional. Offline coach stays functional.
        }
    }

    private String locateOllamaExecutable() {
        String localAppData = System.getenv("LOCALAPPDATA");
        List<String> candidates = new ArrayList<>();
        if (localAppData != null) candidates.add(localAppData + "\\Programs\\Ollama\\ollama.exe");
        candidates.add("C:\\Program Files\\Ollama\\ollama.exe");
        candidates.add("C:\\Program Files (x86)\\Ollama\\ollama.exe");
        String path = System.getenv("PATH");
        if (path != null) {
            for (String directory : path.split(File.pathSeparator)) {
                candidates.add(new File(directory, "ollama.exe").getAbsolutePath());
            }
        }
        return candidates.stream().filter(candidate -> new File(candidate).isFile()).findFirst().orElse(null);
    }

    private double total(List<Transaction> transactions, TransactionType type) {
        return transactions.stream().filter(t -> t.getType() == type).mapToDouble(Transaction::getAmount).sum();
    }

    private Map<String, Double> expenseByCategory(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(t -> blankToOther(t.getCategoryName()), LinkedHashMap::new, Collectors.summingDouble(Transaction::getAmount)));
    }

    private String blankToOther(String value) {
        return value == null || value.isBlank() ? "Khác" : value;
    }

    private String pct(double value) {
        return String.format(Locale.US, "%.1f%%", value);
    }

    private List<String> extractModelNames(String json) {
        if (json == null || json.isBlank()) return List.of();
        List<String> names = new ArrayList<>();
        String marker = "\"name\":\"";
        int index = 0;
        while ((index = json.indexOf(marker, index)) >= 0) {
            int start = index + marker.length();
            int end = json.indexOf('"', start);
            if (end < 0) break;
            names.add(json.substring(start, end));
            index = end + 1;
        }
        return names;
    }

    private String extractJsonString(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return null;
        start += marker.length();
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaped) {
                value.append(switch (ch) { case 'n' -> '\n'; case 'r' -> '\r'; case 't' -> '\t'; default -> ch; });
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                return value.toString();
            } else {
                value.append(ch);
            }
        }
        return null;
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n");
    }

    public record Snapshot(double income, double expense, double net, double savingsRate,
                           String topCategory, double topCategoryAmount,
                           Map<String, Double> expenseByCategory, int transactionCount) { }

    public record RuntimeInfo(boolean aiAvailable, String selectedModel, List<String> models, String statusText) { }
}
