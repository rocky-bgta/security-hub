package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundPreset;
import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
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
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deepfake_render_jobs")
public class DeepfakeRenderJob {

    @Id
    private String id;

    @Indexed(unique = true)
    private UUID renderId;

    @Indexed
    private String clientId;

    private String title;

    private String description;

    private String faceKey;

    private String heygenAvatarId;

    private String heygenAvatarFaceKey;

    private boolean faceConfirmed;

    private String voiceCloneId;

    private String script;

    private String language;

    private DeepfakeBackgroundType backgroundType;

    private DeepfakeBackgroundPreset backgroundPreset;

    private String backgroundKey;

    private VideoRenderProvider videoProvider;

    /**
     * Optional {@code provider_credentials} document id selected at Step 6.
     * When set, the render pipeline resolves credentials by id instead of by name.
     */
    private String videoProviderCredentialId;

    private String model;

    private Map<String, String> variables;

    @Builder.Default
    private int currentStep = 1;

    @Indexed
    @Builder.Default
    private DeepfakeJobStatus status = DeepfakeJobStatus.DRAFT;

    private String audioS3Key;

    private String videoS3Key;

    private String videoUrl;

    private String thumbnailS3Key;

    private String failureReason;

    @Builder.Default
    private boolean isDeleted = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public boolean isEditable() {
        return status == DeepfakeJobStatus.DRAFT
                || status == DeepfakeJobStatus.COMPLETED
                || status == DeepfakeJobStatus.FAILED;
    }

    public void reopenForEdit() {
        if (status == DeepfakeJobStatus.COMPLETED || status == DeepfakeJobStatus.FAILED) {
            status = DeepfakeJobStatus.DRAFT;
        }
    }
}
