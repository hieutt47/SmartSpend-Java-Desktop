package com.example.smartspend.model.enums;

public enum AdvisorMode {
    COACH("Tư vấn tổng quan", "Hãy đánh giá tình hình tài chính hiện tại của tôi và đưa ra 3 hành động ưu tiên."),
    ANALYST("Phân tích dòng tiền", "Phân tích thu nhập, chi tiêu và các xu hướng quan trọng trong dữ liệu của tôi."),
    BUDGET_PLANNER("Lập ngân sách", "Hãy đề xuất ngân sách thực tế cho tháng tới theo từng nhóm chi tiêu."),
    RISK_GUARD("Cảnh báo rủi ro", "Kiểm tra rủi ro chi tiêu và chỉ ra điều gì tôi cần chú ý ngay."),
    SAVINGS_PLANNER("Kế hoạch tiết kiệm", "Lập kế hoạch tiết kiệm ngắn hạn phù hợp với dòng tiền của tôi.");

    private final String label;
    private final String defaultQuestion;

    AdvisorMode(String label, String defaultQuestion) {
        this.label = label;
        this.defaultQuestion = defaultQuestion;
    }

    public String getLabel() { return label; }
    public String getDefaultQuestion() { return defaultQuestion; }

    @Override
    public String toString() { return label; }
}
