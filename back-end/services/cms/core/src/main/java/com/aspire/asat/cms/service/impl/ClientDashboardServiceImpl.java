package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.clientDashboard.ClientDashboardRequestDto;
import com.aspire.asat.cms.dto.clientDashboard.ClientDashboardResponseDto;
import com.aspire.asat.cms.dto.clientDashboard.ClientProductData;
import com.aspire.asat.cms.model.ClientDashboard;
import com.aspire.asat.cms.model.ClientProductReplica;
import com.aspire.asat.cms.repository.ClientDashboardRepository;
import com.aspire.asat.cms.repository.ClientProductReplicaRepository;
import com.aspire.asat.cms.repository.topic.TopicFilterRepositoryCustom;
import com.aspire.asat.cms.service.ClientDashboardService;
import com.aspire.asat.cms.service.OrganizationDashboardService;
import com.aspire.asat.cms.util.CommonUtil;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientDashboardServiceImpl implements ClientDashboardService {

    private final ClientDashboardRepository clientDashboardRepository;
    private final ClientProductReplicaRepository clientProductReplicaRepository;
    private final TopicFilterRepositoryCustom topicFilterRepositoryCustom;
    private final OrganizationDashboardService organizationDashboardService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ClientDashboardResponseDto createOrUpdateClientDashboard(ClientDashboardRequestDto requestDto) {
        log.info("Creating or updating client dashboard for clientAdminId: {}", requestDto.getClientAdminId());

        Instant now = Instant.now();

        // Step 1: Save ClientProductReplica FIRST if provided
        if (requestDto.getClientProductData() != null && !requestDto.getClientProductData().isEmpty()) {
            log.info("Step 1: Saving client product replicas first");
            saveClientProductReplicas(requestDto.getClientAdminId(), requestDto.getClientProductData());
            log.info("Step 1: Successfully saved client product replicas");
        }

        // Step 2: Calculate unique topics count based on ClientProductReplica data
        log.info("Step 2: Calculating unique topics count for clientAdminId: {}", requestDto.getClientAdminId());
        int totalUniqueTopics = calculateUniqueTopicsCount(requestDto.getClientAdminId(), now);
        log.info("Step 2: Calculated total unique topics: {} for clientAdminId: {}", totalUniqueTopics, requestDto.getClientAdminId());

        // Step 3: Get or create ClientDashboard and update with calculated topic count
        Optional<ClientDashboard> existingDashboard = clientDashboardRepository.findByClientAdminId(requestDto.getClientAdminId());

        // Track old values for incremental update calculation
        Integer oldTotalLicense = null;
        boolean isNewClient = false;

        ClientDashboard clientDashboard;
        if (existingDashboard.isPresent()) {
            // Update existing record
            clientDashboard = existingDashboard.get();
            oldTotalLicense = clientDashboard.getTotalLicense(); // Store old value for delta calculation
            clientDashboard.setTotalProduct(requestDto.getTotalProduct());
            clientDashboard.setTotalLicense(requestDto.getTotalLicense());
            clientDashboard.setTotalTopic(totalUniqueTopics); // Use calculated value
            clientDashboard.setTotalCertificate(clientDashboard.getTotalCertificate() + (requestDto.getTotalCertificate() != null ? requestDto.getTotalCertificate() : 0));
            clientDashboard.setUpdatedAt(now);
            log.info("Step 3: Updating existing client dashboard with ID: {}", clientDashboard.getId());
        } else {
            // Create new record
            isNewClient = true;
            clientDashboard = ClientDashboard.builder()
                    .id(CommonUtil.generateUUID())
                    .clientAdminId(requestDto.getClientAdminId())
                    .totalProduct(requestDto.getTotalProduct())
                    .totalLicense(requestDto.getTotalLicense())
                    .totalTopic(totalUniqueTopics) // Use calculated value
                    .totalCertificate(requestDto.getTotalCertificate())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            log.info("Step 3: Creating new client dashboard for clientAdminId: {}", requestDto.getClientAdminId());
        }

        // Step 4: Save ClientDashboard with calculated topic count
        ClientDashboard savedDashboard = clientDashboardRepository.save(clientDashboard);
        log.info("Step 4: Successfully saved client dashboard with ID: {} and totalTopic: {}", 
                savedDashboard.getId(), savedDashboard.getTotalTopic());

        // Step 5: Update OrganizationDashboard incrementally with totalLicense and totalClient counts
        // Calculate license delta: new value - old value (0 for new clients, delta for updates)
        int licenseDelta = isNewClient 
                ? (requestDto.getTotalLicense() != null ? requestDto.getTotalLicense() : 0)
                : (requestDto.getTotalLicense() != null ? requestDto.getTotalLicense() : 0) 
                  - (oldTotalLicense != null ? oldTotalLicense : 0);
        
        updateOrganizationDashboardCountsIncremental(licenseDelta, isNewClient);

        return convertToResponseDto(savedDashboard);
    }

    /**
     * Update OrganizationDashboard incrementally with totalLicense and totalClient counts
     * Uses delta-based updates for efficiency instead of recalculating from entire table
     * Gets organizationAdminId from user context, or uses system default if context not available
     * 
     * @param licenseDelta the change in license count (new - old, or new value for new clients)
     * @param isNewClient true if this is a new client dashboard, false if updating existing
     */
    private void updateOrganizationDashboardCountsIncremental(int licenseDelta, boolean isNewClient) {
        try {
            String organizationAdminId = null;
            String updatedBy = "SYSTEM";
            
            // Try to get user context to extract organizationAdminId
            try {
                CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
                organizationAdminId = getOrganizationAdminId(userContext);
                updatedBy = userContext != null && userContext.getUserId() != null
                        ? userContext.getUserId() : "SYSTEM";
            } catch (Exception contextException) {
                // User context not available (e.g., service-to-service call)
                log.debug("User context not available, using system default for organizationAdminId: {}", 
                        contextException.getMessage());
                // Use a system default organizationAdminId if context is not available
                // This handles service-to-service calls from Registration service
                organizationAdminId = "SYSTEM"; // Default system organization admin ID
            }

            if (organizationAdminId != null && !organizationAdminId.trim().isEmpty()) {
                // Update both totalLicense and totalClient incrementally (efficient O(1) operation)
                organizationDashboardService.incrementLicenseAndClientCounts(
                        organizationAdminId, licenseDelta, isNewClient, updatedBy);
                log.info("Updated organization dashboard incrementally - licenseDelta: {}, isNewClient: {} for organizationAdminId: {}", 
                        licenseDelta, isNewClient, organizationAdminId);
            } else {
                log.warn("Organization admin ID not available, skipping dashboard update");
            }
        } catch (Exception e) {
            // Log error but don't fail the client dashboard creation/update
            log.error("Error updating organization dashboard: {}", e.getMessage(), e);
        }
    }

    /**
     * Get organization admin ID from user context
     * @param userContext the current user context
     * @return organization admin ID
     */
    private String getOrganizationAdminId(CurrentUserContext userContext) {
        if (userContext != null) {
            // Try to get organizationAdminId from context
            // If not available in CurrentUserContext, use userId as fallback
            // You may need to adjust this based on your actual user context structure
            // For now, using userId as organizationAdminId (assuming super admin user)
            return userContext.getUserId();
        }
        return null;
    }

    /**
     * Save client product replica data
     * Always creates new records without checking for existing ones
     * @param clientAdminId the client admin ID
     * @param clientProductDataList list of client product data
     */
    private void saveClientProductReplicas(String clientAdminId, List<ClientProductData> clientProductDataList) {
        log.info("Saving {} client product replicas for clientAdminId: {}", clientProductDataList.size(), clientAdminId);
        
        Instant now = Instant.now();
        
        List<ClientProductReplica> replicas = clientProductDataList.stream()
                .map(data -> ClientProductReplica.builder()
                        .id(CommonUtil.generateUUID())
                        .clientAdminId(data.getClientAdminId())
                        .productId(data.getProductId())
                        .packageId(data.getPackageId())
                        .assignedAt(data.getAssignedAt())
                        .expiryDate(data.getExpiryDate())
                        .email(data.getEmail())
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .collect(Collectors.toList());
        
        clientProductReplicaRepository.saveAll(replicas);
        log.info("Successfully saved {} client product replicas for clientAdminId: {}", replicas.size(), clientAdminId);
    }

    @Override
    public ClientDashboardResponseDto getClientDashboardByClientAdminId(String clientAdminId) {
        log.info("Retrieving client dashboard for clientAdminId: {}", clientAdminId);

        Optional<ClientDashboard> clientDashboard = clientDashboardRepository.findByClientAdminId(clientAdminId);

        if (clientDashboard.isEmpty()) {
            log.warn("Client dashboard not found for clientAdminId: {}", clientAdminId);
            throw new RuntimeException("Client dashboard not found for clientAdminId: " + clientAdminId);
        }

        log.info("Successfully retrieved client dashboard for clientAdminId: {}", clientAdminId);
        return convertToResponseDto(clientDashboard.get());
    }

    /**
     * Calculate unique topics count based on clientAdminId, productIds, and packageIds
     * Retrieved from ClientProductReplica model where expiryDate is valid (not expired)
     * 
     * @param clientAdminId the client admin ID
     * @param currentTime the current time to compare with expiryDate
     * @return count of unique topics
     */
    private int calculateUniqueTopicsCount(String clientAdminId, Instant currentTime) {
        log.info("Calculating unique topics count for clientAdminId: {}, currentTime: {}", clientAdminId, currentTime);

        // Step 1: Retrieve valid productIds and packageIds from ClientProductReplica
        // Filter by clientAdminId and expiryDate >= currentTime (not expired)
        List<ClientProductReplica> validReplicas = clientProductReplicaRepository.findByClientAdminId(clientAdminId)
                .stream()
                .filter(replica -> replica.getExpiryDate() != null && 
                                  (replica.getExpiryDate().isAfter(currentTime) || 
                                   replica.getExpiryDate().equals(currentTime)))
                .collect(Collectors.toList());

        if (validReplicas.isEmpty()) {
            log.info("No valid client product replicas found for clientAdminId: {} (all expired or no records)", clientAdminId);
            return 0;
        }

        // Extract unique productIds and packageIds
        Set<String> productIds = validReplicas.stream()
                .map(ClientProductReplica::getProductId)
                .filter(productId -> productId != null && !productId.trim().isEmpty())
                .collect(Collectors.toSet());

        Set<String> packageIds = validReplicas.stream()
                .map(ClientProductReplica::getPackageId)
                .filter(packageId -> packageId != null && !packageId.trim().isEmpty())
                .collect(Collectors.toSet());

        log.info("Found {} valid replicas with {} unique productIds and {} unique packageIds", 
                validReplicas.size(), productIds.size(), packageIds.size());

        if (productIds.isEmpty() && packageIds.isEmpty()) {
            log.info("No valid productIds or packageIds found for clientAdminId: {}", clientAdminId);
            return 0;
        }

        // Step 2: Query Topic model to find unique topics count using repository
        // Topics should match:
        // - productPackageMappings.productId is in productIds list AND
        // - productPackageMappings.packageIds contains any of the packageIds
        Long uniqueTopicCountLong = topicFilterRepositoryCustom.countUniqueTopicsByProductAndPackage(
                new ArrayList<>(productIds), 
                new ArrayList<>(packageIds)
        );

        int uniqueTopicCount = uniqueTopicCountLong != null ? uniqueTopicCountLong.intValue() : 0;
        log.info("Found {} unique topics for clientAdminId: {} with productIds: {} and packageIds: {}", 
                uniqueTopicCount, clientAdminId, productIds, packageIds);

        return uniqueTopicCount;
    }

    private ClientDashboardResponseDto convertToResponseDto(ClientDashboard clientDashboard) {
        return ClientDashboardResponseDto.builder()
                .id(clientDashboard.getId())
                .clientAdminId(clientDashboard.getClientAdminId())
                .totalProduct(clientDashboard.getTotalProduct())
                .totalLicense(clientDashboard.getTotalLicense())
                .totalTopic(clientDashboard.getTotalTopic())
                .totalCertificate(clientDashboard.getTotalCertificate())
                .createdAt(clientDashboard.getCreatedAt())
                .updatedAt(clientDashboard.getUpdatedAt())
                .build();
    }
}
