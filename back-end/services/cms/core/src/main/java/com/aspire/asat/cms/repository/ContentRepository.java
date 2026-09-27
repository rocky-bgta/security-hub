package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.dto.enums.CommonStatus;
import com.aspire.asat.cms.model.Content;
import com.aspire.asat.cms.model.TextContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends MongoRepository<Content, String> {

    boolean existsByContentName(String contentName);

    @Query("{ 'contentName': { $regex: ?0, $options: 'i' } }")
    Page<Content> findByContentNameContainingIgnoreCase(String text, Pageable pageable);

    @Query("{ 'contentName': { $regex: ?0, $options: 'i' } }")
    long countByContentNameContainingIgnoreCase(String text);

    Page<Content> findByStatus(CommonStatus status, Pageable pageable);

    long countByStatus(CommonStatus status);

    @Query("{ 'contentName': { $regex: ?0, $options: 'i' }, 'status': ?1 }")
    Page<Content> findByContentNameContainingIgnoreCaseAndStatus(String text, CommonStatus status, Pageable pageable);

    @Query("{ 'contentName': { $regex: ?0, $options: 'i' }, 'status': ?1 }")
    long countByContentNameContainingIgnoreCaseAndStatus(String text, CommonStatus status);

}
