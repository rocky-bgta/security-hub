package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.ClientProduct;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ClientProductRepository extends MongoRepository<ClientProduct, String> {

    List<ClientProduct> findByClientAdminId(String clientAdminId);

    List<ClientProduct> findByClientAdminIdAndProductId(String clientAdminId, String productId);

    ClientProduct findByClientAdminIdAndProductIdAndPackageId(String clientAdminId, String productId, String packageId);

    // Methods with licenseStatus filter for ACTIVE status filtering
    List<ClientProduct> findByClientAdminIdAndLicenseStatus(String clientAdminId, String licenseStatus);

    List<ClientProduct> findByClientAdminIdAndLicenseStatusIn(String clientAdminId, List<String> licenseStatuses);

    List<ClientProduct> findByClientAdminIdAndProductIdAndLicenseStatus(String clientAdminId, String productId, String licenseStatus);

    ClientProduct findByClientAdminIdAndProductIdAndPackageIdAndLicenseStatus(String clientAdminId, String productId, String packageId, String licenseStatus);

    List<ClientProduct> findByMspId(String mspId);

    @Query(value = "{ 'clientAdminId': ?0, 'licenseStatus': ?1, 'expiryDate': { $gte: ?2, $lte: ?3 } }", count = true)
    long countByClientAdminIdAndLicenseStatusAndExpiryDateBetween(
            String clientAdminId, String licenseStatus, Instant fromInclusive, Instant toInclusive);
}
