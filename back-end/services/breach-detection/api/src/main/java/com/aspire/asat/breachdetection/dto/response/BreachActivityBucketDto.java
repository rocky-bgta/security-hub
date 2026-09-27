package com.aspire.asat.breachdetection.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One day's bucket in the breach-activity timeseries (email breaches only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachActivityBucketDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    private long critical;
    private long high;
    private long medium;
    private long low;
    private long total;
}
