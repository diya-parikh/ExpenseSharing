package com.expenseshare.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Expense {
    private Long id;
    private Long groupId;
    private Long payerId;
    private Long categoryId;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String splitMethod; // EQUAL, EXACT, PERCENTAGE
    private LocalDateTime date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ExpenseSplit> splits;

    public Expense(Long groupId, Long payerId, Long categoryId, String description, 
                  BigDecimal amount, String currency, String splitMethod, LocalDateTime date) {
        this.groupId = groupId;
        this.payerId = payerId;
        this.categoryId = categoryId;
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.splitMethod = splitMethod;
        this.date = date;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.splits = new ArrayList<>();
    }

    public List<ExpenseSplit> getSplits() {
        return splits;
    }

    public void setSplits(List<ExpenseSplit> splits) {
        this.splits = splits;
    }
} 