package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.role.Role;

import java.util.List;
import java.util.UUID;

public class User {
    private String username;
    private String password;
    private Role role;
    private List<UUID> accessIds;

    public User(String username, String password, Role role, List<UUID> accessIds) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.accessIds = accessIds;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public List<UUID> getAccessIds() { return accessIds; }
}
