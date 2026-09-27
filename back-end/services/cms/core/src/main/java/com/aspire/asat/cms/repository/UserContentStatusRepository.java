package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.UserContentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserContentStatusRepository extends MongoRepository<UserContentStatus, String> {

    Optional<UserContentStatus> findByUserIdAndContentId(String userId, String contentId);

    Optional<UserContentStatus> findByUserIdAndTopicIdAndContentId(String userId, String topicId, String contentId);

    List<UserContentStatus> findByUserId(String userId);

    List<UserContentStatus> findByUserIdAndTopicId(String userId, String topicId);

    boolean existsByUserIdAndContentId(String userId, String contentId);

    void deleteByUserIdAndContentId(String userId, String contentId);

    Optional<UserContentStatus> findByUserIdAndTopicIdAndContentIdAndSubPackageId(String userId, String topicId, String contentId, String subPackageId);

    List<UserContentStatus> findByUserIdAndSubPackageId(String userId, String subPackageId);

    // Legacy methods for backward compatibility
    Optional<UserContentStatus> findByUserIdAndCourseIdAndContentIdAndPackageId(String userId, String courseId, String contentId, String packageId);

    List<UserContentStatus> findByUserIdAndPackageId(String userId, String packageId);

    List<UserContentStatus> findByUserIdAndCourseId(String userId, String courseId);


}
