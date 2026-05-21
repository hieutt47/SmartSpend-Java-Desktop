package com.example.smartspend.repository;

import com.example.smartspend.database.DatabaseConnection;
import com.example.smartspend.model.Budget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BudgetRepository {

    public List<Budget> findMonthlyBudgets(int userId, int month, int year) {
        String sql = "SELECT b.budget_id, b.user_id, b.category_id, c.name AS category_name, b.amount, b.budget_month, b.budget_year, " +
                "COALESCE((SELECT SUM(t.amount) FROM transactions t " +
                "WHERE t.user_id=b.user_id AND t.category_id=b.category_id AND t.type='EXPENSE' " +
                "AND MONTH(t.date)=b.budget_month AND YEAR(t.date)=b.budget_year), 0) AS spent " +
                "FROM monthly_budgets b JOIN categories c ON b.category_id=c.category_id " +
                "WHERE b.user_id=? AND b.budget_month=? AND b.budget_year=? ORDER BY c.name";
        List<Budget> budgets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    budgets.add(new Budget(
                            rs.getInt("budget_id"), rs.getInt("user_id"), rs.getInt("category_id"),
                            rs.getString("category_name"), rs.getDouble("amount"), rs.getDouble("spent"),
                            rs.getInt("budget_month"), rs.getInt("budget_year")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("BudgetRepository.findMonthlyBudgets lỗi: " + e.getMessage());
        }
        return budgets;
    }

    public boolean upsertBudget(int userId, int categoryId, double amount, int month, int year) {
        String sql = "MERGE INTO monthly_budgets (user_id, category_id, amount, budget_month, budget_year) " +
                "KEY(user_id, category_id, budget_month, budget_year) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setDouble(3, amount);
            ps.setInt(4, month);
            ps.setInt(5, year);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("BudgetRepository.upsertBudget lỗi: " + e.getMessage());
            return false;
        }
    }
}
