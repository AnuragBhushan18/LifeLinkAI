package com.lifelinkai.backend.dto;
import lombok.Data;
import com.lifelinkai.backend.model.MedicineItem;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class PrescriptionDto {
    private String id;
    private String patientId;
    private String doctorId;
    private String hospitalId;
    private String emergencyId;
    private List<MedicineItem> medicines;
    private String instructions;
    private LocalDateTime prescribedAt;
    private LocalDateTime createdAt;
}