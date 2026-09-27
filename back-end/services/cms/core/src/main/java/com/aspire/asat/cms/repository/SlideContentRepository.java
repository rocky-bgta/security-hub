package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.model.SlideContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SlideContentRepository extends MongoRepository<SlideContent, UUID> {

    boolean existsByContentName(String contentName);

    @Query("{ 'contentName': { $regex: ?0, $options: 'i' } }")
    List<SlideContent> findByContentNameContainingIgnoreCase(String text);

    Page<SlideContent> findByContentType(ContentType contentType, Pageable pageable);

}
