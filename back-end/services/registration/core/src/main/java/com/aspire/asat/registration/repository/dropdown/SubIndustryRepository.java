package com.aspire.asat.registration.repository.dropdown;

import com.aspire.asat.registration.model.dropdown.SubIndustry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubIndustryRepository extends MongoRepository<SubIndustry, String> {

    List<SubIndustry> findByActiveTrue();

    List<SubIndustry> findByActiveTrueAndOrganizationTypeId(String organizationTypeId);

    List<SubIndustry> findByActiveTrueAndIndustryId(String industryId);

    List<SubIndustry> findByActiveTrueAndOrganizationTypeIdAndIndustryId(String organizationTypeId, String industryId);

    /**
     * List sub-industries with optional case-insensitive search on name/code,
     * optional active filter, and optional industryId filter.
     *
     * @param search     empty string matches all; otherwise regex on name or code
     * @param active     null matches both active and inactive
     * @param industryId null matches all industries; otherwise exact industryId
     */
    @Query("{ $and: [" +
            "{ $or: [" +
            "{ $expr: { $eq: [?0, ''] } }," +
            "{ 'name': { $regex: ?0, $options: 'i' } }," +
            "{ 'code': { $regex: ?0, $options: 'i' } }" +
            "] }," +
            "{ $expr: { $cond: { if: { $ne: [?1, null] }, then: { $eq: ['$active', ?1] }, else: true } } }," +
            "{ $expr: { $cond: { if: { $ne: [?2, null] }, then: { $eq: ['$industryId', ?2] }, else: true } } }" +
            "] }")
    List<SubIndustry> findWithFilters(String search, Boolean active, String industryId);

    Optional<SubIndustry> findByCode(String code);

    Optional<SubIndustry> findByNameIgnoreCase(String name);

    Optional<SubIndustry> findByIndustryIdAndCode(String industryId, String code);

    Optional<SubIndustry> findByIndustryIdAndNameIgnoreCase(String industryId, String name);

    boolean existsByCode(String code);

    boolean existsByIndustryIdAndCodeIgnoreCase(String industryId, String code);

    boolean existsByIndustryIdAndCodeIgnoreCaseAndIdNot(String industryId, String code, String id);
}
