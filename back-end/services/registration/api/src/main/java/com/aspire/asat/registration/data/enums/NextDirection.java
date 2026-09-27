package com.aspire.asat.registration.data.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Frontend next step after trial signup")
public enum NextDirection {
    VERIFY("verify"),
    ONBOARD("onboard");

    private final String value;

    NextDirection(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NextDirection fromValue(String value) {
        for (NextDirection direction : values()) {
            if (direction.value.equalsIgnoreCase(value) || direction.name().equalsIgnoreCase(value)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Invalid nextDirection: " + value);
    }
}
