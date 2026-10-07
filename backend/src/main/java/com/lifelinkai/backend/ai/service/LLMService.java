package com.lifelinkai.backend.ai.service;

import com.lifelinkai.backend.ai.provider.LLMProvider;
import com.lifelinkai.backend.model.AIMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LLMService {
    private final List<LLMProvider> providers;

    public LLMProvider getActiveProvider() {
        return providers.stream().filter(p -> p.getName() != null && !p.getName().isEmpty()).findFirst().orElseThrow(() -> new IllegalStateException("No active LLM provider found"));
    }

    public String generateResponse(List<AIMessage> messages, String systemPrompt) {
        return getActiveProvider().generateResponse(messages, systemPrompt);
    }
}