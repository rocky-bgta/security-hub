package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCallStatusRequest {

    private RecipientStatus status;
    private Instant startedAt;
    private int retries;
}
