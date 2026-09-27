package com.aspire.asat.registration.data.microsoft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicrosoftConnectionStatusDto {
    private boolean connected;
    private String tenantId;
    private String tenantName;
    private Instant connectedAt;
    private Instant lastSyncAt;
}

