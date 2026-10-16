package com.example.smartspend.service;

import com.example.smartspend.model.Budget;
import com.example.smartspend.repository.BudgetRepository;

import java.time.YearMonth;
import java.util.List;

public class BudgetService {
    private final BudgetRepository repository = new BudgetRepository();

    public List<Budget> getCurrentMonthBudgets(int userId) {
        YearMonth now = YearMonth.now();
        return repository.findMonthlyBudgets(userId, now.getMonthValue(), now.getYear());
    }

    public boolean saveBudget(int userId, int categoryId, double amount, int month, int year) {
        if (userId <= 0) throw new IllegalArgumentException("Phiên đăng nhập không hợp lệ.");
        if (categoryId <= 0) throw new IllegalArgumentException("Danh mục không hợp lệ.");
        if (amount <= 0) throw new IllegalArgumentException("Ngân sách phải lớn hơn 0.");
        return repository.upsertBudget(userId, categoryId, amount, month, year);
    }
}
