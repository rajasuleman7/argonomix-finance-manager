package com.argonomix.database;

import com.argonomix.models.Transaction;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    
    public boolean save(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, amount, category, description, transaction_type, transaction_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, transaction.getUserId());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, transaction.getCategory());
            stmt.setString(4, transaction.getDescription());
            stmt.setString(5, transaction.getTransactionType().name().toLowerCase());
            stmt.setDate(6, Date.valueOf(transaction.getTransactionDate()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        transaction.setTransactionId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    public boolean update(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET amount=?, category=?, description=?, transaction_type=?, transaction_date=? WHERE transaction_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, transaction.getAmount());
            stmt.setString(2, transaction.getCategory());
            stmt.setString(3, transaction.getDescription());
            stmt.setString(4, transaction.getTransactionType().name().toLowerCase());
            stmt.setDate(5, Date.valueOf(transaction.getTransactionDate()));
            stmt.setInt(6, transaction.getTransactionId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int transactionId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE transaction_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public Transaction findById(int transactionId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        }
        return null;
    }
    
    public List<Transaction> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id=? ORDER BY transaction_date DESC, created_at DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
    
    public List<Transaction> findByUserIdAndDateRange(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id=? AND transaction_date BETWEEN ? AND ? ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
    
    public List<Transaction> findByUserIdAndCategory(int userId, String category) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id=? AND category=? ORDER BY transaction_date DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
    
    public double getTotalIncomeForMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT SUM(amount) FROM transactions WHERE user_id=? AND transaction_type='income' AND MONTH(transaction_date)=? AND YEAR(transaction_date)=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
    
    public double getTotalExpensesForMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT SUM(amount) FROM transactions WHERE user_id=? AND transaction_type='expense' AND MONTH(transaction_date)=? AND YEAR(transaction_date)=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
    
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setCategory(rs.getString("category"));
        transaction.setDescription(rs.getString("description"));
        
        String typeStr = rs.getString("transaction_type");
        transaction.setTransactionType(typeStr.equalsIgnoreCase("income") ? 
            Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE);
        
        Date date = rs.getDate("transaction_date");
        if (date != null) {
            transaction.setTransactionDate(date.toLocalDate());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return transaction;
    }
}

