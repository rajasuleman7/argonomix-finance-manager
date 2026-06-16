package com.argonomix.database;

import com.argonomix.models.Goal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {
    
    public boolean save(Goal goal) throws SQLException {
        String sql = "INSERT INTO goals (user_id, goal_name, target_amount, current_amount, deadline, goal_icon, is_completed) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, goal.getUserId());
            stmt.setString(2, goal.getGoalName());
            stmt.setDouble(3, goal.getTargetAmount());
            stmt.setDouble(4, goal.getCurrentAmount());
            stmt.setDate(5, goal.getDeadline() != null ? Date.valueOf(goal.getDeadline()) : null);
            stmt.setString(6, goal.getGoalIcon());
            stmt.setBoolean(7, goal.isCompleted());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        goal.setGoalId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    public boolean update(Goal goal) throws SQLException {
        String sql = "UPDATE goals SET goal_name=?, target_amount=?, current_amount=?, deadline=?, goal_icon=?, is_completed=? WHERE goal_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, goal.getGoalName());
            stmt.setDouble(2, goal.getTargetAmount());
            stmt.setDouble(3, goal.getCurrentAmount());
            stmt.setDate(4, goal.getDeadline() != null ? Date.valueOf(goal.getDeadline()) : null);
            stmt.setString(5, goal.getGoalIcon());
            stmt.setBoolean(6, goal.isCompleted());
            stmt.setInt(7, goal.getGoalId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int goalId) throws SQLException {
        String sql = "DELETE FROM goals WHERE goal_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, goalId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public Goal findById(int goalId) throws SQLException {
        String sql = "SELECT * FROM goals WHERE goal_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, goalId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGoal(rs);
                }
            }
        }
        return null;
    }
    
    public List<Goal> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM goals WHERE user_id=? ORDER BY is_completed, deadline";
        List<Goal> goals = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    goals.add(mapResultSetToGoal(rs));
                }
            }
        }
        return goals;
    }
    
    public List<Goal> findActiveByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM goals WHERE user_id=? AND is_completed=FALSE ORDER BY deadline";
        List<Goal> goals = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    goals.add(mapResultSetToGoal(rs));
                }
            }
        }
        return goals;
    }
    
    private Goal mapResultSetToGoal(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setGoalId(rs.getInt("goal_id"));
        goal.setUserId(rs.getInt("user_id"));
        goal.setGoalName(rs.getString("goal_name"));
        goal.setTargetAmount(rs.getDouble("target_amount"));
        goal.setCurrentAmount(rs.getDouble("current_amount"));
        Date deadline = rs.getDate("deadline");
        if (deadline != null) {
            goal.setDeadline(deadline.toLocalDate());
        }
        goal.setGoalIcon(rs.getString("goal_icon"));
        goal.setCompleted(rs.getBoolean("is_completed"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            goal.setCreatedAt(createdAt.toLocalDateTime());
        }
        return goal;
    }
}

