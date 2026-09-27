package com.aspire.asat.phishing.media.model;

import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundPreset;
import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * Normalized input for a video render call. {@code audioUrl} is a publicly
 * reachable (presigned) URL to the synthesized speech; {@code faceImageUrl} is
 * a presigned URL to the avatar photo for HeyGen v3 photo avatar creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRenderInput {
    private File faceFile;
    private File audioFile;
    private String faceImageUrl;
    private String audioUrl;
    private String heygenAvatarId;
    private String heygenAvatarFaceKey;
    private String faceKey;
    private String model;
    private String title;
    private DeepfakeBackgroundType backgroundType;
    private DeepfakeBackgroundPreset backgroundPreset;
    private String customBackgroundUrl;
    private String script;

    /** Resolved credentials for the selected render provider. */
    private ResolvedProviderCredentials credentials;
}
