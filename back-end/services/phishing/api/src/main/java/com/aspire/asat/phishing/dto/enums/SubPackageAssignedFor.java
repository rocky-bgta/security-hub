package com.aspire.asat.phishing.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum SubPackageAssignedFor {
    SIMULATED_PHISHING,
    PHISHING_WITH_TRAINING,
    PHISHING_TRAINING_FOR_CLICKS,
    PHISHING_TRAINING_FOR_COMPROMISES,
    PHISHING_TRAINING_FOR_ALL;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static SubPackageAssignedFor fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid assignedFor value. Allowed values are: SIMULATED_PHISHING, PHISHING_WITH_TRAINING, PHISHING_TRAINING_FOR_CLICKS, PHISHING_TRAINING_FOR_COMPROMISES, PHISHING_TRAINING_FOR_ALL"
                ));
    }
}
