package com.example.smartspend.repository;

import com.example.smartspend.dao.TransactionDAOImpl;
import com.example.smartspend.model.Transaction;

import java.util.List;

public class TransactionRepository {
    private TransactionDAOImpl transactionDAO = new TransactionDAOImpl();

    public boolean saveTransaction(Transaction transaction) {
        return transactionDAO.save(transaction);
    }
    // --- THÊM MỚI ---
    public boolean updateTransaction(Transaction transaction) {
        return transactionDAO.update(transaction);
    }
    // --- THÊM MỚI ---
    public List<Transaction> getAllTransactions() {
        return transactionDAO.findAll();
    }
}