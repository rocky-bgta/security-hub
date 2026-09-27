package com.aspire.asat.cms.dto.loginDto;

import com.aspire.asat.cms.dto.role.Role;

import java.util.List;
import java.util.UUID;

public class LoginResponse {

    private String username;
    private Role role;
    private List<UUID> accessIds;

    public LoginResponse(String username, Role role, List<UUID> accessIds) {
        this.username = username;
        this.role = role;
        this.accessIds = accessIds;
    }

    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public List<UUID> getAccessIds() { return accessIds; }

}
