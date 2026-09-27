package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.UserCourseProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseProgressRepository extends MongoRepository<UserCourseProgress, String> {

    Optional<UserCourseProgress> findByUserIdAndCourseId(String userId, String courseId);

    List<UserCourseProgress> findByUserId(String userId);

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    void deleteByUserIdAndCourseId(String userId, String courseId);

    Optional<UserCourseProgress> findByUserIdAndCourseIdAndPackageId(String userId, String courseId, String packageId);

    List<UserCourseProgress> findByUserIdAndPackageId(String userId, String packageId);


}
