package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.KnowledgeHubTag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeHubTagRepository extends MongoRepository<KnowledgeHubTag, UUID> {
    
    List<KnowledgeHubTag> findByKnowledgeHubId(UUID knowledgeHubId);
    
    void deleteByKnowledgeHubId(UUID knowledgeHubId);
}
