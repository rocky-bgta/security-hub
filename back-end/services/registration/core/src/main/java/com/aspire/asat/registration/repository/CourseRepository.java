package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CourseRepository extends MongoRepository<Course, UUID> {

}
