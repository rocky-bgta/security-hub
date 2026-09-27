package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.KnowledgeHubLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KnowledgeHubLikeRepository extends MongoRepository<KnowledgeHubLike, String> {
    Optional<KnowledgeHubLike> findByKnowledgeHubIdAndUserId(String knowledgeHubId, String userId);
    int countByKnowledgeHubIdAndLiked(String knowledgeHubId, boolean liked);
}
