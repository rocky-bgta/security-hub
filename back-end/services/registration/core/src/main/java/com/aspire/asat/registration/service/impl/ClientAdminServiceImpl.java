package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.dto.invoice_logs.CommentLogRequestDTO;
import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.common.dto.notification.NotificationTemplateValue;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.common.service.files.FileService;
import com.aspire.asat.common.util.InvoiceGenerator;
import com.aspire.asat.common.util.MoneyUtil;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.client.service.PhishingServiceClient;
import com.aspire.asat.registration.data.EmbeddedPackageItem;
import com.aspire.asat.registration.data.EmbeddedPaymentPayload;
import com.aspire.asat.registration.data.OrganizationLicenseStatistics;
import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.registration.data.clientAdmin.request.BillingInfoDto;
import com.aspire.asat.registration.data.clientAdmin.request.BuyNowRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.ClientAdminListRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.ClientAdminUpdateRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.ClientOnboardingRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.ClientProductAssignment;
import com.aspire.asat.registration.data.clientAdmin.request.InvoiceDetailsDto;
import com.aspire.asat.registration.data.clientAdmin.request.OrganizationInfoDto;
import com.aspire.asat.registration.data.clientAdmin.request.ProductSelectionDto;
import com.aspire.asat.registration.data.clientAdmin.request.ValidityUnit;
import com.aspire.asat.registration.data.clientAdmin.response.BuyNowResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientAdminDetailedResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientAdminListResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientAdminWithProductsResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientDropdownDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientMspListItemDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientOnboardingResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductDTO;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductDetailDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductDetailedDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductReassignmentResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductsPaginatedResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.EmailTemplateValidationResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.LicenseOverviewSummaryDto;
import com.aspire.asat.registration.data.clientAdmin.response.LicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.OrganizationLicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.cms.request.ClientDashboardRequestDto;
import com.aspire.asat.registration.data.cms.request.ClientProductData;
import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import com.aspire.asat.registration.data.cms.response.CmsProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsProductWithSinglePackageResponseDto;
import com.aspire.asat.registration.data.phishing.response.CampaignLicenseUsageDto;
import com.aspire.asat.registration.data.coupon.CouponResponseDTO;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.data.enums.OnboardBy;
import com.aspire.asat.registration.data.enums.RoleType;
import com.aspire.asat.registration.data.invoice.InvoiceRequestDTO;
import com.aspire.asat.registration.data.invoice.InvoiceResponseDTO;
import com.aspire.asat.registration.data.invoice.InvoiceStatus;
import com.aspire.asat.registration.service.pricing.ProductPriceResolver;
import com.aspire.asat.registration.exception.ClientActivationException;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.model.UserSuspendReason;
import com.aspire.asat.registration.model.dropdown.Country;
import com.aspire.asat.registration.model.msp.MspProduct;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.UserLicenceRepository;
import com.aspire.asat.registration.repository.UserLoginHistoryRepository;
import com.aspire.asat.registration.repository.UserSuspendReasonRepository;
import com.aspire.asat.registration.repository.custom.ClientAdminRepositoryCustom;
import com.aspire.asat.registration.repository.custom.ClientProductRepositoryCustom;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.repository.dropdown.IndustryRepository;
import com.aspire.asat.registration.repository.dropdown.SubIndustryRepository;
import com.aspire.asat.registration.repository.dropdown.LanguageRepository;
import com.aspire.asat.registration.repository.dropdown.OrganizationSizeRepository;
import com.aspire.asat.registration.repository.dropdown.StateRepository;
import com.aspire.asat.registration.repository.dropdown.TimezoneRepository;
import com.aspire.asat.registration.repository.metadata.OrganizationTypeRepository;
import com.aspire.asat.registration.repository.msp.MspProductRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.ActivityLogService;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.ClientAdminService;
import com.aspire.asat.registration.service.branding.BrandingMapping;
import com.aspire.asat.registration.service.external.BillingService;
import com.aspire.asat.registration.service.support.SimulationProductResolver;
import com.aspire.asat.registration.service.support.UserSessionInvalidationHelper;
import com.aspire.asat.registration.utils.DefaultMspData;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientAdminServiceImpl implements ClientAdminService {

    private static final int EXPIRING_SOON_DAYS = 30;

    @Value("${service.billing.url}")
    private String billingServiceUrl;

    @Value("${service.cms.url}")
    private String cmsServiceUrl;

    @Value("${link.logo}")
    private String logoUrl;

    @Value("${link.help}")
    private String helpUrl;

    @Value("${link.login}")
    private String logInUrl;

    @Value("${link.support-email}")
    private String supportEmail;

    private final ClientAdminRepository clientAdminRepository;
    private final ClientProductRepository clientProductRepository;
    private final AspireUserRepository aspireUserRepository;
    private final ClientAdminRepositoryCustom clientAdminRepositoryCustom;
    private final ClientProductRepositoryCustom clientProductRepositoryCustom;
    private final UserLicenceRepository userLicenceRepository;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final TimezoneRepository timezoneRepository;
    private final LanguageRepository languageRepository;
    private final IndustryRepository industryRepository;
    private final SubIndustryRepository subIndustryRepository;
    private final OrganizationSizeRepository organizationSizeRepository;
    private final OrganizationTypeRepository organizationTypeRepository;
    private final WebClient webClient;
    @Autowired
    private ObjectMapper objectMapper;
    private final FileService fileService;
    private final RegistrationNotificationClient notificationServiceClient;
    private final AspireUserService aspireUserService;
    private final CmsServiceClient cmsServiceClient;
    private final MspUsersRepository mspUsersRepository;
    private final MspProductRepository mspProductRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final ActivityLogService activityLogService;
    private final HttpServletRequest httpServletRequest;
    private final UserLoginHistoryRepository userLoginHistoryRepository;
    private final InvoiceGenerator invoiceGenerator;
    private final BillingService billingService;
    private final com.aspire.asat.registration.service.external.InvoiceService invoiceService;
    private final UserSuspendReasonRepository userSuspendReasonRepository;
    private final com.aspire.asat.registration.service.dropdown.SuspendReasonService suspendReasonService;
    private final ProductPriceResolver productPriceResolver;
    private final UserSessionInvalidationHelper userSessionInvalidationHelper;
    private final SimulationProductResolver simulationProductResolver;
    private final PhishingServiceClient phishingServiceClient;

    @Value("${aws.s3.bucket-name}")
    private String s3BucketName;

    /**
     * Inner class to hold coupon discount information
     */
    private static class CouponDiscountInfo {
        private String couponId;
        private String couponName;
        private String couponCode;
        private Double discountAmount;
        private String discountType; // "PERCENTAGE" or "FIXED"

        public CouponDiscountInfo(String couponId, String couponName, String couponCode, Double discountAmount, String discountType) {
            this.couponId = couponId;
            this.couponName = couponName;
            this.couponCode = couponCode;
            this.discountAmount = discountAmount;
            this.discountType = discountType;
        }

        public String getCouponId() { return couponId; }
        public String getCouponName() { return couponName; }
        public String getCouponCode() { return couponCode; }
        public Double getDiscountAmount() { return discountAmount; }
        public String getDiscountType() { return discountType; }
    }

    /**
     * Wrapper class to hold both InvoiceRequestDTO and CouponDiscountInfo
     */
    private static class InvoiceRequestWithCoupon {
        private InvoiceRequestDTO invoiceRequestDTO;
        private CouponDiscountInfo couponDiscountInfo;

        public InvoiceRequestWithCoupon(InvoiceRequestDTO invoiceRequestDTO, CouponDiscountInfo couponDiscountInfo) {
            this.invoiceRequestDTO = invoiceRequestDTO;
            this.couponDiscountInfo = couponDiscountInfo;
        }

        public InvoiceRequestDTO getInvoiceRequestDTO() { return invoiceRequestDTO; }
        public CouponDiscountInfo getCouponDiscountInfo() { return couponDiscountInfo; }
    }

    private void validateOnboardingRequest(ClientOnboardingRequestDto dto) {
        if (dto == null) {
            throw new RegistationValidationException("Onboarding request is required");
        }
        if (dto.getOrganization() == null) {
            throw new RegistationValidationException("Organization details are required");
        }
        if (dto.getBilling() == null) {
            throw new RegistationValidationException("Billing details are required");
        }
        if (dto.getMspId() == null || dto.getMspId().isBlank()) {
            throw new RegistationValidationException("MSP ID is required");
        }
        if (dto.getMspName() == null || dto.getMspName().isBlank()) {
            throw new RegistationValidationException("MSP name is required");
        }
        if (dto.getProductSelections() == null || dto.getProductSelections().isEmpty()) {
            throw new RegistationValidationException("At least one product selection is required");
        }
        for (int i = 0; i < dto.getProductSelections().size(); i++) {
            if (dto.getProductSelections().get(i) == null) {
                throw new RegistationValidationException("Product selection at index " + i + " must not be null");
            }
        }
        if (dto.getInvoice() == null) {
            throw new RegistationValidationException("Invoice details are required");
        }
    }

    private void validateBuyNowRequest(BuyNowRequestDto dto) {
        if (dto == null) {
            throw new RegistationValidationException("Buy now request is required");
        }
        if (dto.getClientAdminId() == null || dto.getClientAdminId().isBlank()) {
            throw new RegistationValidationException("Client admin ID is required");
        }
        if (dto.getProductSelections() == null || dto.getProductSelections().isEmpty()) {
            throw new RegistationValidationException("At least one product selection is required");
        }
        for (int i = 0; i < dto.getProductSelections().size(); i++) {
            if (dto.getProductSelections().get(i) == null) {
                throw new RegistationValidationException("Product selection at index " + i + " must not be null");
            }
        }
        if (dto.getInvoice() == null) {
            throw new RegistationValidationException("Invoice details are required");
        }
    }

    private void validateReassignmentRequest(ClientProductAssignment dto) {
        if (dto == null) {
            throw new RegistationValidationException("Product reassignment request is required");
        }
        if (dto.getClientAdminId() == null || dto.getClientAdminId().isBlank()) {
            throw new RegistationValidationException("Client admin ID is required");
        }
        if (dto.getProductSelections() == null || dto.getProductSelections().isEmpty()) {
            throw new RegistationValidationException("At least one product selection is required");
        }
        for (int i = 0; i < dto.getProductSelections().size(); i++) {
            if (dto.getProductSelections().get(i) == null) {
                throw new RegistationValidationException("Product selection at index " + i + " must not be null");
            }
        }
        if (dto.getInvoice() == null) {
            throw new RegistationValidationException("Invoice details are required");
        }
    }

    /**
     * Best-effort billing invoice delete during compensating rollback; does not throw.
     */
    private void tryDeleteBillingInvoice(String invoiceId) {
        if (invoiceId == null || invoiceId.isBlank()) {
            return;
        }
        try {
            webClient.delete()
                    .uri(billingServiceUrl + "/invoice/delete/" + invoiceId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Deleted billing invoice {} during rollback", invoiceId);
        } catch (Exception e) {
            log.warn("Failed to delete billing invoice {} during rollback: {}", invoiceId, e.getMessage());
        }
    }

    private void deleteClientProductsByIds(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        for (String id : productIds) {
            try {
                clientProductRepository.deleteById(id);
            } catch (Exception e) {
                log.error("Failed to delete ClientProduct {} during rollback: {}", id, e.getMessage());
            }
        }
    }

    private static RegistrationServiceException wrapAsRegistrationException(String context, Exception e) {
        if (e instanceof RegistrationServiceException) {
            return (RegistrationServiceException) e;
        }
        return new RegistrationServiceException(context + ": " + e.getMessage(), e);
    }

    @Override
    @Transactional
    public ClientOnboardingResponseDto processClientOnboarding(ClientOnboardingRequestDto requestDto) {
        validateOnboardingRequest(requestDto);
        OrganizationInfoDto org = requestDto.getOrganization();
        BillingInfoDto billing = requestDto.getBilling();
        List<ProductSelectionDto> selections = requestDto.getProductSelections();
        InvoiceDetailsDto invoice = requestDto.getInvoice();

        String adminEmail = org.getAdminEmail();
        log.info("Processing onboarding for admin email: {}", adminEmail);

        emailValidation(adminEmail);

        String adminId = UUID.randomUUID().toString();
        String organizationId = UUID.randomUUID().toString();

        ClientAdmin clientAdmin = buildClientAdmin(adminId, org, billing, requestDto.getMspId(), requestDto.getMspName());

        productPriceResolver.applyAuthoritativePricing(selections);
        List<String> clientProductIds = createClientProducts(selections, invoice, adminId, org.getCountry(), "Invoice auto-generated during onboarding", requestDto.getMspId());
        clientAdmin.setClientProductIds(clientProductIds);
        clientAdminRepository.save(clientAdmin);

        AspireUserDto aspireUserRecord;
        try {
            aspireUserRecord = createAspireUserRecord(clientAdmin);
        } catch (Exception e) {
            log.error("AspireUser creation failed during onboarding: {}", e.getMessage(), e);
            performManualRollback(adminId, clientProductIds, null, null);
            throw e;
        }
        UUID aspireUserId = aspireUserRecord.getBaseUserId() != null ? aspireUserRecord.getBaseUserId() : null;

        log.info("Temporary User DTO: {}", aspireUserRecord);

        ClientOnboardingResponseDto response = buildOnboardingResponse(adminId, adminEmail, aspireUserRecord.getPlainPassword(), organizationId, org);

        InvoiceRequestWithCoupon invoiceRequestWithCoupon = buildInvoiceRequestDTOWithCoupon(adminId, clientProductIds, invoice, org, billing, selections, requestDto.getMspId(), requestDto.getMspName(), "Auto-generated invoice during onboarding");
        InvoiceRequestDTO invoiceRequestDTO = invoiceRequestWithCoupon.getInvoiceRequestDTO();
        CouponDiscountInfo couponInfo = invoiceRequestWithCoupon.getCouponDiscountInfo();

        log.info("****Invoice request DTO: {}", invoiceRequestDTO);

        InvoiceResponseDTO invoiceResponse;
        try {
            invoiceResponse = createInvoice(invoiceRequestDTO);
        } catch (Exception e) {
            log.error("Invoice creation failed during onboarding: {}", e.getMessage(), e);
            performManualRollback(adminId, clientProductIds, aspireUserId, null);
            throw wrapAsRegistrationException("Invoice creation failed during onboarding", e);
        }

        log.info("Invoice response: {}", invoiceResponse);
        String invoiceId = invoiceResponse != null ? invoiceResponse.getId() : null;

        try {
            updateClientDashboardInCmsService(adminId);
            log.info("CMS dashboard updated successfully. Proceeding with notifications.");
        } catch (Exception e) {
            log.error("CMS dashboard update failed. Performing manual rollback and aborting onboarding. Error: {}", e.getMessage(), e);
            performManualRollback(adminId, clientProductIds, aspireUserId, invoiceId);
            log.warn("CMS dashboard may be out of sync for clientAdminId {} after onboarding rollback; reconcile if needed.", adminId);
            throw new RegistrationServiceException("Failed to update client dashboard in CMS service. Onboarding aborted: " + e.getMessage(), e);
        }

        try {
            sendInvoiceAndNotification(org, aspireUserRecord.getPlainPassword(), response, invoiceRequestDTO, invoiceResponse, couponInfo);
        } catch (Exception e) {
            log.error("Welcome/invoice notification failed during onboarding: {}", e.getMessage(), e);
            performManualRollback(adminId, clientProductIds, aspireUserId, invoiceId);
            log.warn("CMS dashboard may be stale for clientAdminId {} after onboarding rollback; reconcile if needed.", adminId);
            throw wrapAsRegistrationException("Failed to send onboarding notification (invoice PDF or email)", e);
        }

        String mspId = requestDto.getMspId();
        if (mspId != null && !mspId.isBlank()) {
            updateMspProductUsedLicenseCount(mspId, selections);
        }

        return response;
    }

    @Override
    @Transactional
    public BuyNowResponseDto processBuyNow(BuyNowRequestDto requestDto) {
        validateBuyNowRequest(requestDto);
        String clientAdminId = requestDto.getClientAdminId();
        log.info("Processing buy now flow for client admin: {}", clientAdminId);

        ClientAdmin clientAdmin = clientAdminRepository.findById(clientAdminId)
                .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientAdminId));

        if (requestDto.getOrganization() != null) {
            updateClientAdminFromOrganizationInfo(clientAdmin, requestDto.getOrganization());
        }
        if (requestDto.getBilling() != null) {
            updateClientAdminFromBillingInfo(clientAdmin, requestDto.getBilling());
        }
        if (requestDto.getMspId() != null && !requestDto.getMspId().isBlank()) {
            clientAdmin.setMspId(requestDto.getMspId());
        }

        if (requestDto.getMspName() != null && !requestDto.getMspName().isBlank()) {
            clientAdmin.setMspName(requestDto.getMspName());
        }

        List<ProductSelectionDto> selections = requestDto.getProductSelections();
        InvoiceDetailsDto invoice = requestDto.getInvoice();

        String country = requestDto.getOrganization() != null && requestDto.getOrganization().getCountry() != null
                ? requestDto.getOrganization().getCountry()
                : clientAdmin.getCountry();

        if (country == null || country.isBlank()) {
            throw new RegistationValidationException(
                    "Country is required for product creation. Provide organization.country or ensure the client admin has a country set.");
        }

        String mspIdForProducts = requestDto.getMspId() != null && !requestDto.getMspId().isBlank()
                ? requestDto.getMspId()
                : clientAdmin.getMspId();

        productPriceResolver.applyAuthoritativePricing(selections);
        List<String> newClientProductIds = createClientProducts(
                selections, invoice, clientAdminId, country, "Invoice auto-generated during buy now flow", mspIdForProducts);

        String mspId = requestDto.getMspId() != null && !requestDto.getMspId().isBlank()
                ? requestDto.getMspId()
                : clientAdmin.getMspId();
        String mspName = DefaultMspData.getDefaultMspName();

        OrganizationInfoDto orgInfo = requestDto.getOrganization() != null
                ? requestDto.getOrganization()
                : buildOrganizationInfoFromClientAdmin(clientAdmin);

        BillingInfoDto billingInfo = requestDto.getBilling();
        if (billingInfo == null) {
            billingInfo = buildBillingInfoFromClientAdmin(clientAdmin);
        }

        InvoiceRequestDTO invoiceRequestDTO = buildInvoiceRequestDTO(
                clientAdminId,
                newClientProductIds,
                invoice,
                orgInfo,
                billingInfo,
                selections,
                mspId,
                mspName,
                "Auto-generated invoice during buy now flow"
        );

        log.info("Invoice request DTO for buy now: {}", invoiceRequestDTO);

        InvoiceResponseDTO invoiceResponse;
        try {
            invoiceResponse = createInvoice(invoiceRequestDTO);
        } catch (Exception e) {
            log.error("Invoice creation failed during buy now: {}", e.getMessage(), e);
            deleteClientProductsByIds(newClientProductIds);
            throw wrapAsRegistrationException("Invoice creation failed during buy now", e);
        }

        log.info("Invoice created for buy now flow: {}", invoiceResponse);
        String invoiceId = invoiceResponse != null ? invoiceResponse.getId() : null;

        try {
            updateClientDashboardInCmsService(clientAdminId);
            log.info("CMS dashboard updated successfully for buy now flow.");
        } catch (Exception e) {
            log.error("CMS dashboard update failed for buy now flow. Error: {}", e.getMessage(), e);
            tryDeleteBillingInvoice(invoiceId);
            deleteClientProductsByIds(newClientProductIds);
            throw new RegistrationServiceException("Buy now aborted: CMS dashboard update failed: " + e.getMessage(), e);
        }

        try {
            sendBuyNowInvoiceEmail(clientAdmin, invoiceRequestDTO, invoiceResponse);
        } catch (Exception e) {
            log.error("Failed to send invoice email for buy now flow. Error: {}", e.getMessage(), e);
            tryDeleteBillingInvoice(invoiceId);
            deleteClientProductsByIds(newClientProductIds);
            throw wrapAsRegistrationException("Buy now aborted: failed to send invoice email", e);
        }

        List<String> existingProductIds = clientAdmin.getClientProductIds();
        if (existingProductIds == null) {
            existingProductIds = new ArrayList<>();
        } else {
            existingProductIds = new ArrayList<>(existingProductIds);
        }
        existingProductIds.addAll(newClientProductIds);
        clientAdmin.setClientProductIds(existingProductIds);
        clientAdminRepository.save(clientAdmin);

        if (mspId != null && !mspId.isBlank()) {
            updateMspProductUsedLicenseCount(mspId, selections);
        }

        return BuyNowResponseDto.builder()
                .clientAdminId(clientAdminId)
                .adminEmail(clientAdmin.getEmail())
                .organizationName(clientAdmin.getOrganizationName())
                .clientProductIds(newClientProductIds)
                .invoiceResponse(invoiceResponse)
                .portalLink(logInUrl)
                .build();
    }

    private void updateClientAdminFromOrganizationInfo(ClientAdmin clientAdmin, OrganizationInfoDto org) {
        setIfNotNull(org.getOrganizationName(), clientAdmin::setOrganizationName);
        setIfNotNull(org.getOrganizationType(), clientAdmin::setOrganizationType);
        setIfNotNull(org.getContactEmail(), clientAdmin::setContactEmail);
        setIfNotNull(org.getPhoneNumber(), clientAdmin::setPhoneNumber);
        setIfNotNull(org.getPhoneCode(), clientAdmin::setPhoneCode);
        setIfNotNull(org.getCountry(), clientAdmin::setCountry);
        setIfNotNull(org.getStateProvince(), clientAdmin::setState);
        setIfNotNull(org.getTimeZone(), clientAdmin::setTimeZone);
        setIfNotNull(org.getLanguage(), clientAdmin::setLanguage);
        setIfNotNull(org.getIndustry(), clientAdmin::setIndustry);
        setIfNotNull(org.getSubIndustryId(), clientAdmin::setSubIndustryId);
        setIfNotNull(org.getComplianceId(), clientAdmin::setComplianceId);
        setIfNotNull(org.getComplianceName(), clientAdmin::setComplianceName);
        setIfNotNull(org.getDomain(), clientAdmin::setDomain);
        setIfNotNull(org.getOrganizationSize(), clientAdmin::setOrganizationSize);
        setIfNotNull(org.getStreetAddress(), clientAdmin::setStreetAddress);
        setIfNotNull(org.getStreetAddressLine2(), clientAdmin::setStreetAddressLine2);
        setIfNotNull(org.getCity(), clientAdmin::setCity);
        setIfNotNull(org.getZipPostalCode(), clientAdmin::setZipPostalCode);
        setIfNotNull(org.getLogoUrl(), clientAdmin::setLogoUrl);
    }

    private void updateClientAdminFromBillingInfo(ClientAdmin clientAdmin, BillingInfoDto billing) {
        setIfNotNull(billing.getBillingName(), clientAdmin::setBillingName);
        setIfNotNull(billing.getBillingEmail(), clientAdmin::setBillingEmail);
        
        Boolean useSameAsOrgAddress = billing.getUseSameAsOrganizationAddress();
        if (useSameAsOrgAddress != null) {
            clientAdmin.setBillingUseSameAsOrganizationAddress(useSameAsOrgAddress);
            if (useSameAsOrgAddress) {
                // Copy organization address to billing address
                clientAdmin.setBillingStreetAddress(clientAdmin.getStreetAddress());
                clientAdmin.setBillingStreetAddressLine2(clientAdmin.getStreetAddressLine2());
                clientAdmin.setBillingCity(clientAdmin.getCity());
                clientAdmin.setBillingZipPostalCode(clientAdmin.getZipPostalCode());
                clientAdmin.setBillingCountry(clientAdmin.getCountry());
                clientAdmin.setBillingStateProvince(clientAdmin.getState());
            } else {
                // Use billing-specific address fields
                setIfNotNull(billing.getStreetAddress(), clientAdmin::setBillingStreetAddress);
                setIfNotNull(billing.getStreetAddressLine2(), clientAdmin::setBillingStreetAddressLine2);
                setIfNotNull(billing.getCity(), clientAdmin::setBillingCity);
                setIfNotNull(billing.getZipPostalCode(), clientAdmin::setBillingZipPostalCode);
                setIfNotNull(billing.getCountry(), clientAdmin::setBillingCountry);
                setIfNotNull(billing.getStateProvince(), clientAdmin::setBillingStateProvince);
            }
        }
    }

    private OrganizationInfoDto buildOrganizationInfoFromClientAdmin(ClientAdmin clientAdmin) {
        // Build minimal OrganizationInfoDto with required fields for invoice creation
        // Only include fields that are actually used by buildInvoiceRequestDTO
        return OrganizationInfoDto.builder()
                .organizationName(clientAdmin.getOrganizationName() != null ? clientAdmin.getOrganizationName() : "N/A")
                .country(clientAdmin.getCountry() != null ? clientAdmin.getCountry() : "N/A")
                .stateProvince(clientAdmin.getState())
                .build();
    }

    private BillingInfoDto buildBillingInfoFromClientAdmin(ClientAdmin clientAdmin) {
        // Build BillingInfoDto from ClientAdmin billing fields
        return BillingInfoDto.builder()
                .billingName(clientAdmin.getBillingName())
                .billingEmail(clientAdmin.getBillingEmail())
                .useSameAsOrganizationAddress(clientAdmin.isBillingUseSameAsOrganizationAddress())
                .country(clientAdmin.getBillingCountry())
                .stateProvince(clientAdmin.getBillingStateProvince())
                .streetAddress(clientAdmin.getBillingStreetAddress())
                .streetAddressLine2(clientAdmin.getBillingStreetAddressLine2())
                .city(clientAdmin.getBillingCity())
                .zipPostalCode(clientAdmin.getBillingZipPostalCode())
                .build();
    }

    private String getMspName(String mspId) {
        if (mspId == null || mspId.isBlank()) {
            return null;
        }
        try {
            Optional<MspUser> mspUser = mspUsersRepository.findById(mspId);
            return mspUser.map(MspUser::getOrganizationName).orElse(null);
        } catch (Exception e) {
            log.warn("Failed to fetch MSP name for mspId: {}. Error: {}", mspId, e.getMessage());
            return null;
        }
    }

    private void emailValidation(String adminEmail) {
        if (clientAdminRepository.existsByEmailIgnoreCase(adminEmail) ||
                aspireUserService.getUserByEmail(adminEmail).isPresent()) {
            throw new ResourceAlreadyExistsException("A client admin with this email already exists: " + adminEmail);
        }
    }

    private ClientAdmin buildClientAdmin(String adminId, OrganizationInfoDto org, BillingInfoDto billing, String mspId, String mspName) {
        ClientAdmin clientAdmin = new ClientAdmin();
        clientAdmin.setId(adminId);
        clientAdmin.setEmail(org.getAdminEmail());
        clientAdmin.setHashedPassword(null);
        clientAdmin.setOrganizationName(org.getOrganizationName());
        clientAdmin.setContactEmail(org.getContactEmail());
        clientAdmin.setPhoneNumber(org.getPhoneNumber());
        clientAdmin.setPhoneCode(org.getPhoneCode());
        clientAdmin.setBillingName(billing.getBillingName());
        clientAdmin.setBillingEmail(billing.getBillingEmail());
        clientAdmin.setMspId(mspId);
        clientAdmin.setMspName(mspName);
        clientAdmin.setCountry(org.getCountry());
        clientAdmin.setState(org.getStateProvince());
        clientAdmin.setTimeZone(org.getTimeZone());
        clientAdmin.setLanguage(org.getLanguage());
        clientAdmin.setIndustry(org.getIndustry());
        clientAdmin.setSubIndustryId(org.getSubIndustryId());
        clientAdmin.setComplianceId(org.getComplianceId());
        clientAdmin.setComplianceName(org.getComplianceName());
        clientAdmin.setDomain(org.getDomain());
        clientAdmin.setOrganizationSize(org.getOrganizationSize());
        clientAdmin.setOrganizationType(org.getOrganizationType());
        
        // Map organization address fields
        clientAdmin.setStreetAddress(org.getStreetAddress());
        clientAdmin.setStreetAddressLine2(org.getStreetAddressLine2());
        clientAdmin.setCity(org.getCity());
        clientAdmin.setZipPostalCode(org.getZipPostalCode());
        
        clientAdmin.setLogoUrl(org.getLogoUrl());
        
        // Handle billing address fields
        Boolean useSameAsOrgAddress = billing.getUseSameAsOrganizationAddress();
        if (useSameAsOrgAddress != null && useSameAsOrgAddress) {
            // Copy organization address to billing address
            clientAdmin.setBillingUseSameAsOrganizationAddress(true);
            clientAdmin.setBillingStreetAddress(org.getStreetAddress());
            clientAdmin.setBillingStreetAddressLine2(org.getStreetAddressLine2());
            clientAdmin.setBillingCity(org.getCity());
            clientAdmin.setBillingZipPostalCode(org.getZipPostalCode());
            clientAdmin.setBillingCountry(org.getCountry());
            clientAdmin.setBillingStateProvince(org.getStateProvince());
        } else {
            // Use billing-specific address fields
            clientAdmin.setBillingUseSameAsOrganizationAddress(false);
            clientAdmin.setBillingStreetAddress(billing.getStreetAddress());
            clientAdmin.setBillingStreetAddressLine2(billing.getStreetAddressLine2());
            clientAdmin.setBillingCity(billing.getCity());
            clientAdmin.setBillingZipPostalCode(billing.getZipPostalCode());
            clientAdmin.setBillingCountry(billing.getCountry());
            clientAdmin.setBillingStateProvince(billing.getStateProvince());
        }
        
        clientAdmin.setStatus(AdminStatus.PENDING);
        clientAdmin.setCreatedAt(Instant.now());
        clientAdmin.setClientAdminId(adminId); // Set clientAdminId to same as id
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        // Set onboardBy based on whether UserType
        if (UserType.MSP.name().equals(userType)) {
            clientAdmin.setMspId(userContext.getUserId());
            clientAdmin.setOnboardBy(OnboardBy.MSP);
        } else {
            clientAdmin.setOnboardBy(OnboardBy.ASPIRE_ADMIN);
        }
        
        return clientAdmin;
    }

    private List<String> createClientProducts(List<ProductSelectionDto> selections, InvoiceDetailsDto invoice, String adminId, String country) {
        return createClientProducts(selections, invoice, adminId, country, "Invoice auto-generated during onboarding", null);
    }

    /**
     * Creates ClientProduct records for each product selection.
     * This is the core method that handles product creation with customizable notes.
     *
     * @param selections Product selections from the request
     * @param invoice Invoice details
     * @param adminId Client admin ID
     * @param country Country for region (stored as countryId on ClientProduct)
     * @param notes Custom notes for the payment payload
     * @param mspId MSP admin ID (optional, stored on ClientProduct)
     * @return List of created client product IDs
     */
    private List<String> createClientProducts(List<ProductSelectionDto> selections, InvoiceDetailsDto invoice, String adminId, String country, String notes, String mspId) {
        List<String> clientProductIds = new ArrayList<>();
        for (ProductSelectionDto selection : selections) {
            EmbeddedPackageItem item = new EmbeddedPackageItem();
            item.setPackageId(selection.getPackageId());
            item.setPrice(selection.getPricePerLicense());

            EmbeddedPaymentPayload payload = new EmbeddedPaymentPayload();
            payload.setClientId(adminId);
            payload.setCurrency("USD");
            payload.setAmount(MoneyUtil.round(invoice.getTotalAmount()));
            payload.setSubtotal(MoneyUtil.round(invoice.getSubtotal()));
            payload.setVatAmount(MoneyUtil.round(invoice.getVatAmount()));
            payload.setDiscountAmount(MoneyUtil.round(invoice.getDiscountAmount()));
            payload.setDiscountPercentage(invoice.getDiscountPercentage());
            payload.setDate(Date.from(Instant.now()));
            payload.setNotes(notes);
            payload.setClientRegion(country);
            payload.setPackageItems(List.of(item));
            payload.setPaymentSources(List.of());

            ClientProduct clientProduct = new ClientProduct();
            clientProduct.setId(UUID.randomUUID().toString());
            clientProduct.setClientAdminId(adminId);
            clientProduct.setProductId(selection.getProductId());
            clientProduct.setPackageId(selection.getPackageId());
            clientProduct.setLicenseCount(selection.getLicenseCount());
            clientProduct.setPricePerLicense(selection.getPricePerLicense());
            clientProduct.setTotalPrice(MoneyUtil.round(selection.getLicenseCount() * selection.getPricePerLicense()));
            clientProduct.setValidityPeriod(selection.getValidityPeriod());
            clientProduct.setValidityUnit(selection.getValidityUnit().toString());
            clientProduct.setAssignedAt(Instant.now());
            clientProduct.setExpiryDate(calculateExpiryDate(Instant.now(), selection.getValidityPeriod(), selection.getValidityUnit().toString()));
            clientProduct.setLicenseStatus("PENDING");
            clientProduct.setPaymentPayload(payload);
            clientProduct.setCountryId(country);
            clientProduct.setMspId(mspId);
            clientProductRepository.save(clientProduct);

            clientProductIds.add(clientProduct.getId());
        }
        return clientProductIds;
    }

    private AspireUserDto createAspireUserRecord(ClientAdmin clientAdmin) {
        try {
            AspireUserDto tmpUserDto = aspireUserService.createAspireUser(
                    clientAdmin.getId(),
                    UserType.CLIENT_ADMIN,
                    clientAdmin
            );
            log.info("Created AspireUser record for ClientAdmin with ID: {}", clientAdmin.getId());
            return tmpUserDto;
        } catch (RegistrationServiceException e) {
            log.error("Failed to create AspireUser record for ClientAdmin with ID: {}: {}", clientAdmin.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to create AspireUser record for ClientAdmin with ID: {}", clientAdmin.getId(), e);
            throw new RegistrationServiceException(
                    "Failed to create AspireUser for client admin. Cannot complete onboarding: " + e.getMessage(), e);
        }
    }

    private ClientOnboardingResponseDto buildOnboardingResponse(String adminId, String adminEmail, String tempPassword, String organizationId, OrganizationInfoDto org) {
        ClientOnboardingResponseDto response = new ClientOnboardingResponseDto();
        response.setAdminId(adminId);
        response.setAdminEmail(adminEmail);
        response.setTempPassword(tempPassword);
        response.setOrganizationId(organizationId);
        response.setOrganizationName(org.getOrganizationName());
        response.setPortalLink(logInUrl);
        return response;
    }

    private InvoiceRequestDTO buildInvoiceRequestDTO(String adminId, List<String> clientProductIds, InvoiceDetailsDto invoice, OrganizationInfoDto org, BillingInfoDto billing, List<ProductSelectionDto> selections, String mspId, String mspName) {
        return buildInvoiceRequestDTO(adminId, clientProductIds, invoice, org, billing, selections, mspId, mspName, "Auto-generated invoice during onboarding");
    }

    private InvoiceRequestDTO buildInvoiceRequestDTO(String adminId, List<String> clientProductIds, InvoiceDetailsDto invoice, OrganizationInfoDto org, BillingInfoDto billing, List<ProductSelectionDto> selections, String mspId, String mspName, String statusNote) {
        InvoiceRequestWithCoupon result = buildInvoiceRequestDTOWithCoupon(adminId, clientProductIds, invoice, org, billing, selections, mspId, mspName, statusNote);
        return result.getInvoiceRequestDTO();
    }

    private InvoiceRequestWithCoupon buildInvoiceRequestDTOWithCoupon(String adminId, List<String> clientProductIds, InvoiceDetailsDto invoice, OrganizationInfoDto org, BillingInfoDto billing, List<ProductSelectionDto> selections, String mspId, String mspName, String statusNote) {

        // Validate invoice total price against calculated price from CMS
        double calculatedSubtotal = validateInvoiceTotalPrice(invoice.getSubtotal(), selections);
        
        // Get countryId and stateId from billing info (needed for coupon validation and VAT calculation)
        // Handle useSameAsOrganizationAddress flag - if true, use organization country/state
        String countryId = null;
        String stateId = null;
        
        if (billing.getUseSameAsOrganizationAddress() != null && billing.getUseSameAsOrganizationAddress()) {
            // If billing uses same as organization, use organization country/state
            countryId = org.getCountry();
            stateId = org.getStateProvince();
        } else {
            // Use billing-specific country/state
            countryId = billing.getCountry();
            stateId = billing.getStateProvince();
        }
        
        // Fallback to organization country/state if billing country/state is null or empty
        if (countryId == null || countryId.isBlank()) {
            countryId = org.getCountry();
            stateId = org.getStateProvince();
            log.warn("Billing country is null or empty, falling back to organization country: {}", countryId);
        }
        
        // Validate and calculate coupon discount if coupon code exists
        CouponDiscountInfo couponInfo = null;
        double subtotalAfterCoupon = calculatedSubtotal;
        String couponCode = invoice.getCouponCode();
        
        if (couponCode != null && !couponCode.isBlank()) {
            try {
                couponInfo = validateAndCalculateCouponDiscount(couponCode, calculatedSubtotal, selections, countryId);
                if (couponInfo != null) {
                    subtotalAfterCoupon = MoneyUtil.round(calculatedSubtotal - couponInfo.getDiscountAmount());
                    log.info("Applied coupon discount: {} ({}). Subtotal after coupon: {}", 
                        couponInfo.getDiscountAmount(), couponInfo.getDiscountType(), subtotalAfterCoupon);
                }
            } catch (RegistrationServiceException e) {
                log.error("Coupon validation failed: {}", e.getMessage());
                throw e;
            }
        }
        
        // Apply invoice discount on subtotal after coupon discount
        double discountPercentage = invoice.getDiscountPercentage() != null ? invoice.getDiscountPercentage() : 0.0;
        // Calculate discount amount from percentage applied to subtotal after coupon
        double discount = MoneyUtil.round(subtotalAfterCoupon * (discountPercentage / 100.0));
        
        // Calculate discounted subtotal (after both coupon and invoice discount are applied)
        double discountedSubtotal = MoneyUtil.round(subtotalAfterCoupon - discount);
        
        // Calculate VAT on discounted subtotal using billing service VAT API
        double vatRate = 0.0;
        double calculatedVat = 0.0;
        
        // Validate countryId is provided
        if (countryId == null || countryId.isBlank()) {
            throw new RegistrationServiceException("Country ID is required for VAT calculation. Please provide country in billing information or organization information.");
        }
        
        // Handle zero or negative discounted subtotal
        if (discountedSubtotal <= 0) {
            log.warn("Discounted subtotal is zero or negative: {}. Setting VAT to 0.0", discountedSubtotal);
            calculatedVat = 0.0;
        } else {
            // Call billing service VAT API to get VAT rate
            try {
                vatRate = getVatRateFromBillingService(countryId, stateId);
                // Calculate VAT on discounted subtotal
                calculatedVat = MoneyUtil.round(discountedSubtotal * (vatRate / 100.0));
                log.info("Calculated VAT: {} from rate: {}% on discounted subtotal: {}", 
                    calculatedVat, vatRate, discountedSubtotal);
            } catch (RegistrationServiceException e) {
                // Re-throw RegistrationServiceException as-is
                throw e;
            } catch (Exception e) {
                log.error("Failed to get VAT rate from billing service for countryId: {}, stateId: {}. Error: {}", 
                    countryId, stateId, e.getMessage(), e);
                throw new RegistrationServiceException(
                    "Failed to calculate VAT. VAT configuration not found or billing service unavailable: " + e.getMessage(), e);
            }
        }
        
        calculatedVat = MoneyUtil.round(calculatedVat);
        double expectedTotal = MoneyUtil.round(discountedSubtotal + calculatedVat);

        InvoiceRequestDTO invoiceRequestDTO = new InvoiceRequestDTO();
        invoiceRequestDTO.setClientAdminId(adminId);
        invoiceRequestDTO.setMspAdminId(mspId);
        invoiceRequestDTO.setMspName(mspName);
        invoiceRequestDTO.setClientProductIds(clientProductIds);
        invoiceRequestDTO.setSubtotal(calculatedSubtotal);
        invoiceRequestDTO.setDiscountType(invoice.getDiscountType());
        invoiceRequestDTO.setDiscountAmount(discount);
        invoiceRequestDTO.setDiscountPercentage(discountPercentage);
        invoiceRequestDTO.setCouponCode(couponCode);
        invoiceRequestDTO.setCouponDiscountAmount(
            couponInfo != null && couponInfo.getDiscountAmount() != null 
                ? MoneyUtil.round(couponInfo.getDiscountAmount()) 
                : 0.0
        );
        invoiceRequestDTO.setVatAmount(calculatedVat); // Use backend calculated VAT instead of frontend VAT
        invoiceRequestDTO.setVatRate(vatRate); // Store VAT rate used for calculation (preserves historical accuracy)
        invoiceRequestDTO.setTotalAmount(expectedTotal);
        invoiceRequestDTO.setClientName(org.getOrganizationName());
        invoiceRequestDTO.setStatusNote(statusNote);
        invoiceRequestDTO.setProductSelections(selections);
        // Use billing country/state for invoice DTO (same as VAT calculation)
        String countryName = getCountryNameById(countryId);
        invoiceRequestDTO.setCountryName(countryName != null ? countryName : countryId);
        invoiceRequestDTO.setCountryId(countryId);
        invoiceRequestDTO.setStateName(stateId);
        invoiceRequestDTO.setStateCode(getStateCode(countryName != null ? countryName : countryId, stateId));
        invoiceRequestDTO.setStateId(stateId);
        invoiceRequestDTO.setInvoicePdfLink("NA");

        RoleType roleType = null;
        roleType = RoleType.CLIENT;
        invoiceRequestDTO.setRoleType(roleType);
        
        // Set optional billing email from billing info (may be null)
        invoiceRequestDTO.setBillingEmail(billing != null ? billing.getBillingEmail() : null);
        
        // Set payment status and completed payment details
        invoiceRequestDTO.setPaymentStatus(invoice.getPaymentStatus());
        invoiceRequestDTO.setCompletedPayment(invoice.getCompletedPayment());
        
        // Populate userName and userRole in commentLog if present
        // Create a new CommentLogRequestDTO with user context to ensure proper object reference
        if (invoiceRequestDTO.getCompletedPayment() != null && invoiceRequestDTO.getCompletedPayment().getCommentLog() != null) {
            try {
                CommentLogRequestDTO originalCommentLog = invoiceRequestDTO.getCompletedPayment().getCommentLog();
                
                // Get user context
                String userName = "NoUser"; // Default hardcoded value
                String userRole = "NoRole"; // Default hardcoded value
                try {
                    CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
                    userName = userContext.getUsername();
                    userRole = userContext.getUserType();
                    log.debug("Retrieved user context: userName={}, userRole={}", userName, userRole);
                } catch (Exception e) {
                    log.warn("Could not get user context, using hardcoded values. Error: {}", e.getMessage());
                }
                
                // Create a new CommentLogRequestDTO with all fields including userName and userRole
                // Note: invoiceId is not set here - it will be set by billing service after invoice creation
                CommentLogRequestDTO commentLogWithUserContext = CommentLogRequestDTO.builder()
                        .invoiceId(null) // Will be set by billing service after invoice creation
                        .comment(originalCommentLog.getComment())
                        .actionTakenId(originalCommentLog.getActionTakenId())
                        .nextStepId(originalCommentLog.getNextStepId())
                        .userName(userName)
                        .userRole(userRole)
                        .build();
                
                // Set the new commentLog object back to completedPayment
                invoiceRequestDTO.getCompletedPayment().setCommentLog(commentLogWithUserContext);
                
                log.debug("Populated user context in commentLog: userName={}, userRole={}", userName, userRole);
            } catch (Exception e) {
                log.error("Failed to set user context for comment log. Error: {}", e.getMessage(), e);
                // Continue without user context - billing service will handle fallback
            }
        }
        
        return new InvoiceRequestWithCoupon(invoiceRequestDTO, couponInfo);
    }

    /**
     * Builds an InvoiceRequestDTO for product reassignment using ClientAdmin data.
     * Similar to buildInvoiceRequestDTO but uses ClientAdmin instead of OrganizationInfoDto.
     *
     * @param clientAdminId Client admin ID
     * @param clientProductIds List of new client product IDs
     * @param invoice Invoice details from the request
     * @param clientAdmin The existing ClientAdmin entity
     * @param selections Product selections for the reassignment
     * @param mspId MSP ID for the client
     * @return Populated InvoiceRequestDTO
     */
    private InvoiceRequestDTO buildInvoiceRequestDTOForReassignment(
            String clientAdminId,
            List<String> clientProductIds,
            InvoiceDetailsDto invoice,
            ClientAdmin clientAdmin,
            List<ProductSelectionDto> selections,
            String mspId) {
        InvoiceRequestDTO invoiceRequestDTO = new InvoiceRequestDTO();
        invoiceRequestDTO.setClientAdminId(clientAdminId);
        invoiceRequestDTO.setMspAdminId(mspId);
        
        // Fetch MSP name from MspUser repository
        String mspName = null;
        if (mspId != null && !mspId.isBlank()) {
            try {
                Optional<MspUser> mspUser = mspUsersRepository.findById(mspId);
                if (mspUser.isPresent()) {
                    mspName = mspUser.get().getOrganizationName();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch MSP name for mspId: {}. Error: {}", mspId, e.getMessage());
            }
        }
        invoiceRequestDTO.setMspName(mspName);
        
        invoiceRequestDTO.setClientProductIds(clientProductIds);

        double calculatedSubtotal = validateInvoiceTotalPrice(invoice.getSubtotal(), selections);

        // Get countryId and stateId from billing address (needed for coupon validation and VAT calculation)
        // Handle useSameAsOrganizationAddress flag - if true, use organization country/state
        String countryId = null;
        String stateId = null;
        
        if (clientAdmin.isBillingUseSameAsOrganizationAddress()) {
            // If billing uses same as organization, use organization country/state
            countryId = clientAdmin.getCountry();
            stateId = clientAdmin.getState();
        } else {
            // Use billing-specific country/state
            countryId = clientAdmin.getBillingCountry();
            stateId = clientAdmin.getBillingStateProvince();
        }
        
        // Fallback to organization country/state if billing country/state is null or empty
        if (countryId == null || countryId.isBlank()) {
            countryId = clientAdmin.getCountry();
            stateId = clientAdmin.getState();
            log.warn("Billing country is null or empty for reassignment, falling back to organization country: {}", countryId);
        }
        
        // Validate countryId is provided
        if (countryId == null || countryId.isBlank()) {
            throw new RegistrationServiceException("Country ID is required for VAT calculation. Client admin must have a country set in billing information or organization information.");
        }
        
        // Validate and calculate coupon discount if coupon code exists
        CouponDiscountInfo couponInfo = null;
        double subtotalAfterCoupon = calculatedSubtotal;
        String couponCode = invoice.getCouponCode();
        
        if (couponCode != null && !couponCode.isBlank()) {
            try {
                couponInfo = validateAndCalculateCouponDiscount(couponCode, calculatedSubtotal, selections, countryId);
                if (couponInfo != null) {
                    subtotalAfterCoupon = MoneyUtil.round(calculatedSubtotal - couponInfo.getDiscountAmount());
                    log.info("Applied coupon discount for reassignment: {} ({}). Subtotal after coupon: {}", 
                        couponInfo.getDiscountAmount(), couponInfo.getDiscountType(), subtotalAfterCoupon);
                }
            } catch (RegistrationServiceException e) {
                log.error("Coupon validation failed for reassignment: {}", e.getMessage());
                throw e;
            }
        }
        
        // Apply invoice discount on subtotal after coupon discount
        double discountPercentage = invoice.getDiscountPercentage() != null ? invoice.getDiscountPercentage() : 0.0;
        // Calculate discount amount from percentage applied to subtotal after coupon
        double discount = MoneyUtil.round(subtotalAfterCoupon * (discountPercentage / 100.0));
        
        // Calculate discounted subtotal (after both coupon and invoice discount are applied)
        double discountedSubtotal = MoneyUtil.round(subtotalAfterCoupon - discount);
        
        // Calculate VAT on discounted subtotal using billing service VAT API
        double vatRate = 0.0;
        double calculatedVat = 0.0;
        
        // Handle zero or negative discounted subtotal
        if (discountedSubtotal <= 0) {
            log.warn("Discounted subtotal is zero or negative: {}. Setting VAT to 0.0", discountedSubtotal);
            calculatedVat = 0.0;
        } else {
            // Call billing service VAT API to get VAT rate
            try {
                vatRate = getVatRateFromBillingService(countryId, stateId);
                // Calculate VAT on discounted subtotal
                calculatedVat = MoneyUtil.round(discountedSubtotal * (vatRate / 100.0));
                log.info("Calculated VAT for reassignment: {} from rate: {}% on discounted subtotal: {}", 
                    calculatedVat, vatRate, discountedSubtotal);
            } catch (RegistrationServiceException e) {
                // Re-throw RegistrationServiceException as-is
                throw e;
            } catch (Exception e) {
                log.error("Failed to get VAT rate from billing service for reassignment. countryId: {}, stateId: {}. Error: {}", 
                    countryId, stateId, e.getMessage(), e);
                throw new RegistrationServiceException(
                    "Failed to calculate VAT for reassignment. VAT configuration not found or billing service unavailable: " + e.getMessage(), e);
            }
        }
        
        calculatedVat = MoneyUtil.round(calculatedVat);
        double expectedTotal = MoneyUtil.round(discountedSubtotal + calculatedVat);
        
        invoiceRequestDTO.setSubtotal(calculatedSubtotal);
        invoiceRequestDTO.setDiscountType(invoice.getDiscountType());
        invoiceRequestDTO.setDiscountAmount(discount);
        invoiceRequestDTO.setDiscountPercentage(discountPercentage);
        invoiceRequestDTO.setCouponCode(couponCode);
        invoiceRequestDTO.setCouponDiscountAmount(
            couponInfo != null && couponInfo.getDiscountAmount() != null 
                ? MoneyUtil.round(couponInfo.getDiscountAmount()) 
                : 0.0
        );
        invoiceRequestDTO.setVatAmount(calculatedVat); // Use backend calculated VAT instead of frontend VAT
        invoiceRequestDTO.setVatRate(vatRate); // Store VAT rate used for calculation (preserves historical accuracy)
        invoiceRequestDTO.setTotalAmount(expectedTotal);
        invoiceRequestDTO.setClientName(clientAdmin.getOrganizationName());
        invoiceRequestDTO.setStatusNote("Product reassignment - additional products");
        invoiceRequestDTO.setProductSelections(selections);
        // Use billing country/state for invoice DTO (same as VAT calculation)
        String countryName = getCountryNameById(countryId);
        invoiceRequestDTO.setCountryName(countryName != null ? countryName : countryId);
        invoiceRequestDTO.setCountryId(countryId);
        invoiceRequestDTO.setStateName(stateId);
        invoiceRequestDTO.setStateCode(getStateCode(countryName != null ? countryName : countryId, stateId));
        invoiceRequestDTO.setStateId(stateId);
        invoiceRequestDTO.setInvoicePdfLink("NA");

        RoleType roleType = null;
        roleType = RoleType.CLIENT;
        invoiceRequestDTO.setRoleType(roleType);
        
        // Set optional billing email from client admin (may be null)
        invoiceRequestDTO.setBillingEmail(clientAdmin != null ? clientAdmin.getBillingEmail() : null);
        
        // Set payment status and completed payment details
        invoiceRequestDTO.setPaymentStatus(invoice.getPaymentStatus());
        invoiceRequestDTO.setCompletedPayment(invoice.getCompletedPayment());
        
        // Populate userName and userRole in commentLog if present
        // Create a new CommentLogRequestDTO with user context to ensure proper object reference
        if (invoiceRequestDTO.getCompletedPayment() != null && invoiceRequestDTO.getCompletedPayment().getCommentLog() != null) {
            try {
                CommentLogRequestDTO originalCommentLog = invoiceRequestDTO.getCompletedPayment().getCommentLog();
                
                // Get user context
                String userName = "TestingUser"; // Default hardcoded value
                String userRole = "TestingRole"; // Default hardcoded value
                try {
                    CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
                    userName = userContext.getUsername();
                    userRole = userContext.getUserType();
                    log.debug("Retrieved user context: userName={}, userRole={}", userName, userRole);
                } catch (Exception e) {
                    log.warn("Could not get user context, using hardcoded values. Error: {}", e.getMessage());
                }
                
                // Create a new CommentLogRequestDTO with all fields including userName and userRole
                // Note: invoiceId is not set here - it will be set by billing service after invoice creation
                CommentLogRequestDTO commentLogWithUserContext = CommentLogRequestDTO.builder()
                        .invoiceId(null) // Will be set by billing service after invoice creation
                        .comment(originalCommentLog.getComment())
                        .actionTakenId(originalCommentLog.getActionTakenId())
                        .nextStepId(originalCommentLog.getNextStepId())
                        .userName(userName)
                        .userRole(userRole)
                        .build();
                
                // Set the new commentLog object back to completedPayment
                invoiceRequestDTO.getCompletedPayment().setCommentLog(commentLogWithUserContext);
                
                log.debug("Populated user context in commentLog: userName={}, userRole={}", userName, userRole);
            } catch (Exception e) {
                log.error("Failed to set user context for comment log. Error: {}", e.getMessage(), e);
                // Continue without user context - billing service will handle fallback
            }
        }
        
        return invoiceRequestDTO;
    }

    private InvoiceResponseDTO createInvoice(InvoiceRequestDTO invoiceRequestDTO) {
        return invoiceService.createInvoice(invoiceRequestDTO);
    }

    /**
     * Validates and calculates coupon discount for the given coupon code.
     * 
     * @param couponCode The coupon code to validate and calculate discount for
     * @param calculatedSubtotal The subtotal amount before any discounts
     * @param selections List of product selections to check against product restrictions
     * @param clientCountryId The client's country ID for geographic restriction validation
     * @return CouponDiscountInfo containing coupon details and calculated discount amount
     * @throws RegistrationServiceException if coupon validation fails
     */
    private CouponDiscountInfo validateAndCalculateCouponDiscount(
            String couponCode, 
            double calculatedSubtotal, 
            List<ProductSelectionDto> selections, 
            String clientCountryId) {
        
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }
        
        log.info("Validating and calculating coupon discount for code: {}", couponCode);
        
        try {
            // Get coupon from billing service (availability validated by billing: active, dates, usage limit)
            CouponResponseDTO coupon = billingService.getCouponByCode(couponCode);
            
            if (coupon == null) {
                throw new RegistrationServiceException("Coupon not found with code: " + couponCode);
            }

            // Validate minimum purchase amount
            if (coupon.getMinPurchaseAmount() != null && calculatedSubtotal < coupon.getMinPurchaseAmount()) {
                throw new RegistrationServiceException(
                    String.format("Minimum purchase amount of %.2f not met for coupon: %s",
                        coupon.getMinPurchaseAmount(), couponCode));
            }
            
            // Validate geographic restrictions (if any)
            if (coupon.getGeographicRestrictions() != null && !coupon.getGeographicRestrictions().isEmpty()) {
                if (clientCountryId == null || !coupon.getGeographicRestrictions().contains(clientCountryId)) {
                    throw new RegistrationServiceException("Coupon is not valid in your region: " + couponCode);
                }
            }
            
            // Calculate eligible subtotal (only products matching BOTH productId AND packageId restrictions)
            double eligibleSubtotal = 0.0;
            for (ProductSelectionDto selection : selections) {
                double itemTotal = MoneyUtil.round(
                        selection.getLicenseCount() * selection.getPricePerLicense() * selection.getValidityPeriod());
                
                if (isProductEligibleForCoupon(selection, coupon, clientCountryId)) {
                    eligibleSubtotal = MoneyUtil.round(eligibleSubtotal + itemTotal);
                    log.debug("Product {} (package {}) is eligible for coupon. Item total: {}", 
                        selection.getProductId(), selection.getPackageId(), itemTotal);
                } else {
                    log.debug("Product {} (package {}) is NOT eligible for coupon. Item total: {}", 
                        selection.getProductId(), selection.getPackageId(), itemTotal);
                }
            }
            
            // Validate at least one product is eligible
            if (eligibleSubtotal <= 0) {
                throw new RegistrationServiceException(
                    "Coupon is not valid for any of the selected products/packages: " + couponCode);
            }
            
            log.info("Eligible subtotal for coupon: {} (total subtotal: {})", eligibleSubtotal, calculatedSubtotal);
            
            // Apply discount ONLY to eligible subtotal, not entire calculatedSubtotal
            Double discountAmount;
            String discountType;
            
            if ("PERCENTAGE".equalsIgnoreCase(coupon.getType())) {
                discountType = "PERCENTAGE";
                discountAmount = MoneyUtil.round(eligibleSubtotal * (coupon.getValue() / 100.0));
                log.info("Calculated percentage coupon discount: {}% of eligible {} = {}", 
                    coupon.getValue(), eligibleSubtotal, discountAmount);
            } else if ("FIXED".equalsIgnoreCase(coupon.getType())) {
                discountType = "FIXED";
                discountAmount = MoneyUtil.round(Math.min(coupon.getValue(), eligibleSubtotal));
                log.info("Calculated fixed coupon discount: {}", discountAmount);
            } else {
                throw new RegistrationServiceException("Unsupported coupon type: " + coupon.getType());
            }
            
            // Ensure discount doesn't exceed total subtotal
            if (discountAmount > calculatedSubtotal) {
                discountAmount = MoneyUtil.round(calculatedSubtotal);
                log.warn("Coupon discount exceeded total subtotal, capping at subtotal: {}", discountAmount);
            }
            
            log.info("Coupon validation and discount calculation successful. Code: {}, Discount: {}, Type: {}", 
                couponCode, discountAmount, discountType);
            
            return new CouponDiscountInfo(
                coupon.getId(),
                coupon.getName(),
                coupon.getCode(),
                discountAmount,
                discountType
            );
            
        } catch (RegistrationServiceException e) {
            log.error("Coupon validation failed for code: {}. Error: {}", couponCode, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during coupon validation for code: {}", couponCode, e);
            throw new RegistrationServiceException("Failed to validate coupon: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a product selection is eligible for a coupon discount based on 
     * geographic restrictions and product/package restrictions.
     * 
     * @param selection The product selection to check
     * @param coupon The coupon with restrictions
     * @param clientCountryId The client's country ID for geographic restriction check
     * @return true if the product is eligible for the coupon discount
     */
    private boolean isProductEligibleForCoupon(ProductSelectionDto selection, CouponResponseDTO coupon, String clientCountryId) {
        // If geographic restriction matches client's country, apply to ALL products
        if (coupon.getGeographicRestrictions() != null && !coupon.getGeographicRestrictions().isEmpty()) {
            if (clientCountryId != null && coupon.getGeographicRestrictions().contains(clientCountryId)) {
                return true;
            }
        }
        
        // If no restrictions at all (no geo, no product), apply to ALL products
        if ((coupon.getProductRestrictions() == null || coupon.getProductRestrictions().isEmpty()) &&
            (coupon.getGeographicRestrictions() == null || coupon.getGeographicRestrictions().isEmpty())) {
            return true;
        }
        
        // Check product/package restrictions - must match BOTH productId AND packageId (if specified)
        if (coupon.getProductRestrictions() != null && !coupon.getProductRestrictions().isEmpty()) {
            return coupon.getProductRestrictions().stream()
                .anyMatch(restriction -> {
                    // First check productId match
                    boolean productMatch = selection.getProductId() != null 
                        && restriction.getProductId() != null
                        && selection.getProductId().equals(restriction.getProductId());
                    if (!productMatch) return false;
                    
                    // If packageId is null/blank in restriction, applies to ALL packages of this product
                    if (restriction.getPackageId() == null || restriction.getPackageId().isBlank()) {
                        return true;
                    }
                    
                    // Package-specific restriction: must match BOTH productId AND packageId
                    return selection.getPackageId() != null && 
                           selection.getPackageId().equals(restriction.getPackageId());
                });
        }
        
        return false;
    }

    /**
     * Gets VAT rate from billing service VAT API.
     * Handles region-based VAT by checking if stateId matches any region in the configuration.
     * 
     * @param countryId Country ID (UUID format)
     * @param stateId State/Region ID (UUID or identifier, can be null)
     * @return VAT rate as percentage (e.g., 5.0 for 5%)
     * @throws RegistrationServiceException if VAT API call fails or VAT config not found
     */
    private double getVatRateFromBillingService(String countryId, String stateId) {
        if (countryId == null || countryId.isBlank()) {
            throw new RegistrationServiceException("Country ID is required for VAT calculation");
        }

        try {
            log.info("Fetching VAT configuration from billing service for countryId: {}, stateId: {}", countryId, stateId);
            
            String url = billingServiceUrl + "/vat/country/" + countryId;
            
            JsonNode responseJson = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (responseJson == null) {
                throw new RegistrationServiceException("VAT API returned null response for countryId: " + countryId);
            }

            JsonNode dataNode = responseJson.get("data");
            if (dataNode == null || dataNode.isNull()) {
                throw new RegistrationServiceException("VAT configuration not found for countryId: " + countryId);
            }

            // Extract VAT configuration fields
            Double defaultVatRate = dataNode.has("defaultVatRate") && !dataNode.get("defaultVatRate").isNull() 
                    ? dataNode.get("defaultVatRate").asDouble() 
                    : null;
            boolean regionBased = dataNode.has("regionBased") && dataNode.get("regionBased").asBoolean();
            JsonNode regionsNode = dataNode.get("regions");

            if (defaultVatRate == null) {
                throw new RegistrationServiceException("VAT configuration has no default VAT rate for countryId: " + countryId);
            }

            // Handle region-based VAT
            if (regionBased && stateId != null && !stateId.isBlank() && regionsNode != null && regionsNode.isArray()) {
                log.debug("VAT configuration is region-based, searching for stateId: {}", stateId);
                
                // Search for matching region
                for (JsonNode regionNode : regionsNode) {
                    String regionId = regionNode.has("id") ? regionNode.get("id").asText() : null;
                    if (stateId.equals(regionId)) {
                        double regionVatRate = regionNode.has("vatRate") ? regionNode.get("vatRate").asDouble() : defaultVatRate;
                        log.info("Found region-specific VAT rate: {}% for stateId: {}", regionVatRate, stateId);
                        return regionVatRate;
                    }
                }
                
                log.debug("No matching region found for stateId: {}, using default VAT rate: {}%", stateId, defaultVatRate);
            }

            log.info("Using default VAT rate: {}% for countryId: {}", defaultVatRate, countryId);
            return defaultVatRate;

        } catch (WebClientResponseException e) {
            log.error("VAT API call failed for countryId: {}, stateId: {}. Status: {}, Response: {}", 
                    countryId, stateId, e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode().value() == 404) {
                throw new RegistrationServiceException(
                        "VAT configuration not found for countryId: " + countryId + ". Please ensure VAT is configured for this country.", e);
            } else {
                throw new RegistrationServiceException(
                        "Failed to retrieve VAT configuration from billing service. Status: " + e.getStatusCode() + ", Error: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Unexpected error while fetching VAT rate from billing service for countryId: {}, stateId: {}. Error: {}", 
                    countryId, stateId, e.getMessage(), e);
            throw new RegistrationServiceException(
                    "Failed to calculate VAT. Billing service unavailable or error occurred: " + e.getMessage(), e);
        }
    }

    private void sendInvoiceAndNotification(
            OrganizationInfoDto org,
            String tempPassword,
            ClientOnboardingResponseDto response,
            InvoiceRequestDTO invoiceRequestDTO,
            InvoiceResponseDTO invoiceResponse,
            CouponDiscountInfo couponInfo
    ) {
        try {
            String invoiceId = invoiceResponse.getId();
            log.info("Generating invoice PDF with ID: {}", invoiceId);

            String clientName = invoiceRequestDTO.getClientName() != null ? invoiceRequestDTO.getClientName() : "Client Name Not Provided";
            double subtotal = invoiceResponse.getSubtotal();
            double discount = invoiceResponse.getDiscountAmount();

            double vatPercentage = calculateVatPercentage( invoiceResponse.getVatAmount() , subtotal, discount);
            double vat = invoiceResponse.getVatAmount();
            double total = invoiceResponse.getTotalAmount();
            Instant createdAt = invoiceResponse.getCreatedAt() != null ? invoiceResponse.getCreatedAt() : Instant.now();

            // Convert billing InvoiceStatus to common InvoiceStatus
            com.aspire.asat.common.enums.InvoiceStatus commonStatus = convertToCommonInvoiceStatus(
                    invoiceResponse.getStatus() != null ? invoiceResponse.getStatus() : InvoiceStatus.PENDING);

            // Convert billing ProductSelectionDto to common ProductSelectionDto
            List<com.aspire.asat.common.dto.ProductSelectionDto> commonProductSelections =
                    invoiceRequestDTO.getProductSelections() != null
                            ? invoiceRequestDTO.getProductSelections().stream()
                            .map(this::convertToCommonProductSelectionDto)
                            .toList()
                            : List.of();

            // Extract coupon discount amount if coupon was applied
            double couponDiscount = 0.0;
            if (couponInfo != null && couponInfo.getDiscountAmount() != null) {
                couponDiscount = couponInfo.getDiscountAmount();
                log.info("Including coupon discount in PDF: {}", couponDiscount);
            }

            // Generate PDF using InvoiceGenerator
            ByteArrayOutputStream pdfStream = invoiceGenerator.generateInvoicePdf(
                    invoiceId,
                    clientName,
                    subtotal,
                    couponDiscount, // Use actual coupon discount amount
                    discount,
                    vatPercentage,
                    vat,
                    total,
                    createdAt,
                    commonProductSelections,
                    commonStatus,
                    invoiceResponse.getExpiresAt()
            );

            if (pdfStream == null) {
                throw new RegistrationServiceException("555 Failed to generate invoice PDF");
            }

            // Create a temporary file from ByteArrayOutputStream
            Result result = getResult(invoiceId, pdfStream);

            // Clean up temporary file
            if (result.tempFile().exists()) {
                result.tempFile().delete();
            }

            log.info("sending email to : {} with pdf link : {}", org.getAdminEmail(), result.s3ObjectKey());

            // Follow end user notification sending flow using RegistrationNotificationClient, including the invoice as attachment
            AttachmentDto attachment = AttachmentDto.builder()
                    .bucketName(s3BucketName)
                    .objectKey(result.s3ObjectKey())
                    .build();


            CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
            UUID userUUID = UUID.fromString(context.getUserId());
            Optional<AspireUserDto> mspUserOpt = aspireUserService.getUserById(userUUID);
            // Build template model with coupon information if applicable
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put(NotificationTemplateValue.USER_NAME, org.getOrganizationName());
            templateModel.put(NotificationTemplateValue.EMAIL, org.getAdminEmail());
            templateModel.put(NotificationTemplateValue.USER_ID, response.getAdminId());
            templateModel.put(NotificationTemplateValue.PASSWORD, tempPassword);
            if(mspUserOpt.isPresent()) {
                templateModel.put(NotificationTemplateValue.MSP_EMAIL, mspUserOpt.get().getEmail());
                templateModel.put(NotificationTemplateValue.MSP_NAME, mspUserOpt.get().getCompanyName());
            } else {
                templateModel.put(NotificationTemplateValue.MSP_EMAIL, supportEmail);
                templateModel.put(NotificationTemplateValue.MSP_NAME, "Aspire Support");
            }

            
            // Add coupon information if coupon was applied
            if (couponInfo != null) {
                templateModel.put("couponCode", couponInfo.getCouponCode());
                templateModel.put("couponName", couponInfo.getCouponName());
                templateModel.put("couponDiscount", String.format("%.2f", couponInfo.getDiscountAmount()));
                log.info("Including coupon information in email: Code={}, Name={}, Discount={}", 
                    couponInfo.getCouponCode(), couponInfo.getCouponName(), couponInfo.getDiscountAmount());
            }

            // Use sendCustomChannelNotification to pass custom template model with coupon info
            notificationServiceClient.sendWelcomeEmailNotificationWithCoupon(
                    org.getAdminEmail(),
                    response.getAdminId(),
                    response.getAdminId(), // clientAdminId for client-specific notification settings
                    org.getOrganizationName(),
                    tempPassword,
                    templateModel,
                    List.of(attachment)
            );
        } catch (Exception e) {
            log.error("777 Failed to generate invoice PDF", e);
            throw new RegistrationServiceException("777 Error generating invoice PDF", e);
        }
    }

    private Result getResult(String invoiceId, ByteArrayOutputStream pdfStream) throws IOException {
        File tempFile = File.createTempFile("invoice_" + invoiceId, ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            pdfStream.writeTo(fos);
        }

        // Generate S3 key using common module's KeyStrategy pattern
        String s3Key = "invoices/" + invoiceId + "_payment.pdf";

        // Upload using common module's FileService
        try {
            var uploadResponse = fileService.fileUpload(tempFile.getAbsolutePath(), s3Key);
            String s3ObjectKey = uploadResponse.getPath();
            Result result = new Result(tempFile, s3ObjectKey);
            return result;
        } catch (Exception e) {
            log.error("123 Failed to upload invoice PDF to S3", e);
            throw new IOException("345 Error uploading invoice PDF to S3", e);
        }

    }

    /**
     * Send invoice email to admin for buy now flow (without password)
     */
    private void sendBuyNowInvoiceEmail(
            ClientAdmin clientAdmin,
            InvoiceRequestDTO invoiceRequestDTO,
            InvoiceResponseDTO invoiceResponse
    ) {
        try {
            String invoiceId = invoiceResponse.getId();
            log.info("Generating invoice PDF for buy now flow with ID: {}", invoiceId);

            String clientName = invoiceRequestDTO.getClientName() != null 
                    ? invoiceRequestDTO.getClientName() 
                    : (clientAdmin.getOrganizationName() != null 
                            ? clientAdmin.getOrganizationName() 
                            : "Client Name Not Provided");
            double subtotal = invoiceResponse.getSubtotal();
            double discount = invoiceResponse.getDiscountAmount();

            double vatPercentage = calculateVatPercentage(invoiceResponse.getVatAmount(), subtotal, discount);
            double vat = invoiceResponse.getVatAmount();
            double total = invoiceResponse.getTotalAmount();
            Instant createdAt = invoiceResponse.getCreatedAt() != null 
                    ? invoiceResponse.getCreatedAt() 
                    : Instant.now();

            // Convert billing InvoiceStatus to common InvoiceStatus
            com.aspire.asat.common.enums.InvoiceStatus commonStatus = convertToCommonInvoiceStatus(
                    invoiceResponse.getStatus() != null ? invoiceResponse.getStatus() : InvoiceStatus.PENDING);

            // Convert billing ProductSelectionDto to common ProductSelectionDto
            List<com.aspire.asat.common.dto.ProductSelectionDto> commonProductSelections =
                    invoiceRequestDTO.getProductSelections() != null
                            ? invoiceRequestDTO.getProductSelections().stream()
                            .map(this::convertToCommonProductSelectionDto)
                            .toList()
                            : List.of();

            double couponDiscount = invoiceRequestDTO.getCouponDiscountAmount();

            ByteArrayOutputStream pdfStream = invoiceGenerator.generateInvoicePdf(
                    invoiceId,
                    clientName,
                    subtotal,
                    couponDiscount,
                    discount,
                    vatPercentage,
                    vat,
                    total,
                    createdAt,
                    commonProductSelections,
                    commonStatus,
                    invoiceResponse.getExpiresAt()
            );

            if (pdfStream == null) {
                throw new RegistrationServiceException("Failed to generate invoice PDF for buy now flow");
            }

            // Create a temporary file from ByteArrayOutputStream
            Result result = getResult(invoiceId, pdfStream);

            // Clean up temporary file
            if (result.tempFile().exists()) {
                result.tempFile().delete();
            }

            log.info("Sending invoice email to admin: {} with PDF link: {}", clientAdmin.getEmail(), result.s3ObjectKey());

            // Create attachment DTO
            AttachmentDto attachment = AttachmentDto.builder()
                    .bucketName(s3BucketName)
                    .objectKey(result.s3ObjectKey())
                    .build();

            // Prepare template model for CLIENT_ADMIN_PACKAGE_ASSIGNED notification (without password)
            // Only include expected template variables - password is NOT included
            // Note: companyName, supportEmail, loginUrl, baseUrl, currentYear are auto-enriched by NotificationDeliveryService
            Map<String, Object> templateModel = new HashMap<>();
            
            // User/Organization name
            String userName = clientAdmin.getOrganizationName() != null && !clientAdmin.getOrganizationName().isBlank()
                    ? clientAdmin.getOrganizationName(): "Client";
            templateModel.put(NotificationTemplateValue.USER_NAME, userName);
            
            // Assignment date
            templateModel.put(NotificationTemplateValue.ASSIGNMENT_DATE,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            
            // Package details message
            templateModel.put(NotificationTemplateValue.PACKAGE_DETAILS,
                    "New products have been added to your account. Please find the invoice attached.");
            
            // Logo URL (only if client has custom logo, otherwise auto-enrichment will use default)
            if (clientAdmin.getLogoUrl() != null && !clientAdmin.getLogoUrl().isBlank()) {
                templateModel.put(NotificationTemplateValue.LOGO_URL, clientAdmin.getLogoUrl());
            }

            // Send notification with invoice attachment (NO PASSWORD)
            notificationServiceClient.sendProductReassignmentNotification(
                    clientAdmin.getEmail(),
                    clientAdmin.getId(),
                    clientAdmin.getId(), // clientAdminId for client-specific notification settings
                    templateModel,
                    List.of(attachment)
            );

            log.info("Successfully sent invoice email to admin for buy now flow: {}", clientAdmin.getEmail());
        } catch (Exception e) {
            log.error("Failed to send invoice email for buy now flow: {}", e.getMessage(), e);
            throw new RegistrationServiceException("Failed to send invoice email for buy now flow: " + e.getMessage(), e);
        }
    }

    private record Result(File tempFile, String s3ObjectKey) {
    }

    /**
     * Resolves country display name from Country collection by ID.
     * Falls back to countryId if not found (e.g. legacy or external IDs).
     */
    private String getCountryNameById(String countryId) {
        if (countryId == null || countryId.isBlank()) {
            return null;
        }
        return countryRepository.findById(countryId)
                .map(Country::getName)
                .orElse(countryId);
    }

    private String getStateCode(String countryName, String stateName) {
        if (stateName == null || stateName.trim().isEmpty()) {
            return null;
        }
        
        if ("united states".equalsIgnoreCase(countryName)) {
            return switch (stateName.toLowerCase()) {
                case "california" -> "CA";
                case "new york" -> "NY";
                case "texas" -> "TX";
                default -> stateName.substring(0, 2).toUpperCase(); // fallback
            };
        }
        return stateName != null ? stateName.substring(0, 2).toUpperCase() : null;
    }


    @Override
    public EmailTemplateValidationResponseDto validateTemplate(String templateBody) {
        log.info("Validating template body:\n{}", templateBody);

        List<String> requiredPlaceholders = List.of(
                "{{USERNAME}}", "{{TEMP_PASSWORD}}", "{{INVOICE_ID}}",
                "{{CLIENT_NAME}}", "{{PORTAL_LINK}}", "{{PAYMENT_METHOD}}"
        );

        List<String> missing = requiredPlaceholders.stream()
                .filter(ph -> !templateBody.contains(ph))
                .toList();

        boolean valid = missing.isEmpty();
        String message = valid ? "Template is valid" : "Missing required placeholders";

        return EmailTemplateValidationResponseDto.builder()
                .valid(valid)
                .missingPlaceholders(missing)
                .message(message)
                .build();
    }


    @Override
    public boolean doesUsernameExist(String email) {
        log.info("Checking if username/email exists: {}", email);
        return clientAdminRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public void activateLicense(String clientId) {
        ClientAdmin clientAdmin = clientAdminRepository.findById(clientId)
                .orElseThrow(() -> new ClientActivationException("Client admin is not eligible for activation due to pending setup."));

        // Update ClientAdmin status to ACTIVE
        clientAdmin.setStatus(AdminStatus.ACTIVE);
        ClientAdmin updatedClientAdmin = clientAdminRepository.save(clientAdmin);
        log.info("Updated ClientAdmin status to ACTIVE for ID: {}", clientId);

        // Update all ClientProduct licenseStatus from PENDING to ACTIVE
        List<ClientProduct> clientProducts = clientProductRepository.findByClientAdminId(clientId);
        int activatedCount = 0;
        for (ClientProduct product : clientProducts) {
            if ("PENDING".equals(product.getLicenseStatus())) {
                product.setLicenseStatus("ACTIVE");
                clientProductRepository.save(product);
                activatedCount++;
            }
        }
        log.info("Activated {} client products (licenseStatus set to ACTIVE) for client: {}", activatedCount, clientId);

        // Update client dashboard in CMS service after license activation
        try {
            updateClientDashboardInCmsService(clientId);
            log.info("CMS dashboard updated successfully after license activation for client: {}", clientId);
        } catch (Exception e) {
            log.error("CMS dashboard update failed after license activation for client: {}. Error: {}", clientId, e.getMessage(), e);
            // Don't fail the activation if dashboard update fails, but log the error
            // License activation is more critical than dashboard update
        }

        // Update AspireUser record for centralized user management
        try {
            aspireUserService.updateAspireUser(
                    updatedClientAdmin.getId(),
                    UserType.CLIENT_ADMIN,
                    updatedClientAdmin
            );
            log.info("Updated AspireUser record for ClientAdmin with ID: {}", updatedClientAdmin.getId());
        } catch (Exception e) {
            log.error("Failed to update AspireUser record for ClientAdmin with ID: {}", updatedClientAdmin.getId(), e);
            // Don't fail the main operation if AspireUser update fails
        }

        // Update isTrial field to false if user was on trial
        try {
            UUID userId = UUID.fromString(updatedClientAdmin.getId());
            Optional<AspireUser> aspireUserOpt = aspireUserRepository.findByUserId(userId);
            if (aspireUserOpt.isPresent()) {
                AspireUser aspireUser = aspireUserOpt.get();
                if (Boolean.TRUE.equals(aspireUser.getIsTrial())) {
                    aspireUser.setIsTrial(false);
                    aspireUser.setUpdatedAt(Instant.now());
                    aspireUserRepository.save(aspireUser);
                    log.info("Updated isTrial to false for trial user who purchased product. ClientAdmin ID: {}", clientId);
                }
            } else {
                log.warn("AspireUser not found for ClientAdmin ID: {}. Cannot update isTrial field.", clientId);
            }
        } catch (Exception e) {
            log.error("Failed to update isTrial field for ClientAdmin with ID: {}", clientId, e);
            // Don't fail the main operation if isTrial update fails
        }
    }

    @Override
    public void deactivateLicense(String clientId, List<String> clientProductIds) {
        ClientAdmin clientAdmin = clientAdminRepository.findById(clientId)
                .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientId));

        int deactivatedProducts = 0;
        if (clientProductIds != null && !clientProductIds.isEmpty()) {
            for (String productId : clientProductIds) {
                if (productId == null || productId.isBlank()) {
                    continue;
                }
                Optional<ClientProduct> productOpt = clientProductRepository.findById(productId);
                if (productOpt.isEmpty()) {
                    log.warn("ClientProduct {} not found while deactivating licenses for client {}", productId, clientId);
                    continue;
                }
                ClientProduct product = productOpt.get();
                if (!clientId.equals(product.getClientAdminId())) {
                    log.warn("ClientProduct {} does not belong to client {}; skipping", productId, clientId);
                    continue;
                }
                String status = product.getLicenseStatus();
                if ("PENDING".equals(status) || "ACTIVE".equals(status)) {
                    product.setLicenseStatus("INACTIVE");
                    clientProductRepository.save(product);
                    deactivatedProducts++;
                }
            }
        }
        log.info("Deactivated {} client products for client {}", deactivatedProducts, clientId);

        if (clientAdmin.getStatus() != AdminStatus.PENDING) {
            log.info("ClientAdmin {} status is {}; leaving admin status unchanged", clientId, clientAdmin.getStatus());
            return;
        }

        clientAdmin.setStatus(AdminStatus.INACTIVE);
        clientAdminRepository.save(clientAdmin);
        log.info("Updated ClientAdmin status to INACTIVE for ID: {}", clientId);

        try {
            UUID clientAdminUuid = UUID.fromString(clientId);
            Optional<AspireUser> clientAdminUserOpt = aspireUserRepository.findByUserId(clientAdminUuid);
            if (clientAdminUserOpt.isPresent()) {
                AspireUser clientAdminUser = clientAdminUserOpt.get();
                clientAdminUser.setStatus(AdminStatus.INACTIVE.name());
                clientAdminUser.setUpdatedAt(Instant.now());
                aspireUserRepository.save(clientAdminUser);
                userSessionInvalidationHelper.logoutUsersIfRestrictive(
                        AdminStatus.INACTIVE.name(), List.of(clientId));
            } else {
                log.warn("AspireUser not found for ClientAdmin ID: {} during deactivateLicense", clientId);
            }
        } catch (Exception e) {
            log.error("Failed to sync AspireUser on deactivateLicense for client {}: {}", clientId, e.getMessage(), e);
        }
    }

    @Override
    public void expireLicenseDueToUnpaidInvoice(String clientId, List<String> clientProductIds) {
        ClientAdmin clientAdmin = clientAdminRepository.findById(clientId)
                .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientId));

        int expiredProducts = 0;
        if (clientProductIds != null && !clientProductIds.isEmpty()) {
            for (String productId : clientProductIds) {
                if (productId == null || productId.isBlank()) {
                    continue;
                }
                Optional<ClientProduct> productOpt = clientProductRepository.findById(productId);
                if (productOpt.isEmpty()) {
                    log.warn("ClientProduct {} not found while expiring licenses for client {}", productId, clientId);
                    continue;
                }
                ClientProduct product = productOpt.get();
                if (!clientId.equals(product.getClientAdminId())) {
                    log.warn("ClientProduct {} does not belong to client {}; skipping", productId, clientId);
                    continue;
                }
                String status = product.getLicenseStatus();
                if ("PENDING".equals(status) || "ACTIVE".equals(status)) {
                    product.setLicenseStatus("EXPIRED");
                    clientProductRepository.save(product);
                    expiredProducts++;
                }
            }
        }
        log.info("Expired {} client products for client {} due to unpaid invoice", expiredProducts, clientId);

        if (clientAdmin.getStatus() != AdminStatus.PENDING) {
            log.info("ClientAdmin {} status is {}; leaving admin/users unchanged after invoice expiry",
                    clientId, clientAdmin.getStatus());
            return;
        }

        // PENDING onboarding only: deactivate admin and cascade org users (same as status update)
        updateClientAdminStatus(clientId, AdminStatus.INACTIVE);
    }

    @Override
    public List<String> findProductIdsByClientAdminId(String clientAdminId) {
        // Only return ACTIVE products - PENDING products should not be visible to clients
        return clientProductRepository.findByClientAdminIdAndLicenseStatus(clientAdminId, "ACTIVE").stream()
                .map(ClientProduct::getProductId)
                .distinct() // optional: avoid duplicates if needed
                .toList();
    }


    @Override
    public List<String> findEndUsersByClientAdminId(String clientAdminId) {
        return clientAdminRepository.findIdsOnlyByClientAdminId(clientAdminId).stream()
                .map(ClientAdmin::getId)
                .toList();

    }

    @Override
    public List<ClientProductDTO> findAllClientProductsByClientAdminId(String clientAdminId, String productId) {
        List<ClientProduct> products;

        // Only return ACTIVE products - PENDING products should not be visible to clients
        if (productId != null && !productId.isBlank()) {
            products = clientProductRepository.findByClientAdminIdAndProductIdAndLicenseStatus(clientAdminId, productId, "ACTIVE");
        } else {
            products = clientProductRepository.findByClientAdminIdAndLicenseStatus(clientAdminId, "ACTIVE");
        }

        return products.stream()
                .map(product -> ClientProductDTO.builder()
                        .id(product.getId())
                        .clientAdminId(product.getClientAdminId())
                        .productId(product.getProductId())
                        .packageId(product.getPackageId())
                        .licenseCount(product.getLicenseCount())
                        .usedLicenseCount(product.getUsedLicenseCount())
                        .pricePerLicense(product.getPricePerLicense())
                        .totalPrice(product.getTotalPrice())
                        .validityPeriod(product.getValidityPeriod())
                        .validityUnit(product.getValidityUnit())
                        .assignedAt(product.getAssignedAt())
                        .expiryDate(product.getExpiryDate())
                        .build()
                ).toList();
    }

    @Override
    public void updateUsedLicenseCount(String clientAdminId, String productId, int usedLicenseCount) {
        // Only update ACTIVE products - PENDING products should not have their license count updated
        List<ClientProduct> products = clientProductRepository.findByClientAdminIdAndProductIdAndLicenseStatus(clientAdminId, productId, "ACTIVE");
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Active client product not found with given clientAdminId and productId");
        }

        // Assuming only one match per client-product pair
        ClientProduct product = products.get(0);
        product.setUsedLicenseCount(usedLicenseCount);
        product.setAssignedAt(Instant.now()); // optionally update timestamp
        clientProductRepository.save(product);
    }

    @Override
    public void updateUsedLicenseCountByClientProductId(String clientProductId, int usedLicenseCount) {
        if (clientProductId == null || clientProductId.isBlank()) {
            throw new IllegalArgumentException("Client product id is required");
        }
        ClientProduct product = clientProductRepository.findById(clientProductId.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active client product not found with id: " + clientProductId));
        if (!"ACTIVE".equalsIgnoreCase(product.getLicenseStatus())) {
            throw new IllegalArgumentException(
                    "Active client product not found with id: " + clientProductId);
        }
        product.setUsedLicenseCount(usedLicenseCount);
        product.setAssignedAt(Instant.now());
        clientProductRepository.save(product);
        log.info("Updated usedLicenseCount={} for clientProductId={}", usedLicenseCount, clientProductId);
    }

    @Override
    @Transactional
    public ClientProductReassignmentResponseDto reassignProductsToClient(ClientProductAssignment assignmentRequest) {
        validateReassignmentRequest(assignmentRequest);
        String clientAdminId = assignmentRequest.getClientAdminId();
        log.info("Reassigning products to client admin: {}", clientAdminId);

        ClientAdmin clientAdmin = clientAdminRepository.findById(clientAdminId)
                .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientAdminId));

        List<ProductSelectionDto> selections = assignmentRequest.getProductSelections();
        InvoiceDetailsDto invoice = assignmentRequest.getInvoice();
        String mspId = resolveMspIdForClientAssignment(assignmentRequest.getMspId(), clientAdmin);

        if (mspId != null && !mspId.isBlank()) {
            if (clientAdmin.getMspId() == null || !mspId.equals(clientAdmin.getMspId())) {
                throw new RegistrationServiceException("MSP ID mismatch for client admin: " + clientAdminId);
            }
            validateMspLicenseAvailabilityForAssignment(mspId, selections);
        }

        if (clientAdmin.getCountry() == null || clientAdmin.getCountry().isBlank()) {
            throw new RegistationValidationException(
                    "Client admin country is required for product reassignment; set organization country on the client admin.");
        }

        productPriceResolver.applyAuthoritativePricing(selections);
        List<String> originalProductIds = clientAdmin.getClientProductIds() != null ?
                new ArrayList<>(clientAdmin.getClientProductIds()) : new ArrayList<>();

        List<String> newClientProductIds = createClientProducts(
                selections,
                invoice,
                clientAdminId,
                clientAdmin.getCountry(),
                "Product reassignment - additional products",
                mspId
        );

        double totalAmount = MoneyUtil.round(clientProductRepository.findAllById(newClientProductIds).stream()
                .mapToDouble(ClientProduct::getTotalPrice)
                .sum());

        List<String> allProductIds = new ArrayList<>(originalProductIds);
        allProductIds.addAll(newClientProductIds);
        clientAdmin.setClientProductIds(allProductIds);
        clientAdminRepository.save(clientAdmin);

        InvoiceRequestDTO invoiceRequestDTO = buildInvoiceRequestDTOForReassignment(
                clientAdminId, newClientProductIds, invoice, clientAdmin, selections, mspId);

        InvoiceResponseDTO invoiceResponse;
        try {
            invoiceResponse = createInvoice(invoiceRequestDTO);
        } catch (Exception e) {
            log.error("Invoice creation failed during reassignment: {}", e.getMessage(), e);
            performReassignmentRollback(clientAdminId, newClientProductIds, originalProductIds, null);
            throw wrapAsRegistrationException("Invoice creation failed during product reassignment", e);
        }

        log.info("Reassignment invoice response: {}", invoiceResponse);
        String billingInvoiceId = invoiceResponse != null ? invoiceResponse.getId() : null;

        try {
            updateClientDashboardInCmsService(clientAdminId);
            log.info("CMS dashboard updated successfully for reassignment.");
        } catch (Exception e) {
            log.error("CMS dashboard update failed during reassignment. Performing rollback. Error: {}", e.getMessage(), e);
            performReassignmentRollback(clientAdminId, newClientProductIds, originalProductIds, billingInvoiceId);
            throw new RegistrationServiceException(
                    "Failed to update client dashboard in CMS service. Reassignment aborted: " + e.getMessage(), e);
        }

        sendReassignmentNotification(clientAdmin, invoiceRequestDTO, invoiceResponse, newClientProductIds);

        if (mspId != null && !mspId.isBlank()) {
            updateMspProductUsedLicenseCount(mspId, selections);
        }

        return ClientProductReassignmentResponseDto.builder()
                .clientAdminId(clientAdminId)
                .clientAdminEmail(clientAdmin.getEmail())
                .organizationName(clientAdmin.getOrganizationName())
                .assignedProductIds(newClientProductIds)
                .totalProductsReassigned(newClientProductIds.size())
                .totalAmount(totalAmount)
                .invoiceId(invoiceResponse.getId())
                .portalLink(logInUrl)
                .build();
    }

    /**
     * Sends notification for product reassignment with invoice PDF attachment.
     * This is a non-critical operation - failures are logged but don't fail the main operation.
     *
     * @param clientAdmin The client admin
     * @param invoiceRequestDTO Invoice request DTO with details
     * @param invoiceResponse Invoice response with ID
     * @param newClientProductIds List of new product IDs
     */
    private void sendReassignmentNotification(
            ClientAdmin clientAdmin,
            InvoiceRequestDTO invoiceRequestDTO,
            InvoiceResponseDTO invoiceResponse,
            List<String> newClientProductIds) {
        try {
            String invoiceId = invoiceResponse.getId();
            log.info("Generating reassignment invoice PDF with ID: {}", invoiceId);

            ByteArrayOutputStream pdfStream = invoiceGenerator.generateInvoicePdf(
                    invoiceId,
                    invoiceRequestDTO.getClientName(),
                    invoiceRequestDTO.getTotalAmount(),
                    Date.from(Instant.now())
            );

            if (pdfStream == null) {
                log.warn("Failed to generate reassignment invoice PDF for ID: {}", invoiceId);
                return;
            }

            // Upload the PDF and get S3 object key
            Result result = getResult(invoiceId, pdfStream);

            AttachmentDto attachment = AttachmentDto.builder()
                    .bucketName(s3BucketName)
                    .objectKey(result.s3ObjectKey())
                    .build();

            String assignmentDate = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));

            String packageDetailsSummary = "Total products assigned: " + newClientProductIds.size();

            java.util.Map<String, Object> templateModel = new java.util.HashMap<>();
            templateModel.put(NotificationTemplateValue.USER_NAME, clientAdmin.getEmail());
            templateModel.put(NotificationTemplateValue.ASSIGNMENT_DATE, assignmentDate);
            templateModel.put(NotificationTemplateValue.PACKAGE_DETAILS, packageDetailsSummary);
            templateModel.put(NotificationTemplateValue.LOGO_URL, clientAdmin.getLogoUrl() != null ? clientAdmin.getLogoUrl() : logoUrl);
            templateModel.put(NotificationTemplateValue.LOGIN_URL, logInUrl);

            notificationServiceClient.sendProductReassignmentNotification(
                clientAdmin.getEmail(),
                    clientAdmin.getId(),
                    clientAdmin.getId(),
                templateModel,
                java.util.List.of(attachment)
            );
            log.info("Reassignment notification sent to: {}", clientAdmin.getEmail());

        } catch (Exception e) {
            log.error("Failed to generate reassignment invoice PDF or send notification", e);
            // Don't fail the main operation if PDF generation or notification fails
        }
    }


    @Override
    public ClientAdminListResponseDto listClientAdminsWithProducts(ClientAdminListRequestDto requestDto) {
        log.info("Listing client admins with products - request: {}", requestDto);

        try {
            // Set default values for pagination
            int offset = requestDto.getOffset() != null ? requestDto.getOffset() : 0;
            int pageSize = requestDto.getPageSize() != null ? requestDto.getPageSize() : 10;

            // Create pageable object
            Pageable pageable = PageRequest.of(offset, pageSize);

            String mspId = null;
            CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
            String userType = userContext.getUserType();

            if(UserType.MSP.name().equalsIgnoreCase(userType)) {
                mspId = userContext.getUserId();
                log.info("MSP Admin user detected, filtering client admins by MSP ID: {}", mspId);
            } else {
                mspId =  requestDto.getMspId();
            }
            // Get filtered and paginated client admins
            Page<ClientAdmin> clientAdminPage = clientAdminRepositoryCustom.findClientAdminsWithFilters(
                    requestDto.getSearch(),
                    mspId,
                    requestDto.getStatus(),
                    requestDto.getCreatedAt(),
                    requestDto.getCountry(),
                    requestDto.getState(),
                    pageable
            );

            // Convert to response DTOs with product details
            List<ClientAdminWithProductsResponseDto> clientAdminDtos = clientAdminPage.getContent().stream()
                    .map(this::convertToClientAdminWithProductsDto)
                    .toList();

            // Build pagination info
            int totalPages = clientAdminPage.getTotalPages();
            boolean hasNext = clientAdminPage.hasNext();
            boolean hasPrevious = clientAdminPage.hasPrevious();

            log.info("Retrieved {} client admins out of {} total, page {} of {}",
                    clientAdminDtos.size(), clientAdminPage.getTotalElements(),
                    offset + 1, totalPages);

            return ClientAdminListResponseDto.builder()
                    .clientAdmins(clientAdminDtos)
                    .offset(offset)
                    .pageSize(pageSize)
                    .total(clientAdminPage.getTotalElements())
                    .totalPages(totalPages)
                    .hasNext(hasNext)
                    .hasPrevious(hasPrevious)
                    .build();

        } catch (Exception e) {
            log.error("Error listing client admins with products", e);
            throw new RegistrationServiceException("Failed to retrieve client admins: " + e.getMessage(), e);
        }
    }

    private ClientAdminWithProductsResponseDto convertToClientAdminWithProductsDto(ClientAdmin clientAdmin) {
        // Get client products for this admin
        List<ClientProduct> clientProducts = clientProductRepository.findByClientAdminId(clientAdmin.getId());

        // Convert client products to detail DTOs
        List<ClientProductDetailDto> productDetails = new ArrayList<>(clientProducts.stream()
                .map(this::convertToClientProductDetailDto)
                .toList());

        // Map buy-now selection into clientProducts as a minimal entry (ids and names only) when there are no real products yet
        if (clientProducts.isEmpty() && (clientAdmin.getSelectedProductId() != null || clientAdmin.getSelectedPackageId() != null)) {
            CmsProductResponseDto product = CmsProductResponseDto.builder()
                    .productId(clientAdmin.getSelectedProductId())
                    .productName(clientAdmin.getSelectedProductName())
                    .build();
            CmsPackageDto packageDetails = CmsPackageDto.builder()
                    .id(clientAdmin.getSelectedPackageId())
                    .packageName(clientAdmin.getSelectedPackageName())
                    .userRangeId(clientAdmin.getSelectedUserRangeId())
                    .build();
            productDetails.add(ClientProductDetailDto.builder()
                    .productId(clientAdmin.getSelectedProductId())
                    .packageId(clientAdmin.getSelectedPackageId())
                    .product(product)
                    .packageDetails(packageDetails)
                    .build());
        }

        return ClientAdminWithProductsResponseDto.builder()
                .id(clientAdmin.getId())
                .email(clientAdmin.getEmail())
                .organizationName(clientAdmin.getOrganizationName())
                .contactEmail(clientAdmin.getContactEmail())
                .phoneNumber(clientAdmin.getPhoneNumber())
                .phoneCode(clientAdmin.getPhoneCode())
                .billingName(clientAdmin.getBillingName())
                .billingEmail(clientAdmin.getBillingEmail())
                .billingUseSameAsOrganizationAddress(clientAdmin.isBillingUseSameAsOrganizationAddress())
                .billingStreetAddress(clientAdmin.getBillingStreetAddress())
                .billingStreetAddressLine2(clientAdmin.getBillingStreetAddressLine2())
                .billingCity(clientAdmin.getBillingCity())
                .billingZipPostalCode(clientAdmin.getBillingZipPostalCode())
                .billingCountry(clientAdmin.getBillingCountry())
                .billingStateProvince(clientAdmin.getBillingStateProvince())
                .mspId(clientAdmin.getMspId())
                .country(clientAdmin.getCountry())
                .countryCode(clientAdmin.getCountryCode())
                .state(clientAdmin.getState())
                .stateCode(clientAdmin.getStateCode())
                .timeZone(clientAdmin.getTimeZone())
                .language(clientAdmin.getLanguage())
                .industry(clientAdmin.getIndustry())
                .subIndustryId(clientAdmin.getSubIndustryId())
                .complianceId(clientAdmin.getComplianceId())
                .complianceName(clientAdmin.getComplianceName())
                .domain(clientAdmin.getDomain())
                .organizationSize(clientAdmin.getOrganizationSize())
                .organizationType(clientAdmin.getOrganizationType())
                .streetAddress(clientAdmin.getStreetAddress())
                .streetAddressLine2(clientAdmin.getStreetAddressLine2())
                .city(clientAdmin.getCity())
                .zipPostalCode(clientAdmin.getZipPostalCode())
                .logoUrl(clientAdmin.getLogoUrl())
                .status(clientAdmin.getStatus())
                .createdAt(clientAdmin.getCreatedAt())
                .clientAdminId(clientAdmin.getClientAdminId())
                .creditId(clientAdmin.getCreditId())
                .tierId(clientAdmin.getTierId())
                .mspType(clientAdmin.getMspType())
                .mspAdminEmail(clientAdmin.getMspAdminEmail())
                .netDays(clientAdmin.getNetDays())
                .department(clientAdmin.getDepartment())
                .roleIds(clientAdmin.getRoleIds())
                .clientProducts(productDetails)
                .build();
    }

    private ClientProductDetailDto convertToClientProductDetailDto(ClientProduct clientProduct) {
        try {
            // Fetch product details from CMS service
            CmsProductResponseDto productDetails = cmsServiceClient.getProductWithPackages(clientProduct.getProductId());

            log.debug("Converting ClientProduct to DTO - ProductId: {}, PackageId: {}",
                    clientProduct.getProductId(), clientProduct.getPackageId());

            // Find the specific package details
            CmsPackageDto packageDetails = null;
            if (productDetails != null) {
                // Get package details separately
                packageDetails = cmsServiceClient.getPackageDetails(clientProduct.getProductId(), clientProduct.getPackageId());
                if (packageDetails != null && clientProduct.getUserRangeId() != null) {
                    packageDetails.setUserRangeId(clientProduct.getUserRangeId());
                }
            } else {
                log.warn("No product details found for productId: {}", clientProduct.getProductId());
            }

            return ClientProductDetailDto.builder()
                    .id(clientProduct.getId())
                    .clientAdminId(clientProduct.getClientAdminId())
                    .productId(clientProduct.getProductId())
                    .packageId(clientProduct.getPackageId())
                    .product(productDetails)
                    .packageDetails(packageDetails)
                    .licenseCount(clientProduct.getLicenseCount())
                    .usedLicenseCount(clientProduct.getUsedLicenseCount())
                    .pricePerLicense(clientProduct.getPricePerLicense())
                    .totalPrice(clientProduct.getTotalPrice())
                    .validityPeriod(clientProduct.getValidityPeriod())
                    .validityUnit(clientProduct.getValidityUnit())
                    .assignedAt(clientProduct.getAssignedAt())
                    .expiryDate(clientProduct.getExpiryDate())
                    .licenseStatus(clientProduct.getLicenseStatus())
                    .build();

        } catch (Exception e) {
            log.error("Error converting ClientProduct to DTO for productId: {}, packageId: {}",
                    clientProduct.getProductId(), clientProduct.getPackageId(), e);

            // Return DTO without CMS details in case of error
            return ClientProductDetailDto.builder()
                    .id(clientProduct.getId())
                    .clientAdminId(clientProduct.getClientAdminId())
                    .productId(clientProduct.getProductId())
                    .packageId(clientProduct.getPackageId())
                    .product(null)
                    .packageDetails(null)
                    .licenseCount(clientProduct.getLicenseCount())
                    .usedLicenseCount(clientProduct.getUsedLicenseCount())
                    .pricePerLicense(clientProduct.getPricePerLicense())
                    .totalPrice(clientProduct.getTotalPrice())
                    .validityPeriod(clientProduct.getValidityPeriod())
                    .validityUnit(clientProduct.getValidityUnit())
                    .assignedAt(clientProduct.getAssignedAt())
                    .expiryDate(clientProduct.getExpiryDate())
                    .licenseStatus(clientProduct.getLicenseStatus())
                    .build();
        }
    }

    @Override
    public ClientAdminDetailedResponseDto getClientAdminDetailedById(String clientAdminId) {
        log.info("Retrieving detailed client admin information for ID: {}", clientAdminId);

        // Find client admin by ID
        ClientAdmin clientAdmin = clientAdminRepository.findById(clientAdminId)
                .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientAdminId));

        // Convert to detailed response DTO with dropdown details and product information
        ClientAdminDetailedResponseDto response = convertToClientAdminDetailedDto(clientAdmin);

        log.info("Successfully retrieved detailed client admin information for ID: {}", clientAdminId);
        return response;
    }

    private ClientAdminDetailedResponseDto convertToClientAdminDetailedDto(ClientAdmin clientAdmin) {
        // Get client products for this admin
        List<ClientProduct> clientProducts = clientProductRepository.findByClientAdminId(clientAdmin.getId());

        // Convert client products to detailed DTOs
        List<ClientProductDetailedDto> productDetails = new ArrayList<>(clientProducts.stream()
                .map(this::convertToClientProductDetailedDto)
                .toList());

        // Map buy-now selection into clientProducts as a minimal entry (ids and names only) when there are no real products yet
        if (clientProducts.isEmpty() && (clientAdmin.getSelectedProductId() != null || clientAdmin.getSelectedPackageId() != null)) {
            CmsProductResponseDto product = CmsProductResponseDto.builder()
                    .productId(clientAdmin.getSelectedProductId())
                    .productName(clientAdmin.getSelectedProductName())
                    .build();
            CmsPackageDto packageDetails = CmsPackageDto.builder()
                    .id(clientAdmin.getSelectedPackageId())
                    .packageName(clientAdmin.getSelectedPackageName())
                    .userRangeId(clientAdmin.getSelectedUserRangeId())
                    .build();
            productDetails.add(ClientProductDetailedDto.builder()
                    .productId(clientAdmin.getSelectedProductId())
                    .packageId(clientAdmin.getSelectedPackageId())
                    .product(product)
                    .packageDetails(packageDetails)
                    .build());
        }

        // Fetch dropdown details
        ClientAdminDetailedResponseDto.DropdownDetailDto countryDetail = getCountryDetail(clientAdmin.getCountry());
        ClientAdminDetailedResponseDto.DropdownDetailDto stateDetail = getStateDetail(clientAdmin.getState());
        ClientAdminDetailedResponseDto.DropdownDetailDto timezoneDetail = getTimezoneDetail(clientAdmin.getTimeZone());
        ClientAdminDetailedResponseDto.DropdownDetailDto languageDetail = getLanguageDetail(clientAdmin.getLanguage());
        ClientAdminDetailedResponseDto.DropdownDetailDto industryDetail = getIndustryDetail(clientAdmin.getIndustry());
        ClientAdminDetailedResponseDto.DropdownDetailDto subIndustryDetail = getSubIndustryDetail(clientAdmin.getSubIndustryId());
        ClientAdminDetailedResponseDto.DropdownDetailDto organizationSizeDetail = getOrganizationSizeDetail(clientAdmin.getOrganizationSize());
        ClientAdminDetailedResponseDto.DropdownDetailDto organizationTypeDetail = getOrganizationTypeDetail(clientAdmin.getOrganizationType());

        String logoUrlFromProfile = aspireUserRepository.findByUserId(UUID.fromString(clientAdmin.getId()))
                .map(AspireUser::getProfilePicture)
                .orElse(null);

        return ClientAdminDetailedResponseDto.builder()
                .id(clientAdmin.getId())
                .email(clientAdmin.getEmail())
                .organizationName(clientAdmin.getOrganizationName())
                .contactEmail(clientAdmin.getContactEmail())
                .phoneNumber(clientAdmin.getPhoneNumber())
                .phoneCode(clientAdmin.getPhoneCode())
                .billingName(clientAdmin.getBillingName())
                .billingEmail(clientAdmin.getBillingEmail())
                .billingUseSameAsOrganizationAddress(clientAdmin.isBillingUseSameAsOrganizationAddress())
                .billingStreetAddress(clientAdmin.getBillingStreetAddress())
                .billingStreetAddressLine2(clientAdmin.getBillingStreetAddressLine2())
                .billingCity(clientAdmin.getBillingCity())
                .billingZipPostalCode(clientAdmin.getBillingZipPostalCode())
                .billingCountry(clientAdmin.getBillingCountry())
                .billingStateProvince(clientAdmin.getBillingStateProvince())
                .mspId(clientAdmin.getMspId())
                .mspName(clientAdmin.getMspName())
                .country(countryDetail)
                .state(stateDetail)
                .timeZone(timezoneDetail)
                .language(languageDetail)
                .industry(industryDetail)
                .subIndustry(subIndustryDetail)
                .complianceId(clientAdmin.getComplianceId())
                .complianceName(clientAdmin.getComplianceName())
                .organizationSize(organizationSizeDetail)
                .organizationType(organizationTypeDetail)
                .domain(clientAdmin.getDomain())
                .streetAddress(clientAdmin.getStreetAddress())
                .streetAddressLine2(clientAdmin.getStreetAddressLine2())
                .city(clientAdmin.getCity())
                .zipPostalCode(clientAdmin.getZipPostalCode())
                .logoUrl(logoUrlFromProfile)
                .branding(BrandingMapping.toDto(clientAdmin))
                .status(clientAdmin.getStatus())
                .createdAt(clientAdmin.getCreatedAt())
                .clientAdminId(clientAdmin.getClientAdminId())
                .creditId(clientAdmin.getCreditId())
                .tierId(clientAdmin.getTierId())
                .mspType(clientAdmin.getMspType())
                .mspAdminEmail(clientAdmin.getMspAdminEmail())
                .netDays(clientAdmin.getNetDays())
                .department(clientAdmin.getDepartment())
                .roleIds(clientAdmin.getRoleIds())
                .onboardBy(clientAdmin.getOnboardBy() != null ? clientAdmin.getOnboardBy().name() : null)
                .clientProducts(productDetails)
                .build();
    }

    private ClientAdminDetailedResponseDto.DropdownDetailDto getCountryDetail(String countryId) {
        if (countryId == null || countryId.isEmpty()) {
            return null;
        }
        return countryRepository.findById(countryId)
                .map(country -> ClientAdminDetailedResponseDto.DropdownDetailDto.builder()
                        .id(country.getId())
                        .code(country.getCode())
                        .name(country.getName())
                        .active(country.getActive())
                        .build())
                .orElse(null);
    }

    private ClientAdminDetailedResponseDto.DropdownDetailDto getStateDetail(String stateId) {
        if (stateId == null || stateId.isEmpty()) {
            return null;
        }
        return stateRepository.findById(stateId)
                .map(state -> ClientAdminDetailedResponseDto.DropdownDetailDto.builder()
                        .id(state.getId())
                        .code(state.getCode())
                        .name(state.getName())
                        .active(state.getActive())
                        .build())
                .orElse(null);
    }

    private ClientAdminDetailedResponseDto.DropdownDetailDto getTimezoneDetail(String timezoneId) {
        if (timezoneId == null || timezoneId.isEmpty()) {
            return null;
        }
        return timezoneRepository.findById(timezoneId)
                .map(timezone -> ClientAdminDetailedResponseDto.DropdownDetailDto.builder()
                        .id(timezone.getId())
                        .code(timezone.getTimezoneId())
                        .name(timezone.getDisplayName())
                        .displayName(timezone.getDisplayName())
                        .active(timezone.getActive())
                        .build())
                .orElse(null);
    }

    private ClientAdminDetailedResponseDto.DropdownDetailDto getLanguageDetail(String languageId) {
        if (languageId == null || languageId.isEmpty()) {
            return null;
        }
        return languageRepository.findById(languageId)
                .map(language -> ClientAdminDetailedResponseDto.DropdownDetailDto.builder()
                        .id(language.getId())
                        .code(language.getCode())
                        .name(language.getDisplayName())
                        .displayName(language.getDisplayName())
                        .active(language.getActive())
                        .build())
                .orElse(null);
    }

    private ClientAdminDetailedResponseDto.DropdownDetailDto getIndustryDetail(String industryId) {
        if (industryId == null || industryId.isEmpty()) {
            return null;
        }
        return industryRepository.findById(industryId)
                .map(industry -> ClientAdminDetailedResponseDto.DropdownDetailDto.builder()
                        .id(industry.getId())
                        .code(industry.getCode())
                        .name(industry.getName())
                        .active(industry.getActive())
                        .build())
                .orElse(null);
    }

    private ClientAdminDetailedResponseDto.DropdownDetailDto getSubIndustryDetail(String subIndustryId) {
        if (subIndustryId == null || subIndustryId.isEmpty()) {
            return null;
        }
        return subIndustryRepository.findById(subIndustryId)
                .map(subIndustry -> ClientAdminDetailedResponseDto.DropdownDetailDto.builder()
                        .id(subIndustry.getId())
                        .code(subIndustry.getCode())
                        .name(subIndustry.getName())
                        .active(subIndustry.getActive())
                        .build())
                .orElse(null);
    }

    private ClientAdminDetailedResponseDto.DropdownDetailDto getOrganizationSizeDetail(String organizationSizeId) {
        if (organizationSizeId == null || organizationSizeId.isEmpty()) {
            return null;
        }
        return organizationSizeRepository.findById(organizationSizeId)
                .map(orgSize -> ClientAdminDetailedResponseDto.DropdownDetailDto.builder()
                        .id(orgSize.getId())
                        .name(orgSize.getName())
                        .range(orgSize.getRange())
                        .build())
                .orElse(null);
    }

    private ClientAdminDetailedResponseDto.DropdownDetailDto getOrganizationTypeDetail(String organizationTypeId) {
        if (organizationTypeId == null || organizationTypeId.isEmpty()) {
            return null;
        }
        return organizationTypeRepository.findById(organizationTypeId)
                .map(orgType -> ClientAdminDetailedResponseDto.DropdownDetailDto.builder()
                        .id(orgType.getId())
                        .name(orgType.getName())
                        .active(orgType.getIsActive())
                        .build())
                .orElse(null);
    }

    private ClientProductDetailedDto convertToClientProductDetailedDto(ClientProduct clientProduct) {
        try {
            log.debug("Converting ClientProduct to Detailed DTO - ProductId: {}, PackageId: {}",
                    clientProduct.getProductId(), clientProduct.getPackageId());

            // Fetch product with package details and topic count in a single CMS API call
            CmsProductWithSinglePackageResponseDto productPackageDetails = cmsServiceClient.getProductPackageDetails(
                    clientProduct.getProductId(), 
                    clientProduct.getPackageId()
            );

            // Extract product and package details from the response
            CmsProductResponseDto productDetails = null;
            CmsPackageDto packageDetails = null;
            Integer topicCount = null;

            if (productPackageDetails != null) {
                // Extract product details
                productDetails = CmsProductResponseDto.builder()
                        .productId(productPackageDetails.getProductId())
                        .productName(productPackageDetails.getProductName())
                        .productDescription(productPackageDetails.getProductDescription())
                        .productStatus(productPackageDetails.getProductStatus())
                        .thumbnailUrl(productPackageDetails.getThumbnailUrl())
                        .displayOrder(productPackageDetails.getDisplayOrder())
                        .tags(productPackageDetails.getTags())
                        .build();

                // Extract package details
                packageDetails = productPackageDetails.getPackages();
                if (packageDetails != null && clientProduct.getUserRangeId() != null) {
                    packageDetails.setUserRangeId(clientProduct.getUserRangeId());
                }
                
                // Extract topic count
                topicCount = productPackageDetails.getTopicCount();
            } else {
                log.warn("No product package details found for productId: {}, packageId: {}",
                        clientProduct.getProductId(), clientProduct.getPackageId());
            }

            // Convert payment payload
            ClientProductDetailedDto.PaymentPayloadDto paymentPayloadDto = null;
            if (clientProduct.getPaymentPayload() != null) {
                paymentPayloadDto = ClientProductDetailedDto.PaymentPayloadDto.builder()
                        .clientId(clientProduct.getPaymentPayload().getClientId())
                        .currency(clientProduct.getPaymentPayload().getCurrency())
                        .amount(clientProduct.getPaymentPayload().getAmount())
                        .subtotal(clientProduct.getPaymentPayload().getSubtotal())
                        .vatAmount(clientProduct.getPaymentPayload().getVatAmount())
                        .discountAmount(clientProduct.getPaymentPayload().getDiscountAmount())
                        .discountPercentage(clientProduct.getPaymentPayload().getDiscountPercentage())
                        .date(clientProduct.getPaymentPayload().getDate() != null ?
                                clientProduct.getPaymentPayload().getDate().toInstant() : null)
                        .notes(clientProduct.getPaymentPayload().getNotes())
                        .clientRegion(clientProduct.getPaymentPayload().getClientRegion())
                        .build();
            }

            return ClientProductDetailedDto.builder()
                    .id(clientProduct.getId())
                    .clientAdminId(clientProduct.getClientAdminId())
                    .productId(clientProduct.getProductId())
                    .packageId(clientProduct.getPackageId())
                    .product(productDetails)
                    .packageDetails(packageDetails)
                    .licenseCount(clientProduct.getLicenseCount())
                    .usedLicenseCount(clientProduct.getUsedLicenseCount())
                    .pricePerLicense(clientProduct.getPricePerLicense())
                    .totalPrice(clientProduct.getTotalPrice())
                    .validityPeriod(clientProduct.getValidityPeriod())
                    .validityUnit(clientProduct.getValidityUnit())
                    .assignedAt(clientProduct.getAssignedAt())
                    .expiryDate(clientProduct.getExpiryDate())
                    .licenseStatus(clientProduct.getLicenseStatus())
                    .paymentPayload(paymentPayloadDto)
                    .topicCount(topicCount)
                    .build();

        } catch (Exception e) {
            log.error("Error converting ClientProduct to Detailed DTO for productId: {}, packageId: {}",
                    clientProduct.getProductId(), clientProduct.getPackageId(), e);

            // Return DTO without CMS details in case of error
            return ClientProductDetailedDto.builder()
                    .id(clientProduct.getId())
                    .clientAdminId(clientProduct.getClientAdminId())
                    .productId(clientProduct.getProductId())
                    .packageId(clientProduct.getPackageId())
                    .product(null)
                    .packageDetails(null)
                    .licenseCount(clientProduct.getLicenseCount())
                    .usedLicenseCount(clientProduct.getUsedLicenseCount())
                    .pricePerLicense(clientProduct.getPricePerLicense())
                    .totalPrice(clientProduct.getTotalPrice())
                    .validityPeriod(clientProduct.getValidityPeriod())
                    .validityUnit(clientProduct.getValidityUnit())
                    .assignedAt(clientProduct.getAssignedAt())
                    .expiryDate(clientProduct.getExpiryDate())
                    .licenseStatus(clientProduct.getLicenseStatus())
                    .paymentPayload(null)
                    .topicCount(null)
                    .build();
        }
    }

    @Override
    @Transactional
    public ClientAdminWithProductsResponseDto updateClientAdminById(String clientAdminId, ClientAdminUpdateRequestDto updateRequestDto) {
        log.info("Updating organization details for ID: {}", clientAdminId);

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext != null ? userContext.getUserType() : null;

        if (UserType.MSP.name().equalsIgnoreCase(userType)) {
            return updateMspUserFromClientAdminRequest(clientAdminId, updateRequestDto);
        }

        return updateClientAdminEntityById(clientAdminId, updateRequestDto);
    }

    private ClientAdminWithProductsResponseDto updateClientAdminEntityById(
            String clientAdminId, ClientAdminUpdateRequestDto updateRequestDto) {
        ClientAdmin existingClientAdmin = clientAdminRepository.findById(clientAdminId)
                .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientAdminId));

        // Update ClientAdmin fields except logoUrl (logo goes to AspireUser.profilePicture)
        updateClientAdminFields(existingClientAdmin, updateRequestDto);
        ClientAdmin updatedClientAdmin = clientAdminRepository.save(existingClientAdmin);

        // Apply logoUrl only for AspireUser sync; restore afterward to avoid dirty write-back
        String persistedLogoUrl = updatedClientAdmin.getLogoUrl();
        updatedClientAdmin.setLogoUrl(updateRequestDto.getLogoUrl());
        syncAspireUserAfterUpdate(updatedClientAdmin.getId(), UserType.CLIENT_ADMIN, updatedClientAdmin);
        updatedClientAdmin.setLogoUrl(persistedLogoUrl);

        ClientAdminWithProductsResponseDto response = convertToClientAdminWithProductsDto(updatedClientAdmin);
        response.setLogoUrl(resolveProfilePicture(updatedClientAdmin.getId()));

        log.info("Successfully updated client admin details for ID: {}", clientAdminId);
        return response;
    }

    private ClientAdminWithProductsResponseDto updateMspUserFromClientAdminRequest(
            String mspUserId, ClientAdminUpdateRequestDto updateRequestDto) {
        MspUser existingMspUser = mspUsersRepository.findById(mspUserId)
                .or(() -> mspUsersRepository.findByMspId(mspUserId))
                .orElseThrow(() -> new RegistrationServiceException("MSP user not found with ID: " + mspUserId));

        // Update MspUser fields except logoUrl (logo goes to AspireUser.profilePicture)
        updateMspUserFields(existingMspUser, updateRequestDto);
        existingMspUser.setUpdatedAt(Instant.now());
        MspUser updatedMspUser = mspUsersRepository.save(existingMspUser);

        // Apply logoUrl only for AspireUser sync; restore afterward to avoid dirty write-back
        String persistedLogoUrl = updatedMspUser.getLogoUrl();
        updatedMspUser.setLogoUrl(updateRequestDto.getLogoUrl());
        syncAspireUserAfterUpdate(updatedMspUser.getId(), UserType.MSP, updatedMspUser);
        updatedMspUser.setLogoUrl(persistedLogoUrl);

        ClientAdminWithProductsResponseDto response = convertMspUserToClientAdminWithProductsDto(updatedMspUser);
        response.setLogoUrl(resolveProfilePicture(updatedMspUser.getId()));

        log.info("Successfully updated MSP user details for ID: {}", updatedMspUser.getId());
        return response;
    }

    private void syncAspireUserAfterUpdate(String baseUserId, UserType userType, Object domainUserData) {
        try {
            aspireUserService.updateAspireUser(baseUserId, userType, domainUserData);
            log.info("Updated AspireUser record for {} with ID: {}", userType, baseUserId);
        } catch (Exception e) {
            log.error("Failed to update AspireUser record for {} with ID: {}", userType, baseUserId, e);
        }
    }

    private String resolveProfilePicture(String userId) {
        try {
            return aspireUserRepository.findByUserId(UUID.fromString(userId))
                    .map(AspireUser::getProfilePicture)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user ID format while resolving profile picture: {}", userId);
            return null;
        }
    }

    private void updateClientAdminFields(ClientAdmin clientAdmin, ClientAdminUpdateRequestDto updateRequestDto) {
        // Use a small helper to apply updates only when values are non-null.
        setIfNotNull(updateRequestDto.getOrganizationName(), clientAdmin::setOrganizationName);
        setIfNotNull(updateRequestDto.getOrganizationType(), clientAdmin::setOrganizationType);
        setIfNotNull(updateRequestDto.getIndustry(), clientAdmin::setIndustry);
        setIfNotNull(updateRequestDto.getSubIndustryId(), clientAdmin::setSubIndustryId);
        setIfNotNull(updateRequestDto.getComplianceId(), clientAdmin::setComplianceId);
        setIfNotNull(updateRequestDto.getComplianceName(), clientAdmin::setComplianceName);
        setIfNotNull(updateRequestDto.getContactEmail(), clientAdmin::setContactEmail);

        setIfNotNull(updateRequestDto.getCountry(), clientAdmin::setCountry);
        setIfNotNull(updateRequestDto.getPhoneNumber(), clientAdmin::setPhoneNumber);
        setIfNotNull(updateRequestDto.getPhoneCode(), clientAdmin::setPhoneCode);
        setIfNotNull(updateRequestDto.getStateProvince(), clientAdmin::setState);
        setIfNotNull(updateRequestDto.getTimeZone(), clientAdmin::setTimeZone);
        setIfNotNull(updateRequestDto.getLanguage(), clientAdmin::setLanguage);
        setIfNotNull(updateRequestDto.getOrganizationSize(), clientAdmin::setOrganizationSize);

        // Organization address fields
        setIfNotNull(updateRequestDto.getStreetAddress(), clientAdmin::setStreetAddress);
        setIfNotNull(updateRequestDto.getStreetAddressLine2(), clientAdmin::setStreetAddressLine2);
        setIfNotNull(updateRequestDto.getCity(), clientAdmin::setCity);
        setIfNotNull(updateRequestDto.getZipPostalCode(), clientAdmin::setZipPostalCode);

        // Billing information
        Boolean useSameAsOrgAddress = updateRequestDto.getSameAsOrganizationAddress();
        if (useSameAsOrgAddress != null) {
            clientAdmin.setBillingUseSameAsOrganizationAddress(useSameAsOrgAddress);
            if (useSameAsOrgAddress) {
                // Copy organization address to billing address
                clientAdmin.setBillingStreetAddress(clientAdmin.getStreetAddress());
                clientAdmin.setBillingStreetAddressLine2(clientAdmin.getStreetAddressLine2());
                clientAdmin.setBillingCity(clientAdmin.getCity());
                clientAdmin.setBillingZipPostalCode(clientAdmin.getZipPostalCode());
                clientAdmin.setBillingCountry(clientAdmin.getCountry());
                clientAdmin.setBillingStateProvince(clientAdmin.getState());
            }
        }
        setIfNotNull(updateRequestDto.getBillingName(), clientAdmin::setBillingName);
        setIfNotNull(updateRequestDto.getBillingEmail(), clientAdmin::setBillingEmail);

        // Billing address fields (only used if useSameAsOrganizationAddress is false)
        if (useSameAsOrgAddress == null || !useSameAsOrgAddress) {
            setIfNotNull(updateRequestDto.getBillingStreetAddress(), clientAdmin::setBillingStreetAddress);
            setIfNotNull(updateRequestDto.getBillingStreetAddressLine2(), clientAdmin::setBillingStreetAddressLine2);
            setIfNotNull(updateRequestDto.getBillingCity(), clientAdmin::setBillingCity);
            setIfNotNull(updateRequestDto.getBillingZipPostalCode(), clientAdmin::setBillingZipPostalCode);
            setIfNotNull(updateRequestDto.getBillingCountry(), clientAdmin::setBillingCountry);
            setIfNotNull(updateRequestDto.getBillingStateProvince(), clientAdmin::setBillingStateProvince);
        }

        // Codes — logoUrl is intentionally excluded (stored on AspireUser.profilePicture)
        setIfNotNull(updateRequestDto.getCountryCode(), clientAdmin::setCountryCode);
        setIfNotNull(updateRequestDto.getStateCode(), clientAdmin::setStateCode);
    }

    private void updateMspUserFields(MspUser mspUser, ClientAdminUpdateRequestDto updateRequestDto) {
        setIfNotNull(updateRequestDto.getOrganizationName(), mspUser::setOrganizationName);
        setIfNotNull(updateRequestDto.getOrganizationType(), mspUser::setOrganizationType);
        setIfNotNull(updateRequestDto.getIndustry(), mspUser::setIndustry);
        setIfNotNull(updateRequestDto.getSubIndustryId(), mspUser::setSubIndustry);
        setIfNotNull(updateRequestDto.getContactEmail(), mspUser::setContactEmail);
        setIfNotNull(updateRequestDto.getCountry(), mspUser::setCountry);
        setIfNotNull(updateRequestDto.getPhoneNumber(), mspUser::setPhoneNumber);
        setIfNotNull(updateRequestDto.getPhoneCode(), mspUser::setPhoneCode);
        setIfNotNull(updateRequestDto.getStateProvince(), mspUser::setStateProvince);
        setIfNotNull(updateRequestDto.getTimeZone(), mspUser::setTimeZone);
        setIfNotNull(updateRequestDto.getLanguage(), mspUser::setLanguage);
        setIfNotNull(updateRequestDto.getOrganizationSize(), mspUser::setOrganizationSize);

        setIfNotNull(updateRequestDto.getStreetAddress(), mspUser::setOrganizationStreetAddress);
        setIfNotNull(updateRequestDto.getStreetAddressLine2(), mspUser::setOrganizationStreetAddressLine2);
        setIfNotNull(updateRequestDto.getCity(), mspUser::setOrganizationCity);
        setIfNotNull(updateRequestDto.getZipPostalCode(), mspUser::setOrganizationZipPostalCode);
        setIfNotNull(updateRequestDto.getCountry(), mspUser::setOrganizationCountry);
        setIfNotNull(updateRequestDto.getStateProvince(), mspUser::setOrganizationStateProvince);

        setIfNotNull(updateRequestDto.getBillingName(), mspUser::setBillingName);
        setIfNotNull(updateRequestDto.getBillingEmail(), mspUser::setBillingEmail);

        Boolean useSameAsOrgAddress = updateRequestDto.getSameAsOrganizationAddress();
        if (Boolean.TRUE.equals(useSameAsOrgAddress)) {
            mspUser.setBillingStreetAddress(mspUser.getOrganizationStreetAddress());
            mspUser.setBillingStreetAddressLine2(mspUser.getOrganizationStreetAddressLine2());
            mspUser.setBillingCity(mspUser.getOrganizationCity());
            mspUser.setBillingZipPostalCode(mspUser.getOrganizationZipPostalCode());
            mspUser.setBillingCountry(mspUser.getOrganizationCountry());
            mspUser.setBillingStateProvince(mspUser.getOrganizationStateProvince());
        } else if (useSameAsOrgAddress == null || !useSameAsOrgAddress) {
            setIfNotNull(updateRequestDto.getBillingStreetAddress(), mspUser::setBillingStreetAddress);
            setIfNotNull(updateRequestDto.getBillingStreetAddressLine2(), mspUser::setBillingStreetAddressLine2);
            setIfNotNull(updateRequestDto.getBillingCity(), mspUser::setBillingCity);
            setIfNotNull(updateRequestDto.getBillingZipPostalCode(), mspUser::setBillingZipPostalCode);
            setIfNotNull(updateRequestDto.getBillingCountry(), mspUser::setBillingCountry);
            setIfNotNull(updateRequestDto.getBillingStateProvince(), mspUser::setBillingStateProvince);
        }
        // logoUrl is intentionally excluded (stored on AspireUser.profilePicture)
    }

    private ClientAdminWithProductsResponseDto convertMspUserToClientAdminWithProductsDto(MspUser mspUser) {
        return ClientAdminWithProductsResponseDto.builder()
                .id(mspUser.getId())
                .email(mspUser.getMspAdminEmail())
                .organizationName(mspUser.getOrganizationName())
                .contactEmail(mspUser.getContactEmail())
                .phoneNumber(mspUser.getPhoneNumber())
                .phoneCode(mspUser.getPhoneCode())
                .billingName(mspUser.getBillingName())
                .billingEmail(mspUser.getBillingEmail())
                .billingStreetAddress(mspUser.getBillingStreetAddress())
                .billingStreetAddressLine2(mspUser.getBillingStreetAddressLine2())
                .billingCity(mspUser.getBillingCity())
                .billingZipPostalCode(mspUser.getBillingZipPostalCode())
                .billingCountry(mspUser.getBillingCountry())
                .billingStateProvince(mspUser.getBillingStateProvince())
                .mspId(mspUser.getMspId() != null ? mspUser.getMspId() : mspUser.getId())
                .country(mspUser.getCountry())
                .state(mspUser.getStateProvince())
                .timeZone(mspUser.getTimeZone())
                .language(mspUser.getLanguage())
                .industry(mspUser.getIndustry())
                .subIndustryId(mspUser.getSubIndustry())
                .domain(mspUser.getDomain())
                .organizationSize(mspUser.getOrganizationSize())
                .organizationType(mspUser.getOrganizationType())
                .streetAddress(mspUser.getOrganizationStreetAddress())
                .streetAddressLine2(mspUser.getOrganizationStreetAddressLine2())
                .city(mspUser.getOrganizationCity())
                .zipPostalCode(mspUser.getOrganizationZipPostalCode())
                .logoUrl(null)
                .status(parseAdminStatus(mspUser.getStatus()))
                .createdAt(mspUser.getCreatedAt())
                .creditId(mspUser.getCreditId())
                .tierId(mspUser.getMspTier())
                .mspType(mspUser.getMspTypeId())
                .mspAdminEmail(mspUser.getMspAdminEmail())
                .department(mspUser.getDepartment())
                .roleIds(mspUser.getRoleIds())
                .clientProducts(List.of())
                .build();
    }

    private AdminStatus parseAdminStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return AdminStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            if ("SUSPENDED".equalsIgnoreCase(status)) {
                return AdminStatus.SUSPEND;
            }
            log.warn("Unable to map MSP status '{}' to AdminStatus", status);
            return null;
        }
    }

    private static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }


    @Override
    public ClientProductsPaginatedResponseDto getAssignedClientProducts(String clientAdminId, String search, Integer offset, Integer pageSize, String sortBy, String order) {
        return getAssignedClientProductsByLicenseStatuses(
                clientAdminId, search, offset, pageSize, sortBy, order, List.of("ACTIVE"));
    }

    @Override
    public ClientProductsPaginatedResponseDto getAssignedActiveAndPendingClientProducts(
            String clientAdminId, String search, Integer offset, Integer pageSize, String sortBy, String order) {
        return getAssignedClientProductsByLicenseStatuses(
                clientAdminId, search, offset, pageSize, sortBy, order, List.of("ACTIVE", "PENDING"));
    }

    private ClientProductsPaginatedResponseDto getAssignedClientProductsByLicenseStatuses(
            String clientAdminId,
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order,
            List<String> licenseStatuses) {
        log.info("Retrieving assigned client products for client admin: {} with statuses: {}, search: {}, offset: {}, pageSize: {}, sortBy: {}, order: {}",
                clientAdminId, licenseStatuses, search, offset, pageSize, sortBy, order);

        try {
            // Validate client admin exists
            ClientAdmin clientAdmin = clientAdminRepository.findById(clientAdminId)
                    .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientAdminId));

            // Set default values for pagination
            int pageOffset = offset != null ? offset : 0;
            int pageSizeValue = pageSize != null ? pageSize : 10;
            String sortField = sortBy != null ? sortBy : "displayOrder";
            String sortOrder = order != null ? order : "asc";

            // Validate sort field
            if (!isValidSortField(sortField)) {
                throw new IllegalArgumentException("Invalid sort field: " + sortField);
            }

            // Validate sort order
            if (!isValidSortOrder(sortOrder)) {
                throw new IllegalArgumentException("Invalid sort order: " + sortOrder);
            }

            List<ClientProduct> allClientProducts = clientProductRepository.findByClientAdminIdAndLicenseStatusIn(
                    clientAdminId, licenseStatuses);

            // Convert to detailed DTOs and apply search filter
            List<ClientProductDetailedDto> allProductDetails = allClientProducts.stream()
                    .map(this::convertToClientProductDetailedDto)
                    .filter(dto -> {
                        if (search == null || search.trim().isEmpty()) {
                            return true;
                        }
                        // Search by productName and packageName
                        String searchTerm = search.toLowerCase();
                        String productName = dto.getProduct() != null ? dto.getProduct().getProductName() : "";
                        String packageName = dto.getPackageDetails() != null ? dto.getPackageDetails().getPackageName() : "";

                        return productName.toLowerCase().contains(searchTerm) ||
                                packageName.toLowerCase().contains(searchTerm);
                    })
                    .toList();

            // Apply sorting
            allProductDetails = applySorting(allProductDetails, sortField, sortOrder);

            // Apply pagination
            long totalCount = allProductDetails.size();
            int startIndex = pageOffset;
            int endIndex = Math.min(startIndex + pageSizeValue, allProductDetails.size());

            List<ClientProductDetailedDto> paginatedItems = allProductDetails.subList(startIndex, endIndex);

            log.info("Retrieved {} client products out of {} total for client admin: {}",
                    paginatedItems.size(), totalCount, clientAdminId);

            return ClientProductsPaginatedResponseDto.builder()
                    .items(paginatedItems)
                    .offset(pageOffset)
                    .pageSize(pageSizeValue)
                    .total(totalCount)
                    .search(search)
                    .sortBy(sortField)
                    .order(sortOrder)
                    .clientAdminId(clientAdminId)
                    .clientAdminEmail(clientAdmin.getEmail())
                    .organizationName(clientAdmin.getOrganizationName())
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving assigned client products for client admin: {}", clientAdminId, e);
            throw new RegistrationServiceException("Failed to retrieve assigned client products: " + e.getMessage(), e);
        }
    }

    /**
     * Validate if the sort field is allowed
     */
    private boolean isValidSortField(String sortField) {
        List<String> allowedSortFields = List.of(
                "displayOrder", "assignedAt", "expiryDate", "licenseCount", "usedLicenseCount",
                "pricePerLicense", "totalPrice", "validityPeriod", "licenseStatus"
        );
        return allowedSortFields.contains(sortField);
    }

    /**
     * Validate if the sort order is valid
     */
    private boolean isValidSortOrder(String sortOrder) {
        return "asc".equalsIgnoreCase(sortOrder) || "desc".equalsIgnoreCase(sortOrder);
    }

    /**
     * Apply sorting to the list of client product detailed DTOs
     */
    private List<ClientProductDetailedDto> applySorting(List<ClientProductDetailedDto> items, String sortField, String sortOrder) {
        return items.stream()
                .sorted((a, b) -> {
                    int comparison = 0;

                    switch (sortField) {
                        case "displayOrder":
                            Integer orderA = a.getProduct() != null ? a.getProduct().getDisplayOrder() : null;
                            Integer orderB = b.getProduct() != null ? b.getProduct().getDisplayOrder() : null;
                            comparison = compareNullableDisplayOrder(orderA, orderB);
                            break;
                        case "assignedAt":
                            comparison = compareInstants(a.getAssignedAt(), b.getAssignedAt());
                            break;
                        case "expiryDate":
                            comparison = compareInstants(a.getExpiryDate(), b.getExpiryDate());
                            break;
                        case "licenseCount":
                            comparison = Integer.compare(
                                    a.getLicenseCount() != null ? a.getLicenseCount() : 0,
                                    b.getLicenseCount() != null ? b.getLicenseCount() : 0
                            );
                            break;
                        case "usedLicenseCount":
                            comparison = Integer.compare(
                                    a.getUsedLicenseCount() != null ? a.getUsedLicenseCount() : 0,
                                    b.getUsedLicenseCount() != null ? b.getUsedLicenseCount() : 0
                            );
                            break;
                        case "pricePerLicense":
                            comparison = Double.compare(
                                    a.getPricePerLicense() != null ? a.getPricePerLicense() : 0.0,
                                    b.getPricePerLicense() != null ? b.getPricePerLicense() : 0.0
                            );
                            break;
                        case "totalPrice":
                            comparison = Double.compare(
                                    a.getTotalPrice() != null ? a.getTotalPrice() : 0.0,
                                    b.getTotalPrice() != null ? b.getTotalPrice() : 0.0
                            );
                            break;
                        case "validityPeriod":
                            comparison = Integer.compare(
                                    a.getValidityPeriod() != null ? a.getValidityPeriod() : 0,
                                    b.getValidityPeriod() != null ? b.getValidityPeriod() : 0
                            );
                            break;
                        default:
                            comparison = 0;
                    }

                    return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
                })
                .toList();
    }

    /**
     * Compare CMS displayOrder; nulls sort last in ascending order.
     */
    private static int compareNullableDisplayOrder(Integer a, Integer b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return a.compareTo(b);
    }

    /**
     * Compare two Instant objects safely
     */
    private int compareInstants(Instant a, Instant b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    @Override
    public LicenseStatisticsResponseDto getLicenseStatistics(String clientAdminId) {
        return getLicenseStatistics(clientAdminId, null);
    }

    @Override
    public LicenseStatisticsResponseDto getLicenseStatistics(String clientAdminId, String productId) {
        log.info("Retrieving license statistics for client admin: {}, productId: {}", clientAdminId, productId);

        try {
            // Validate client admin exists
            Optional<ClientAdmin> clientAdmin = clientAdminRepository.findById(clientAdminId);
            if (clientAdmin.isEmpty()) {
                throw new RegistrationServiceException("Client admin not found with ID: " + clientAdminId);
            }

            // Purchased seats always come from ACTIVE client_products
            ClientProductRepositoryCustom.LicenseStatistics statistics =
                    clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(clientAdminId, productId);

            int totalLicenseCount = statistics.getTotalLicenseCount();
            int totalUsedLicenseCount = resolveUsedLicenseCount(clientAdminId, productId, statistics);

            int availableLicenseCount = totalLicenseCount - totalUsedLicenseCount;

            // Calculate utilization percentage
            double utilizationPercentage = totalLicenseCount > 0
                    ? (double) totalUsedLicenseCount / totalLicenseCount * 100.0
                    : 0.0;

            log.info("License statistics for client admin {} (productId={}): Total={}, Used={}, Available={}, Utilization={}%",
                    clientAdminId, productId, totalLicenseCount, totalUsedLicenseCount, availableLicenseCount, utilizationPercentage);

            return LicenseStatisticsResponseDto.builder()
                    .licenseCount(totalLicenseCount)
                    .usedLicenseCount(totalUsedLicenseCount)
                    .availableLicenseCount(availableLicenseCount)
                    .utilizationPercentage(Math.round(utilizationPercentage * 100.0) / 100.0) // Round to 2 decimal places
                    .build();

        } catch (RegistrationServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving license statistics for client admin: {}, productId: {}", clientAdminId, productId, e);
            throw new RegistrationServiceException("Failed to retrieve license statistics: " + e.getMessage(), e);
        }
    }

    @Override
    public LicenseOverviewSummaryDto getLicenseOverviewSummary(String clientAdminId) {
        log.info("Retrieving license overview summary for client admin: {}", clientAdminId);

        if (clientAdminRepository.findById(clientAdminId).isEmpty()) {
            throw new RegistrationServiceException("Client admin not found with ID: " + clientAdminId);
        }

        List<ClientProduct> activeProducts = clientProductRepository
                .findByClientAdminIdAndLicenseStatus(clientAdminId, "ACTIVE");
        if (activeProducts == null) {
            activeProducts = List.of();
        }

        Instant now = Instant.now();
        Instant in30Days = now.plus(EXPIRING_SOON_DAYS, ChronoUnit.DAYS);
        Instant monthAgo = now.minus(EXPIRING_SOON_DAYS, ChronoUnit.DAYS);

        int totalLicenses = 0;
        int assignedUsers = 0;
        int expiringSoon = 0;
        Set<String> productIds = new HashSet<>();
        for (ClientProduct product : activeProducts) {
            totalLicenses += product.getLicenseCount();
            assignedUsers += product.getUsedLicenseCount();
            if (expiresWithin(product.getExpiryDate(), now, in30Days)) {
                expiringSoon++;
            }
            if (product.getProductId() != null && !product.getProductId().isBlank()) {
                productIds.add(product.getProductId());
            }
        }
        int availableSeats = totalLicenses - assignedUsers;

        double assignedPercent = percentOf(assignedUsers, totalLicenses);
        double availablePercent = percentOf(availableSeats, totalLicenses);
        double utilizationChange = roundOneDecimal(
                assignedPercent - utilizationAsOf(clientAdminId, activeProducts, monthAgo));

        return LicenseOverviewSummaryDto.builder()
                .totalLicenses(totalLicenses)
                .assignedUsers(assignedUsers)
                .assignedUsersPercent(assignedPercent)
                .availableSeats(availableSeats)
                .availableSeatsPercent(availablePercent)
                .utilizationRate(assignedPercent)
                .utilizationChangeVsLastMonth(utilizationChange)
                .expiringSoon(expiringSoon)
                .expiringSoonDays(EXPIRING_SOON_DAYS)
                .activeProducts(productIds.size())
                .build();
    }

    private double utilizationAsOf(String clientAdminId, List<ClientProduct> activeProducts, Instant asOf) {
        int totalThen = activeProducts.stream()
                .filter(cp -> cp.getAssignedAt() == null || !cp.getAssignedAt().isAfter(asOf))
                .mapToInt(ClientProduct::getLicenseCount)
                .sum();
        if (totalThen <= 0) {
            return 0.0;
        }
        long usedThen = userLicenceRepository.countAssignedAsOf(clientAdminId, asOf);
        return percentOf((int) usedThen, totalThen);
    }

    private static boolean expiresWithin(Instant expiryDate, Instant fromInclusive, Instant toInclusive) {
        return expiryDate != null
                && !expiryDate.isBefore(fromInclusive)
                && !expiryDate.isAfter(toInclusive);
    }

    private static double percentOf(int part, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return roundOneDecimal(part * 100.0 / total);
    }

    private static double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * For Phishing/Smishing/Vishing products, used seats are unique campaign participants
     * (per ClientProduct assignment / productPackageId) from the phishing service.
     * All other products keep the denormalized {@code client_products.usedLicenseCount} sum.
     */
    private int resolveUsedLicenseCount(
            String clientAdminId,
            String productId,
            ClientProductRepositoryCustom.LicenseStatistics statistics) {

        if (productId != null && !productId.isBlank()) {
            if (!simulationProductResolver.isSimulationProduct(productId)) {
                return statistics.getTotalUsedLicenseCount();
            }
            return sumUniqueCampaignUsers(clientAdminId, productId);
        }

        // All Products: non-simulation seat usage + simulation unique campaign users
        Set<String> simulationProductIds = simulationProductResolver.getSimulationProductIds();
        int nonSimulationUsed = sumNonSimulationUsedLicenseCount(clientAdminId, simulationProductIds);

        if (simulationProductIds.isEmpty()) {
            return nonSimulationUsed;
        }

        // Only call phishing when the client actually has an ACTIVE simulation product
        boolean hasActiveSimulationProduct = clientProductRepository
                .findByClientAdminIdAndLicenseStatus(clientAdminId, "ACTIVE").stream()
                .anyMatch(cp -> simulationProductIds.contains(cp.getProductId()));
        if (!hasActiveSimulationProduct) {
            return nonSimulationUsed;
        }

        return nonSimulationUsed + sumUniqueCampaignUsers(clientAdminId, null);
    }

    private int sumNonSimulationUsedLicenseCount(String clientAdminId, Set<String> simulationProductIds) {
        return clientProductRepository.findByClientAdminIdAndLicenseStatus(clientAdminId, "ACTIVE").stream()
                .filter(cp -> cp.getProductId() == null || !simulationProductIds.contains(cp.getProductId()))
                .mapToInt(ClientProduct::getUsedLicenseCount)
                .sum();
    }

    private int sumUniqueCampaignUsers(String clientAdminId, String productId) {
        List<CampaignLicenseUsageDto> usage = phishingServiceClient.getCampaignLicenseUsage(clientAdminId);
        if (usage == null || usage.isEmpty()) {
            return 0;
        }
        if (productId == null || productId.isBlank()) {
            return usage.stream().mapToInt(CampaignLicenseUsageDto::getUniqueUserCount).sum();
        }
        Set<String> productPackageIds = clientProductRepository
                .findByClientAdminIdAndProductIdAndLicenseStatus(clientAdminId, productId, "ACTIVE")
                .stream()
                .map(ClientProduct::getId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
        if (productPackageIds.isEmpty()) {
            return 0;
        }
        return usage.stream()
                .filter(row -> row.getProductPackageId() != null
                        && productPackageIds.contains(row.getProductPackageId()))
                .mapToInt(CampaignLicenseUsageDto::getUniqueUserCount)
                .sum();
    }

    @Override
    public int getUniqueProductCountByClientAdminId(String clientAdminId) {
        log.info("Retrieving unique product count for client admin: {}", clientAdminId);

        try {
            int uniqueProductCount = clientProductRepositoryCustom.getUniqueProductCountByClientAdminId(clientAdminId);
            log.info("Unique product count for client admin {}: {}", clientAdminId, uniqueProductCount);
            return uniqueProductCount;
        } catch (Exception e) {
            log.error("Error retrieving unique product count for client admin: {}", clientAdminId, e);
            throw new RegistrationServiceException("Failed to retrieve unique product count: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateClientDashboardInCmsService(String clientAdminId) {
        log.info("Updating client dashboard in CMS service for clientAdminId: {}", clientAdminId);

        try {
            // 1. Get unique product count
            int totalProduct = getUniqueProductCountByClientAdminId(clientAdminId);

            // 2. Get license statistics
            LicenseStatisticsResponseDto licenseStats = getLicenseStatistics(clientAdminId);
            int totalLicense = licenseStats.getLicenseCount();

            // 3. Set topic and certificate counts to 0 as requested
            int totalTopic = 0;
            int totalCertificate = 0;

            // 4. Get ClientAdmin email
            Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(clientAdminId);
            String email = clientAdminOpt.map(ClientAdmin::getEmail).orElse(null);

            // 5. Get ClientProduct data
            List<ClientProduct> clientProducts = clientProductRepository.findByClientAdminId(clientAdminId);
            List<ClientProductData> clientProductDataList = 
                    clientProducts.stream()
                            .map(cp -> ClientProductData.builder()
                                    .clientAdminId(cp.getClientAdminId())
                                    .productId(cp.getProductId())
                                    .packageId(cp.getPackageId())
                                    .assignedAt(cp.getAssignedAt())
                                    .expiryDate(cp.getExpiryDate())
                                    .email(email)
                                    .build())
                            .toList();

            // 6. Create dashboard request
            ClientDashboardRequestDto dashboardRequest = ClientDashboardRequestDto.builder()
                    .clientAdminId(clientAdminId)
                    .totalProduct(totalProduct)
                    .totalLicense(totalLicense)
                    .totalTopic(totalTopic)
                    .totalCertificate(totalCertificate)
                    .clientProductData(clientProductDataList)
                    .build();

            log.info("Dashboard data for clientAdminId {}: products={}, licenses={}, topics={}, certificates={}, clientProductData size={}",
                    clientAdminId, totalProduct, totalLicense, totalTopic, totalCertificate, clientProductDataList.size());

            // 7. Call CMS service
            // If this fails, exception will propagate and trigger transaction rollback
            webClient.post()
                    .uri(cmsServiceUrl + "/client-dashboard")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(dashboardRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Successfully updated client dashboard in CMS service for clientAdminId: {}", clientAdminId);

        } catch (WebClientResponseException e) {
            log.error("CMS service returned error for dashboard update: Status={}, Response={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            // Throw exception to trigger transaction rollback
            throw new RegistrationServiceException(
                    String.format("CMS service API call failed with status %d: %s", 
                            e.getStatusCode(), e.getResponseBodyAsString()), e);
        } catch (Exception e) {
            log.error("Failed to update client dashboard in CMS service for clientAdminId: {}", clientAdminId, e);
            // Throw exception to trigger transaction rollback
            throw new RegistrationServiceException(
                    "Failed to update client dashboard in CMS service: " + e.getMessage(), e);
        }
    }

    @Override
    public ClientProductDetailedDto getProductPackageDetailById(String id) {
        log.info("Retrieving product-package details for ID: {}", id);

        try {
            // Find the client product by ID
            ClientProduct clientProduct = clientProductRepository.findById(id)
                    .orElseThrow(() -> new RegistrationServiceException("Product-package not found with ID: " + id));

            log.debug("Found client product for ID: {}, productId: {}, packageId: {}", 
                    id, clientProduct.getProductId(), clientProduct.getPackageId());

            // Convert to detailed DTO using existing method
            ClientProductDetailedDto detailedDto = convertToClientProductDetailedDto(clientProduct);

            log.info("Successfully retrieved product-package details for ID: {}", id);
            return detailedDto;

        } catch (RegistrationServiceException e) {
            log.error("Product-package not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving product-package details for ID: {}", id, e);
            throw new RegistrationServiceException("Failed to retrieve product-package details: " + e.getMessage(), e);
        }
    }

    /**
     * Performs manual rollback by deleting all entities created during onboarding
     * This is necessary because MongoDB transactions require replica set configuration
     * 
     * @param adminId Client admin ID to delete
     * @param clientProductIds List of client product IDs to delete
     * @param aspireUserId AspireUser ID to delete (if created)
     * @param invoiceId Billing invoice ID to delete first, if created (best-effort)
     */
    private void performManualRollback(String adminId, List<String> clientProductIds, UUID aspireUserId, String invoiceId) {
        log.info("Performing manual rollback for adminId: {}, clientProductIds: {}, aspireUserId: {}, invoiceId: {}",
                adminId, clientProductIds, aspireUserId, invoiceId);

        try {
            tryDeleteBillingInvoice(invoiceId);

            // 1. Delete ClientProducts first (to avoid foreign key issues if any)
            if (clientProductIds != null && !clientProductIds.isEmpty()) {
                for (String productId : clientProductIds) {
                    try {
                        clientProductRepository.deleteById(productId);
                        log.debug("Deleted ClientProduct with ID: {}", productId);
                    } catch (Exception e) {
                        log.error("Failed to delete ClientProduct with ID: {}. Error: {}", productId, e.getMessage(), e);
                        // Continue with deletion of other entities
                    }
                }
                log.info("Deleted {} ClientProduct entities", clientProductIds.size());
            }
            
            // 2. Delete AspireUser if it was created
            if (aspireUserId != null) {
                try {
                    // Try deleting by ID first (as id is set to baseUserId during creation)
                    aspireUserRepository.deleteById(aspireUserId);
                    log.debug("Deleted AspireUser with ID: {}", aspireUserId);
                } catch (Exception e) {
                    // Fallback: try to find and delete by userId field
                    try {
                        Optional<AspireUser> user = aspireUserRepository.findByUserId(aspireUserId);
                        if (user.isPresent()) {
                            aspireUserRepository.delete(user.get());
                            log.debug("Deleted AspireUser found by userId: {}", aspireUserId);
                        } else {
                            log.warn("AspireUser not found for rollback with userId: {}", aspireUserId);
                        }
                    } catch (Exception e2) {
                        log.error("Failed to delete AspireUser with userId: {}. Error: {}", aspireUserId, e2.getMessage(), e2);
                    }
                }
            }
            
            // 3. Delete ClientAdmin (should be last)
            try {
                clientAdminRepository.deleteById(adminId);
                log.debug("Deleted ClientAdmin with ID: {}", adminId);
            } catch (Exception e) {
                log.error("Failed to delete ClientAdmin with ID: {}. Error: {}", adminId, e.getMessage(), e);
            }
            
            log.info("Manual rollback completed for adminId: {}", adminId);
            
        } catch (Exception e) {
            log.error("Error during manual rollback for adminId: {}. Some data may not have been cleaned up.", adminId, e);
            // Don't throw exception here - we want to ensure the original exception is thrown
        }
    }

    /**
     * Performs rollback for product reassignment by:
     * 1. Deleting the newly created ClientProduct records
     * 2. Reverting the ClientAdmin's clientProductIds to original state
     *
     * @param clientAdminId Client admin ID
     * @param newClientProductIds List of new client product IDs to delete
     * @param originalProductIds Original list of product IDs before reassignment
     * @param invoiceId Billing invoice to remove if it was created (best-effort)
     */
    private void performReassignmentRollback(String clientAdminId, List<String> newClientProductIds,
                                            List<String> originalProductIds, String invoiceId) {
        log.info("Performing reassignment rollback for clientAdminId: {}, newProductIds: {}, invoiceId: {}",
                clientAdminId, newClientProductIds, invoiceId);

        try {
            tryDeleteBillingInvoice(invoiceId);

            // 1. Delete newly created ClientProducts
            if (newClientProductIds != null && !newClientProductIds.isEmpty()) {
                for (String productId : newClientProductIds) {
                    try {
                        clientProductRepository.deleteById(productId);
                        log.debug("Deleted new ClientProduct with ID: {}", productId);
                    } catch (Exception e) {
                        log.error("Failed to delete ClientProduct with ID: {}. Error: {}", productId, e.getMessage(), e);
                        // Continue with deletion of other products
                    }
                }
                log.info("Deleted {} new ClientProduct entities", newClientProductIds.size());
            }
            
            // 2. Revert ClientAdmin's clientProductIds to original state
            try {
                Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(clientAdminId);
                if (clientAdminOpt.isPresent()) {
                    ClientAdmin clientAdmin = clientAdminOpt.get();
                    clientAdmin.setClientProductIds(originalProductIds != null ? originalProductIds : new ArrayList<>());
                    clientAdminRepository.save(clientAdmin);
                    log.debug("Reverted ClientAdmin product IDs for ID: {}", clientAdminId);
                } else {
                    log.warn("ClientAdmin not found for rollback with ID: {}", clientAdminId);
                }
            } catch (Exception e) {
                log.error("Failed to revert ClientAdmin product IDs for ID: {}. Error: {}", clientAdminId, e.getMessage(), e);
            }
            
            log.info("Reassignment rollback completed for clientAdminId: {}", clientAdminId);
            
        } catch (Exception e) {
            log.error("Error during reassignment rollback for clientAdminId: {}. Some data may not have been cleaned up.", clientAdminId, e);
            // Don't throw exception here - we want to ensure the original exception is thrown
        }
    }

    /**
     * Calculate expiry date based on assigned date, validity period, and validity unit
     * @param assignedAt The assignment date
     * @param validityPeriod The validity period (number)
     * @param validityUnit The validity unit (MONTH or YEAR)
     * @return Calculated expiry date
     */
    private Instant calculateExpiryDate(Instant assignedAt, int validityPeriod, String validityUnit) {
        if (assignedAt == null || validityPeriod <= 0) {
            return null;
        }

        try {
            java.time.LocalDateTime assignedDateTime = assignedAt.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            java.time.LocalDateTime expiryDateTime;

            switch (validityUnit.toUpperCase()) {
                case "MONTH":
                    expiryDateTime = assignedDateTime.plusMonths(validityPeriod);
                    break;
                case "YEAR":
                    expiryDateTime = assignedDateTime.plusYears(validityPeriod);
                    break;
                default:
                    log.warn("Unknown validity unit: {}. Using MONTH as default.", validityUnit);
                    expiryDateTime = assignedDateTime.plusMonths(validityPeriod);
                    break;
            }

            return expiryDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            log.error("Error calculating expiry date for assignedAt: {}, validityPeriod: {}, validityUnit: {}", 
                    assignedAt, validityPeriod, validityUnit, e);
            return null;
        }
    }

    @Override
    public String getMspIdByClientAdminId(String clientAdminId) {
        log.info("Retrieving MSP ID for client admin: {}", clientAdminId);

        try {
            ClientAdmin clientAdmin = clientAdminRepository.findById(clientAdminId)
                    .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientAdminId));

            String mspId = clientAdmin.getMspId();
            
            if (mspId == null || mspId.trim().isEmpty()) {
                log.warn("MSP ID is null or empty for client admin: {}", clientAdminId);
                throw new RegistrationServiceException("MSP ID not found for client admin: " + clientAdminId);
            }

            log.info("Successfully retrieved MSP ID: {} for client admin: {}", mspId, clientAdminId);
            return mspId;

        } catch (RegistrationServiceException e) {
            log.error("Error retrieving MSP ID for client admin {}: {}", clientAdminId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error retrieving MSP ID for client admin: {}", clientAdminId, e);
            throw new RegistrationServiceException("Failed to retrieve MSP ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ClientMspListItemDto> getClientsOrMspsByCountry(String country, boolean isClient) {
        log.info("Retrieving clients/MSPs by country: {}, isClient: {}", country, isClient);

        try {
            if (isClient) {
                // Find clients using ClientAdmin model
                List<ClientAdmin> clientAdmins = clientAdminRepository.findByCountry(country);
                
                log.info("Found {} clients for country: {}", clientAdmins.size(), country);
                
                return clientAdmins.stream()
                        .map(clientAdmin -> ClientMspListItemDto.builder()
                                .id(clientAdmin.getId())
                                .email(clientAdmin.getEmail())
                                .organizationName(clientAdmin.getOrganizationName())
                                .build())
                        .toList();
            } else {
                // Find MSPs using MspUser model
                List<MspUser> mspUsers = mspUsersRepository.findByCountry(country);
                
                log.info("Found {} MSPs for country: {}", mspUsers.size(), country);
                
                return mspUsers.stream()
                        .map(mspUser -> ClientMspListItemDto.builder()
                                .id(mspUser.getId())
                                .email(mspUser.getMspAdminEmail() != null ? mspUser.getMspAdminEmail() : mspUser.getContactEmail())
                                .organizationName(mspUser.getOrganizationName())
                                .build())
                        .toList();
            }
        } catch (Exception e) {
            log.error("Error retrieving clients/MSPs by country: {}", country, e);
            throw new RegistrationServiceException("Failed to retrieve clients/MSPs by country: " + e.getMessage(), e);
        }
    }

    @Override
    public OrganizationLicenseStatisticsResponseDto getOrganizationLicenseStatistics(String clientAdminId) {
        log.info("Retrieving organization license statistics for client admin: {}", clientAdminId);

        try {
            // Validate client admin exists
            Optional<ClientAdmin> clientAdmin = clientAdminRepository.findById(clientAdminId);
            if (clientAdmin.isEmpty()) {
                throw new RegistrationServiceException("Client admin not found with ID: " + clientAdminId);
            }

            // Get organization license statistics from repository
            OrganizationLicenseStatistics statistics =
                    clientProductRepositoryCustom.getOrganizationLicenseStatisticsByClientAdminId(clientAdminId);

            log.info("Organization license statistics for client admin {}: Available={}, Allocated={}, Active={}, Expired={}",
                    clientAdminId, statistics.getTotalAvailableLicenses(), statistics.getTotalAllocatedLicenses(),
                    statistics.getTotalActiveLicenses(), statistics.getTotalExpiredLicenses());

            return OrganizationLicenseStatisticsResponseDto.builder()
                    .totalAvailableLicenses(statistics.getTotalAvailableLicenses())
                    .totalAllocatedLicenses(statistics.getTotalAllocatedLicenses())
                    .totalActiveLicenses(statistics.getTotalActiveLicenses())
                    .totalExpiredLicenses(statistics.getTotalExpiredLicenses())
                    .build();

        } catch (RegistrationServiceException e) {
            log.error("Error retrieving organization license statistics for client admin {}: {}", clientAdminId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error retrieving organization license statistics for client admin: {}", clientAdminId, e);
            throw new RegistrationServiceException("Failed to retrieve organization license statistics: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ClientDropdownDto> getClientsByMspId(String mspId) {
        return getClientsDropdownForMsp(mspId, null, null, null);
    }

    @Override
    public List<ClientDropdownDto> getClientsDropdownForMsp(String mspId, String country, String state, String search) {
        log.info("Fetching clients dropdown for mspId: {}, country: {}, state: {}, search: {}",
                mspId, country, state, search);

        if (mspId == null || mspId.isBlank()) {
            throw new RegistationValidationException("MSP ID is required");
        }

        enforceMspResourceAccess(mspId);

        Pageable pageable = PageRequest.of(0, 500);
        Page<ClientAdmin> clientPage = clientAdminRepositoryCustom.findClientAdminsWithFilters(
                search, mspId, null, null, country, state, pageable);

        return clientPage.getContent().stream()
                .map(client -> ClientDropdownDto.builder()
                        .id(client.getId())
                        .organizationName(client.getOrganizationName())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void updateClientAdminStatus(String clientAdminId, AdminStatus status) {
        log.info("Updating client admin status for ID: {} to status: {}", clientAdminId, status);

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        ClientAdmin clientAdmin = clientAdminRepository.findById(clientAdminId)
                .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientAdminId));

        clientAdmin.setStatus(status);
        clientAdminRepository.save(clientAdmin);
        log.info("Updated ClientAdmin status to: {} for ID: {}", status, clientAdminId);

        UUID clientAdminUuid;
        try {
            clientAdminUuid = UUID.fromString(clientAdminId);
        } catch (IllegalArgumentException e) {
            throw new RegistrationServiceException("Invalid client admin ID format: " + clientAdminId);
        }

        Optional<AspireUser> clientAdminUserOpt = aspireUserRepository.findByUserId(clientAdminUuid);
        if (clientAdminUserOpt.isEmpty()) {
            throw new RegistrationServiceException("Client admin AspireUser not found with ID: " + clientAdminId);
        }

        // Update AspireUser status to stay in sync with ClientAdmin
        AspireUser clientAdminUser = clientAdminUserOpt.get();
        String statusString = status.toString();
        clientAdminUser.setStatus(statusString);
        clientAdminUser.setUpdatedAt(Instant.now());
        aspireUserRepository.save(clientAdminUser);
        log.info("Updated client admin status to: {} for ID: {}", statusString, clientAdminId);

        // Handle associated users based on status
        List<String> usersToLogout = new ArrayList<>();
        usersToLogout.add(clientAdminId);

        if (status == AdminStatus.INACTIVE) {
            // Find all ACTIVE users under this client admin
            List<AspireUser> activeUsers = aspireUserRepository.findByClientAdminIdAndStatus(clientAdminId, "ACTIVE");
            
            log.info("Found {} active users under client admin: {}", activeUsers.size(), clientAdminId);
            
            // Update each user to INACTIVE and set isAdminInactive to true
            for (AspireUser user : activeUsers) {
                user.setStatus("INACTIVE");
                user.setIsAdminInactive(true);
                user.setUpdatedAt(Instant.now());
                aspireUserRepository.save(user);
                if (user.getUserId() != null) {
                    usersToLogout.add(user.getUserId().toString());
                }
                log.debug("Updated user {} to INACTIVE with isAdminInactive=true", user.getId());
            }
            
            log.info("Updated {} users to INACTIVE status with isAdminInactive=true", activeUsers.size());
            userSessionInvalidationHelper.logoutUsersIfRestrictive(statusString, usersToLogout);
            
        } else if (status == AdminStatus.ACTIVE) {
            // Find all users with isAdminInactive=true under this client admin
            List<AspireUser> inactiveUsers = aspireUserRepository.findByClientAdminIdAndIsAdminInactive(clientAdminId, true);
            
            log.info("Found {} users with isAdminInactive=true under client admin: {}", inactiveUsers.size(), clientAdminId);
            
            // Update each user to ACTIVE and set isAdminInactive to false
            for (AspireUser user : inactiveUsers) {
                user.setStatus("ACTIVE");
                user.setIsAdminInactive(false);
                user.setUpdatedAt(Instant.now());
                aspireUserRepository.save(user);
                log.debug("Updated user {} to ACTIVE with isAdminInactive=false", user.getId());
            }
            
            log.info("Updated {} users to ACTIVE status with isAdminInactive=false", inactiveUsers.size());
        } else if (userSessionInvalidationHelper.isRestrictiveStatus(statusString)) {
            userSessionInvalidationHelper.logoutUsersIfRestrictive(statusString, usersToLogout);
        }

        log.info("Successfully updated client admin status and associated users for ID: {}", clientAdminId);
    }

    @Override
    @Transactional
    public void updateAspireUserStatus(String userId, UserSuspendRequestDto requestDto) {
        log.info("Updating AspireUser status for userId: {} to status: {}", userId, requestDto.getStatus());

        // Validate status
        String status = requestDto.getStatus();
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        String statusUpper = status.toUpperCase();
        if (!"SUSPEND".equals(statusUpper) && !"ACTIVE".equals(statusUpper)) {
            throw new IllegalArgumentException("Status must be either 'SUSPEND' or 'ACTIVE'");
        }

        // Get current user context
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String currentUserId = context != null ? context.getUserId() : "SYSTEM";

        // Find the AspireUser by userId
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new RegistrationServiceException("Invalid userId format: " + userId);
        }

        Optional<AspireUser> aspireUserOpt = aspireUserRepository.findByUserId(userUuid);
        if (aspireUserOpt.isEmpty()) {
            throw new RegistrationServiceException("AspireUser not found with userId: " + userId);
        }

        AspireUser aspireUser = aspireUserOpt.get();

        if (UserType.CLIENT_ADMIN.name().equals(aspireUser.getUserType())) {
            ClientAdmin clientAdmin = clientAdminRepository.findById(userId)
                    .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + userId));
            clientAdmin.setStatus(AdminStatus.valueOf(statusUpper));
            clientAdminRepository.save(clientAdmin);
            log.info("Updated ClientAdmin status to: {} for ID: {}", statusUpper, userId);
        }

        String clientAdminId = aspireUser.getUserId().toString();

        // Fetch suspend reason details if provided
        String suspendReasonName = null;
        if (requestDto.getSuspendReason() != null && !requestDto.getSuspendReason().isBlank()) {
            try {
                suspendReasonName = requestDto.getSuspendReason();
                log.info("Fetched suspend reason: {}", suspendReasonName);

                // Save suspend reason to UserSuspendReason table only when suspending
                if ("SUSPEND".equals(statusUpper)) {
                    UserSuspendReason userSuspendReason = UserSuspendReason.builder()
                            .id(UUID.randomUUID().toString())
                            .userId(userId)
                            .reason(requestDto.getSuspendReason())
                            .createdAt(Instant.now())
                            .createdBy(currentUserId)
                            .build();

                    userSuspendReasonRepository.save(userSuspendReason);
                    log.info("Saved suspend reason to UserSuspendReason table for userId: {}, reasonId: {}", 
                            userId, requestDto.getSuspendReason());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch or save suspend reason with ID: {}. Error: {}", 
                        requestDto.getSuspendReason(), e.getMessage());
                // Continue without suspend reason details
            }
        }

        // Update AspireUser status
        aspireUser.setStatus(statusUpper);
        aspireUser.setUpdatedBy(currentUserId);
        aspireUser.setUpdatedAt(Instant.now());
        aspireUserRepository.save(aspireUser);
        log.info("Successfully updated AspireUser status to: {} for userId: {}", statusUpper, userId);

        List<String> usersToLogout = new ArrayList<>();
        usersToLogout.add(userId);

        // Handle cascading to associated users based on status
        if ("SUSPEND".equals(statusUpper) && clientAdminId != null && !clientAdminId.isBlank()) {
            // Find all users under this client admin where isAdminInactive is false
            List<AspireUser> usersToSuspend = aspireUserRepository.findByClientAdminIdAndIsAdminInactive(clientAdminId, false);

            log.info("Found {} users to suspend under client admin: {}", usersToSuspend.size(), clientAdminId);

            // Update each user to INACTIVE and set isAdminInactive to true
            for (AspireUser user : usersToSuspend) {
                // Skip the admin user itself as it's already updated
                if (user.getUserId().equals(userUuid)) {
                    continue;
                }
                user.setStatus(statusUpper);
                user.setIsAdminInactive(true);
                user.setUpdatedAt(Instant.now());
                user.setUpdatedBy(currentUserId);
                aspireUserRepository.save(user);
                if (user.getUserId() != null) {
                    usersToLogout.add(user.getUserId().toString());
                }
                log.debug("Updated user {} to INACTIVE with isAdminInactive=true", user.getId());

                // Send email notification for Client User
                sendUserStatusChangeNotification(user, statusUpper, suspendReasonName);
            }

            log.info("Updated {} users to INACTIVE status with isAdminInactive=true", usersToSuspend.size());

            // Send email notification for client admin as well
            sendUserStatusChangeNotification(aspireUser, statusUpper, suspendReasonName);

            userSessionInvalidationHelper.logoutUsersIfRestrictive(statusUpper, usersToLogout);

        } else if ("ACTIVE".equals(statusUpper) && clientAdminId != null && !clientAdminId.isBlank()) {
            // Find all users with isAdminInactive=true under this client admin
            List<AspireUser> usersToReactivate = aspireUserRepository
                    .findByClientAdminIdAndStatusAndIsAdminInactive(clientAdminId, "SUSPEND", true);

            log.info("Found {} users to reactivate under client admin: {}", usersToReactivate.size(), clientAdminId);

            // Update each user to ACTIVE and set isAdminInactive to false
            for (AspireUser user : usersToReactivate) {
                user.setStatus("ACTIVE");
                user.setIsAdminInactive(false);
                user.setUpdatedAt(Instant.now());
                user.setUpdatedBy(currentUserId);
                aspireUserRepository.save(user);
                log.debug("Updated user {} to ACTIVE with isAdminInactive=false", user.getId());
                sendUserStatusChangeNotification(aspireUser, statusUpper, suspendReasonName);
            }

            log.info("Updated {} users to ACTIVE status with isAdminInactive=false", usersToReactivate.size());
            sendUserStatusChangeNotification(aspireUser, statusUpper, suspendReasonName);
        } else if (userSessionInvalidationHelper.isRestrictiveStatus(statusUpper)) {
            userSessionInvalidationHelper.logoutUsersIfRestrictive(statusUpper, usersToLogout);
        }

        log.info("Successfully updated AspireUser status and associated users for userId: {}", userId);
    }

    /**
     * Sends email notification to user about their status change (suspended/activated).
     *
     * @param aspireUser The AspireUser whose status changed
     * @param status The new status (SUSPEND or ACTIVE)
     * @param suspendReasonName The name of the suspend reason (can be null)
     */
    private void sendUserStatusChangeNotification(AspireUser aspireUser, String status, String suspendReasonName) {
        try {
            String userFullName = (aspireUser.getFirstName() != null ? aspireUser.getFirstName() : "") +
                    (aspireUser.getLastName() != null ? " " + aspireUser.getLastName() : "").trim();
            if (userFullName.isBlank()) {
                userFullName = aspireUser.getEmail();
            }

            notificationServiceClient.sendUserStatusChangeNotification(
                    aspireUser.getEmail(),
                    status,
                    suspendReasonName,
                    userFullName,
                    aspireUser.getCompanyName() != null ? aspireUser.getCompanyName() : ""
            );
            log.info("Status change notification sent to user: {}", aspireUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send status change notification to user: {}. Error: {}", 
                    aspireUser.getEmail(), e.getMessage(), e);
            // Don't fail the operation if notification fails
        }
    }

    /**
     * Validates the invoice total price by calculating expected price from CMS package range pricing.
     * Loops through each product selection, fetches the price per user from CMS,
     * calculates total by multiplying with license count, and compares with the invoice subtotal.
     *
     * @param subtotal The subtotal amount from the invoice to validate against
     * @param productSelections The product selection DTO containing product selections and total License counts
     * @throws RegistationValidationException if the calculated total doesn't match the provided subtotal
     */
    private double validateInvoiceTotalPrice(Double subtotal, List<ProductSelectionDto> productSelections) {
        if (productSelections == null || productSelections.isEmpty()) {
            log.info("No product selections provided, skipping price validation");
            throw new RegistationValidationException(" No product selections provided for invoice price validation.");
        }

        double calculatedSubtotal = 0.0;

        for (ProductSelectionDto productSelection : productSelections) {
            String packageId = productSelection.getPackageId();
            Integer licenseCount = productSelection.getLicenseCount();
            Integer validityPeriod = productSelection.getValidityPeriod();
            ValidityUnit validityUnit = productSelection.getValidityUnit();
            Double pricePerLicense = productSelection.getPricePerLicense();

            if (licenseCount == null || licenseCount <= 0) {
                log.error("Invalid license count: {} for packageId: {}", licenseCount, packageId);
                throw new RegistationValidationException(
                        "Invalid license count. License count must be greater than 0 for packageId: " + packageId);
            }

            if (pricePerLicense == null || pricePerLicense <= 0) {
                log.error("Price per license is null or invalid for packageId: {}", packageId);
                throw new RegistationValidationException(
                        "Invalid pricing. Price per license must be greater than 0 for packageId: " + packageId);
            }

            int totalTimeUnit = calculateTotalMonths(validityPeriod, validityUnit);
            double productTotal = calculateProductTotal(pricePerLicense, licenseCount, totalTimeUnit);

            log.debug("Product price calculation - packageId: {}, pricePerLicense: {}, licenseCount: {}, totalMonths: {}, productTotal: {}",
                    packageId, pricePerLicense, licenseCount, totalTimeUnit, productTotal);

            calculatedSubtotal += productTotal;
        }

        log.info("Invoice price validation successful. Calculated subtotal: {}, Provided subtotal: {}",
                calculatedSubtotal, subtotal);

        return MoneyUtil.round(calculatedSubtotal);
    }

    /**
     * Convert billing module's InvoiceStatus to common module's InvoiceStatus
     */
    private com.aspire.asat.common.enums.InvoiceStatus convertToCommonInvoiceStatus(InvoiceStatus invStatus) {
        if (invStatus == null) {
            return com.aspire.asat.common.enums.InvoiceStatus.PENDING;
        }
        return com.aspire.asat.common.enums.InvoiceStatus.valueOf(invStatus.name());
    }

    /**
     * Convert billing module's ProductSelectionDto to common module's ProductSelectionDto
     */
    private com.aspire.asat.common.dto.ProductSelectionDto convertToCommonProductSelectionDto(ProductSelectionDto invDto) {
        if (invDto == null) {
            return null;
        }
        return com.aspire.asat.common.dto.ProductSelectionDto.builder()
                .productId(invDto.getProductId())
                .packageId(invDto.getPackageId())
                .licenseCount(invDto.getLicenseCount())
                .pricePerLicense(invDto.getPricePerLicense())
                .validityPeriod(invDto.getValidityPeriod())
                .validityUnit(invDto.getValidityUnit() != null
                        ? com.aspire.asat.common.dto.ValidityUnit.valueOf(invDto.getValidityUnit().name())
                        : null)
                .productName(invDto.getProductName())
                .packageName(invDto.getPackageName())
                .build();
    }

    /**
     * Calculate VAT percentage based on VAT amount, subtotal, and discount.
     *
     * @param vatAmount the VAT amount
     * @param subtotal  the subtotal before discount
     * @param discount  the discount amount
     * @return the VAT percentage, or 0.0 if VAT amount is zero or base amount is zero
     */
    private double calculateVatPercentage(double vatAmount, double subtotal, double discount) {
        if (vatAmount <= 0) {
            return 0.0;
        }
        double baseAmount = subtotal - discount;
        if (baseAmount <= 0) {
            return 0.0;
        }
        return (vatAmount / baseAmount) * 100.0;
    }

    /**
     * Calculate product total based on price per unit, license count, and total months.
     *
     * @param pricePerUnit the price per user or per license
     * @param licenseCount the number of licenses
     * @param totalTimeUnit  the total months (validity period converted to months)
     * @return the calculated product total
     */
    private double calculateProductTotal(double pricePerUnit, int licenseCount, int totalTimeUnit) {
        return MoneyUtil.round(pricePerUnit * licenseCount * totalTimeUnit);
    }

    /**
     * Calculate total months based on validity period and validity unit.
     *
     * @param validityPeriod the validity period value
     * @param validityUnit   the validity unit (DAYS, MONTH, or YEAR)
     * @return the total months
     */
    private int calculateTotalMonths(Integer validityPeriod, ValidityUnit validityUnit) {
        if (validityPeriod == null || validityPeriod <= 0) {
            return 0;
        }
        if (validityUnit == null) {
            return validityPeriod; // Default to MONTH behavior if null
        }
        return switch (validityUnit) {
            case DAYS -> validityPeriod / 30;
            case YEAR -> validityPeriod * 1;
            case MONTH -> validityPeriod;
        };
    }

    /**
     * Resolves MSP ID for client product assignment/reassignment.
     * MSP users are always scoped to their own MSP ID from the security context.
     */
    private String resolveMspIdForClientAssignment(String requestMspId, ClientAdmin clientAdmin) {
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (userContext != null && UserType.MSP.name().equalsIgnoreCase(userContext.getUserType())) {
            String loggedInMspId = userContext.getUserId();
            if (requestMspId != null && !requestMspId.isBlank() && !requestMspId.equals(loggedInMspId)) {
                throw new RegistrationServiceException("MSP ID does not match logged-in MSP user");
            }
            log.info("MSP Admin user detected, using logged-in MSP ID: {}", loggedInMspId);
            return loggedInMspId;
        }

        if (requestMspId == null || requestMspId.isBlank()) {
            String clientMspId = clientAdmin.getMspId();
            log.info("MSP ID not provided, using client admin's existing MSP ID: {}", clientMspId);
            return clientMspId;
        }

        return requestMspId;
    }

    /**
     * Ensures an MSP user can only access resources belonging to their own MSP.
     */
    private void enforceMspResourceAccess(String mspId) {
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (userContext != null
                && UserType.MSP.name().equalsIgnoreCase(userContext.getUserType())
                && !mspId.equals(userContext.getUserId())) {
            throw new RegistrationServiceException("Access denied: MSP can only access own resources");
        }
    }

    /**
     * Validates that the MSP has enough active licenses for each product/package being assigned to a client.
     */
    private void validateMspLicenseAvailabilityForAssignment(String mspId, List<ProductSelectionDto> selections) {
        if (selections == null || selections.isEmpty()) {
            return;
        }

        for (ProductSelectionDto selection : selections) {
            if (selection == null) {
                continue;
            }

            String productId = selection.getProductId();
            String packageId = selection.getPackageId();
            Integer licenseCount = selection.getLicenseCount();
            String productLabel = selection.getProductName() != null ? selection.getProductName() : productId;

            if (productId == null || packageId == null || licenseCount == null || licenseCount <= 0) {
                throw new RegistationValidationException(
                        "Valid productId, packageId, and licenseCount are required for MSP license validation");
            }

            MspProduct mspProduct = mspProductRepository.findByMspIdAndProductIdAndPackageId(mspId, productId, packageId);
            if (mspProduct == null) {
                throw new RegistationValidationException(
                        "MSP does not have the selected product/package assigned: " + productLabel);
            }

            if (!isActiveMspProductForAssignment(mspProduct)) {
                throw new RegistationValidationException(
                        "MSP product license is not active for: " + productLabel);
            }

            int availableLicenses = mspProduct.getLicenseCount() - mspProduct.getUsedLicenseCount();
            if (licenseCount > availableLicenses) {
                throw new RegistationValidationException(String.format(
                        "Insufficient MSP licenses for %s. Requested: %d, Available: %d",
                        productLabel, licenseCount, availableLicenses));
            }
        }
    }

    private boolean isActiveMspProductForAssignment(MspProduct product) {
        if (product == null || !"ACTIVE".equalsIgnoreCase(product.getLicenseStatus())) {
            return false;
        }
        return product.getExpiryDate() == null || product.getExpiryDate().isAfter(Instant.now());
    }

    /**
     * Updates the used license count for MSP products based on product selections.
     * For each product selection, finds the corresponding MspProduct and adds the license count
     * to its usedLicenseCount field.
     *
     * @param mspId The MSP ID
     * @param selections List of product selections from the onboarding request
     */
    private void updateMspProductUsedLicenseCount(String mspId, List<ProductSelectionDto> selections) {
        if (selections == null || selections.isEmpty()) {
            log.debug("No product selections provided, skipping MSP product license count update");
            return;
        }

        log.info("Updating MSP product used license count for mspId: {} with {} product selections", mspId, selections.size());

        for (ProductSelectionDto selection : selections) {
            String productId = selection.getProductId();
            String packageId = selection.getPackageId();
            Integer licenseCount = selection.getLicenseCount();

            if (productId == null || packageId == null || licenseCount == null || licenseCount <= 0) {
                log.warn("Skipping invalid product selection: productId={}, packageId={}, licenseCount={}", 
                        productId, packageId, licenseCount);
                continue;
            }

            try {
                MspProduct mspProduct = mspProductRepository.findByMspIdAndProductIdAndPackageId(mspId, productId, packageId);
                
                if (mspProduct == null) {
                    log.warn("MspProduct not found for mspId: {}, productId: {}, packageId: {}. Skipping license count update.", 
                            mspId, productId, packageId);
                    continue;
                }

                int currentUsedLicenseCount = mspProduct.getUsedLicenseCount();
                int newUsedLicenseCount = currentUsedLicenseCount + licenseCount;
                
                mspProduct.setUsedLicenseCount(newUsedLicenseCount);
                mspProductRepository.save(mspProduct);
                
                log.info("Updated MspProduct usedLicenseCount: mspId={}, productId={}, packageId={}, " +
                        "currentUsedLicenseCount={}, addedLicenseCount={}, newUsedLicenseCount={}", 
                        mspId, productId, packageId, currentUsedLicenseCount, licenseCount, newUsedLicenseCount);
                        
            } catch (Exception e) {
                log.error("Failed to update MspProduct usedLicenseCount for mspId: {}, productId: {}, packageId: {}. Error: {}", 
                        mspId, productId, packageId, e.getMessage(), e);
                // Continue processing other selections even if one fails
            }
        }
    }

}
