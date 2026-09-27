package com.aspire.asat.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuPermissionDto {
    private String menuId; // Unique identifier for the menu
    private String menuName; // Name of the menu
    private String menuCode; // Code for the menu, e.g., "USER_MANAGEMENT"
    private List<String> permittedActions; // Comma-separated list of actions allowed, e.g., "CREATE,READ,UPDATE,DELETE"
    private List<String> permissions; // List of permissions associated with the menu, e.g., "VIEW_USERS,EDIT_USERS"
}
