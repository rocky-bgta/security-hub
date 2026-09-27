package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.model.Package;
import com.aspire.asat.cms.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PackageRepository extends MongoRepository<Package, String> {

    boolean existsByPackageName(String packageName);

    @Query("{ 'packageName': { $regex: ?0, $options: 'i' } }")
    Page<Package> findByPackageName(String text, Pageable pageable);

    @Query("{ 'packageName': { $regex: ?0, $options: 'i' }, 'packageStatus': ?1 }")
    Page<Package> findByPackageNameAndPackageStatus(String packageName, PackageStatus status, Pageable pageable);

    Page<Package> findByPackageStatus(PackageStatus status, Pageable pageable);

    long countByPackageStatus(PackageStatus status);

    @Query(value = "{ 'packageName': { $regex: ?0, $options: 'i' } }", count = true)
    long countByPackageName(String search);

    @Query(value = "{ 'packageName': { $regex: ?0, $options: 'i' }, 'packageStatus': ?1 }", count = true)
    long countByPackageNameAndPackageStatus(String search, PackageStatus status);




}
