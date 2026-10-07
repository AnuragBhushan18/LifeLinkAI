package com.lifelinkai.backend.repository;

import com.lifelinkai.backend.model.EmergencyRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRequestRepository extends MongoRepository<EmergencyRequest, String> {
    List<EmergencyRequest> findByPatientId(String patientId);
    List<EmergencyRequest> findByRecommendedHospitalId(String hospitalId);
    List<EmergencyRequest> findByAssignedDriverId(String driverId);
}
