package com.aspire.asat.breachdetection.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionLog {
    private String action;
    private String performedBy;
    private Instant performedAt;
    private String notes;
}
