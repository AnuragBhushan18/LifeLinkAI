package com.lifelinkai.backend.service;
import com.lifelinkai.backend.model.Patient;
import com.lifelinkai.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repository;

    public Patient save(Patient entity) { return repository.save(entity); }
    public List<Patient> findAll() { return repository.findAll(); }
    public Patient findById(String id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Patient not found")); }
    public void deleteById(String id) { repository.deleteById(id); }
    public Patient findByUserId(String userId) { 
        return repository.findByUserId(userId).orElseGet(() -> {
            Patient p = new Patient();
            p.setUserId(userId);
            return repository.save(p);
        }); 
    }
}
