package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for available filter options.
 * Used to populate filter dropdowns in the UI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterOptionsDto {
    
    /**
     * Available difficulty levels: BEGINNER, INTERMEDIATE, ADVANCED, SPEAR_PHISHING
     */
    private List<String> difficultyLevels;
    
    /**
     * Available payload types
     */
    private List<String> payloadTypes;
    
    /**
     * Available service locations/regions
     */
    private List<String> locations;
    
    /**
     * Available tags for filtering
     */
    private List<String> tags;
    
    /**
     * Available languages
     */
    private List<String> languages;
}
