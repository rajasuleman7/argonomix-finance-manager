package com.argonomix.models;

import java.time.LocalDateTime;

public class Notification {
    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private NotificationSeverity severity;
    private String notificationType;
    private boolean isRead;
    private LocalDateTime createdAt;

    public enum NotificationSeverity {
        INFO, WARNING, ERROR, SUCCESS
    }

    public Notification() {
        this.severity = NotificationSeverity.INFO;
        this.isRead = false;
    }

    public Notification(int userId, String title, String message, NotificationSeverity severity, String notificationType) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.notificationType = notificationType;
        this.isRead = false;
    }

    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(NotificationSeverity severity) {
        this.severity = severity;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

