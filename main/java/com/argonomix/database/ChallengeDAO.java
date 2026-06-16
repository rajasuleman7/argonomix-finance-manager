package com.argonomix.database;

import com.argonomix.models.Challenge;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChallengeDAO {
    
    public boolean save(Challenge challenge) throws SQLException {
        String sql = "INSERT INTO challenges (user_id, challenge_name, challenge_type, start_date, end_date, days_completed, total_days, amount_saved, is_completed, current_streak) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, challenge.getUserId());
            stmt.setString(2, challenge.getChallengeName());
            stmt.setString(3, challenge.getChallengeType());
            stmt.setDate(4, challenge.getStartDate() != null ? Date.valueOf(challenge.getStartDate()) : null);
            stmt.setDate(5, challenge.getEndDate() != null ? Date.valueOf(challenge.getEndDate()) : null);
            stmt.setInt(6, challenge.getDaysCompleted());
            stmt.setInt(7, challenge.getTotalDays());
            stmt.setDouble(8, challenge.getAmountSaved());
            stmt.setBoolean(9, challenge.isCompleted());
            stmt.setInt(10, challenge.getCurrentStreak());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        challenge.setChallengeId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    public boolean update(Challenge challenge) throws SQLException {
        String sql = "UPDATE challenges SET challenge_name=?, challenge_type=?, start_date=?, end_date=?, days_completed=?, total_days=?, amount_saved=?, is_completed=?, current_streak=? WHERE challenge_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, challenge.getChallengeName());
            stmt.setString(2, challenge.getChallengeType());
            stmt.setDate(3, challenge.getStartDate() != null ? Date.valueOf(challenge.getStartDate()) : null);
            stmt.setDate(4, challenge.getEndDate() != null ? Date.valueOf(challenge.getEndDate()) : null);
            stmt.setInt(5, challenge.getDaysCompleted());
            stmt.setInt(6, challenge.getTotalDays());
            stmt.setDouble(7, challenge.getAmountSaved());
            stmt.setBoolean(8, challenge.isCompleted());
            stmt.setInt(9, challenge.getCurrentStreak());
            stmt.setInt(10, challenge.getChallengeId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int challengeId) throws SQLException {
        String sql = "DELETE FROM challenges WHERE challenge_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, challengeId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<Challenge> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM challenges WHERE user_id=? ORDER BY is_completed, start_date DESC";
        List<Challenge> challenges = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    challenges.add(mapResultSetToChallenge(rs));
                }
            }
        }
        return challenges;
    }
    
    public List<Challenge> findActiveByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM challenges WHERE user_id=? AND is_completed=FALSE ORDER BY start_date DESC";
        List<Challenge> challenges = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    challenges.add(mapResultSetToChallenge(rs));
                }
            }
        }
        return challenges;
    }
    
    private Challenge mapResultSetToChallenge(ResultSet rs) throws SQLException {
        Challenge challenge = new Challenge();
        challenge.setChallengeId(rs.getInt("challenge_id"));
        challenge.setUserId(rs.getInt("user_id"));
        challenge.setChallengeName(rs.getString("challenge_name"));
        challenge.setChallengeType(rs.getString("challenge_type"));
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            challenge.setStartDate(startDate.toLocalDate());
        }
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            challenge.setEndDate(endDate.toLocalDate());
        }
        challenge.setDaysCompleted(rs.getInt("days_completed"));
        challenge.setTotalDays(rs.getInt("total_days"));
        challenge.setAmountSaved(rs.getDouble("amount_saved"));
        challenge.setCompleted(rs.getBoolean("is_completed"));
        challenge.setCurrentStreak(rs.getInt("current_streak"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            challenge.setCreatedAt(createdAt.toLocalDateTime());
        }
        return challenge;
    }
}


