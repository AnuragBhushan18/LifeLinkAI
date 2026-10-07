package com.lifelinkai.backend.dto;
import lombok.Data;
import com.lifelinkai.backend.model.OperationalStatus;
import java.time.LocalDateTime;
@Data
public class PharmacyDto {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private OperationalStatus operationalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}