package com.lifelinkai.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medicines")
public class Medicine {
    @Id
    private String id;
    
    @Indexed
    private String pharmacyId;
    
    @Indexed
    private String name;
    
    @Indexed
    private String category;
    
    private int quantity;
    private double unitPrice;
    
    private LocalDateTime expiryDate;
    
    private String description;
    
    private int lowStockThreshold;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
