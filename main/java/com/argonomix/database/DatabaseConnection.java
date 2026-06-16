package com.argonomix.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/argonomix_db?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "Awais7865.";
    
    private static String url;
    private static String user;
    private static String password;
    
    static {
        loadDatabaseConfig();
    }
    
    private static void loadDatabaseConfig() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
                url = props.getProperty("db.url", DEFAULT_URL);
                user = props.getProperty("db.user", DEFAULT_USER);
                password = props.getProperty("db.password", DEFAULT_PASSWORD);
            } else {
                // Use defaults if properties file doesn't exist
                url = DEFAULT_URL;
                user = DEFAULT_USER;
                password = DEFAULT_PASSWORD;
            }
        } catch (IOException e) {
            System.err.println("Error loading database properties, using defaults: " + e.getMessage());
            url = DEFAULT_URL;
            user = DEFAULT_USER;
            password = DEFAULT_PASSWORD;
        }
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
    
    public static void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            System.out.println("Database connection successful!");
        }
    }
    
    public static void setConnectionParams(String url, String user, String password) {
        DatabaseConnection.url = url;
        DatabaseConnection.user = user;
        DatabaseConnection.password = password;
    }
}

