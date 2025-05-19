package com.expenseshare.util;

import com.expenseshare.model.Debt;
import com.expenseshare.model.Expense;
import com.expenseshare.model.ExpenseSplit;
import com.expenseshare.model.User;
import java.math.BigDecimal;
import java.util.*;

public class DebtCalculator {
    
    public static class UserBalance {
        private final Long userId;
        private final String username;
        private BigDecimal balance;
        
        public UserBalance(Long userId, String username) {
            this.userId = userId;
            this.username = username;
            this.balance = BigDecimal.ZERO;
        }
        
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public BigDecimal getBalance() { return balance; }
        public void addToBalance(BigDecimal amount) { this.balance = this.balance.add(amount); }
    }
    
    public static Map<Long, UserBalance> calculateBalances(List<Expense> expenses, List<User> users) {
        Map<Long, UserBalance> balances = new HashMap<>();
        
        // Initialize balances for all users
        for (User user : users) {
            balances.put(user.getId(), new UserBalance(user.getId(), user.getUsername()));
        }
        
        System.out.println("Calculating balances for expenses:");
        
        for (Expense expense : expenses) {
            System.out.println("Processing expense: amount=" + expense.getAmount() + 
                             ", payer=" + expense.getPayerId());
            
            // Process each split
            for (ExpenseSplit split : expense.getSplits()) {
                System.out.println("Split: userId=" + split.getUserId() + 
                                 ", amount=" + split.getAmount());
                
                // Person who owes money (split.getUserId()) gets negative balance
                balances.get(split.getUserId()).addToBalance(split.getAmount().negate());
                
                // Person who paid (expense.getPayerId()) gets positive balance
                balances.get(expense.getPayerId()).addToBalance(split.getAmount());
            }
        }
        
        // Debug print final balances
        for (UserBalance balance : balances.values()) {
            System.out.println("Final balance for user " + balance.getUserId() + 
                             ": " + balance.getBalance());
        }
        
        return balances;
    }
    
    public static List<Debt> simplifyDebts(Map<Long, UserBalance> balances) {
        List<Debt> debts = new ArrayList<>();
        List<UserBalance> positiveBalances = new ArrayList<>();
        List<UserBalance> negativeBalances = new ArrayList<>();
        
        // Separate positive and negative balances
        for (UserBalance balance : balances.values()) {
            if (balance.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                positiveBalances.add(balance);
            } else if (balance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                negativeBalances.add(balance);
            }
        }
        
        // Sort by absolute balance amount
        positiveBalances.sort((a, b) -> b.getBalance().abs().compareTo(a.getBalance().abs()));
        negativeBalances.sort((a, b) -> b.getBalance().abs().compareTo(a.getBalance().abs()));
        
        // Match positive and negative balances
        int posIndex = 0;
        int negIndex = 0;
        
        while (posIndex < positiveBalances.size() && negIndex < negativeBalances.size()) {
            UserBalance pos = positiveBalances.get(posIndex);
            UserBalance neg = negativeBalances.get(negIndex);
            
            BigDecimal amount = pos.getBalance().min(neg.getBalance().abs());
            
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                debts.add(new Debt(
                    neg.getUserId(),
                    pos.getUserId(),
                    amount,
                    neg.getUsername(),
                    pos.getUsername()
                ));
                
                pos.addToBalance(amount.negate());
                neg.addToBalance(amount);
            }
            
            if (pos.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                posIndex++;
            }
            if (neg.getBalance().compareTo(BigDecimal.ZERO) >= 0) {
                negIndex++;
            }
        }
        
        return debts;
    }
    
    public static BigDecimal calculateUserBalance(Map<Long, UserBalance> balances, Long userId) {
        UserBalance balance = balances.get(userId);
        return balance != null ? balance.getBalance() : BigDecimal.ZERO;
    }
} 