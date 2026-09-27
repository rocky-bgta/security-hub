package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Maps a client to its single CMS "microContent" topic so it is created once and reused across jobs.
 * {@code nextChapterPosition} keeps chapter ordering stable across successive jobs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "client_micro_content_topics")
public class ClientMicroContentTopic {

    @Id
    private String id;

    @Indexed(unique = true)
    private String clientId;

    private String topicId;

    /** Final CMS topic name that was created for this client. */
    private String topicName;

    @Builder.Default
    private int nextChapterPosition = 1;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
