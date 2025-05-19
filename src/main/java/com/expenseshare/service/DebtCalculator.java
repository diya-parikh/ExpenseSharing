package com.expenseshare.service;

import com.expenseshare.model.Expense;
import com.expenseshare.model.ExpenseSplit;
import com.expenseshare.model.Debt;
import com.expenseshare.dao.UserDAO;
import java.math.BigDecimal;
import java.util.*;

public class DebtCalculator {
    private final List<Expense> expenses;
    private final UserDAO userDAO;
    
    public DebtCalculator(List<Expense> expenses) {
        this.expenses = expenses;
        this.userDAO = new UserDAO();
    }
    
    private Map<Long, BigDecimal> calculateBalances() {
        Map<Long, BigDecimal> balances = new HashMap<>();
        
        for (Expense expense : expenses) {
            // First, subtract the full amount from payer's balance
            balances.merge(expense.getPayerId(), expense.getAmount().negate(), BigDecimal::add);
            
            // Then add split amounts to each member's balance
            for (ExpenseSplit split : expense.getSplits()) {
                balances.merge(split.getUserId(), split.getAmount(), BigDecimal::add);
            }
        }
        
        return balances;
    }
    
    public List<Debt> calculateDebts() {
        List<Debt> debts = new ArrayList<>();
        Map<Long, BigDecimal> balances = calculateBalances();
        
        while (true) {
            // Find max debtor and creditor
            Map.Entry<Long, BigDecimal> maxDebtor = null;
            Map.Entry<Long, BigDecimal> maxCreditor = null;
            
            for (Map.Entry<Long, BigDecimal> entry : balances.entrySet()) {
                if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                    if (maxDebtor == null || entry.getValue().compareTo(maxDebtor.getValue()) < 0) {
                        maxDebtor = entry;
                    }
                } else if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    if (maxCreditor == null || entry.getValue().compareTo(maxCreditor.getValue()) > 0) {
                        maxCreditor = entry;
                    }
                }
            }
            
            // If no more debts to settle, break
            if (maxDebtor == null || maxCreditor == null) {
                break;
            }
            
            // Create debt record
            BigDecimal amount = maxDebtor.getValue().abs().min(maxCreditor.getValue());
            String fromUsername = userDAO.findById(maxDebtor.getKey()).getUsername();
            String toUsername = userDAO.findById(maxCreditor.getKey()).getUsername();
            debts.add(new Debt(maxDebtor.getKey(), maxCreditor.getKey(), amount, fromUsername, toUsername));
            
            // Update balances
            balances.put(maxDebtor.getKey(), maxDebtor.getValue().add(amount));
            balances.put(maxCreditor.getKey(), maxCreditor.getValue().subtract(amount));
        }
        
        return debts;
    }
} 