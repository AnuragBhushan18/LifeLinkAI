package com.lifelinkai.backend.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "ambulances")
public class Ambulance {
    @Id private String id;
    private String vehicleNumber;
    private String hospitalId;
    private String driverId;
    private AmbulanceType type;
    private List<String> equipment;
    private Double latitude;
    private Double longitude;
    private Availability availability = Availability.AVAILABLE;
    private AmbulanceStatus status = AmbulanceStatus.IDLE;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
