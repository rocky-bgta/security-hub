package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageDetailsResponseDTO;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.dto.product.ProductWithSinglePackageResponse;
import com.aspire.asat.cms.dto.subPackage.SubPackageAssignedUserDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageDetailResponseDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageRequestDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageResponseDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageUpdateDto;
import com.aspire.asat.cms.dto.subPackage.TrialSubPackageCreationRequestDto;
import com.aspire.asat.cms.dto.subPackage.TrialSubPackageResponseDto;
import com.aspire.asat.cms.dto.registration.AspireUserBasicDto;
import com.aspire.asat.cms.dto.topic.TopicMinimalDto;
import com.aspire.asat.cms.dto.topic.TopicRespDto;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.exception.ResourceUpdateNotAllowedException;
import com.aspire.asat.cms.model.Exams;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.ProductPackageRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.SubPackageRepositoryCustom;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.ProductService;
import com.aspire.asat.cms.service.SubPackageService;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.service.topic.TopicService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubPackageServiceImpl implements SubPackageService {

    private final SubPackageRepository subPackageRepository;
    private final SubPackageRepositoryCustom subPackageRepositoryCustom;
    private final ProductRepository productRepository;
    private final ProductPackageRepository productPackageRepository;
    private final TopicRepository topicRepository;
    private final UserSubPackageRepository userSubPackageRepository;
    private final ExamRepository examRepository;
    private final TopicService topicService;
    private final ProductService productService;
    private final UserCurrentContextService userCurrentContextService;
    private final RegistrationServiceClient registrationServiceClient;
    private static final Logger log = LoggerFactory.getLogger(SubPackageServiceImpl.class);

    public SubPackageServiceImpl(SubPackageRepository subPackageRepository,
                                 SubPackageRepositoryCustom subPackageRepositoryCustom,
                                 ProductRepository productRepository,
                                 ProductPackageRepository productPackageRepository,
                                 TopicRepository topicRepository,
                                 UserSubPackageRepository userSubPackageRepository,
                                 ExamRepository examRepository,
                                 TopicService topicService,
                                 ProductService productService,
                                 UserCurrentContextService userCurrentContextService,
                                 RegistrationServiceClient registrationServiceClient) {
        this.subPackageRepository = subPackageRepository;
        this.subPackageRepositoryCustom = subPackageRepositoryCustom;
        this.productRepository = productRepository;
        this.productPackageRepository = productPackageRepository;
        this.topicRepository = topicRepository;
        this.userSubPackageRepository = userSubPackageRepository;
        this.examRepository = examRepository;
        this.topicService = topicService;
        this.productService = productService;
        this.userCurrentContextService = userCurrentContextService;
        this.registrationServiceClient = registrationServiceClient;
    }

    private SubPackageResponseDto convertToResponseDto(SubPackage subPackage) {

        Product product = productRepository.findById(subPackage.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + subPackage.getProductId()));

        // Count assigned users for this subpackage
        long assignedUserCount = userSubPackageRepository.countBySubPackageId(subPackage.getId());

        return SubPackageResponseDto.builder()
                .id(subPackage.getId())
                .name(subPackage.getName())
                .description(subPackage.getDescription())
                .productId(subPackage.getProductId())
                .productName(product.getProductName())
                .packageId(subPackage.getPackageId())
                .productPackageId(subPackage.getProductPackageId())
                .clientId(subPackage.getClientId())
                .clientAdminId(subPackage.getClientAdminId())
                .topicId(subPackage.getTopicId())
                .createdBy(subPackage.getCreatedBy())
                .status(subPackage.getStatus())
                .assignedFor(subPackage.getAssignedFor())
                .channel(subPackage.getChannel())
                .createdAt(subPackage.getCreatedAt())
                .updatedAt(subPackage.getUpdatedAt())
                .assignedUserCount(assignedUserCount)
                .isTrial(subPackage.getIsTrial())
                .showInSite(subPackage.getShowInSite())
                .isAlreadyAssigned(subPackage.getIsAlreadyAssigned())
                .build();
    }

    /**
     * Optimized method to convert SubPackages to DTOs with batch user count queries
     * to avoid N+1 query problem
     */
    private List<SubPackageResponseDto> convertToResponseDtoWithBatchUserCounts(List<SubPackage> subPackages) {
        if (subPackages.isEmpty()) {
            return List.of();
        }

        // Extract all subpackage IDs for batch user count query
        List<String> subPackageIds = subPackages.stream()
                .map(SubPackage::getId)
                .toList();

        // Batch query to get user counts for all subpackages
        List<UserSubPackage> userSubPackages = userSubPackageRepository.findBySubPackageIdIn(subPackageIds);

        // Create a map of subPackageId -> user count
        Map<String, Long> userCountMap = userSubPackages.stream()
                .collect(Collectors.groupingBy(
                        UserSubPackage::getSubPackageId,
                        Collectors.counting()
                ));

        // Convert each SubPackage to DTO with pre-calculated user count
        return subPackages.stream()
                .map(subPackage -> {
                    Product product = productRepository.findById(subPackage.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + subPackage.getProductId()));

                    // Get user count from map, default to 0 if not found
                    Long assignedUserCount = userCountMap.getOrDefault(subPackage.getId(), 0L);

                    return SubPackageResponseDto.builder()
                            .id(subPackage.getId())
                            .name(subPackage.getName())
                            .description(subPackage.getDescription())
                            .productId(subPackage.getProductId())
                            .productName(product.getProductName())
                            .packageId(subPackage.getPackageId())
                            .productPackageId(subPackage.getProductPackageId())
                            .clientId(subPackage.getClientId())
                            .clientAdminId(subPackage.getClientAdminId())
                            .topicId(subPackage.getTopicId())
                            .createdBy(subPackage.getCreatedBy())
                            .status(subPackage.getStatus())
                            .assignedFor(subPackage.getAssignedFor())
                            .channel(subPackage.getChannel())
                            .createdAt(subPackage.getCreatedAt())
                            .updatedAt(subPackage.getUpdatedAt())
                            .assignedUserCount(assignedUserCount)
                            .isTrial(subPackage.getIsTrial())
                            .showInSite(subPackage.getShowInSite())
                            .isAlreadyAssigned(subPackage.getIsAlreadyAssigned())
                            .build();
                })
                .toList();
    }

    @Override
    public SubPackageResponseDto createSubPackage(SubPackageRequestDto requestDto) {
        log.info("Creating sub-package with name: {} for client: {}", requestDto.getName(), requestDto.getClientId());

        // Validate minimum topic requirement
        if (requestDto.getTopicId() == null || requestDto.getTopicId().size() < 2) {
            throw new IllegalArgumentException("SubPackage must have at least 2 topics. Provided topics: " +
                    (requestDto.getTopicId() != null ? requestDto.getTopicId().size() : 0));
        }

        // Check if sub-package with same name exists for the client
        if (subPackageRepository.existsByNameAndClientId(requestDto.getName(), requestDto.getClientId())) {
            throw new DuplicateNameException("SubPackage already exists with name: " + requestDto.getName() + " for this client");
        }

        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();

        SubPackage subPackage = SubPackage.builder()
                .id(id)
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .productId(requestDto.getProductId())
                .packageId(requestDto.getPackageId())
                .productPackageId(requestDto.getProductPackageId())
                .clientId(requestDto.getClientId())
                .clientAdminId(requestDto.getClientAdminId())
                .topicId(requestDto.getTopicId())
                .createdBy(requestDto.getCreatedBy())
                .status(requestDto.getStatus())
                .assignedFor(requestDto.getAssignedFor() != null ? requestDto.getAssignedFor().getValue() : null)
                .channel(requestDto.getChannel())
                .deleted(false)
                .isTrial(requestDto.getIsTrial() != null ? requestDto.getIsTrial() : false)
                .showInSite(requestDto.getShowInSite() != null ? requestDto.getShowInSite() : false)
                .createdAt(now)
                .updatedAt(now)
                .isAlreadyAssigned(false)
                .isPhishingSubpackage(requestDto.getIsPhishingSubpackage())
                .build();

        SubPackage savedSubPackage = subPackageRepository.save(subPackage);

        log.info("SubPackage created successfully with ID: {}", id);
        return convertToResponseDto(savedSubPackage);
    }

    @Override
    public SubPackageResponseDto getSubPackageById(String id) {
        log.info("Fetching sub-package with ID: {}", id);

        return subPackageRepository.findById(id)
                .map(this::convertToResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("SubPackage not found with id: " + id));
    }

    @Override
    public SubPackageDetailResponseDto getSubPackageByIdWithDetails(String id) {
        log.info("Fetching sub-package with details for ID: {}", id);

        SubPackage subPackage = getSubPackage(id);

        // Get product details using Product model
        Object productDetails = productRepository.findById(subPackage.getProductId())
                .orElse(null);

        // Get package details using ProductPackage model
        Object packageDetails = productPackageRepository.findById(subPackage.getPackageId())
                .orElse(null);

        // Get topic details
        List<TopicRespDto> topicDetails = topicRepository.findAllById(subPackage.getTopicId())
                .stream()
                .map(Topic::toTopicRespDto)
                .toList();

        // Count assigned users for this subpackage
        long assignedUserCount = userSubPackageRepository.countBySubPackageId(subPackage.getId());

        return SubPackageDetailResponseDto.builder()
                .id(subPackage.getId())
                .name(subPackage.getName())
                .description(subPackage.getDescription())
                .productId(subPackage.getProductId())
                .packageId(subPackage.getPackageId())
                .productPackageId(subPackage.getProductPackageId())
                .clientId(subPackage.getClientId())
                .clientAdminId(subPackage.getClientAdminId())
                .createdBy(subPackage.getCreatedBy())
                .status(subPackage.getStatus())
                .assignedFor(subPackage.getAssignedFor())
                .channel(subPackage.getChannel())
                .createdAt(subPackage.getCreatedAt())
                .updatedAt(subPackage.getUpdatedAt())
                .assignedUserCount(assignedUserCount)
                .isTrial(subPackage.getIsTrial())
                .showInSite(subPackage.getShowInSite())
                .productDetails(productDetails)
                .packageDetails(packageDetails)
                .topicDetails(topicDetails)
                .isAlreadyAssigned(subPackage.getIsAlreadyAssigned())
                .build();
    }

    @Override
    public List<SubPackageResponseDto> getAllSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            List<String> clientAdminIds,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    ) {
        log.info("Fetching SubPackages with filters - search: {}, status: {}, clientId: {}, productId: {}, clientAdminIds: {}, offset: {}, pageSize: {}",
                search, status, clientId, productId, clientAdminIds, offset, pageSize);

        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));

        Page<SubPackage> pageSubPackages = subPackageRepositoryCustom.findSubPackagesWithFilters(
                search, status, clientId, productId, clientAdminIds, pageable
        );

        // Optimize user count queries by batching
        return convertToResponseDtoWithBatchUserCounts(pageSubPackages.getContent());
    }

    @Override
    public long getSubPackageCountWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            List<String> clientAdminIds
    ) {
        log.info("Counting SubPackages with filters - search: {}, status: {}, clientId: {}, productId: {}, clientAdminIds: {}",
                search, status, clientId, productId, clientAdminIds);

        return subPackageRepositoryCustom.countSubPackagesWithFilters(
                search, status, clientId, productId, clientAdminIds);
    }

    @Override
    public SubPackageResponseDto updateSubPackage(String id, SubPackageUpdateDto updateDto) {
        log.info("Updating sub-package with ID: {}", id);

        SubPackage existingSubPackage = getSubPackage(id);

        // Checking if the subpackage already has assigned users
        if (existingSubPackage.getIsAlreadyAssigned() != null && existingSubPackage.getIsAlreadyAssigned()) {
            throw new ResourceUpdateNotAllowedException("This sub-package has already been assigned to the user. Updates are not allowed.: "+ id);
        }

        // Check for name conflict if the name is being updated
        String newName = updateDto.getName();
        if (newName != null && !existingSubPackage.getName().equals(newName)) {
            boolean nameExists = subPackageRepository.existsByNameAndClientIdAndIdNot(
                    newName, existingSubPackage.getClientId(), id);
            if (nameExists) {
                throw new DuplicateNameException("SubPackage already exists with name: " + newName + " for this client");
            }
            existingSubPackage.setName(newName);
        }

        // Update description if provided
        if (updateDto.getDescription() != null) {
            existingSubPackage.setDescription(updateDto.getDescription());
        }

        // Update topics if provided and validate minimum requirement
        if (updateDto.getTopicId() != null) {
            if (updateDto.getTopicId().size() < 2) {
                throw new IllegalArgumentException("SubPackage must have at least 2 topics. Provided topics: " + updateDto.getTopicId().size());
            }
            existingSubPackage.setTopicId(updateDto.getTopicId());
        }

        // Update status if provided
        if (updateDto.getStatus() != null) {
            existingSubPackage.setStatus(updateDto.getStatus());
        }

        // Update assignment target if provided
        if (updateDto.getAssignedFor() != null) {
            existingSubPackage.setAssignedFor(updateDto.getAssignedFor().getValue());
        }

        if (updateDto.getChannel() != null) {
            existingSubPackage.setChannel(updateDto.getChannel());
        }

        // Update client admin ID if provided
        if (updateDto.getClientAdminId() != null) {
            existingSubPackage.setClientAdminId(updateDto.getClientAdminId());
        }

        // Update isTrial if provided
        if (updateDto.getIsTrial() != null) {
            existingSubPackage.setIsTrial(updateDto.getIsTrial());
        }

        // Update showInSite if provided
        if (updateDto.getShowInSite() != null) {
            existingSubPackage.setShowInSite(updateDto.getShowInSite());
        }

        existingSubPackage.setUpdatedAt(Instant.now());

        SubPackage updatedSubPackage = subPackageRepository.save(existingSubPackage);
        log.info("SubPackage updated successfully with ID: {}", id);
        return convertToResponseDto(updatedSubPackage);
    }

    @Override
    public SubPackageResponseDto deleteSubPackage(String id) {
        log.info("Soft deleting sub-package with ID: {}", id);

        SubPackage subPackage = getSubPackage(id);

        subPackage.setStatus(SubPackageStatus.INACTIVE);
        subPackage.setDeleted(true);
        subPackage.setUpdatedAt(Instant.now());

        SubPackage deletedSubPackage = subPackageRepository.save(subPackage);
        log.info("SubPackage soft deleted successfully with ID: {}", id);
        return convertToResponseDto(deletedSubPackage);
    }

    private SubPackage getSubPackage(String id) {
        return subPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubPackage not found with id: " + id));
    }

    @Override
    public UserSubPackageDetailsResponseDTO getUserSubPackageDetails(String userId, String subPackageId) {
        log.info("Fetching user subpackage details for userId: {} and subPackageId: {}", userId, subPackageId);

        UserSubPackage userSubPackage = userSubPackageRepository.findByUserIdAndSubPackageId(userId, subPackageId)
                .orElseThrow(() -> {
                    log.warn("User subpackage not found for userId: {} and subPackageId: {}", userId, subPackageId);
                    return new ResourceNotFoundException("User subpackage not found for userId: " + userId + " and subPackageId: " + subPackageId);
                });

        log.debug("User subpackage found: id={}, status={}, progress={}%",
                userSubPackage.getId(), userSubPackage.getStatus(), userSubPackage.getProgress());

        // Get latest exam (by createdAt) from Exams table for display/resume
        Optional<Exams> examOpt = examRepository.findFirstByUserIdAndSubPackageIdOrderByCreatedAtDesc(userId, subPackageId);

        UserSubPackageDetailsResponseDTO.UserSubPackageDetailsResponseDTOBuilder builder = UserSubPackageDetailsResponseDTO.builder()
                .id(userSubPackage.getId())
                .userId(userSubPackage.getUserId())
                .subPackageId(userSubPackage.getSubPackageId())
                .subPackageName(userSubPackage.getSubPackageName())
                .status(userSubPackage.getStatus())
                .validity(userSubPackage.getValidity())
                .assignedDate(userSubPackage.getAssignedDate())
                .expiryDate(userSubPackage.getExpiryDate())
                .completedTopicIds(userSubPackage.getCompletedTopicIds())
                .certificateLink(userSubPackage.getCertificateLink())
                .imageCertificateLink(userSubPackage.getImageCertificateLink())
                .progress(userSubPackage.getProgress())
                .lastSynced(userSubPackage.getLastSynced());

        // Set exam data from Exams table if exists, otherwise use defaults
        if (examOpt.isPresent()) {
            Exams exam = examOpt.get();
            builder.examCompleted(exam.isExamCompleted())
                   .examScore(exam.getExamScore())
                   .examPassed(exam.isExamPassed())
                   .correctAnswers(exam.getCorrectAnswers())
                   .incorrectAnswers(exam.getIncorrectAnswers())
                   .examCompletedAt(exam.getExamCompletedAt())
                   .examAttempts(exam.getExamAttempts());
        } else {
            builder.examCompleted(false)
                   .examScore(0.0)
                   .examPassed(false)
                   .correctAnswers(0)
                   .incorrectAnswers(0)
                   .examCompletedAt(null)
                   .examAttempts(0);
        }

        return builder.build();
    }

    @Override
    public List<TrialSubPackageResponseDto> getAllTrialSubPackages(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    ) {
        log.info("Fetching Trial SubPackages with filters - search: {}, status: {}, clientId: {}, productId: {}, offset: {}, pageSize: {}",
                search, status, clientId, productId, offset, pageSize);

        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));

        // Use repository method that filters by isTrial = true at database level
        Page<SubPackage> pageSubPackages = subPackageRepositoryCustom.findTrialSubPackagesWithFilters(
                search, status, clientId, productId, pageable
        );

        // Convert to TrialSubPackageResponseDto with batch user counts
        List<SubPackage> trialSubPackages = pageSubPackages.getContent();
        return convertTrialSubPackagesToResponseDto(trialSubPackages);
    }

    @Override
    public long getTrialSubPackageCount(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId
    ) {
        log.info("Counting Trial SubPackages with filters - search: {}, status: {}, clientId: {}, productId: {}",
                search, status, clientId, productId);

        // Use repository method that counts only trial packages (isTrial = true)
        return subPackageRepositoryCustom.countTrialSubPackagesWithFilters(search, status, clientId, productId);
    }

    /**
     * Convert Trial SubPackages to TrialSubPackageResponseDto with batch user count queries
     */
    private List<TrialSubPackageResponseDto> convertTrialSubPackagesToResponseDto(List<SubPackage> trialSubPackages) {
        if (trialSubPackages.isEmpty()) {
            return List.of();
        }

        // Extract all subpackage IDs for batch user count query
        List<String> subPackageIds = trialSubPackages.stream()
                .map(SubPackage::getId)
                .toList();

        // Batch query to get user counts for all subpackages
        List<UserSubPackage> userSubPackages = userSubPackageRepository.findBySubPackageIdIn(subPackageIds);
        
        // Create a map of subPackageId -> user count
        Map<String, Long> userCountMap = userSubPackages.stream()
                .collect(Collectors.groupingBy(
                    UserSubPackage::getSubPackageId,
                    Collectors.counting()
                ));

        // Convert each SubPackage to TrialSubPackageResponseDto with pre-calculated user count
        return trialSubPackages.stream()
                .map(subPackage -> {
                    Product product = productRepository.findById(subPackage.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + subPackage.getProductId()));

                    // Get user count from map, default to 0 if not found
                    Long assignedUserCount = userCountMap.getOrDefault(subPackage.getId(), 0L);

                    return TrialSubPackageResponseDto.builder()
                            .id(subPackage.getId())
                            .name(subPackage.getName())
                            .description(subPackage.getDescription())
                            .productId(subPackage.getProductId())
                            .productName(product.getProductName())
                            .packageId(subPackage.getPackageId())
                            .productPackageId(subPackage.getProductPackageId())
                            .clientId(subPackage.getClientId())
                            .clientAdminId(subPackage.getClientAdminId())
                            .topicId(subPackage.getTopicId())
                            .createdBy(subPackage.getCreatedBy())
                            .status(subPackage.getStatus())
                            .channel(subPackage.getChannel())
                            .createdAt(subPackage.getCreatedAt())
                            .updatedAt(subPackage.getUpdatedAt())
                            .assignedUserCount(assignedUserCount)
                            .isTrial(subPackage.getIsTrial())
                            .showInSite(subPackage.getShowInSite())
                            .build();
                })
                .toList();
    }

    @Override
    public boolean isSubPackageNameExistsForClient(String subPackageName, String clientAdminId) {
        if (!StringUtils.hasText(subPackageName)) {
            return false;
        }

        String resolvedClientAdminId = clientAdminId;
        if (!StringUtils.hasText(resolvedClientAdminId)) {
            resolvedClientAdminId = userCurrentContextService.getCurrentUserContext().getUserId();
        }

        if (!StringUtils.hasText(resolvedClientAdminId)) {
            return false;
        }

        return subPackageRepository.existsByNameAndClientId(subPackageName.trim(), resolvedClientAdminId);
    }

    @Override
    public SubPackageResponseDto createTrialSubPackage(TrialSubPackageCreationRequestDto requestDto) {
        log.info("Creating trial sub-package for productId: {}, packageId: {}, clientAdminId: {}, trialPeriodDays: {}",
                requestDto.getProductId(), requestDto.getPackageId(), requestDto.getClientAdminId(), requestDto.getTrialPeriodDays());

        // 1. Get topics for this product and package
        Page<TopicMinimalDto> topicsPage = topicService.getTopicsByProductAndPackage(
                requestDto.getProductId(),
                requestDto.getPackageId(),
                null, // no search
                0,    // offset
                1000, // large page size to get all topics
                "createdAt",
                "desc"
        );

        List<String> topicIds = topicsPage.getContent().stream()
                .map(TopicMinimalDto::getTopicId)
                .filter(topicId -> topicId != null && !topicId.trim().isEmpty())
                .collect(Collectors.toList());

        if (topicIds.size() < 2) {
            throw new IllegalArgumentException("SubPackage must have at least 2 topics. Found: " + topicIds.size() +
                    " for productId: " + requestDto.getProductId() + ", packageId: " + requestDto.getPackageId());
        }

        log.info("Found {} topics for productId: {}, packageId: {}", topicIds.size(), requestDto.getProductId(), requestDto.getPackageId());

        // 2. Get product and package details for naming
        ProductWithSinglePackageResponse productPackageDetails =
                productService.getProductPackageDetails(requestDto.getProductId(), requestDto.getPackageId());

        String packageName = "A-SAT Trial";
        if (productPackageDetails != null && productPackageDetails.getProductName() != null
                && !productPackageDetails.getProductName().isBlank()
                && !"Security Awareness Training".equalsIgnoreCase(productPackageDetails.getProductName().trim())) {
            packageName = productPackageDetails.getProductName().trim() + " Trial";
        }

        // 3. Build subPackage name: "{OriginalPackageName}-Trial-{TrialPeriod}Days"
//        String subPackageName = String.format("%s-Trial-%dDays", packageName, requestDto.getTrialPeriodDays());
        String subPackageName = packageName;
        String subPackageDescription = String.format("Trial subpackage for %s with %d days validity",
                packageName, requestDto.getTrialPeriodDays());

        // 4. Check if sub-package with same name exists for the client
        if (subPackageRepository.existsByNameAndClientId(subPackageName, requestDto.getClientAdminId())) {
            log.warn("Trial subPackage already exists with name: {} for client: {}. Returning existing one.", 
                    subPackageName, requestDto.getClientAdminId());
            // Return existing subPackage instead of throwing error
            SubPackage existing = subPackageRepository.findByNameAndClientId(subPackageName, requestDto.getClientAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("SubPackage not found"));
            return convertToResponseDto(existing);
        }

        // 5. Create subPackage
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();

        SubPackage subPackage = SubPackage.builder()
                .id(id)
                .name(subPackageName)
                .description(subPackageDescription)
                .productId(requestDto.getProductId())
                .packageId(requestDto.getPackageId())
                .productPackageId(requestDto.getProductPackageId())
                .clientId(requestDto.getClientAdminId()) // Use clientAdminId as clientId
                .clientAdminId(requestDto.getClientAdminId())
                .topicId(topicIds)
                .createdBy("TRIAL_SIGNUP")
                .status(SubPackageStatus.ACTIVE)
                .deleted(false)
                .isTrial(true)
                .showInSite(false)
                .createdAt(now)
                .updatedAt(now)
                .isAlreadyAssigned(false)
                .build();

        SubPackage savedSubPackage = subPackageRepository.save(subPackage);

        log.info("Trial subPackage created successfully with ID: {}, name: {}", id, subPackageName);
        return convertToResponseDto(savedSubPackage);
    }

    @Override
    public List<SubPackageResponseDto> getSubPackagesByPackageIds(List<String> packageIds, String clientAdminId) {
        if (packageIds == null || packageIds.isEmpty()) {
            return List.of();
        }

        List<String> distinctPackageIds = packageIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        if (distinctPackageIds.isEmpty()) {
            return List.of();
        }

        List<SubPackage> subPackages = subPackageRepository.findByPackageIdInAndDeletedFalse(distinctPackageIds);
        Map<String, SubPackage> selectedByPackageId = new java.util.LinkedHashMap<>();

        for (SubPackage subPackage : subPackages) {
            if (!StringUtils.hasText(subPackage.getPackageId())) {
                continue;
            }
            SubPackage existing = selectedByPackageId.get(subPackage.getPackageId());
            if (existing == null) {
                selectedByPackageId.put(subPackage.getPackageId(), subPackage);
                continue;
            }
            if (StringUtils.hasText(clientAdminId)
                    && clientAdminId.equals(subPackage.getClientAdminId())
                    && !clientAdminId.equals(existing.getClientAdminId())) {
                selectedByPackageId.put(subPackage.getPackageId(), subPackage);
            }
        }

        return selectedByPackageId.values().stream()
                .map(subPackage -> SubPackageResponseDto.builder()
                        .id(subPackage.getId())
                        .name(subPackage.getName())
                        .packageId(subPackage.getPackageId())
                        .productId(subPackage.getProductId())
                        .clientAdminId(subPackage.getClientAdminId())
                        .channel(subPackage.getChannel())
                        .build())
                .toList();
    }

    @Override
    public Page<SubPackageAssignedUserDto> getAssignedUsersBySubPackageId(String subPackageId, int offset, int pageSize) {
        if (!StringUtils.hasText(subPackageId)) {
            throw new IllegalArgumentException("subPackageId is required");
        }

        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.DESC, "assignedDate"));
        Page<UserSubPackage> assignments = userSubPackageRepository.findBySubPackageId(subPackageId.trim(), pageable);

        List<String> userIds = assignments.getContent().stream()
                .map(UserSubPackage::getUserId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        Map<String, AspireUserBasicDto> usersById = new HashMap<>();
        for (AspireUserBasicDto user : registrationServiceClient.getUsersByIds(userIds)) {
            if (user.getUserId() != null) {
                usersById.put(user.getUserId(), user);
            }
        }

        List<SubPackageAssignedUserDto> items = assignments.getContent().stream()
                .map(assignment -> {
                    AspireUserBasicDto user = usersById.get(assignment.getUserId());
                    return SubPackageAssignedUserDto.builder()
                            .userId(assignment.getUserId())
                            .email(user != null ? user.getEmail() : null)
                            .fullName(user != null ? user.getFullName() : null)
                            .department(user != null ? user.getDepartment() : null)
                            .riskGroup(user != null ? user.getRiskGroup() : null)
                            .subPackageName(assignment.getSubPackageName())
                            .status(assignment.getStatus())
                            .assignedAt(assignment.getAssignedDate())
                            .expiryDate(assignment.getExpiryDate())
                            .progress(assignment.getProgress())
                            .build();
                })
                .toList();

        return new PageImpl<>(items, pageable, assignments.getTotalElements());
    }
}
