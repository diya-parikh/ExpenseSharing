package com.expenseshare.dao;

import com.expenseshare.model.Category;
import com.expenseshare.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private final DatabaseConfig dbConfig;

    public CategoryDAO() {
        this.dbConfig = new DatabaseConfig();
    }

    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";

        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public Category findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return category;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Category findByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return category;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(Category category) {
        String sql = "INSERT INTO categories (name) VALUES (?)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, category.getName());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getLong(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.getName());
            pstmt.setLong(2, category.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 