package com.lifelinkai.backend.service;
import com.lifelinkai.backend.model.Doctor;
import com.lifelinkai.backend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository repository;

    public Doctor save(Doctor entity) { return repository.save(entity); }
    public List<Doctor> findAll() { return repository.findAll(); }
    public Doctor findById(String id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found")); }
    public void deleteById(String id) { repository.deleteById(id); }
    public Doctor findByUserId(String userId) { 
        return repository.findByUserId(userId).orElseGet(() -> {
            Doctor d = new Doctor();
            d.setUserId(userId);
            d.setAvailability(com.lifelinkai.backend.model.Availability.AVAILABLE);
            return repository.save(d);
        });
    }
}
