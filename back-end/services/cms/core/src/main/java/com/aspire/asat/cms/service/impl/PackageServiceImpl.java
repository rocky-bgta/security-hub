package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.bundle.BundleDto;
import com.aspire.asat.cms.dto.clientAdmin.AssignedPackageOverviewDTO;
import com.aspire.asat.cms.dto.clientAdmin.AssignedPackageOverviewWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.AvailablePackageDTO;
import com.aspire.asat.cms.dto.clientAdmin.AvailablePackageWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.ClientProductDTO;
import com.aspire.asat.cms.dto.clientAdmin.ClientProductIdListResponseDto;
import com.aspire.asat.cms.dto.clientAdmin.ClientProductOverviewResponseDTO;
import com.aspire.asat.cms.dto.clientAdmin.ClientProductOverviewWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.LicenseHistoryDTO;
import com.aspire.asat.cms.dto.clientAdmin.LicenseHistoryWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.PackagePerformanceDTO;
import com.aspire.asat.cms.dto.clientAdmin.PackagePerformanceWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.ProductAnalyticsDTO;
import com.aspire.asat.cms.dto.course.CourseResponseDto;
import com.aspire.asat.cms.dto.enums.CourseStatus;
import com.aspire.asat.cms.dto.enums.FeatureStatus;
import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.dto.exam.PackageExamCreateDto;
import com.aspire.asat.cms.dto.packageDto.BundleWithFeatureDto;
import com.aspire.asat.cms.dto.packageDto.FeatureResponseDto;
import com.aspire.asat.cms.dto.packageDto.RequestDto;
import com.aspire.asat.cms.dto.packageDto.ResponseDto;
import com.aspire.asat.cms.dto.packageDto.ResponseDtoWithCourseFeatureAndBundleFeatureDetails;
import com.aspire.asat.cms.dto.packageDto.ResponseDtoWithCourseFeatureDetails;
import com.aspire.asat.cms.dto.packageDto.UpdateRequestDto;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.NullException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Bundle;
import com.aspire.asat.cms.model.Course;
import com.aspire.asat.cms.model.Feature;
import com.aspire.asat.cms.model.Package;
import com.aspire.asat.cms.model.Exams;
import com.aspire.asat.cms.repository.BundleRepository;
import com.aspire.asat.cms.repository.CourseRepository;
import com.aspire.asat.cms.repository.FeatureRepository;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.PackageRepository;
import com.aspire.asat.cms.repository.custom.BundleRepositoryCustom;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import com.aspire.asat.cms.service.PackageService;
import com.aspire.asat.cms.util.AzureCertificateUploader;
import com.aspire.asat.cms.util.ExcelGeneratorUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PackageServiceImpl implements PackageService {

    @Value("${service.registration.url}")
    private String registrationUrl;

    private final PackageRepository packageRepository;
    private final CourseRepository courseRepository;
    private final FeatureRepository featureRepository;
    private final BundleRepository bundleRepository;
    private final UserPackageRepositoryCustom userPackageRepositoryCustom;
    private final BundleRepositoryCustom bundleRepositoryCustom;
    private final ExamRepository examRepository;

    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final AzureCertificateUploader azureCertificateUploader;
    
    public PackageServiceImpl(PackageRepository packageRepository, CourseRepository courseRepository, FeatureRepository featureRepository, BundleRepository bundleRepository, UserPackageRepositoryCustom userPackageRepositoryCustom, BundleRepositoryCustom bundleRepositoryCustom, ExamRepository examRepository, ObjectMapper objectMapper, WebClient webClient, AzureCertificateUploader azureCertificateUploader) {
        this.packageRepository = packageRepository;
        this.courseRepository = courseRepository;
        this.featureRepository = featureRepository;
        this.bundleRepository = bundleRepository;
        this.userPackageRepositoryCustom = userPackageRepositoryCustom;
        this.bundleRepositoryCustom = bundleRepositoryCustom;
        this.examRepository = examRepository;
        this.objectMapper = objectMapper;
        this.webClient = webClient;
        this.azureCertificateUploader = azureCertificateUploader;
    }

    @Override
    @Transactional
    public ResponseDto savePackage(RequestDto requestDto) {

        // Validate the package name
        if (requestDto.getPackageName() == null || requestDto.getPackageName().isEmpty()) {
            throw new NullException("Package name cannot be null or empty");
        }

        checkUniquePackageName(requestDto.getPackageName());

        // Generate a new UUID and current timestamp for the package
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        // Convert the RequestDto into a Package entity
        Package packageEntity = Package.toPackage(id.toString(), requestDto, now, now);

        // Initialize the list for bundleIds
        List<String> savedBundleIds = new ArrayList<>();

        // Save Bundles first in batch
        List<BundleDto> bundleDtos = requestDto.getBundles(); // List of BundleDto
        if (bundleDtos != null && !bundleDtos.isEmpty()) {
            // Convert BundleDto to Bundle entity before saving to MongoDB
            List<Bundle> bundles = bundleDtos.stream()
                    .map(bundleDto -> {
                        Bundle bundle = Bundle.builder()
                                .id(UUID.randomUUID().toString())  // Mapping BundleDto fields
                                .bundleName(bundleDto.getBundleName())
                                .packageId(packageEntity.getId())  // Assigning the packageId
                                .featureIds(bundleDto.getFeatureIds())
                                .price(bundleDto.getPrice())
                                .createdAt(now)  // Setting createdAt
                                .bundleStatus(bundleDto.getBundleStatus())
                                .build();
                        return bundle;
                    })
                    .collect(Collectors.toList());

            // Save all bundles in batch to MongoDB
            List<Bundle> savedBundles = bundleRepository.saveAll(bundles); // MongoDB batch save

            // Extract the bundleIds from saved bundles
            savedBundleIds = savedBundles.stream()
                    .map(Bundle::getId)  // Extract the bundleId from each saved Bundle
                    .collect(Collectors.toList());

            // Set the bundleIds to the Package entity
            packageEntity.setBundlesIds(savedBundleIds);
        }

        // Save the package after bundles are saved
        Package savedPackage = packageRepository.save(packageEntity);

        // Convert the saved package entity to ResponseDto
        ResponseDto responseDto = Package.toPackageDto(savedPackage);

        // Set the list of bundleIds in the response DTO
        responseDto.setBundlesIds(savedBundleIds);

        // Save exam if provided
        PackageExamCreateDto examDto = requestDto.getExam();
        if (examDto != null) {
            Exams exam = new Exams();
            exam.setExamId(UUID.randomUUID().toString());  // optional: use packageId
            exam.setSubPackageId(savedPackage.getId());
            exam.setTitle(examDto.getTitle());
            exam.setPassingScore(examDto.getPassingScore());
            exam.setExamDetails(examDto.getExamDetails());
            examRepository.save(exam);
        }


        return responseDto;
    }

    @Override
    public List<ResponseDto> getAllPackages(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));
        Page<Package> pagePackage;
        if (search != null && !search.isEmpty()) {
            pagePackage = packageRepository.findByPackageName(search, pageable);
        } else {
            pagePackage = packageRepository.findAll(pageable);
        }
        return pagePackage.getContent()
                .stream()
                .map(Package::toPackageDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseDtoWithCourseFeatureDetails> getAllPackagesNew(
            String search,
            PackageStatus status,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    ) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));

        Page<Package> pagePackage;
        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasStatus = status != null;

        if (hasSearch && hasStatus) {
            pagePackage = packageRepository.findByPackageNameAndPackageStatus(search, status, pageable);
        } else if (hasSearch) {
            pagePackage = packageRepository.findByPackageName(search, pageable);
        } else if (hasStatus) {
            pagePackage = packageRepository.findByPackageStatus(status, pageable);
        } else {
            pagePackage = packageRepository.findAll(pageable);
        }

        List<Package> packages = pagePackage.getContent();

        // Collect all related IDs
        Set<String> allCourseIds = new HashSet<>();
        Set<String> allFeatureIds = new HashSet<>();
        Set<String> allBundleIds = new HashSet<>();

        for (Package pkg : packages) {
            if (pkg.getCourseIds() != null) allCourseIds.addAll(pkg.getCourseIds());
            if (pkg.getFeatureIds() != null) allFeatureIds.addAll(pkg.getFeatureIds());
            if (pkg.getBundlesIds() != null) allBundleIds.addAll(pkg.getBundlesIds());
        }

        // Fetch related models
        Map<String, CourseResponseDto> courseMap = courseRepository.findAllById(allCourseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::toCourseDto));

        Map<String, com.aspire.asat.cms.dto.featureDto.ResponseDto> featureMap = featureRepository.findAllById(allFeatureIds).stream()
                .collect(Collectors.toMap(Feature::getId, Feature::toFeatureDto));

        Map<String, BundleDto> bundleMap = bundleRepository.findAllById(allBundleIds).stream()
                .collect(Collectors.toMap(Bundle::getId, Bundle::toDto));

        // Enrich package response
        return packages.stream()
                .map(pkg -> ResponseDtoWithCourseFeatureDetails.builder()
                        .id(pkg.getId())
                        .packageName(pkg.getPackageName())
                        .packageDescription(pkg.getPackageDescription())
                        .price(pkg.getPrice())
                        .packageStatus(pkg.getPackageStatus())
                        .courseIds(pkg.getCourseIds() != null ? pkg.getCourseIds().stream()
                                .map(courseMap::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()) : null)
                        .featureIds(pkg.getFeatureIds() != null ? pkg.getFeatureIds().stream()
                                .map(featureMap::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()) : null)
                        .thumbnailUrl(pkg.getThumbnailUrl())
                        .createdAt(pkg.getCreatedAt())
                        .updatedAt(pkg.getUpdatedAt())
                        .lastModifiedBy(pkg.getLastModifiedBy())
                        .bundlesIds(pkg.getBundlesIds() != null ? pkg.getBundlesIds().stream()
                                .map(bundleMap::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ResponseDto getPackageDetailsById(String id) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        return ResponseDto.builder()
                .id(pkg.getId())
                .packageName(pkg.getPackageName())
                .packageDescription(pkg.getPackageDescription())
                .price(pkg.getPrice())
                .packageStatus(pkg.getPackageStatus())
                .courseIds(pkg.getCourseIds()) // Optional: remove if not needed
                .featureIds(pkg.getFeatureIds()) // Optional: remove if not needed
                .thumbnailUrl(pkg.getThumbnailUrl())
                .createdAt(pkg.getCreatedAt())
                .updatedAt(pkg.getUpdatedAt())
                .lastModifiedBy(pkg.getLastModifiedBy())
                .bundlesIds(pkg.getBundlesIds())
                .build();
    }


    @Override
    public ResponseDtoWithCourseFeatureAndBundleFeatureDetails getPackageById(String id) {
        // 1. Fetch the package
        Package packages = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));

        // 2. Fetch course details (filtered to ENABLED only)
        List<CourseResponseDto> courseDetails = courseRepository.findAllById(packages.getCourseIds())
                .stream()
                .filter(course -> course.getCourseStatus() == CourseStatus.ENABLED)
                .map(course -> CourseResponseDto.builder()
                        .id(course.getId())
                        .courseName(course.getCourseName())
                        .courseDescription(course.getCourseDescription())
                        .courseStatus(course.getCourseStatus())
                        .chapterIds(course.getChapterIds())
                        .productIds(course.getProductIds())
                        .createdAt(course.getCreatedAt())
                        .updatedAt(course.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        // 3. Fetch bundle details for the package
        List<BundleWithFeatureDto> enrichedBundles = bundleRepository.findByPackageId(id)
                .stream()
                .map(bundle -> {
                    // For each bundle, fetch and enrich the feature details
                    List<FeatureResponseDto> features = featureRepository.findAllById(bundle.getFeatureIds())
                            .stream()
                            .filter(feature -> feature.getFeatureStatus() == FeatureStatus.ENABLED)
                            .map(feature -> FeatureResponseDto.builder()
                                    .id(feature.getId())
                                    .featureName(feature.getFeatureName())
                                    .featureDescription(feature.getFeatureDescription())
                                    .featureStatus(feature.getFeatureStatus())
                                    .packageIds(feature.getPackageIds())
                                    .availability(feature.getAvailability())
                                    .thumbnailUrl(feature.getThumbnailUrl())
                                    .createdAt(feature.getCreatedAt())
                                    .updatedAt(feature.getUpdatedAt())
                                    .lastModifiedBy(feature.getLastModifiedBy())
                                    .build())
                            .collect(Collectors.toList());

                    return BundleWithFeatureDto.builder()
                            .id(bundle.getId())
                            .bundleName(bundle.getBundleName())
                            .packageId(bundle.getPackageId())
                            .price(bundle.getPrice())
                            .createdAt(bundle.getCreatedAt())
                            .bundleStatus(bundle.getBundleStatus())
                            .features(features)
                            .build();
                }).collect(Collectors.toList());

        // 4. Return final DTO
        return ResponseDtoWithCourseFeatureAndBundleFeatureDetails.builder()
                .id(packages.getId())
                .packageName(packages.getPackageName())
                .packageDescription(packages.getPackageDescription())
                .price(packages.getPrice())
                .packageStatus(packages.getPackageStatus())
                .courseIds(courseDetails)
                .thumbnailUrl(packages.getThumbnailUrl())
                .createdAt(packages.getCreatedAt())
                .updatedAt(packages.getUpdatedAt())
                .lastModifiedBy(packages.getLastModifiedBy())
                .bundlesIds(enrichedBundles)
                .build();
    }

    public ClientProductIdListResponseDto getProductIdsByClient(String clientAdminId) {
        String url = registrationUrl + "/client/admin/products?clientAdminId=" + clientAdminId;

        JsonNode response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || response.get("data") == null) {
            throw new RuntimeException("No data field in registration response");
        }

        JsonNode dataNode = response.get("data");
        JsonNode clientIdsNode = dataNode.get("clientIds");
        JsonNode clientProductsNode = dataNode.get("clientProductDTOS");

        if (clientIdsNode == null || !clientIdsNode.isArray()) {
            throw new RuntimeException("Missing or invalid clientIds in registration response");
        }

        if (clientProductsNode == null || !clientProductsNode.isArray()) {
            throw new RuntimeException("Missing or invalid clientProductDTOS in registration response");
        }

        try {
            List<String> clientIds = objectMapper.treeToValue(clientIdsNode, new TypeReference<List<String>>() {});
            List<ClientProductDTO> clientProductDTOS = objectMapper
                    .readerFor(new TypeReference<List<ClientProductDTO>>() {})
                    .readValue(clientProductsNode); // this can throw IOException

            return ClientProductIdListResponseDto.builder()
                    .clientIds(clientIds)
                    .clientProductDTOS(clientProductDTOS)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse clientIds or clientProductDTOS from registration response", e);
        }

    }
    @Override
    public ClientProductOverviewWrapperDTO getClientProducts(String clientAdminId, String search, String category) {
        // Step 1: Call Registration module to get all client products assigned to this client admin
        ClientProductIdListResponseDto clientProductIdListResponseDto = getProductIdsByClient(clientAdminId);

        List<ClientProductDTO> clientProductDTOS = clientProductIdListResponseDto.getClientProductDTOS();
        List<String> clientIds = clientProductIdListResponseDto.getClientIds();

        if (clientProductDTOS == null || clientProductDTOS.isEmpty()) {
            return ClientProductOverviewWrapperDTO.builder()
                    .availableProducts(0)
                    .products(List.of())
                    .build();
        }

        // Step 2: Extract productIds from clientProductDTOS
        List<String> productIds = clientProductDTOS.stream()
                .map(ClientProductDTO::getProductId)  // Using productId field
                .distinct()
                .toList();

        // Step 3: Call custom repo to get stats
        List<Document> productStats = userPackageRepositoryCustom.getClientProductStatsByPackageIds(productIds, clientIds, search);

        // Step 4: Map results to DTO
        List<ClientProductOverviewResponseDTO> productList = productStats.stream().map(doc ->
                ClientProductOverviewResponseDTO.builder()
                        .productId(doc.getString("productId"))
                        .title(doc.getString("productName"))
                        .description(doc.getString("packageDescription"))
                        .enrolledUsers(doc.getInteger("enrolledUsers", 0))
                        .completionRate(doc.get("completionRate") != null
                                ? ((Number) doc.get("completionRate")).intValue()
                                : 0)
                        .active("ENABLED".equalsIgnoreCase(doc.getString("packageStatus")))
                        .thumbnailUrl(doc.getString("thumbnailUrl"))
                        .build()
        ).toList();

        // Step 5: Wrap in response
        return ClientProductOverviewWrapperDTO.builder()
                .availableProducts(productIds.size())
                .products(productList)
                .build();
    }

    @Override
    public List<ProductAnalyticsDTO> getClientProductAnalytics(String clientAdminId) {
        ClientProductIdListResponseDto clientProductIdListResponseDto = getProductIdsByClient(clientAdminId);
        List<ClientProductDTO> clientProductDTOS = clientProductIdListResponseDto.getClientProductDTOS();

        if (clientProductDTOS == null || clientProductDTOS.isEmpty()) return List.of();

        // Extract productIds from DTOs
        List<String> productIds = clientProductDTOS.stream()
                .map(ClientProductDTO::getProductId)
                .distinct()
                .toList();

        List<String> clientIds = clientProductIdListResponseDto.getClientIds();

        List<Document> rawStats = userPackageRepositoryCustom.getAnalyticsByPackageIdsAndUserIds(productIds, clientIds);

        return rawStats.stream().map(doc -> ProductAnalyticsDTO.builder()
                .productId(doc.getString("packageId"))
                .courseName(doc.getString("packageName"))
                .totalEnrolled(doc.getInteger("totalEnrolled", 0))
                .completed(doc.getInteger("completed", 0))
                .inProgress(doc.getInteger("inProgress", 0))
                .notStarted(doc.getInteger("notStarted", 0))
                .completionRate(doc.get("completionRate") != null
                        ? ((Number) doc.get("completionRate")).intValue()
                        : 0)
                .build()
        ).toList();
    }


    @Override
    public String downloadClientProductAnalytics(String clientAdminId) {
        List<ProductAnalyticsDTO> analytics = getClientProductAnalytics(clientAdminId);
        if (analytics.isEmpty()) throw new RuntimeException("No analytics data found");

        ByteArrayOutputStream excelStream = null;
                // ExcelGeneratorUtil.generateProductAnalyticsExcel(analytics);

        String fileName = "product-analytics-" + Instant.now().toEpochMilli() + ".xlsx";
        InputStream inputStream = new ByteArrayInputStream(excelStream.toByteArray());
        long size = excelStream.size();

        return azureCertificateUploader.uploadCertificate(fileName, inputStream, size, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }



    @Override
    public AssignedPackageOverviewWrapperDTO getAssignedPackagesForClientAdmin(String clientAdminId, String search, String statusFilter) {
        ClientProductIdListResponseDto productIdList = getProductIdsByClient(clientAdminId);
        List<ClientProductDTO> clientProductDTOS = productIdList.getClientProductDTOS();

        if (clientProductDTOS == null || clientProductDTOS.isEmpty()) {
            return AssignedPackageOverviewWrapperDTO.builder()
                    .totalAssignedPackages(0)
                    .packages(List.of())
                    .build();
        }

        Map<String, String> productIdToProductName = userPackageRepositoryCustom.getProductNamesByProductIds(
                clientProductDTOS.stream().map(ClientProductDTO::getProductId).distinct().toList()
        );

        Map<String, String> packageIdToBundleName = userPackageRepositoryCustom.getBundleNamesByPackageIds(
                clientProductDTOS.stream().map(ClientProductDTO::getPackageId).distinct().toList()
        );

        Instant now = Instant.now();

        List<AssignedPackageOverviewDTO> overviewList = clientProductDTOS.stream()
                .map(dto -> {
                    boolean isExpired = dto.getExpiryDate() != null && dto.getExpiryDate().isBefore(now);
                    String status = isExpired ? "EXPIRED" : "ACTIVE";

                    return AssignedPackageOverviewDTO.builder()
                            .productName(productIdToProductName.getOrDefault(dto.getProductId(), "N/A"))
                            .packageName(packageIdToBundleName.getOrDefault(dto.getPackageId(), "N/A"))
                            .status(status)
                            .licenseExpiry(dto.getExpiryDate() != null ? dto.getExpiryDate().toString() : "N/A")
                            .licenseUsed(dto.getUsedLicenseCount())
                            .licenseCount(dto.getLicenseCount())
                            .build();
                })
                .filter(dto -> {
                    boolean matchesSearch = (search == null || search.isBlank())
                            || dto.getPackageName().toLowerCase().contains(search.toLowerCase())
                            || dto.getProductName().toLowerCase().contains(search.toLowerCase());

                    boolean matchesStatus = (statusFilter == null || statusFilter.isBlank())
                            || dto.getStatus().equalsIgnoreCase(statusFilter);

                    return matchesSearch && matchesStatus;
                })
                .toList();

        return AssignedPackageOverviewWrapperDTO.builder()
                .totalAssignedPackages(overviewList.size())
                .packages(overviewList)
                .build();
    }



    @Override
    public AvailablePackageWrapperDTO getAvailablePackagesForClientAdmin(String clientAdminId) {
        // Step 1: Get assigned product/package details
        ClientProductIdListResponseDto productIdList = getProductIdsByClient(clientAdminId);
        List<ClientProductDTO> clientProductDTOS = productIdList.getClientProductDTOS();

        if (clientProductDTOS == null || clientProductDTOS.isEmpty()) {
            return AvailablePackageWrapperDTO.builder().total(0).packages(List.of()).build();
        }

        // Step 2: Fetch enriched bundle (package) details using custom repo
        List<Document> enrichedBundles = bundleRepositoryCustom.getEnrichedBundleDetails(clientProductDTOS);

        // Step 3: Map results to DTOs
        List<AvailablePackageDTO> available = enrichedBundles.stream().map(doc ->
                AvailablePackageDTO.builder()
                        .packageId(doc.getString("packageId"))
                        .packageName(doc.getString("bundleName")) // bundleName is packageName
                        .productName(doc.getString("productName"))
                        .packageDescription(doc.getString("packageDescription"))
                        .featureList(((List<?>) doc.get("featureNames")).stream()
                                .map(Object::toString)
                                .toList())
                        .build()
        ).toList();


        return AvailablePackageWrapperDTO.builder()
                .total(available.size())
                .packages(available)
                .build();
    }

    private String determineStatus(Instant expiryDate) {
        if (expiryDate == null) return "N/A";

        Instant now = Instant.now();
        if (expiryDate.isBefore(now)) return "Expired";

        long daysUntilExpiry = java.time.Duration.between(now, expiryDate).toDays();

        if (daysUntilExpiry <= 30) return "Expiring Soon";
        return "Active";
    }



    @Override
    public LicenseHistoryWrapperDTO getLicenseHistoryByClientAdmin(String clientAdminId) {
        // Step 1: Get client product assignments
        ClientProductIdListResponseDto productIdList = getProductIdsByClient(clientAdminId);
        List<ClientProductDTO> clientProducts = productIdList.getClientProductDTOS();

        if (clientProducts == null || clientProducts.isEmpty()) {
            return LicenseHistoryWrapperDTO.builder()
                    .total(0)
                    .historyList(List.of())
                    .build();
        }

        // Step 2: Fetch bundle names using packageId (which maps to bundle._id)
        List<String> bundleIds = clientProducts.stream()
                .map(ClientProductDTO::getPackageId)
                .distinct()
                .toList();

        Map<String, String> bundleIdToName = userPackageRepositoryCustom.getBundleNamesByPackageIds(bundleIds);

        // Step 3: Build license history list
        List<LicenseHistoryDTO> historyList = clientProducts.stream()
                .map(dto -> LicenseHistoryDTO.builder()
                        .packageName(bundleIdToName.getOrDefault(dto.getPackageId(), "N/A"))
                        .action("License Allocation") //TODO: need to change this static value
                        .date(dto.getAssignedAt() != null ? dto.getAssignedAt().toString() : "N/A")
                        .licensesAllocated(dto.getLicenseCount())
                        .expiryDate(dto.getExpiryDate() != null ? dto.getExpiryDate().toString() : "N/A")
                        .status(determineStatus(dto.getExpiryDate()))
                        .build())
                .toList();

        // Step 4: Return wrapped response
        return LicenseHistoryWrapperDTO.builder()
                .total(historyList.size())
                .historyList(historyList)
                .build();
    }


    @Override
    public PackagePerformanceWrapperDTO getPackagePerformanceStats(String clientAdminId) {
        // Step 1: Fetch products and packages for this client admin
        ClientProductIdListResponseDto productIdList = getProductIdsByClient(clientAdminId);
        List<ClientProductDTO> clientProducts = productIdList != null ? productIdList.getClientProductDTOS() : List.of();
        List<String> endUserIds = productIdList != null ? productIdList.getClientIds() : List.of();

        if (clientProducts.isEmpty() || endUserIds.isEmpty()) {
            return PackagePerformanceWrapperDTO.builder()
                    .total(0)
                    .data(List.of())
                    .build();
        }

        // Step 2: Extract unique bundleIds (aka packageId in user_packages) and productIds
        List<String> bundleIds = clientProducts.stream()
                .map(ClientProductDTO::getPackageId)
                .distinct()
                .toList();

        List<String> productIds = clientProducts.stream()
                .map(ClientProductDTO::getProductId)
                .distinct()
                .toList();

        Map<String, String> bundleIdToName = userPackageRepositoryCustom.getBundleNamesByPackageIds(bundleIds);
        Map<String, String> productIdToName = userPackageRepositoryCustom.getProductNamesByProductIds(productIds);

        // Step 3: Fetch aggregated stats from user_packages collection
        List<Document> aggregationResults = userPackageRepositoryCustom.getPackagePerformanceStats(productIds, endUserIds);

        // Step 4: Map results to DTOs
        List<PackagePerformanceDTO> stats = aggregationResults.stream().map(doc -> {
            String packageId = doc.getString("packageId");
            int assigned = doc.getInteger("assignedUsers", 0);
            int completed = doc.getInteger("completedUsers", 0);
            int topicsCompleted = doc.getInteger("topicsCompleted", 0);

            double completionRate = assigned == 0 ? 0.0 : (double) completed / assigned * 100;

            // Map packageId to productId (reverse lookup)
            String bundleId = clientProducts.stream()
                    .filter(p -> p.getProductId().equals(packageId)) // because in user_packages it's actually productId
                    .map(ClientProductDTO::getPackageId)
                    .findFirst()
                    .orElse("N/A");


            return PackagePerformanceDTO.builder()
                    .packageName(bundleIdToName.getOrDefault(bundleId, "N/A"))
                    .productName(productIdToName.getOrDefault(packageId, "N/A"))
                    .assignedUsers(assigned)
                    .licensesUsed(assigned)
                    .topicsCompleted(topicsCompleted)
                    .completionRate(String.format("%.0f%%", completionRate))
                    .avgTimeSpent("N/A") // Optional: Can be calculated later
                    .certifications(0)    // Optional: Can be extended
                    .certificationRate("N/A")
                    .build();
        }).toList();

        return PackagePerformanceWrapperDTO.builder()
                .total(stats.size())
                .data(stats)
                .build();
    }





    @Override
    public long getTotalClientProductCount(String clientAdminId, String search, String category) {
        // Return mock total (e.g. if you had 3 products for this clientAdmin)
        return 3L;
    }







//    @Override
//    public ResponseDtoWithCourseFeatureDetails getPackageById(String id) {
//        // Fetch the package by id
//        Package packages = packageRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
//
//        // Fetch course details and filter by enabled status
//        List<CourseResponseDto> courseDetails = courseRepository.findAllById(packages.getCourseIds())
//                .stream()
//                .filter(course -> course.getCourseStatus() == CourseStatus.ENABLED) // Filter courses with ENABLED status
//                .map(course -> CourseResponseDto.builder()
//                        .id(course.getId())
//                        .courseName(course.getCourseName())
//                        .courseDescription(course.getCourseDescription())
//                        .courseStatus(course.getCourseStatus())
//                        .chapterIds(course.getChapterIds())
//                        .productIds(course.getProductIds())
//                        .createdAt(course.getCreatedAt())
//                        .updatedAt(course.getUpdatedAt())
//                        .build())
//                .collect(Collectors.toList());
//
//        // Fetch feature details and filter by enabled status
////        List<com.aspire.asat.cms.dto.featureDto.ResponseDto> featureDetails = featureRepository.findAllById(packages.getFeatureIds())
////                .stream()
////                .filter(feature -> feature.getFeatureStatus() == FeatureStatus.ENABLED) // Filter features with ENABLED status
////                .map(feature -> com.aspire.asat.cms.dto.featureDto.ResponseDto.builder()
////                        .id(feature.getId())
////                        .featureName(feature.getFeatureName())
////                        .featureDescription(feature.getFeatureDescription())
////                        .featureStatus(feature.getFeatureStatus())
////                        .packageIds(feature.getPackageIds())
////                        .availability(feature.getAvailability())
////                        .createdAt(feature.getCreatedAt())
////                        .updatedAt(feature.getUpdatedAt())
////                        .lastModifiedBy(feature.getLastModifiedBy())
////                        .build())
////                .collect(Collectors.toList());
//
//        // Fetch bundle details by bundleIds
//        List<BundleDto> bundleDetails = bundleRepository.findAllById(packages.getBundlesIds())
//                .stream()
//                .map(bundle -> BundleDto.builder()
//                        .id(bundle.getId())
//                        .bundleName(bundle.getBundleName())
//                        .packageId(bundle.getPackageId())
//                        .featureIds(bundle.getFeatureIds())
//                        .price(bundle.getPrice())
//                        .createdAt(bundle.getCreatedAt())
//                        .bundleStatus(bundle.getBundleStatus())
//                        .build())
//                .collect(Collectors.toList());
//
//        // Return the DTO with all necessary details
//        return Package.toPackageDtoWithCourseFeatureDetails(packages, courseDetails, bundleDetails);
//    }


//    @Override
//    public ResponseDto updatePackageById(String packageId, UpdateRequestDto updateRequestDto) {
//        String id = packageId;
//        return packageRepository.findById(id)
//                .map(existingPackage -> {
//                    if(updateRequestDto.getPackageName() == null || updateRequestDto.getPackageName().isEmpty()){
//                        throw new NullException("Package name cannot be null or empty");
//                    }
//                    Package updatedPackage = Package.toUpdatePackage(id, updateRequestDto);
//                    updatedPackage.setCreatedAt(existingPackage.getCreatedAt());
//                    return Package.toPackageDto(packageRepository.save(updatedPackage));
//                })
//                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: "+id));
//    }
@Override
@Transactional
public ResponseDto updatePackageById(String packageId, UpdateRequestDto updateRequestDto) {
    return packageRepository.findById(packageId)
            .map(existingPackage -> {
                if (updateRequestDto.getPackageName() == null || updateRequestDto.getPackageName().isEmpty()) {
                    throw new NullException("Package name cannot be null or empty");
                }

                // Process Bundles
                List<BundleDto> newBundleDtos = updateRequestDto.getBundles();
                List<String> updatedBundleIds = new ArrayList<>();

                if (newBundleDtos != null && !newBundleDtos.isEmpty()) {
                    List<Bundle> existingBundles = bundleRepository.findByPackageId(packageId);
                    Map<String, Bundle> existingBundleMap = existingBundles.stream()
                            .collect(Collectors.toMap(Bundle::getId, b -> b));

                    List<Bundle> bundlesToSave = new ArrayList<>();

                    for (BundleDto dto : newBundleDtos) {
                        String bundleId = dto.getId();
                        if (bundleId != null && existingBundleMap.containsKey(bundleId)) {
                            // Update existing bundle
                            Bundle updated = Bundle.toEntity(bundleId, dto);
                            updated.setPackageId(packageId);
                            updated.setCreatedAt(existingBundleMap.get(bundleId).getCreatedAt());
                            bundlesToSave.add(updated);
                            updatedBundleIds.add(bundleId);
                        } else {
                            // New bundle
                            String newId = UUID.randomUUID().toString();
                            Bundle newBundle = Bundle.toEntity(newId, dto);
                            newBundle.setPackageId(packageId);
                            newBundle.setCreatedAt(Instant.now());
                            bundlesToSave.add(newBundle);
                            updatedBundleIds.add(newId);
                        }
                    }

                    // Save all modified and new bundles
                    bundleRepository.saveAll(bundlesToSave);

                    // Delete removed bundles
                    List<String> incomingIds = newBundleDtos.stream()
                            .map(BundleDto::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    List<String> existingIds = existingBundles.stream().map(Bundle::getId).toList();
                    List<String> toDelete = existingIds.stream()
                            .filter(id -> !incomingIds.contains(id))
                            .toList();

                    if (!toDelete.isEmpty()) {
                        bundleRepository.deleteAllById(toDelete);
                    }
                }

                // Update package entity
                Package updatedPackage = Package.toUpdatePackage(packageId, updateRequestDto);
                updatedPackage.setCreatedAt(existingPackage.getCreatedAt());
                updatedPackage.setBundlesIds(updatedBundleIds);

                Package saved = packageRepository.save(updatedPackage);
                ResponseDto response = Package.toPackageDto(saved);
                response.setBundlesIds(updatedBundleIds);

                return response;
            })
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + packageId));
}


    private void checkUniquePackageName(String packageName) {
        if (packageRepository.existsByPackageName(packageName)) {
            throw new DuplicateNameException("Package name '" + packageName + "' already exists.");
        }
    }

    @Override
    public String deletePackageById(String id) {
        Package packages = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: "+id));

        String packageName = packages.getPackageName();
        packageRepository.deleteById(id);

        return packageName;
    }

    @Override
    public void exportPackages(HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=packages.csv");

        List<ResponseDto> packages = getAllPackages(null, 0, 1000, "createdAt", "desc");

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {

            String[] header = { "ID", "Package Name", "Description", "Price", "Status", "Course IDs", "Feature IDs", "Created At", "Updated At" };
            writer.writeNext(header);

            for (ResponseDto pkg : packages) {
                writer.writeNext(new String[]{
                        pkg.getId().toString(),
                        pkg.getPackageName(),
                        pkg.getPackageDescription(),
                        String.valueOf(pkg.getPrice()),
                        pkg.getPackageStatus().toString(),
                        pkg.getCourseIds().toString(),
                        pkg.getFeatureIds().toString(),
                        pkg.getCreatedAt().toString(),
                        pkg.getUpdatedAt().toString()
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export packages to CSV", e);
        }
    }

    @Override
    public long getTotalPackageCount(String search, PackageStatus status) {
        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasStatus = status != null;

        if (hasSearch && hasStatus) {
            return packageRepository.countByPackageNameAndPackageStatus(search, status);
        } else if (hasSearch) {
            return packageRepository.countByPackageName(search);
        } else if (hasStatus) {
            return packageRepository.countByPackageStatus(status);
        } else {
            return packageRepository.count();
        }
    }


    @Override
    public List<String> deletePackagesByIds(List<String> ids) {
        List<String> deletedNames = new ArrayList<>();
        for (String id : ids) {
            deletedNames.add(deletePackageById(id));
        }
        return deletedNames;
    }

    @Override
    public List<String> updatePackagesStatusByIds(List<String> ids, Status status) {
        List<String> updatedPackageNames = new ArrayList<>();
        for (String id : ids) {
            Package packages = packageRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
            packages.setPackageStatus(PackageStatus.valueOf(status.name()));
            packageRepository.save(packages);
            updatedPackageNames.add(packages.getPackageName());
        }
        return updatedPackageNames;
    }

    @Override
    public void exportBulkPackages(List<String> ids, HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=bulk_packages.csv");

        List<com.aspire.asat.cms.dto.packageDto.ResponseDto> packages = ids.stream()
                .map(id -> {
                    Package aPackage = packageRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
                    return Package.toPackageDto(aPackage);
                })
                .collect(Collectors.toList());

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            String[] header = { "ID", "Package Name", "Package Description", "Price", "Status", "Course IDs", "Feature IDs", "Created At", "Updated At" };
            writer.writeNext(header);

            for (com.aspire.asat.cms.dto.packageDto.ResponseDto tmppackage : packages) {
                writer.writeNext(new String[]{
                        tmppackage.getId().toString(),
                        tmppackage.getPackageName(),
                        tmppackage.getPackageDescription(),
                        String.valueOf(tmppackage.getPrice()),
                        tmppackage.getPackageStatus().toString(),
                        tmppackage.getCourseIds().toString(),
                        tmppackage.getFeatureIds().toString(),
                        tmppackage.getCreatedAt().toString(),
                        tmppackage.getUpdatedAt().toString()
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export bulk packages to CSV", e);
        }
    }

    @Override
    public boolean packageExistsByName(String packageName) {
        return packageRepository.existsByPackageName(packageName);
    }


}
