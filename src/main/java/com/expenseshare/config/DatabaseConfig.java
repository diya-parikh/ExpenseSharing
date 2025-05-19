package com.expenseshare.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:postgresql://db.xycokmruduurqrjwwbpj.supabase.co:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Mitwpu@123#";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 