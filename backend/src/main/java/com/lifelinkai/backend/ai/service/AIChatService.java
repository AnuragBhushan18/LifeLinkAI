package com.lifelinkai.backend.ai.service;

import com.lifelinkai.backend.dto.ai.AIChatRequest;
import com.lifelinkai.backend.dto.ai.AIChatResponse;
import com.lifelinkai.backend.model.AIConversation;
import com.lifelinkai.backend.model.AIMessage;
import com.lifelinkai.backend.model.Role;
import com.lifelinkai.backend.repository.AIConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIChatService {
    private final LLMService llmService;
    private final PromptService promptService;
    private final AIConversationRepository conversationRepository;

    public AIChatResponse handlePatientChat(String userId, AIChatRequest request) {
        AIConversation conv = conversationRepository.findByUserId(userId).orElseGet(() -> {
            AIConversation newConv = new AIConversation();
            newConv.setUserId(userId);
            newConv.setUserRole(Role.PATIENT);
            newConv.setMessages(new ArrayList<>());
            return newConv;
        });

        conv.getMessages().add(new AIMessage("user", request.getMessage(), LocalDateTime.now()));

        // Keep context small (e.g. last 10 messages)
        List<AIMessage> context = conv.getMessages().size() > 10 ? 
            conv.getMessages().subList(conv.getMessages().size() - 10, conv.getMessages().size()) : 
            new ArrayList<>(conv.getMessages());

        String responseText = llmService.generateResponse(context, promptService.getPatientAssistantPrompt());

        conv.getMessages().add(new AIMessage("assistant", responseText, LocalDateTime.now()));
        conversationRepository.save(conv);

        return AIChatResponse.builder()
                .response(responseText)
                .model(llmService.getActiveProvider().getModelName())
                .disclaimer("AI-generated information. This assistant does not diagnose medical conditions or replace professional medical care.")
                .build();
    }
}