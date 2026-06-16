package com.argonomix.database;

import com.argonomix.models.UserSettings;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserSettingsDAO {
    
    public boolean save(UserSettings settings) throws SQLException {
        String sql = "INSERT INTO user_settings (user_id, theme, currency, notifications_enabled, onboarding_complete) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, settings.getUserId());
            stmt.setString(2, settings.getTheme());
            stmt.setString(3, settings.getCurrency());
            stmt.setBoolean(4, settings.isNotificationsEnabled());
            stmt.setBoolean(5, settings.isOnboardingComplete());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        settings.setSettingId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    public boolean update(UserSettings settings) throws SQLException {
        String sql = "UPDATE user_settings SET theme=?, currency=?, notifications_enabled=?, onboarding_complete=? WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, settings.getTheme());
            stmt.setString(2, settings.getCurrency());
            stmt.setBoolean(3, settings.isNotificationsEnabled());
            stmt.setBoolean(4, settings.isOnboardingComplete());
            stmt.setInt(5, settings.getUserId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public UserSettings findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM user_settings WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSettings(rs);
                }
            }
        }
        return null;
    }
    
    public UserSettings getOrCreate(int userId) throws SQLException {
        UserSettings settings = findByUserId(userId);
        if (settings == null) {
            settings = new UserSettings(userId);
            save(settings);
        }
        return settings;
    }
    
    private UserSettings mapResultSetToSettings(ResultSet rs) throws SQLException {
        UserSettings settings = new UserSettings();
        settings.setSettingId(rs.getInt("setting_id"));
        settings.setUserId(rs.getInt("user_id"));
        settings.setTheme(rs.getString("theme"));
        settings.setCurrency(rs.getString("currency"));
        settings.setNotificationsEnabled(rs.getBoolean("notifications_enabled"));
        settings.setOnboardingComplete(rs.getBoolean("onboarding_complete"));
        return settings;
    }
}


