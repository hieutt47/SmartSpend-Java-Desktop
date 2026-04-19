package com.example.smartspend.dao;

import com.example.smartspend.database.DatabaseConnection;
import com.example.smartspend.model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements ITransactionDAO {


    public boolean save(Transaction transaction) {
        String sql = "INSERT INTO transactions (amount, date, category_id, note, type) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, transaction.getAmount());
            pstmt.setDate(2, java.sql.Date.valueOf(transaction.getDate()));
            pstmt.setInt(3, transaction.getCategoryId());
            pstmt.setString(4, transaction.getNote());
            pstmt.setString(5, transaction.getType().name());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // --- THÊM MỚI: Implement update ---
    @Override
    public boolean update(Transaction transaction) {
        String sql = "UPDATE transactions SET amount=?, date=?, note=?, category_id=?, type=? WHERE transaction_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, transaction.getAmount());
            pstmt.setDate(2, java.sql.Date.valueOf(transaction.getDate()));
            pstmt.setString(3, transaction.getNote());
            pstmt.setInt(4, transaction.getCategoryId());
            pstmt.setString(5, transaction.getType().name());
            pstmt.setInt(6, transaction.getTransactionId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // --- THÊM MỚI ---
    @Override
    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transaction_id, amount, date, note, category_id, type FROM transactions ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setAmount(rs.getDouble("amount"));
                t.setDate(rs.getDate("date").toLocalDate());
                t.setNote(rs.getString("note"));
                t.setCategoryId(rs.getInt("category_id"));
                t.setType(com.example.smartspend.model.enums.TransactionType.valueOf(rs.getString("type")));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}