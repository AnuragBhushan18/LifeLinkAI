package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.MedicalReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface MedicalReportRepository extends MongoRepository<MedicalReport, String> {
    List<MedicalReport> findByPatientId(String patientId);
    List<MedicalReport> findByHospitalId(String hospitalId);
    List<MedicalReport> findByDoctorId(String doctorId);
    List<MedicalReport> findByEmergencyId(String emergencyId);
}