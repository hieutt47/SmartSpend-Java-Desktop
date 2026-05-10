package com.example.smartspend.model;

import com.example.smartspend.model.enums.TransactionType;

public class Category {

    private int id;                    // ← id chính
    private String name;
    private TransactionType type;
    private String icon;

    // Constructors
    public Category() {}

    public Category(int id, String name, TransactionType type, String icon) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }           // ← Quan trọng

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    @Override
    public String toString() {
        return name;
    }
}