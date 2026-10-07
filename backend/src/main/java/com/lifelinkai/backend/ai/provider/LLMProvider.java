package com.lifelinkai.backend.ai.provider;

import com.lifelinkai.backend.model.AIMessage;

import java.util.List;

public interface LLMProvider {
    String getName();
    String getModelName();
    String generateResponse(List<AIMessage> messages, String systemPrompt);
}