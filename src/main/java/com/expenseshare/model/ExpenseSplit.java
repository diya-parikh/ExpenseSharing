package com.expenseshare.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExpenseSplit {
    private Long id;
    private Long expenseId;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal percentage;
    private boolean isPaid;

    public ExpenseSplit(Long expenseId, Long userId, BigDecimal amount, BigDecimal percentage) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.amount = amount;
        this.percentage = percentage;
        this.isPaid = false;
    }
} 