package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.Category;
import com.aspire.asat.universal.enums.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByStatus(Status status);
}
