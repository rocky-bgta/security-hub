package com.aspire.asat.vps.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {

    private String id;
    private String specificContentId;
    private String contentType;
    private String processingStatus;
    private String videoUrl;
    private String processedVideoUrl;
    private String userId;
    List<String> uploadedFiles;
    Instant uploadedAt;
}
