package com.aspire.asat.cms.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoProcessRequestDto {
    private String id;
    private String specificContentId;
    private String contentType;
    private String processingStatus;
    private String videoUrl;
    private String processedVideoUrl;
    private String userId;
}
