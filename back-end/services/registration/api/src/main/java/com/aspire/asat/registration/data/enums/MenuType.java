package com.aspire.asat.registration.data.enums;

import lombok.Getter;

@Getter

public enum MenuType {
    MAIN_MENU("MAIN_MENU"),
    SUB_MENU("SUB_MENU");

    private final String menuType;

    MenuType(String menuType) {
        this.menuType = menuType;
    }
}
