package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.BasePackageConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasePackageConfigRepository extends MongoRepository<BasePackageConfig, String> {
    
    Optional<BasePackageConfig> findByBasePackageId(String basePackageId);
    
    List<BasePackageConfig> findByActiveTrue();
    
    List<BasePackageConfig> findByNameContainingIgnoreCase(String name);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndBasePackageIdNot(String name, String basePackageId);
}
