package com.aspire.asat.cms.service.impl.topic;

import com.aspire.asat.cms.client.service.CountryServiceClient;
import com.aspire.asat.cms.config.TopicStatusConfig;
import com.aspire.asat.cms.dto.client.responseDto.ChapterDetailsDTO;
import com.aspire.asat.cms.dto.client.responseDto.ContentStatusDTO;
import com.aspire.asat.cms.dto.client.responseDto.TopicDetailsResponseDTO;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.question.QuestionStatus;
import com.aspire.asat.cms.dto.topic.CategoryRespDto;
import com.aspire.asat.cms.dto.topic.ComplianceRespDto;
import com.aspire.asat.cms.dto.topic.ContentTypeRespDto;
import com.aspire.asat.cms.dto.topic.CountryDetailsDto;
import com.aspire.asat.cms.dto.topic.MspPackagePurchaseStatusDto;
import com.aspire.asat.cms.dto.topic.MspTopicViewResponseDto;
import com.aspire.asat.cms.dto.topic.PackageDetailsDto;
import com.aspire.asat.cms.dto.topic.ProductDetailsDto;
import com.aspire.asat.cms.dto.topic.ProductPackageDetailsMapping;
import com.aspire.asat.cms.dto.topic.ProductPackageMapping;
import com.aspire.asat.cms.dto.topic.ProductPackagePairDto;
import com.aspire.asat.cms.dto.topic.TopicDetailResponseDto;
import com.aspire.asat.cms.dto.topic.TopicFilterRequest;
import com.aspire.asat.cms.dto.topic.TopicFilterResponse;
import com.aspire.asat.cms.dto.topic.TopicDistributionItemDto;
import com.aspire.asat.cms.dto.topic.TopicDistributionResponseDto;
import com.aspire.asat.cms.dto.topic.TopicMinimalDto;
import com.aspire.asat.cms.dto.topic.TopicReqDto;
import com.aspire.asat.cms.dto.topic.TopicRespDto;
import com.aspire.asat.cms.dto.topic.TopicsByProductPackagesPageDto;
import com.aspire.asat.cms.dto.topic.TopicsByProductPackagesRequest;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesRequest;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesResponseDto;
import com.aspire.asat.cms.exception.DuplicateDataFoundException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.mapper.CountryMapper;
import com.aspire.asat.cms.model.topic.Category;
import com.aspire.asat.cms.model.topic.Compliance;
import com.aspire.asat.cms.model.topic.ContentType;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ProductPackageRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.question.QuestionCustomRepository;
import com.aspire.asat.cms.repository.topic.CategoryRepository;
import com.aspire.asat.cms.repository.topic.ComplianceRepository;
import com.aspire.asat.cms.repository.topic.ContentTypeRepository;
import com.aspire.asat.cms.repository.topic.TopicFilterRepositoryCustom;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.repository.topic.TopicRepositoryCustom;
import com.aspire.asat.cms.repository.topic.TopicDistributionRepositoryCustom;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.service.topic.TopicService;
import com.aspire.asat.cms.util.TopicPrivacyCriteria;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.service.files.FileService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final TopicFilterRepositoryCustom topicFilterRepositoryCustom;
    private final TopicRepositoryCustom topicRepositoryCustom;
    private final TopicDistributionRepositoryCustom topicDistributionRepositoryCustom;
    private final CategoryRepository categoryRepository;
    private final CountryServiceClient countryServiceClient;
    private final CountryMapper countryMapper;
    private final ComplianceRepository complianceRepository;
    private final ContentTypeRepository contentTypeRepository;
    private final ProductRepository productRepository;
    private final ProductPackageRepository productPackageRepository;
    private final QuestionCustomRepository questionCustomRepository;
    private final FileService fileService;
    private final TopicStatusConfig topicStatusConfig;
    private final SubPackageRepository subPackageRepository;
    private final MongoTemplate mongoTemplate;
    private final UserCurrentContextService userCurrentContextService;
    private final RegistrationServiceClient registrationServiceClient;

    @Override
    public TopicRespDto createTopic(TopicReqDto topicReqDto) {
        log.info("Creating topic with topic name: {}", topicReqDto.getTopicName());

        // Validate topic name uniqueness
        if (topicRepository.existsByTopicNameIgnoreCase(topicReqDto.getTopicName())) {
            log.info("Topic with name '{}' already exists.", topicReqDto.getTopicName());
            throw new DuplicateDataFoundException("Topic with name '" + topicReqDto.getTopicName() + "' already exists.");
        }

        // Validate category IDs
        if (topicReqDto.getCategoryIds() != null && !topicReqDto.getCategoryIds().isEmpty()) {
            validateCategoryIds(topicReqDto.getCategoryIds());
        }

        // Validate compliance IDs
        if (topicReqDto.getComplianceIds() != null && !topicReqDto.getComplianceIds().isEmpty()) {
            validateComplianceIds(topicReqDto.getComplianceIds());
        }

        // Validate content type ID
        if (topicReqDto.getContentTypeId() != null && !topicReqDto.getContentTypeId().isEmpty()) {
            validateContentTypeId(topicReqDto.getContentTypeId());
        }

        Instant currentTime = Instant.now();
        Topic topicToSave = Topic.toTopic(UUID.randomUUID().toString(), topicReqDto, currentTime, currentTime);
        Topic savedTopic = topicRepository.save(topicToSave);
        log.info("Topic created successfully with id: {}", savedTopic.getId());
        return Topic.toTopicRespDto(savedTopic);
    }

    @Override
    public TopicRespDto updateTopic(String id, TopicReqDto topicReqDto) {
        log.info("Updating topic with id: {}", id);
        Topic existingTopic = topicRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Topic with id '{}' not found for update.", id);
                    return new ResourceAccessException("Topic with id '" + id + "' not found.");
                });

        if (!existingTopic.getTopicName().equalsIgnoreCase(topicReqDto.getTopicName())
                && topicRepository.existsByTopicNameIgnoreCase(topicReqDto.getTopicName())) {
            log.info("Topic with name '{}' already exists.", topicReqDto.getTopicName());
            throw new DuplicateDataFoundException("Topic with name '" + topicReqDto.getTopicName() + "' already exists.");
        }

        // Validate category IDs if provided
        if (topicReqDto.getCategoryIds() != null && !topicReqDto.getCategoryIds().isEmpty()) {
            validateCategoryIds(topicReqDto.getCategoryIds());
        }

        // Validate compliance IDs if provided
        if (topicReqDto.getComplianceIds() != null && !topicReqDto.getComplianceIds().isEmpty()) {
            validateComplianceIds(topicReqDto.getComplianceIds());
        }

        // Validate content type ID if provided
        if (topicReqDto.getContentTypeId() != null && !topicReqDto.getContentTypeId().isEmpty()) {
            validateContentTypeId(topicReqDto.getContentTypeId());
        }

        // Store the original duration to check if it changed
        Integer originalDuration = existingTopic.getDurationMinutes();

        updateTopicFromRequest(existingTopic, topicReqDto);
        Instant currentTime = Instant.now();
        existingTopic.setUpdatedAt(currentTime);

        // Check if duration was updated and apply new status logic
        Integer newDuration = existingTopic.getDurationMinutes();
        boolean durationChanged = !Objects.equals(originalDuration, newDuration);

        if (durationChanged) {
            log.info("Topic duration updated from {} to {} minutes, checking status requirements",
                    originalDuration, newDuration);
            updateTopicStatusBasedOnDurationAndQuestions(existingTopic);
        }

        Topic updatedTopic = topicRepository.save(existingTopic);
        log.info("Topic updated successfully with id: {}", updatedTopic.getId());
        return Topic.toTopicRespDto(updatedTopic);
    }

    @Override
    public TopicDetailResponseDto getByIdWithDetails(String id) {
        log.info("Fetching topic with details for id: {}", id);

        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Topic with id '{}' not found.", id);
                    return new ResourceNotFoundException("Topic with id '" + id + "' not found.");
                });
        assertTopicVisibleToCaller(topic);

        // Get category details
        List<CategoryRespDto> categoryDetails = topic.getCategoryIds() != null ?
                categoryRepository.findAllById(topic.getCategoryIds())
                        .stream()
                        .map(Category::toCategoryRespDto)
                        .toList() : List.of();

        // Get country details from the Registration service
        List<CountryDetailsDto> countryDetails = List.of();
        if (topic.getCountryIds() != null && !topic.getCountryIds().isEmpty()) {
            try {
                // Fetch active countries from Registration service
                List<CountryDetailsDto> allActiveCountries = countryMapper.toCountryDetailsDtoList(
                        countryServiceClient.getActiveCountries()
                );
                
                // Filter countries by topic's country IDs
                countryDetails = countryMapper.filterCountriesByTopic(allActiveCountries, topic.getCountryIds());
                
                log.info("Found {} countries for topic {} from Registration service", 
                        countryDetails.size(), topic.getId());
                        
            } catch (Exception e) {
                log.error("Error fetching countries from Registration service for topic {}", topic.getId(), e);
                countryDetails = List.of();
            }
        }

        // Get compliance details
        List<ComplianceRespDto> complianceDetails = topic.getComplianceIds() != null ?
                complianceRepository.findAllById(topic.getComplianceIds())
                        .stream()
                        .map(Compliance::toComplianceRespDto)
                        .toList() : List.of();

        // Get content type details
        ContentTypeRespDto contentTypeDetails = topic.getContentTypeId() != null ?
                contentTypeRepository.findById(topic.getContentTypeId())
                        .map(ContentType::contentTypeRespDto)
                        .orElse(null) : null;

        // Get detailed product and package information
        List<ProductPackageDetailsMapping> productPackageDetails = getProductPackageDetails(topic.getProductPackageMappings());

        return TopicDetailResponseDto.builder()
                .id(topic.getId())
                .topicName(topic.getTopicName())
                .description(topic.getDescription())
                .status(topic.getStatus())
                .chapterIds(topic.getChapterIds())
                .productPackages(productPackageDetails)
                .thumbnailUrl(topic.getThumbnailUrl())
                .durationMinutes(topic.getDurationMinutes())
                .totalContentCount(topic.getTotalContentCount())
                .payloadType(topic.getPayloadType())
                .difficulty(topic.getDifficulty())
                .tone(topic.getTone())
                .attackerPersona(topic.getAttackerPersona())
                .socialEngineeringStrategy(topic.getSocialEngineeringStrategy())
                .campaignObjective(topic.getCampaignObjective())
                .triggerEvent(topic.getTriggerEvent())
                .attackTechnique(topic.getAttackTechnique())
                .emotionalTrigger(topic.getEmotionalTrigger())
                .urgencyLevel(topic.getUrgencyLevel())
                .brand(topic.getBrand())
                .callToAction(topic.getCallToAction())
                .industry(topic.getIndustry())
                .subIndustry(topic.getSubIndustry())
                .tags(topic.getTags())
                .createdBy(topic.getCreatedBy())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .clientId(topic.getClientId())
                .isPrivate(topic.getIsPrivate())
                .categoryDetails(categoryDetails)
                .countryDetails(countryDetails)
                .complianceDetails(complianceDetails)
                .contentTypeDetails(contentTypeDetails)
                .build();
    }

    @Override
    public MspTopicViewResponseDto getTopicForMsp(String topicId, String mspId) {
        log.info("Fetching MSP topic view for topicId: {}, mspId: {}", topicId, mspId);

        if (!StringUtils.hasText(mspId)) {
            throw new IllegalArgumentException("mspId is required");
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> {
                    log.error("Topic with id '{}' not found.", topicId);
                    return new ResourceNotFoundException("Topic with id '" + topicId + "' not found.");
                });
        // assertTopicVisibleToCaller(topic);

        List<String> categories = topic.getCategoryIds() != null ?
                categoryRepository.findAllById(topic.getCategoryIds())
                        .stream()
                        .map(Category::getCategoryName)
                        .filter(StringUtils::hasText)
                        .toList() : List.of();

        List<String> availableCountries = List.of();
        if (topic.getCountryIds() != null && !topic.getCountryIds().isEmpty()) {
            try {
                List<CountryDetailsDto> allActiveCountries = countryMapper.toCountryDetailsDtoList(
                        countryServiceClient.getActiveCountries()
                );
                availableCountries = countryMapper.filterCountriesByTopic(allActiveCountries, topic.getCountryIds())
                        .stream()
                        .map(CountryDetailsDto::getCountryName)
                        .filter(StringUtils::hasText)
                        .toList();
            } catch (Exception e) {
                log.error("Error fetching countries for MSP topic view, topicId={}", topicId, e);
            }
        }

        String contentType = topic.getContentTypeId() != null ?
                contentTypeRepository.findById(topic.getContentTypeId())
                        .map(ContentType::getTypeName)
                        .orElse(null) : null;

        List<String> difficultyLevel = topic.getDifficulty() != null ?
                topic.getDifficulty().stream()
                        .filter(d -> d != null && StringUtils.hasText(d.getDifficultyName()))
                        .map(d -> d.getDifficultyName())
                        .toList() : List.of();

        String productId = null;
        String productName = null;
        List<ProductPackageMapping> mappings = topic.getProductPackageMappings();
        if (mappings != null && !mappings.isEmpty()) {
            ProductPackageMapping primaryMapping = mappings.stream()
                    .filter(m -> m != null && StringUtils.hasText(m.getProductId()))
                    .findFirst()
                    .orElse(null);
            if (primaryMapping != null) {
                productId = primaryMapping.getProductId();
                productName = StringUtils.hasText(primaryMapping.getProductName())
                        ? primaryMapping.getProductName()
                        : productRepository.findById(productId)
                                .map(product -> product.getProductName())
                                .orElse(null);
            }
        }

        List<MspPackagePurchaseStatusDto> packages = List.of();
        if (StringUtils.hasText(productId)) {
            List<com.aspire.asat.cms.model.ProductPackage> productPackages =
                    productPackageRepository.findByProductId(productId);
            Set<String> purchasedPackageIds = new HashSet<>(
                    registrationServiceClient.getPurchasedPackageIds(mspId, productId));

            packages = productPackages.stream()
                    .map(pkg -> MspPackagePurchaseStatusDto.builder()
                            .packageName(pkg.getName())
                            .isPurchase(purchasedPackageIds.contains(pkg.getId()))
                            .build())
                    .toList();
        }

        return MspTopicViewResponseDto.builder()
                .id(topic.getId())
                .topicName(topic.getTopicName())
                .categories(categories)
                .availableCountries(availableCountries)
                .productName(productName)
                .contentType(contentType)
                .duration(topic.getDurationMinutes())
                .difficultyLevel(difficultyLevel)
                .packages(packages)
                .build();
    }

    @Override
    public Page<TopicRespDto> getAllTopics(String search, TopicStatus status, Integer page, Integer size, String sortBy, String order) {
        log.info("Getting all topics with search: {}, status: {}, page: {}, size: {}, sortBy: {}, order: {}", 
                search, status, page, size, sortBy, order);
        
        // Handle pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10;
        
        // Handle sorting
        Sort.Direction direction = Sort.Direction.DESC;
        if (order != null) {
            try {
                direction = Sort.Direction.fromString(order.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid sort direction '{}', defaulting to DESC", order);
                direction = Sort.Direction.DESC;
            }
        }
        
        String sortField = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "createdAt";
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));

        String viewerClientId = resolveViewerClientId();
        // No client context (Aspire admin / internal): keep the original repository path so
        // catalog listing behavior is unchanged. Privacy scoping only applies for client viewers.
        if (!StringUtils.hasText(viewerClientId)) {
            Page<Topic> topicPage;
            if (search != null && !search.trim().isEmpty() && status != null) {
                topicPage = topicRepository.findByTopicNameContainingIgnoreCaseAndStatus(search.trim(), status, pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                topicPage = topicRepository.findByTopicNameContainingIgnoreCase(search.trim(), pageable);
            } else if (status != null) {
                topicPage = topicRepository.findByStatus(status, pageable);
            } else {
                topicPage = topicRepository.findAll(pageable);
            }
            return new PageImpl<>(getTopicRespList(topicPage.getContent()), pageable, topicPage.getTotalElements());
        }

        List<Criteria> criteriaList = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            String escaped = java.util.regex.Pattern.quote(search.trim());
            criteriaList.add(Criteria.where("topicName").regex(".*" + escaped + ".*", "i"));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }
        TopicPrivacyCriteria.appendIfViewerPresent(criteriaList, viewerClientId);

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        long total = mongoTemplate.count(query, Topic.class);
        query.with(pageable);
        List<Topic> topics = mongoTemplate.find(query, Topic.class);
        return new PageImpl<>(getTopicRespList(topics), pageable, total);
    }

    @Override
    public Page<TopicRespDto> getPrivateTopicsForCurrentClient(
            String search, TopicStatus status, Integer page, Integer size, String sortBy, String order) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        if (!StringUtils.hasText(clientId)) {
            throw new ResourceNotFoundException("Unauthorized resource access");
        }
        clientId = clientId.trim();
        log.info("Getting private topics for clientId={}, search={}, status={}, page={}, size={}",
                clientId, search, status, page, size);

        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10;

        Sort.Direction direction = Sort.Direction.DESC;
        if (order != null) {
            try {
                direction = Sort.Direction.fromString(order.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid sort direction '{}', defaulting to DESC", order);
                direction = Sort.Direction.DESC;
            }
        }
        String sortField = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "createdAt";
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));

        boolean hasSearch = search != null && !search.trim().isEmpty();
        Page<Topic> topicPage;
        if (hasSearch && status != null) {
            topicPage = topicRepository.findByIsPrivateTrueAndClientIdAndStatusAndTopicNameContainingIgnoreCase(
                    clientId, status, search.trim(), pageable);
        } else if (hasSearch) {
            topicPage = topicRepository.findByIsPrivateTrueAndClientIdAndTopicNameContainingIgnoreCase(
                    clientId, search.trim(), pageable);
        } else if (status != null) {
            topicPage = topicRepository.findByIsPrivateTrueAndClientIdAndStatus(clientId, status, pageable);
        } else {
            topicPage = topicRepository.findByIsPrivateTrueAndClientId(clientId, pageable);
        }

        return new PageImpl<>(getTopicRespList(topicPage.getContent()), pageable, topicPage.getTotalElements());
    }

    @Override
    public boolean existsByTopicName(String topicName) {
        if (topicName == null || topicName.isBlank()) {
            return false;
        }
        return topicRepository.existsByTopicNameIgnoreCase(topicName.trim());
    }

    @Override
    public Long getTotalTopicCount() {
        return topicRepository.count();
    }

    @Override
    public void deleteTopicById(String id) {
        log.info("Deleting topic with id: {}", id);
        Topic existingTopic = topicRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Topic with id '{}' not found for deletion.", id);
                    return new ResourceNotFoundException("Topic with id '" + id + "' not found.");
                });

        existingTopic.setStatus(TopicStatus.DISABLED);
        existingTopic.setUpdatedAt(Instant.now());
        topicRepository.save(existingTopic);
    }

    @Override
    public TopicFilterResponse filterTopicsByPackage(String packageId, TopicFilterRequest topicFilterRequest) {
        applyViewerClientId(topicFilterRequest);
        Page<Topic> topicsPage = topicFilterRepositoryCustom.findByFilters(packageId, topicFilterRequest);
        return toTopicFilterResponse(topicsPage);
    }

    @Override
    public TopicFilterResponse filterTopicsByProduct(String productId, TopicFilterRequest topicFilterRequest) {
        applyViewerClientId(topicFilterRequest);
        Page<Topic> topicsPage = topicFilterRepositoryCustom.findByFiltersByProduct(productId, topicFilterRequest);
        return toTopicFilterResponse(topicsPage);
    }

    @Override
    public TopicFilterResponse filterTopics(TopicFilterRequest topicFilterRequest) {
        applyViewerClientId(topicFilterRequest);
        Page<Topic> topicsPage = topicFilterRepositoryCustom.findByFiltersUnscoped(topicFilterRequest);
        return toTopicFilterResponse(topicsPage);
    }

    @Override
    public TopicFilterResponse recommendTopicsByProduct(String productId, TopicFilterRequest topicFilterRequest) {
        applyViewerClientId(topicFilterRequest);
        Page<TopicFilterRepositoryCustom.RecommendedTopic> page =
                topicFilterRepositoryCustom.recommendByProduct(productId, topicFilterRequest);
        return toRecommendFilterResponse(page);
    }

    @Override
    public TopicFilterResponse recommendTopicsByPackage(String packageId, TopicFilterRequest topicFilterRequest) {
        applyViewerClientId(topicFilterRequest);
        Page<TopicFilterRepositoryCustom.RecommendedTopic> page =
                topicFilterRepositoryCustom.recommendByPackage(packageId, topicFilterRequest);
        return toRecommendFilterResponse(page);
    }

    @Override
    public TopicFilterResponse recommendTopics(TopicFilterRequest topicFilterRequest) {
        applyViewerClientId(topicFilterRequest);
        Page<TopicFilterRepositoryCustom.RecommendedTopic> page =
                topicFilterRepositoryCustom.recommendUnscoped(topicFilterRequest);
        return toRecommendFilterResponse(page);
    }

    private TopicFilterResponse toTopicFilterResponse(Page<Topic> topicsPage) {
        List<TopicRespDto> topicRespDtos = topicsPage.getContent().stream()
                .map(this::convertToTopicRespDtoWithDetails)
                .toList();
        return TopicFilterResponse.builder()
                .topics(topicRespDtos)
                .totalElements(topicsPage.getTotalElements())
                .totalPages(topicsPage.getTotalPages())
                .currentPage(topicsPage.getNumber())
                .hasNext(topicsPage.hasNext())
                .hasPrevious(topicsPage.hasPrevious())
                .build();
    }

    private TopicFilterResponse toRecommendFilterResponse(
            Page<TopicFilterRepositoryCustom.RecommendedTopic> page) {
        List<TopicRespDto> topicRespDtos = page.getContent().stream()
                .map(recommended -> {
                    TopicRespDto dto = convertToTopicRespDtoWithDetails(recommended.topic());
                    dto.setMatchScore(recommended.matchScore());
                    return dto;
                })
                .toList();
        return TopicFilterResponse.builder()
                .topics(topicRespDtos)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private void updateTopicFromRequest(Topic topic, TopicReqDto request) {
        topic.setTopicName(request.getTopicName());

        if (request.getCategoryIds() != null) {
            topic.setCategoryIds(request.getCategoryIds());
        }

        if (request.getCountryIds() != null) {
            topic.setCountryIds(request.getCountryIds());
        }

        if (request.getComplianceIds() != null) {
            topic.setComplianceIds(request.getComplianceIds());
        }

        if (request.getContentTypeId() != null) {
            topic.setContentTypeId(request.getContentTypeId());
        }

        if (request.getDurationMinutes() != null) {
            topic.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getDescription() != null) {
            topic.setDescription(request.getDescription());
        }

        if (request.getThumbnailUrl() != null) {
            topic.setThumbnailUrl(request.getThumbnailUrl());
        }

        if (request.getProductPackages() != null) {
            topic.setProductPackageMappings(request.getProductPackages());
        }

        if (request.getChapterIds() != null) {
            topic.setChapterIds(request.getChapterIds());
        }

        if (request.getTotalContentCount() != null) {
            topic.setTotalContentCount(request.getTotalContentCount());
        }

        // Optional phishing-style metadata: null = leave untouched, empty list = clear.
        if (request.getPayloadType() != null) {
            topic.setPayloadType(request.getPayloadType());
        }
        if (request.getDifficulty() != null) {
            topic.setDifficulty(request.getDifficulty());
        }
        if (request.getTone() != null) {
            topic.setTone(request.getTone());
        }
        if (request.getAttackerPersona() != null) {
            topic.setAttackerPersona(request.getAttackerPersona());
        }
        if (request.getSocialEngineeringStrategy() != null) {
            topic.setSocialEngineeringStrategy(request.getSocialEngineeringStrategy());
        }
        if (request.getCampaignObjective() != null) {
            topic.setCampaignObjective(request.getCampaignObjective());
        }
        if (request.getTriggerEvent() != null) {
            topic.setTriggerEvent(request.getTriggerEvent());
        }
        if (request.getAttackTechnique() != null) {
            topic.setAttackTechnique(request.getAttackTechnique());
        }
        if (request.getEmotionalTrigger() != null) {
            topic.setEmotionalTrigger(request.getEmotionalTrigger());
        }
        if (request.getUrgencyLevel() != null) {
            topic.setUrgencyLevel(request.getUrgencyLevel());
        }
        if (request.getBrand() != null) {
            topic.setBrand(request.getBrand());
        }
        if (request.getCallToAction() != null) {
            topic.setCallToAction(request.getCallToAction());
        }
        if (request.getIndustry() != null) {
            topic.setIndustry(request.getIndustry());
        }
        if (request.getSubIndustry() != null) {
            topic.setSubIndustry(request.getSubIndustry());
        }
        if (request.getTags() != null) {
            topic.setTags(request.getTags());
        }

        if (request.getClientId() != null) {
            if (request.getClientId().isBlank()) {
                topic.setClientId(null);
                topic.setIsPrivate(false);
            } else {
                topic.setClientId(request.getClientId().trim());
                topic.setIsPrivate(true);
            }
        }

        if (request.getStatus() != null && !request.getStatus().equals(topic.getStatus())) {
            topic.setStatus(request.getStatus());
        }
    }

    /**
     * Validates that all provided category IDs exist in the database
     */
    private void validateCategoryIds(List<String> categoryIds) {
        log.info("Validating category IDs: {}", categoryIds);
        List<String> existingCategoryIds = categoryRepository.findAllById(categoryIds)
                .stream()
                .map(Category::getId)
                .toList();

        List<String> invalidIds = categoryIds.stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();

        if (!invalidIds.isEmpty()) {
            log.error("Invalid category IDs found: {}", invalidIds);
            throw new ResourceNotFoundException("Invalid category IDs: " + String.join(", ", invalidIds));
        }
        log.info("All category IDs are valid");
    }

    /**
     * Validates that all provided compliance IDs exist in the database
     */
    private void validateComplianceIds(List<String> complianceIds) {
        log.info("Validating compliance IDs: {}", complianceIds);
        List<String> existingComplianceIds = complianceRepository.findAllById(complianceIds)
                .stream()
                .map(Compliance::getId)
                .toList();

        List<String> invalidIds = complianceIds.stream()
                .filter(id -> !existingComplianceIds.contains(id))
                .toList();

        if (!invalidIds.isEmpty()) {
            log.error("Invalid compliance IDs found: {}", invalidIds);
            throw new ResourceNotFoundException("Invalid compliance IDs: " + String.join(", ", invalidIds));
        }
        log.info("All compliance IDs are valid");
    }

    /**
     * Validates that the provided content type ID exists in the database
     */
    private void validateContentTypeId(String contentTypeId) {
        log.info("Validating content type ID: {}", contentTypeId);
        boolean exists = contentTypeRepository.existsById(contentTypeId);

        if (!exists) {
            log.error("Invalid content type ID: {}", contentTypeId);
            throw new ResourceNotFoundException("Invalid content type ID: " + contentTypeId);
        }
        log.info("Content type ID is valid");
    }

    /**
     * Gets detailed product and package information for the given product package mappings
     */
    private List<ProductPackageDetailsMapping> getProductPackageDetails(List<ProductPackageMapping> productPackageMappings) {
        if (productPackageMappings == null || productPackageMappings.isEmpty()) {
            return List.of();
        }

        return productPackageMappings.stream()
                .map(mapping -> {
                    // Get product details
                    ProductDetailsDto productDetails = productRepository.findById(mapping.getProductId())
                            .map(product -> ProductDetailsDto.builder()
                                    .id(product.getId())
                                    .productName(product.getProductName())
                                    .productDescription(product.getProductDescription())
                                    .productStatus(product.getProductStatus())
                                    .thumbnailUrl(product.getThumbnailUrl())
                                    .createdAt(product.getCreatedAt())
                                    .updatedAt(product.getUpdatedAt())
                                    .lastModifiedBy(product.getLastModifiedBy())
                                    .build())
                            .orElse(null);

                    // Get package details
                    List<PackageDetailsDto> packageDetails = mapping.getPackageIds() != null && !mapping.getPackageIds().isEmpty() ?
                            productPackageRepository.findAllById(mapping.getPackageIds())
                                    .stream()
                                    .map(pkg -> PackageDetailsDto.builder()
                                            .id(pkg.getId())
                                            .name(pkg.getName())
                                            .productId(pkg.getProductId())
                                            .featureId(pkg.getFeatureId())
                                            .price(pkg.getPrice())
                                            .createdAt(pkg.getCreatedAt())
                                            .packageStatus(pkg.getPackageStatus())
                                            .basePackageId(pkg.getBasePackageId())
                                            .build())
                                    .toList() : List.of();

                    return ProductPackageDetailsMapping.builder()
                            .productDetails(productDetails)
                            .packageDetails(packageDetails)
                            .build();
                })
                .toList();
    }

    /**
     * Updates topic status based on duration and question count criteria using TopicStatusConfig.
     * This method uses the centralized configuration for determining topic status.
     */
    private void updateTopicStatusBasedOnDurationAndQuestions(Topic topic) {
        Integer durationMinutes = topic.getDurationMinutes();
        if (durationMinutes == null) {
            log.info("Topic {} has no duration set, skipping status update", topic.getId());
            return;
        }

        // Count active questions for this topic
        long questionCount = questionCustomRepository.countQuestionsWithFilters(
                topic.getId(),
                null, // search - null for no text search
                QuestionStatus.ACTIVE
        );

        log.info("Topic {} has {} active questions, duration: {} minutes",
                topic.getId(), questionCount, durationMinutes);

        // Use the centralized config to determine the appropriate status
        TopicStatus newStatus = topicStatusConfig.determineTopicStatus(durationMinutes, questionCount);

        // Log the criteria being applied
        String criteriaDescription = topicStatusConfig.getCriteriaDescription(durationMinutes);
        log.info("Applying criteria: {}", criteriaDescription);

        // Update topic status if it changed
        if (!newStatus.equals(topic.getStatus())) {
            TopicStatus oldStatus = topic.getStatus();
            topic.setStatus(newStatus);
            topic.setUpdatedAt(Instant.now());
            log.info("Topic {} status updated from {} to {} based on duration and question count",
                    topic.getId(), oldStatus, newStatus);
        } else {
            log.info("Topic {} status remains {} - no change needed", topic.getId(), topic.getStatus());
        }
    }

    @NotNull
    private List<TopicRespDto> getTopicRespList(List<Topic> topics) {
        return topics.stream()
                .map(topic -> {
                    TopicRespDto dto = Topic.toTopicRespDto(topic);
                    String previewUrl = ObjectUtils.isNotEmpty(dto.getThumbnailUrl())
                            ? fileService.buildUrl(dto.getThumbnailUrl())
                            : "";
                    dto.setThumbnailPreviewUrl(previewUrl);
                    return dto;
                })
                .toList();
    }

    /**
     * Converts a Topic entity to TopicRespDto with detailed category and content type information
     */
    private TopicRespDto convertToTopicRespDtoWithDetails(Topic topic) {
        TopicRespDto dto = Topic.toTopicRespDto(topic);
        
        // Get category details
        List<CategoryRespDto> categoryDetails = topic.getCategoryIds() != null ?
                categoryRepository.findAllById(topic.getCategoryIds())
                        .stream()
                        .map(Category::toCategoryRespDto)
                        .toList() : List.of();
        
        // Get content type details
        ContentTypeRespDto contentTypeDetails = topic.getContentTypeId() != null ?
                contentTypeRepository.findById(topic.getContentTypeId())
                        .map(ContentType::contentTypeRespDto)
                        .orElse(null) : null;
        
        // Set detailed information
        dto.setCategoryDetails(categoryDetails);
        dto.setContentTypeDetails(contentTypeDetails);
        
        // Set thumbnail preview URL
        String previewUrl = ObjectUtils.isNotEmpty(dto.getThumbnailUrl())
                ? fileService.buildUrl(dto.getThumbnailUrl())
                : "";
        dto.setThumbnailPreviewUrl(previewUrl);
        
        return dto;
    }


    @Override
    public Long getTopicCountBySubPackage(String subPackageId) {
        return topicFilterRepositoryCustom.countTopicsBySubPackage(subPackageId);
    }

    @Override
    public Page<TopicMinimalDto> getTopicsByProductAndPackage(String productId, String packageId, String search,
                                                               Integer offset, Integer pageSize, String sortBy, String order) {
        log.info("Getting topics by productId: {} and packageId: {} with search: {}, offset: {}, pageSize: {}, sortBy: {}, order: {}",
                productId, packageId, search, offset, pageSize, sortBy, order);

        // Validate required parameters
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("ProductId is required");
        }
        if (packageId == null || packageId.trim().isEmpty()) {
            throw new IllegalArgumentException("PackageId is required");
        }

        // Handle pagination parameters - offset is treated as page number (0-based)
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;
        int pageOffset = (offset != null && offset >= 0) ? offset : 0;
        
        // Calculate actual skip size: offset is treated as page number
        // offset=0, pageSize=4: skip 0, take 4 (items 0-3)
        // offset=1, pageSize=4: skip 4, take 4 (items 4-7)
        // offset=2, pageSize=4: skip 8, take 4 (items 8-11)
        int actualSkipSize = pageOffset * size;

        // Handle sorting
        Sort.Direction direction = Sort.Direction.DESC;
        if (order != null) {
            try {
                direction = Sort.Direction.fromString(order.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid sort direction '{}', defaulting to DESC", order);
                direction = Sort.Direction.DESC;
            }
        }

        String sortField = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "createdAt";
        
        // Create Pageable for sorting (page number is used only for sorting, actual skip is calculated separately)
        Pageable pageable = PageRequest.of(pageOffset, size, Sort.by(direction, sortField));

        log.debug("Pagination calculation: offset={}, pageSize={}, skipSize={}", pageOffset, size, actualSkipSize);

        // Delegate to repository - repository will handle offset-based pagination with calculated skip size
        return topicRepositoryCustom.findTopicsByProductAndPackage(
                productId, packageId, search, pageable, actualSkipSize, size, resolveViewerClientId());
    }

    private void applyViewerClientId(TopicFilterRequest request) {
        if (request == null) {
            return;
        }
        if (!StringUtils.hasText(request.getClientId())) {
            userCurrentContextService.findClientAdminId().ifPresent(request::setClientId);
        }
    }

    private String resolveViewerClientId() {
        return userCurrentContextService.findClientAdminId().orElse(null);
    }

    private void assertTopicVisibleToCaller(Topic topic) {
        String viewerClientId = resolveViewerClientId();
        if (!TopicPrivacyCriteria.isVisible(topic.getIsPrivate(), topic.getClientId(), viewerClientId)) {
            log.warn("Private topic {} not visible to client {}", topic.getId(), viewerClientId);
            throw new ResourceNotFoundException("Topic with id '" + topic.getId() + "' not found.");
        }
    }

    @Override
    public TopicsByProductPackagesPageDto getTopicsByProductPackagePairs(TopicsByProductPackagesRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        TopicFilterRequest filter = mergeProductPackagesFilter(request);
        boolean excludeMatching = Boolean.TRUE.equals(request.getExcludeMatchingPairs());
        List<ProductPackagePairDto> productPackages = request.getProductPackages() != null
                ? request.getProductPackages()
                : List.of();

        var result = topicFilterRepositoryCustom.findByProductPackagePairsWithTotals(
                productPackages, filter, excludeMatching);

        List<TopicMinimalDto> items = mapTopicsToMinimalDtos(result.page().getContent());
        int offset = request.getOffset() != null && request.getOffset() >= 0 ? request.getOffset() : 0;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;

        return TopicsByProductPackagesPageDto.builder()
                .offset(offset)
                .pageSize(pageSize)
                .total(result.assignedTotal())
                .totalLocked(result.lockedTotal())
                .items(items)
                .build();
    }

    @Override
    public TopicCountsByProductPackagesResponseDto countTopicsByProductPackages(
            TopicCountsByProductPackagesRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        LocalDate now = LocalDate.now(ZoneId.of("UTC"));
        int currentYear = now.getYear();
        Instant yearStart = LocalDate.of(currentYear, 1, 1)
                .atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant yearEnd = LocalDate.of(currentYear, 12, 31)
                .atTime(23, 59, 59).atZone(ZoneId.of("UTC")).toInstant();

        Map<Integer, Long> totalByMonth = topicFilterRepositoryCustom.countTopicsByProductPackagePairsByMonth(
                request.getMspProductPackages(), yearStart, yearEnd);
        Map<Integer, Long> usedByMonth = topicFilterRepositoryCustom.countTopicsByProductPackagePairsByMonth(
                request.getClientProductPackages(), yearStart, yearEnd);

        String[] monthNames = {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        List<TopicDistributionItemDto> data = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> TopicDistributionItemDto.builder()
                        .month(monthNames[month - 1])
                        .totalContent(totalByMonth.getOrDefault(month, 0L))
                        .usedContent(usedByMonth.getOrDefault(month, 0L))
                        .build())
                .toList();

        log.info("Built monthly topic distribution for year {} (msp months with data={}, client months with data={})",
                currentYear, totalByMonth.size(), usedByMonth.size());

        return TopicCountsByProductPackagesResponseDto.builder()
                .data(data)
                .build();
    }

    private TopicFilterRequest mergeProductPackagesFilter(TopicsByProductPackagesRequest request) {
        TopicFilterRequest filter = request.getFilter() != null
                ? request.getFilter()
                : new TopicFilterRequest();

        if (filter.getStatus() == null) {
            filter.setStatus(TopicStatus.ENABLED);
        }
        if ((filter.getSearchText() == null || filter.getSearchText().isBlank())
                && request.getSearch() != null && !request.getSearch().isBlank()) {
            filter.setSearchText(request.getSearch());
        }

        // Pagination and sort always come from TopicsByProductPackagesRequest, not from filter
        int offset = request.getOffset() != null && request.getOffset() >= 0 ? request.getOffset() : 0;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
        filter.setPage(offset); // TopicFilterRequest / executeFilterQuery uses 1-based page
        filter.setSize(pageSize);
        filter.setSortBy(request.getSortBy() != null && !request.getSortBy().isBlank()
                ? request.getSortBy()
                : "createdAt");
        filter.setSortDirection(request.getOrder() != null && !request.getOrder().isBlank()
                ? request.getOrder()
                : "DESC");

        return filter;
    }

    private List<TopicMinimalDto> mapTopicsToMinimalDtos(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            return List.of();
        }

        Set<String> contentTypeIds = topics.stream()
                .map(Topic::getContentTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> categoryIds = topics.stream()
                .map(Topic::getCategoryIds)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        Map<String, String> contentTypeMap = contentTypeRepository.findAllById(contentTypeIds).stream()
                .collect(Collectors.toMap(ContentType::getId, ContentType::getTypeName));
        Map<String, String> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategoryName));

        return topics.stream()
                .map(topic -> TopicMinimalDto.builder()
                        .topicId(topic.getId())
                        .topicName(topic.getTopicName())
                        .description(topic.getDescription())
                        .durationMinutes(topic.getDurationMinutes())
                        .thumbnail(topic.getThumbnailUrl())
                        .contentType(topic.getContentTypeId() != null
                                ? contentTypeMap.get(topic.getContentTypeId())
                                : null)
                        .category(topic.getCategoryIds() == null ? null : topic.getCategoryIds().stream()
                                .map(categoryMap::get)
                                .filter(Objects::nonNull)
                                .toList())
                        .build())
                .toList();
    }

    /**
     * Extracts content list from interactive video/content's specific content
     * @param contentDoc The content document from MongoDB
     * @return List of InteractiveContentListItem or empty list if not applicable
     */
    @SuppressWarnings("unchecked")
    private List<ContentStatusDTO.InteractiveContentListItem> extractContentList(Document contentDoc) {
        try {
            // Navigate: specificContent -> contentList
            Document specificContent = (Document) contentDoc.get("specificContent");
            if (specificContent == null) {
                log.debug("No specificContent found for content: {}", contentDoc.getString("_id"));
                return List.of();
            }
            
            List<Document> contentList = (List<Document>) specificContent.get("contentList");
            if (contentList == null || contentList.isEmpty()) {
                Object byLanguage = specificContent.get("interactiveVideoByLanguage");
                if (byLanguage instanceof Document byLanguageMap) {
                    contentList = byLanguageMap.values().stream()
                            .filter(Document.class::isInstance)
                            .map(Document.class::cast)
                            .map(videoDoc -> (List<Document>) videoDoc.get("contentList"))
                            .filter(Objects::nonNull)
                            .flatMap(List::stream)
                            .toList();
                } else if (byLanguage instanceof List<?> byLanguageList) {
                    contentList = byLanguageList.stream()
                            .filter(Document.class::isInstance)
                            .map(Document.class::cast)
                            .map(videoDoc -> (List<Document>) videoDoc.get("contentList"))
                            .filter(Objects::nonNull)
                            .flatMap(List::stream)
                            .toList();
                }
            }
            if (contentList == null || contentList.isEmpty()) {
                log.debug("No contentList found in specificContent for content: {}", contentDoc.getString("_id"));
                return List.of();
            }
            
            log.debug("Extracting {} items from contentList for content: {}", contentList.size(), contentDoc.getString("_id"));
            
            // Extract id and contentName from each contentListItem
            List<ContentStatusDTO.InteractiveContentListItem> extractedItems = contentList.stream()
                    .map(item -> {
                        // MongoDB uses _id field, try both id and _id for compatibility
                        String id = item.getString("id");
                        if (id == null || id.isEmpty()) {
                            id = item.getString("_id");
                        }
                        
                        // Extract contentName from contentBody.contentName or commonContent.contentName
                        Document contentBody = (Document) item.get("contentBody");
                        String contentName = null;
                        if (contentBody != null) {
                            // First try direct contentName at contentBody level
                            contentName = contentBody.getString("contentName");
                            if (contentName == null || contentName.isEmpty()) {
                                // Fallback to contentBody.commonContent.contentName
                                Document commonContent = (Document) contentBody.get("commonContent");
                                if (commonContent != null) {
                                    contentName = commonContent.getString("contentName");
                                }
                            }
                        }
                        
                        return new ContentStatusDTO.InteractiveContentListItem(id, contentName);
                    })
                    .filter(item -> item.getId() != null && !item.getId().isEmpty()) // Filter out items without ID
                    .collect(Collectors.toMap(
                            ContentStatusDTO.InteractiveContentListItem::getId,
                            item -> item,
                            (first, second) -> first,
                            LinkedHashMap::new
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            
            log.debug("Successfully extracted {} items from contentList for content: {}", 
                    extractedItems.size(), contentDoc.getString("_id"));
            return extractedItems;
                    
        } catch (Exception e) {
            log.warn("Error extracting contentList from content {}: {}", 
                    contentDoc.getString("_id"), e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public TopicDetailsResponseDTO getTopicDetails(String topicId, String userId, String subpackageId) {
        log.info("Fetching topic details for userId={}, topicId={}, subpackageId={}", userId, topicId, subpackageId);

        // Get full topic hierarchy using MongoDB aggregation
        Document doc = topicRepositoryCustom.getFullTopicHierarchy(userId, topicId, subpackageId);
        if (doc == null) {
            log.warn("No topic data found for userId={}, topicId={}, subpackageId={}", userId, topicId, subpackageId);
            throw new ResourceNotFoundException("Topic data not found for user " + userId + " and topicId " + topicId);
        }

        // Extract topic metadata
        String topicName = doc.getString("topicName");
        String topicDescription = doc.getString("topicDescription");
        String thumbnailUrl = doc.getString("thumbnailUrl");
        String status = doc.getString("status");
        Boolean isSaved = doc.getBoolean("isSaved");
        Integer durationMinutes = doc.getInteger("durationMinutes");
        List<String> categoryIds = (List<String>) doc.get("categoryIds");
        List<String> countryIds = (List<String>) doc.get("countryIds");
        List<String> complianceIds = (List<String>) doc.get("complianceIds");
        String contentTypeId = doc.getString("contentTypeId");
        Instant publishDate = doc.getDate("publishDate") != null ? doc.getDate("publishDate").toInstant() : null;

        List<Document> chapters = (List<Document>) doc.get("chapters");
        List<Document> contents = (List<Document>) doc.get("contents");

        // Filter content status for this user
        List<Document> contentStatus = ((List<Document>) doc.get("contentStatus")).stream()
                .filter(statusDoc -> userId.equals(statusDoc.getString("userId")))
                .toList();

        // Calculate completed content IDs
        Set<String> completedIds = contentStatus.stream()
                .filter(statusDoc -> Boolean.TRUE.equals(statusDoc.getBoolean("isDone")))
                .map(statusDoc -> statusDoc.getString("contentId"))
                .collect(Collectors.toSet());

        // Create content map for efficient lookup
        Map<String, Document> contentMap = contents.stream()
                .collect(Collectors.toMap(c -> c.getString("_id"), c -> c));

        // Build chapter details with content status
        List<ChapterDetailsDTO> chapterDetails = chapters.stream()
                .map(ch -> {
                    List<String> contentIds = (List<String>) ch.get("contentIds");
                    List<ContentStatusDTO> contentItems = contentIds.stream()
                            .map(cid -> {
                                Document cDoc = contentMap.get(cid);
                                if (cDoc == null) return null;
                                
                                // Extract basic info
                                String contentType = cDoc.get("contentType") != null ? cDoc.get("contentType").toString() : null;
                                ContentStatusDTO contentDTO = new ContentStatusDTO(
                                        cDoc.getString("_id"),
                                        cDoc.getString("contentName"),
                                        contentType,
                                        completedIds.contains(cDoc.getString("_id"))
                                );
                                
                                // For INTERACTIVE_VIDEO and INTERACTIVE_CONTENT, extract contentList
                                if ("INTERACTIVE_VIDEO".equals(contentType) || "INTERACTIVE_CONTENT".equals(contentType)) {
                                    List<ContentStatusDTO.InteractiveContentListItem> contentList = extractContentList(cDoc);
                                    contentDTO.setContentList(contentList);
                                }
                                
                                return contentDTO;
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    return new ChapterDetailsDTO(
                            ch.getString("_id"),
                            ch.getString("chapterName"),
                            ch.getString("chapterDescription"),
                            contentItems
                    );
                })
                // Filter out chapters with empty contents
                .filter(ch -> ch.getContents() != null && !ch.getContents().isEmpty())
                .toList();

        // Calculate progress metrics
        int contentCount = contents.size();
        int chapterCount = chapterDetails.size(); // Use filtered chapter count
        double progress = contentCount == 0 ? 0.0 : (completedIds.size() * 100.0) / contentCount;

        // Certificate URLs are no longer stored in UserTopicProgress
        // Set to default values
        String certLink = "NA";
        String imageCertLink = "NA";

        log.debug("Topic loaded: name='{}', chapters={}, contents={}, progress={}% for userId={}",
                topicName, chapterCount, contentCount, progress, userId);

        // Build and return response
        return new TopicDetailsResponseDTO(
                topicId, topicName, topicDescription, publishDate,
                progress, chapterCount, contentCount, chapterDetails,
                thumbnailUrl, certLink, imageCertLink,
                status, isSaved != null ? isSaved : false, durationMinutes,
                categoryIds, countryIds, complianceIds, contentTypeId
        );
    }

    @Override
    public TopicDistributionResponseDto getTopicDistribution() {
        log.info("Fetching topic distribution for 12 months using optimized aggregation");

        // Get current year - using UTC for consistency
        LocalDate now = LocalDate.now(ZoneId.of("UTC"));
        int currentYear = now.getYear();
        Instant yearStart = LocalDate.of(currentYear, 1, 1)
                .atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant yearEnd = LocalDate.of(currentYear, 12, 31)
                .atTime(23, 59, 59).atZone(ZoneId.of("UTC")).toInstant();

        // Use optimized aggregation-based repository method
        List<TopicDistributionItemDto> distributionData = 
                topicDistributionRepositoryCustom.getTopicDistribution(yearStart, yearEnd);

        log.info("Topic distribution calculated successfully for {} months", distributionData.size());
        
        return TopicDistributionResponseDto.builder()
                .data(distributionData)
                .build();
    }
}
