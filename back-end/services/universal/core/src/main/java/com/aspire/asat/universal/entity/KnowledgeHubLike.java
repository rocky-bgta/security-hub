package com.aspire.asat.universal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "knowledge_hub_like")
public class KnowledgeHubLike {

    @Id
    private String id;
    private String knowledgeHubId;
    private String userId;
    private boolean liked;
    private LocalDateTime createdAt;
}
