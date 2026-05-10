package com.example.smartspend.dao;

import com.example.smartspend.database.DatabaseConnection;
import com.example.smartspend.model.Category;
import com.example.smartspend.model.enums.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl {

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        // SỬA Ở ĐÂY: dùng 'id' thay vì 'category_id'
        String sql = "SELECT id, name, type, icon FROM categories ORDER BY type, name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getInt("id"));                    // ← Sửa thành setId
                c.setName(rs.getString("name"));

                String typeStr = rs.getString("type");
                if (typeStr != null) {
                    try {
                        c.setType(TransactionType.valueOf(typeStr.toUpperCase()));
                    } catch (Exception e) {
                        c.setType(TransactionType.EXPENSE); // default
                    }
                }

                c.setIcon(rs.getString("icon"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi lấy danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public void seedDefaultCategoriesIfEmpty() {
        // Kiểm tra xem đã có dữ liệu chưa
        if (!getAllCategories().isEmpty()) {
            System.out.println("✅ Đã có danh mục, bỏ qua seed.");
            return;
        }

        String sql = "INSERT INTO categories (name, type, icon) VALUES (?, ?, ?)";
        String[][] defaults = {
                {"Lương", "INCOME", "💰"},
                {"Ăn uống", "EXPENSE", "🍔"},
                {"Mua sắm", "EXPENSE", "🛍️"},
                {"Giáo dục", "EXPENSE", "📚"},
                {"Điện nước", "EXPENSE", "💡"},
                {"Di chuyển", "EXPENSE", "🛵"},
                {"Khác", "EXPENSE", "❓"}
        };

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (String[] row : defaults) {
                pstmt.setString(1, row[0]);
                pstmt.setString(2, row[1]);
                pstmt.setString(3, row[2]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("✅ Đã seed " + defaults.length + " danh mục mặc định!");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi seed categories: " + e.getMessage());
            e.printStackTrace();
        }
    }
}