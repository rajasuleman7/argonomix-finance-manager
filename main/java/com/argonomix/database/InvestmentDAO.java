package com.argonomix.database;

import com.argonomix.models.Investment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestmentDAO {
    
    public boolean save(Investment investment) throws SQLException {
        String sql = "INSERT INTO investments (user_id, investment_name, investment_type, amount_invested, current_value, purchase_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, investment.getUserId());
            stmt.setString(2, investment.getInvestmentName());
            stmt.setString(3, investment.getInvestmentType());
            stmt.setDouble(4, investment.getAmountInvested());
            stmt.setDouble(5, investment.getCurrentValue());
            stmt.setDate(6, investment.getPurchaseDate() != null ? Date.valueOf(investment.getPurchaseDate()) : null);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        investment.setInvestmentId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    public boolean update(Investment investment) throws SQLException {
        String sql = "UPDATE investments SET investment_name=?, investment_type=?, amount_invested=?, current_value=?, purchase_date=? WHERE investment_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, investment.getInvestmentName());
            stmt.setString(2, investment.getInvestmentType());
            stmt.setDouble(3, investment.getAmountInvested());
            stmt.setDouble(4, investment.getCurrentValue());
            stmt.setDate(5, investment.getPurchaseDate() != null ? Date.valueOf(investment.getPurchaseDate()) : null);
            stmt.setInt(6, investment.getInvestmentId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int investmentId) throws SQLException {
        String sql = "DELETE FROM investments WHERE investment_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, investmentId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public Investment findById(int investmentId) throws SQLException {
        String sql = "SELECT * FROM investments WHERE investment_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, investmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvestment(rs);
                }
            }
        }
        return null;
    }
    
    public List<Investment> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM investments WHERE user_id=? ORDER BY purchase_date DESC";
        List<Investment> investments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    investments.add(mapResultSetToInvestment(rs));
                }
            }
        }
        return investments;
    }
    
    private Investment mapResultSetToInvestment(ResultSet rs) throws SQLException {
        Investment investment = new Investment();
        investment.setInvestmentId(rs.getInt("investment_id"));
        investment.setUserId(rs.getInt("user_id"));
        investment.setInvestmentName(rs.getString("investment_name"));
        investment.setInvestmentType(rs.getString("investment_type"));
        investment.setAmountInvested(rs.getDouble("amount_invested"));
        investment.setCurrentValue(rs.getDouble("current_value"));
        Date purchaseDate = rs.getDate("purchase_date");
        if (purchaseDate != null) {
            investment.setPurchaseDate(purchaseDate.toLocalDate());
        }
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            investment.setCreatedAt(createdAt.toLocalDateTime());
        }
        return investment;
    }
}


