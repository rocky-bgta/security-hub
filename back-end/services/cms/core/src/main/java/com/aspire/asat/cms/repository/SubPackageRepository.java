package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.SubPackage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubPackageRepository extends MongoRepository<SubPackage, String> {

    List<SubPackage> findByClientAdminIdAndAssignedForInAndDeletedFalse(
            String clientAdminId, Collection<String> assignedForValues);

    // Check if sub-package exists by name and client ID (for create validation)
    boolean existsByNameAndClientId(String name, String clientId);

    // Check if sub-package exists by name, client ID, and different ID (for update validation)
    boolean existsByNameAndClientIdAndIdNot(String name, String clientId, String id);

    // Find by name and client ID
    Optional<SubPackage> findByNameAndClientId(String name, String clientId);

    // Find by package ID (used in other services)
    Optional<SubPackage> findByPackageId(String packageId);

    List<SubPackage> findByPackageIdInAndDeletedFalse(Collection<String> packageIds);
}
