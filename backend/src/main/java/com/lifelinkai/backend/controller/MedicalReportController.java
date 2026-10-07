package com.lifelinkai.backend.controller;

import com.lifelinkai.backend.dto.MedicalReportDto;
import com.lifelinkai.backend.service.MedicalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MedicalReportController {
    private final MedicalReportService service;

    @PostMapping("/medical-reports")
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalReportDto create(@RequestBody MedicalReportDto dto) {
        return service.create(dto);
    }

    @GetMapping("/medical-reports/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'HOSPITAL_STAFF', 'ADMIN')")
    public MedicalReportDto getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/patients/{patientId}/reports")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'HOSPITAL_STAFF', 'ADMIN')")
    public List<MedicalReportDto> getByPatientId(@PathVariable String patientId) {
        return service.getByPatientId(patientId);
    }
}