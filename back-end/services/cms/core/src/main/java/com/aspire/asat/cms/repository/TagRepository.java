package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends MongoRepository<Tag, String> {

    boolean existsByNameIgnoreCase(String name);
}
