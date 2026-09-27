package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.requiredinfo.RequiredInfoResponseDTO;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.service.branding.BrandingMapping;
import com.aspire.asat.registration.service.RequiredInfoService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation for required info operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequiredInfoServiceImpl implements RequiredInfoService {

    private final ClientAdminRepository clientAdminRepository;
    private final AspireUserRepository aspireUserRepository;
    private final EndUserPackageRepository endUserPackageRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final CmsServiceClient cmsServiceClient;

    @Override
    public RequiredInfoResponseDTO getRequiredInfo() {
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String clientAdminId = null;
        String userId = userContext.getUserId();

        // Determine clientAdminId based on user type
        if (UserType.CLIENT_ADMIN.name().equals(userContext.getUserType())) {
            clientAdminId = userId;
        } else if (UserType.USER.name().equals(userContext.getUserType())) {
            clientAdminId = userContext.getClientAdminId();
        }

        log.info("Getting required info for userId: {}, clientAdminId: {}, userType: {}", 
                userId, clientAdminId, userContext.getUserType());

        // Branding is stored on client_admins (organizationName, logoUrl); hasBranding when logoUrl is set
        boolean hasBranding = false;
        if (clientAdminId != null) {
            hasBranding = clientAdminRepository.findById(clientAdminId)
                    .map(BrandingMapping::shouldExposeBranding)
                    .orElse(false);
        }

        // Check if users exist for client admin
        boolean hasUser = false;
        if (clientAdminId != null) {
            hasUser = !aspireUserRepository.findByUserTypeAndClientAdminId(
                    UserType.USER.name(), clientAdminId).isEmpty();
        }

        // Check if products are assigned to the logged-in user
        boolean productAssigned = false;
        if (userId != null) {
            productAssigned = !endUserPackageRepository.findAllByClientAdminIdAndActiveTrue(userId).isEmpty();
        }

        // Check if certificate template is configured for client admin
        boolean hasCertificateTemplate = false;
        if (clientAdminId != null) {
            try {
                hasCertificateTemplate = cmsServiceClient.checkCertificateTemplateExists(clientAdminId);
            } catch (Exception e) {
                log.error("Error checking certificate template existence for clientAdminId: {}", clientAdminId, e);
                // Default to false on error to ensure API doesn't fail
            }
        }

        log.info("Required info status - hasBranding: {}, hasUser: {}, productAssigned: {}, hasCertificateTemplate: {}",
                hasBranding, hasUser, productAssigned, hasCertificateTemplate);

        return RequiredInfoResponseDTO.builder()
                .hasBranding(hasBranding)
                .hasUser(hasUser)
                .productAssigned(productAssigned)
                .hasCertificateTemplate(hasCertificateTemplate)
                .build();
    }
}

