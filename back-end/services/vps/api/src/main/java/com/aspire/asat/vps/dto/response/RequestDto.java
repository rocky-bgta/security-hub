package com.aspire.asat.vps.dto.response;

import com.aspire.asat.vps.dto.enums.VideoStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {


    private String id;
    private String specificContentId;
    private String contentType;
    private String processingStatus;
    private String videoUrl;
    private String processedVideoUrl;
    private String userId;
}
