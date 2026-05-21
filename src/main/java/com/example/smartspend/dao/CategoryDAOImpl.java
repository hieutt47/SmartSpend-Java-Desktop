package com.example.smartspend.dao;

import com.example.smartspend.database.DatabaseConnection;
import com.example.smartspend.model.Category;
import com.example.smartspend.model.enums.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl {

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, name, type FROM categories ORDER BY type, name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        TransactionType.valueOf(rs.getString("type"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("CategoryDAO.getAllCategories lỗi: " + e.getMessage());
        }
        return list;
    }

    public List<Category> getCategoriesByType(TransactionType type) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, name, type FROM categories WHERE type=? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Category(rs.getInt("category_id"), rs.getString("name"), type));
                }
            }
        } catch (SQLException e) {
            System.err.println("CategoryDAO.getCategoriesByType lỗi: " + e.getMessage());
        }
        return list;
    }

    public void seedDefaultCategoriesIfEmpty() {
        com.example.smartspend.database.DatabaseConnection.initializeDatabase();
    }
}
