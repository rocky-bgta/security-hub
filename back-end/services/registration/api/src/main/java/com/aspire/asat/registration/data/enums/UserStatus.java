package com.aspire.asat.registration.data.enums;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED,
    ;

    public static UserStatus fromString(String status) {
        for (UserStatus userStatus : UserStatus.values()) {
            if (userStatus.name().equalsIgnoreCase(status)) {
                return userStatus;
            }
        }
        throw new IllegalArgumentException("Invalid User status provided: " + status);
    }
}
