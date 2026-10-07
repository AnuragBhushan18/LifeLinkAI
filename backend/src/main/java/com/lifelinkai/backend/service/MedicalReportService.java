package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.MedicalReportDto;
import com.lifelinkai.backend.model.MedicalReport;
import com.lifelinkai.backend.repository.MedicalReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalReportService {
    private final MedicalReportRepository repository;

    public MedicalReportDto create(MedicalReportDto dto) {
        MedicalReport report = new MedicalReport();
        updateEntity(report, dto);
        report.setReportDate(dto.getReportDate() != null ? dto.getReportDate() : LocalDateTime.now());
        return mapToDto(repository.save(report));
    }

    public MedicalReportDto getById(String id) {
        return mapToDto(repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Report not found")));
    }

    public List<MedicalReportDto> getByPatientId(String patientId) {
        return repository.findByPatientId(patientId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private void updateEntity(MedicalReport report, MedicalReportDto dto) {
        report.setPatientId(dto.getPatientId());
        report.setHospitalId(dto.getHospitalId());
        report.setDoctorId(dto.getDoctorId());
        report.setEmergencyId(dto.getEmergencyId());
        report.setTitle(dto.getTitle());
        report.setSummary(dto.getSummary());
        report.setDiagnosisNotes(dto.getDiagnosisNotes());
        report.setTreatmentProvided(dto.getTreatmentProvided());
        report.setRecommendations(dto.getRecommendations());
    }

    private MedicalReportDto mapToDto(MedicalReport report) {
        MedicalReportDto dto = new MedicalReportDto();
        dto.setId(report.getId());
        dto.setPatientId(report.getPatientId());
        dto.setHospitalId(report.getHospitalId());
        dto.setDoctorId(report.getDoctorId());
        dto.setEmergencyId(report.getEmergencyId());
        dto.setTitle(report.getTitle());
        dto.setSummary(report.getSummary());
        dto.setDiagnosisNotes(report.getDiagnosisNotes());
        dto.setTreatmentProvided(report.getTreatmentProvided());
        dto.setRecommendations(report.getRecommendations());
        dto.setReportDate(report.getReportDate());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        return dto;
    }
}