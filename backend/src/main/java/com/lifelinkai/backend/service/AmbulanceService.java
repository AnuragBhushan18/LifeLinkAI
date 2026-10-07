package com.lifelinkai.backend.service;
import com.lifelinkai.backend.model.Ambulance;
import com.lifelinkai.backend.repository.AmbulanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AmbulanceService {
    private final AmbulanceRepository repository;

    public Ambulance save(Ambulance entity) { return repository.save(entity); }
    public List<Ambulance> findAll() { return repository.findAll(); }
    public Ambulance findById(String id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Ambulance not found")); }
    public void deleteById(String id) { repository.deleteById(id); }
}
