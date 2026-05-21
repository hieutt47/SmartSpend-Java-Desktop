package com.example.smartspend.service;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.utils.CurrencyFormatter;
import com.example.smartspend.utils.HostInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Offline-first financial coach.
 *
 * SmartSpend always has a built-in rule-based advisor so the coaching screen
 * remains usable without internet or API keys. When Ollama is available locally,
 * the service can ask a local model for richer explanations and still falls back
 * safely to deterministic advice when anything goes wrong.
 */
public class FinancialAdvisorService {
    private static volatile boolean attemptedOllamaStart = false;
    private static final int OLLAMA_TIMEOUT_SECONDS = 10;
    private static final String DEFAULT_OLLAMA_ENDPOINT = "http://localhost:11434/api/generate";
    private static final String DEFAULT_OLLAMA_TAGS_ENDPOINT = "http://localhost:11434/api/tags";
    private static final List<String> MODEL_PRIORITY = List.of(
            "llama3.2", "llama3.1", "llama3", "mistral", "gemma3", "gemma2", "phi3", "qwen2.5"
    );

    public Snapshot buildSnapshot(List<Transaction> transactions) {
        List<Transaction> safe = transactions == null ? List.of() : transactions;
        double income = safe.stream().filter(t -> t.getType() == TransactionType.INCOME).mapToDouble(Transaction::getAmount).sum();
        double expense = safe.stream().filter(t -> t.getType() == TransactionType.EXPENSE).mapToDouble(Transaction::getAmount).sum();
        double net = income - expense;
        double savingsRate = income <= 0 ? 0 : Math.max(0, net) * 100.0 / income;
        Map<String, Double> expenseByCategory = expenseByCategory(safe);
        String topCategory = expenseByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Chưa có dữ liệu");
        double topCategoryAmount = expenseByCategory.getOrDefault(topCategory, 0.0);
        return new Snapshot(income, expense, net, savingsRate, topCategory, topCategoryAmount, expenseByCategory, safe.size());
    }

    public String buildSummary(List<Transaction> transactions) {
        Snapshot s = buildSnapshot(transactions);
        if (s.transactionCount == 0) {
            return "Bạn chưa có đủ dữ liệu. Hãy thêm vài giao dịch thu/chi để Smart Coach bắt đầu phân tích chính xác hơn.";
        }
        if (s.net < 0) {
            return "Dòng tiền đang âm " + CurrencyFormatter.format(Math.abs(s.net)) + ". Ưu tiên giảm nhóm chi lớn nhất và tạm dừng các khoản mua sắm không bắt buộc.";
        }
        return "Dòng tiền đang dương " + CurrencyFormatter.format(s.net) + ". Tỷ lệ tiết kiệm hiện khoảng "
                + String.format(Locale.US, "%.1f", s.savingsRate) + "% — đây là nền tảng tốt để tăng quỹ dự phòng.";
    }

    public List<String> buildActionPlan(List<Transaction> transactions) {
        Snapshot s = buildSnapshot(transactions);
        List<String> advice = new ArrayList<>();
        if (s.transactionCount == 0) {
            advice.add("Thêm ít nhất 1 khoản thu nhập và 3 khoản chi để dashboard, budgets và coach có dữ liệu thực tế.");
            advice.add("Bắt đầu với 3 budget cơ bản: Ăn uống, Di chuyển, Nhà ở & Hóa đơn.");
            advice.add("Dùng Export Report sau mỗi tuần để lưu lại dữ liệu demo hoặc nộp bài.");
            return advice;
        }

        if (s.net < 0) {
            advice.add("Cần cắt tối thiểu " + CurrencyFormatter.format(Math.abs(s.net)) + " để đưa dòng tiền về hòa vốn.");
        } else {
            advice.add("Tự động chuyển khoảng " + CurrencyFormatter.format(s.net * 0.5) + " vào quỹ dự phòng hoặc tiết kiệm ngay sau khi nhận thu nhập.");
        }

        if (s.savingsRate < 10) {
            advice.add("Tỷ lệ tiết kiệm thấp. Mục tiêu ngắn hạn: đạt 15% bằng cách giảm chi tiêu linh hoạt trước.");
        } else if (s.savingsRate < 30) {
            advice.add("Tỷ lệ tiết kiệm ổn. Mục tiêu tiếp theo: nâng lên 30% trong 2–3 tháng tới.");
        } else {
            advice.add("Tỷ lệ tiết kiệm rất tốt. Có thể chia phần dư thành quỹ dự phòng, học tập và đầu tư dài hạn.");
        }

        if (s.topCategoryAmount > 0) {
            double share = s.expense == 0 ? 0 : s.topCategoryAmount * 100 / s.expense;
            advice.add("Danh mục chi lớn nhất là " + s.topCategory + " (" + String.format(Locale.US, "%.1f", share) + "% chi tiêu). Đặt cảnh báo khi danh mục này vượt 80% ngân sách.");
        }

        if (s.expense > s.income * 0.75 && s.income > 0) {
            advice.add("Chi tiêu đang vượt 75% thu nhập. Hãy rà lại subscription, hóa đơn định kỳ và các khoản mua sắm nhỏ lặp lại.");
        } else {
            advice.add("Cấu trúc chi tiêu hiện khá an toàn. Duy trì review dữ liệu mỗi cuối tuần.");
        }
        return advice;
    }

    public String answerQuestion(String question, List<Transaction> transactions) {
        String normalizedQuestion = question == null ? "" : question.trim();
        if (normalizedQuestion.isBlank()) {
            return "Bạn có thể hỏi: 'Tháng này tôi nên tiết kiệm bao nhiêu?', 'Danh mục nào nguy hiểm?', hoặc 'Lập kế hoạch tiết kiệm 3 tháng cho tôi'.";
        }

        String ollamaAnswer = tryOllama(normalizedQuestion, transactions);
        if (ollamaAnswer != null && !ollamaAnswer.isBlank()) return ollamaAnswer.trim();

        Snapshot s = buildSnapshot(transactions);
        String lower = normalizedQuestion.toLowerCase(Locale.ROOT);
        if (lower.contains("tiết kiệm") || lower.contains("save") || lower.contains("saving")) {
            double target = Math.max(0, s.net * 0.5);
            return "Theo dữ liệu hiện tại, bạn nên đặt mục tiêu tiết kiệm tối thiểu " + CurrencyFormatter.format(target)
                    + ". Nếu muốn an toàn hơn, hãy ưu tiên quỹ dự phòng trước khi tăng chi tiêu giải trí.";
        }
        if (lower.contains("nguy") || lower.contains("rủi ro") || lower.contains("risk") || lower.contains("vượt")) {
            return s.topCategoryAmount > 0
                    ? "Rủi ro lớn nhất hiện nằm ở danh mục " + s.topCategory + " với tổng chi " + CurrencyFormatter.format(s.topCategoryAmount) + ". Hãy đặt trần ngân sách và bật cảnh báo budget."
                    : "Chưa có đủ dữ liệu chi tiêu để xác định rủi ro. Hãy thêm giao dịch expense trước.";
        }
        if (lower.contains("budget") || lower.contains("ngân sách")) {
            return buildBudgetSuggestion(transactions);
        }
        return buildSummary(transactions) + "\n\nKế hoạch đề xuất:\n- " + String.join("\n- ", buildActionPlan(transactions));
    }

    public String buildBudgetSuggestion(List<Transaction> transactions) {
        Snapshot s = buildSnapshot(transactions);
        if (s.expenseByCategory.isEmpty()) return "Chưa có dữ liệu chi tiêu. Hãy thêm expense để Smart Coach đề xuất ngân sách.";
        StringBuilder sb = new StringBuilder("Ngân sách tháng sau nên dựa trên mức chi hiện tại, giảm nhẹ 10% ở nhóm linh hoạt:\n");
        s.expenseByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(6)
                .forEach(e -> sb.append("- ").append(e.getKey()).append(": ")
                        .append(CurrencyFormatter.format(Math.max(50, e.getValue() * 0.9))).append("\n"));
        return sb.toString().trim();
    }

    public String buildEmailReport(List<Transaction> transactions) {
        Snapshot s = buildSnapshot(transactions);
        return "SmartSpend AI Coach Report\n"
                + "Host: " + HostInfo.HOST_NAME + " <" + HostInfo.HOST_EMAIL + ">\n\n"
                + "Income: " + CurrencyFormatter.format(s.income) + "\n"
                + "Expense: " + CurrencyFormatter.format(s.expense) + "\n"
                + "Net: " + CurrencyFormatter.format(s.net) + "\n"
                + "Savings rate: " + String.format(Locale.US, "%.1f%%", s.savingsRate) + "\n"
                + "Top expense category: " + s.topCategory + " (" + CurrencyFormatter.format(s.topCategoryAmount) + ")\n\n"
                + buildSummary(transactions) + "\n\nAction plan:\n- " + String.join("\n- ", buildActionPlan(transactions));
    }

    private Map<String, Double> expenseByCategory(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(t -> blankToOther(t.getCategoryName()), LinkedHashMap::new, Collectors.summingDouble(Transaction::getAmount)));
    }

    private String blankToOther(String value) {
        return value == null || value.isBlank() ? "Khác" : value;
    }

    public String providerStatus() {
        String mode = System.getenv().getOrDefault("SMARTSPEND_USE_OLLAMA", "auto").trim().toLowerCase(Locale.ROOT);
        if ("false".equals(mode) || "0".equals(mode) || "off".equals(mode)) {
            return "Offline coach active · Ollama disabled by SMARTSPEND_USE_OLLAMA=false";
        }
        String model = resolveOllamaModel();
        if (model == null) {
            return "Offline coach active · Ollama not ready. Optional: install Ollama and pull llama3.2.";
        }
        return "Ollama local AI active · Model: " + model;
    }

    private String tryOllama(String question, List<Transaction> transactions) {
        String mode = System.getenv().getOrDefault("SMARTSPEND_USE_OLLAMA", "auto").trim().toLowerCase(Locale.ROOT);
        if ("false".equals(mode) || "0".equals(mode) || "off".equals(mode)) return null;
        try {
            String model = resolveOllamaModel();
            if (model == null || model.isBlank()) return null;

            Snapshot s = buildSnapshot(transactions);
            String prompt = "Bạn là tư vấn viên tài chính cá nhân trong app SmartSpend. "
                    + "Trả lời tiếng Việt, ngắn gọn, thực tế, không phán xét. "
                    + "Hãy dùng dữ liệu thật sau: income=" + s.income + ", expense=" + s.expense + ", net=" + s.net
                    + ", savingsRate=" + String.format(Locale.US, "%.1f", s.savingsRate)
                    + ", topCategory=" + s.topCategory + ", topCategoryAmount=" + s.topCategoryAmount
                    + ". Câu hỏi của người dùng: " + question;
            String endpoint = System.getenv().getOrDefault("SMARTSPEND_OLLAMA_URL", DEFAULT_OLLAMA_ENDPOINT);
            String json = "{\"model\":\"" + escapeJson(model) + "\",\"prompt\":\"" + escapeJson(prompt)
                    + "\",\"stream\":false}";
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(java.time.Duration.ofSeconds(OLLAMA_TIMEOUT_SECONDS))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) return null;
            return extractJsonString(response.body(), "response");
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveOllamaModel() {
        String configured = System.getenv().getOrDefault("SMARTSPEND_OLLAMA_MODEL", "").trim();
        if (!configured.isBlank()) return configured;
        List<String> installed = listInstalledOllamaModels();
        if (installed.isEmpty()) return null;
        for (String preferred : MODEL_PRIORITY) {
            for (String model : installed) {
                if (model.equalsIgnoreCase(preferred) || model.toLowerCase(Locale.ROOT).startsWith(preferred.toLowerCase(Locale.ROOT) + ":")) {
                    return model;
                }
            }
        }
        return installed.get(0);
    }

    private List<String> listInstalledOllamaModels() {
        List<String> firstAttempt = queryOllamaTags();
        if (!firstAttempt.isEmpty()) return firstAttempt;
        startOllamaServerIfPossible();
        return queryOllamaTags();
    }

    private List<String> queryOllamaTags() {
        String tagsEndpoint = System.getenv().getOrDefault("SMARTSPEND_OLLAMA_TAGS_URL", DEFAULT_OLLAMA_TAGS_ENDPOINT);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(tagsEndpoint))
                    .timeout(java.time.Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) return List.of();
            return extractModelNames(response.body());
        } catch (Exception e) {
            return List.of();
        }
    }

    private void startOllamaServerIfPossible() {
        if (attemptedOllamaStart) return;
        attemptedOllamaStart = true;
        String executable = findOllamaExecutable();
        if (executable == null || executable.isBlank()) return;
        try {
            new ProcessBuilder(executable, "serve")
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            Thread.sleep(1200);
        } catch (Exception ignored) {
            // Keep SmartSpend reliable: Ollama is optional, so any launch failure falls back to offline coach.
        }
    }

    private String findOllamaExecutable() {
        String configured = System.getenv().getOrDefault("SMARTSPEND_OLLAMA_EXE", "").trim();
        if (!configured.isBlank() && new java.io.File(configured).isFile()) return configured;

        List<String> candidates = new ArrayList<>();
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            candidates.add(localAppData + "\\Programs\\Ollama\\ollama.exe");
        }
        candidates.add("C:\\Program Files\\Ollama\\ollama.exe");
        candidates.add("C:\\Program Files (x86)\\Ollama\\ollama.exe");

        for (String candidate : candidates) {
            if (new java.io.File(candidate).isFile()) return candidate;
        }
        return findExecutableOnPath();
    }

    private String findExecutableOnPath() {
        String path = System.getenv("PATH");
        if (path == null || path.isBlank()) return null;
        String exe = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win") ? "ollama.exe" : "ollama";
        for (String dir : path.split(java.io.File.pathSeparator)) {
            if (dir == null || dir.isBlank()) continue;
            java.io.File candidate = new java.io.File(dir, exe);
            if (candidate.isFile()) return candidate.getAbsolutePath();
        }
        return null;
    }

    private List<String> extractModelNames(String json) {
        if (json == null || json.isBlank()) return List.of();
        List<String> names = new ArrayList<>();
        String marker = "\"name\":\"";
        int index = 0;
        while ((index = json.indexOf(marker, index)) >= 0) {
            index += marker.length();
            StringBuilder sb = new StringBuilder();
            boolean escape = false;
            for (int i = index; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escape) {
                    sb.append(c);
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            String name = sb.toString().trim();
            if (!name.isBlank()) names.add(name);
        }
        return names;
    }

    private String extractJsonString(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return null;
        start += marker.length();
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else sb.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public record Snapshot(double income, double expense, double net, double savingsRate,
                           String topCategory, double topCategoryAmount,
                           Map<String, Double> expenseByCategory, int transactionCount) {}
}
