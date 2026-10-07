package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
    List<MedicalRecord> findByPatientId(String patientId);
    List<MedicalRecord> findByHospitalId(String hospitalId);
    List<MedicalRecord> findByDoctorId(String doctorId);
    List<MedicalRecord> findByEmergencyId(String emergencyId);
}