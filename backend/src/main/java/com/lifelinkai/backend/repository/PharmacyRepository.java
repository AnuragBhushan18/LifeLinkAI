package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.Pharmacy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PharmacyRepository extends MongoRepository<Pharmacy, String> {}