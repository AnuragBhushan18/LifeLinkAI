package com.lifelinkai.backend.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lifelinkai.backend.model.AIMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GeminiProvider implements LLMProvider {

    @Value("${llm.provider.name:gemini}")
    private String providerName;

    @Value("${llm.model.name:gemini-flash-latest}")
    private String modelName;

    @Value("${llm.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return providerName;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public String generateResponse(List<AIMessage> messages, String systemPrompt) {
        if (!"gemini".equalsIgnoreCase(providerName)) {
            throw new IllegalStateException("Provider is not set to gemini");
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // System instructions
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                ObjectNode systemInstruction = objectMapper.createObjectNode();
                ObjectNode parts = objectMapper.createObjectNode();
                parts.put("text", systemPrompt);
                systemInstruction.set("parts", objectMapper.createArrayNode().add(parts));
                requestBody.set("systemInstruction", systemInstruction);
            }

            ArrayNode contentsArray = objectMapper.createArrayNode();
            for (AIMessage msg : messages) {
                ObjectNode content = objectMapper.createObjectNode();
                content.put("role", msg.getRole().equalsIgnoreCase("assistant") ? "model" : "user");
                ObjectNode parts = objectMapper.createObjectNode();
                parts.put("text", msg.getContent());
                content.set("parts", objectMapper.createArrayNode().add(parts));
                contentsArray.add(content);
            }
            requestBody.set("contents", contentsArray);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String responseStr = restTemplate.postForObject(url, request, String.class);
            JsonNode rootNode = objectMapper.readTree(responseStr);

            return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            throw new RuntimeException("LLM Provider Error: " + e.getMessage(), e);
        }
    }
}