package com.expenseshare.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    private Long id;
    private String name;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Group(String name, String description, Long createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 