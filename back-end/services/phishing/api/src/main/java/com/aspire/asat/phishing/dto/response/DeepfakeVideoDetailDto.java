package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundPreset;
import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepfakeVideoDetailDto {
    private String id;
    private String title;
    private String description;
    private int currentStep;
    private String faceKey;
    private String facePreviewUrl;
    private boolean faceConfirmed;
    private String voiceCloneId;
    private String script;
    private String language;
    private DeepfakeBackgroundType backgroundType;
    private DeepfakeBackgroundPreset backgroundPreset;
    private String backgroundKey;
    private String backgroundPreviewUrl;
    private String audioPreviewUrl;
    private VideoRenderProvider videoProvider;
    private String model;
    private Map<String, String> variables;
    private DeepfakeJobStatus status;
    private String videoUrl;
    private String thumbnailUrl;
    private String failureReason;
    private Instant uploadDate;
    private Instant updatedAt;
}
