package com.aspire.asat.phishing.util;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/** Parsed schedule start/end instants (UTC). */
@Value
@Builder
public class ScheduleDateTimeParseResult {
    Instant startDateTime;
    Instant endDateTime;
}
