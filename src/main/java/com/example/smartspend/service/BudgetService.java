package com.example.smartspend.service;

import com.example.smartspend.repository.BudgetRepository;
import java.util.Map;

public class BudgetService {

    private final BudgetRepository budgetRepo = new BudgetRepository();

    /**
     * Lấy map [categoryId → ngân sách] cho tháng/năm.
     */
    public Map<Integer, Double> getBudgetsForMonth(int month, int year) {
        return budgetRepo.getBudgetsForMonth(month, year);
    }

    /**
     * Đặt/cập nhật ngân sách cho một danh mục trong tháng/năm.
     * @throws IllegalArgumentException nếu amount < 0
     */
    public void setCategoryBudget(int categoryId, double amount, int month, int year) {
        if (amount < 0) throw new IllegalArgumentException("Ngân sách không được âm!");
        budgetRepo.setCategoryBudget(categoryId, amount, month, year);
    }
}