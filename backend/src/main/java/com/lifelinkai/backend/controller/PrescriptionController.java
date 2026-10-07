package com.lifelinkai.backend.controller;

import com.lifelinkai.backend.dto.PrescriptionDto;
import com.lifelinkai.backend.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PrescriptionController {
    private final PrescriptionService service;

    @PostMapping("/prescriptions")
    @PreAuthorize("hasRole('DOCTOR')")
    public PrescriptionDto create(@RequestBody PrescriptionDto dto) {
        return service.create(dto);
    }

    @GetMapping("/prescriptions/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'HOSPITAL_STAFF', 'PHARMACY', 'ADMIN')")
    public PrescriptionDto getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/patients/{patientId}/prescriptions")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'HOSPITAL_STAFF', 'ADMIN')")
    public List<PrescriptionDto> getByPatientId(@PathVariable String patientId) {
        return service.getByPatientId(patientId);
    }
}