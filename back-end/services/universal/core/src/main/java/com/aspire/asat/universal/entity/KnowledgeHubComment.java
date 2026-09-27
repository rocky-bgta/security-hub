package com.aspire.asat.universal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "knowledge_hub_comment")
public class KnowledgeHubComment {

    @Id
    private UUID id = UUID.randomUUID();

    private UUID knowledgeHubId; // reference to LatestNews.id (UUID)
    private UUID parentCommentId; // null for root comments

    private String userId;
    private String userName; // snapshot of user's display name at time of commenting

    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
