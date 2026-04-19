package com.example.smartspend.dao;
import java.util.List;
import com.example.smartspend.model.Transaction;

public interface ITransactionDAO {
    boolean save(Transaction transaction);
    // --- THÊM MỚI ---

    List<Transaction> findAll();
    boolean update(Transaction transaction);
}