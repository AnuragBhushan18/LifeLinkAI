package com.lifelinkai.backend;

import com.lifelinkai.backend.ai.service.AIAdminService;
import com.lifelinkai.backend.ai.service.AIChatService;
import com.lifelinkai.backend.ai.service.AIMedicalSummaryService;
import com.lifelinkai.backend.dto.ai.AIChatRequest;
import com.lifelinkai.backend.dto.ai.AdminQueryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class AITest {

    @Autowired
    private AIChatService chatService;
    
    @Autowired
    private AIMedicalSummaryService medicalSummaryService;
    
    @Autowired
    private AIAdminService adminService;

    @Test
    public void testAIContextValidation() {
        // Just verify context doesn't crash on empty
        assertNotNull(chatService);
        assertNotNull(medicalSummaryService);
        assertNotNull(adminService);
    }
}