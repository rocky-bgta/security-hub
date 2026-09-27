package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.model.EmailMetrics;

import java.time.LocalDate;

/**
 * One row: calendar day plus aggregated email counters/rates for platform-wide dashboard trends.
 */
public record DailyEmailMetricsAggregation(LocalDate date, EmailMetrics emailMetrics) {}
