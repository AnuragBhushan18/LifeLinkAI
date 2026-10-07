package com.lifelinkai.backend.repository;

import com.lifelinkai.backend.model.AIConversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIConversationRepository extends MongoRepository<AIConversation, String> {
    Optional<AIConversation> findByUserId(String userId);
}