package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.model.LinkContent;
import com.aspire.asat.cms.model.TextContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LinkContentRepository  extends MongoRepository<LinkContent, UUID> {

    boolean existsByContentName(String contentName);

    @Query("{ 'contentName': { $regex: ?0, $options: 'i' } }")
    List<LinkContent> findByContentNameContainingIgnoreCase(String text);

    Page<LinkContent> findByContentType(ContentType contentType, Pageable pageable);

    long countByContentType(ContentType contentType);

    @Query("{ 'contentName': { $regex: ?0, $options: 'i' }, 'contentType': ?1 }")
    Page<LinkContent> findByContentNameContainingIgnoreCaseAndContentType(String contentName, ContentType contentType, Pageable pageable);
}
