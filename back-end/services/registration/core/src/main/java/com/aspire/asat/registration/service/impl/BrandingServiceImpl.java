package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.branding.request.BrandingCreateRequestDTO;
import com.aspire.asat.registration.data.branding.request.BrandingUpdateRequestDTO;
import com.aspire.asat.registration.data.branding.response.BrandingResponseDTO;
import com.aspire.asat.registration.exception.ResourceAlreadyExistsException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.BrandingService;
import com.aspire.asat.registration.service.branding.BrandingMapping;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandingServiceImpl implements BrandingService {

    private final ClientAdminRepository clientAdminRepository;
    private final MspUsersRepository mspUsersRepository;
    private final UserCurrentContextService userCurrentContextService;

    /**
     * Branding is stored on {@code client_admins} or {@code msp_user}:
     * {@code organizationName} (company display name) and {@code logoUrl} (logo file URL/path).
     */
    private static String normalizeLogoUrl(String logoFilePath) {
        if (!StringUtils.hasText(logoFilePath)) {
            return null;
        }
        return logoFilePath.trim();
    }

    /**
     * Resolves the client admin row for create/update/delete. CLIENT_ADMIN: id == JWT userId.
     * USER: uses {@code clientAdminId} from context (organization's admin).
     */
    private ClientAdmin loadClientAdminForMutation(CurrentUserContext ctx) {
        String clientAdminId;
        if (UserType.CLIENT_ADMIN.name().equals(ctx.getUserType())) {
            clientAdminId = ctx.getUserId();
        } else if (UserType.USER.name().equals(ctx.getUserType())) {
            clientAdminId = ctx.getClientAdminId();
            if (!StringUtils.hasText(clientAdminId)) {
                throw new ResourceNotFoundException("Client admin context not found for current user");
            }
        } else {
            throw new ResourceNotFoundException("Branding is only available for client admin organization users");
        }
        return clientAdminRepository.findById(clientAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("Client admin not found for current user"));
    }

    private MspUser loadMspUserForMutation(CurrentUserContext ctx) {
        String mspUserId = ctx.getUserId();
        if (!StringUtils.hasText(mspUserId)) {
            throw new ResourceNotFoundException("MSP user context not found");
        }
        return mspUsersRepository.findById(mspUserId)
                .or(() -> mspUsersRepository.findByMspId(mspUserId))
                .orElseThrow(() -> new ResourceNotFoundException("MSP user not found for current user"));
    }

    private String resolveClientAdminIdForRead(CurrentUserContext userContext) {
        if (UserType.CLIENT_ADMIN.name().equals(userContext.getUserType())) {
            return userContext.getUserId();
        }
        if (UserType.USER.name().equals(userContext.getUserType())) {
            return userContext.getClientAdminId();
        }
        return userContext.getUserId();
    }

    @Override
    public BrandingResponseDTO createBranding(BrandingCreateRequestDTO requestDTO) {
        log.info("Creating branding for company: {}", requestDTO.getCompanyName());

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (UserType.MSP.name().equalsIgnoreCase(userContext.getUserType())) {
            return createMspBranding(userContext, requestDTO);
        }

        ClientAdmin clientAdmin = loadClientAdminForMutation(userContext);

        String logoUrl = normalizeLogoUrl(requestDTO.getLogoFilePath());
        if (clientAdmin.getLogoUrl() != null && !clientAdmin.getLogoUrl().isBlank()) {
            throw new ResourceAlreadyExistsException("Branding for Client Admin Id Already exists");
        }

        Instant now = Instant.now();
        clientAdmin.setOrganizationName(requestDTO.getCompanyName().trim());
        clientAdmin.setLogoUrl(logoUrl);
        clientAdmin.setUpdatedAt(now);

        ClientAdmin saved = clientAdminRepository.save(clientAdmin);
        log.info("Saved branding on client_admins id={} organizationName set, logoUrl={}",
                saved.getId(), saved.getLogoUrl() != null ? "(set)" : "(null)");

        return BrandingMapping.toDto(saved);
    }

    private BrandingResponseDTO createMspBranding(CurrentUserContext userContext, BrandingCreateRequestDTO requestDTO) {
        MspUser mspUser = loadMspUserForMutation(userContext);

        String logoUrl = normalizeLogoUrl(requestDTO.getLogoFilePath());
        if (mspUser.getLogoUrl() != null && !mspUser.getLogoUrl().isBlank()) {
            throw new ResourceAlreadyExistsException("Branding for MSP Id Already exists");
        }

        Instant now = Instant.now();
        mspUser.setOrganizationName(requestDTO.getCompanyName().trim());
        mspUser.setLogoUrl(logoUrl);
        mspUser.setUpdatedAt(now);

        MspUser saved = mspUsersRepository.save(mspUser);
        log.info("Saved branding on msp_user id={} organizationName set, logoUrl={}",
                saved.getId(), saved.getLogoUrl() != null ? "(set)" : "(null)");

        return BrandingMapping.toDto(saved);
    }

    @Override
    public BrandingResponseDTO updateBranding(BrandingUpdateRequestDTO requestDTO) {
        log.info("Updating branding: {}", requestDTO.getCompanyName());
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();

        if (UserType.MSP.name().equalsIgnoreCase(userContext.getUserType())) {
            return updateMspBranding(userContext, requestDTO);
        }

        return updateClientAdminBranding(userContext, requestDTO);
    }

    private BrandingResponseDTO updateClientAdminBranding(
            CurrentUserContext userContext, BrandingUpdateRequestDTO requestDTO) {
        ClientAdmin clientAdmin = loadClientAdminForMutation(userContext);

        boolean deactivating = Boolean.FALSE.equals(requestDTO.getActive());
        Instant now = Instant.now();

        if (deactivating) {
            clientAdmin.setLogoUrl(null);
        } else {
            clientAdmin.setOrganizationName(requestDTO.getCompanyName().trim());
            clientAdmin.setLogoUrl(normalizeLogoUrl(requestDTO.getLogoFilePath()));
        }

        clientAdmin.setUpdatedAt(now);

        ClientAdmin saved = clientAdminRepository.save(clientAdmin);
        log.info("Updated branding on client_admins id={} organizationName set, logoUrl={}",
                saved.getId(), saved.getLogoUrl() != null ? "(set)" : "(null)");

        return BrandingMapping.toDto(saved);
    }

    private BrandingResponseDTO updateMspBranding(
            CurrentUserContext userContext, BrandingUpdateRequestDTO requestDTO) {
        MspUser mspUser = loadMspUserForMutation(userContext);

        boolean deactivating = Boolean.FALSE.equals(requestDTO.getActive());
        Instant now = Instant.now();

        if (deactivating) {
            mspUser.setLogoUrl(null);
        } else {
            mspUser.setOrganizationName(requestDTO.getCompanyName().trim());
            mspUser.setLogoUrl(normalizeLogoUrl(requestDTO.getLogoFilePath()));
        }

        mspUser.setUpdatedAt(now);

        MspUser saved = mspUsersRepository.save(mspUser);
        log.info("Updated branding on msp_user id={} organizationName set, logoUrl={}",
                saved.getId(), saved.getLogoUrl() != null ? "(set)" : "(null)");

        return BrandingMapping.toDto(saved);
    }

    @Override
    public void deleteBranding(String brandingId) {
        log.info("Soft deleting branding for brandingId: {}", brandingId);

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (UserType.MSP.name().equalsIgnoreCase(userContext.getUserType())) {
            MspUser mspUser = loadMspUserForMutation(userContext);
            if (!mspUser.getId().equals(brandingId)) {
                throw new ResourceNotFoundException("Branding not found with ID: " + brandingId);
            }
            mspUser.setLogoUrl(null);
            mspUser.setUpdatedAt(Instant.now());
            mspUsersRepository.save(mspUser);
            log.info("Cleared logoUrl on msp_user for mspUserId: {}", brandingId);
            return;
        }

        ClientAdmin clientAdmin = loadClientAdminForMutation(userContext);

        if (!clientAdmin.getId().equals(brandingId)) {
            throw new ResourceNotFoundException("Branding not found with ID: " + brandingId);
        }

        clientAdmin.setLogoUrl(null);
        clientAdmin.setUpdatedAt(Instant.now());

        clientAdminRepository.save(clientAdmin);
        log.info("Cleared logoUrl on client_admins for clientAdminId: {}", brandingId);
    }

    @Override
    public BrandingResponseDTO getBranding() {
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();

        if (UserType.MSP.name().equalsIgnoreCase(userContext.getUserType())) {
            String mspUserId = userContext.getUserId();
            log.info("Getting branding by mspUserId: {}", mspUserId);
            if (!StringUtils.hasText(mspUserId)) {
                return null;
            }
            return mspUsersRepository.findById(mspUserId)
                    .or(() -> mspUsersRepository.findByMspId(mspUserId))
                    .map(BrandingMapping::toDto)
                    .orElse(null);
        }

        String clientAdminId = resolveClientAdminIdForRead(userContext);

        log.info("Getting branding by clientAdminId: {}", clientAdminId);

        if (!StringUtils.hasText(clientAdminId)) {
            return null;
        }

        Optional<ClientAdmin> opt = clientAdminRepository.findById(clientAdminId);
        return opt.map(BrandingMapping::toDto)
                .orElse(null);
    }
}
