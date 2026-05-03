package com.example.smartspend.repository;

import com.example.smartspend.dao.TransactionDAOImpl;
import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public class TransactionRepository {

    private final TransactionDAOImpl dao = new TransactionDAOImpl();

    public boolean saveTransaction(Transaction transaction) {
        return dao.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return dao.findAll();
    }

    public Transaction getTransactionById(int id) {
        return dao.findById(id);
    }

    public List<Transaction> getTransactionsByUserId(int userId) {
        return dao.findByUserId(userId);
    }

    public List<Transaction> getTransactionsByType(int userId, TransactionType type) {
        return dao.findByType(userId, type);
    }

    public List<Transaction> getTransactionsByDateRange(int userId, LocalDate from, LocalDate to) {
        return dao.findByDateRange(userId, from, to);
    }

    public List<Transaction> getTransactionsByCategory(int userId, int categoryId) {
        return dao.findByCategory(userId, categoryId);
    }

    public boolean updateTransaction(Transaction transaction) {
        return dao.update(transaction);
    }

    public boolean deleteTransaction(int transactionId) {
        return dao.deleteById(transactionId);
    }

    public double getTotalByType(int userId, TransactionType type) {
        return dao.getTotalByType(userId, type);
    }

    public double getTotalByMonth(int userId, TransactionType type, int month, int year) {
        return dao.getTotalByMonth(userId, type, month, year);
    }
}