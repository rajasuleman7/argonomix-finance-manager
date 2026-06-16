package com.argonomix.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Investment {
    private int investmentId;
    private int userId;
    private String investmentName;
    private String investmentType;
    private double amountInvested;
    private double currentValue;
    private LocalDate purchaseDate;
    private LocalDateTime createdAt;

    public Investment() {}

    public Investment(int userId, String investmentName, String investmentType, 
                     double amountInvested, double currentValue, LocalDate purchaseDate) {
        this.userId = userId;
        this.investmentName = investmentName;
        this.investmentType = investmentType;
        this.amountInvested = amountInvested;
        this.currentValue = currentValue;
        this.purchaseDate = purchaseDate;
    }

    // Getters and Setters
    public int getInvestmentId() {
        return investmentId;
    }

    public void setInvestmentId(int investmentId) {
        this.investmentId = investmentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getInvestmentName() {
        return investmentName;
    }

    public void setInvestmentName(String investmentName) {
        this.investmentName = investmentName;
    }

    public String getInvestmentType() {
        return investmentType;
    }

    public void setInvestmentType(String investmentType) {
        this.investmentType = investmentType;
    }

    public double getAmountInvested() {
        return amountInvested;
    }

    public void setAmountInvested(double amountInvested) {
        this.amountInvested = amountInvested;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public double getReturn() {
        return currentValue - amountInvested;
    }

    public double getReturnPercentage() {
        if (amountInvested == 0) return 0;
        return ((currentValue - amountInvested) / amountInvested) * 100;
    }
}

