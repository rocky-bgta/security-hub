package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.model.topic.ContentType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentTypeRepository extends MongoRepository<ContentType, String> {
    boolean existsByTypeNameIgnoreCase(String typeName);
}
