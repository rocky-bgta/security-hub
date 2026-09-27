package com.aspire.asat.universal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "knowledge_hub_tags")
public class KnowledgeHubTag {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    private UUID knowledgeHubId;
    
    private UUID tagId;

    @DBRef
    private Tag tag;

    public KnowledgeHubTag(UUID knowledgeHubId, UUID tagId) {
        this.knowledgeHubId = knowledgeHubId;
        this.tagId = tagId;
    }
}
