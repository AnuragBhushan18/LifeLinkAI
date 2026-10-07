package com.lifelinkai.backend.repository;

import com.lifelinkai.backend.model.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    Optional<Hospital> findByUserId(String userId);
}
