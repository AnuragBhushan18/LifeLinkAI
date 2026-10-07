package com.lifelinkai.backend.repository;
import com.lifelinkai.backend.model.Medicine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface MedicineRepository extends MongoRepository<Medicine, String> {
    List<Medicine> findByPharmacyId(String pharmacyId);
    List<Medicine> findByNameContainingIgnoreCase(String name);
    List<Medicine> findByCategoryIgnoreCase(String category);
}