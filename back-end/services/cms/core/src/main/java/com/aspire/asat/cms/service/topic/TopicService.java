package com.aspire.asat.cms.service.topic;

import com.aspire.asat.cms.dto.client.responseDto.TopicDetailsResponseDTO;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.topic.TopicDetailResponseDto;
import com.aspire.asat.cms.dto.topic.TopicFilterRequest;
import com.aspire.asat.cms.dto.topic.TopicFilterResponse;
import com.aspire.asat.cms.dto.topic.TopicMinimalDto;
import com.aspire.asat.cms.dto.topic.TopicReqDto;
import com.aspire.asat.cms.dto.topic.TopicRespDto;
import com.aspire.asat.cms.dto.topic.TopicsByProductPackagesPageDto;
import com.aspire.asat.cms.dto.topic.TopicsByProductPackagesRequest;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesRequest;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesResponseDto;
import com.aspire.asat.cms.dto.topic.MspTopicViewResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TopicService {
    TopicRespDto createTopic(TopicReqDto topicReqDto);
    TopicRespDto updateTopic(String id, TopicReqDto topicReqDto);
    TopicDetailResponseDto getByIdWithDetails(String id);
    Page<TopicRespDto> getAllTopics(String search, TopicStatus status, Integer page, Integer size, String sortBy, String order);

    /**
     * Paginated private topics owned by the current client ({@code isPrivate=true},
     * {@code clientId} = CurrentContext.clientAdminId).
     */
    Page<TopicRespDto> getPrivateTopicsForCurrentClient(
            String search, TopicStatus status, Integer page, Integer size, String sortBy, String order);

    /**
     * Returns {@code true} when a topic with the given name already exists (case-insensitive).
     */
    boolean existsByTopicName(String topicName);

    Long getTotalTopicCount();
    void deleteTopicById(String id);
    TopicFilterResponse filterTopicsByPackage(String packageId, TopicFilterRequest topicFilterRequest);
    TopicFilterResponse filterTopicsByProduct(String productId, TopicFilterRequest topicFilterRequest);
    TopicFilterResponse filterTopics(TopicFilterRequest topicFilterRequest);

    /**
     * Soft-match recommendation: ranks hard-scoped topics by maximum matching criteria.
     * Unmatched soft criteria never exclude a topic. Does not change filter APIs.
     */
    TopicFilterResponse recommendTopicsByProduct(String productId, TopicFilterRequest topicFilterRequest);

    TopicFilterResponse recommendTopicsByPackage(String packageId, TopicFilterRequest topicFilterRequest);

    TopicFilterResponse recommendTopics(TopicFilterRequest topicFilterRequest);

    Long getTopicCountBySubPackage(String subPackageId);
    
    // Client-facing topic details method
    TopicDetailsResponseDTO getTopicDetails(String topicId, String userId, String subpackageId);
    
    // Get minimal topic information by product and package
    Page<TopicMinimalDto> getTopicsByProductAndPackage(String productId, String packageId, String search, 
                                                        Integer offset, Integer pageSize, String sortBy, String order);

    /**
     * Get ENABLED topics matching (or excluding) any of the provided productId/packageId pairs.
     * Always includes both assigned ({@code total}) and locked ({@code totalLocked}) counts.
     */
    TopicsByProductPackagesPageDto getTopicsByProductPackagePairs(TopicsByProductPackagesRequest request);

    /**
     * Monthly topic distribution for MSP product/package pairs (totalContent) and
     * client product/package pairs (usedContent) for the current year.
     * Same response shape as {@link #getTopicDistribution()}.
     */
    TopicCountsByProductPackagesResponseDto countTopicsByProductPackages(
            TopicCountsByProductPackagesRequest request);

    /**
     * MSP topic view: category, available countries, product name, content type, duration,
     * and all packages for the topic's product with {@code isPurchase} based on {@code msp_products}.
     */
    MspTopicViewResponseDto getTopicForMsp(String topicId, String mspId);
    
    // Get topic distribution for 12 months
    com.aspire.asat.cms.dto.topic.TopicDistributionResponseDto getTopicDistribution();
}
