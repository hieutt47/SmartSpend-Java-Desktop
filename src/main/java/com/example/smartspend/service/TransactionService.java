package com.example.smartspend.service;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.repository.TransactionRepository;

import java.util.List;

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
    // --- THÊM MỚI ---
    public void updateTransaction(Transaction transaction) throws IllegalArgumentException {
        if (transaction.getTransactionId() <= 0) {
            throw new IllegalArgumentException("Lỗi: ID giao dịch không hợp lệ!");
        }
        if (transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Lỗi: Số tiền giao dịch phải lớn hơn 0!");
        }
        if (transaction.getNote() == null || transaction.getNote().trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Vui lòng nhập mô tả!");
        }
        if (transaction.getDate() == null) {
            throw new IllegalArgumentException("Lỗi: Vui lòng chọn ngày giao dịch!");
        }

        if (!transactionRepo.updateTransaction(transaction)) {
            throw new RuntimeException("Lỗi hệ thống: Không thể cập nhật vào Database.");
        }
    }
    // --- THÊM MỚI ---
    public List<Transaction> getAllTransactions() {
        return transactionRepo.getAllTransactions();
    }
}