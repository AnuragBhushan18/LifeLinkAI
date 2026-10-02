package com.lifelinkai.backend.service;
import com.lifelinkai.backend.model.Driver;
import com.lifelinkai.backend.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository repository;

    public Driver save(Driver entity) { return repository.save(entity); }
    public List<Driver> findAll() { return repository.findAll(); }
    public Driver findById(String id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Driver not found")); }
    public void deleteById(String id) { repository.deleteById(id); }
    public Driver findByUserId(String userId) { 
        return repository.findByUserId(userId).orElseGet(() -> {
            Driver d = new Driver();
            d.setUserId(userId);
            d.setAvailability(com.lifelinkai.backend.model.Availability.AVAILABLE);
            return repository.save(d);
        }); 
    }
}
