package com.aspire.asat.phishing.dto.sqs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQS message payload for asynchronous micro content (Topic/Chapter/Content) creation in CMS.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicroContentMessage {
    private String jobId;
}
