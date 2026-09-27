package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Acknowledgement returned immediately after queuing a micro content creation job.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicroContentJobResponse {
    private String jobId;
    private DeepfakeJobStatus status;
}
