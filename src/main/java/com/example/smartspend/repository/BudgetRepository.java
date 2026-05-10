package com.example.smartspend.repository;

import com.example.smartspend.dao.BudgetDAOImpl;
import java.util.Map;

public class BudgetRepository {

    private final BudgetDAOImpl budgetDAO = new BudgetDAOImpl();

    public Map<Integer, Double> getBudgetsForMonth(int month, int year) {
        return budgetDAO.getBudgetsForMonth(month, year);
    }

    public void setCategoryBudget(int categoryId, double amount, int month, int year) {
        budgetDAO.setCategoryBudget(categoryId, amount, month, year);
    }
}