package com.example.smartspend.model;
import com.example.smartspend.model.enums.TransactionType;
public class Category {
    private int categoryId;
    private String name;
    private TransactionType type; // Thuộc loại Thu hay Chi

    public Category() {}

    public Category(int categoryId, String name, TransactionType type) {
        this.categoryId = categoryId;
        this.name = name;
        this.type = type;
    }

    // Getter và Setter
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
}