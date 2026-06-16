package com.argonomix.models;

import java.time.LocalDateTime;

public class Budget {
    private int budgetId;
    private int userId;
    private String category;
    private double amountLimit;
    private String period;
    private LocalDateTime createdAt;

    public Budget() {
        this.period = "monthly";
    }

    public Budget(int userId, String category, double amountLimit, String period) {
        this.userId = userId;
        this.category = category;
        this.amountLimit = amountLimit;
        this.period = period != null ? period : "monthly";
    }

    // Getters and Setters
    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(double amountLimit) {
        this.amountLimit = amountLimit;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

