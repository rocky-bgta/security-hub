package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.model.topic.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {
    // Basic queries
    Optional<Topic> findByIdAndStatus(String id, String status);

    boolean existsByTopicNameIgnoreCase(String topicName);

    Page<Topic> findByStatus(TopicStatus status, Pageable pageable);

    @Query("{ 'topicName': { $regex: ?0, $options: 'i' }, 'status': ?1 }")
    Page<Topic> findByTopicNameContainingIgnoreCaseAndStatus(String topicName, TopicStatus status, Pageable pageable);

    @Query("{ 'topicName': { $regex: ?0, $options: 'i' } }")
    Page<Topic> findByTopicNameContainingIgnoreCase(String topicName, Pageable pageable);

    // Product queries
    @Query("{'productPackages.productName': ?0, 'status': 'ACTIVE'}")
    List<Topic> findByProductPackages_ProductName(String productName);

    @Query("{'productPackages.productName': ?0, 'productPackages.packages': { $in: ?1 }, 'status': 'ACTIVE'}")
    List<Topic> findByProductAndPackages(String productName, List<String> packages);

    // Chapter queries
    @Query("{'chapterIds': { $in: ?0 }, 'status': 'ACTIVE'}")
    List<Topic> findByChapterIds(List<String> chapterIds);

    // Count queries
    Long countByStatus(TopicStatus status);

    // Bulk operations
    @Query("{'status': 'ENABLED', '_id': { $in: ?0 }}")
    List<Topic> findActiveTopicsByIds(List<String> ids);

    // Query topics by productPackageIds
    @Query("{'productPackageMappings.packageIds': { $in: ?0 }, 'status': 'ENABLED'}")
    List<Topic> findBySubPackageIds(List<String> productPackageIds);

    // Private topics owned by a client
    Page<Topic> findByIsPrivateTrueAndClientId(String clientId, Pageable pageable);

    Page<Topic> findByIsPrivateTrueAndClientIdAndStatus(String clientId, TopicStatus status, Pageable pageable);

    @Query("{ 'isPrivate': true, 'clientId': ?0, 'topicName': { $regex: ?1, $options: 'i' } }")
    Page<Topic> findByIsPrivateTrueAndClientIdAndTopicNameContainingIgnoreCase(
            String clientId, String topicName, Pageable pageable);

    @Query("{ 'isPrivate': true, 'clientId': ?0, 'status': ?1, 'topicName': { $regex: ?2, $options: 'i' } }")
    Page<Topic> findByIsPrivateTrueAndClientIdAndStatusAndTopicNameContainingIgnoreCase(
            String clientId, TopicStatus status, String topicName, Pageable pageable);
}
