package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    List<Prescription> findByPatientId(String patientId);
    List<Prescription> findByDoctorId(String doctorId);
    List<Prescription> findByEmergencyId(String emergencyId);
}