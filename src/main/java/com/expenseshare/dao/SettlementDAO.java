package com.expenseshare.dao;

import com.expenseshare.config.DatabaseConfig;
import com.expenseshare.model.Debt;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class SettlementDAO {
    
    public boolean recordSettlements(Long groupId, List<Debt> debts) {
        String sql = "INSERT INTO settlements (group_id, from_user_id, to_user_id, amount, settled_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Debt debt : debts) {
                stmt.setLong(1, groupId);
                stmt.setLong(2, debt.getFromUserId());
                stmt.setLong(3, debt.getToUserId());
                stmt.setBigDecimal(4, debt.getAmount());
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 