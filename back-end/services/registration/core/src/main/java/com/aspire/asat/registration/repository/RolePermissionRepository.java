package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.RolePermission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends MongoRepository<RolePermission, String> {

    Optional<RolePermission> findByRoleId(String roleId);
    
    @Query("{ 'roleId': { $in: ?0 } }")
    List<RolePermission> findRolePermissionsByRoleIds(List<String> roleIds);
}
