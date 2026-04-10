package com.example.smartspend.repository;

import com.example.smartspend.dao.TransactionDAOImpl;
import com.example.smartspend.model.Transaction;

public class TransactionRepository {
    private TransactionDAOImpl transactionDAO = new TransactionDAOImpl();

    public boolean saveTransaction(Transaction transaction) {
        return transactionDAO.save(transaction);
    }
}