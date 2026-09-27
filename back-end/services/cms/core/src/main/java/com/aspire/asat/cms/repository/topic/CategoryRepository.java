package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.model.topic.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
