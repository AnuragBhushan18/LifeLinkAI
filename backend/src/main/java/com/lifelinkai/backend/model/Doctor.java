package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "doctors")
public class Doctor {
    @Id private String id;
    private String userId;
    private String hospitalId;
    private String name;
    private String specialization;
    private String department;
    private String licenseNumber;
    private String phone;
    private Availability availability = Availability.AVAILABLE;
    private String shift;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
