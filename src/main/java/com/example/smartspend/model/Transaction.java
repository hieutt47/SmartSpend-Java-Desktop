package com.example.smartspend.model;

import com.example.smartspend.model.enums.TransactionType;
import java.time.LocalDate;

public class Transaction {

    private int           transactionId;
    private int           userId;          // FK → users.id  (THÊM MỚI v2)
    private double        amount;
    private LocalDate     date;
    private String        note;
    private TransactionType type;
    private int           categoryId;      // FK → categories.category_id

    private transient String categoryName;

    public Transaction() {
        this.note = "";
    }

    public Transaction(int transactionId, int userId, double amount,
                       LocalDate date, String note,
                       TransactionType type, int categoryId,
                       String categoryName) {
        this.transactionId = transactionId;
        this.userId        = userId;
        this.amount        = amount;
        this.date          = date;
        this.note          = (note == null) ? "" : note;
        this.type          = type;
        this.categoryId    = categoryId;
        this.categoryName  = categoryName;
    }

    public int getTransactionId()              { return transactionId; }
    public void setTransactionId(int id)       { this.transactionId = id; }

    public int getUserId()                     { return userId; }
    public void setUserId(int userId)          { this.userId = userId; }

    public double getAmount()                  { return amount; }
    public void setAmount(double amount)       { this.amount = amount; }

    public LocalDate getDate()                 { return date; }
    public void setDate(LocalDate date)        { this.date = date; }

    public String getNote()                    { return note == null ? "" : note; }
    public void setNote(String note)           { this.note = (note == null) ? "" : note; }

    public TransactionType getType()           { return type; }
    public void setType(TransactionType type)  { this.type = type; }

    public int getCategoryId()                 { return categoryId; }
    public void setCategoryId(int id)          { this.categoryId = id; }

    public String getCategoryName()            { return categoryName == null ? "" : categoryName; }
    public void setCategoryName(String name)   { this.categoryName = name; }

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, userId=%d, type=%s, amount=%.2f, date=%s, note='%s', category=%s}",
                transactionId, userId, type, amount, date, note, categoryName);
    }
}