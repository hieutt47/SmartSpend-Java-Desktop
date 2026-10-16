package com.example.smartspend.dao;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface ITransactionDAO {

    boolean save(Transaction transaction);

    List<Transaction> findAll();

    Transaction findById(int transactionId);

    List<Transaction> findByUserId(int userId);

    List<Transaction> findByType(int userId, TransactionType type);

    List<Transaction> findByDateRange(int userId, LocalDate from, LocalDate to);

    List<Transaction> findByCategory(int userId, int categoryId);

    // ─── UPDATE ───────────────────────────────────────────────────
    boolean update(Transaction transaction);

    // ─── DELETE ───────────────────────────────────────────────────
    boolean deleteById(int transactionId);

    // ─── AGGREGATE ────────────────────────────────────────────────
    double getTotalByType(int userId, TransactionType type);

    double getTotalByMonth(int userId, TransactionType type, int month, int year);
}