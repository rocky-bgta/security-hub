package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for HTML sanitization result.
 * Based on BR-04: HTML content sanitization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanitizeHtmlResult {
    
    /**
     * The sanitized HTML content
     */
    private String sanitizedHtml;
    
    /**
     * Whether any content was modified during sanitization
     */
    private boolean wasModified;
    
    /**
     * List of elements that were removed or modified
     */
    private List<String> removedElements;
    
    /**
     * List of attributes that were removed
     */
    private List<String> removedAttributes;
    
    /**
     * Whether the content is safe for use
     */
    private boolean isSafe;
    
    /**
     * Warning message if content had dangerous elements
     */
    private String warningMessage;
}
