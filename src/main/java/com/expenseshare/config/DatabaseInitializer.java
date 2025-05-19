package com.expenseshare.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGSERIAL PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password_hash VARCHAR(60) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
            """);
            
            // Create categories table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(50) UNIQUE NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
            """);
            
            // Create groups table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS groups (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    description TEXT,
                    created_by BIGINT NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL,
                    FOREIGN KEY (created_by) REFERENCES users(id)
                )
            """);
            
            // Create user_group table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_group (
                    user_id BIGINT NOT NULL,
                    group_id BIGINT NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    joined_at TIMESTAMP NOT NULL,
                    PRIMARY KEY (user_id, group_id),
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (group_id) REFERENCES groups(id)
                )
            """);
            
            // Create expenses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id BIGSERIAL PRIMARY KEY,
                    group_id BIGINT NOT NULL,
                    payer_id BIGINT NOT NULL,
                    category_id BIGINT NOT NULL,
                    description VARCHAR(255) NOT NULL,
                    amount DECIMAL(10,2) NOT NULL,
                    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
                    split_method VARCHAR(20) NOT NULL CHECK (split_method IN ('EQUAL', 'EXACT', 'PERCENTAGE')),
                    date TIMESTAMP NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL,
                    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
                    FOREIGN KEY (payer_id) REFERENCES users(id),
                    FOREIGN KEY (category_id) REFERENCES categories(id)
                )
            """);
            
            // Create expense_splits table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS expense_splits (
                    id BIGSERIAL PRIMARY KEY,
                    expense_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    amount DECIMAL(10,2) NOT NULL,
                    percentage DECIMAL(5,2) NOT NULL,
                    is_paid BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL,
                    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);
            
            // Create settlements table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS settlements (
                    id BIGSERIAL PRIMARY KEY,
                    payer_id BIGINT NOT NULL,
                    payee_id BIGINT NOT NULL,
                    amount DECIMAL(10,2) NOT NULL,
                    group_id BIGINT NOT NULL,
                    settled_at TIMESTAMP NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL,
                    FOREIGN KEY (payer_id) REFERENCES users(id),
                    FOREIGN KEY (payee_id) REFERENCES users(id),
                    FOREIGN KEY (group_id) REFERENCES groups(id)
                )
            """);
            
            System.out.println("Database tables initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 