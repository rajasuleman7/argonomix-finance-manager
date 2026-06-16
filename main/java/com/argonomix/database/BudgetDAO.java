package com.argonomix.database;

import com.argonomix.models.Budget;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {
    
    public boolean save(Budget budget) throws SQLException {
        String sql = "INSERT INTO budgets (user_id, category, amount_limit, period) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, budget.getUserId());
            stmt.setString(2, budget.getCategory());
            stmt.setDouble(3, budget.getAmountLimit());
            stmt.setString(4, budget.getPeriod());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        budget.setBudgetId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    public boolean update(Budget budget) throws SQLException {
        String sql = "UPDATE budgets SET category=?, amount_limit=?, period=? WHERE budget_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, budget.getCategory());
            stmt.setDouble(2, budget.getAmountLimit());
            stmt.setString(3, budget.getPeriod());
            stmt.setInt(4, budget.getBudgetId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int budgetId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE budget_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, budgetId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public Budget findById(int budgetId) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE budget_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, budgetId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBudget(rs);
                }
            }
        }
        return null;
    }
    
    public List<Budget> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE user_id=? ORDER BY category";
        List<Budget> budgets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    budgets.add(mapResultSetToBudget(rs));
                }
            }
        }
        return budgets;
    }
    
    public Budget findByUserIdAndCategory(int userId, String category) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE user_id=? AND category=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, category);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBudget(rs);
                }
            }
        }
        return null;
    }
    
    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setBudgetId(rs.getInt("budget_id"));
        budget.setUserId(rs.getInt("user_id"));
        budget.setCategory(rs.getString("category"));
        budget.setAmountLimit(rs.getDouble("amount_limit"));
        budget.setPeriod(rs.getString("period"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            budget.setCreatedAt(createdAt.toLocalDateTime());
        }
        return budget;
    }
}

