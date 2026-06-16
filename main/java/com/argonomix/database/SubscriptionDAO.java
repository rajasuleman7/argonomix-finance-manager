package com.argonomix.database;

import com.argonomix.models.Subscription;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionDAO {
    
    public boolean save(Subscription subscription) throws SQLException {
        String sql = "INSERT INTO subscriptions (user_id, service_name, monthly_cost, billing_date, category, status, next_billing_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, subscription.getUserId());
            stmt.setString(2, subscription.getServiceName());
            stmt.setDouble(3, subscription.getMonthlyCost());
            stmt.setInt(4, subscription.getBillingDate());
            stmt.setString(5, subscription.getCategory());
            stmt.setString(6, subscription.getStatus().name().toLowerCase());
            stmt.setDate(7, subscription.getNextBillingDate() != null ? Date.valueOf(subscription.getNextBillingDate()) : null);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        subscription.setSubscriptionId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    public boolean update(Subscription subscription) throws SQLException {
        String sql = "UPDATE subscriptions SET service_name=?, monthly_cost=?, billing_date=?, category=?, status=?, next_billing_date=? WHERE subscription_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subscription.getServiceName());
            stmt.setDouble(2, subscription.getMonthlyCost());
            stmt.setInt(3, subscription.getBillingDate());
            stmt.setString(4, subscription.getCategory());
            stmt.setString(5, subscription.getStatus().name().toLowerCase());
            stmt.setDate(6, subscription.getNextBillingDate() != null ? Date.valueOf(subscription.getNextBillingDate()) : null);
            stmt.setInt(7, subscription.getSubscriptionId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int subscriptionId) throws SQLException {
        String sql = "DELETE FROM subscriptions WHERE subscription_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, subscriptionId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<Subscription> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM subscriptions WHERE user_id=? ORDER BY service_name";
        List<Subscription> subscriptions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subscriptions.add(mapResultSetToSubscription(rs));
                }
            }
        }
        return subscriptions;
    }
    
    private Subscription mapResultSetToSubscription(ResultSet rs) throws SQLException {
        Subscription subscription = new Subscription();
        subscription.setSubscriptionId(rs.getInt("subscription_id"));
        subscription.setUserId(rs.getInt("user_id"));
        subscription.setServiceName(rs.getString("service_name"));
        subscription.setMonthlyCost(rs.getDouble("monthly_cost"));
        subscription.setBillingDate(rs.getInt("billing_date"));
        subscription.setCategory(rs.getString("category"));
        
        String statusStr = rs.getString("status");
        subscription.setStatus(statusStr != null && statusStr.equalsIgnoreCase("consider_canceling") ? 
            Subscription.SubscriptionStatus.CONSIDER_CANCELING : Subscription.SubscriptionStatus.KEEP);
        
        Date nextBillingDate = rs.getDate("next_billing_date");
        if (nextBillingDate != null) {
            subscription.setNextBillingDate(nextBillingDate.toLocalDate());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            subscription.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return subscription;
    }
}


