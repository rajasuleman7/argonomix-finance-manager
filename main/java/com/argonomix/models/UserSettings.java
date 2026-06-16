package com.argonomix.models;

public class UserSettings {
    private int settingId;
    private int userId;
    private String theme;
    private String currency;
    private boolean notificationsEnabled;
    private boolean onboardingComplete;

    public UserSettings() {
        this.theme = "light";
        this.currency = "USD";
        this.notificationsEnabled = true;
        this.onboardingComplete = false;
    }

    public UserSettings(int userId) {
        this();
        this.userId = userId;
    }

    // Getters and Setters
    public int getSettingId() {
        return settingId;
    }

    public void setSettingId(int settingId) {
        this.settingId = settingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isOnboardingComplete() {
        return onboardingComplete;
    }

    public void setOnboardingComplete(boolean onboardingComplete) {
        this.onboardingComplete = onboardingComplete;
    }
}

