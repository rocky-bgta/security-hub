package com.aspire.asat.registration.data.enums;

import lombok.Getter;

/**
 * Enum representing predefined system roles that are automatically created
 * and cannot be modified through the application's Role Management APIs.
 */
@Getter
public enum SystemRole {
    
    SUPER_ADMIN("SUPER_ADMIN", "Complete system control and administration", 5, "#FF0000"),
    ASPIRE_ADMIN("ASPIRE_ADMIN", "Aspire platform administration", 4, "#FF5733"),
    ADMIN("ADMIN", "General system administration", 3, "#FFC300"),
    MSP("MSP", "Managed Service Provider administration", 3, "#FF8C00"),
    CLIENT_ADMIN("CLIENT_ADMIN", "Client organization administration", 2, "#33FF57"),
    USER("USER", "Standard client user access", 1, "#3357FF");
    
    private final String roleName;
    private final String description;
    private final Integer accessLevel;
    private final String colorTheme;
    
    SystemRole(String roleName, String description, Integer accessLevel, String colorTheme) {
        this.roleName = roleName;
        this.description = description;
        this.accessLevel = accessLevel;
        this.colorTheme = colorTheme;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Integer getAccessLevel() {
        return accessLevel;
    }
    
    public String getColorTheme() {
        return colorTheme;
    }
    
    /**
     * Check if a given role name corresponds to a system role
     * @param roleName the role name to check
     * @return true if the role name matches a system role
     */
    public static boolean isSystemRole(String roleName) {
        if (roleName == null) {
            return false;
        }
        for (SystemRole systemRole : values()) {
            if (systemRole.getRoleName().equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get SystemRole enum by role name
     * @param roleName the role name to find
     * @return SystemRole enum or null if not found
     */
    public static SystemRole getByRoleName(String roleName) {
        if (roleName == null) {
            return null;
        }
        for (SystemRole systemRole : values()) {
            if (systemRole.getRoleName().equalsIgnoreCase(roleName)) {
                return systemRole;
            }
        }
        return null;
    }
}
