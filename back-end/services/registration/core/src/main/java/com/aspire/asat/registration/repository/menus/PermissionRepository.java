package com.aspire.asat.registration.repository.menus;

import com.aspire.asat.registration.model.menu.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PermissionRepository extends MongoRepository<Permission, String> {
    List<Permission> findByMenuCode(String menuCode);
    void deleteByMenuCode(String menuCode);
    List<Permission> findByMenuCodeIn(List<String> menuCodes);

}
