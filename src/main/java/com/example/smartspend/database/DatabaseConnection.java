package com.example.smartspend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // Cấu hình Database - Đảm bảo PASSWORD khớp với pass bạn vừa reset (30032006)
    private static final String URL = "jdbc:mysql://localhost:3306/smartspend?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "30032006"; // Mật khẩu bạn vừa reset

    public static Connection getConnection() {
        try {
            // Đăng ký Driver (Rất quan trọng cho các bản Java/MySQL mới)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Không tìm thấy Driver MySQL!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Lỗi kết nối Database! Kiểm tra lại pass hoặc tên DB.");
            e.printStackTrace();
        }
        return null;
    }

    public static void initializeDatabase() {
        // ... (Giữ nguyên các chuỗi String createUserTable, createCategoryTable... của bạn)
        // Lưu ý: Code của bạn phần tạo bảng rất tốt, không cần sửa nội dung SQL.

        String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        // (Thêm các câu lệnh tạo bảng khác của bạn vào đây...)

        try (Connection conn = getConnection()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.execute(createUserTable);
                // stmt.execute(createCategoryTable); ... gọi hết các lệnh của bạn

                // Demo gọi thử các bảng còn lại dựa trên code của bạn
                stmt.execute("CREATE TABLE IF NOT EXISTS categories (category_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, type VARCHAR(50) NOT NULL);");
                stmt.execute("CREATE TABLE IF NOT EXISTS transactions (transaction_id INT AUTO_INCREMENT PRIMARY KEY, amount DECIMAL(15, 2) NOT NULL, date DATE NOT NULL, note VARCHAR(255), category_id INT, type VARCHAR(20) NOT NULL, FOREIGN KEY (category_id) REFERENCES categories(category_id));");

                System.out.println("✅ Chúc mừng! Đã kết nối và khởi tạo tất cả các bảng thành công!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi thực thi lệnh SQL tạo bảng!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}