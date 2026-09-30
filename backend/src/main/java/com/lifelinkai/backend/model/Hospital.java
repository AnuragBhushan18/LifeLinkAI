package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "hospitals")
public class Hospital {
    @Id private String id;
    private String userId;
    private String name;
    private String registrationNumber;
    private String phone;
    private String email;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private OperationalStatus operationalStatus = OperationalStatus.ACTIVE;
    private Integer totalBeds = 0;
    private Integer availableBeds = 0;
    private Integer totalIcuBeds = 0;
    private Integer availableIcuBeds = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
