package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "topic_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "topic_question_idx", def = "{'topicId': 1, 'questionId': 1}", unique = true)
public class TopicQuestion {
    
    @Id
    private String id;
    
    @Indexed
    private String topicId;
    
    @Indexed
    private String questionId;
    
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Static method for creating a new TopicQuestion
    public static TopicQuestion create(String topicId, String  questionId, String createdBy) {
        return TopicQuestion.builder()
                .topicId(topicId)
                .questionId(questionId)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
