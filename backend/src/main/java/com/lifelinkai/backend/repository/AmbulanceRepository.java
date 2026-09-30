package com.lifelinkai.backend.repository;

import com.lifelinkai.backend.model.Ambulance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmbulanceRepository extends MongoRepository<Ambulance, String> {
    Optional<Ambulance> findByVehicleNumber(String vehicleNumber);
    List<Ambulance> findByHospitalId(String hospitalId);
}
