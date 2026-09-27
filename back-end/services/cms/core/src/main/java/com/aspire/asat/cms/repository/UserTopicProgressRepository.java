package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.UserTopicProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTopicProgressRepository extends MongoRepository<UserTopicProgress, String> {

    Optional<UserTopicProgress> findByUserIdAndTopicId(String userId, String topicId);

    List<UserTopicProgress> findAllByUserIdAndTopicId(String userId, String topicId);

    List<UserTopicProgress> findByUserId(String userId);

    boolean existsByUserIdAndTopicId(String userId, String topicId);

    void deleteByUserIdAndTopicId(String userId, String topicId);

    Optional<UserTopicProgress> findByUserIdAndTopicIdAndSubPackageId(String userId, String topicId, String subPackageId);

    List<UserTopicProgress> findByUserIdAndSubPackageId(String userId, String subPackageId);

    List<UserTopicProgress> findByUserIdAndIsBookmarkedTrue(String userId);

    @Query("{'userId': ?0, 'progress': 100}")
    Page<UserTopicProgress> findCompletedTopicsByUserId(String userId, Pageable pageable);

    long countByUserIdAndProgress(String userId, double progress);

    // ========== NEW METHODS FOR OPTIMIZED PAGINATION WITH SEARCH ==========

    /**
     * Find user topic progress with database-level pagination
     * @param userId the user ID
     * @param skip pagination skip value
     * @param limit pagination limit value
     * @return list of user topic progress records
     */
    @Aggregation(pipeline = {
        "{ $match: { userId: ?0 } }",
        "{ $sort: { lastSynced: -1 } }",
        "{ $skip: ?1 }",
        "{ $limit: ?2 }"
    })
    List<UserTopicProgress> findByUserIdWithPagination(String userId, int skip, int limit);

    long countByUserId(String userId);

    // ========== METHODS MIGRATED FROM UserTopicRepository ==========

    List<UserTopicProgress> findByUserIdAndStatus(String userId, String status);

    List<UserTopicProgress> findByUserIdAndSubPackageIdIn(String userId, List<String> subPackageIds);

}
