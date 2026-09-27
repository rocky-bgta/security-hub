package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.UploadToContentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Lightweight list-item view of a deepfake video.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepfakeVideoDto {
    private String id;
    private String title;
    private String description;
    private DeepfakeJobStatus status;
    private UploadToContentStatus uploadToContent;
    private String videoUrl;
    private String thumbnailUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
