package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.BloodBank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface BloodBankRepository extends MongoRepository<BloodBank, String> {}