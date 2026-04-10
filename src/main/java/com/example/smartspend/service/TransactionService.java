package com.example.smartspend.service;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.repository.TransactionRepository;

public class TransactionService {
    private TransactionRepository transactionRepo = new TransactionRepository();

    public void addTransaction(Transaction transaction) throws IllegalArgumentException {
        // Validate dữ liệu
        if (transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Lỗi: Số tiền giao dịch phải lớn hơn 0!");
        }
        if (transaction.getNote() == null || transaction.getNote().trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Vui lòng nhập ghi chú!");
        }

        // Pass qua ải thì gọi Repo lưu xuống DB
        if (!transactionRepo.saveTransaction(transaction)) {
            throw new RuntimeException("Lỗi hệ thống: Không thể lưu vào Database.");
        }
    }
}