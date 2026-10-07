package com.lifelinkai.backend.dto;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BedCapacityDto {
    private long totalBeds;
    private long availableBeds;
    private long occupiedBeds;
    private long reservedBeds;
    private long maintenanceBeds;
    private long totalIcuBeds;
    private long availableIcuBeds;
    private long occupiedIcuBeds;
}