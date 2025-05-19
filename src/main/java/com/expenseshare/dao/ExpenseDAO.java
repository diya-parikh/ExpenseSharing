package com.expenseshare.dao;

import com.expenseshare.config.DatabaseConfig;
import com.expenseshare.model.Expense;
import com.expenseshare.model.ExpenseSplit;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {
    
    public List<Expense> findByGroupId(Long groupId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE group_id = ? ORDER BY date DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Expense expense = new Expense(
                        rs.getLong("group_id"),
                        rs.getLong("payer_id"),
                        rs.getLong("category_id"),
                        rs.getString("description"),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        rs.getString("split_method"),
                        rs.getTimestamp("date").toLocalDateTime()
                    );
                    expense.setId(rs.getLong("id"));
                    expense.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    expense.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    
                    // Load splits for this expense
                    expense.setSplits(getExpenseSplits(expense.getId()));
                    
                    expenses.add(expense);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    public Expense findById(Long id) {
        String sql = "SELECT * FROM expenses WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Expense expense = new Expense(
                        rs.getLong("group_id"),
                        rs.getLong("payer_id"),
                        rs.getLong("category_id"),
                        rs.getString("description"),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency"),
                        rs.getString("split_method"),
                        rs.getTimestamp("date").toLocalDateTime()
                    );
                    expense.setId(rs.getLong("id"));
                    expense.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    expense.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    
                    // Load splits for this expense
                    expense.setSplits(getExpenseSplits(expense.getId()));
                    
                    return expense;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(Expense expense) {
        String sql = "INSERT INTO expenses (group_id, payer_id, category_id, description, amount, " +
                    "currency, split_method, date, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, expense.getGroupId());
            stmt.setLong(2, expense.getPayerId());
            stmt.setLong(3, expense.getCategoryId());
            stmt.setString(4, expense.getDescription());
            stmt.setBigDecimal(5, expense.getAmount());
            stmt.setString(6, expense.getCurrency());
            stmt.setString(7, expense.getSplitMethod());
            stmt.setTimestamp(8, Timestamp.valueOf(expense.getDate()));
            stmt.setTimestamp(9, Timestamp.valueOf(expense.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.valueOf(expense.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        expense.setId(rs.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Expense expense) {
        String sql = "UPDATE expenses SET description = ?, amount = ?, currency = ?, " +
                    "split_method = ?, date = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, expense.getDescription());
            stmt.setBigDecimal(2, expense.getAmount());
            stmt.setString(3, expense.getCurrency());
            stmt.setString(4, expense.getSplitMethod());
            stmt.setTimestamp(5, Timestamp.valueOf(expense.getDate()));
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(7, expense.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(Long id) {
        // First delete all expense splits
        String deleteSplitsSql = "DELETE FROM expense_splits WHERE expense_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSplitsSql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
            // Then delete the expense
            String deleteExpenseSql = "DELETE FROM expenses WHERE id = ?";
            try (PreparedStatement expenseStmt = conn.prepareStatement(deleteExpenseSql)) {
                expenseStmt.setLong(1, id);
                return expenseStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ExpenseSplit> getExpenseSplits(Long expenseId) {
        List<ExpenseSplit> splits = new ArrayList<>();
        String sql = "SELECT * FROM expense_splits WHERE expense_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, expenseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExpenseSplit split = new ExpenseSplit(
                        rs.getLong("expense_id"),
                        rs.getLong("user_id"),
                        rs.getBigDecimal("amount"),
                        rs.getBigDecimal("percentage")
                    );
                    split.setId(rs.getLong("id"));
                    split.setPaid(rs.getBoolean("is_paid"));
                    splits.add(split);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return splits;
    }

    public boolean createExpenseSplit(ExpenseSplit split) {
        String sql = "INSERT INTO expense_splits (expense_id, user_id, amount, percentage, is_paid, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            stmt.setLong(1, split.getExpenseId());
            stmt.setLong(2, split.getUserId());
            stmt.setBigDecimal(3, split.getAmount());
            stmt.setBigDecimal(4, split.getPercentage());
            stmt.setBoolean(5, split.isPaid());
            stmt.setTimestamp(6, Timestamp.valueOf(now));
            stmt.setTimestamp(7, Timestamp.valueOf(now));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        split.setId(rs.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateExpenseSplit(ExpenseSplit split) {
        String sql = "UPDATE expense_splits SET amount = ?, percentage = ?, is_paid = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, split.getAmount());
            stmt.setBigDecimal(2, split.getPercentage());
            stmt.setBoolean(3, split.isPaid());
            stmt.setLong(4, split.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteExpenseSplit(Long id) {
        String sql = "DELETE FROM expense_splits WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 