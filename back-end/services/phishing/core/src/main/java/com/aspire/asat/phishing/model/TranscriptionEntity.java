package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.TranscriptionStatus;
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
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stt_transcriptions")
public class TranscriptionEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private UUID audioId;

    private String s3Key;

    @Indexed
    private TranscriptionStatus status;

    private String transcriptionText;

    private String languageHint;

    private String failureReason;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
