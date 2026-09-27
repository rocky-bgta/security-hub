package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.SendingPattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded model for email sending configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendingConfig {

    @Builder.Default
    private SendingPattern pattern = SendingPattern.ALL_AT_ONCE;

    private int batchSize;

    private int batchIntervalMinutes;
}
