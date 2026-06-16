package com.argonomix.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Goal {
    private int goalId;
    private int userId;
    private String goalName;
    private double targetAmount;
    private double currentAmount;
    private LocalDate deadline;
    private String goalIcon;
    private boolean isCompleted;
    private LocalDateTime createdAt;

    public Goal() {
        this.currentAmount = 0.0;
        this.isCompleted = false;
        this.goalIcon = "🎯";
    }

    public Goal(int userId, String goalName, double targetAmount, LocalDate deadline) {
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.deadline = deadline;
        this.currentAmount = 0.0;
        this.isCompleted = false;
        this.goalIcon = "🎯";
    }

    // Getters and Setters
    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getGoalIcon() {
        return goalIcon;
    }

    public void setGoalIcon(String goalIcon) {
        this.goalIcon = goalIcon;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public double getProgressPercentage() {
        if (targetAmount == 0) return 0;
        return Math.min((currentAmount / targetAmount) * 100, 100);
    }
}

