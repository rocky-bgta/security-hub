package com.aspire.asat.registration.service.menu;

import com.aspire.asat.registration.model.menu.Menu;
import com.aspire.asat.registration.model.menu.Permission;

import java.util.List;

public interface MenuPermissionService {
    List<Permission> generatePermissions(Menu menu);
}
