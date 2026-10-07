package com.lifelinkai.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blood_inventories")
public class BloodInventory {
    @Id
    private String id;
    
    @Indexed
    private String bloodBankId;
    
    @Indexed
    private BloodGroup bloodGroup;
    
    private int unitsAvailable;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
