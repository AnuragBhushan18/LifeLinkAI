package com.lifelinkai.backend.repository;

import com.lifelinkai.backend.model.Bed;
import com.lifelinkai.backend.model.BedStatus;
import com.lifelinkai.backend.model.BedType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends MongoRepository<Bed, String> {
    List<Bed> findByHospitalId(String hospitalId);
    List<Bed> findByHospitalIdAndType(String hospitalId, BedType type);
    List<Bed> findByHospitalIdAndStatus(String hospitalId, BedStatus status);
    Optional<Bed> findByHospitalIdAndBedNumber(String hospitalId, String bedNumber);
    long countByHospitalId(String hospitalId);
    long countByHospitalIdAndStatus(String hospitalId, BedStatus status);
    long countByHospitalIdAndType(String hospitalId, BedType type);
    long countByHospitalIdAndTypeAndStatus(String hospitalId, BedType type, BedStatus status);
}