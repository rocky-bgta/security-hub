package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.KnowledgeHubComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeHubCommentRepository extends MongoRepository<KnowledgeHubComment, UUID> {
    List<KnowledgeHubComment> findByKnowledgeHubIdAndParentCommentIdIsNullOrderByCreatedAtAsc(UUID knowledgeHubId);
    List<KnowledgeHubComment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);
    List<KnowledgeHubComment> findByKnowledgeHubIdOrderByCreatedAtAsc(UUID knowledgeHubId);
}
