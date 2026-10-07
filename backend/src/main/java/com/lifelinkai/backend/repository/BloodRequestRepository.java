package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.BloodRequest;
import com.lifelinkai.backend.model.BloodRequestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface BloodRequestRepository extends MongoRepository<BloodRequest, String> {
    List<BloodRequest> findByHospitalId(String hospitalId);
    List<BloodRequest> findByBloodBankId(String bloodBankId);
    List<BloodRequest> findByEmergencyId(String emergencyId);
    List<BloodRequest> findByBloodBankIdAndStatus(String bloodBankId, BloodRequestStatus status);
}