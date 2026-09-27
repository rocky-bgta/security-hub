package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.UserCourse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseRepository extends MongoRepository<UserCourse, String> {

    List<UserCourse> findByUserId(String userId);

    List<UserCourse> findByUserIdAndStatus(String userId, String status);

    Optional<UserCourse> findByUserIdAndCourseId(String userId, String courseId);

    Optional<UserCourse> findByUserIdAndCourseIdAndPackageId(String userId, String courseId, String packageId);

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    List<UserCourse> findByUserIdAndPackageId(String userId, String packageId);

}
