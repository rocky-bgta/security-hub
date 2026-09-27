package com.aspire.asat.cms.dto.enums;

/**
 * Enum representing different risk categories for user risk analysis
 * Based on training completion progress percentages
 */
public enum RiskCategory {
    
    /**
     * Safe users: 90% or more training completion
     */
    SAFE(90.0, "Safe"),
    
    /**
     * Low risk users: 70-89% training completion
     */
    LOW_RISK(70.0, "Low Risk"),
    
    /**
     * Average risk users: 50-69% training completion
     */
    AVERAGE_RISK(50.0, "Average Risk"),
    
    /**
     * High risk users: Less than 50% training completion
     */
    HIGH_RISK(0.0, "High Risk");
    
    private final double threshold;
    private final String displayName;
    
    RiskCategory(double threshold, String displayName) {
        this.threshold = threshold;
        this.displayName = displayName;
    }
    
    /**
     * Gets the minimum threshold percentage for this risk category
     * @return threshold percentage
     */
    public double getThreshold() {
        return threshold;
    }
    
    /**
     * Gets the display name for this risk category
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Determines the risk category based on progress percentage
     * @param progressPercentage the progress percentage (0-100)
     * @return the corresponding risk category
     */
    public static RiskCategory fromProgressPercentage(Double progressPercentage) {
        if (progressPercentage == null) {
            return HIGH_RISK;
        }
        
        if (progressPercentage >= SAFE.threshold) {
            return SAFE;
        } else if (progressPercentage >= LOW_RISK.threshold) {
            return LOW_RISK;
        } else if (progressPercentage >= AVERAGE_RISK.threshold) {
            return AVERAGE_RISK;
        } else {
            return HIGH_RISK;
        }
    }
    
    /**
     * Gets the risk category by name (case-insensitive)
     * @param name the risk category name
     * @return the corresponding risk category, or null if not found
     */
    public static RiskCategory fromName(String name) {
        if (name == null) {
            return null;
        }
        
        try {
            return RiskCategory.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Checks if this risk category represents a high-risk user
     * @return true if this is HIGH_RISK
     */
    public boolean isHighRisk() {
        return this == HIGH_RISK;
    }
    
    /**
     * Checks if this risk category represents a safe user
     * @return true if this is SAFE
     */
    public boolean isSafe() {
        return this == SAFE;
    }
    
    /**
     * Gets the priority level for this risk category (lower number = higher priority)
     * @return priority level (1=HIGH_RISK, 2=AVERAGE_RISK, 3=LOW_RISK, 4=SAFE)
     */
    public int getPriority() {
        return switch (this) {
            case HIGH_RISK -> 1;
            case AVERAGE_RISK -> 2;
            case LOW_RISK -> 3;
            case SAFE -> 4;
        };
    }
}
