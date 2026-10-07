package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.PrescriptionDto;
import com.lifelinkai.backend.model.Prescription;
import com.lifelinkai.backend.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionRepository repository;

    public PrescriptionDto create(PrescriptionDto dto) {
        Prescription p = new Prescription();
        p.setPatientId(dto.getPatientId());
        p.setDoctorId(dto.getDoctorId());
        p.setHospitalId(dto.getHospitalId());
        p.setEmergencyId(dto.getEmergencyId());
        p.setMedicines(dto.getMedicines());
        p.setInstructions(dto.getInstructions());
        p.setPrescribedAt(dto.getPrescribedAt() != null ? dto.getPrescribedAt() : LocalDateTime.now());
        return mapToDto(repository.save(p));
    }

    public PrescriptionDto getById(String id) {
        return mapToDto(repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Prescription not found")));
    }

    public List<PrescriptionDto> getByPatientId(String patientId) {
        return repository.findByPatientId(patientId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private PrescriptionDto mapToDto(Prescription p) {
        PrescriptionDto dto = new PrescriptionDto();
        dto.setId(p.getId());
        dto.setPatientId(p.getPatientId());
        dto.setDoctorId(p.getDoctorId());
        dto.setHospitalId(p.getHospitalId());
        dto.setEmergencyId(p.getEmergencyId());
        dto.setMedicines(p.getMedicines());
        dto.setInstructions(p.getInstructions());
        dto.setPrescribedAt(p.getPrescribedAt());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}