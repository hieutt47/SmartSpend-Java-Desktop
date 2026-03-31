package com.example.smartspend.model;

public class User {
    private int userId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public double getBalace() {
        return balace;
    }

    public void setBalace(double balace) {
        this.balace = balace;
    }

    private String username;
    private String passwordHash;
    private double balace;

    public User(){
    }

    public User(int UsedId,String username,String passwordHash,double balance){
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.balace = balance;
    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + userId +
                ", name='" + username + '\'' +
                ", balance=" + balance +
                '}';
    }
}
