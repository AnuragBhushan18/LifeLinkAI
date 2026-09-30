package com.lifelinkai.backend.service;
import com.lifelinkai.backend.model.Hospital;
import com.lifelinkai.backend.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {
    private final HospitalRepository repository;

    public Hospital save(Hospital entity) { return repository.save(entity); }
    public List<Hospital> findAll() { return repository.findAll(); }
    public Hospital findById(String id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Hospital not found")); }
    public void deleteById(String id) { repository.deleteById(id); }
    public Hospital findByUserId(String userId) { return repository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Hospital not found for user ID")); }
}
