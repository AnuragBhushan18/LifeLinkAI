package com.lifelinkai.backend.dto;
import lombok.Data;
import com.lifelinkai.backend.model.BloodGroup;
import java.time.LocalDateTime;
@Data
public class BloodInventoryDto {
    private String id;
    private String bloodBankId;
    private BloodGroup bloodGroup;
    private int unitsAvailable;
    private LocalDateTime updatedAt;
}