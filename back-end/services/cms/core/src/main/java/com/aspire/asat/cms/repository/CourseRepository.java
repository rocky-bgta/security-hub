package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.dto.enums.CourseStatus;
import com.aspire.asat.cms.dto.enums.ProductStatus;
import com.aspire.asat.cms.model.Course;
import com.aspire.asat.cms.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    boolean existsByCourseName(String courseName);

    @Query("{ 'courseName': { $regex: ?0, $options: 'i' } }")
    Page<Course> findByCourseName(String text, Pageable pageable);

    Page<Course> findByCourseStatus(CourseStatus status, Pageable pageable);
}
