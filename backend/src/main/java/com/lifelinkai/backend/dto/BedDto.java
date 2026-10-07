package com.lifelinkai.backend.dto;
import lombok.Data;
import com.lifelinkai.backend.model.BedType;
import com.lifelinkai.backend.model.BedStatus;
import java.time.LocalDateTime;
@Data
public class BedDto {
    private String id;
    private String hospitalId;
    private String bedNumber;
    private BedType type;
    private BedStatus status;
    private String floor;
    private String department;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}