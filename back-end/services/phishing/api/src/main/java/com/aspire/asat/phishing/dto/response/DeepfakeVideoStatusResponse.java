package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepfakeVideoStatusResponse {
    private String videoId;
    private DeepfakeJobStatus status;
    private String videoUrl;
    private String thumbnailUrl;
    private String failureReason;
}
