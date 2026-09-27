package com.aspire.asat.phishing.dto.sqs;

import com.aspire.asat.phishing.dto.enums.AiGenerationJobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight SQS payload pointing at an {@code ai_generation_jobs} document.
 *
 * <p>The full AI request body is intentionally not duplicated on the queue: the listener
 * fetches it from MongoDB by {@link #jobId}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContentGenerationMessage {

    private String jobId;

    private AiGenerationJobType jobType;
}
