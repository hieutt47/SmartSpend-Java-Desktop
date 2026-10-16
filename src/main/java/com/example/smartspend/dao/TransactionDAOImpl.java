package com.example.smartspend.dao;

import com.example.smartspend.database.DatabaseConnection;
import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements ITransactionDAO {

    private static final String SQL_INSERT =
            "INSERT INTO transactions (user_id, amount, date, note, type, category_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE transactions SET amount=?, date=?, note=?, type=?, category_id=? " +
                    "WHERE transaction_id=? AND user_id=?";

    private static final String SQL_DELETE =
            "DELETE FROM transactions WHERE transaction_id=?";

    private static final String SQL_SELECT_BASE =
            "SELECT t.transaction_id, t.user_id, t.amount, t.date, t.note, t.type, " +
                    "       t.category_id, COALESCE(c.name, '—') AS category_name " +
                    "FROM transactions t " +
                    "LEFT JOIN categories c ON t.category_id = c.category_id ";

    @Override
    public boolean save(Transaction transaction) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            ps.setInt   (1, transaction.getUserId());
            ps.setDouble(2, transaction.getAmount());
            ps.setDate  (3, Date.valueOf(transaction.getDate()));
            ps.setString(4, transaction.getNote());
            ps.setString(5, transaction.getType().name());
            // category_id có thể null
            if (transaction.getCategoryId() > 0)
                ps.setInt(6, transaction.getCategoryId());
            else
                ps.setNull(6, Types.INTEGER);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.save() lỗi: " + e.getMessage());
            return false;
        }
    }


    @Override
    public List<Transaction> findAll() {
        String sql = SQL_SELECT_BASE + "ORDER BY t.date DESC, t.transaction_id DESC";
        return queryList(sql);
    }

    @Override
    public Transaction findById(int transactionId) {
        String sql = SQL_SELECT_BASE + "WHERE t.transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.findById() lỗi: " + e.getMessage());
        }
        return null;
    }


    @Override
    public List<Transaction> findByUserId(int userId) {
        String sql = SQL_SELECT_BASE +
                "WHERE t.user_id = ? " +
                "ORDER BY t.date DESC, t.transaction_id DESC";
        return queryListWith1Int(sql, userId);
    }

    @Override
    public List<Transaction> findByType(int userId, TransactionType type) {
        String sql = SQL_SELECT_BASE +
                "WHERE t.user_id = ? AND t.type = ? " +
                "ORDER BY t.date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, userId);
            ps.setString(2, type.name());
            return execQuery(ps);

        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.findByType() lỗi: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Transaction> findByDateRange(int userId, LocalDate from, LocalDate to) {
        String sql = SQL_SELECT_BASE +
                "WHERE t.user_id = ? AND t.date BETWEEN ? AND ? " +
                "ORDER BY t.date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt (1, userId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            return execQuery(ps);

        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.findByDateRange() lỗi: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Transaction> findByCategory(int userId, int categoryId) {
        String sql = SQL_SELECT_BASE +
                "WHERE t.user_id = ? AND t.category_id = ? " +
                "ORDER BY t.date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            return execQuery(ps);

        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.findByCategory() lỗi: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    @Override
    public boolean update(Transaction transaction) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setDouble(1, transaction.getAmount());
            ps.setDate  (2, Date.valueOf(transaction.getDate()));
            ps.setString(3, transaction.getNote());
            ps.setString(4, transaction.getType().name());
            if (transaction.getCategoryId() > 0)
                ps.setInt(5, transaction.getCategoryId());
            else
                ps.setNull(5, Types.INTEGER);
            ps.setInt   (6, transaction.getTransactionId());
            ps.setInt   (7, transaction.getUserId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.update() lỗi: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(int transactionId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, transactionId);
            int affected = ps.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Đã xóa transaction id=" + transactionId);
                return true;
            } else {
                System.out.println("⚠️ Không tìm thấy transaction id=" + transactionId + " để xóa.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.deleteById() lỗi: " + e.getMessage());
            return false;
        }
    }

    @Override
    public double getTotalByType(int userId, TransactionType type) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE user_id = ? AND type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, userId);
            ps.setString(2, type.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.getTotalByType() lỗi: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public double getTotalByMonth(int userId, TransactionType type, int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE user_id = ? AND type = ? " +
                "AND MONTH(date) = ? AND YEAR(date) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, userId);
            ps.setString(2, type.name());
            ps.setInt   (3, month);
            ps.setInt   (4, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.getTotalByMonth() lỗi: " + e.getMessage());
        }
        return 0.0;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt   ("transaction_id"),
                rs.getInt   ("user_id"),
                rs.getDouble("amount"),
                rs.getDate  ("date").toLocalDate(),
                rs.getString("note"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getInt   ("category_id"),
                rs.getString("category_name")
        );
    }

    private List<Transaction> execQuery(PreparedStatement ps) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        return list;
    }

    private List<Transaction> queryList(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return execQuery(ps);
        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.queryList() lỗi: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Query với một tham số Integer */
    private List<Transaction> queryListWith1Int(String sql, int param) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            return execQuery(ps);
        } catch (SQLException e) {
            System.err.println("❌ TransactionDAO.queryListWith1Int() lỗi: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}