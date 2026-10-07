package com.lifelinkai.backend.service;

import com.lifelinkai.backend.dto.MedicalRecordDto;
import com.lifelinkai.backend.model.MedicalRecord;
import com.lifelinkai.backend.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {
    private final MedicalRecordRepository repository;

    public MedicalRecordDto create(MedicalRecordDto dto) {
        MedicalRecord record = new MedicalRecord();
        updateEntity(record, dto);
        return mapToDto(repository.save(record));
    }

    public MedicalRecordDto update(String id, MedicalRecordDto dto) {
        MedicalRecord record = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Record not found"));
        updateEntity(record, dto);
        return mapToDto(repository.save(record));
    }

    public MedicalRecordDto getById(String id) {
        return mapToDto(repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Record not found")));
    }

    public List<MedicalRecordDto> getByPatientId(String patientId) {
        return repository.findByPatientId(patientId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private void updateEntity(MedicalRecord record, MedicalRecordDto dto) {
        record.setPatientId(dto.getPatientId());
        record.setHospitalId(dto.getHospitalId());
        record.setDoctorId(dto.getDoctorId());
        record.setEmergencyId(dto.getEmergencyId());
        record.setBloodGroup(dto.getBloodGroup());
        record.setAllergies(dto.getAllergies());
        record.setCurrentMedications(dto.getCurrentMedications());
        record.setMedicalHistory(dto.getMedicalHistory());
        record.setClinicalNotes(dto.getClinicalNotes());
        record.setTreatmentNotes(dto.getTreatmentNotes());
    }

    private MedicalRecordDto mapToDto(MedicalRecord record) {
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setId(record.getId());
        dto.setPatientId(record.getPatientId());
        dto.setHospitalId(record.getHospitalId());
        dto.setDoctorId(record.getDoctorId());
        dto.setEmergencyId(record.getEmergencyId());
        dto.setBloodGroup(record.getBloodGroup());
        dto.setAllergies(record.getAllergies());
        dto.setCurrentMedications(record.getCurrentMedications());
        dto.setMedicalHistory(record.getMedicalHistory());
        dto.setClinicalNotes(record.getClinicalNotes());
        dto.setTreatmentNotes(record.getTreatmentNotes());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }
}