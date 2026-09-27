package com.aspire.asat.registration.service.menu;

import com.aspire.asat.registration.model.menu.Menu;
import com.aspire.asat.registration.model.menu.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuPermissionServiceImpl implements MenuPermissionService {
    public List<Permission> generatePermissions(Menu menu) {
        return menu.getActions().stream()
                .map(action -> {
                    Permission p = new Permission();
                    p.setId(UUID.randomUUID().toString()); // Generate a unique ID
                    p.setMenuId(menu.getId());
                    p.setMenuCode(menu.getCode());
                    p.setAction(action);
                    p.setName(menu.getCode() + ":" + action); // e.g., "users:view"
                    return p;
                })
                .toList();
    }
}
