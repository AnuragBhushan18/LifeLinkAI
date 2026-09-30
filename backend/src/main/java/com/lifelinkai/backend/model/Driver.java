package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "drivers")
public class Driver {
    @Id private String id;
    private String userId;
    private String licenseNumber;
    private String phone;
    private Integer experienceYears;
    private Availability availability = Availability.AVAILABLE;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
