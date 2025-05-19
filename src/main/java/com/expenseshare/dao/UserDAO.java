package com.expenseshare.dao;

import com.expenseshare.config.DatabaseConfig;
import com.expenseshare.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return Optional.of(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean create(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(user.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password_hash = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(5, user.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                    );
                    user.setId(rs.getLong("id"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
} 