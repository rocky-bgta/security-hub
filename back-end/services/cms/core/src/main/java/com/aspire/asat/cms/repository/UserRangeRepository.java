package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.UserRange;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRangeRepository extends MongoRepository<UserRange, String> {

    List<UserRange> findByIsActiveTrue(Sort sort);

    Optional<UserRange> findByIdAndIsActiveTrue(String id);

    boolean existsByRangeNameAndIsActiveTrue(String rangeName);

    // Check for overlapping ranges
    // Range overlaps if: (minUsers <= other.maxUsers OR other.maxUsers is null) AND (maxUsers >= other.minUsers OR maxUsers is null)
    @org.springframework.data.mongodb.repository.Query("{ " +
            "$and: [ " +
            "{ 'isActive': true }, " +
            "{ $or: [ " +
            "{ $and: [ { 'minUsers': { $lte: ?1 } }, { $or: [ { 'maxUsers': { $gte: ?0 } }, { 'maxUsers': null } ] } ] }, " +
            "{ $and: [ { 'maxUsers': null }, { 'minUsers': { $lte: ?1 } } ] } " +
            "] } " +
            "] }")
    List<UserRange> findOverlappingRanges(Integer minUsers, Integer maxUsers);

    @org.springframework.data.mongodb.repository.Query("{ " +
            "$and: [ " +
            "{ 'isActive': true }, " +
            "{ '_id': { $ne: ?2 } }, " +
            "{ $or: [ " +
            "{ $and: [ { 'minUsers': { $lte: ?1 } }, { $or: [ { 'maxUsers': { $gte: ?0 } }, { 'maxUsers': null } ] } ] }, " +
            "{ $and: [ { 'maxUsers': null }, { 'minUsers': { $lte: ?1 } } ] } " +
            "] } " +
            "] }")
    List<UserRange> findOverlappingRangesExcludingId(Integer minUsers, Integer maxUsers, String excludeId);
}

