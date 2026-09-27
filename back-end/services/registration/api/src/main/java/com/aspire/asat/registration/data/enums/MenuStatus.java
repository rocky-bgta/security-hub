package com.aspire.asat.registration.data.enums;

import lombok.Getter;

@Getter
public enum MenuStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");
    private final String menusStatus ;
    MenuStatus(String menusStatus) {
        this.menusStatus = menusStatus;
    }
}
