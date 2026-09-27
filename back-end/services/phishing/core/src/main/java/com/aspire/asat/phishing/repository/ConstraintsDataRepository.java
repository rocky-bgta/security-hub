package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.ConstraintsData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for configurable {@link ConstraintsData} documents.
 */
@Repository
public interface ConstraintsDataRepository extends MongoRepository<ConstraintsData, String> {

    @Query("{ 'isActive': { $ne: false } }")
    Page<ConstraintsData> findWhereEffectiveActive(Pageable pageable);

    @Query("{ 'isActive': false }")
    Page<ConstraintsData> findWhereInactive(Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': { $ne: false } }")
    Page<ConstraintsData> searchByNameWhereEffectiveActive(String search, Pageable pageable);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': false }")
    Page<ConstraintsData> searchByNameWhereInactive(String search, Pageable pageable);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': { $ne: false } }", count = true)
    long countSearchByNameWhereEffectiveActive(String search);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': false }", count = true)
    long countSearchByNameWhereInactive(String search);

    @Query(value = "{ 'isActive': { $ne: false } }", count = true)
    long countWhereEffectiveActive();

    @Query(value = "{ 'isActive': false }", count = true)
    long countWhereInactive();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    List<ConstraintsData> findAllByIsDefaultTrue();
}
