package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.OrganizationDashboard;
import com.aspire.asat.cms.repository.OrganizationDashboardRepository;
import com.aspire.asat.cms.service.OrganizationDashboardService;
import com.aspire.asat.cms.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Organization Dashboard (Super Admin Dashboard)
 * This dashboard represents system-wide statistics
 * There is only ONE entry in the collection, identified by organizationAdminId from context
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationDashboardServiceImpl implements OrganizationDashboardService {

    private final OrganizationDashboardRepository organizationDashboardRepository;

    /**
     * Get or create the organization dashboard
     * Uses organizationAdminId from context to identify the single dashboard entry
     * @param organizationAdminId the organization admin ID from user context
     * @param createdBy the user who is creating/updating
     * @return OrganizationDashboard
     */
    private OrganizationDashboard getOrCreateDashboard(String organizationAdminId, String createdBy) {
        Optional<OrganizationDashboard> existingDashboard = 
                organizationDashboardRepository.findByOrganizationAdminId(organizationAdminId);

        if (existingDashboard.isPresent()) {
            return existingDashboard.get();
        }

        // Create new dashboard if it doesn't exist
        Instant now = Instant.now();
        OrganizationDashboard dashboard = OrganizationDashboard.builder()
                .id(CommonUtil.generateUUID())
                .organizationAdminId(organizationAdminId)
                .totalProduct(0)
                .totalPackage(0)
                .totalLicense(0)
                .totalClient(0)
                .totalMsp(0)
                .createdAt(now)
                .updatedAt(now)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        return organizationDashboardRepository.save(dashboard);
    }

    @Override
    @Transactional
    public void incrementTotalProduct(String organizationAdminId, String createdBy) {
        log.info("Incrementing total product count in organization dashboard for organizationAdminId: {}", organizationAdminId);
        
        Instant now = Instant.now();
        OrganizationDashboard dashboard = getOrCreateDashboard(organizationAdminId, createdBy);
        
        // Increment total product count
        int currentCount = dashboard.getTotalProduct() != null ? dashboard.getTotalProduct() : 0;
        dashboard.setTotalProduct(currentCount + 1);
        dashboard.setUpdatedAt(now);
        dashboard.setUpdatedBy(createdBy);
        
        organizationDashboardRepository.save(dashboard);
        log.info("Successfully updated organization dashboard - totalProduct: {}", dashboard.getTotalProduct());
    }

    @Override
    @Transactional
    public void incrementTotalPackage(String organizationAdminId, int packageCount, String createdBy) {
        log.info("Incrementing total package count by {} in organization dashboard for organizationAdminId: {}", 
                packageCount, organizationAdminId);
        
        Instant now = Instant.now();
        OrganizationDashboard dashboard = getOrCreateDashboard(organizationAdminId, createdBy);
        
        // Increment total package count
        int currentCount = dashboard.getTotalPackage() != null ? dashboard.getTotalPackage() : 0;
        dashboard.setTotalPackage(currentCount + packageCount);
        dashboard.setUpdatedAt(now);
        dashboard.setUpdatedBy(createdBy);
        
        organizationDashboardRepository.save(dashboard);
        log.info("Successfully updated organization dashboard - totalPackage: {}", dashboard.getTotalPackage());
    }

    @Override
    public OrganizationDashboard getOrganizationDashboard(String organizationAdminId) {
        log.info("Retrieving organization dashboard for organizationAdminId: {}", organizationAdminId);
        
        Optional<OrganizationDashboard> dashboard = 
                organizationDashboardRepository.findByOrganizationAdminId(organizationAdminId);
        
        if (dashboard.isEmpty()) {
            log.warn("Organization dashboard not found for organizationAdminId: {}", organizationAdminId);
            throw new ResourceNotFoundException("Organization dashboard not found for organizationAdminId: " + organizationAdminId);
        }

        log.info("Successfully retrieved organization dashboard for organizationAdminId: {}", organizationAdminId);
        return dashboard.get();
    }

    @Override
    public OrganizationDashboard getConsolidatedOrganizationDashboard() {
        log.info("Retrieving consolidated organization dashboard (summed across all dashboards)");
        List<OrganizationDashboard> allDashboards = organizationDashboardRepository.findAll();

        int totalProduct = 0;
        int totalPackage = 0;
        int totalLicense = 0;
        int totalClient = 0;
        int totalMsp = 0;

        for (OrganizationDashboard d : allDashboards) {
            totalProduct += (d.getTotalProduct() != null ? d.getTotalProduct() : 0);
            totalPackage += (d.getTotalPackage() != null ? d.getTotalPackage() : 0);
            totalLicense += (d.getTotalLicense() != null ? d.getTotalLicense() : 0);
            totalClient += (d.getTotalClient() != null ? d.getTotalClient() : 0);
            totalMsp += (d.getTotalMsp() != null ? d.getTotalMsp() : 0);
        }

        log.info("Consolidated totals - totalProduct: {}, totalPackage: {}, totalLicense: {}, totalClient: {}, totalMsp: {}",
                totalProduct, totalPackage, totalLicense, totalClient, totalMsp);

        return OrganizationDashboard.builder()
                .id(null)
                .organizationAdminId(null)
                .totalProduct(totalProduct)
                .totalPackage(totalPackage)
                .totalLicense(totalLicense)
                .totalClient(totalClient)
                .totalMsp(totalMsp)
                .createdAt(null)
                .updatedAt(null)
                .createdBy(null)
                .updatedBy(null)
                .build();
    }

    @Override
    @Transactional
    public OrganizationDashboard createOrUpdateDashboard(String organizationAdminId, String createdBy) {
        log.info("Creating or updating organization dashboard for organizationAdminId: {}", organizationAdminId);
        
        Instant now = Instant.now();
        OrganizationDashboard dashboard = getOrCreateDashboard(organizationAdminId, createdBy);
        
        dashboard.setUpdatedAt(now);
        dashboard.setUpdatedBy(createdBy);
        
        OrganizationDashboard savedDashboard = organizationDashboardRepository.save(dashboard);
        log.info("Successfully saved organization dashboard with ID: {}", savedDashboard.getId());
        return savedDashboard;
    }

    @Override
    @Transactional
    public void incrementLicenseAndClientCounts(String organizationAdminId, int licenseDelta, boolean isNewClient, String updatedBy) {
        log.info("Incrementing license and client counts in organization dashboard for organizationAdminId: {} - licenseDelta: {}, isNewClient: {}", 
                organizationAdminId, licenseDelta, isNewClient);
        
        try {
            Instant now = Instant.now();
            OrganizationDashboard dashboard = getOrCreateDashboard(organizationAdminId, updatedBy);
            
            // Increment total license count by delta
            int currentLicense = dashboard.getTotalLicense() != null ? dashboard.getTotalLicense() : 0;
            dashboard.setTotalLicense(currentLicense + licenseDelta);
            
            // Increment total client count only if new client
            if (isNewClient) {
                int currentClient = dashboard.getTotalClient() != null ? dashboard.getTotalClient() : 0;
                dashboard.setTotalClient(currentClient + 1);
            }
            
            dashboard.setUpdatedAt(now);
            dashboard.setUpdatedBy(updatedBy);
            
            organizationDashboardRepository.save(dashboard);
            log.info("Successfully updated organization dashboard - totalLicense: {} (delta: {}), totalClient: {} (new: {})", 
                    dashboard.getTotalLicense(), licenseDelta, dashboard.getTotalClient(), isNewClient);
            
        } catch (Exception e) {
            log.error("Error incrementing license and client counts in organization dashboard for organizationAdminId: {}", 
                    organizationAdminId, e);
            // Don't fail the main operation if dashboard update fails
        }
    }
}

