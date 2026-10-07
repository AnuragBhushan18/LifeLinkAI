package com.lifelinkai.backend.ai.controller;

import com.lifelinkai.backend.ai.service.AIAdminService;
import com.lifelinkai.backend.ai.service.AIChatService;
import com.lifelinkai.backend.ai.service.AIMedicalSummaryService;
import com.lifelinkai.backend.dto.ai.AIChatRequest;
import com.lifelinkai.backend.dto.ai.AIChatResponse;
import com.lifelinkai.backend.dto.ai.AdminQueryRequest;
import com.lifelinkai.backend.dto.ai.MedicalSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIChatService chatService;
    private final AIMedicalSummaryService summaryService;
    private final AIAdminService adminService;

    @PostMapping("/chat")
    @PreAuthorize("hasRole('PATIENT')")
    public AIChatResponse handleChat(@RequestBody AIChatRequest request, Authentication authentication) {
        String userId = authentication.getName(); // assuming ID is name or extract from principal
        return chatService.handlePatientChat(userId, request);
    }

    @PostMapping("/medical-summary/{patientId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public MedicalSummaryResponse getMedicalSummary(@PathVariable String patientId) {
        // In a real system, verify Doctor has authorization for this specific patient
        return summaryService.generateSummary(patientId);
    }

    @PostMapping("/admin-query")
    @PreAuthorize("hasRole('ADMIN')")
    public AIChatResponse handleAdminQuery(@RequestBody AdminQueryRequest request) {
        return adminService.handleAdminQuery(request);
    }
}