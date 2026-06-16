-- ARGonomix Database Schema
-- MySQL Database for JavaFX Desktop Application

CREATE DATABASE IF NOT EXISTS argonomix_db;
USE argonomix_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    monthly_income DECIMAL(10,2),
    hourly_wage DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    category VARCHAR(100),
    description VARCHAR(255),
    transaction_type ENUM('income', 'expense') NOT NULL,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, transaction_date),
    INDEX idx_category (category)
);

-- Budgets table
CREATE TABLE IF NOT EXISTS budgets (
    budget_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    category VARCHAR(100),
    amount_limit DECIMAL(10,2),
    period VARCHAR(20) DEFAULT 'monthly',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_category (user_id, category)
);

-- Goals table
CREATE TABLE IF NOT EXISTS goals (
    goal_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    goal_name VARCHAR(255),
    target_amount DECIMAL(10,2),
    current_amount DECIMAL(10,2) DEFAULT 0,
    deadline DATE,
    goal_icon VARCHAR(50),
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
);

-- Investments table
CREATE TABLE IF NOT EXISTS investments (
    investment_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    investment_name VARCHAR(255),
    investment_type VARCHAR(100),
    amount_invested DECIMAL(10,2),
    current_value DECIMAL(10,2),
    purchase_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
);

-- Bill split groups table
CREATE TABLE IF NOT EXISTS bill_split_groups (
    group_id INT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(255),
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_created_by (created_by)
);

-- Group members table
CREATE TABLE IF NOT EXISTS group_members (
    member_id INT PRIMARY KEY AUTO_INCREMENT,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (group_id) REFERENCES bill_split_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_member (group_id, user_id)
);

-- Shared expenses table
CREATE TABLE IF NOT EXISTS shared_expenses (
    expense_id INT PRIMARY KEY AUTO_INCREMENT,
    group_id INT NOT NULL,
    description VARCHAR(255),
    total_amount DECIMAL(10,2),
    paid_by INT,
    expense_date DATE,
    is_settled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES bill_split_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (paid_by) REFERENCES users(user_id),
    INDEX idx_group (group_id)
);

-- Expense splits table
CREATE TABLE IF NOT EXISTS expense_splits (
    split_id INT PRIMARY KEY AUTO_INCREMENT,
    expense_id INT NOT NULL,
    user_id INT NOT NULL,
    amount_owed DECIMAL(10,2),
    is_paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (expense_id) REFERENCES shared_expenses(expense_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_expense (expense_id)
);

-- Challenges table
CREATE TABLE IF NOT EXISTS challenges (
    challenge_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    challenge_name VARCHAR(255),
    challenge_type VARCHAR(100),
    start_date DATE,
    end_date DATE,
    days_completed INT DEFAULT 0,
    total_days INT,
    amount_saved DECIMAL(10,2) DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    current_streak INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
);

-- Subscriptions table
CREATE TABLE IF NOT EXISTS subscriptions (
    subscription_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    service_name VARCHAR(255),
    monthly_cost DECIMAL(10,2),
    billing_date INT,
    category VARCHAR(100),
    status ENUM('keep', 'consider_canceling') DEFAULT 'keep',
    next_billing_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user (user_id)
);

-- User settings table
CREATE TABLE IF NOT EXISTS user_settings (
    setting_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    theme VARCHAR(20) DEFAULT 'light',
    currency VARCHAR(10) DEFAULT 'USD',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    onboarding_complete BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(255),
    message TEXT,
    severity ENUM('info', 'warning', 'error', 'success') DEFAULT 'info',
    notification_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_read (user_id, is_read)
);

