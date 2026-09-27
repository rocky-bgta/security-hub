package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Dedupe cache for synthesized vishing audio. Identical rendered scripts
 * (same voice + language + text) are synthesized once and reused across calls,
 * avoiding redundant provider TTS spend and latency.
 */
@Document(collection = "vishing_audio_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingAudioCache {

    @Id
    private String id;

    /** SHA-256 hash of {@code provider|externalVoiceId|language|text}. */
    @Indexed(unique = true)
    private String cacheKey;

    private VoiceCloneProvider provider;

    private String externalVoiceId;

    private String language;

    private String s3Key;

    @CreatedDate
    private Instant createdAt;
}
