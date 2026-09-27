package com.aspire.asat.registration.service.msp.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.dto.invoice_logs.CommentLogRequestDTO;
import com.aspire.asat.common.dto.notification.NotificationTemplateValue;
import com.aspire.asat.common.enums.Currency;
import com.aspire.asat.common.enums.TokenActionType;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.common.service.files.FileService;
import com.aspire.asat.common.util.MoneyUtil;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.common.dto.notification.AttachmentDto;
import com.aspire.asat.registration.data.EmbeddedPaymentPayload;
import com.aspire.asat.registration.data.OrganizationLicenseStatistics;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.clientAdmin.request.InvoiceDetailsDto;
import com.aspire.asat.registration.data.clientAdmin.request.ProductSelectionDto;
import com.aspire.asat.registration.data.clientAdmin.request.ValidityUnit;
import com.aspire.asat.registration.data.clientAdmin.response.OrganizationLicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.EmbeddedPackageItem;
import com.aspire.asat.registration.data.cms.request.CmsProductPackagePairDto;
import com.aspire.asat.registration.data.cms.request.CmsTopicCountsByProductPackagesRequestDto;
import com.aspire.asat.registration.data.cms.request.CmsTopicsByProductPackagesRequestDto;
import com.aspire.asat.registration.data.cms.request.TrialSubPackageCreationRequestDto;
import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import com.aspire.asat.registration.data.cms.response.CmsPackageSimpleResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsFeatureDto;
import com.aspire.asat.registration.data.cms.response.CmsFullProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsProductWithSinglePackageResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsTopicMinimalDto;
import com.aspire.asat.registration.data.cms.response.CmsTopicPageResponseDto;
import com.aspire.asat.registration.data.mspUser.request.MspClientProductTopicsRequestDto;
import com.aspire.asat.registration.service.pricing.ProductPriceResolver;
import com.aspire.asat.registration.service.support.UserSessionInvalidationHelper;
import com.aspire.asat.registration.data.coupon.CouponResponseDTO;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.data.enums.MspStatus;
import com.aspire.asat.registration.data.enums.RoleType;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.invoice.InvoiceRequestDTO;
import com.aspire.asat.registration.data.invoice.InvoiceResponseDTO;
import com.aspire.asat.registration.data.invoice.InvoiceStatus;
import com.aspire.asat.registration.data.mspUser.LicenseStatus;
import com.aspire.asat.registration.data.mspUser.request.*;
import com.aspire.asat.registration.data.mspUser.response.*;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.mapper.MspUserMapper;
import com.aspire.asat.registration.model.*;
import com.aspire.asat.registration.model.dropdown.Country;
import com.aspire.asat.registration.model.dropdown.Industry;
import com.aspire.asat.registration.model.dropdown.SubIndustry;
import com.aspire.asat.registration.model.msp.MspInvoice;
import com.aspire.asat.registration.model.msp.MspProduct;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.repository.*;
import com.aspire.asat.registration.repository.custom.MspUsersRepositoryCustom;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.repository.dropdown.IndustryRepository;
import com.aspire.asat.registration.repository.dropdown.MspTypeRepository;
import com.aspire.asat.registration.repository.dropdown.OrganizationSizeRepository;
import com.aspire.asat.registration.repository.dropdown.StateRepository;
import com.aspire.asat.registration.repository.dropdown.SubIndustryRepository;
import com.aspire.asat.registration.repository.metadata.OrganizationTypeRepository;
import com.aspire.asat.registration.repository.msp.MspInvoiceRepository;
import com.aspire.asat.registration.repository.msp.MspProductRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.external.BillingService;
import com.aspire.asat.registration.service.external.InvoiceService;
import com.aspire.asat.registration.service.msp.MspUserService;
import com.aspire.asat.registration.utils.CommonUtils;
import com.aspire.asat.common.util.InvoiceGenerator;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MspUserServiceImpl implements MspUserService {

    private final MspProductRepository mspProductRepository;
    private final MspUsersRepository mspUsersRepository;
    private final MspUsersRepositoryCustom mspUsersRepositoryCustom;
    private final MspInvoiceRepository mspInvoiceRepository;
    private final AspireUserRepository aspireUserRepository;
    private final RoleRepository roleRepository;
    private final TierConfigurationRepository tierConfigurationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AspireUserService aspireUserService;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final BillingService billingService;
    private final InvoiceService invoiceService;
    private final InvoiceGenerator invoiceGenerator;
    private final FileService fileService;
    private final RegistrationNotificationClient notificationServiceClient;
    private final UserCurrentContextService currentContextService;
    private final NetTermConfigurationRepository netTermConfigurationRepository;
    private final ClientAdminRepository clientAdminRepository;
    private final ClientProductRepository clientProductRepository;
    private final CmsServiceClient cmsServiceClient;
    private final UserLoginHistoryRepository userLoginHistoryRepository;
    private final WebClient webClient;
    private final IndustryRepository industryRepository;
    private final SubIndustryRepository subIndustryRepository;
    private final OrganizationSizeRepository organizationSizeRepository;
    private final OrganizationTypeRepository organizationTypeRepository;
    private final MspTypeRepository mspTypeRepository;
    private final ProductPriceResolver productPriceResolver;
    private final UserSessionInvalidationHelper userSessionInvalidationHelper;

    @Value("${aws.s3.bucket-name}")
    private String s3BucketName;

    @Value("${service.billing.url}")
    private String billingServiceUrl;

    @Value("${link.login}")
    private String logInUrl;

    @Override
    @Transactional
    public MspOnboardingResponseDto processMspOnboarding(MspOnboardingRequestDto requestDto) {
        OrganizationInfoForMspDto org = requestDto.getOrganization();
        List<ProductSelectionDto> selections = requestDto.getProductSelections();

        // Standardize to use mspAdminEmail, fallback to adminEmail if not provided
        String adminEmail = org.getMspAdminEmail();

        log.info("Processing MSP onboarding for admin email: {}", adminEmail);

        existenceValidation(adminEmail);

        resolveOrganizationIndustryAndSubIndustry(org);

        // Note: Coupon and discount can be applied together
        // Order: First apply coupon (if exists), then apply discount (if exists), then calculate VAT


        String tempPassword = CommonUtils.generateTemporaryPassword();
        String adminId = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(tempPassword);

        UUID aspireUserId = null;
        List<String> mspProductIds = null;
        MspUser mspUser = null;

        CurrentUserContext userContext = null;

        try {
            userContext = currentContextService.getCurrentUserContext();
        } catch (Exception e){
            log.warn("Could not get current user context, using SYSTEM as default. Error: {}", e.getMessage());
            // Create a minimal context with SYSTEM as userId
            userContext = new CurrentUserContext();
            userContext.setUserId("SYSTEM");
        }

        try {
            // Create MSP products first
            productPriceResolver.applyAuthoritativePricing(selections);
            mspProductIds = createAndSaveMspProducts(selections, adminId, org, requestDto);

            // Create MSP user in new MSP_USER table
            mspUser = MspUserMapper.mapToMspUser(requestDto, adminId, mspProductIds, userContext, adminEmail);

            // Calculate credit end date if credit allocation is enabled
            if (ObjectUtils.isNotEmpty(requestDto.getCreditInfo()) && Boolean.TRUE.equals(requestDto.getCreditInfo().getEnableCredit())) {
                calculateCreditEndDate(requestDto, mspUser);
            }

            mspUsersRepository.save(mspUser);
            log.info("Created MSP user in MSP_USER table with ID: {}", adminId);

            // Create centralized AspireUser record directly from MspUser
            aspireUserId = createAspireUserRecord(mspUser, adminId, hashedPassword, adminEmail);

            // Create invoice
            InvoiceRequestDTO invoiceRequestDTO = buildInvoiceRequestDTO(adminId, org, mspProductIds, selections, requestDto);
            InvoiceResponseDTO invoiceResponse = invoiceService.createInvoiceForMsp(invoiceRequestDTO);
            log.info("Invoice created for MSP onboarding: {}", invoiceResponse.getId());

            // Save invoice to MSP_INVOICE table
            MspInvoice mspInvoice = mapToMspInvoice(invoiceResponse, invoiceRequestDTO, userContext, mspUser, adminEmail);
            mspInvoiceRepository.save(mspInvoice);
            log.info("Invoice saved to MSP_INVOICE table with ID: {}", mspInvoice.getId());

            // Update MSP user with invoice information
            MspUserMapper.updateMspUserWithInvoice(mspUser, invoiceResponse.getId(), InvoiceStatus.CREATED.name());
            mspUsersRepository.save(mspUser);

            // Create credit if needed
            createCreditIfNeeded(requestDto, adminId, mspUser);

            // Send notifications with invoice PDF attachment
            sendMspOnboardingNotifications(org, invoiceResponse, invoiceRequestDTO, adminId, tempPassword);

            // Keep status as PENDING (aligned with client onboarding flow)
            // Status will be updated to ACTIVE via separate activation step if needed
            log.info("MSP onboarding completed successfully for admin ID: {}", adminId);

            return MspUserMapper.mapToResponseDto(mspUser, tempPassword);

        } catch (Exception e) {
            log.error("Error during MSP onboarding for admin ID: {}. Performing rollback.", adminId, e);
            // Perform rollback on any failure
            performManualRollback(adminId, mspProductIds, aspireUserId, mspUser);
            throw new RegistrationServiceException("MSP onboarding failed: " + e.getMessage(), e);
        }
    }

    @Override
    public AllResponseDto<List<MspOnboardedListResponseDto>> getOnboardedMsps(
            Integer offset, Integer pageSize, String search,
            MspStatus status, Integer minLicenses, Integer maxLicenses) {

        Map<String, CmsFullProductResponseDto> fullProductListMap = cmsServiceClient.fetchAndCacheAllProducts();

        log.info("Fetching onboarded MSPs - offset: {}, pageSize: {}, search: {}, status: {}, minLicenses: {}, maxLicenses: {}",
                offset, pageSize, search, status, minLicenses, maxLicenses);

        if (offset == null || offset < 0) offset = 0;
        if (pageSize == null || pageSize <= 0) pageSize = 10;

        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<MspUser> mspPage = mspUsersRepositoryCustom.findMspUsersWithFilters(search, status, pageable);

        List<MspOnboardedListResponseDto> mspList = mspPage.getContent().stream()
                .map(msp -> convertToOnboardedListDto(msp, fullProductListMap))
                .filter(dto -> filterByLicenseCount(dto, minLicenses, maxLicenses))
                .toList();

        long totalElements = mspPage.getTotalElements();

        if (mspList.isEmpty()) {
            log.info("No onboarded MSPs found");
        } else {
            log.info("Retrieved {} MSPs out of {} total", mspList.size(), totalElements);
        }

        return new AllResponseDto<>(offset, pageSize, totalElements, mspList);
    }

    private MspOnboardedListResponseDto convertToOnboardedListDto(MspUser mspUser, Map<String, CmsFullProductResponseDto> fullProductListMap) {
        List<MspProduct> products = mspProductRepository.findByMspId(mspUser.getId());
        List<MspInvoice> invoices = mspInvoiceRepository.findByMspId(mspUser.getId());
        List<ClientAdmin> clients = clientAdminRepository.findAllByMspId(mspUser.getId());
        Optional<UserLoginHistory> loginHistoriy = userLoginHistoryRepository.findTopByUsernameIgnoreCaseAndActionOrderByLoginTimeDesc(mspUser.getMspAdminEmail(), TokenActionType.LOGIN);

        double revenue = invoices.stream()
                .mapToDouble(MspInvoice::getTotalAmount)
                .sum();

        int totalLicenses = products.stream()
                .mapToInt(MspProduct::getLicenseCount)
                .sum();

        int usedLicenses = products.stream()
                .mapToInt(MspProduct::getUsedLicenseCount)
                .sum();
        List<String> productNameList = products.stream()
                .map(mspProduct -> fullProductListMap.get(mspProduct.getProductId()))
                .filter(Objects::nonNull)
                .map(CmsFullProductResponseDto::getProductName)
                .collect(Collectors.toList());

        MspStatus mspStatus = MspStatus.PENDING;
        try {
            if (mspUser.getStatus() != null) {
                mspStatus = MspStatus.valueOf(mspUser.getStatus());
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status for MSP {}: {}", mspUser.getId(), mspUser.getStatus());
        }

        Instant lastLoginAt = loginHistoriy.map(UserLoginHistory::getLoginTime).orElse(null);

        return MspOnboardedListResponseDto.builder()
                .id(mspUser.getId())
                .mspId(mspUser.getMspId())
                .organizationName(mspUser.getOrganizationName())
                .status(mspStatus)
                .licenseCount(totalLicenses)
                .usedLicenseCount(usedLicenses)
                .joinedDate(mspUser.getCreatedAt())
                .contactEmail(mspUser.getContactEmail())
                .phoneNumber(mspUser.getPhoneNumber())
                .productLists(productNameList)
                .revenue(revenue)
                .totalClients(clients.size())
                .lastLoginAt(lastLoginAt)
                .creditAmount(mspUser.getCreditAmount() != null ? mspUser.getCreditAmount().doubleValue() : 0.0)
                .build();
    }

    private boolean filterByLicenseCount(MspOnboardedListResponseDto dto, Integer minLicenses, Integer maxLicenses) {
        if (minLicenses != null && dto.getLicenseCount() < minLicenses) {
            return false;
        }
        if (maxLicenses != null && dto.getLicenseCount() > maxLicenses) {
            return false;
        }
        return true;
    }


    private void calculateCreditEndDate(MspOnboardingRequestDto requestDto, MspUser mspUser) {
        CreditInfoDto creditInfo = requestDto.getCreditInfo();
        NetTermConfiguration netTermConfig = netTermConfigurationRepository.findById(creditInfo.getNetDaysId())
                .orElseGet(() -> {
                    NetTermConfiguration ntc = new NetTermConfiguration();
                    ntc.setNetTermInDays(30);
                    return ntc;
                });
        Date startDate = creditInfo.getCreditStartDate() != null ? creditInfo.getCreditStartDate() : Date.from(Instant.now());
        LocalDateTime startLdt = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime endLdt = startLdt.plusDays(Math.max(0, netTermConfig.getNetTermInDays()));
        Date creditEndDate = Date.from(endLdt.atZone(ZoneId.systemDefault()).toInstant());


        mspUser.setCreditEndDate(creditEndDate);
    }

    private void existenceValidation(String adminEmail) {
        if (mspUsersRepository.existsByMspAdminEmailIgnoreCase(adminEmail)) {
            throw new ResourceAlreadyExistsException("An MSP user with this email already exists: " + adminEmail);
        }

        aspireUserService.getUserByEmail(adminEmail).ifPresent(user -> {
            throw new ResourceAlreadyExistsException("An Aspire user with this email already exists: " + adminEmail);
        });
    }

    private void resolveOrganizationIndustryAndSubIndustry(OrganizationInfoForMspDto org) {
        String industryId = findOrCreateIndustry(org.getIndustry());
        org.setIndustry(industryId);
        if (org.getSubIndustry() != null && !org.getSubIndustry().isBlank()) {
            org.setSubIndustry(findOrCreateSubIndustry(org.getSubIndustry(), industryId));
        }
    }

    private String findOrCreateIndustry(String value) {
        if (value == null || value.isBlank()) {
            throw new RegistationValidationException("Industry is required");
        }

        String trimmedValue = value.trim();

        Optional<Industry> existingIndustry = industryRepository.findById(trimmedValue)
                .or(() -> industryRepository.findByCode(trimmedValue))
                .or(() -> industryRepository.findByNameIgnoreCase(trimmedValue));

        if (existingIndustry.isPresent()) {
            return existingIndustry.get().getId();
        }

        Instant now = Instant.now();
        Industry industry = Industry.builder()
                .id(UUID.randomUUID().toString())
                .code(generateUniqueIndustryCode(trimmedValue))
                .name(trimmedValue)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Industry savedIndustry = industryRepository.save(industry);
        log.info("Created industry during MSP onboarding: {} ({})", savedIndustry.getName(), savedIndustry.getId());
        return savedIndustry.getId();
    }

    private String findOrCreateSubIndustry(String value, String industryId) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmedValue = value.trim();

        Optional<SubIndustry> existingSubIndustry = subIndustryRepository.findById(trimmedValue)
                .or(() -> subIndustryRepository.findByIndustryIdAndCode(industryId, trimmedValue))
                .or(() -> subIndustryRepository.findByIndustryIdAndNameIgnoreCase(industryId, trimmedValue));

        if (existingSubIndustry.isPresent()) {
            return existingSubIndustry.get().getId();
        }

        Instant now = Instant.now();
        SubIndustry subIndustry = SubIndustry.builder()
                .id(UUID.randomUUID().toString())
                .industryId(industryId)
                .code(generateUniqueSubIndustryCode(trimmedValue, industryId))
                .name(trimmedValue)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        SubIndustry savedSubIndustry = subIndustryRepository.save(subIndustry);
        log.info("Created sub-industry during MSP onboarding: {} ({})", savedSubIndustry.getName(), savedSubIndustry.getId());
        return savedSubIndustry.getId();
    }

    private String generateUniqueIndustryCode(String name) {
        return generateUniqueDropdownCode(name, industryRepository::existsByCode);
    }

    private String generateUniqueSubIndustryCode(String name, String industryId) {
        return generateUniqueDropdownCode(name,
                code -> subIndustryRepository.existsByIndustryIdAndCodeIgnoreCase(industryId, code));
    }

    private String generateUniqueDropdownCode(String name, java.util.function.Predicate<String> codeExists) {
        String baseCode = generateDropdownCode(name);
        String code = baseCode;
        int suffix = 1;
        while (codeExists.test(code)) {
            code = baseCode + "_" + suffix++;
        }
        return code;
    }

    private String generateDropdownCode(String name) {
        String baseCode = name.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_|_$", "");
        if (baseCode.isEmpty()) {
            baseCode = "ITEM";
        }
        if (baseCode.length() > 50) {
            baseCode = baseCode.substring(0, 50);
        }
        return baseCode;
    }

    /**
     * Validates tier configuration if tierId is provided.
     * Ensures the tier exists and is active.
     *
     * @param tierId The tier ID to validate (can be null/blank if tier is optional)
     * @throws RegistrationServiceException if tier is invalid or inactive
     */
    private void validateTierConfiguration(String tierId) {
        // Tier is optional, skip validation if not provided
        if (tierId == null || tierId.isBlank()) {
            log.error("Tier ID not provided, skipping tier validation");
            return;
        }

        log.info("Validating tier configuration for tierId: {}", tierId);

        TierConfiguration tier = tierConfigurationRepository.findById(tierId)
                .orElseThrow(() -> new RegistrationServiceException(
                        "Tier configuration not found with ID: " + tierId + ". Please ensure the tier exists and is configured by admin."));

        if (!tier.isActive()) {
            throw new RegistrationServiceException(
                    "Tier configuration is not active for ID: " + tierId + ". Please select an active tier or contact admin.");
        }

        log.info("Tier validation successful for tierId: {} (Tier: {}, Commission: {}%)",
                tierId, tier.getTierName(), tier.getCommissionPercentage());
    }


    /**
     * Creates and saves MSP products for the selected products and packages
     *
     * @param selections The product selections from the onboarding request (flat structure)
     * @param adminId    The MSP admin ID
     * @param org        The organization information
     * @param requestDto The MSP onboarding request DTO
     * @return List of created MSP product IDs
     */
    private List<String> createAndSaveMspProducts(List<ProductSelectionDto> selections, String adminId, OrganizationInfoForMspDto org, MspOnboardingRequestDto requestDto) {
        List<String> mspProductIds = new ArrayList<>();
        for (ProductSelectionDto selection : selections) {
            EmbeddedPackageItem item = new EmbeddedPackageItem();
            item.setPackageId(selection.getPackageId());
            item.setPrice(selection.getPricePerLicense());

            EmbeddedPaymentPayload payload = EmbeddedPaymentPayload.builder()
                    .clientId(adminId)
                    .currency(Currency.USD.name())
                    .amount(requestDto.getInvoice().getTotalAmount())
                    .subtotal(requestDto.getInvoice().getSubtotal())
                    .vatAmount(requestDto.getInvoice().getVatAmount())
                    .discountAmount(requestDto.getInvoice().getDiscountAmount())
                    .discountPercentage(requestDto.getInvoice().getDiscountPercentage())
                    .date(new Date())
                    .notes("Invoice auto-generated for MSP onboarding")
                    .clientRegion(org.getCountry())
                    .packageItems(List.of(item))
                    .paymentSources(Collections.emptyList())
                    .build();

            Instant assignedAt = Instant.now();
            Instant expiryDate = calculateExpiryDate(
                    assignedAt,
                    selection.getValidityPeriod(),
                    convertValidityUnit(selection.getValidityUnit())
            );

            MspProduct product = MspProduct.builder()
                    .id(UUID.randomUUID().toString())
                    .mspId(adminId)
                    .productId(selection.getProductId())
                    .packageId(selection.getPackageId())
                    .licenseCount(selection.getLicenseCount())
                    .usedLicenseCount(0)
                    .pricePerLicense(selection.getPricePerLicense())
                    .totalPrice(selection.getLicenseCount() * selection.getPricePerLicense())
                    .validityPeriod(selection.getValidityPeriod())
                    .validityUnit(selection.getValidityUnit().toString())
                    .assignedAt(assignedAt)
                    .expiryDate(expiryDate)
                    .licenseStatus(LicenseStatus.PENDING.name())
                    .paymentPayload(payload)
                    .countryId(org.getCountry())
                    .build();

            mspProductRepository.save(product);
            mspProductIds.add(product.getId());
            log.debug("Created MSP product: {} for MSP: {}", product.getId(), adminId);
        }
        log.info("Created {} MSP products for MSP: {}", mspProductIds.size(), adminId);
        return mspProductIds;
    }

    /**
     * Converts ValidityUnit enum to ValidityUnitForMspDto enum for backward compatibility
     * Note: Both enums have the same values (MONTH, YEAR), so this is a simple mapping
     */
    private ValidityUnitForMspDto convertValidityUnit(ValidityUnit validityUnit) {
        if (validityUnit == null) {
            return ValidityUnitForMspDto.MONTH; // Default
        }
        return switch (validityUnit) {
            case DAYS -> ValidityUnitForMspDto.DAYS;
            case MONTH -> ValidityUnitForMspDto.MONTH;
            case YEAR -> ValidityUnitForMspDto.YEAR;
        };
    }

    /**
     * Creates AspireUser record directly from MspUser using AspireUserRepository
     *
     * @param mspUser        The MspUser entity
     * @param adminId        The MSP admin ID
     * @param hashedPassword The hashed password
     * @param adminEmail     The admin email address
     * @return The UUID of the created AspireUser, or null if creation failed
     */
    private UUID createAspireUserRecord(MspUser mspUser, String adminId, String hashedPassword, String adminEmail) {
        try {
            UUID baseUserId = UUID.fromString(adminId);

            // Check if user already exists by userId or by username (case-insensitive)
            if (aspireUserRepository.existsByUserId(baseUserId)) {
                log.warn("AspireUser already exists for baseUserId: {}", adminId);
                return baseUserId;
            }
            if (aspireUserRepository.existsByUsernameIgnoreCase(adminEmail)) {
                throw new ResourceAlreadyExistsException("User with username/email " + adminEmail + " already exists");
            }

            // Convert role names to role IDs
            List<String> roleIds = convertRoleNamesToIds(List.of(UserType.MSP.name()));

            // Extract first and last name from organization name
            String[] nameParts = extractNameParts(mspUser.getOrganizationName());
            String firstName = nameParts[0];
            String lastName = nameParts[1];

            // Fetch mspId from ClientAdmin if it exists
            String mspId = null;
            try {
                Optional<ClientAdmin> clientAdmin = clientAdminRepository.findById(adminId);
                if (clientAdmin.isPresent() && clientAdmin.get().getMspId() != null) {
                    mspId = clientAdmin.get().getMspId();
                    log.info("Retrieved mspId: {} for MSP Admin with ID: {}", mspId, adminId);
                }
            } catch (Exception e) {
                log.warn("Could not fetch mspId for MSP Admin with ID: {}", adminId, e);
            }

            // Create AspireUser entity
            AspireUser aspireUser = AspireUser.builder()
                    .id(baseUserId)
                    .userId(baseUserId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .username(adminEmail)
                    .email(adminEmail)
                    .password(hashedPassword)
                    .phoneNumber(mspUser.getPhoneNumber())
                    .phoneCode(mspUser.getPhoneCode())
                    .country(mspUser.getCountry())
                    .address(mspUser.getCompanyAddress())
                    .roles(roleIds)
                    .userType(UserType.MSP.name())
                    .status(mspUser.getStatus() != null ? mspUser.getStatus() : UserStatus.INACTIVE.name())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .createdBy(mspUser.getCreatedBy())
                    .clientAdminId(adminId)
                    .mspId(mspId)
                    .department(mspUser.getDepartment())
                    .profilePicture(mspUser.getLogoUrl())
                    .companyName(mspUser.getOrganizationName())
                    .isDefault(true) // Force password change on first login (temp password)
                    .build();

            AspireUser savedUser = aspireUserRepository.save(aspireUser);
            log.info("Created AspireUser record for MSP Admin with ID: {}", adminId);
            return savedUser.getUserId();

        } catch (Exception e) {
            log.error("Failed to create AspireUser record for MSP Admin with ID: {}", adminId, e);
            // Don't fail onboarding if AspireUser creation fails, but log the error
            return null;
        }
    }

    /**
     * Extracts first and last name from full name
     */
    private String[] extractNameParts(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        String[] names = fullName.trim().split("\\s+");
        if (names.length == 1) {
            return new String[]{names[0], ""};
        }
        return new String[]{names[0], String.join(" ", java.util.Arrays.copyOfRange(names, 1, names.length))};
    }

    /**
     * Convert role names to role IDs using RoleRepository
     */
    private List<String> convertRoleNamesToIds(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return List.of();
        }

        return roleNames.stream()
                .map(roleName -> {
                    Role role = roleRepository.findByRoleName(roleName);
                    return role != null ? role.getId() : null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Calculates and applies coupon discount to the invoice details
     *
     * @param requestDto The MSP onboarding request DTO containing invoice details
     */

    private InvoiceRequestDTO buildInvoiceRequestDTO(String adminId, OrganizationInfoForMspDto org, List<String> mspProductIds, List<ProductSelectionDto> selections, MspOnboardingRequestDto requestDto) {
        InvoiceRequestWithCoupon result = buildInvoiceRequestDTOWithCoupon(adminId, org, mspProductIds, selections, requestDto);
        return result.getInvoiceRequestDTO();
    }

    private InvoiceRequestWithCoupon buildInvoiceRequestDTOWithCoupon(String adminId, OrganizationInfoForMspDto org, List<String> mspProductIds, List<ProductSelectionDto> selections, MspOnboardingRequestDto requestDto) {
        InvoiceDetailsDto invoice = requestDto.getInvoice();
        BillingInfoForMspDto billing = requestDto.getBilling();

        // Validate invoice total price against calculated price from CMS
        double calculatedSubtotal = validateInvoiceTotalPrice(invoice.getSubtotal(), selections);

        // Get countryId and stateId from billing info (needed for coupon validation and VAT calculation)
        // For MSP, use billing country/state if available, otherwise fall back to organization country/state
        String countryId = null;
        String stateId = null;

        if (billing != null && billing.getCountry() != null && !billing.getCountry().isBlank()) {
            // Use billing-specific country/state
            countryId = billing.getCountry();
            stateId = billing.getStateProvince();
            } else {
            // Fallback to organization country/state
            countryId = org.getCountry();
            stateId = org.getStateProvince();
            log.warn("Billing country is null or empty, falling back to organization country: {}", countryId);
        }

        // Step 1: Apply coupon discount first (if coupon code exists)
        // Coupon and discount can be applied together - coupon is applied first
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

        // Step 2: Apply invoice discount on subtotal after coupon (if discount exists)
        double discountAmount = 0.0;
        double discountPercentage = invoice.getDiscountPercentage() != null ? invoice.getDiscountPercentage() : 0.0;
        
        // Check if discount is provided (either as percentage or flat amount)
        if (discountPercentage > 0) {
            // Calculate discount amount from percentage applied to subtotal after coupon
            discountAmount = MoneyUtil.round(subtotalAfterCoupon * (discountPercentage / 100.0));
            log.info("Applied invoice discount: {}% = {} on subtotal after coupon: {}", 
                discountPercentage, discountAmount, subtotalAfterCoupon);
        } else if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount() > 0) {
            // Use flat discount amount (don't exceed subtotal after coupon)
            discountAmount = MoneyUtil.round(Math.min(invoice.getDiscountAmount(), subtotalAfterCoupon));
            // Calculate percentage for display purposes
            discountPercentage = subtotalAfterCoupon > 0 ? (discountAmount / subtotalAfterCoupon) * 100.0 : 0.0;
            log.info("Applied flat invoice discount: {} = {}% on subtotal after coupon: {}", 
                discountAmount, discountPercentage, subtotalAfterCoupon);
        }

        // Calculate discounted subtotal (after both coupon and invoice discount are applied)
        double discountedSubtotal = MoneyUtil.round(subtotalAfterCoupon - discountAmount);

        // Step 3: Calculate VAT on discounted subtotal using billing service VAT API
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
        invoiceRequestDTO.setMspAdminId(adminId);
        invoiceRequestDTO.setClientName(org.getOrganizationName());
        invoiceRequestDTO.setClientProductIds(mspProductIds);
        invoiceRequestDTO.setSubtotal(calculatedSubtotal);
        invoiceRequestDTO.setDiscountType(invoice.getDiscountType());
        invoiceRequestDTO.setDiscountAmount(discountAmount);
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
        invoiceRequestDTO.setStatusNote("Auto-generated invoice for MSP onboarding");
        invoiceRequestDTO.setProductSelections(selections);
        // Use billing country/state for invoice DTO (same as VAT calculation)
        invoiceRequestDTO.setCountryName(countryId);
        invoiceRequestDTO.setCountryCode(getCountryCode(countryId));
        invoiceRequestDTO.setCountryId(countryId);
        invoiceRequestDTO.setStateName(stateId);
        invoiceRequestDTO.setStateCode(getStateCode(stateId));
        invoiceRequestDTO.setStateId(stateId);
        invoiceRequestDTO.setInvoicePdfLink("NA");
        invoiceRequestDTO.setRoleType(RoleType.MSP);
        
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
                    CurrentUserContext userContext = currentContextService.getCurrentUserContext();
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
     * Maps InvoiceResponseDTO and InvoiceRequestDTO to MspInvoice entity
     * Includes all invoice information and audited fields
     */
    private MspInvoice mapToMspInvoice(InvoiceResponseDTO invoiceResponse, InvoiceRequestDTO invoiceRequest, CurrentUserContext userContext, MspUser mspUser, String adminEmail) {
        Instant now = Instant.now();
        String createdBy = userContext != null && userContext.getUserId() != null
                ? userContext.getUserId()
                : "SYSTEM";

        return MspInvoice.builder()
                .id(invoiceResponse.getId())
                .mspId(mspUser.getId())
                .mspAdminEmail(adminEmail)
                .clientProductIds(invoiceResponse.getClientProductIds())
                .subtotal(invoiceResponse.getSubtotal())
                .discountAmount(invoiceResponse.getDiscountAmount())
                .discountPercentage(invoiceRequest.getDiscountPercentage())
                .vatAmount(invoiceResponse.getVatAmount())
                // Calculate VAT percentage on discounted subtotal (after coupon and discount)
                .vatPercentage(calculateVatPercentage(
                        invoiceResponse.getVatAmount(),
                        invoiceResponse.getSubtotal(),
                        invoiceResponse.getDiscountAmount()))
                .totalAmount(invoiceResponse.getTotalAmount())
                .status(invoiceResponse.getStatus())
                .statusNote(invoiceRequest.getStatusNote())
                .couponCode(invoiceRequest.getCouponCode())
                .invoiceUrl(invoiceResponse.getInvoicePdfLink())

                .paidAt(invoiceResponse.getPaidAt())
                .createdBy(createdBy)
                .createdAt(now)
                .updatedBy(createdBy)
                .updatedAt(now)
                .build();
    }

    /**
     * Calculates VAT percentage from VAT amount and discounted subtotal.
     * VAT should be calculated on the amount after coupon and discount are applied.
     *
     * @param vatAmount The VAT amount
     * @param subtotal The original subtotal
     * @param discountAmount The total discount amount (coupon + invoice discount)
     * @return The VAT percentage
     */
    private double calculateVatPercentage(double vatAmount, double subtotal, double discountAmount) {
        if (vatAmount <= 0) {
            return 0.0;
        }
        // Calculate discounted subtotal (base amount for VAT calculation)
        double discountedSubtotal = subtotal - discountAmount;
        if (discountedSubtotal <= 0) {
            return 0.0;
        }
        return (vatAmount / discountedSubtotal) * 100.0;
    }


    /**
     * Creates credit for MSP if credit allocation is enabled and all required fields are provided.
     *
     * @param requestDto The MSP onboarding request DTO
     * @param adminId    The MSP admin ID
     * @param mspUser    The MspUser entity to update with credit information
     */
    private void createCreditIfNeeded(MspOnboardingRequestDto requestDto, String adminId, MspUser mspUser) {
        if (requestDto.getCreditInfo() == null) {
            log.debug("No credit information provided for MSP onboarding");
            return;
        }

        var creditInfo = requestDto.getCreditInfo();

        // Check if credit is enabled
        if (creditInfo.getEnableCredit() == null || !creditInfo.getEnableCredit()) {
            log.debug("Credit allocation is not enabled for MSP onboarding");
            return;
        }

        // Validate required fields (validation should have been done at DTO level, but double-check here)
        if (creditInfo.getCreditAmount() == null || creditInfo.getCreditAmount() < 0) {
            log.warn("Credit amount is missing or invalid. Skipping credit creation.");
            return;
        }

        if (creditInfo.getReason() == null || creditInfo.getReason().trim().isEmpty()) {
            log.warn("Credit reason is missing. Skipping credit creation.");
            return;
        }

        try {
            // Create credit with provided dates (or defaults)
            String creditId = billingService.creatCreditForMsp(
                    adminId,
                    creditInfo.getCreditAmount(),
                    creditInfo.getReason(),
                    creditInfo.getCreditStartDate(),
                    mspUser.getCreditEndDate()
            );

            // Update MSP user with credit information
            MspUserMapper.updateMspUserWithCredit(mspUser, creditId, creditInfo.getReason());

            // Store additional credit metadata if needed (netTerms, autoSuspendOnOverdue)
            // Note: These fields may need to be stored in MspUser entity or passed to billing service
            // For now, we store the credit ID and reason in MspUser
            log.info("Credit created successfully for MSP: {} with credit ID: {}", adminId, creditId);

        } catch (Exception e) {
            log.error("Failed to create credit for MSP: {}. Error: {}", adminId, e.getMessage(), e);
            // Don't fail onboarding if credit creation fails, but log the error
            // Credit can be created later through a separate process if needed
        }
    }


    private String getCountryCode(String countryName) {
        return countryRepository.findByNameIgnoreCase(countryName)
                .map(Country::getCode)
                .orElseGet(() -> countryName != null && countryName.length() >= 2
                        ? countryName.substring(0, 2).toUpperCase()
                        : null);
    }

    private String getStateCode(String stateName) {
        return stateRepository.findByNameIgnoreCase(stateName)
                .map(state -> state.getCode() != null ? state.getCode().toUpperCase() : null)
                .orElseGet(() -> stateName != null && stateName.length() >= 2
                        ? stateName.substring(0, 2).toUpperCase()
                        : null);
    }

    /**
     * Sends notifications related to MSP onboarding with invoice PDF attachment.
     *
     * @param org               The organization information
     * @param invoiceResponse   The created invoice response
     * @param invoiceRequestDTO The invoice request DTO
     * @param adminId           The MSP admin ID
     * @param tempPassword      The temporary password for the MSP admin
     */
    private void sendMspOnboardingNotifications(
            OrganizationInfoForMspDto org,
            InvoiceResponseDTO invoiceResponse,
            InvoiceRequestDTO invoiceRequestDTO,
            String adminId,
            String tempPassword
    ) {
        // Standardize to use mspAdminEmail, fallback to adminEmail if not provided
        String adminEmail = org.getMspAdminEmail();

        String organizationName = org.getOrganizationName();

        try {
            String invoiceId = invoiceResponse.getId();
            log.info("Generating invoice PDF with ID: {}", invoiceId);

            // Generate invoice PDF
            ByteArrayOutputStream pdfStream = invoiceGenerator.generateInvoicePdf(
                    invoiceId,
                    invoiceRequestDTO.getClientName(),
                    invoiceRequestDTO.getTotalAmount(),
                    Date.from(Instant.now())
            );

            if (pdfStream == null) {
                throw new RegistrationServiceException("Failed to generate invoice PDF");
            }

            // Upload PDF to S3
            Result result = getResult(invoiceId, pdfStream);

            // Clean up temporary file
            if (result.tempFile().exists()) {
                result.tempFile().delete();
            }

            log.info("Sending welcome email to: {} with PDF link: {}", adminEmail, result.s3ObjectKey());

            // Create attachment DTO
            AttachmentDto attachment = AttachmentDto.builder()
                    .bucketName(s3BucketName)
                    .objectKey(result.s3ObjectKey())
                    .build();

            // Send welcome email with invoice PDF attachment using RegistrationNotificationClient
            notificationServiceClient.sendWelcomeEmailNotification(
                    adminEmail,
                    adminId,
                    adminId, // clientAdminId for client-specific notification settings
                    organizationName,
                    tempPassword,
                    List.of(attachment)
            );

            log.info("Successfully sent MSP onboarding welcome email with invoice PDF to: {}", adminEmail);

        } catch (Exception e) {
            log.error("Failed to generate invoice PDF or send notification for MSP onboarding. Error: {}", e.getMessage(), e);
            // Don't fail onboarding if notification fails, but log the error
            // Fallback: send notification without PDF
            try {
                notificationServiceClient.sendWelcomeEmailNotification(
                        adminEmail,
                        adminId,
                        adminId,
                        organizationName,
                        tempPassword
                );
                log.info("Sent fallback welcome email without PDF attachment to: {}", adminEmail);
            } catch (Exception fallbackError) {
                log.error("Failed to send fallback welcome email to: {}", adminEmail, fallbackError);
            }
        }
    }

    /**
     * Helper method to upload PDF to S3 and return result
     */
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
            return new Result(tempFile, s3ObjectKey);
        } catch (Exception e) {
            log.error("Failed to upload invoice PDF to S3", e);
            throw new IOException("Error uploading invoice PDF to S3", e);
        }
    }

    /**
     * Record to hold temporary file and S3 object key
     */
    private record Result(File tempFile, String s3ObjectKey) {
    }

    /**
     * Calculate expiry date based on assigned date, validity period, and validity unit
     *
     * @param assignedAt     The assignment date
     * @param validityPeriod The validity period (number)
     * @param validityUnit   The validity unit enum (MONTH or YEAR)
     * @return Calculated expiry date
     */
    private Instant calculateExpiryDate(Instant assignedAt, int validityPeriod, ValidityUnitForMspDto validityUnit) {
        if (assignedAt == null || validityPeriod <= 0 || validityUnit == null) {
            return null;
        }

        try {
            LocalDateTime assignedDateTime = assignedAt.atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime expiryDateTime = switch (validityUnit) {
                case DAYS -> assignedDateTime.plusMonths(validityPeriod/30);
                case MONTH -> assignedDateTime.plusMonths(validityPeriod);
                case YEAR -> assignedDateTime.plusYears(validityPeriod);
            };

            return expiryDateTime.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            log.error("Error calculating expiry date for assignedAt: {}, validityPeriod: {}, validityUnit: {}",
                    assignedAt, validityPeriod, validityUnit, e);
            return null;
        }
    }

    /**
     * Overloaded method to accept ValidityUnit enum (from ProductSelectionDto)
     */
    private Instant calculateExpiryDate(Instant assignedAt, int validityPeriod, ValidityUnit validityUnit) {
        if (assignedAt == null || validityPeriod <= 0 || validityUnit == null) {
            return null;
        }

        try {
            LocalDateTime assignedDateTime = assignedAt.atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime expiryDateTime = switch (validityUnit) {
                case DAYS -> assignedDateTime.plusMonths(validityPeriod/30);
                case MONTH -> assignedDateTime.plusMonths(validityPeriod);
                case YEAR -> assignedDateTime.plusYears(validityPeriod);
            };

            return expiryDateTime.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            log.error("Error calculating expiry date for assignedAt: {}, validityPeriod: {}, validityUnit: {}",
                    assignedAt, validityPeriod, validityUnit, e);
            return null;
        }
    }

    /**
     * Performs manual rollback by deleting all entities created during MSP onboarding
     * This is necessary because MongoDB transactions require replica set configuration
     *
     * @param adminId      MSP admin ID to delete
     * @param aspireUserId AspireUser ID to delete (if created)
     * @param mspUser      MspUser entity to delete (if created)
     */
    private void performManualRollback(String adminId, List<String> mspProductIds, UUID aspireUserId, MspUser mspUser) {
        log.info("Performing manual rollback for adminId: {}, mspProductIds: {}, aspireUserId: {}, mspUser: {}",
                adminId, mspProductIds, aspireUserId, mspUser != null ? mspUser.getId() : null);

        try {
            // 1. Delete MspProducts first (to avoid foreign key issues if any)
            if (mspProductIds != null && !mspProductIds.isEmpty()) {
                for (String productId : mspProductIds) {
                    try {
                        mspProductRepository.deleteById(productId);
                        log.debug("Deleted MspProduct with ID: {}", productId);
                    } catch (Exception e) {
                        log.error("Failed to delete MspProduct with ID: {}. Error: {}", productId, e.getMessage(), e);
                        // Continue with deletion of other entities
                    }
                }
                log.info("Deleted {} MspProduct entities", mspProductIds.size());
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

            // 3. Delete MspUser (should be last)
            if (mspUser != null && mspUser.getId() != null) {
                try {
                    mspUsersRepository.deleteById(mspUser.getId());
                    log.debug("Deleted MspUser with ID: {}", mspUser.getId());
                } catch (Exception e) {
                    log.error("Failed to delete MspUser with ID: {}. Error: {}", mspUser.getId(), e.getMessage(), e);
                }
            }

            log.info("Manual rollback completed for adminId: {}", adminId);

        } catch (Exception e) {
            log.error("Error during manual rollback for adminId: {}. Some data may not have been cleaned up.", adminId, e);
            // Don't throw exception here - we want to ensure the original exception is thrown
        }
    }

    private MspDetailsResponseDto convertToMspDetailsResponseDto(MspUser mspUser) {
        OrganizationInfoForMspResponseDto organization = buildOrganizationInfoResponse(mspUser);
        IdNameDto netDays = resolveNetDaysIdName(mspUser);

        // Build BillingInfoForMspDto
        BillingInfoForMspDto billing = BillingInfoForMspDto.builder()
                .billingEmail(mspUser.getBillingEmail())
                .billingName(mspUser.getBillingName())
                .streetAddress(mspUser.getBillingStreetAddress())
                .streetAddressLine2(mspUser.getBillingStreetAddressLine2())
                .city(mspUser.getBillingCity())
                .stateProvince(mspUser.getBillingStateProvince())
                .country(mspUser.getBillingCountry())
                .zipPostalCode(mspUser.getBillingZipPostalCode())
                .build();

        // Build CreditInfoResponseDto - reuse netDays from organization if available
        CreditInfoResponseDto creditInfo = CreditInfoResponseDto.builder()
                .enableCredit(mspUser.getCreditEnabled())
                .reason(mspUser.getCreditReason())
                .creditAmount(mspUser.getCreditAmount() != null ? mspUser.getCreditAmount().doubleValue() : null)
                .netDays(netDays) // Reuse the same netDays IdNameDto from organization
                .creditStartDate(mspUser.getCreditStartDate())
                .creditEndDate(mspUser.getCreditEndDate())
                .autoSuspendOnOverdue(mspUser.getAutoSuspendOverDue())
                .build();

        return MspDetailsResponseDto.builder()
                .id(mspUser.getId())
                .mspId(mspUser.getMspId())
                .phoneNumber(mspUser.getPhoneNumber())
                .phoneCode(mspUser.getPhoneCode())
                .organization(organization)
                .billing(billing)
                .creditInfo(creditInfo)
                .productIds(mspUser.getProductIds())
                .packageIds(mspUser.getPackageIds())
                .clientProductIds(mspUser.getClientProductIds())
                .creditId(mspUser.getCreditId())
                .invoiceId(mspUser.getInvoiceId())
                .invoiceStatus(mspUser.getInvoiceStatus())
                .notes(mspUser.getNotes())
                .status(mspUser.getStatus())
                .createdBy(mspUser.getCreatedBy())
                .createdAt(mspUser.getCreatedAt())
                .updatedAt(mspUser.getUpdatedAt())
                .build();
    }

    private OrganizationInfoForMspResponseDto buildOrganizationInfoResponse(MspUser mspUser) {
        return OrganizationInfoForMspResponseDto.builder()
                .organizationName(mspUser.getOrganizationName())
                .organizationType(resolveOrganizationTypeIdName(mspUser.getOrganizationType()))
                .contactEmail(mspUser.getContactEmail())
                .phoneNumber(mspUser.getPhoneNumber())
                .phoneCode(mspUser.getPhoneCode())
                .country(firstNonBlank(mspUser.getOrganizationCountry(), mspUser.getCountry()))
                .stateProvince(firstNonBlank(mspUser.getOrganizationStateProvince(), mspUser.getStateProvince()))
                .timeZone(mspUser.getTimeZone())
                .language(mspUser.getLanguage())
                .industry(resolveIndustryIdName(mspUser.getIndustry()))
                .domain(mspUser.getDomain())
                .organizationSize(resolveOrganizationSizeIdName(mspUser.getOrganizationSize()))
                .streetAddress(mspUser.getOrganizationStreetAddress())
                .streetAddressLine2(mspUser.getOrganizationStreetAddressLine2())
                .city(mspUser.getOrganizationCity())
                .zipPostalCode(mspUser.getOrganizationZipPostalCode())
                .logoUrl(mspUser.getLogoUrl())
                .tier(resolveTierIdName(mspUser))
                .mspType(resolveMspTypeIdName(mspUser))
                .netDays(resolveNetDaysIdName(mspUser))
                .mspAdminEmail(mspUser.getMspAdminEmail())
                .build();
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    private IdNameDto resolveTierIdName(MspUser mspUser) {
        if (mspUser.getMspTier() == null || mspUser.getMspTier().isBlank()) {
            return null;
        }
        return tierConfigurationRepository.findById(mspUser.getMspTier())
                .map(t -> IdNameDto.builder().id(t.getId()).name(t.getTierName()).build())
                .orElse(IdNameDto.builder().id(mspUser.getMspTier()).name(null).build());
    }

    private IdNameDto resolveNetDaysIdName(MspUser mspUser) {
        if (mspUser.getNetDaysId() == null || mspUser.getNetDaysId().isBlank()) {
            return null;
        }
        return netTermConfigurationRepository.findById(mspUser.getNetDaysId())
                .map(n -> IdNameDto.builder().id(n.getId()).name(n.getNetTermName()).build())
                .orElse(IdNameDto.builder().id(mspUser.getNetDaysId()).name(null).build());
    }

    private IdNameDto resolveMspTypeIdName(MspUser mspUser) {
        if (mspUser.getMspTypeId() == null || mspUser.getMspTypeId().isBlank()) {
            return null;
        }
        return mspTypeRepository.findById(mspUser.getMspTypeId())
                .map(t -> IdNameDto.builder().id(t.getId()).name(t.getName()).build())
                .orElse(IdNameDto.builder().id(mspUser.getMspTypeId()).name(null).build());
    }

    private IdNameDto resolveIndustryIdName(String industryId) {
        if (industryId == null || industryId.isBlank()) {
            return null;
        }
        return industryRepository.findById(industryId)
                .map(industry -> IdNameDto.builder().id(industry.getId()).name(industry.getName()).build())
                .orElse(IdNameDto.builder().id(industryId).name(null).build());
    }

    private IdNameDto resolveOrganizationSizeIdName(String organizationSizeId) {
        if (organizationSizeId == null || organizationSizeId.isBlank()) {
            return null;
        }
        return organizationSizeRepository.findById(organizationSizeId)
                .map(orgSize -> IdNameDto.builder().id(orgSize.getId()).name(orgSize.getName()).build())
                .orElse(IdNameDto.builder().id(organizationSizeId).name(null).build());
    }

    private IdNameDto resolveOrganizationTypeIdName(String organizationTypeId) {
        if (organizationTypeId == null || organizationTypeId.isBlank()) {
            return null;
        }
        return organizationTypeRepository.findById(organizationTypeId)
                .map(orgType -> IdNameDto.builder().id(orgType.getId()).name(orgType.getName()).build())
                .orElse(IdNameDto.builder().id(organizationTypeId).name(null).build());
    }

    private TrainingProgressDto calculateTrainingProgress(String mspId) {
        // Placeholder implementation
        // TODO: Integrate with training service to fetch actual progress
        return TrainingProgressDto.builder()
                .totalTrainings(0)
                .completedTrainings(0)
                .inProgressTrainings(0)
                .completionPercentage(0.0)
                .build();
    }


    @Override
    public AllResponseDto<List<MspListResponseDto>> getAllMsp(Integer offset, Integer pageSize, String search, MspStatus status) {
        log.info("Fetching all MSPs with offset: {}, pageSize: {}, search: {}, status: {}", offset, pageSize, search, status);

        // Set default values
        if (offset == null || offset < 0) {
            offset = 0;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        // Create pageable object
        Pageable pageable = PageRequest.of(offset, pageSize);

        // Get paginated MSPs with filters
        Page<MspUser> mspPage = mspUsersRepositoryCustom.findMspUsersWithFilters(search, status, pageable);

        // Convert to response DTOs
        List<MspListResponseDto> mspList = mspPage.getContent().stream()
                .map(this::convertToMspListResponseDto)
                .toList();

        log.info("Retrieved {} MSPs out of {} total, page {} of {}",
                mspList.size(), mspPage.getTotalElements(), offset + 1, mspPage.getTotalPages());

        return new AllResponseDto<>(offset, pageSize, mspPage.getTotalElements(), mspList);
    }

    @Override
    public MspDetailsResponseDto getMspDetails(String mspId) {
        log.info("Fetching MSP details for ID: {}", mspId);

        MspUser mspUser = mspUsersRepository.findById(mspId)
                .orElseThrow(() -> new RegistrationServiceException("MSP not found with ID: " + mspId));

        return convertToMspDetailsResponseDto(mspUser);
    }

    @Override
    public MspViewDetailsResponseDto getMspViewDetails(String mspId) {
        log.info("Fetching MSP details for ID: {}", mspId);

        MspUser mspUser = mspUsersRepository.findById(mspId)
                .orElseThrow(() -> new RegistrationServiceException("MSP not found with ID: " + mspId));

        return convertToMspViewDetailsResponseDto(mspUser);
    }

    private MspListResponseDto convertToMspListResponseDto(MspUser mspUser) {
        return MspListResponseDto.builder()
                .id(mspUser.getId())
                .mspId(mspUser.getMspId())
                .organizationName(mspUser.getOrganizationName())
                .logoUrl(mspUser.getLogoUrl())
                .mspAdminEmail(mspUser.getMspAdminEmail())
                .contactEmail(mspUser.getContactEmail())
                .phoneNumber(mspUser.getPhoneNumber())
                .phoneCode(mspUser.getPhoneCode())
                .country(mspUser.getCountry())
                .stateProvince(mspUser.getStateProvince())
                .status(mspUser.getStatus())
                .createdAt(mspUser.getCreatedAt())
                .updatedAt(mspUser.getUpdatedAt())
                .build();
    }

    private MspViewDetailsResponseDto convertToMspViewDetailsResponseDto(MspUser mspUser) {

        // Calculate license allocation from MspProducts
        LicenseAllocationDto licenseAllocation = calculateLicenseAllocation(mspUser.getId());

        OrganizationInfoForMspResponseDto organization = buildOrganizationInfoResponse(mspUser);
        IdNameDto netDays = resolveNetDaysIdName(mspUser);

        // Build CreditInfoResponseDto - reuse netDays from organization if available
        CreditInfoResponseDto creditInfo = CreditInfoResponseDto.builder()
                .enableCredit(mspUser.getCreditEnabled())
                .reason(mspUser.getCreditReason())
                .creditAmount(mspUser.getCreditAmount() != null ? mspUser.getCreditAmount().doubleValue() : null)
                .netDays(netDays) // Reuse the same netDays IdNameDto from organization
                .creditStartDate(mspUser.getCreditStartDate())
                .creditEndDate(mspUser.getCreditEndDate())
                .autoSuspendOnOverdue(mspUser.getAutoSuspendOverDue())
                .build();

        return MspViewDetailsResponseDto.builder()
                .id(mspUser.getId())
                .mspId(mspUser.getMspId())
                .phoneNumber(mspUser.getPhoneNumber())
                .phoneCode(mspUser.getPhoneCode())
                .organization(organization)
                .billingEmail(mspUser.getBillingEmail())
                .billingName(mspUser.getBillingName())
                .billingStreetAddress(mspUser.getBillingStreetAddress())
                .billingStreetAddressLine2(mspUser.getBillingStreetAddressLine2())
                .billingCity(mspUser.getBillingCity())
                .billingStateProvince(mspUser.getBillingStateProvince())
                .billingCountry(mspUser.getBillingCountry())
                .billingZipPostalCode(mspUser.getBillingZipPostalCode())
                .billingAddress(mspUser.getBillingAddress())
                .creditInfo(creditInfo)
                .licenseAllocation(licenseAllocation)
                .notes(mspUser.getNotes())
                .status(mspUser.getStatus())
                .createdAt(mspUser.getCreatedAt())
                .updatedAt(mspUser.getUpdatedAt())
                .build();
    }

    private LicenseAllocationDto calculateLicenseAllocation(String mspId) {
        List<MspProduct> mspProducts = mspProductRepository.findByMspId(mspId);

        int totalLicenses = mspProducts.stream()
                .mapToInt(MspProduct::getLicenseCount)
                .sum();

        int usedLicenses = mspProducts.stream()
                .mapToInt(MspProduct::getUsedLicenseCount)
                .sum();

        int availableLicenses = totalLicenses - usedLicenses;
        double usagePercentage = totalLicenses > 0
                ? ((double) usedLicenses / totalLicenses) * 100.0
                : 0.0;

        return LicenseAllocationDto.builder()
                .totalLicenses(totalLicenses)
                .usedLicenses(usedLicenses)
                .availableLicenses(availableLicenses)
                .usagePercentage(usagePercentage)
                .build();
    }

    private List<AssignedClientDto> getAssignedClientsForMsp(String mspId) {
        List<ClientAdmin> clientAdmins = clientAdminRepository.findAllByMspId(mspId);

        return clientAdmins.stream()
                .map(clientAdmin -> {
                    // Get total license count from all ClientProducts for this client
                    List<ClientProduct> clientProducts = clientProductRepository
                            .findByClientAdminId(clientAdmin.getId());

                    int licenseCount = clientProducts.stream()
                            .mapToInt(ClientProduct::getLicenseCount)
                            .sum();

                    return AssignedClientDto.builder()
                            .clientId(clientAdmin.getId())
                            .clientName(clientAdmin.getOrganizationName())
                            .contactEmail(clientAdmin.getEmail())
                            .status(clientAdmin.getStatus() != null ? clientAdmin.getStatus().name() : null)
                            .licenseCount(licenseCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrganizationLicenseStatisticsResponseDto getLicenseStatistics(String mspId) {
        log.info("Retrieving organization license statistics for MSP: {}", mspId);

        try {
            // Validate MSP exists
            Optional<MspUser> mspUser = mspUsersRepository.findById(mspId);
            if (mspUser.isEmpty()) {
                throw new RegistrationServiceException("MSP not found with ID: " + mspId);
            }

            // Get organization license statistics from repository
            OrganizationLicenseStatistics statistics =
                    mspUsersRepositoryCustom.getOrganizationLicenseStatisticsByMspId(mspId);

            log.info("Organization license statistics for MSP {}: Available={}, Allocated={}, Active={}, Expired={}",
                    mspId, statistics.getTotalAvailableLicenses(), statistics.getTotalAllocatedLicenses(),
                    statistics.getTotalActiveLicenses(), statistics.getTotalExpiredLicenses());

            return OrganizationLicenseStatisticsResponseDto.builder()
                    .totalAvailableLicenses(statistics.getTotalAvailableLicenses())
                    .totalAllocatedLicenses(statistics.getTotalAllocatedLicenses())
                    .totalActiveLicenses(statistics.getTotalActiveLicenses())
                    .totalExpiredLicenses(statistics.getTotalExpiredLicenses())
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving organization license statistics for MSP: {}", mspId, e);
            throw new RegistrationServiceException("Failed to retrieve organization license statistics: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMspStatus(String id, MspStatus status) {
        log.info("Updating MSP status - ID: {}, Status: {}", id, status);

        MspUser mspUser = mspUsersRepository.findById(id)
                .orElseThrow(() -> new RegistrationServiceException("MSP not found with ID: " + id));

        String oldStatus = mspUser.getStatus();
        mspUser.setStatus(status.name());
        mspUser.setUpdatedAt(Instant.now());

        mspUsersRepository.save(mspUser);

        log.info("MSP status updated successfully - ID: {}, Old Status: {}, New Status: {}",
                id, oldStatus, status);
    }

    @Override
    @Transactional
    public void updateMspUserStatus(String userId, MspStatus status) {
        log.info("Updating MSP user status for userId: {} to status: {}", userId, status);

        // Find the MSP user from AspireUser by userId
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new RegistrationServiceException("Invalid userId format: " + userId);
        }

        AspireUser mspUser = aspireUserRepository.findByUserId(userUuid)
                .orElseThrow(() -> new RegistrationServiceException("MSP user not found with userId: " + userId));

        // Validate that this is an MSP user
        if (!UserType.MSP.name().equals(mspUser.getUserType())) {
            throw new RegistrationServiceException("User with userId " + userId + " is not an MSP user");
        }

        // Get mspId from userId (for MSP users, userId is the mspId)
        String mspId = mspUser.getUserId() != null ? mspUser.getUserId().toString() : null;
        if (mspId == null || mspId.isBlank()) {
            throw new RegistrationServiceException("MSP ID not found for user: " + userId);
        }

        // Validate status
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        // Update MSP user status in AspireUser
        String statusString = status.name();
        mspUser.setStatus(statusString);
        mspUser.setUpdatedAt(Instant.now());
        aspireUserRepository.save(mspUser);
        log.info("Updated MSP user status to: {} for userId: {}", statusString, userId);

        List<String> usersToLogout = new ArrayList<>();
        usersToLogout.add(userId);

        // Handle associated client admins and users based on status
        if (status == MspStatus.INACTIVE) {
            usersToLogout.addAll(handleMspInactive(mspId));
        } else if (status == MspStatus.ACTIVE) {
            handleMspActive(mspId);
        }

        if (userSessionInvalidationHelper.isRestrictiveStatus(statusString)) {
            userSessionInvalidationHelper.logoutUsersIfRestrictive(statusString, usersToLogout);
        }

        log.info("Successfully updated MSP user status and associated users for userId: {}", userId);
    }

    @Override
    @Transactional
    public void suspendMspUser(String userId, UserSuspendRequestDto requestDto) {
        log.info("Suspending/activating MSP user for userId: {} to status: {}", userId, requestDto.getStatus());

        // Validate status
        String status = requestDto.getStatus();
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        String statusUpper = status.toUpperCase();
        if (!"SUSPEND".equals(statusUpper) && !"ACTIVE".equals(statusUpper)) {
            throw new IllegalArgumentException("Status must be either 'SUSPEND' or 'ACTIVE'");
        }

        // Find the MSP user from AspireUser by userId
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new RegistrationServiceException("Invalid userId format: " + userId);
        }

        AspireUser mspUser = aspireUserRepository.findByUserId(userUuid)
                .orElseThrow(() -> new RegistrationServiceException("MSP user not found with userId: " + userId));

        // Validate that this is an MSP user
        if (!UserType.MSP.name().equals(mspUser.getUserType())) {
            throw new RegistrationServiceException("User with userId " + userId + " is not an MSP user");
        }

        // Get mspId from userId (for MSP users, userId is the mspId)
        String mspId = mspUser.getUserId() != null ? mspUser.getUserId().toString() : null;
        if (mspId == null || mspId.isBlank()) {
            throw new RegistrationServiceException("MSP ID not found for user: " + userId);
        }

        // Get current user context
        String currentUserId = "SYSTEM";
        try {
            CurrentUserContext context = currentContextService.getCurrentUserContext();
            if (context != null && context.getUserId() != null) {
                currentUserId = context.getUserId();
            }
        } catch (Exception e) {
            log.warn("Could not get current user context, using SYSTEM: {}", e.getMessage());
        }

        // Update MSP user status in AspireUser
        mspUser.setStatus(statusUpper);
        mspUser.setUpdatedBy(currentUserId);
        mspUser.setUpdatedAt(Instant.now());
        aspireUserRepository.save(mspUser);
        log.info("Updated MSP user status to: {} for userId: {}", statusUpper, userId);

        List<String> usersToLogout = new ArrayList<>();
        usersToLogout.add(userId);

        // Handle associated client admins and users based on status
        String suspendReasonName = requestDto.getSuspendReason(); // Accept but don't save yet
        if ("SUSPEND".equals(statusUpper)) {
            usersToLogout.addAll(handleMspSuspend(mspId, suspendReasonName));
            userSessionInvalidationHelper.logoutUsersIfRestrictive(statusUpper, usersToLogout);
        } else if ("ACTIVE".equals(statusUpper)) {
            handleMspSuspendActivate(mspId);
        }

        // Send email notification to MSP user
        sendMspUserStatusChangeNotification(mspUser, statusUpper, suspendReasonName);

        log.info("Successfully updated MSP user status and associated users for userId: {}", userId);
    }

    /**
     * Handle MSP suspend status - suspend all users under MSP's client admins
     * @return userIds whose status was set to SUSPEND
     */
    private List<String> handleMspSuspend(String mspId, String suspendReasonName) {
        log.info("Handling MSP suspend status for mspId: {}", mspId);
        List<String> affectedUserIds = new ArrayList<>();

        // Find all ClientAdmins by mspId
        List<ClientAdmin> clientAdmins = clientAdminRepository.findAllByMspId(mspId);
        log.info("Found {} client admins under MSP: {}", clientAdmins.size(), mspId);

        for (ClientAdmin clientAdmin : clientAdmins) {
            List<String> clientProductIds = clientAdmin.getClientProductIds();
            if (clientProductIds == null || clientProductIds.isEmpty()) {
                continue;
            }

            // Fetch all ClientProducts by IDs
            List<ClientProduct> clientProducts = clientProductRepository.findAllById(clientProductIds);

            for (ClientProduct clientProduct : clientProducts) {
                // Check if licenseStatus is PENDING
                if ("PENDING".equals(clientProduct.getLicenseStatus())) {
                    String clientAdminId = clientAdmin.getId();
                    log.debug("Found PENDING product for clientAdminId: {}", clientAdminId);

                    // Find AspireUser by userId (using clientAdminId) - this is the client admin user
                    try {
                        UUID clientAdminUuid = UUID.fromString(clientAdminId);
                        Optional<AspireUser> clientAdminUserOpt = aspireUserRepository.findByUserId(clientAdminUuid);
                        
                        if (clientAdminUserOpt.isPresent()) {
                            AspireUser clientAdminUser = clientAdminUserOpt.get();
                            clientAdminUser.setStatus("SUSPEND");
                            clientAdminUser.setUpdatedAt(Instant.now());
                            aspireUserRepository.save(clientAdminUser);
                            affectedUserIds.add(clientAdminId);
                            log.debug("Updated client admin user {} to SUSPEND", clientAdminId);

                            // Send email notification for client admin
                            sendMspUserStatusChangeNotification(clientAdminUser, "SUSPEND", suspendReasonName);
                        }
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid clientAdminId format: {}", clientAdminId);
                    }

                    // Find all AspireUsers by clientAdminId where isAdminInactive is false
                    List<AspireUser> usersToSuspend = aspireUserRepository
                            .findByClientAdminIdAndIsAdminInactive(clientAdminId, false);

                    log.info("Found {} users to suspend for clientAdminId: {}", usersToSuspend.size(), clientAdminId);

                    // Update each user to SUSPEND and set isAdminInactive to true
                    for (AspireUser user : usersToSuspend) {
                        user.setStatus("SUSPEND");
                        user.setIsAdminInactive(true);
                        user.setUpdatedAt(Instant.now());
                        aspireUserRepository.save(user);
                        if (user.getUserId() != null) {
                            affectedUserIds.add(user.getUserId().toString());
                        }
                        log.debug("Updated user {} to SUSPEND with isAdminInactive=true", user.getId());

                        // Send email notification for each user
                        sendMspUserStatusChangeNotification(user, "SUSPEND", suspendReasonName);
                    }

                    log.info("Updated {} users to SUSPEND status with isAdminInactive=true for clientAdminId: {}",
                            usersToSuspend.size(), clientAdminId);
                    break;
                }
            }
        }
        return affectedUserIds;
    }

    /**
     * Handle MSP activate status - reactivate previously suspended users
     */
    private void handleMspSuspendActivate(String mspId) {
        log.info("Handling MSP activate status for mspId: {}", mspId);

        // Find all ClientAdmins by mspId
        List<ClientAdmin> clientAdmins = clientAdminRepository.findAllByMspId(mspId);
        log.info("Found {} client admins under MSP: {}", clientAdmins.size(), mspId);

        for (ClientAdmin clientAdmin : clientAdmins) {
            String clientAdminId = clientAdmin.getId();

            // Find AspireUsers by clientAdminId where status is SUSPEND and isAdminInactive is true
            List<AspireUser> usersToReactivate = aspireUserRepository
                    .findByClientAdminIdAndStatusAndIsAdminInactive(clientAdminId, "SUSPEND", true);

            log.info("Found {} users to reactivate for clientAdminId: {}", usersToReactivate.size(), clientAdminId);

            // Update each user to ACTIVE and set isAdminInactive to false
            for (AspireUser user : usersToReactivate) {
                user.setStatus("ACTIVE");
                user.setIsAdminInactive(false);
                user.setUpdatedAt(Instant.now());
                aspireUserRepository.save(user);
                log.debug("Updated user {} to ACTIVE with isAdminInactive=false", user.getId());

                // Send email notification for each user
                sendMspUserStatusChangeNotification(user, "ACTIVE", null);
            }

            // Also reactivate the client admin user if it was suspended
            try {
                UUID clientAdminUuid = UUID.fromString(clientAdminId);
                Optional<AspireUser> clientAdminUserOpt = aspireUserRepository.findByUserId(clientAdminUuid);

                if (clientAdminUserOpt.isPresent()) {
                    AspireUser clientAdminUser = clientAdminUserOpt.get();
                    if ("SUSPEND".equals(clientAdminUser.getStatus())) {
                        clientAdminUser.setStatus("ACTIVE");
                        clientAdminUser.setUpdatedAt(Instant.now());
                        aspireUserRepository.save(clientAdminUser);
                        log.debug("Updated client admin user {} to ACTIVE", clientAdminId);

                        // Send email notification for client admin
                        sendMspUserStatusChangeNotification(clientAdminUser, "ACTIVE", null);
                    }
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid clientAdminId format: {}", clientAdminId);
            }

            log.info("Updated {} users to ACTIVE status with isAdminInactive=false for clientAdminId: {}",
                    usersToReactivate.size(), clientAdminId);
        }
    }

    /**
     * Sends email notification to user about their status change (suspended/activated).
     *
     * @param aspireUser The AspireUser whose status changed
     * @param status The new status (SUSPEND or ACTIVE)
     * @param suspendReasonName The name of the suspend reason (can be null)
     */
    private void sendMspUserStatusChangeNotification(AspireUser aspireUser, String status, String suspendReasonName) {
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
     * Handle MSP inactive status - deactivate client admins with PENDING products and their users
     * @return userIds whose status was set to INACTIVE
     */
    private List<String> handleMspInactive(String mspId) {
        log.info("Handling MSP inactive status for mspId: {}", mspId);
        List<String> affectedUserIds = new ArrayList<>();

        // Find all ClientAdmins by mspId
        List<ClientAdmin> clientAdmins = clientAdminRepository.findAllByMspId(mspId);
        log.info("Found {} client admins under MSP: {}", clientAdmins.size(), mspId);

        for (ClientAdmin clientAdmin : clientAdmins) {
            List<String> clientProductIds = clientAdmin.getClientProductIds();
            if (clientProductIds == null || clientProductIds.isEmpty()) {
                continue;
            }

            // Fetch all ClientProducts by IDs
            List<ClientProduct> clientProducts = clientProductRepository.findAllById(clientProductIds);

            for (ClientProduct clientProduct : clientProducts) {
                // Check if licenseStatus is PENDING
                if ("PENDING".equals(clientProduct.getLicenseStatus())) {
                    String clientAdminId = clientAdmin.getId();
                    log.debug("Found PENDING product for clientAdminId: {}", clientAdminId);

                    // Find AspireUser by userId (using clientAdminId) - this is the client admin user
                    try {
                        UUID clientAdminUuid = UUID.fromString(clientAdminId);
                        Optional<AspireUser> clientAdminUserOpt = aspireUserRepository.findByUserId(clientAdminUuid);
                        
                        if (clientAdminUserOpt.isPresent()) {
                            AspireUser clientAdminUser = clientAdminUserOpt.get();
                            clientAdminUser.setStatus("INACTIVE");
                            clientAdminUser.setUpdatedAt(Instant.now());
                            aspireUserRepository.save(clientAdminUser);
                            affectedUserIds.add(clientAdminId);
                            log.debug("Updated client admin user {} to INACTIVE", clientAdminId);
                        }
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid clientAdminId format: {}", clientAdminId);
                    }

                    // Find all AspireUsers by clientAdminId where isAdminInactive is false
                    List<AspireUser> usersToDeactivate = aspireUserRepository
                            .findByClientAdminIdAndIsAdminInactive(clientAdminId, false);

                    log.info("Found {} users to deactivate for clientAdminId: {}", usersToDeactivate.size(), clientAdminId);

                    // Update each user to INACTIVE and set isAdminInactive to true
                    for (AspireUser user : usersToDeactivate) {
                        user.setStatus("INACTIVE");
                        user.setIsAdminInactive(true);
                        user.setUpdatedAt(Instant.now());
                        aspireUserRepository.save(user);
                        if (user.getUserId() != null) {
                            affectedUserIds.add(user.getUserId().toString());
                        }
                        log.debug("Updated user {} to INACTIVE with isAdminInactive=true", user.getId());
                    }

                    log.info("Updated {} users to INACTIVE status with isAdminInactive=true for clientAdminId: {}",
                            usersToDeactivate.size(), clientAdminId);
                    break;
                }
            }
        }
        return affectedUserIds;
    }

    /**
     * Handle MSP active status - reactivate previously deactivated users
     */
    private void handleMspActive(String mspId) {
        log.info("Handling MSP active status for mspId: {}", mspId);

        // Find all ClientAdmins by mspId
        List<ClientAdmin> clientAdmins = clientAdminRepository.findAllByMspId(mspId);
        log.info("Found {} client admins under MSP: {}", clientAdmins.size(), mspId);

        for (ClientAdmin clientAdmin : clientAdmins) {
            String clientAdminId = clientAdmin.getId();
            log.debug("Found PENDING product for clientAdminId: {}", clientAdminId);

            // Find AspireUsers by clientAdminId where status is INACTIVE and isAdminInactive is true
            List<AspireUser> usersToReactivate = aspireUserRepository
                    .findByClientAdminIdAndStatusAndIsAdminInactive(clientAdminId, "INACTIVE", true);

            log.info("Found {} users to reactivate for clientAdminId: {}", usersToReactivate.size(), clientAdminId);

            // Update each user to ACTIVE and set isAdminInactive to false
            for (AspireUser user : usersToReactivate) {
                user.setStatus("ACTIVE");
                user.setIsAdminInactive(false);
                user.setUpdatedAt(Instant.now());
                aspireUserRepository.save(user);
                log.debug("Updated user {} to ACTIVE with isAdminInactive=false", user.getId());
            }

            // Also reactivate the client admin user if it was deactivated
            try {
                UUID clientAdminUuid = UUID.fromString(clientAdminId);
                Optional<AspireUser> clientAdminUserOpt = aspireUserRepository.findByUserId(clientAdminUuid);

                if (clientAdminUserOpt.isPresent()) {
                    AspireUser clientAdminUser = clientAdminUserOpt.get();
                    if ("INACTIVE".equals(clientAdminUser.getStatus())) {
                        clientAdminUser.setStatus("ACTIVE");
                        clientAdminUser.setUpdatedAt(Instant.now());
                        aspireUserRepository.save(clientAdminUser);
                        log.debug("Updated client admin user {} to ACTIVE", clientAdminId);
                    }
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid clientAdminId format: {}", clientAdminId);
            }

            log.info("Updated {} users to ACTIVE status with isAdminInactive=false for clientAdminId: {}",
                    usersToReactivate.size(), clientAdminId);
        }
    }

    @Override
    @Transactional
    public MspUpdateResponseDto updateMsp(String mspId, MspUpdateRequestDto requestDto) {
        log.info("Updating MSP with ID: {}", mspId);

        MspUser mspUser = mspUsersRepository.findById(mspId)
                .orElseThrow(() -> new RegistrationServiceException("MSP not found with ID: " + mspId));

//        CurrentUserContext userContext = currentContextService.getCurrentUserContext();
        List<String> updatedFields = new ArrayList<>();
        List<String> updatedProductIds = new ArrayList<>();
        List<String> assignedClientIds = new ArrayList<>();
        List<String> removedClientIds = new ArrayList<>();
        boolean creditUpdated = false;

        try {
            // 1. Update Organization Information
            updatedFields.addAll(updateOrganizationInfo(mspUser, requestDto));

            // 2. Update Billing Information
            updatedFields.addAll(updateBillingInfo(mspUser, requestDto));

            // 3. Validate and update tier if changed
            if (requestDto.getMspTier() != null && !requestDto.getMspTier().equals(mspUser.getMspTier())) {
                validateTierConfiguration(requestDto.getMspTier());
                mspUser.setMspTier(requestDto.getMspTier());
                updatedFields.add("mspTier");
            }

            // 4. Update Products and Licenses
            if (requestDto.getProductUpdates() != null && !requestDto.getProductUpdates().isEmpty()) {
                updatedProductIds.addAll(updateMspProducts(mspId, requestDto.getProductUpdates(), mspUser));
                updatedFields.add("products");
            }

            // 5. Handle Client Assignments
            if (requestDto.getClientIdsToAssign() != null && !requestDto.getClientIdsToAssign().isEmpty()) {
                assignedClientIds.addAll(assignClientsToMsp(mspId, requestDto.getClientIdsToAssign()));
                updatedFields.add("clientAssignments");
            }

            // 6. Handle Client Removals
            if (requestDto.getClientIdsToRemove() != null && !requestDto.getClientIdsToRemove().isEmpty()) {
                removedClientIds.addAll(removeClientsFromMsp(mspId, requestDto.getClientIdsToRemove()));
                updatedFields.add("clientRemovals");
            }

            // 7. Update Credit/Payment Terms
            if (requestDto.getCreditUpdate() != null) {
                creditUpdated = updateCreditInfo(mspUser, requestDto.getCreditUpdate(), mspId);
                if (creditUpdated) {
                    updatedFields.add("creditInfo");
                }
            }

            // 8. Update notes if provided
            if (requestDto.getNotes() != null) {
                mspUser.setNotes(requestDto.getNotes());
                updatedFields.add("notes");
            }

            // 9. Update audit fields
            mspUser.setUpdatedAt(Instant.now());

            // 10. Save MspUser
            mspUsersRepository.save(mspUser);

            // 11. Update AspireUser record if email changed
            updateAspireUserIfNeeded(mspUser, requestDto);

            log.info("MSP updated successfully - ID: {}, Updated fields: {}", mspId, updatedFields);

            return MspUpdateResponseDto.builder()
                    .id(mspUser.getId())
                    .mspId(mspUser.getMspId())
                    .organizationName(mspUser.getOrganizationName())
                    .status(mspUser.getStatus())
                    .updatedAt(mspUser.getUpdatedAt())
                    .updatedFields(updatedFields)
                    .updatedProductIds(updatedProductIds)
                    .assignedClientIds(assignedClientIds)
                    .removedClientIds(removedClientIds)
                    .creditUpdated(creditUpdated)
                    .message("MSP updated successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error updating MSP with ID: {}", mspId, e);
            throw new RegistrationServiceException("Failed to update MSP: " + e.getMessage(), e);
        }
    }

    private List<String> updateOrganizationInfo(MspUser mspUser, MspUpdateRequestDto requestDto) {
        List<String> updatedFields = new ArrayList<>();

        if (requestDto.getOrganizationName() != null && !requestDto.getOrganizationName().equals(mspUser.getOrganizationName())) {
            mspUser.setOrganizationName(requestDto.getOrganizationName());
            updatedFields.add("organizationName");
        }
        if (requestDto.getContactEmail() != null && !requestDto.getContactEmail().equals(mspUser.getContactEmail())) {
            mspUser.setContactEmail(requestDto.getContactEmail());
            updatedFields.add("contactEmail");
        }
        if (requestDto.getMspAdminEmail() != null && !requestDto.getMspAdminEmail().equals(mspUser.getMspAdminEmail())) {
            // Validate new email doesn't exist
            if (mspUsersRepository.existsByMspAdminEmailIgnoreCase(requestDto.getMspAdminEmail())) {
                throw new ResourceAlreadyExistsException("An MSP with this admin email already exists: " + requestDto.getMspAdminEmail());
            }
            mspUser.setMspAdminEmail(requestDto.getMspAdminEmail());
            updatedFields.add("mspAdminEmail");
        }
        if (requestDto.getPhoneNumber() != null && !requestDto.getPhoneNumber().equals(mspUser.getPhoneNumber())) {
            mspUser.setPhoneNumber(requestDto.getPhoneNumber());
            updatedFields.add("phoneNumber");
        }
        if (requestDto.getPhoneCode() != null && !requestDto.getPhoneCode().equals(mspUser.getPhoneCode())) {
            mspUser.setPhoneCode(requestDto.getPhoneCode());
            updatedFields.add("phoneCode");
        }
        if (requestDto.getCountry() != null && !requestDto.getCountry().equals(mspUser.getCountry())) {
            mspUser.setCountry(requestDto.getCountry());
            updatedFields.add("country");
        }
        if (requestDto.getStateProvince() != null && !requestDto.getStateProvince().equals(mspUser.getStateProvince())) {
            mspUser.setStateProvince(requestDto.getStateProvince());
            updatedFields.add("stateProvince");
        }
        if (requestDto.getTimeZone() != null && !requestDto.getTimeZone().equals(mspUser.getTimeZone())) {
            mspUser.setTimeZone(requestDto.getTimeZone());
            updatedFields.add("timeZone");
        }
        if (requestDto.getLanguage() != null && !requestDto.getLanguage().equals(mspUser.getLanguage())) {
            mspUser.setLanguage(requestDto.getLanguage());
            updatedFields.add("language");
        }
        if (requestDto.getIndustry() != null && !requestDto.getIndustry().equals(mspUser.getIndustry())) {
            mspUser.setIndustry(requestDto.getIndustry());
            updatedFields.add("industry");
        }
        if (requestDto.getDomain() != null && !requestDto.getDomain().equals(mspUser.getDomain())) {
            mspUser.setDomain(requestDto.getDomain());
            updatedFields.add("domain");
        }
        if (requestDto.getOrganizationSize() != null && !requestDto.getOrganizationSize().equals(mspUser.getOrganizationSize())) {
            mspUser.setOrganizationSize(requestDto.getOrganizationSize());
            updatedFields.add("organizationSize");
        }
        if (requestDto.getOrganizationType() != null && !requestDto.getOrganizationType().equals(mspUser.getOrganizationType())) {
            mspUser.setOrganizationType(requestDto.getOrganizationType());
            updatedFields.add("organizationType");
        }
        if (requestDto.getMspTypeId() != null && !requestDto.getMspTypeId().equals(mspUser.getMspTypeId())) {
            mspUser.setMspTypeId(requestDto.getMspTypeId());
            updatedFields.add("mspTypeId");
        }
        if (requestDto.getLogoUrl() != null && !requestDto.getLogoUrl().equals(mspUser.getLogoUrl())) {
            mspUser.setLogoUrl(requestDto.getLogoUrl());
            updatedFields.add("logoUrl");
        }

        // Organization Address
        if (requestDto.getOrganizationStreetAddress() != null) {
            mspUser.setOrganizationStreetAddress(requestDto.getOrganizationStreetAddress());
            updatedFields.add("organizationStreetAddress");
        }
        if (requestDto.getOrganizationStreetAddressLine2() != null) {
            mspUser.setOrganizationStreetAddressLine2(requestDto.getOrganizationStreetAddressLine2());
            updatedFields.add("organizationStreetAddressLine2");
        }
        if (requestDto.getOrganizationCity() != null) {
            mspUser.setOrganizationCity(requestDto.getOrganizationCity());
            updatedFields.add("organizationCity");
        }
        if (requestDto.getOrganizationStateProvince() != null) {
            mspUser.setOrganizationStateProvince(requestDto.getOrganizationStateProvince());
            updatedFields.add("organizationStateProvince");
        }
        if (requestDto.getOrganizationCountry() != null) {
            mspUser.setOrganizationCountry(requestDto.getOrganizationCountry());
            updatedFields.add("organizationCountry");
        }
        if (requestDto.getOrganizationZipPostalCode() != null) {
            mspUser.setOrganizationZipPostalCode(requestDto.getOrganizationZipPostalCode());
            updatedFields.add("organizationZipPostalCode");
        }

        return updatedFields;
    }

    private List<String> updateBillingInfo(MspUser mspUser, MspUpdateRequestDto requestDto) {
        List<String> updatedFields = new ArrayList<>();

        if (requestDto.getBillingEmail() != null && !requestDto.getBillingEmail().equals(mspUser.getBillingEmail())) {
            mspUser.setBillingEmail(requestDto.getBillingEmail());
            updatedFields.add("billingEmail");
        }
        if (requestDto.getBillingName() != null && !requestDto.getBillingName().equals(mspUser.getBillingName())) {
            mspUser.setBillingName(requestDto.getBillingName());
            updatedFields.add("billingName");
        }
        if (requestDto.getBillingStreetAddress() != null) {
            mspUser.setBillingStreetAddress(requestDto.getBillingStreetAddress());
            updatedFields.add("billingStreetAddress");
        }
        if (requestDto.getBillingStreetAddressLine2() != null) {
            mspUser.setBillingStreetAddressLine2(requestDto.getBillingStreetAddressLine2());
            updatedFields.add("billingStreetAddressLine2");
        }
        if (requestDto.getBillingCity() != null) {
            mspUser.setBillingCity(requestDto.getBillingCity());
            updatedFields.add("billingCity");
        }
        if (requestDto.getBillingStateProvince() != null) {
            mspUser.setBillingStateProvince(requestDto.getBillingStateProvince());
            updatedFields.add("billingStateProvince");
        }
        if (requestDto.getBillingCountry() != null) {
            mspUser.setBillingCountry(requestDto.getBillingCountry());
            updatedFields.add("billingCountry");
        }
        if (requestDto.getBillingZipPostalCode() != null) {
            mspUser.setBillingZipPostalCode(requestDto.getBillingZipPostalCode());
            updatedFields.add("billingZipPostalCode");
        }

        return updatedFields;
    }

    private List<String> updateMspProducts(String mspId, List<ProductLicenseUpdateDto> productUpdates, MspUser mspUser) {
        List<String> updatedProductIds = new ArrayList<>();
        List<String> currentProductIds = mspUser.getProductIds() != null ? new ArrayList<>(mspUser.getProductIds()) : new ArrayList<>();

        for (ProductLicenseUpdateDto update : productUpdates) {
            if (update.isRemove() && update.getMspProductId() != null) {
                // Remove existing product
                mspProductRepository.deleteById(update.getMspProductId());
                currentProductIds.remove(update.getMspProductId());
                log.info("Removed MSP product: {} from MSP: {}", update.getMspProductId(), mspId);
            } else if (update.getMspProductId() != null) {
                // Update existing product
                MspProduct existingProduct = mspProductRepository.findById(update.getMspProductId())
                        .orElseThrow(() -> new RegistrationServiceException("MSP Product not found: " + update.getMspProductId()));

                if (update.getLicenseCount() != null) {
                    // Validate license count change
                    int usedLicenses = existingProduct.getUsedLicenseCount();
                    if (update.getLicenseCount() < usedLicenses) {
                        throw new RegistrationServiceException(
                                String.format("Cannot reduce license count to %d. Currently %d licenses are in use.",
                                        update.getLicenseCount(), usedLicenses));
                    }
                    existingProduct.setLicenseCount(update.getLicenseCount());
                }
                if (update.getPricePerLicense() != null) {
                    existingProduct.setPricePerLicense(update.getPricePerLicense());
                }
                if (update.getLicenseCount() != null && update.getPricePerLicense() != null) {
                    existingProduct.setTotalPrice(update.getLicenseCount() * update.getPricePerLicense());
                }
                if (update.getValidityPeriod() != null) {
                    existingProduct.setValidityPeriod(update.getValidityPeriod());
                }
                if (update.getValidityUnit() != null) {
                    existingProduct.setValidityUnit(update.getValidityUnit().name());
                    // Recalculate expiry date
                    Instant newExpiry = calculateExpiryDate(existingProduct.getAssignedAt(),
                            existingProduct.getValidityPeriod(), update.getValidityUnit());
                    existingProduct.setExpiryDate(newExpiry);
                }

                mspProductRepository.save(existingProduct);
                updatedProductIds.add(existingProduct.getId());
                log.info("Updated MSP product: {} for MSP: {}", existingProduct.getId(), mspId);
            } else if (update.getProductId() != null && update.getPackageId() != null) {
                // Add new product
                String newProductId = CommonUtils.generateStringId();
                Instant assignedAt = Instant.now();
                Instant expiryDate = calculateExpiryDate(assignedAt,
                        update.getValidityPeriod() != null ? update.getValidityPeriod() : 12,
                        update.getValidityUnit() != null ? update.getValidityUnit() : ValidityUnitForMspDto.MONTH);

                MspProduct newProduct = MspProduct.builder()
                        .id(newProductId)
                        .mspId(mspId)
                        .productId(update.getProductId())
                        .packageId(update.getPackageId())
                        .licenseCount(update.getLicenseCount() != null ? update.getLicenseCount() : 0)
                        .usedLicenseCount(0)
                        .pricePerLicense(update.getPricePerLicense() != null ? update.getPricePerLicense() : 0.0)
                        .totalPrice((update.getLicenseCount() != null ? update.getLicenseCount() : 0) *
                                (update.getPricePerLicense() != null ? update.getPricePerLicense() : 0.0))
                        .validityPeriod(update.getValidityPeriod() != null ? update.getValidityPeriod() : 12)
                        .validityUnit(update.getValidityUnit() != null ? update.getValidityUnit().name() : "MONTH")
                        .assignedAt(assignedAt)
                        .expiryDate(expiryDate)
                        .licenseStatus(LicenseStatus.PENDING.name())
                        .countryId(mspUser.getCountry())
                        .build();

                mspProductRepository.save(newProduct);
                currentProductIds.add(newProductId);
                updatedProductIds.add(newProductId);
                log.info("Added new MSP product: {} for MSP: {}", newProductId, mspId);
            }
        }

        // Update product IDs in MspUser
        mspUser.setProductIds(currentProductIds.stream().distinct().collect(Collectors.toList()));

        return updatedProductIds;
    }

    private List<String> assignClientsToMsp(String mspId, List<String> clientIds) {
        List<String> assignedIds = new ArrayList<>();

        for (String clientId : clientIds) {
            Optional<ClientAdmin> clientOpt = clientAdminRepository.findById(clientId);
            if (clientOpt.isPresent()) {
                ClientAdmin client = clientOpt.get();
                if (client.getMspId() != null && !client.getMspId().equals(mspId)) {
                    log.warn("Client {} is already assigned to MSP {}. Reassigning to MSP {}",
                            clientId, client.getMspId(), mspId);
                }
                client.setMspId(mspId);
                clientAdminRepository.save(client);
                assignedIds.add(clientId);
                log.info("Assigned client {} to MSP {}", clientId, mspId);
            } else {
                log.warn("Client not found: {}", clientId);
            }
        }

        return assignedIds;
    }

    private List<String> removeClientsFromMsp(String mspId, List<String> clientIds) {
        List<String> removedIds = new ArrayList<>();

        for (String clientId : clientIds) {
            Optional<ClientAdmin> clientOpt = clientAdminRepository.findById(clientId);
            if (clientOpt.isPresent()) {
                ClientAdmin client = clientOpt.get();
                if (mspId.equals(client.getMspId())) {
                    client.setMspId(null);
                    clientAdminRepository.save(client);
                    removedIds.add(clientId);
                    log.info("Removed client {} from MSP {}", clientId, mspId);
                } else {
                    log.warn("Client {} is not assigned to MSP {}", clientId, mspId);
                }
            } else {
                log.warn("Client not found: {}", clientId);
            }
        }

        return removedIds;
    }

    private boolean updateCreditInfo(MspUser mspUser, CreditUpdateDto creditUpdate, String mspId) {
        boolean updated = false;

        if (creditUpdate.getEnableCredit() != null) {
            mspUser.setCreditEnabled(creditUpdate.getEnableCredit());
            updated = true;
        }
        if (creditUpdate.getReason() != null) {
            mspUser.setCreditReason(creditUpdate.getReason());
            updated = true;
        }
        if (creditUpdate.getCreditAmount() != null) {
            mspUser.setCreditAmount(creditUpdate.getCreditAmount());
            updated = true;
        }
        if (creditUpdate.getNetDaysId() != null) {
            mspUser.setNetDaysId(creditUpdate.getNetDaysId());
            // Recalculate credit end date
            if (Boolean.TRUE.equals(mspUser.getCreditEnabled())) {
                NetTermConfiguration netTermConfig = netTermConfigurationRepository.findById(creditUpdate.getNetDaysId())
                        .orElseGet(() -> {
                            NetTermConfiguration ntc = new NetTermConfiguration();
                            ntc.setNetTermInDays(30);
                            return ntc;
                        });
                Date startDate = creditUpdate.getCreditStartDate() != null ?
                        creditUpdate.getCreditStartDate() :
                        (mspUser.getCreditStartDate() != null ? mspUser.getCreditStartDate() : Date.from(Instant.now()));
                LocalDateTime startLdt = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime endLdt = startLdt.plusDays(Math.max(0, netTermConfig.getNetTermInDays()));
                mspUser.setCreditEndDate(Date.from(endLdt.atZone(ZoneId.systemDefault()).toInstant()));
            }
            updated = true;
        }
        if (creditUpdate.getCreditStartDate() != null) {
            mspUser.setCreditStartDate(creditUpdate.getCreditStartDate());
            updated = true;
        }
        if (creditUpdate.getAutoSuspendOnOverdue() != null) {
            mspUser.setAutoSuspendOverDue(creditUpdate.getAutoSuspendOnOverdue());
            updated = true;
        }

        // Update credit in billing service if credit ID exists
        if (updated && mspUser.getCreditId() != null) {
            try {
                billingService.updateCreditForMsp(
                        mspUser.getCreditId(),
                        mspUser.getCreditAmount(),
                        mspUser.getCreditReason(),
                        mspUser.getCreditStartDate(),
                        mspUser.getCreditEndDate()
                );
                log.info("Updated credit in billing service for MSP: {}", mspId);
            } catch (Exception e) {
                log.error("Failed to update credit in billing service for MSP: {}", mspId, e);
                // Don't fail the update if billing service update fails
            }
        }

        return updated;
    }

    private void updateAspireUserIfNeeded(MspUser mspUser, MspUpdateRequestDto requestDto) {
        boolean needsUpdate = requestDto.getMspAdminEmail() != null
                || requestDto.getOrganizationName() != null
                || requestDto.getPhoneNumber() != null
                || requestDto.getPhoneCode() != null;
        if (!needsUpdate) {
            return;
        }

        try {
            UUID userId = UUID.fromString(mspUser.getId());
            Optional<AspireUser> aspireUserOpt = aspireUserRepository.findByUserId(userId);

            if (aspireUserOpt.isPresent()) {
                AspireUser aspireUser = aspireUserOpt.get();

                if (requestDto.getMspAdminEmail() != null) {
                    aspireUser.setEmail(requestDto.getMspAdminEmail());
                    aspireUser.setUsername(requestDto.getMspAdminEmail());
                }
                if (requestDto.getOrganizationName() != null) {
                    String[] nameParts = extractNameParts(requestDto.getOrganizationName());
                    aspireUser.setFirstName(nameParts[0]);
                    aspireUser.setLastName(nameParts[1]);
                    aspireUser.setCompanyName(requestDto.getOrganizationName());
                }
                if (requestDto.getPhoneNumber() != null) {
                    aspireUser.setPhoneNumber(mspUser.getPhoneNumber());
                }
                if (requestDto.getPhoneCode() != null) {
                    aspireUser.setPhoneCode(mspUser.getPhoneCode());
                }
                aspireUser.setUpdatedAt(Instant.now());

                aspireUserRepository.save(aspireUser);
                log.info("Updated AspireUser for MSP: {}", mspUser.getId());
            }
        } catch (Exception e) {
            log.error("Failed to update AspireUser for MSP: {}", mspUser.getId(), e);
        }
    }

    /**
     * Validates the invoice total price by calculating expected price from CMS package range pricing.
     * Loops through each product selection, fetches the price per user from CMS,
     * calculates total by multiplying with license count, and compares with the invoice subtotal.
     *
     * @param subtotal The subtotal amount from the invoice to validate against
     * @param productSelections The product selection DTO containing product selections and total License counts
     * @return The calculated subtotal
     * @throws RegistationValidationException if validation fails
     */
    private double validateInvoiceTotalPrice(Double subtotal, List<ProductSelectionDto> productSelections) {
        if (productSelections == null || productSelections.isEmpty()) {
            log.info("No product selections provided, skipping price validation");
            throw new RegistationValidationException("No product selections provided for invoice price validation.");
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

            log.debug("Product price calculation - packageId: {}, pricePerLicense: {}, licenseCount: {}, totalTimeUnit: {}, productTotal: {}",
                    packageId, pricePerLicense, licenseCount, totalTimeUnit, productTotal);

            calculatedSubtotal += productTotal;
        }

        log.info("Invoice price validation successful. Calculated subtotal: {}, Provided subtotal: {}",
                calculatedSubtotal, subtotal);

        return MoneyUtil.round(calculatedSubtotal);
    }

    /**
     * Calculate total months based on validity period and validity unit.
     *
     * @param validityPeriod the validity period value
     * @param validityUnit   the validity unit (DAYS, MONTH, or YEAR)
     * @return the total time unit (months for MONTH/DAYS, years for YEAR)
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
            case YEAR -> validityPeriod * 1;  // Changed from * 12 to * 1
            case MONTH -> validityPeriod;
            default -> validityPeriod; // Fallback to MONTH behavior for any unexpected values
        };
    }

    /**
     * Calculate product total based on price per unit, license count, and total months.
     *
     * @param pricePerUnit the price per user or per license
     * @param licenseCount the number of licenses
     * @param totalMonths  the total months (validity period converted to months)
     * @return the calculated product total
     */
    private double calculateProductTotal(double pricePerUnit, int licenseCount, int totalTimeUnit) {
        return MoneyUtil.round(pricePerUnit * licenseCount * totalTimeUnit);
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

    /**
     * Checks if a product selection is eligible for a coupon discount based on
     * geographic restrictions and product/package restrictions.
     */
    private boolean isProductEligibleForCoupon(
            ProductSelectionDto selection,
            CouponResponseDTO coupon,
            String clientCountryId) {
        if (coupon.getGeographicRestrictions() != null && !coupon.getGeographicRestrictions().isEmpty()) {
            if (clientCountryId != null && coupon.getGeographicRestrictions().contains(clientCountryId)) {
                return true;
            }
        }

        if ((coupon.getProductRestrictions() == null || coupon.getProductRestrictions().isEmpty())
                && (coupon.getGeographicRestrictions() == null || coupon.getGeographicRestrictions().isEmpty())) {
            return true;
        }

        if (coupon.getProductRestrictions() != null && !coupon.getProductRestrictions().isEmpty()) {
            return coupon.getProductRestrictions().stream()
                    .anyMatch(restriction -> {
                        boolean productMatch = selection.getProductId() != null
                                && restriction.getProductId() != null
                                && selection.getProductId().equals(restriction.getProductId());
                        if (!productMatch) {
                            return false;
                        }
                        if (restriction.getPackageId() == null || restriction.getPackageId().isBlank()) {
                            return true;
                        }
                        return selection.getPackageId() != null
                                && selection.getPackageId().equals(restriction.getPackageId());
                    });
        }

        return false;
    }

    /**
     * Validates and calculates coupon discount with comprehensive validation including
     * geographic restrictions and product/package restrictions.
     *
     * @param couponCode The coupon code to validate
     * @param calculatedSubtotal The calculated subtotal from product selections
     * @param selections The product selections for restriction validation
     * @param clientCountryId The client country ID for geographic restriction validation
     * @return CouponDiscountInfo with discount details, or null if no coupon code
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

            if (eligibleSubtotal <= 0) {
                throw new RegistrationServiceException(
                        "Coupon is not valid for any of the selected products/packages: " + couponCode);
            }

            log.info("Eligible subtotal for coupon: {} (total subtotal: {})", eligibleSubtotal, calculatedSubtotal);

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

    @Override
    @Transactional
    public MspBuyNowResponseDto processMspBuyNow(MspBuyNowRequestDto requestDto) {
        validateMspBuyNowRequest(requestDto);
        String mspId = requestDto.getMspId();
        log.info("Processing MSP buy-now flow for mspId: {}", mspId);

        MspUser mspUser = mspUsersRepository.findById(mspId)
                .orElseThrow(() -> new RegistrationServiceException("MSP not found with ID: " + mspId));

        List<ProductSelectionDto> selections = requestDto.getProductSelections();
        InvoiceDetailsDto invoice = requestDto.getInvoice();
        validateMspProductsAvailableForPurchase(mspId, selections);

        OrganizationInfoForMspDto org = buildOrganizationInfoFromMspUser(mspUser);
        BillingInfoForMspDto billing = requestDto.getBilling() != null
                ? requestDto.getBilling()
                : buildBillingInfoFromMspUser(mspUser);

        MspOnboardingRequestDto syntheticRequest = MspOnboardingRequestDto.builder()
                .organization(org)
                .billing(billing)
                .productSelections(selections)
                .invoice(invoice)
                .build();

        productPriceResolver.applyAuthoritativePricing(selections);
        List<String> newMspProductIds = createAndSaveMspProducts(selections, mspId, org, syntheticRequest);

        InvoiceRequestWithCoupon invoiceRequestWithCoupon = buildInvoiceRequestDTOWithCoupon(
                mspId, org, newMspProductIds, selections, syntheticRequest);
        InvoiceRequestDTO invoiceRequestDTO = invoiceRequestWithCoupon.getInvoiceRequestDTO();
        invoiceRequestDTO.setStatusNote("Auto-generated invoice during MSP buy-now flow");

        InvoiceResponseDTO invoiceResponse;
        try {
            invoiceResponse = invoiceService.createInvoiceForMsp(invoiceRequestDTO);
        } catch (Exception e) {
            log.error("Invoice creation failed during MSP buy-now: {}", e.getMessage(), e);
            deleteMspProductsByIds(newMspProductIds);
            throw wrapAsRegistrationException("Invoice creation failed during MSP buy-now", e);
        }

        String billingInvoiceId = invoiceResponse != null ? invoiceResponse.getId() : null;

        CurrentUserContext userContext;
        try {
            userContext = currentContextService.getCurrentUserContext();
        } catch (Exception e) {
            userContext = new CurrentUserContext();
            userContext.setUserId("SYSTEM");
        }

        try {
            MspInvoice mspInvoice = mapToMspInvoice(
                    invoiceResponse, invoiceRequestDTO, userContext, mspUser, mspUser.getMspAdminEmail());
            mspInvoiceRepository.save(mspInvoice);
            log.info("MSP buy-now invoice saved to MSP_INVOICE table with ID: {}", mspInvoice.getId());
        } catch (Exception e) {
            log.error("Failed to save MspInvoice during buy-now: {}", e.getMessage(), e);
            tryDeleteBillingInvoice(billingInvoiceId);
            deleteMspProductsByIds(newMspProductIds);
            throw wrapAsRegistrationException("MSP buy-now aborted: failed to save MSP invoice", e);
        }

        List<String> existingProductIds = mspUser.getProductIds();
        if (existingProductIds == null) {
            existingProductIds = new ArrayList<>();
        } else {
            existingProductIds = new ArrayList<>(existingProductIds);
        }
        existingProductIds.addAll(newMspProductIds);
        mspUser.setProductIds(existingProductIds.stream().distinct().collect(Collectors.toList()));
        mspUser.setUpdatedAt(Instant.now());
        mspUsersRepository.save(mspUser);

        try {
            sendMspBuyNowInvoiceEmail(mspUser, invoiceRequestDTO, invoiceResponse);
        } catch (Exception e) {
            log.error("Failed to send invoice email for MSP buy-now flow. Error: {}", e.getMessage(), e);
            tryDeleteBillingInvoice(billingInvoiceId);
            deleteMspProductsByIds(newMspProductIds);
            throw wrapAsRegistrationException("MSP buy-now aborted: failed to send invoice email", e);
        }

        return MspBuyNowResponseDto.builder()
                .mspId(mspId)
                .adminEmail(mspUser.getMspAdminEmail())
                .organizationName(mspUser.getOrganizationName())
                .mspProductIds(newMspProductIds)
                .invoiceResponse(invoiceResponse)
                .portalLink(logInUrl)
                .build();
    }

    @Override
    public MspProductCatalogResponseDto getMspProductCatalog(
            String mspId, String search, String mspProductStatus, Integer offset, Integer pageSize) {

        if (!mspUsersRepository.existsById(mspId)) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        if (offset == null || offset < 0) {
            offset = 0;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        Map<String, CmsFullProductResponseDto> cmsProducts = cmsServiceClient.fetchAndCacheAllProducts();
        List<MspProduct> assignedProducts = mspProductRepository.findByMspId(mspId);
        Map<String, MspProduct> activeProductByCmsProductId = assignedProducts.stream()
                .filter(this::isActiveMspProduct)
                .collect(Collectors.toMap(
                        MspProduct::getProductId,
                        p -> p,
                        this::preferLatestMspProduct
                ));

        Map<String, List<MspProduct>> productsByCmsProductId = assignedProducts.stream()
                .collect(Collectors.groupingBy(MspProduct::getProductId));

        List<MspProductCatalogItemDto> allItems = cmsProducts.values().stream()
                .filter(product -> product.getProductId() != null)
                .sorted(Comparator.comparing(
                        CmsFullProductResponseDto::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(cmsProduct -> toMspProductCatalogItem(
                        cmsProduct, activeProductByCmsProductId, productsByCmsProductId))
                .filter(item -> matchesCatalogSearch(item, search))
                .filter(item -> matchesCatalogStatusFilter(item, mspProductStatus))
                .toList();

        long totalCount = allItems.size();
        int startIndex = Math.min(offset, allItems.size());
        int endIndex = Math.min(startIndex + pageSize, allItems.size());
        List<MspProductCatalogItemDto> pageItems = startIndex < endIndex
                ? allItems.subList(startIndex, endIndex)
                : List.of();

        return MspProductCatalogResponseDto.builder()
                .mspId(mspId)
                .offset(offset)
                .pageSize(pageSize)
                .totalCount(totalCount)
                .items(pageItems)
                .build();
    }

    @Override
    public AllResponseDto<List<MspAssignedProductResponseDto>> getAssignedMspProducts(
            String mspId, String search, String status, Integer offset, Integer pageSize, String sortBy, String order) {

        if (!mspUsersRepository.existsById(mspId)) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        int normalizedOffset = offset != null && offset >= 0 ? offset : 0;
        int normalizedPageSize = pageSize != null && pageSize > 0 ? pageSize : 10;

        Map<String, CmsFullProductResponseDto> cmsProducts = cmsServiceClient.fetchAndCacheAllProducts();
        List<MspProduct> assignedProducts = mspProductRepository.findByMspId(mspId);

        Map<String, List<MspProduct>> assignedByProductId = assignedProducts.stream()
                .filter(product -> product.getProductId() != null && !product.getProductId().isBlank())
                .collect(Collectors.groupingBy(MspProduct::getProductId));

        List<MspAssignedProductResponseDto> allAssignedProducts = assignedByProductId.entrySet().stream()
                .map(entry -> toAssignedMspProductResponse(cmsProducts.get(entry.getKey()), entry.getValue()))
                .filter(Objects::nonNull)
                .filter(product -> matchesAssignedProductSearch(product, search))
                .filter(product -> matchesAssignedProductStatus(product, status))
                .sorted(assignedProductComparator(sortBy, order))
                .toList();

        long total = allAssignedProducts.size();
        int startIndex = Math.min(normalizedOffset, allAssignedProducts.size());
        int endIndex = Math.min(startIndex + normalizedPageSize, allAssignedProducts.size());
        List<MspAssignedProductResponseDto> pageItems = startIndex < endIndex
                ? allAssignedProducts.subList(startIndex, endIndex)
                : List.of();

        return new AllResponseDto<>(normalizedOffset, normalizedPageSize, total, pageItems);
    }

    @Override
    public List<MspProductSimpleResponseDto> getUniqueProductsByMspId(String mspId) {
        log.info("Fetching unique products for mspId: {}", mspId);

        if (!mspUsersRepository.existsById(mspId)) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        List<MspProduct> assignedProducts = mspProductRepository.findByMspId(mspId);
        if (assignedProducts.isEmpty()) {
            log.info("No MSP products found for mspId: {}", mspId);
            return List.of();
        }

        Set<String> uniqueProductIds = assignedProducts.stream()
                .map(MspProduct::getProductId)
                .filter(productId -> productId != null && !productId.isBlank())
                .collect(Collectors.toSet());

        if (uniqueProductIds.isEmpty()) {
            log.info("No valid productIds found for mspId: {}", mspId);
            return List.of();
        }

        Map<String, CmsFullProductResponseDto> cmsProducts = cmsServiceClient.fetchAndCacheAllProducts();

        List<MspProductSimpleResponseDto> response = uniqueProductIds.stream()
                .map(cmsProducts::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        CmsFullProductResponseDto::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(product -> MspProductSimpleResponseDto.builder()
                        .id(product.getProductId())
                        .productName(product.getProductName())
                        .thumbnailUrl(product.getThumbnailUrl())
                        .displayOrder(product.getDisplayOrder())
                        .build())
                .toList();

        log.info("Successfully mapped {} unique products for mspId: {}", response.size(), mspId);
        return response;
    }

    @Override
    public List<String> getPurchasedPackageIdsByMspIdAndProductId(String mspId, String productId) {
        log.info("Fetching purchased package IDs for mspId: {} and productId: {}", mspId, productId);

        if (!mspUsersRepository.existsById(mspId)) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        List<MspProduct> assignedProducts = mspProductRepository.findByMspIdAndProductId(mspId, productId);
        if (assignedProducts == null || assignedProducts.isEmpty()) {
            log.info("No MSP products found for mspId: {} and productId: {}", mspId, productId);
            return List.of();
        }

        List<String> packageIds = assignedProducts.stream()
                .map(MspProduct::getPackageId)
                .filter(packageId -> packageId != null && !packageId.isBlank())
                .distinct()
                .toList();

        log.info("Found {} purchased package IDs for mspId: {} and productId: {}",
                packageIds.size(), mspId, productId);
        return packageIds;
    }

    @Override
    public List<MspPackageSimpleResponseDto> getPackagesByMspIdAndProductId(String mspId, String productId) {
        log.info("Fetching packages for mspId: {} and productId: {}", mspId, productId);

        if (!mspUsersRepository.existsById(mspId)) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        List<MspProduct> assignedProducts = mspProductRepository.findByMspIdAndProductId(mspId, productId);
        if (assignedProducts == null || assignedProducts.isEmpty()) {
            log.info("No MSP products found for mspId: {} and productId: {}", mspId, productId);
            return List.of();
        }

        Set<String> uniquePackageIds = assignedProducts.stream()
                .map(MspProduct::getPackageId)
                .filter(packageId -> packageId != null && !packageId.isBlank())
                .collect(Collectors.toSet());

        if (uniquePackageIds.isEmpty()) {
            log.info("No valid packageIds found for mspId: {} and productId: {}", mspId, productId);
            return List.of();
        }

        log.info("Found {} unique packageIds for mspId: {} and productId: {}",
                uniquePackageIds.size(), mspId, productId);

        List<CmsPackageSimpleResponseDto> packages = cmsServiceClient.getPackagesByIds(uniquePackageIds);
        if (packages == null || packages.isEmpty()) {
            log.warn("No packages returned from CMS for packageIds: {} (mspId: {}, productId: {})",
                    uniquePackageIds, mspId, productId);
            return List.of();
        }

        List<MspPackageSimpleResponseDto> response = packages.stream()
                .filter(pkg -> pkg.getId() != null && uniquePackageIds.contains(pkg.getId()))
                .map(pkg -> MspPackageSimpleResponseDto.builder()
                        .id(pkg.getId())
                        .name(pkg.getName())
                        .build())
                .toList();
        log.info("Successfully mapped {} packages for mspId: {} and productId: {}",
                response.size(), mspId, productId);
        return response;
    }

    @Override
    public AllResponseDto<List<CmsTopicMinimalDto>> getClientProductTopicsByMspId(
            String mspId, MspClientProductTopicsRequestDto request) {
        log.info("Fetching MSP-product topics for mspId: {}", mspId);

        if (!mspUsersRepository.existsById(mspId)) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        MspClientProductTopicsRequestDto effectiveRequest = request != null
                ? request
                : MspClientProductTopicsRequestDto.builder().build();

        int normalizedOffset = effectiveRequest.getOffset() != null && effectiveRequest.getOffset() >= 0
                ? effectiveRequest.getOffset() : 1;
        int normalizedPageSize = effectiveRequest.getPageSize() != null && effectiveRequest.getPageSize() > 0
                ? effectiveRequest.getPageSize() : 10;
        boolean isAvailable = effectiveRequest.getIsAvailable() == null || Boolean.TRUE.equals(effectiveRequest.getIsAvailable());
        String sortBy = effectiveRequest.getSortBy() != null ? effectiveRequest.getSortBy() : "createdAt";
        String order = effectiveRequest.getOrder() != null ? effectiveRequest.getOrder() : "desc";

        List<MspProduct> mspProducts = mspProductRepository.findByMspId(mspId);
        List<CmsProductPackagePairDto> productPackages = (mspProducts == null ? List.<MspProduct>of() : mspProducts).stream()
                .filter(mp -> mp.getProductId() != null && !mp.getProductId().isBlank()
                        && mp.getPackageId() != null && !mp.getPackageId().isBlank())
                .map(mp -> CmsProductPackagePairDto.builder()
                        .productId(mp.getProductId())
                        .packageId(mp.getPackageId())
                        .build())
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new java.util.LinkedHashSet<>()),
                        ArrayList::new));

        if (productPackages.isEmpty()) {
            log.info("No valid productId/packageId pairs for mspId: {} — fetching locked total as all topics", mspId);
        }

        // Single CMS call returns the requested page plus both totals
        // (empty productPackages => total=0, totalLocked=all topics)
        CmsTopicsByProductPackagesRequestDto cmsRequest = CmsTopicsByProductPackagesRequestDto.builder()
                .productPackages(productPackages)
                .filter(effectiveRequest.getFilter())
                .search(effectiveRequest.getSearch())
                .offset(normalizedOffset)
                .pageSize(normalizedPageSize)
                .sortBy(sortBy)
                .order(order)
                .excludeMatchingPairs(!isAvailable)
                .build();

        CmsTopicPageResponseDto cmsResponse = cmsServiceClient.fetchTopicsByProductPackages(cmsRequest);

        long total = cmsResponse != null ? cmsResponse.getTotal() : 0L;
        long totalLocked = cmsResponse != null ? cmsResponse.getTotalLocked() : 0L;
        List<CmsTopicMinimalDto> items = cmsResponse != null && cmsResponse.getItems() != null
                ? cmsResponse.getItems()
                : List.of();

        log.info("Retrieved {} topics (isAvailable={}, total={}, totalLocked={}) for mspId: {} from {} MSP product/package pairs",
                items.size(), isAvailable, total, totalLocked, mspId, productPackages.size());

        return new AllResponseDto<>(normalizedOffset, normalizedPageSize, total, totalLocked, items);
    }

    @Override
    public MspProductDetailedDto getMspProductDetailById(String id) {
        log.info("Retrieving MSP product-package details for ID: {}", id);

        try {
            MspProduct mspProduct = mspProductRepository.findById(id)
                    .orElseThrow(() -> new RegistrationServiceException(
                            "MSP product-package not found with ID: " + id));

            return convertToMspProductDetailedDto(mspProduct);
        } catch (RegistrationServiceException e) {
            log.error("MSP product-package not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving MSP product-package details for ID: {}", id, e);
            throw new RegistrationServiceException(
                    "Failed to retrieve MSP product-package details: " + e.getMessage(), e);
        }
    }

    private MspProductDetailedDto convertToMspProductDetailedDto(MspProduct mspProduct) {
        try {
            log.debug("Converting MspProduct to Detailed DTO - ProductId: {}, PackageId: {}",
                    mspProduct.getProductId(), mspProduct.getPackageId());

            CmsProductWithSinglePackageResponseDto productPackageDetails = cmsServiceClient.getProductPackageDetails(
                    mspProduct.getProductId(),
                    mspProduct.getPackageId()
            );

            CmsProductResponseDto productDetails = null;
            CmsPackageDto packageDetails = null;
            Integer topicCount = null;

            if (productPackageDetails != null) {
                productDetails = CmsProductResponseDto.builder()
                        .productId(productPackageDetails.getProductId())
                        .productName(productPackageDetails.getProductName())
                        .productDescription(productPackageDetails.getProductDescription())
                        .productStatus(productPackageDetails.getProductStatus())
                        .thumbnailUrl(productPackageDetails.getThumbnailUrl())
                        .displayOrder(productPackageDetails.getDisplayOrder())
                        .tags(productPackageDetails.getTags())
                        .build();

                packageDetails = productPackageDetails.getPackages();
                topicCount = productPackageDetails.getTopicCount();
            } else {
                log.warn("No product package details found for productId: {}, packageId: {}",
                        mspProduct.getProductId(), mspProduct.getPackageId());
            }

            MspProductDetailedDto.PaymentPayloadDto paymentPayloadDto = null;
            if (mspProduct.getPaymentPayload() != null) {
                paymentPayloadDto = MspProductDetailedDto.PaymentPayloadDto.builder()
                        .clientId(mspProduct.getPaymentPayload().getClientId())
                        .currency(mspProduct.getPaymentPayload().getCurrency())
                        .amount(mspProduct.getPaymentPayload().getAmount())
                        .subtotal(mspProduct.getPaymentPayload().getSubtotal())
                        .vatAmount(mspProduct.getPaymentPayload().getVatAmount())
                        .discountAmount(mspProduct.getPaymentPayload().getDiscountAmount())
                        .discountPercentage(mspProduct.getPaymentPayload().getDiscountPercentage())
                        .date(mspProduct.getPaymentPayload().getDate() != null
                                ? mspProduct.getPaymentPayload().getDate().toInstant()
                                : null)
                        .notes(mspProduct.getPaymentPayload().getNotes())
                        .clientRegion(mspProduct.getPaymentPayload().getClientRegion())
                        .build();
            }

            return MspProductDetailedDto.builder()
                    .id(mspProduct.getId())
                    .mspId(mspProduct.getMspId())
                    .productId(mspProduct.getProductId())
                    .packageId(mspProduct.getPackageId())
                    .product(productDetails)
                    .packageDetails(packageDetails)
                    .licenseCount(mspProduct.getLicenseCount())
                    .usedLicenseCount(mspProduct.getUsedLicenseCount())
                    .pricePerLicense(mspProduct.getPricePerLicense())
                    .totalPrice(mspProduct.getTotalPrice())
                    .validityPeriod(mspProduct.getValidityPeriod())
                    .validityUnit(mspProduct.getValidityUnit())
                    .assignedAt(mspProduct.getAssignedAt())
                    .expiryDate(mspProduct.getExpiryDate())
                    .licenseStatus(mspProduct.getLicenseStatus())
                    .countryId(mspProduct.getCountryId())
                    .paymentPayload(paymentPayloadDto)
                    .topicCount(topicCount)
                    .build();

        } catch (Exception e) {
            log.error("Error converting MspProduct to Detailed DTO for productId: {}, packageId: {}",
                    mspProduct.getProductId(), mspProduct.getPackageId(), e);

            return MspProductDetailedDto.builder()
                    .id(mspProduct.getId())
                    .mspId(mspProduct.getMspId())
                    .productId(mspProduct.getProductId())
                    .packageId(mspProduct.getPackageId())
                    .product(null)
                    .packageDetails(null)
                    .licenseCount(mspProduct.getLicenseCount())
                    .usedLicenseCount(mspProduct.getUsedLicenseCount())
                    .pricePerLicense(mspProduct.getPricePerLicense())
                    .totalPrice(mspProduct.getTotalPrice())
                    .validityPeriod(mspProduct.getValidityPeriod())
                    .validityUnit(mspProduct.getValidityUnit())
                    .assignedAt(mspProduct.getAssignedAt())
                    .expiryDate(mspProduct.getExpiryDate())
                    .licenseStatus(mspProduct.getLicenseStatus())
                    .countryId(mspProduct.getCountryId())
                    .paymentPayload(null)
                    .topicCount(null)
                    .build();
        }
    }

    private MspAssignedProductResponseDto toAssignedMspProductResponse(
            CmsFullProductResponseDto cmsProduct,
            List<MspProduct> assignedProducts) {
        if (cmsProduct == null) {
            return null;
        }

        Map<String, MspProduct> assignedByPackageId = assignedProducts == null
                ? Map.of()
                : assignedProducts.stream()
                .filter(product -> product.getPackageId() != null && !product.getPackageId().isBlank())
                .collect(Collectors.toMap(MspProduct::getPackageId, product -> product, (existing, ignored) -> existing));

        List<MspAssignedPackageResponseDto> assignedPackages = cmsProduct.getPackages() == null
                ? List.of()
                : cmsProduct.getPackages().stream()
                .filter(pkg -> pkg.getId() != null && assignedByPackageId.containsKey(pkg.getId()))
                .map(pkg -> toAssignedMspPackageResponse(pkg, cmsProduct.getProductId(), assignedByPackageId.get(pkg.getId())))
                .toList();

        return MspAssignedProductResponseDto.builder()
                .productId(cmsProduct.getProductId())
                .productName(cmsProduct.getProductName())
                .productDescription(cmsProduct.getProductDescription())
                .productStatus(cmsProduct.getProductStatus())
                .thumbnailUrl(cmsProduct.getThumbnailUrl())
                .tags(cmsProduct.getTags() != null ? cmsProduct.getTags() : List.of())
                .createdAt(cmsProduct.getCreatedAt())
                .updatedAt(cmsProduct.getUpdatedAt())
                .lastModifiedBy(cmsProduct.getLastModifiedBy())
                .packages(assignedPackages)
                .isTrial(cmsProduct.getIsTrial())
                .showInSite(cmsProduct.getShowInSite())
                .displayOrder(cmsProduct.getDisplayOrder())
                .build();
    }

    private MspAssignedPackageResponseDto toAssignedMspPackageResponse(
            CmsPackageDto pkg, String productId, MspProduct mspProduct) {
        List<CmsFeatureDto> features = pkg.getFeatures() == null
                ? List.of()
                : pkg.getFeatures().stream()
                .map(feature -> CmsFeatureDto.builder()
                        .id(feature.getId())
                        .name(feature.getName())
                        .build())
                .toList();

        MspAssignedPackageResponseDto.MspAssignedPackageResponseDtoBuilder builder = MspAssignedPackageResponseDto.builder()
                .id(pkg.getId())
                .packageName(pkg.getPackageName())
                .productId(pkg.getProductId() != null ? pkg.getProductId() : productId)
                .features(features)
                .price(pkg.getPrice())
                .yearlyPrice(pkg.getYearlyPrice())
                .packageStatus(pkg.getPackageStatus())
                .basePackageId(pkg.getBasePackageId())
                .isTrial(pkg.getIsTrial())
                .showInSite(pkg.getShowInSite())
                .isPriceRange(pkg.getIsPriceRange())
                .rangePricing(pkg.getRangePricing() != null ? pkg.getRangePricing() : List.of())
                .rangePricingResponse(pkg.getRangePricingResponse() != null ? pkg.getRangePricingResponse() : List.of());

        if (mspProduct != null) {
            builder
                    .mspProductId(mspProduct.getId())
                    .licenseCount(mspProduct.getLicenseCount())
                    .usedLicenseCount(mspProduct.getUsedLicenseCount())
                    .pricePerLicense(mspProduct.getPricePerLicense())
                    .totalPrice(mspProduct.getTotalPrice())
                    .validityPeriod(mspProduct.getValidityPeriod())
                    .validityUnit(mspProduct.getValidityUnit())
                    .assignedAt(mspProduct.getAssignedAt())
                    .expiryDate(mspProduct.getExpiryDate())
                    .licenseStatus(mspProduct.getLicenseStatus())
                    .countryId(mspProduct.getCountryId());
        }

        return builder.build();
    }

    private boolean matchesAssignedProductSearch(MspAssignedProductResponseDto product, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String normalizedSearch = search.toLowerCase();
        return product.getProductName() != null
                && product.getProductName().toLowerCase().contains(normalizedSearch);
    }

    private boolean matchesAssignedProductStatus(MspAssignedProductResponseDto product, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return product.getProductStatus() != null && status.equalsIgnoreCase(product.getProductStatus());
    }

    private Comparator<MspAssignedProductResponseDto> assignedProductComparator(String sortBy, String order) {
        Comparator<MspAssignedProductResponseDto> comparator = switch (sortBy != null ? sortBy : "") {
            case "productName" -> Comparator.comparing(
                    MspAssignedProductResponseDto::getProductName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "productStatus" -> Comparator.comparing(
                    MspAssignedProductResponseDto::getProductStatus,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "createdAt" -> Comparator.comparing(
                    MspAssignedProductResponseDto::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "updatedAt" -> Comparator.comparing(
                    MspAssignedProductResponseDto::getUpdatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(
                    MspAssignedProductResponseDto::getDisplayOrder,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };

        return "desc".equalsIgnoreCase(order) ? comparator.reversed() : comparator;
    }

    private void validateMspBuyNowRequest(MspBuyNowRequestDto dto) {
        if (dto == null) {
            throw new RegistationValidationException("MSP buy-now request is required");
        }
        if (dto.getMspId() == null || dto.getMspId().isBlank()) {
            throw new RegistationValidationException("MSP ID is required");
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

    private void validateMspProductsAvailableForPurchase(String mspId, List<ProductSelectionDto> selections) {
        for (ProductSelectionDto selection : selections) {
            List<MspProduct> existing = mspProductRepository.findByMspIdAndProductId(mspId, selection.getProductId());
            boolean hasActive = existing.stream().anyMatch(this::isActiveMspProduct);
            if (hasActive) {
                throw new RegistationValidationException(
                        "Product is already active for this MSP: " + selection.getProductName());
            }
        }
    }

    private boolean isActiveMspProduct(MspProduct product) {
        if (product == null || !"ACTIVE".equalsIgnoreCase(product.getLicenseStatus())) {
            return false;
        }
        return product.getExpiryDate() == null || product.getExpiryDate().isAfter(Instant.now());
    }

    private OrganizationInfoForMspDto buildOrganizationInfoFromMspUser(MspUser mspUser) {
        return OrganizationInfoForMspDto.builder()
                .organizationName(mspUser.getOrganizationName())
                .mspAdminEmail(mspUser.getMspAdminEmail())
                .contactEmail(mspUser.getContactEmail())
                .phoneNumber(mspUser.getPhoneNumber())
                .phoneCode(mspUser.getPhoneCode())
                .country(mspUser.getCountry())
                .stateProvince(mspUser.getStateProvince())
                .build();
    }

    private BillingInfoForMspDto buildBillingInfoFromMspUser(MspUser mspUser) {
        return BillingInfoForMspDto.builder()
                .billingEmail(mspUser.getBillingEmail())
                .billingName(mspUser.getBillingName())
                .streetAddress(mspUser.getBillingStreetAddress())
                .streetAddressLine2(mspUser.getBillingStreetAddressLine2())
                .city(mspUser.getBillingCity())
                .stateProvince(mspUser.getBillingStateProvince())
                .country(mspUser.getBillingCountry() != null ? mspUser.getBillingCountry() : mspUser.getCountry())
                .zipPostalCode(mspUser.getBillingZipPostalCode())
                .build();
    }

    private void deleteMspProductsByIds(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        for (String id : productIds) {
            try {
                mspProductRepository.deleteById(id);
            } catch (Exception e) {
                log.error("Failed to delete MspProduct {} during rollback: {}", id, e.getMessage());
            }
        }
    }

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
            log.info("Deleted billing invoice {} during MSP buy-now rollback", invoiceId);
        } catch (Exception e) {
            log.warn("Failed to delete billing invoice {} during rollback: {}", invoiceId, e.getMessage());
        }
    }

    private static RegistrationServiceException wrapAsRegistrationException(String context, Exception e) {
        if (e instanceof RegistrationServiceException) {
            return (RegistrationServiceException) e;
        }
        return new RegistrationServiceException(context + ": " + e.getMessage(), e);
    }

    private void sendMspBuyNowInvoiceEmail(
            MspUser mspUser,
            InvoiceRequestDTO invoiceRequestDTO,
            InvoiceResponseDTO invoiceResponse
    ) {
        String adminEmail = mspUser.getMspAdminEmail();
        if (adminEmail == null || adminEmail.isBlank()) {
            throw new RegistrationServiceException("MSP admin email is required to send buy-now invoice email");
        }

        String invoiceId = invoiceResponse.getId();
        String organizationName = mspUser.getOrganizationName() != null
                ? mspUser.getOrganizationName()
                : invoiceRequestDTO.getClientName();

        double subtotal = invoiceResponse.getSubtotal();
        double discount = invoiceResponse.getDiscountAmount();
        double couponDiscount = invoiceRequestDTO.getCouponDiscountAmount();
        double vatPercentage = calculateVatPercentage(invoiceResponse.getVatAmount(), subtotal, discount);
        double vat = invoiceResponse.getVatAmount();
        double total = invoiceResponse.getTotalAmount();
        Instant createdAt = invoiceResponse.getCreatedAt() != null
                ? invoiceResponse.getCreatedAt()
                : Instant.now();

        com.aspire.asat.common.enums.InvoiceStatus commonStatus = convertToCommonInvoiceStatus(
                invoiceResponse.getStatus() != null ? invoiceResponse.getStatus() : InvoiceStatus.PENDING);

        List<com.aspire.asat.common.dto.ProductSelectionDto> commonProductSelections =
                invoiceRequestDTO.getProductSelections() != null
                        ? invoiceRequestDTO.getProductSelections().stream()
                        .map(this::convertToCommonProductSelectionDto)
                        .toList()
                        : List.of();

        ByteArrayOutputStream pdfStream = invoiceGenerator.generateInvoicePdf(
                invoiceId,
                organizationName,
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
            throw new RegistrationServiceException("Failed to generate invoice PDF for MSP buy-now flow");
        }

        Result result;
        try {
            result = getResult(invoiceId, pdfStream);
        } catch (IOException e) {
            throw new RegistrationServiceException("Failed to upload invoice PDF for MSP buy-now flow", e);
        }
        if (result.tempFile().exists()) {
            result.tempFile().delete();
        }

        AttachmentDto attachment = AttachmentDto.builder()
                .bucketName(s3BucketName)
                .objectKey(result.s3ObjectKey())
                .build();

        Map<String, Object> templateModel = new HashMap<>();
        String userName = organizationName != null && !organizationName.isBlank()
                ? organizationName
                : "MSP Admin";
        templateModel.put(NotificationTemplateValue.USER_NAME, userName);
        templateModel.put(NotificationTemplateValue.ASSIGNMENT_DATE,
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        templateModel.put(NotificationTemplateValue.PACKAGE_DETAILS,
                "New products have been added to your MSP account. Please find the invoice attached.");
        if (mspUser.getLogoUrl() != null && !mspUser.getLogoUrl().isBlank()) {
            templateModel.put(NotificationTemplateValue.LOGO_URL, mspUser.getLogoUrl());
        }

        notificationServiceClient.sendProductReassignmentNotification(
                adminEmail,
                mspUser.getId(),
                mspUser.getId(),
                templateModel,
                List.of(attachment)
        );
    }

    private MspProductCatalogItemDto toMspProductCatalogItem(
            CmsFullProductResponseDto cmsProduct,
            Map<String, MspProduct> activeProductByCmsProductId,
            Map<String, List<MspProduct>> productsByCmsProductId) {

        MspProduct activeProduct = activeProductByCmsProductId.get(cmsProduct.getProductId());
        String mspProductStatus = activeProduct != null ? "ENABLED" : "DISABLED";
        List<MspProduct> assignedForProduct = productsByCmsProductId.getOrDefault(
                cmsProduct.getProductId(), List.of());
        Map<String, MspProduct> assignedByPackageId = assignedForProduct.stream()
                .collect(Collectors.toMap(
                        MspProduct::getPackageId,
                        p -> p,
                        this::preferLatestMspProduct
                ));

        List<MspProductCatalogPackageDto> packages = cmsProduct.getPackages() == null
                ? List.of()
                : cmsProduct.getPackages().stream()
                .filter(pkg -> pkg.getPackageStatus() == null || "ENABLED".equalsIgnoreCase(pkg.getPackageStatus()))
                .map(pkg -> toMspProductCatalogPackage(pkg, assignedByPackageId.get(pkg.getId())))
                .toList();

        return MspProductCatalogItemDto.builder()
                .productId(cmsProduct.getProductId())
                .productName(cmsProduct.getProductName())
                .productDescription(cmsProduct.getProductDescription())
                .thumbnailUrl(cmsProduct.getThumbnailUrl())
                .displayOrder(cmsProduct.getDisplayOrder())
                .mspProductStatus(mspProductStatus)
                .mspProductId(activeProduct != null ? activeProduct.getId() : null)
                .licenseStatus(activeProduct != null ? activeProduct.getLicenseStatus() : null)
                .packages(packages)
                .build();
    }

    private MspProductCatalogPackageDto toMspProductCatalogPackage(CmsPackageDto pkg, MspProduct assigned) {
        return MspProductCatalogPackageDto.builder()
                .packageId(pkg.getId())
                .packageName(pkg.getPackageName())
                .price(pkg.getPrice())
                .packageStatus(pkg.getPackageStatus())
                .mspProductId(assigned != null ? assigned.getId() : null)
                .licenseStatus(assigned != null ? assigned.getLicenseStatus() : null)
                .build();
    }

    private MspProduct preferLatestMspProduct(MspProduct existing, MspProduct replacement) {
        if (existing.getAssignedAt() == null) {
            return replacement;
        }
        if (replacement.getAssignedAt() == null) {
            return existing;
        }
        return existing.getAssignedAt().isAfter(replacement.getAssignedAt()) ? existing : replacement;
    }

    private boolean matchesCatalogSearch(MspProductCatalogItemDto item, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        return item.getProductName() != null
                && item.getProductName().toLowerCase().contains(search.toLowerCase());
    }

    private boolean matchesCatalogStatusFilter(MspProductCatalogItemDto item, String mspProductStatus) {
        if (mspProductStatus == null || mspProductStatus.isBlank()) {
            return true;
        }
        return mspProductStatus.equalsIgnoreCase(item.getMspProductStatus());
    }

    @Override
    @Transactional
    public void activateLicense(String mspId, String invoiceId) {
        log.info("Starting MSP license activation for mspId: {}, invoiceId: {}", mspId, invoiceId);

        MspUser mspUser = mspUsersRepository.findById(mspId)
                .orElseThrow(() -> new RegistrationServiceException("MSP is not eligible for activation. MSP not found with ID: " + mspId));

        mspUser.setStatus("ACTIVE");
        mspUser.setUpdatedAt(Instant.now());
        MspUser updatedMspUser = mspUsersRepository.save(mspUser);
        log.info("Updated MspUser status to ACTIVE for ID: {}", mspId);

        final Set<String> productIdsToActivate;
        final List<MspInvoice> mspInvoices;
        if (invoiceId != null && !invoiceId.isBlank()) {
            mspInvoices = mspInvoiceRepository.findByIdAndMspId(invoiceId, mspId);
            if (mspInvoices.isEmpty()) {
                throw new RegistrationServiceException(
                        "MSP invoice not found for id: " + invoiceId + " and mspId: " + mspId);
            }
            productIdsToActivate = mspInvoices.stream()
                    .map(MspInvoice::getClientProductIds)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toCollection(HashSet::new));
            if (productIdsToActivate.isEmpty()) {
                throw new RegistrationServiceException(
                        "No MSP products linked to invoice id: " + invoiceId + " for mspId: " + mspId);
            }
        } else {
            productIdsToActivate = null;
            mspInvoices = mspInvoiceRepository.findByMspId(mspId);
        }

        List<MspProduct> mspProducts = mspProductRepository.findByMspId(mspId);
        int activatedCount = 0;
        for (MspProduct product : mspProducts) {
            if (!"PENDING".equals(product.getLicenseStatus())) {
                continue;
            }
            if (productIdsToActivate != null && !productIdsToActivate.contains(product.getId())) {
                continue;
            }
            product.setLicenseStatus("ACTIVE");
            mspProductRepository.save(product);
            activatedCount++;
        }
        log.info("Activated {} MSP products (licenseStatus set to ACTIVE) for MSP: {}", activatedCount, mspId);


        MspInvoice invoiceToUpdate = resolveMspInvoiceForActivation(mspInvoices, productIdsToActivate, invoiceId);
        if (invoiceToUpdate != null) {
            invoiceToUpdate.setStatus(InvoiceStatus.PAID);
            invoiceToUpdate.setPaidAt(Instant.now());
            invoiceToUpdate.setUpdatedAt(Instant.now());
            mspInvoiceRepository.save(invoiceToUpdate);
            log.info("Updated MspInvoice status to PAID for invoice ID: {} (MSP: {})", invoiceToUpdate.getId(), mspId);
        } else {
            log.warn("No matching PENDING or CREATED invoice found for MSP: {}. Invoice status not updated.", mspId);
        }

        try {
            aspireUserService.updateAspireUser(
                    updatedMspUser.getId(),
                    UserType.MSP,
                    updatedMspUser
            );
            log.info("Updated AspireUser record for MspUser with ID: {}", updatedMspUser.getId());
        } catch (Exception e) {
            log.error("Failed to update AspireUser record for MspUser with ID: {}", updatedMspUser.getId(), e);
        }

        log.info("MSP license activation completed successfully for mspId: {}", mspId);
    }

    private MspInvoice resolveMspInvoiceForActivation(
            List<MspInvoice> mspInvoices,
            Set<String> productIdsToActivate,
            String invoiceId) {
        if (invoiceId != null && !invoiceId.isBlank()) {
            return mspInvoices.stream()
                    .filter(inv -> invoiceId.equals(inv.getId()))
                    .filter(inv -> inv.getStatus() == InvoiceStatus.PENDING || inv.getStatus() == InvoiceStatus.CREATED)
                    .findFirst()
                    .orElse(null);
        }
        return mspInvoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PENDING || inv.getStatus() == InvoiceStatus.CREATED)
                .filter(inv -> matchesActivationProducts(inv.getClientProductIds(), productIdsToActivate))
                .max(Comparator.comparing(MspInvoice::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private boolean matchesActivationProducts(List<String> invoiceProductIds, Set<String> productIdsToActivate) {
        if (productIdsToActivate == null) {
            return true;
        }
        if (invoiceProductIds == null || invoiceProductIds.isEmpty()) {
            return false;
        }
        return new HashSet<>(invoiceProductIds).equals(productIdsToActivate);
    }

    @Override
    public long countAssignedClientsForMsp(String mspId) {
        log.info("Counting assigned clients for MSP: {}", mspId);

        if (mspId == null || mspId.isBlank()) {
            throw new RegistrationServiceException("MSP ID cannot be null or empty");
        }

        // Validate MSP exists
        Optional<MspUser> mspUserOpt = mspUsersRepository.findById(mspId);
        if (mspUserOpt.isEmpty()) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        return countAssignedClientsForMspInternal(mspId);
    }

    /**
     * Count total assigned clients for MSP (private helper method)
     */
    private long countAssignedClientsForMspInternal(String mspId) {
        return clientAdminRepository.countByMspId(mspId);
    }

    @Override
    public MspDashboardResponseDto getMspDashboard(String mspId) {
        log.info("Fetching MSP dashboard totals for mspId: {}", mspId);

        if (mspId == null || mspId.isBlank()) {
            throw new RegistrationServiceException("MSP ID cannot be null or empty");
        }

        if (!mspUsersRepository.existsById(mspId)) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        List<MspProduct> mspProducts = mspProductRepository.findByMspId(mspId);

        long totalProducts = mspProducts.stream()
                .map(MspProduct::getProductId)
                .filter(productId -> productId != null && !productId.isBlank())
                .distinct()
                .count();

        long totalPackages = mspProducts.stream()
                .map(MspProduct::getPackageId)
                .filter(packageId -> packageId != null && !packageId.isBlank())
                .distinct()
                .count();

        long totalLicenses = mspProducts.stream()
                .mapToLong(MspProduct::getLicenseCount)
                .sum();

        long totalClients = countAssignedClientsForMspInternal(mspId);

        log.info("MSP dashboard for {}: products={}, licenses={}, packages={}, clients={}",
                mspId, totalProducts, totalLicenses, totalPackages, totalClients);

        return MspDashboardResponseDto.builder()
                .totalProducts(totalProducts)
                .totalLicenses(totalLicenses)
                .totalPackages(totalPackages)
                .totalClients(totalClients)
                .build();
    }

    @Override
    public MspTopicCountsResponseDto getMspTopicCounts(String mspId) {
        log.info("Fetching MSP topic monthly distribution for mspId: {}", mspId);

        if (mspId == null || mspId.isBlank()) {
            throw new RegistrationServiceException("MSP ID cannot be null or empty");
        }

        if (!mspUsersRepository.existsById(mspId)) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        List<CmsProductPackagePairDto> mspPairs = mspProductRepository.findByMspId(mspId).stream()
                .filter(mp -> mp.getProductId() != null && !mp.getProductId().isBlank()
                        && mp.getPackageId() != null && !mp.getPackageId().isBlank())
                .map(mp -> CmsProductPackagePairDto.builder()
                        .productId(mp.getProductId().trim())
                        .packageId(mp.getPackageId().trim())
                        .build())
                .distinct()
                .toList();

        List<CmsProductPackagePairDto> clientPairs = clientProductRepository.findByMspId(mspId).stream()
                .filter(cp -> cp.getProductId() != null && !cp.getProductId().isBlank()
                        && cp.getPackageId() != null && !cp.getPackageId().isBlank())
                .map(cp -> CmsProductPackagePairDto.builder()
                        .productId(cp.getProductId().trim())
                        .packageId(cp.getPackageId().trim())
                        .build())
                .distinct()
                .toList();

        log.info("MSP topic distribution pairs for mspId={}: mspPairs={}, clientPairs={}",
                mspId, mspPairs.size(), clientPairs.size());

        var cmsResponse = cmsServiceClient.countTopicsByProductPackages(
                CmsTopicCountsByProductPackagesRequestDto.builder()
                        .mspProductPackages(mspPairs)
                        .clientProductPackages(clientPairs)
                        .build());

        List<MspTopicDistributionItemDto> data = (cmsResponse != null && cmsResponse.getData() != null)
                ? cmsResponse.getData().stream()
                .map(item -> MspTopicDistributionItemDto.builder()
                        .month(item.getMonth())
                        .totalContent(item.getTotalContent() != null ? item.getTotalContent() : 0L)
                        .usedContent(item.getUsedContent() != null ? item.getUsedContent() : 0L)
                        .build())
                .toList()
                : List.of();

        return MspTopicCountsResponseDto.builder()
                .data(data)
                .build();
    }

    @Override
    public List<com.aspire.asat.registration.data.mspUser.response.AssignedClientDto> getAssignedClientsForMsp(
            String mspId, Integer offset, Integer pageSize) {
        log.info("Getting assigned clients for MSP: {} with offset: {}, pageSize: {}", mspId, offset, pageSize);

        if (mspId == null || mspId.isBlank()) {
            throw new RegistrationServiceException("MSP ID cannot be null or empty");
        }

        // Validate MSP exists
        Optional<MspUser> mspUserOpt = mspUsersRepository.findById(mspId);
        if (mspUserOpt.isEmpty()) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        return getAssignedClientsForMspInternal(mspId, offset, pageSize);
    }

    /**
     * Get assigned clients for MSP with pagination (private helper method)
     */
    private List<AssignedClientDto> getAssignedClientsForMspInternal(String mspId, Integer offset, Integer pageSize) {
        List<ClientAdmin> clientAdmins = clientAdminRepository.findAllByMspId(mspId);

        // Apply pagination
        int effectiveOffset = (offset != null && offset >= 0) ? offset : 0;
        int effectivePageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;

        int skip = effectiveOffset == 0 ? 0 : (effectiveOffset * effectivePageSize);

        return clientAdmins.stream()
                .skip(skip)
                .limit(effectivePageSize)
                .map(clientAdmin -> {
                    // Get total license count from all ClientProducts for this client
                    List<ClientProduct> clientProducts = clientProductRepository
                            .findByClientAdminId(clientAdmin.getId());

                    int licenseCount = clientProducts.stream()
                            .mapToInt(ClientProduct::getLicenseCount)
                            .sum();

                    return AssignedClientDto.builder()
                            .clientId(clientAdmin.getId())
                            .clientName(clientAdmin.getOrganizationName())
                            .contactEmail(clientAdmin.getEmail())
                            .status(clientAdmin.getStatus() != null ? clientAdmin.getStatus().name() : null)
                            .licenseCount(licenseCount)
                            .createdAt(clientAdmin.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public AllResponseDto<List<MspListWithLicenseResponseDto>> getMspListWithLicenses(
            Integer offset,
            Integer pageSize,
            String search,
            MspStatus status,
            String mspTier,
            String country) {

        log.info("Fetching MSP list with licenses - offset: {}, pageSize: {}, search: {}, status: {}, mspTier: {}, country: {}",
                offset, pageSize, search, status, mspTier, country);

        // Set default values
        if (offset == null || offset < 0) {
            offset = 0;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        // Create pageable object
        Pageable pageable = PageRequest.of(offset, pageSize);

        // Fetch paginated MSP users with filters
        Page<MspUser> mspPage = mspUsersRepositoryCustom.findMspUsersWithFilters(search, status, mspTier, country, pageable);

        // Extract MSP IDs from the page
        List<String> mspIds = mspPage.getContent().stream()
                .map(MspUser::getId)
                .collect(Collectors.toList());

        // Batch fetch all products for these MSPs
        Map<String, LicenseAggregate> licenseMap = aggregateLicenseCountsByMspId(mspIds);

        // Convert to response DTOs
        List<MspListWithLicenseResponseDto> mspList = mspPage.getContent().stream()
                .map(mspUser -> convertToMspListWithLicenseResponseDto(mspUser, licenseMap))
                .collect(Collectors.toList());

        log.info("Retrieved {} MSPs out of {} total with license counts",
                mspList.size(), mspPage.getTotalElements());

        return new AllResponseDto<>(offset, pageSize, mspPage.getTotalElements(), mspList);
    }

    /**
     * Aggregates license counts for multiple MSPs by batch fetching products
     *
     * @param mspIds List of MSP IDs
     * @return Map of MSP ID to LicenseAggregate containing total licenseCount and usedLicenseCount
     */
    private Map<String, LicenseAggregate> aggregateLicenseCountsByMspId(List<String> mspIds) {
        if (mspIds == null || mspIds.isEmpty()) {
            return new HashMap<>();
        }

        // Batch fetch all products for all MSPs in one query
        List<MspProduct> allProducts = mspProductRepository.findByMspIdIn(mspIds);

        // Initialize map with zero counts for all MSPs
        Map<String, LicenseAggregate> licenseMap = new HashMap<>();
        for (String mspId : mspIds) {
            licenseMap.put(mspId, new LicenseAggregate(0, 0));
        }

        // Group products by mspId and calculate aggregates
        for (MspProduct product : allProducts) {
            String mspId = product.getMspId();
            LicenseAggregate current = licenseMap.get(mspId);
            if (current != null) {
                licenseMap.put(mspId, new LicenseAggregate(
                        current.getLicenseCount() + product.getLicenseCount(),
                        current.getUsedLicenseCount() + product.getUsedLicenseCount()
                ));
            }
        }

        return licenseMap;
    }

    /**
     * Converts MspUser entity to MspListWithLicenseResponseDto with aggregated license counts
     *
     * @param mspUser    The MSP user entity
     * @param licenseMap Map of MSP ID to LicenseAggregate
     * @return MspListWithLicenseResponseDto
     */
    private MspListWithLicenseResponseDto convertToMspListWithLicenseResponseDto(
            MspUser mspUser,
            Map<String, LicenseAggregate> licenseMap) {

        LicenseAggregate licenseAggregate = licenseMap.getOrDefault(mspUser.getId(), new LicenseAggregate(0, 0));

        return MspListWithLicenseResponseDto.builder()
                .id(mspUser.getId())
                .organizationName(mspUser.getOrganizationName())
                .mspAdminEmail(mspUser.getMspAdminEmail())
                .mspTier(mspUser.getMspTier())
                .status(mspUser.getStatus())
                .licenseCount(licenseAggregate.getLicenseCount())
                .usedLicenseCount(licenseAggregate.getUsedLicenseCount())
                .country(mspUser.getCountry())
                .build();
    }

    /**
     * Helper class to hold aggregated license counts
     */
    private static class LicenseAggregate {
        private final int licenseCount;
        private final int usedLicenseCount;

        public LicenseAggregate(int licenseCount, int usedLicenseCount) {
            this.licenseCount = licenseCount;
            this.usedLicenseCount = usedLicenseCount;
        }

        public int getLicenseCount() {
            return licenseCount;
        }

        public int getUsedLicenseCount() {
            return usedLicenseCount;
        }
    }

    @Override
    public MspActiveLicenseSummaryResponseDto getActiveLicenseSummary() {
        log.info("Fetching total active license summary across all MSPs");

        // Fetch all products
        List<MspProduct> allProducts = mspProductRepository.findAll();

        // Filter products with ACTIVE licenseStatus and calculate totals
        int totalLicenseCount = 0;
        int totalUsedLicenseCount = 0;

        for (MspProduct product : allProducts) {
            if ("ACTIVE".equalsIgnoreCase(product.getLicenseStatus())) {
                totalLicenseCount += product.getLicenseCount();
                totalUsedLicenseCount += product.getUsedLicenseCount();
            }
        }

        log.info("Total active licenses: {}, Total active used licenses: {}", totalLicenseCount, totalUsedLicenseCount);

        return MspActiveLicenseSummaryResponseDto.builder()
                .licenseCount(totalLicenseCount)
                .usedLicenseCount(totalUsedLicenseCount)
                .build();
    }

    private com.aspire.asat.common.enums.InvoiceStatus convertToCommonInvoiceStatus(InvoiceStatus invStatus) {
        if (invStatus == null) {
            return com.aspire.asat.common.enums.InvoiceStatus.PENDING;
        }
        return com.aspire.asat.common.enums.InvoiceStatus.valueOf(invStatus.name());
    }

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

}
