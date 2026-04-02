package com.example.smartspend.model;

import java.time.LocalDate;

public class Transaction {
    private int transactionId;
    private double amount;
    private LocalDate date; // Dùng LocalDate để khớp với JavaFX DatePicker
    private String note;
    private int categoryId; // Khóa ngoại liên kết với bảng Category

    public Transaction() {}

    public Transaction(int transactionId, double amount, LocalDate date, String note, int categoryId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.categoryId = categoryId;
    }

    // Getter và Setter
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
}