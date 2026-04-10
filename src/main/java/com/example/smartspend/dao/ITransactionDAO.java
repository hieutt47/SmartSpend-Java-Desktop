package com.example.smartspend.dao;

import com.example.smartspend.model.Transaction;

public interface ITransactionDAO {
    boolean save(Transaction transaction);
}