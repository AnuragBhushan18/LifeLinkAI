package com.lifelinkai.backend.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lifelinkai.backend.model.MedicalRecord;
import com.lifelinkai.backend.model.MedicalReport;
import com.lifelinkai.backend.repository.MedicalRecordRepository;
import com.lifelinkai.backend.repository.MedicalReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIContextService {
    private final MedicalRecordRepository recordRepository;
    private final MedicalReportRepository reportRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildPatientContext(String patientId) {
        List<MedicalRecord> records = recordRepository.findByPatientId(patientId);
        List<MedicalReport> reports = reportRepository.findByPatientId(patientId);

        ObjectNode context = objectMapper.createObjectNode();
        context.put("patientId", patientId);

        if (!records.isEmpty()) {
            MedicalRecord latest = records.get(records.size() - 1);
            context.put("bloodGroup", latest.getBloodGroup() != null ? latest.getBloodGroup().getValue() : "Unknown");
            context.set("allergies", objectMapper.valueToTree(latest.getAllergies()));
            context.set("currentMedications", objectMapper.valueToTree(latest.getCurrentMedications()));
            context.put("medicalHistory", latest.getMedicalHistory());
            context.put("clinicalNotes", latest.getClinicalNotes());
        }

        if (!reports.isEmpty()) {
            MedicalReport latest = reports.get(reports.size() - 1);
            context.put("recentReportSummary", latest.getSummary());
            context.put("recentDiagnosisNotes", latest.getDiagnosisNotes());
        }

        return context.toString();
    }
}