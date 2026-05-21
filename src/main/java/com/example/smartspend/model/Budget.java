package com.example.smartspend.model;

public class Budget {
    private int budgetId;
    private int userId;
    private int categoryId;
    private String categoryName;
    private double amount;
    private double spent;
    private int month;
    private int year;

    public Budget() {}

    public Budget(int budgetId, int userId, int categoryId, String categoryName,
                  double amount, double spent, int month, int year) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amount = amount;
        this.spent = spent;
        this.month = month;
        this.year = year;
    }

    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName == null ? "" : categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getSpent() { return spent; }
    public void setSpent(double spent) { this.spent = spent; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public double getRemaining() { return amount - spent; }
    public double getProgress() { return amount <= 0 ? 0 : Math.min(spent / amount, 1.0); }
}
