package com.lifelinkai.backend.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelinkai.backend.dto.ai.AIChatResponse;
import com.lifelinkai.backend.dto.ai.AdminQueryRequest;
import com.lifelinkai.backend.model.AIMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIAdminService {
    private final LLMService llmService;
    private final PromptService promptService;
    private final AIToolRegistry toolRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIChatResponse handleAdminQuery(AdminQueryRequest request) {
        String systemPrompt = promptService.getAdminAnalyticsPrompt(toolRegistry.getToolsJson());
        List<AIMessage> messages = new ArrayList<>();
        messages.add(new AIMessage("user", request.getQuery(), LocalDateTime.now()));

        String responseText = llmService.generateResponse(messages, systemPrompt);

        // Check if the LLM decided to call a tool
        try {
            String cleanResponse = responseText.trim();
            if (cleanResponse.startsWith("```json")) cleanResponse = cleanResponse.substring(7, cleanResponse.length() - 3);
            else if (cleanResponse.startsWith("```")) cleanResponse = cleanResponse.substring(3, cleanResponse.length() - 3);
            
            JsonNode root = objectMapper.readTree(cleanResponse);
            if (root.has("tool")) {
                String toolName = root.get("tool").asText();
                Map<String, Object> args = root.has("args") ? objectMapper.convertValue(root.get("args"), new TypeReference<Map<String, Object>>() {}) : Map.of();
                
                String toolResult = toolRegistry.executeTool(toolName, args);
                
                // Add tool result to context and ask LLM again to format the final answer
                messages.add(new AIMessage("assistant", responseText, LocalDateTime.now()));
                messages.add(new AIMessage("user", "Tool returned: " + toolResult + ". Now answer my original question.", LocalDateTime.now()));
                
                responseText = llmService.generateResponse(messages, systemPrompt);
            }
        } catch (Exception e) {
            // It might just be a normal text response, continue
        }

        return AIChatResponse.builder()
                .response(responseText)
                .model(llmService.getActiveProvider().getModelName())
                .disclaimer("Data retrieved from LifeLink system via Admin AI tools.")
                .build();
    }
}