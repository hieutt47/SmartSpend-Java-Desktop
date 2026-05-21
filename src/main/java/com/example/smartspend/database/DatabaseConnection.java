package com.example.smartspend.database;

import com.example.smartspend.utils.PasswordUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:h2:file:./data/smartspend;MODE=MySQL;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private static final String URL = System.getenv().getOrDefault("SMARTSPEND_DB_URL", DEFAULT_URL);
    private static final String USER = System.getenv().getOrDefault("SMARTSPEND_DB_USER", DEFAULT_USER);
    private static final String PASSWORD = System.getenv().getOrDefault("SMARTSPEND_DB_PASSWORD", DEFAULT_PASSWORD);

    static {
        try {
            Files.createDirectories(Path.of("data"));
            if (URL.startsWith("jdbc:h2:")) {
                Class.forName("org.h2.Driver");
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(150) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "category_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "UNIQUE(name, type)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "transaction_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "amount DECIMAL(15,2) NOT NULL, " +
                    "date DATE NOT NULL, " +
                    "note VARCHAR(255), " +
                    "type VARCHAR(20) NOT NULL, " +
                    "category_id INT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL" +
                    ")");

            migrateTransactionsUserIdIfNeeded(conn);

            stmt.execute("CREATE TABLE IF NOT EXISTS monthly_budgets (" +
                    "budget_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "category_id INT NOT NULL, " +
                    "amount DECIMAL(15,2) NOT NULL, " +
                    "budget_month INT NOT NULL, " +
                    "budget_year INT NOT NULL, " +
                    "UNIQUE(user_id, category_id, budget_month, budget_year), " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                    "bill_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "amount DECIMAL(15,2) NOT NULL, " +
                    "due_date DATE NOT NULL, " +
                    "is_paid BOOLEAN DEFAULT FALSE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS password_reset_codes (" +
                    "reset_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "email VARCHAR(150) NOT NULL, " +
                    "code_hash VARCHAR(255) NOT NULL, " +
                    "expires_at TIMESTAMP NOT NULL, " +
                    "used BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            seedCategories(conn);
            seedDemoData(conn);
            System.out.println("SmartSpend database is ready: " + URL);
        } catch (SQLException e) {
            System.err.println("Cannot initialize SmartSpend database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void migrateTransactionsUserIdIfNeeded(Connection conn) {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "transactions", "user_id")) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE transactions ADD COLUMN user_id INT DEFAULT 1");
                }
            }
        } catch (SQLException ignored) {
            // New installations already use the correct schema.
        }
    }

    private static void seedCategories(Connection conn) throws SQLException {
        String[][] defaults = {
                {"Lương & Thu nhập", "INCOME"}, {"Đầu tư", "INCOME"}, {"Kinh doanh", "INCOME"}, {"Thu nhập khác", "INCOME"},
                {"Ăn uống", "EXPENSE"}, {"Mua sắm", "EXPENSE"}, {"Giải trí", "EXPENSE"}, {"Di chuyển", "EXPENSE"},
                {"Nhà ở & Hóa đơn", "EXPENSE"}, {"Sức khỏe", "EXPENSE"}, {"Giáo dục", "EXPENSE"}, {"Chi tiêu khác", "EXPENSE"}
        };
        try (PreparedStatement ps = conn.prepareStatement("MERGE INTO categories (name, type) KEY(name, type) VALUES (?, ?)")) {
            for (String[] row : defaults) {
                ps.setString(1, row[0]);
                ps.setString(2, row[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedDemoData(Connection conn) throws SQLException {
        int userCount;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            userCount = rs.getInt(1);
        }
        if (userCount > 0) return;

        int demoUserId;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (name, email, password) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Demo User");
            ps.setString(2, "demo@smartspend.test");
            ps.setString(3, PasswordUtil.hash("demo123"));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                demoUserId = keys.getInt(1);
            }
        }

        String[][] txs = {
                {"Lương & Thu nhập", "INCOME", "1800", "Lương tháng"},
                {"Đầu tư", "INCOME", "220", "Lãi đầu tư"},
                {"Ăn uống", "EXPENSE", "86", "Ăn uống cuối tuần"},
                {"Mua sắm", "EXPENSE", "145", "Mua đồ cá nhân"},
                {"Di chuyển", "EXPENSE", "42", "Xăng xe"},
                {"Nhà ở & Hóa đơn", "EXPENSE", "310", "Tiền điện nước"},
                {"Giải trí", "EXPENSE", "60", "Xem phim"}
        };
        try (PreparedStatement findCat = conn.prepareStatement("SELECT category_id FROM categories WHERE name=? AND type=?");
             PreparedStatement insertTx = conn.prepareStatement("INSERT INTO transactions (user_id, amount, date, note, type, category_id) VALUES (?, ?, ?, ?, ?, ?)");
             PreparedStatement insertBudget = conn.prepareStatement("MERGE INTO monthly_budgets (user_id, category_id, amount, budget_month, budget_year) KEY(user_id, category_id, budget_month, budget_year) VALUES (?, ?, ?, ?, ?)")) {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < txs.length; i++) {
                int catId = findCategoryId(findCat, txs[i][0], txs[i][1]);
                insertTx.setInt(1, demoUserId);
                insertTx.setDouble(2, Double.parseDouble(txs[i][2]));
                insertTx.setDate(3, java.sql.Date.valueOf(today.minusDays(i * 3L)));
                insertTx.setString(4, txs[i][3]);
                insertTx.setString(5, txs[i][1]);
                insertTx.setInt(6, catId);
                insertTx.addBatch();

                if ("EXPENSE".equals(txs[i][1])) {
                    insertBudget.setInt(1, demoUserId);
                    insertBudget.setInt(2, catId);
                    insertBudget.setDouble(3, Math.max(150, Double.parseDouble(txs[i][2]) * 2.5));
                    insertBudget.setInt(4, today.getMonthValue());
                    insertBudget.setInt(5, today.getYear());
                    insertBudget.addBatch();
                }
            }
            insertTx.executeBatch();
            insertBudget.executeBatch();
        }
    }

    private static int findCategoryId(PreparedStatement ps, String name, String type) throws SQLException {
        ps.setString(1, name);
        ps.setString(2, type);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("Missing category " + name + " / " + type);
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}
