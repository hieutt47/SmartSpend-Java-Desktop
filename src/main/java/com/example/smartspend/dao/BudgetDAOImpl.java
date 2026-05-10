package com.example.smartspend.dao;

import com.example.smartspend.database.DatabaseConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BudgetDAOImpl {

    /**
     * Lấy tất cả ngân sách theo danh mục cho tháng/năm cụ thể.
     * Trả về Map: categoryId → budgetAmount
     */
    public Map<Integer, Double> getBudgetsForMonth(int month, int year) {
        Map<Integer, Double> map = new HashMap<>();
        String sql = "SELECT category_id, budget_amount FROM category_budgets WHERE month = ? AND year = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return map;
            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt("category_id"), rs.getDouble("budget_amount"));
            }
        } catch (SQLException e) {
            System.err.println("BudgetDAOImpl.getBudgetsForMonth error: " + e.getMessage());
        }
        return map;
    }

    /**
     * Upsert: nếu đã có → UPDATE, chưa có → INSERT.
     * Dùng MySQL ON DUPLICATE KEY UPDATE (dựa vào UNIQUE key category_id+month+year).
     */
    public void setCategoryBudget(int categoryId, double amount, int month, int year) {
        String sql = "INSERT INTO category_budgets (category_id, budget_amount, month, year) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE budget_amount = VALUES(budget_amount)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return;
            pstmt.setInt(1, categoryId);
            pstmt.setDouble(2, amount);
            pstmt.setInt(3, month);
            pstmt.setInt(4, year);
            pstmt.executeUpdate();
            System.out.println("✅ Đã lưu ngân sách danh mục " + categoryId + " = " + amount);
        } catch (SQLException e) {
            System.err.println("BudgetDAOImpl.setCategoryBudget error: " + e.getMessage());
        }
    }
}