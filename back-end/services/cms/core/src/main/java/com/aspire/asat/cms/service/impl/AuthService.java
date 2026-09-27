package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.loginDto.LoginRequest;
import com.aspire.asat.cms.dto.loginDto.LoginResponse;
import com.aspire.asat.cms.dto.role.Role;
import com.aspire.asat.cms.model.User;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final List<User> users = Arrays.asList(
            new User("admin", "admin123", Role.ADMIN, List.of(UUID.randomUUID(), UUID.randomUUID())),
            new User("user", "user123", Role.USER, List.of(UUID.randomUUID()))
    );

    public LoginResponse authenticate(LoginRequest request) {
        return users.stream()
                .filter(user -> user.getUsername().equals(request.getUsername())
                        && user.getPassword().equals(request.getPassword()))
                .findFirst()
                .map(user -> new LoginResponse(user.getUsername(), user.getRole(), user.getAccessIds()))
                .orElse(null);
    }
}
