package com.example.smartspend.service;

import com.example.smartspend.model.Transaction;
import com.example.smartspend.model.enums.TransactionType;
import com.example.smartspend.repository.TransactionRepository;

import java.time.LocalDate;
import java.util.List;


public class TransactionService {

    private final TransactionRepository repo = new TransactionRepository();

    public void addTransaction(Transaction transaction) throws IllegalArgumentException {
        validateCoreFields(transaction);
        if (!repo.saveTransaction(transaction)) {
            throw new RuntimeException("Lỗi hệ thống: Không thể lưu giao dịch vào Database.");
        }
    }

    public void updateTransaction(Transaction transaction) throws IllegalArgumentException {
        if (transaction.getTransactionId() <= 0) {
            throw new IllegalArgumentException("Lỗi: ID giao dịch không hợp lệ!");
        }
        validateCoreFields(transaction);
        if (!repo.updateTransaction(transaction)) {
            throw new RuntimeException("Lỗi hệ thống: Không thể cập nhật giao dịch.");
        }
    }

    /**
     * Xóa giao dịch theo ID.
     *
     * @param transactionId ID của giao dịch cần xóa
     * @throws IllegalArgumentException nếu id không hợp lệ
     * @throws RuntimeException         nếu DB không tìm thấy hoặc lỗi khi xóa
     */
    public void deleteTransaction(int transactionId) {
        if (transactionId <= 0) {
            throw new IllegalArgumentException("Lỗi: ID giao dịch phải lớn hơn 0!");
        }
        if (!repo.deleteTransaction(transactionId)) {
            throw new RuntimeException(
                    "Lỗi: Không tìm thấy giao dịch có ID=" + transactionId + " hoặc đã bị xóa trước đó."
            );
        }
    }

    public List<Transaction> getAllTransactions() {
        return repo.getAllTransactions();
    }

    public List<Transaction> getTransactionsByUser(int userId) {
        return repo.getTransactionsByUserId(userId);
    }

    public List<Transaction> getByType(int userId, TransactionType type) {
        return repo.getTransactionsByType(userId, type);
    }

    public List<Transaction> getByDateRange(int userId, LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.of(2000, 1, 1);
        if (to   == null) to   = LocalDate.now();
        if (from.isAfter(to))
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc!");
        return repo.getTransactionsByDateRange(userId, from, to);
    }

    public List<Transaction> getByCategory(int userId, int categoryId) {
        return repo.getTransactionsByCategory(userId, categoryId);
    }

    public double getTotalIncome(int userId) {
        return repo.getTotalByType(userId, TransactionType.INCOME);
    }

    public double getTotalExpense(int userId) {
        return repo.getTotalByType(userId, TransactionType.EXPENSE);
    }

    public double getNetBalance(int userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    public double getMonthlyIncome(int userId, int month, int year) {
        return repo.getTotalByMonth(userId, TransactionType.INCOME, month, year);
    }

    public double getMonthlyExpense(int userId, int month, int year) {
        return repo.getTotalByMonth(userId, TransactionType.EXPENSE, month, year);
    }

    private void validateCoreFields(Transaction t) {
        if (t.getAmount() <= 0) {
            throw new IllegalArgumentException("Số tiền giao dịch phải lớn hơn 0!");
        }
        if (t.getDate() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày giao dịch!");
        }
        if (t.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày giao dịch không thể là ngày trong tương lai!");
        }
        if (t.getType() == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại giao dịch (Thu nhập / Chi tiêu)!");
        }
    }
}