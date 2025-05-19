package com.expenseshare.dao;

import com.expenseshare.config.DatabaseConfig;
import com.expenseshare.model.Group;
import com.expenseshare.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupDAO {
    
    public List<User> getGroupMembers(Long groupId) {
        List<User> members = new ArrayList<>();
        String sql = """
            SELECT u.* FROM users u
            JOIN user_group ug ON u.id = ug.user_id
            WHERE ug.group_id = ?
            ORDER BY u.username
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, groupId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                members.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }
    
    public List<Group> findByUserId(Long userId) {
        List<Group> groups = new ArrayList<>();
        String sql = """
            SELECT g.* FROM groups g
            JOIN user_group ug ON g.id = ug.group_id
            WHERE ug.user_id = ?
            ORDER BY g.created_at DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Group group = new Group();
                group.setId(rs.getLong("id"));
                group.setName(rs.getString("name"));
                group.setDescription(rs.getString("description"));
                group.setCreatedBy(rs.getLong("created_by"));
                group.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                group.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                groups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }
    
    public Optional<Group> findById(Long id) {
        String sql = "SELECT * FROM groups WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Group group = new Group();
                group.setId(rs.getLong("id"));
                group.setName(rs.getString("name"));
                group.setDescription(rs.getString("description"));
                group.setCreatedBy(rs.getLong("created_by"));
                group.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                group.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return Optional.of(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public boolean create(Group group) {
        String sql = "INSERT INTO groups (name, description, created_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, group.getName());
            stmt.setString(2, group.getDescription());
            stmt.setLong(3, group.getCreatedBy());
            stmt.setTimestamp(4, Timestamp.valueOf(group.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(group.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    group.setId(rs.getLong(1));
                    return addUserToGroup(group.getId(), group.getCreatedBy(), "ADMIN");
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addUserToGroup(Long groupId, Long userId, String role) {
        String sql = "INSERT INTO user_group (user_id, group_id, role, joined_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, groupId);
            stmt.setString(3, role);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean removeUserFromGroup(Long groupId, Long userId) {
        String sql = "DELETE FROM user_group WHERE group_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, groupId);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean update(Group group) {
        String sql = "UPDATE groups SET name = ?, description = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, group.getName());
            stmt.setString(2, group.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(4, group.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Long groupId) {
        // First delete all user_group associations
        String deleteUserGroupSql = "DELETE FROM user_group WHERE group_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteUserGroupSql)) {
            
            stmt.setLong(1, groupId);
            stmt.executeUpdate();
            
            // Then delete the group
            String deleteGroupSql = "DELETE FROM groups WHERE id = ?";
            try (PreparedStatement groupStmt = conn.prepareStatement(deleteGroupSql)) {
                groupStmt.setLong(1, groupId);
                return groupStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 