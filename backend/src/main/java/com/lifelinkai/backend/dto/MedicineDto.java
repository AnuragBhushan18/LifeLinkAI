package com.lifelinkai.backend.dto;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class MedicineDto {
    private String id;
    private String pharmacyId;
    private String name;
    private String category;
    private int quantity;
    private double unitPrice;
    private LocalDateTime expiryDate;
    private String description;
    private int lowStockThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}