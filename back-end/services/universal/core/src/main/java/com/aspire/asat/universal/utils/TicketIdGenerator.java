package com.aspire.asat.universal.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating unique ticket IDs.
 * Format: TKT-yyyymmdd-XXX (e.g., TKT-20241215-001)
 */
public class TicketIdGenerator {
    
    private static final String TICKET_PREFIX = "TKT";
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final String SEPARATOR = "-";
    private static final int SEQUENCE_LENGTH = 3;
    
    private TicketIdGenerator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates a ticket ID in the format TKT-yyyymmdd-XXX
     * 
     * @param date The date for the ticket ID
     * @param sequenceNumber The sequential number for the day (1-based)
     * @return Formatted ticket ID (e.g., TKT-20241215-001)
     */
    public static String generateTicketId(LocalDate date, int sequenceNumber) {
        String dateStr = date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        String sequenceStr = String.format("%0" + SEQUENCE_LENGTH + "d", sequenceNumber);
        return TICKET_PREFIX + SEPARATOR + dateStr + SEPARATOR + sequenceStr;
    }

    /**
     * Generates a ticket ID for today's date
     * 
     * @param sequenceNumber The sequential number for the day (1-based)
     * @return Formatted ticket ID (e.g., TKT-20241215-001)
     */
    public static String generateTicketId(int sequenceNumber) {
        return generateTicketId(LocalDate.now(), sequenceNumber);
    }
}

