package com.aspire.asat.cms.dto.enums;

public enum DistributionStrategy {
    EQUAL,
    WEIGHTED,
    CUSTOM;


    public static DistributionStrategy fromString(String strategy) {
        for (DistributionStrategy ds : DistributionStrategy.values()) {
            if (ds.name().equalsIgnoreCase(strategy)) {
                return ds;
            }
        }
        throw new IllegalArgumentException("No constant with text " + strategy + " found");
    }

}
