package com.aspire.asat.cms.client.dto;

import com.aspire.asat.cms.dto.interactive.InteractiveVideo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoProcessResponseDto {
    private String message;
    private int statusCode;
    private InteractiveVideo data;
}
