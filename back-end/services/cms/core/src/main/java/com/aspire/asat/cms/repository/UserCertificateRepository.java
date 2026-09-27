package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.UserCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCertificateRepository extends MongoRepository<UserCertificate, String> {

    List<UserCertificate> findByUserId(String userId);

    List<UserCertificate> findByUserIdAndClientAdminId(String userId, String clientAdminId);

    Optional<UserCertificate> findByUserIdAndSubPackageId(String userId, String subPackageId);

    List<UserCertificate> findByClientAdminId(String clientAdminId);

    // New methods for certificate details API with pagination and filtering
    Page<UserCertificate> findByUserIdAndProductNameContainingIgnoreCase(String userId, String productName, Pageable pageable);

    Page<UserCertificate> findByClientAdminIdAndProductNameContainingIgnoreCase(String clientAdminId, String productName, Pageable pageable);

    Page<UserCertificate> findByUserId(String userId, Pageable pageable);

    Page<UserCertificate> findByClientAdminId(String clientAdminId, Pageable pageable);

    // Count methods for pagination
    long countByUserIdAndProductNameContainingIgnoreCase(String userId, String productName);

    long countByClientAdminIdAndProductNameContainingIgnoreCase(String clientAdminId, String productName);

    long countByUserId(String userId);

    long countByClientAdminId(String clientAdminId);

    List<UserCertificate> findByCertificateIdIn(List<String> certificateIds);
}
