package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "patients")
public class Patient {
    @Id private String id;
    private String userId;
    private String dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String address;
    private String city;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private List<String> allergies;
    private List<String> currentMedications;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
