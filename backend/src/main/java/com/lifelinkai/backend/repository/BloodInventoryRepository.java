package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.BloodInventory;
import com.lifelinkai.backend.model.BloodGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface BloodInventoryRepository extends MongoRepository<BloodInventory, String> {
    List<BloodInventory> findByBloodBankId(String bloodBankId);
    Optional<BloodInventory> findByBloodBankIdAndBloodGroup(String bloodBankId, BloodGroup bloodGroup);
}