package com.example.smartspend.dao;

import com.example.smartspend.database.DatabaseConnection;
import com.example.smartspend.model.Category;
import com.example.smartspend.model.enums.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl {

    // --- Lấy toàn bộ danh mục từ DB (ĐÃ BỌC GIÁP CHỐNG LỖI) ---
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, name, type FROM categories ORDER BY type, name";

        // 1. Mở kết nối và kiểm tra ngay lập tức
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("❌ LỖI [getAllCategories]: Không thể kết nối tới Database. Biến conn bị NULL!");
            return list; // Trả về list rỗng để app không bị văng
        }

        // 2. Chạy SQL
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setName(rs.getString("name"));
                c.setType(TransactionType.valueOf(rs.getString("type")));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Nhớ đóng kết nối
            try {
                if (!conn.isClosed()) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    // --- Seed dữ liệu mặc định (ĐÃ BỌC GIÁP CHỐNG LỖI) ---
    public void seedDefaultCategoriesIfEmpty() {
        // 1. Mở kết nối và kiểm tra
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("❌ LỖI [seedDefaultCategories]: Không thể kết nối tới Database để seed dữ liệu!");
            return; // Dừng lại luôn
        }

        if (!getAllCategories().isEmpty()) {
            try { if (!conn.isClosed()) conn.close(); } catch (SQLException e) {}
            return; // Đã có data → bỏ qua
        }

        String sql = "INSERT INTO categories (name, type) VALUES (?, ?)";
        String[][] defaults = {
                {"Lương & Thu nhập",    "INCOME"},
                {"Đầu tư",              "INCOME"},
                {"Kinh doanh",          "INCOME"},
                {"Thu nhập khác",       "INCOME"},
                {"Ăn uống",             "EXPENSE"},
                {"Mua sắm",             "EXPENSE"},
                {"Giải trí",            "EXPENSE"},
                {"Di chuyển",           "EXPENSE"},
                {"Nhà ở & Hóa đơn",     "EXPENSE"},
                {"Sức khỏe",            "EXPENSE"},
                {"Giáo dục",            "EXPENSE"},
                {"Chi tiêu khác",       "EXPENSE"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String[] row : defaults) {
                pstmt.setString(1, row[0]);
                pstmt.setString(2, row[1]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("✅ Đã seed " + defaults.length + " danh mục mặc định!");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (!conn.isClosed()) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}