package com.example.smartspend.model;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private double balance;

    public User() {}

    public User(int userId, String username, String passwordHash, double balance) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.balance = balance;
    }


    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}