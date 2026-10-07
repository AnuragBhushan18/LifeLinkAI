package com.lifelinkai.backend.controller;

import com.lifelinkai.backend.dto.MedicalRecordDto;
import com.lifelinkai.backend.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MedicalRecordController {
    private final MedicalRecordService service;

    @PostMapping("/medical-records")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_STAFF')")
    public MedicalRecordDto create(@RequestBody MedicalRecordDto dto) {
        return service.create(dto);
    }

    @PutMapping("/medical-records/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_STAFF')")
    public MedicalRecordDto update(@PathVariable String id, @RequestBody MedicalRecordDto dto) {
        return service.update(id, dto);
    }

    @GetMapping("/medical-records/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'HOSPITAL_STAFF', 'ADMIN')")
    public MedicalRecordDto getById(@PathVariable String id) {
        // Implement fine-grained auth check if necessary in real system
        return service.getById(id);
    }

    @GetMapping("/patients/{patientId}/medical-records")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'HOSPITAL_STAFF', 'ADMIN')")
    public List<MedicalRecordDto> getByPatientId(@PathVariable String patientId) {
        return service.getByPatientId(patientId);
    }
}