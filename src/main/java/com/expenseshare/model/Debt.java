package com.expenseshare.model;

import java.math.BigDecimal;

public class Debt {
    private final Long fromUserId;
    private final Long toUserId;
    private final BigDecimal amount;
    private final String fromUsername;
    private final String toUsername;
    
    public Debt(Long fromUserId, Long toUserId, BigDecimal amount, String fromUsername, String toUsername) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
    }
    
    public Long getFromUserId() { return fromUserId; }
    public Long getToUserId() { return toUserId; }
    public BigDecimal getAmount() { return amount; }
    public String getFromUsername() { return fromUsername; }
    public String getToUsername() { return toUsername; }
} 