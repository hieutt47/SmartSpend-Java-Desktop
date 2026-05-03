package com.example.smartspend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // CẤU HÌNH LOCALHOST (Dùng cho quá trình phát triển trên máy)
    private static final String URL = "jdbc:mysql://localhost:3306/smartspend";
    private static final String USER = "root";
    private static final String PASSWORD = "alexander1601";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("❌ Lỗi kết nối Database!");
            e.printStackTrace();
        }
        return connection;
    }

    public static void initializeDatabase() {
        String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String createCategoryTable = "CREATE TABLE IF NOT EXISTS categories (" +
                "category_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "type VARCHAR(50) NOT NULL" +
                ");";

        String createTransactionTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "transaction_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "amount DECIMAL(15, 2) NOT NULL, " +
                "date DATE NOT NULL, " +
                "note VARCHAR(255), " +
                "category_id INT, " +
                "type VARCHAR(20) NOT NULL, " + // Thêm cột này để lưu INCOME/EXPENSE
                "FOREIGN KEY (category_id) REFERENCES categories(category_id)" +
                ");";

        String createBudgetTable = "CREATE TABLE IF NOT EXISTS monthly_budgets (" +
                "budget_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "amount DECIMAL(15, 2) NOT NULL, " +
                "month INT NOT NULL, " +
                "year INT NOT NULL" +
                ");";

        String createBillTable = "CREATE TABLE IF NOT EXISTS bills (" +
                "bill_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "amount DECIMAL(15, 2) NOT NULL, " +
                "due_date DATE NOT NULL, " +
                "is_paid BOOLEAN DEFAULT FALSE" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(createUserTable);
                stmt.execute(createCategoryTable);
                stmt.execute(createTransactionTable);
                stmt.execute(createBudgetTable);
                stmt.execute(createBillTable);
                System.out.println("✅ Khởi tạo tất cả các bảng thành công!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi tạo bảng!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}