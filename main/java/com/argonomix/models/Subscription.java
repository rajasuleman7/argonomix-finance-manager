package com.argonomix.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Subscription {
    private int subscriptionId;
    private int userId;
    private String serviceName;
    private double monthlyCost;
    private int billingDate; // Day of month (1-31)
    private String category;
    private SubscriptionStatus status;
    private LocalDate nextBillingDate;
    private LocalDateTime createdAt;

    public enum SubscriptionStatus {
        KEEP, CONSIDER_CANCELING
    }

    public Subscription() {
        this.status = SubscriptionStatus.KEEP;
    }

    public Subscription(int userId, String serviceName, double monthlyCost, 
                        int billingDate, String category) {
        this.userId = userId;
        this.serviceName = serviceName;
        this.monthlyCost = monthlyCost;
        this.billingDate = billingDate;
        this.category = category;
        this.status = SubscriptionStatus.KEEP;
    }

    // Getters and Setters
    public int getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }

    public void setMonthlyCost(double monthlyCost) {
        this.monthlyCost = monthlyCost;
    }

    public int getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(int billingDate) {
        this.billingDate = billingDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public double getAnnualCost() {
        return monthlyCost * 12;
    }
}

