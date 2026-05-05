package model;

public class User {
    private String username;
    private double balance;      
    private double initialBalance;

    public User(String username, double initialBalance) {
        this.username       = username;
        this.balance        = initialBalance;
        this.initialBalance = initialBalance;
    }

    public boolean deductBalance(double amount) {
        if (this.balance >= amount) {
            this.balance = Math.round((this.balance - amount) * 100.0) / 100.0;
            return true;
        }
        return false; 
    }

    public void addBalance(double amount) {
        this.balance = Math.round((this.balance + amount) * 100.0) / 100.0;
    }

    public String getUsername()      { return username; }
    public double getBalance()       { return balance; }
    public double getInitialBalance(){ return initialBalance; }

    public void setBalance(double balance) { this.balance = balance; }
}