package com.example.smartspend.dao;

import com.example.smartspend.database.DatabaseConnection;
import com.example.smartspend.model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}