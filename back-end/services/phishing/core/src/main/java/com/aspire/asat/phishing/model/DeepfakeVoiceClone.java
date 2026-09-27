package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.enums.VoiceCloneSource;
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
@Document(collection = "deepfake_voice_clones")
public class DeepfakeVoiceClone {

    @Id
    private String id;

    @Indexed(unique = true)
    private UUID voiceCloneId;

    @Indexed
    private String clientId;

    private VoiceCloneProvider provider;

    /**
     * Optional {@code provider_credentials} document id used when the clone was
     * created via explicit providerId selection. Null for legacy name-based clones.
     */
    private String providerCredentialId;

    /**
     * Where this clone was created (vishing campaign voice-setup vs deepfake video wizard).
     * Null on legacy records; list queries also match the vishing S3 sample prefix.
     */
    private VoiceCloneSource voiceCloneSource;

    /**
     * Provider-side identifier (e.g. ElevenLabs voice_id) used for synthesis.
     */
    private String externalVoiceId;

    private String sampleS3Key;

    /** User-provided display name for the cloned voice. */
    private String voiceName;

    /** Original uploaded sample file name, surfaced in the cloned-voices list. */
    private String sampleFileName;

    private String language;

    private DeepfakeJobStatus status;

    private String failureReason;

    /**
     * True when this clone reused another provider voice id because the account's
     * custom-voice quota was exhausted (only when reuse-on-limit-reached is enabled).
     */
    @Builder.Default
    private boolean usedFallbackVoice = false;

    @Builder.Default
    private boolean isDeleted = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
