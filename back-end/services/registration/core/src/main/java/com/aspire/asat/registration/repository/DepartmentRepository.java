package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {

    Optional<Department> findByNameIgnoreCase(String name);

    Optional<Department> findByNameAndClientAdminId(String name, String clientAdminId);

    List<Department> findByClientAdminIdAndActive(String clientAdminId, boolean active);

    @Query("{ $and: [" +
           "{ $or: [" +
           "{ 'name': { $regex: ?0, $options: 'i' } }," +
           "{ 'clientAdminId': { $regex: ?0, $options: 'i' } }" +
           "] }," +
           "{ $expr: { $cond: { if: { $ne: [?1, null] }, then: { $eq: ['$active', ?1] }, else: true } } }," +
           "{ $expr: { $cond: { if: { $ne: [?2, null] }, then: { $eq: ['$isSystemDefined', ?2] }, else: true } } }," +
           "{ $expr: { $cond: { if: { $ne: [?3, null] }, then: { $eq: ['$clientAdminId', ?3] }, else: true } } }" +
           "] }")
    Page<Department> findDepartmentsWithFilters(String search, Boolean active, Boolean isSystemDefined, 
                                               String clientAdminId, Pageable pageable);

    @Query(value = "{ $and: [" +
           "{ $or: [" +
           "{ 'name': { $regex: ?0, $options: 'i' } }," +
           "{ 'clientAdminId': { $regex: ?0, $options: 'i' } }" +
           "] }," +
           "{ $expr: { $cond: { if: { $ne: [?1, null] }, then: { $eq: ['$active', ?1] }, else: true } } }," +
           "{ $expr: { $cond: { if: { $ne: [?2, null] }, then: { $eq: ['$isSystemDefined', ?2] }, else: true } } }," +
           "{ $expr: { $cond: { if: { $ne: [?3, null] }, then: { $eq: ['$clientAdminId', ?3] }, else: true } } }" +
           "] }", count = true)
    long countDepartmentsWithFilters(String search, Boolean active, Boolean isSystemDefined, String clientAdminId);

    @Query("{ $and: [ " +
            "  { $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { $expr: { $eq: [?0, ''] } } ] }, " +
            "  { $or: [ { 'active': ?1 }, { $expr: { $eq: [?1, null] } } ] }, " +
            "  { $or: [ { 'clientAdminId': ?2 }, { 'isSystemDefined': true } ] } " +
            "] }")
    Page<Department> findDepartmentsForClientAdminWithSystemDefined(String search, Boolean active, String clientAdminId, Pageable pageable);

}
