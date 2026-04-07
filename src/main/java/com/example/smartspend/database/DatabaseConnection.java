package com.example.smartspend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://smart-spend-trantrunghieu30032006-cb3f.j.aivencloud.com:12126/defaultdb?sslMode=REQUIRED";
    private static final String USER = "avnadmin";

    private static final String PASSWORD = "";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }


    
    public static void initializeDatabase() {
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
                "FOREIGN KEY (category_id) REFERENCES categories(category_id)" +
                ");";

        String createBudgetTable = "CREATE TABLE IF NOT EXISTS monthly_budgets (" +
                "budget_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "amount DECIMAL(15, 2) NOT NULL, " +
                "month INT NOT NULL, " +
                "year INT NOT NULL" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(createCategoryTable);
                stmt.execute(createTransactionTable);
                stmt.execute(createBudgetTable);
                System.out.println("Tạo 3 Bảng (Categories, Transactions, Budgets) trên Aiven thành công!");
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi tạo bảng!");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        // Gọi hàm tạo bảng
        initializeDatabase();
    }
}