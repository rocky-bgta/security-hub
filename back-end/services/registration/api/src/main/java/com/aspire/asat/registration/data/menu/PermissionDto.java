package com.aspire.asat.registration.data.menu;

import lombok.Data;

@Data
public class PermissionDto {
    private String id;
    private String name;       // e.g., users:view
    private String menuCode;   // e.g., users
    private String action;     // e.g., view
}
