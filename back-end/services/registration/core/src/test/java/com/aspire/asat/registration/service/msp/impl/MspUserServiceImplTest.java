package com.aspire.asat.registration.service.msp.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.common.service.files.FileService;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.OrganizationLicenseStatistics;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.clientAdmin.request.InvoiceDetailsDto;
import com.aspire.asat.registration.data.clientAdmin.request.ProductSelectionDto;
import com.aspire.asat.registration.data.clientAdmin.request.ValidityUnit;
import com.aspire.asat.registration.data.clientAdmin.response.OrganizationLicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.data.enums.MspStatus;
import com.aspire.asat.registration.data.invoice.InvoiceResponseDTO;
import com.aspire.asat.registration.data.invoice.InvoiceStatus;
import com.aspire.asat.registration.data.mspUser.request.BillingInfoForMspDto;
import com.aspire.asat.registration.data.cms.response.CmsFullProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import com.aspire.asat.registration.data.mspUser.request.MspBuyNowRequestDto;
import com.aspire.asat.registration.data.mspUser.request.MspOnboardingRequestDto;
import com.aspire.asat.registration.data.mspUser.request.MspUpdateRequestDto;
import com.aspire.asat.registration.data.mspUser.request.OrganizationInfoForMspDto;
import com.aspire.asat.registration.data.mspUser.response.AssignedClientDto;
import com.aspire.asat.registration.data.mspUser.response.MspActiveLicenseSummaryResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspDashboardResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspTopicCountsResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspDetailsResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspListResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspListWithLicenseResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspOnboardedListResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspOnboardingResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspUpdateResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspViewDetailsResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspProductCatalogResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspAssignedProductResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspPackageSimpleResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspProductDetailedDto;
import com.aspire.asat.registration.data.mspUser.response.MspProductSimpleResponseDto;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.model.dropdown.Industry;
import com.aspire.asat.registration.model.dropdown.SubIndustry;
import com.aspire.asat.registration.model.msp.MspInvoice;
import com.aspire.asat.registration.model.msp.MspProduct;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.NetTermConfigurationRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.TierConfigurationRepository;
import com.aspire.asat.registration.repository.UserLoginHistoryRepository;
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
import com.aspire.asat.registration.service.pricing.ProductPriceResolver;
import com.aspire.asat.registration.service.support.UserSessionInvalidationHelper;
import com.aspire.asat.registration.service.external.BillingService;
import com.aspire.asat.registration.service.external.InvoiceService;
import com.aspire.asat.common.dto.files.FileUploadResponse;
import com.aspire.asat.common.util.InvoiceGenerator;
import com.aspire.asat.registration.data.coupon.CouponResponseDTO;
import com.aspire.asat.registration.data.invoice.InvoiceRequestDTO;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MspUserServiceImplTest {

    private static final String MSP_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String ADMIN_EMAIL = "admin@msp.com";

    @Mock private MspProductRepository mspProductRepository;
    @Mock private MspUsersRepository mspUsersRepository;
    @Mock private MspUsersRepositoryCustom mspUsersRepositoryCustom;
    @Mock private MspInvoiceRepository mspInvoiceRepository;
    @Mock private AspireUserRepository aspireUserRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private TierConfigurationRepository tierConfigurationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AspireUserService aspireUserService;
    @Mock private CountryRepository countryRepository;
    @Mock private StateRepository stateRepository;
    @Mock private BillingService billingService;
    @Mock private InvoiceService invoiceService;
    @Mock private InvoiceGenerator invoiceGenerator;
    @Mock private FileService fileService;
    @Mock private RegistrationNotificationClient notificationServiceClient;
    @Mock private UserCurrentContextService currentContextService;
    @Mock private NetTermConfigurationRepository netTermConfigurationRepository;
    @Mock private ClientAdminRepository clientAdminRepository;
    @Mock private ClientProductRepository clientProductRepository;
    @Mock private CmsServiceClient cmsServiceClient;
    @Mock private UserLoginHistoryRepository userLoginHistoryRepository;
    @Mock private WebClient webClient;
    @Mock private IndustryRepository industryRepository;
    @Mock private SubIndustryRepository subIndustryRepository;
    @Mock private OrganizationSizeRepository organizationSizeRepository;
    @Mock private OrganizationTypeRepository organizationTypeRepository;
    @Mock private MspTypeRepository mspTypeRepository;
    @Mock private UserSessionInvalidationHelper userSessionInvalidationHelper;

    private ProductPriceResolver productPriceResolver;

    @InjectMocks
    private MspUserServiceImpl mspUserService;

    @BeforeEach
    void setUp() {
        productPriceResolver = new ProductPriceResolver(cmsServiceClient);
        setField(mspUserService, "productPriceResolver", productPriceResolver);
        stubFlatPackagePrice("product-id", "package-id", 100.0);
        stubFlatPackagePrice("prod-1", "pkg-1", 100.0);
    }

    private void stubFlatPackagePrice(String productId, String packageId, double price) {
        CmsPackageDto cmsPackage = CmsPackageDto.builder().id(packageId).price(price).build();
        CmsFullProductResponseDto product = CmsFullProductResponseDto.builder()
                .productId(productId)
                .packages(List.of(cmsPackage))
                .build();
        lenient().when(cmsServiceClient.getFullProductFromCache(productId)).thenReturn(product);
    }

    @Test
    void processMspOnboarding_throwsWhenAdminEmailAlreadyExists() {
        MspOnboardingRequestDto request = buildOnboardingRequest("New Industry", "New Sub Industry");
        when(mspUsersRepository.existsByMspAdminEmailIgnoreCase(ADMIN_EMAIL)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> mspUserService.processMspOnboarding(request));
        verify(industryRepository, never()).save(any());
    }

    @Test
    void processMspOnboarding_createsIndustryAndSubIndustryWhenNotExist() {
        MspOnboardingRequestDto request = buildOnboardingRequest("Custom Industry", "Custom Sub Industry");
        stubSuccessfulOnboardingDependencies();

        when(industryRepository.findById("Custom Industry")).thenReturn(Optional.empty());
        when(industryRepository.findByCode("Custom Industry")).thenReturn(Optional.empty());
        when(industryRepository.findByNameIgnoreCase("Custom Industry")).thenReturn(Optional.empty());
        when(industryRepository.save(any(Industry.class))).thenAnswer(invocation -> {
            Industry industry = invocation.getArgument(0);
            industry.setId("industry-id-1");
            return industry;
        });

        when(subIndustryRepository.findById("Custom Sub Industry")).thenReturn(Optional.empty());
        when(subIndustryRepository.findByIndustryIdAndCode("industry-id-1", "Custom Sub Industry"))
                .thenReturn(Optional.empty());
        when(subIndustryRepository.findByIndustryIdAndNameIgnoreCase("industry-id-1", "Custom Sub Industry"))
                .thenReturn(Optional.empty());
        when(subIndustryRepository.existsByIndustryIdAndCodeIgnoreCase(eq("industry-id-1"), anyString()))
                .thenReturn(false);
        when(subIndustryRepository.save(any(SubIndustry.class))).thenAnswer(invocation -> {
            SubIndustry subIndustry = invocation.getArgument(0);
            subIndustry.setId("sub-industry-id-1");
            return subIndustry;
        });

        MspOnboardingResponseDto response = mspUserService.processMspOnboarding(request);

        assertNotNull(response);
        verify(industryRepository).save(any(Industry.class));

        ArgumentCaptor<SubIndustry> subIndustryCaptor = ArgumentCaptor.forClass(SubIndustry.class);
        verify(subIndustryRepository).save(subIndustryCaptor.capture());
        assertEquals("industry-id-1", subIndustryCaptor.getValue().getIndustryId());

        ArgumentCaptor<MspUser> mspUserCaptor = ArgumentCaptor.forClass(MspUser.class);
        verify(mspUsersRepository, atLeastOnce()).save(mspUserCaptor.capture());
        MspUser savedMspUser = mspUserCaptor.getAllValues().get(0);
        assertEquals("industry-id-1", savedMspUser.getIndustry());
        assertEquals("sub-industry-id-1", savedMspUser.getSubIndustry());
    }

    @Test
    void processMspOnboarding_reusesExistingIndustryByName() {
        MspOnboardingRequestDto request = buildOnboardingRequest("Existing Industry", null);
        stubSuccessfulOnboardingDependencies();

        Industry existingIndustry = Industry.builder()
                .id("existing-industry-id")
                .name("Existing Industry")
                .code("EXISTING_INDUSTRY")
                .active(true)
                .build();
        when(industryRepository.findById("Existing Industry")).thenReturn(Optional.empty());
        when(industryRepository.findByCode("Existing Industry")).thenReturn(Optional.empty());
        when(industryRepository.findByNameIgnoreCase("Existing Industry")).thenReturn(Optional.of(existingIndustry));

        mspUserService.processMspOnboarding(request);

        verify(industryRepository, never()).save(any());
        ArgumentCaptor<MspUser> mspUserCaptor = ArgumentCaptor.forClass(MspUser.class);
        verify(mspUsersRepository, atLeastOnce()).save(mspUserCaptor.capture());
        assertEquals("existing-industry-id", mspUserCaptor.getAllValues().get(0).getIndustry());
    }

    @Test
    void getAllMsp_returnsPaginatedResults() {
        MspUser mspUser = sampleMspUser();
        Page<MspUser> page = new PageImpl<>(List.of(mspUser));
        when(mspUsersRepositoryCustom.findMspUsersWithFilters(eq("search"), eq(MspStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        AllResponseDto<List<MspListResponseDto>> result =
                mspUserService.getAllMsp(0, 10, "search", MspStatus.ACTIVE);

        assertEquals(1, result.getItems().size());
        assertEquals(mspUser.getOrganizationName(), result.getItems().get(0).getOrganizationName());
    }

    @Test
    void getMspDetails_returnsDetailsWhenFound() {
        MspUser mspUser = sampleMspUser();
        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(mspUser));

        MspDetailsResponseDto result = mspUserService.getMspDetails(MSP_ID);

        assertNotNull(result);
        assertEquals(MSP_ID, result.getId());
        assertEquals(mspUser.getOrganizationName(), result.getOrganization().getOrganizationName());
    }

    @Test
    void getMspDetails_throwsWhenNotFound() {
        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class, () -> mspUserService.getMspDetails(MSP_ID));
    }

    @Test
    void getMspViewDetails_returnsViewDetailsWhenFound() {
        MspUser mspUser = sampleMspUser();
        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(mspUser));
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder().licenseCount(10).usedLicenseCount(3).build()
        ));

        MspViewDetailsResponseDto result = mspUserService.getMspViewDetails(MSP_ID);

        assertNotNull(result);
        assertEquals(MSP_ID, result.getId());
        assertEquals(10, result.getLicenseAllocation().getTotalLicenses());
    }

    @Test
    void getLicenseStatistics_returnsStatisticsWhenMspExists() {
        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(sampleMspUser()));
        when(mspUsersRepositoryCustom.getOrganizationLicenseStatisticsByMspId(MSP_ID))
                .thenReturn(OrganizationLicenseStatistics.builder()
                        .totalAvailableLicenses(100)
                        .totalAllocatedLicenses(80)
                        .totalActiveLicenses(70)
                        .totalExpiredLicenses(5)
                        .build());

        OrganizationLicenseStatisticsResponseDto result = mspUserService.getLicenseStatistics(MSP_ID);

        assertEquals(100, result.getTotalAvailableLicenses());
        assertEquals(80, result.getTotalAllocatedLicenses());
        assertEquals(70, result.getTotalActiveLicenses());
        assertEquals(5, result.getTotalExpiredLicenses());
    }

    @Test
    void getOnboardedMsps_returnsPaginatedOnboardedList() {
        MspUser mspUser = sampleMspUser();
        Page<MspUser> page = new PageImpl<>(List.of(mspUser));
        when(cmsServiceClient.fetchAndCacheAllProducts()).thenReturn(Map.of());
        when(mspUsersRepositoryCustom.findMspUsersWithFilters(any(), any(), any(Pageable.class))).thenReturn(page);
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(Collections.emptyList());
        when(mspInvoiceRepository.findByMspId(MSP_ID)).thenReturn(Collections.emptyList());
        when(clientAdminRepository.findAllByMspId(MSP_ID)).thenReturn(Collections.emptyList());
        when(userLoginHistoryRepository.findTopByUsernameIgnoreCaseAndActionOrderByLoginTimeDesc(anyString(), any()))
                .thenReturn(Optional.empty());

        AllResponseDto<List<MspOnboardedListResponseDto>> result =
                mspUserService.getOnboardedMsps(0, 10, null, null, null, null);

        assertEquals(1, result.getItems().size());
        assertEquals(mspUser.getOrganizationName(), result.getItems().get(0).getOrganizationName());
    }

    @Test
    void updateMspStatus_updatesStatusWhenMspExists() {
        MspUser mspUser = sampleMspUser();
        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(mspUser));
        when(mspUsersRepository.save(any(MspUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mspUserService.updateMspStatus(MSP_ID, MspStatus.ACTIVE);

        assertEquals(MspStatus.ACTIVE.name(), mspUser.getStatus());
        verify(mspUsersRepository).save(mspUser);
    }

    @Test
    void updateMsp_updatesOrganizationFields() {
        MspUser mspUser = sampleMspUser();
        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(mspUser));
        when(mspUsersRepository.save(any(MspUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MspUpdateRequestDto request = MspUpdateRequestDto.builder()
                .organizationName("Updated Org")
                .notes("Updated notes")
                .build();

        MspUpdateResponseDto response = mspUserService.updateMsp(MSP_ID, request);

        assertEquals("Updated Org", mspUser.getOrganizationName());
        assertEquals("Updated notes", mspUser.getNotes());
        assertNotNull(response);
        assertEquals("Updated Org", response.getOrganizationName());
    }

    @Test
    void updateMspUserStatus_updatesAspireUserStatus() {
        UUID userUuid = UUID.randomUUID();
        AspireUser aspireUser = AspireUser.builder()
                .userId(userUuid)
                .userType(UserType.MSP.name())
                .status(MspStatus.PENDING.name())
                .build();

        when(aspireUserRepository.findByUserId(userUuid)).thenReturn(Optional.of(aspireUser));
        when(clientAdminRepository.findAllByMspId(userUuid.toString())).thenReturn(Collections.emptyList());
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mspUserService.updateMspUserStatus(userUuid.toString(), MspStatus.ACTIVE);

        assertEquals(MspStatus.ACTIVE.name(), aspireUser.getStatus());
        verify(aspireUserRepository).save(aspireUser);
        verify(userSessionInvalidationHelper, never()).logoutUsersIfRestrictive(eq("ACTIVE"), anyList());
    }

    @Test
    void updateMspUserStatus_inactiveForcesLogout() {
        UUID userUuid = UUID.randomUUID();
        AspireUser aspireUser = AspireUser.builder()
                .userId(userUuid)
                .userType(UserType.MSP.name())
                .status(MspStatus.ACTIVE.name())
                .build();

        when(aspireUserRepository.findByUserId(userUuid)).thenReturn(Optional.of(aspireUser));
        when(clientAdminRepository.findAllByMspId(userUuid.toString())).thenReturn(Collections.emptyList());
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userSessionInvalidationHelper.isRestrictiveStatus("INACTIVE")).thenReturn(true);

        mspUserService.updateMspUserStatus(userUuid.toString(), MspStatus.INACTIVE);

        assertEquals(MspStatus.INACTIVE.name(), aspireUser.getStatus());
        verify(userSessionInvalidationHelper).logoutUsersIfRestrictive(eq("INACTIVE"), anyList());
    }

    @Test
    void suspendMspUser_updatesStatusToSuspend() {
        UUID userUuid = UUID.randomUUID();
        AspireUser aspireUser = AspireUser.builder()
                .userId(userUuid)
                .userType(UserType.MSP.name())
                .username(ADMIN_EMAIL)
                .email(ADMIN_EMAIL)
                .status(MspStatus.ACTIVE.name())
                .build();

        when(aspireUserRepository.findByUserId(userUuid)).thenReturn(Optional.of(aspireUser));
        when(currentContextService.getCurrentUserContext()).thenReturn(new CurrentUserContext());
        when(clientAdminRepository.findAllByMspId(userUuid.toString())).thenReturn(Collections.emptyList());
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSuspendRequestDto request = UserSuspendRequestDto.builder()
                .status("SUSPEND")
                .suspendReason("Policy violation")
                .build();

        mspUserService.suspendMspUser(userUuid.toString(), request);

        assertEquals("SUSPEND", aspireUser.getStatus());
        verify(aspireUserRepository).save(aspireUser);
        verify(userSessionInvalidationHelper).logoutUsersIfRestrictive(eq("SUSPEND"), anyList());
    }

    @Test
    void activateLicense_activatesMspProductsAndInvoice() {
        MspUser mspUser = sampleMspUser();
        mspUser.setStatus(MspStatus.PENDING.name());

        MspProduct pendingProduct = MspProduct.builder()
                .id("product-1")
                .mspId(MSP_ID)
                .licenseStatus("PENDING")
                .build();

        MspInvoice invoice = MspInvoice.builder()
                .id("invoice-1")
                .status(InvoiceStatus.CREATED)
                .createdAt(Instant.now())
                .build();

        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(mspUser));
        when(mspUsersRepository.save(any(MspUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(pendingProduct));
        when(mspInvoiceRepository.findByMspId(MSP_ID)).thenReturn(List.of(invoice));

        mspUserService.activateLicense(MSP_ID);

        assertEquals(MspStatus.ACTIVE.name(), mspUser.getStatus());
        assertEquals("ACTIVE", pendingProduct.getLicenseStatus());
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        verify(mspProductRepository).save(pendingProduct);
        verify(mspInvoiceRepository).save(invoice);
    }

    @Test
    void activateLicense_withInvoiceId_activatesOnlyInvoiceProducts() {
        MspUser mspUser = sampleMspUser();
        MspProduct invoiceProduct = MspProduct.builder()
                .id("product-on-invoice")
                .mspId(MSP_ID)
                .licenseStatus("PENDING")
                .build();
        MspProduct otherProduct = MspProduct.builder()
                .id("product-other")
                .mspId(MSP_ID)
                .licenseStatus("PENDING")
                .build();
        MspInvoice invoice = MspInvoice.builder()
                .id("billing-inv-1")
                .mspId(MSP_ID)
                .clientProductIds(List.of("product-on-invoice"))
                .status(InvoiceStatus.CREATED)
                .createdAt(Instant.now())
                .build();

        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(mspUser));
        when(mspUsersRepository.save(any(MspUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(invoiceProduct, otherProduct));
        when(mspInvoiceRepository.findByIdAndMspId("billing-inv-1", MSP_ID)).thenReturn(List.of(invoice));

        mspUserService.activateLicense(MSP_ID, "billing-inv-1");

        assertEquals("ACTIVE", invoiceProduct.getLicenseStatus());
        assertEquals("PENDING", otherProduct.getLicenseStatus());
        verify(mspProductRepository).save(invoiceProduct);
        verify(mspProductRepository, never()).save(otherProduct);
        verify(mspInvoiceRepository).save(invoice);
    }

    @Test
    void getMspProductCatalog_marksActiveProductsAsEnabled() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(cmsServiceClient.fetchAndCacheAllProducts()).thenReturn(Map.of(
                "prod-1", CmsFullProductResponseDto.builder()
                        .productId("prod-1")
                        .productName("Phishing Simulation")
                        .displayOrder(1)
                        .build(),
                "prod-2", CmsFullProductResponseDto.builder()
                        .productId("prod-2")
                        .productName("Security Awareness")
                        .displayOrder(2)
                        .build()
        ));
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder()
                        .id("msp-prod-1")
                        .productId("prod-1")
                        .packageId("pkg-1")
                        .licenseStatus("ACTIVE")
                        .assignedAt(Instant.now())
                        .expiryDate(Instant.now().plusSeconds(86400))
                        .build()
        ));

        MspProductCatalogResponseDto result =
                mspUserService.getMspProductCatalog(MSP_ID, null, null, 0, 10);

        assertEquals(2, result.getTotalCount());
        assertEquals("ENABLED", result.getItems().get(0).getMspProductStatus());
        assertEquals("DISABLED", result.getItems().get(1).getMspProductStatus());
    }

    @Test
    void getAssignedMspProducts_returnsOnlyAssignedProductsWithAssignedPackages() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(cmsServiceClient.fetchAndCacheAllProducts()).thenReturn(Map.of(
                "prod-1", CmsFullProductResponseDto.builder()
                        .productId("prod-1")
                        .productName("Phishing Simulation")
                        .productStatus("ENABLED")
                        .displayOrder(1)
                        .packages(List.of(
                                CmsPackageDto.builder().id("pkg-1").packageName("Gold").build(),
                                CmsPackageDto.builder().id("pkg-2").packageName("Silver").build()
                        ))
                        .build(),
                "prod-2", CmsFullProductResponseDto.builder()
                        .productId("prod-2")
                        .productName("Security Awareness")
                        .productStatus("ENABLED")
                        .displayOrder(2)
                        .packages(List.of(CmsPackageDto.builder().id("pkg-3").packageName("Basic").build()))
                        .build()
        ));
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder()
                        .id("msp-prod-1")
                        .mspId(MSP_ID)
                        .productId("prod-1")
                        .packageId("pkg-1")
                        .licenseCount(10)
                        .usedLicenseCount(3)
                        .pricePerLicense(25.0)
                        .totalPrice(250.0)
                        .validityPeriod(12)
                        .validityUnit("MONTH")
                        .licenseStatus("ACTIVE")
                        .countryId("US")
                        .assignedAt(Instant.now())
                        .expiryDate(Instant.now().plusSeconds(86400))
                        .build()
        ));

        AllResponseDto<List<MspAssignedProductResponseDto>> result =
                mspUserService.getAssignedMspProducts(MSP_ID, null, null, 0, 10, "displayOrder", "asc");

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("prod-1", result.getItems().get(0).getProductId());
        assertEquals(1, result.getItems().get(0).getPackages().size());
        assertEquals("pkg-1", result.getItems().get(0).getPackages().get(0).getId());
        assertEquals("prod-1", result.getItems().get(0).getPackages().get(0).getProductId());
        assertEquals("msp-prod-1", result.getItems().get(0).getPackages().get(0).getMspProductId());
        assertEquals(10, result.getItems().get(0).getPackages().get(0).getLicenseCount());
        assertEquals(3, result.getItems().get(0).getPackages().get(0).getUsedLicenseCount());
        assertEquals(25.0, result.getItems().get(0).getPackages().get(0).getPricePerLicense());
        assertEquals(250.0, result.getItems().get(0).getPackages().get(0).getTotalPrice());
        assertEquals(12, result.getItems().get(0).getPackages().get(0).getValidityPeriod());
        assertEquals("MONTH", result.getItems().get(0).getPackages().get(0).getValidityUnit());
        assertEquals("ACTIVE", result.getItems().get(0).getPackages().get(0).getLicenseStatus());
        assertEquals("US", result.getItems().get(0).getPackages().get(0).getCountryId());
        assertNotNull(result.getItems().get(0).getPackages().get(0).getAssignedAt());
        assertNotNull(result.getItems().get(0).getPackages().get(0).getExpiryDate());
    }

    @Test
    void getUniqueProductsByMspId_returnsUniqueCmsProductsSortedByDisplayOrder() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder().mspId(MSP_ID).productId("prod-2").packageId("pkg-2").build(),
                MspProduct.builder().mspId(MSP_ID).productId("prod-1").packageId("pkg-1").build(),
                MspProduct.builder().mspId(MSP_ID).productId("prod-1").packageId("pkg-1b").build()
        ));
        when(cmsServiceClient.fetchAndCacheAllProducts()).thenReturn(Map.of(
                "prod-1", CmsFullProductResponseDto.builder()
                        .productId("prod-1")
                        .productName("Phishing Simulation")
                        .thumbnailUrl("http://thumb-1")
                        .displayOrder(2)
                        .build(),
                "prod-2", CmsFullProductResponseDto.builder()
                        .productId("prod-2")
                        .productName("Security Awareness")
                        .thumbnailUrl("http://thumb-2")
                        .displayOrder(1)
                        .build()
        ));

        List<MspProductSimpleResponseDto> result = mspUserService.getUniqueProductsByMspId(MSP_ID);

        assertEquals(2, result.size());
        assertEquals("prod-2", result.get(0).getId());
        assertEquals("Security Awareness", result.get(0).getProductName());
        assertEquals("http://thumb-2", result.get(0).getThumbnailUrl());
        assertEquals(1, result.get(0).getDisplayOrder());
        assertEquals("prod-1", result.get(1).getId());
        assertEquals("Phishing Simulation", result.get(1).getProductName());
    }

    @Test
    void getUniqueProductsByMspId_throwsWhenMspNotFound() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(false);

        assertThrows(RegistrationServiceException.class,
                () -> mspUserService.getUniqueProductsByMspId(MSP_ID));
    }

    @Test
    void getUniqueProductsByMspId_returnsEmptyWhenNoAssignments() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of());

        List<MspProductSimpleResponseDto> result = mspUserService.getUniqueProductsByMspId(MSP_ID);

        assertEquals(0, result.size());
    }

    @Test
    void getPackagesByMspIdAndProductId_returnsUniquePackagesFromCms() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(mspProductRepository.findByMspIdAndProductId(MSP_ID, "prod-1")).thenReturn(List.of(
                MspProduct.builder().mspId(MSP_ID).productId("prod-1").packageId("pkg-1").build(),
                MspProduct.builder().mspId(MSP_ID).productId("prod-1").packageId("pkg-2").build(),
                MspProduct.builder().mspId(MSP_ID).productId("prod-1").packageId("pkg-1").build()
        ));
        when(cmsServiceClient.getPackagesByIds(any())).thenReturn(List.of(
                com.aspire.asat.registration.data.cms.response.CmsPackageSimpleResponseDto.builder()
                        .id("pkg-1").name("Gold").build(),
                com.aspire.asat.registration.data.cms.response.CmsPackageSimpleResponseDto.builder()
                        .id("pkg-2").name("Silver").build()
        ));

        List<MspPackageSimpleResponseDto> result =
                mspUserService.getPackagesByMspIdAndProductId(MSP_ID, "prod-1");

        assertEquals(2, result.size());
        assertEquals("pkg-1", result.get(0).getId());
        assertEquals("Gold", result.get(0).getName());
        assertEquals("pkg-2", result.get(1).getId());
        assertEquals("Silver", result.get(1).getName());
        verify(cmsServiceClient).getPackagesByIds(any());
    }

    @Test
    void getPackagesByMspIdAndProductId_throwsWhenMspNotFound() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(false);

        assertThrows(RegistrationServiceException.class,
                () -> mspUserService.getPackagesByMspIdAndProductId(MSP_ID, "prod-1"));
    }

    @Test
    void getPackagesByMspIdAndProductId_returnsEmptyWhenNoAssignments() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(mspProductRepository.findByMspIdAndProductId(MSP_ID, "prod-1")).thenReturn(List.of());

        List<MspPackageSimpleResponseDto> result =
                mspUserService.getPackagesByMspIdAndProductId(MSP_ID, "prod-1");

        assertEquals(0, result.size());
        verify(cmsServiceClient, never()).getPackagesByIds(any());
    }

    @Test
    void getClientProductTopicsByMspId_extractsPairsAndFetchesFromCms() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder().mspId(MSP_ID).productId("prod-1").packageId("pkg-1").build(),
                MspProduct.builder().mspId(MSP_ID).productId("prod-1").packageId("pkg-1").build(),
                MspProduct.builder().mspId(MSP_ID).productId("prod-2").packageId("pkg-2").build()
        ));
        when(cmsServiceClient.fetchTopicsByProductPackages(any())).thenReturn(
                com.aspire.asat.registration.data.cms.response.CmsTopicPageResponseDto.builder()
                        .offset(0)
                        .pageSize(10)
                        .total(1)
                        .totalLocked(5)
                        .items(List.of(com.aspire.asat.registration.data.cms.response.CmsTopicMinimalDto.builder()
                                .topicId("topic-1")
                                .topicName("Phishing Basics")
                                .build()))
                        .build()
        );

        AllResponseDto<List<com.aspire.asat.registration.data.cms.response.CmsTopicMinimalDto>> result =
                mspUserService.getClientProductTopicsByMspId(MSP_ID,
                        com.aspire.asat.registration.data.mspUser.request.MspClientProductTopicsRequestDto.builder()
                                .offset(0)
                                .pageSize(10)
                                .sortBy("createdAt")
                                .order("desc")
                                .build());

        assertEquals(1L, result.getTotal());
        assertEquals(5L, result.getTotalLocked());
        assertEquals(1, result.getItems().size());
        assertEquals("topic-1", result.getItems().get(0).getTopicId());

        ArgumentCaptor<com.aspire.asat.registration.data.cms.request.CmsTopicsByProductPackagesRequestDto> captor =
                ArgumentCaptor.forClass(com.aspire.asat.registration.data.cms.request.CmsTopicsByProductPackagesRequestDto.class);
        verify(cmsServiceClient, times(1)).fetchTopicsByProductPackages(captor.capture());
        assertEquals(2, captor.getValue().getProductPackages().size());
        assertFalse(Boolean.TRUE.equals(captor.getValue().getExcludeMatchingPairs()));
    }

    @Test
    void getClientProductTopicsByMspId_whenIsAvailableFalse_returnsLockedTopics() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder().mspId(MSP_ID).productId("prod-1").packageId("pkg-1").build()
        ));
        when(cmsServiceClient.fetchTopicsByProductPackages(any())).thenReturn(
                com.aspire.asat.registration.data.cms.response.CmsTopicPageResponseDto.builder()
                        .offset(0).pageSize(10).total(3).totalLocked(2)
                        .items(List.of(com.aspire.asat.registration.data.cms.response.CmsTopicMinimalDto.builder()
                                .topicId("locked-1")
                                .topicName("Locked Topic")
                                .build()))
                        .build()
        );

        AllResponseDto<List<com.aspire.asat.registration.data.cms.response.CmsTopicMinimalDto>> result =
                mspUserService.getClientProductTopicsByMspId(MSP_ID,
                        com.aspire.asat.registration.data.mspUser.request.MspClientProductTopicsRequestDto.builder()
                                .isAvailable(false)
                                .offset(0)
                                .pageSize(10)
                                .build());

        assertEquals(3L, result.getTotal());
        assertEquals(2L, result.getTotalLocked());
        assertEquals(1, result.getItems().size());
        assertEquals("locked-1", result.getItems().get(0).getTopicId());

        ArgumentCaptor<com.aspire.asat.registration.data.cms.request.CmsTopicsByProductPackagesRequestDto> captor =
                ArgumentCaptor.forClass(com.aspire.asat.registration.data.cms.request.CmsTopicsByProductPackagesRequestDto.class);
        verify(cmsServiceClient, times(1)).fetchTopicsByProductPackages(captor.capture());
        assertTrue(Boolean.TRUE.equals(captor.getValue().getExcludeMatchingPairs()));
    }

    @Test
    void getClientProductTopicsByMspId_passesFilterToCms() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder().mspId(MSP_ID).productId("prod-1").packageId("pkg-1").build()
        ));
        when(cmsServiceClient.fetchTopicsByProductPackages(any())).thenReturn(
                com.aspire.asat.registration.data.cms.response.CmsTopicPageResponseDto.builder()
                        .offset(0).pageSize(10).total(0).totalLocked(0).items(List.of()).build()
        );

        var filter = com.aspire.asat.registration.data.cms.request.CmsTopicFilterRequestDto.builder()
                .categoryIds(List.of("cat-1"))
                .status("ENABLED")
                .build();

        mspUserService.getClientProductTopicsByMspId(MSP_ID,
                com.aspire.asat.registration.data.mspUser.request.MspClientProductTopicsRequestDto.builder()
                        .filter(filter)
                        .build());

        ArgumentCaptor<com.aspire.asat.registration.data.cms.request.CmsTopicsByProductPackagesRequestDto> captor =
                ArgumentCaptor.forClass(com.aspire.asat.registration.data.cms.request.CmsTopicsByProductPackagesRequestDto.class);
        verify(cmsServiceClient, times(1)).fetchTopicsByProductPackages(captor.capture());
        assertNotNull(captor.getValue().getFilter());
        assertEquals(List.of("cat-1"), captor.getValue().getFilter().getCategoryIds());
    }

    @Test
    void getClientProductTopicsByMspId_throwsWhenMspNotFound() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(false);

        assertThrows(RegistrationServiceException.class,
                () -> mspUserService.getClientProductTopicsByMspId(MSP_ID, null));
    }

    @Test
    void getMspProductDetailById_returnsDetailsWhenFound() {
        MspProduct mspProduct = MspProduct.builder()
                .id("msp-prod-1")
                .mspId(MSP_ID)
                .productId("prod-1")
                .packageId("pkg-1")
                .licenseCount(10)
                .usedLicenseCount(3)
                .pricePerLicense(25.0)
                .totalPrice(250.0)
                .validityPeriod(12)
                .validityUnit("MONTH")
                .licenseStatus("ACTIVE")
                .countryId("US")
                .assignedAt(Instant.now())
                .expiryDate(Instant.now().plusSeconds(86400))
                .build();

        when(mspProductRepository.findById("msp-prod-1")).thenReturn(Optional.of(mspProduct));
        when(cmsServiceClient.getProductPackageDetails("prod-1", "pkg-1")).thenReturn(
                com.aspire.asat.registration.data.cms.response.CmsProductWithSinglePackageResponseDto.builder()
                        .productId("prod-1")
                        .productName("Phishing Simulation")
                        .productDescription("desc")
                        .productStatus("ENABLED")
                        .thumbnailUrl("http://thumb")
                        .displayOrder(1)
                        .tags(List.of("security"))
                        .packages(CmsPackageDto.builder().id("pkg-1").packageName("Gold").build())
                        .topicCount(2)
                        .build()
        );

        MspProductDetailedDto result = mspUserService.getMspProductDetailById("msp-prod-1");

        assertEquals("msp-prod-1", result.getId());
        assertEquals(MSP_ID, result.getMspId());
        assertEquals("prod-1", result.getProductId());
        assertEquals("pkg-1", result.getPackageId());
        assertEquals(10, result.getLicenseCount());
        assertEquals(3, result.getUsedLicenseCount());
        assertEquals("ACTIVE", result.getLicenseStatus());
        assertEquals("US", result.getCountryId());
        assertNotNull(result.getProduct());
        assertEquals("Phishing Simulation", result.getProduct().getProductName());
        assertNotNull(result.getPackageDetails());
        assertEquals("Gold", result.getPackageDetails().getPackageName());
        assertEquals(2, result.getTopicCount());
    }

    @Test
    void getMspProductDetailById_throwsWhenNotFound() {
        when(mspProductRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class,
                () -> mspUserService.getMspProductDetailById("missing-id"));
    }

    @Test
    void processMspBuyNow_throwsWhenMspNotFound() {
        MspBuyNowRequestDto request = MspBuyNowRequestDto.builder()
                .mspId(MSP_ID)
                .productSelections(List.of(ProductSelectionDto.builder()
                        .productId("prod-1")
                        .packageId("pkg-1")
                        .licenseCount(1)
                        .pricePerLicense(10.0)
                        .validityPeriod(1)
                        .validityUnit(ValidityUnit.MONTH)
                        .productName("Product")
                        .packageName("Gold")
                        .build()))
                .invoice(InvoiceDetailsDto.builder().subtotal(10.0).totalAmount(10.0).build())
                .build();

        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class, () -> mspUserService.processMspBuyNow(request));
    }

    @Test
    void processMspBuyNow_throwsWhenProductAlreadyActive() {
        MspBuyNowRequestDto request = MspBuyNowRequestDto.builder()
                .mspId(MSP_ID)
                .productSelections(List.of(ProductSelectionDto.builder()
                        .productId("prod-1")
                        .packageId("pkg-1")
                        .licenseCount(1)
                        .pricePerLicense(10.0)
                        .validityPeriod(1)
                        .validityUnit(ValidityUnit.MONTH)
                        .productName("Phishing Simulation")
                        .packageName("Gold")
                        .build()))
                .invoice(InvoiceDetailsDto.builder().subtotal(10.0).totalAmount(10.0).build())
                .build();

        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(sampleMspUser()));
        when(mspProductRepository.findByMspIdAndProductId(MSP_ID, "prod-1")).thenReturn(List.of(
                MspProduct.builder()
                        .productId("prod-1")
                        .licenseStatus("ACTIVE")
                        .expiryDate(Instant.now().plusSeconds(86400))
                        .build()
        ));

        assertThrows(RegistationValidationException.class, () -> mspUserService.processMspBuyNow(request));
    }

    @Test
    void processMspBuyNow_appliesCouponDiscountOnInvoice() throws Exception {
        ProductSelectionDto selection = ProductSelectionDto.builder()
                .productId("prod-1")
                .packageId("pkg-1")
                .licenseCount(1)
                .pricePerLicense(100.0)
                .validityPeriod(1)
                .validityUnit(ValidityUnit.MONTH)
                .productName("Product")
                .packageName("Gold")
                .build();

        MspBuyNowRequestDto request = MspBuyNowRequestDto.builder()
                .mspId(MSP_ID)
                .productSelections(List.of(selection))
                .invoice(InvoiceDetailsDto.builder()
                        .subtotal(100.0)
                        .totalAmount(100.0)
                        .couponCode("SUMMER10")
                        .build())
                .build();

        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(sampleMspUser()));
        when(mspProductRepository.findByMspIdAndProductId(MSP_ID, "prod-1")).thenReturn(List.of());
        when(mspProductRepository.save(any(MspProduct.class))).thenAnswer(invocation -> {
            MspProduct product = invocation.getArgument(0);
            product.setId("msp-product-new");
            return product;
        });
        when(mspUsersRepository.save(any(MspUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(billingService.getCouponByCode("SUMMER10")).thenReturn(
                CouponResponseDTO.builder()
                        .id("coupon-1")
                        .code("SUMMER10")
                        .name("Summer Sale")
                        .type("PERCENTAGE")
                        .value(10.0)
                        .build());

        stubVatBillingService();
        setField(mspUserService, "s3BucketName", "test-bucket");

        InvoiceResponseDTO invoiceResponse = new InvoiceResponseDTO();
        invoiceResponse.setId("invoice-id");
        invoiceResponse.setSubtotal(100.0);
        invoiceResponse.setDiscountAmount(0.0);
        invoiceResponse.setVatAmount(0.0);
        invoiceResponse.setTotalAmount(90.0);
        invoiceResponse.setStatus(InvoiceStatus.PENDING);
        when(invoiceService.createInvoiceForMsp(any())).thenReturn(invoiceResponse);
        when(mspInvoiceRepository.save(any(MspInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CurrentUserContext context = new CurrentUserContext();
        context.setUserId("SYSTEM");
        when(currentContextService.getCurrentUserContext()).thenReturn(context);

        when(invoiceGenerator.generateInvoicePdf(
                anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), any(Instant.class), anyList(), any(), any()))
                .thenReturn(new ByteArrayOutputStream());
        when(fileService.fileUpload(anyString(), anyString()))
                .thenReturn(FileUploadResponse.builder().path("invoices/invoice-id_payment.pdf").build());

        mspUserService.processMspBuyNow(request);

        ArgumentCaptor<InvoiceRequestDTO> invoiceCaptor = ArgumentCaptor.forClass(InvoiceRequestDTO.class);
        verify(invoiceService).createInvoiceForMsp(invoiceCaptor.capture());
        InvoiceRequestDTO captured = invoiceCaptor.getValue();
        assertEquals("SUMMER10", captured.getCouponCode());
        assertEquals(10.0, captured.getCouponDiscountAmount());
        assertEquals(90.0, captured.getTotalAmount());
    }

    @Test
    void countAssignedClientsForMsp_returnsClientCount() {
        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(sampleMspUser()));
        when(clientAdminRepository.countByMspId(MSP_ID)).thenReturn(2L);

        long count = mspUserService.countAssignedClientsForMsp(MSP_ID);

        assertEquals(2, count);
    }

    @Test
    void getAssignedClientsForMsp_returnsPaginatedClients() {
        ClientAdmin clientAdmin = new ClientAdmin();
        clientAdmin.setId("client-1");
        clientAdmin.setOrganizationName("Client Org");
        clientAdmin.setEmail("client@example.com");

        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(sampleMspUser()));
        when(clientAdminRepository.findAllByMspId(MSP_ID)).thenReturn(List.of(clientAdmin));
        when(clientProductRepository.findByClientAdminId("client-1")).thenReturn(List.of(
                ClientProduct.builder().licenseCount(5).build()
        ));

        List<AssignedClientDto> result = mspUserService.getAssignedClientsForMsp(MSP_ID, 0, 10);

        assertEquals(1, result.size());
        assertEquals("Client Org", result.get(0).getClientName());
        assertEquals(5, result.get(0).getLicenseCount());
    }

    @Test
    void getMspListWithLicenses_returnsAggregatedLicenseCounts() {
        MspUser mspUser = sampleMspUser();
        Page<MspUser> page = new PageImpl<>(List.of(mspUser));
        MspProduct product = MspProduct.builder()
                .mspId(MSP_ID)
                .licenseCount(20)
                .usedLicenseCount(8)
                .build();

        when(mspUsersRepositoryCustom.findMspUsersWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(mspProductRepository.findByMspIdIn(List.of(MSP_ID))).thenReturn(List.of(product));

        AllResponseDto<List<MspListWithLicenseResponseDto>> result =
                mspUserService.getMspListWithLicenses(0, 10, null, null, null, null);

        assertEquals(1, result.getItems().size());
        assertEquals(20, result.getItems().get(0).getLicenseCount());
        assertEquals(8, result.getItems().get(0).getUsedLicenseCount());
    }

    @Test
    void getActiveLicenseSummary_returnsTotalsForActiveProducts() {
        when(mspProductRepository.findAll()).thenReturn(List.of(
                MspProduct.builder().licenseStatus("ACTIVE").licenseCount(10).usedLicenseCount(4).build(),
                MspProduct.builder().licenseStatus("PENDING").licenseCount(5).usedLicenseCount(1).build(),
                MspProduct.builder().licenseStatus("ACTIVE").licenseCount(6).usedLicenseCount(2).build()
        ));

        MspActiveLicenseSummaryResponseDto result = mspUserService.getActiveLicenseSummary();

        assertEquals(16, result.getLicenseCount());
        assertEquals(6, result.getUsedLicenseCount());
    }

    @Test
    void getMspDashboard_returnsAggregatedTotals() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder().productId("p1").packageId("pkg1").licenseCount(10).build(),
                MspProduct.builder().productId("p1").packageId("pkg2").licenseCount(20).build(),
                MspProduct.builder().productId("p2").packageId("pkg1").licenseCount(5).build()
        ));
        when(clientAdminRepository.countByMspId(MSP_ID)).thenReturn(3L);

        MspDashboardResponseDto result = mspUserService.getMspDashboard(MSP_ID);

        assertEquals(2, result.getTotalProducts());
        assertEquals(2, result.getTotalPackages());
        assertEquals(35, result.getTotalLicenses());
        assertEquals(3, result.getTotalClients());
    }

    @Test
    void getMspDashboard_throwsWhenMspNotFound() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(false);

        assertThrows(RegistrationServiceException.class, () -> mspUserService.getMspDashboard(MSP_ID));
    }

    @Test
    void getMspTopicCounts_buildsPairsAndReturnsCmsMonthlyDistribution() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(true);
        when(mspProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                MspProduct.builder().productId("p1").packageId("pkg1").build(),
                MspProduct.builder().productId("p1").packageId("pkg1").build(),
                MspProduct.builder().productId("p2").packageId("pkg2").build()
        ));
        when(clientProductRepository.findByMspId(MSP_ID)).thenReturn(List.of(
                ClientProduct.builder().productId("p1").packageId("pkg1").build(),
                ClientProduct.builder().productId("p3").packageId("pkg3").build()
        ));
        when(cmsServiceClient.countTopicsByProductPackages(any())).thenReturn(
                com.aspire.asat.registration.data.cms.response.CmsTopicCountsByProductPackagesResponseDto.builder()
                        .data(List.of(
                                com.aspire.asat.registration.data.cms.response.CmsTopicDistributionItemDto.builder()
                                        .month("Apr")
                                        .totalContent(33L)
                                        .usedContent(32L)
                                        .build()
                        ))
                        .build());

        MspTopicCountsResponseDto result = mspUserService.getMspTopicCounts(MSP_ID);

        assertEquals(1, result.getData().size());
        assertEquals("Apr", result.getData().get(0).getMonth());
        assertEquals(33L, result.getData().get(0).getTotalContent());
        assertEquals(32L, result.getData().get(0).getUsedContent());
        verify(cmsServiceClient).countTopicsByProductPackages(any());
    }

    @Test
    void getMspTopicCounts_throwsWhenMspNotFound() {
        when(mspUsersRepository.existsById(MSP_ID)).thenReturn(false);

        assertThrows(RegistrationServiceException.class, () -> mspUserService.getMspTopicCounts(MSP_ID));
    }

    private void stubSuccessfulOnboardingDependencies() {
        when(mspUsersRepository.existsByMspAdminEmailIgnoreCase(ADMIN_EMAIL)).thenReturn(false);
        when(aspireUserService.getUserByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");

        CurrentUserContext context = new CurrentUserContext();
        context.setUserId("SYSTEM");
        lenient().when(currentContextService.getCurrentUserContext()).thenReturn(context);

        when(mspProductRepository.save(any(MspProduct.class))).thenAnswer(invocation -> {
            MspProduct product = invocation.getArgument(0);
            product.setId("msp-product-id");
            return product;
        });
        when(mspUsersRepository.save(any(MspUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(aspireUserRepository.existsByUserId(any(UUID.class))).thenReturn(false);
        when(aspireUserRepository.existsByUsernameIgnoreCase(ADMIN_EMAIL)).thenReturn(false);
        when(clientAdminRepository.findById(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(UserType.MSP.name())).thenReturn(Role.builder().id("role-id").build());
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceResponseDTO invoiceResponse = new InvoiceResponseDTO();
        invoiceResponse.setId("invoice-id");
        when(invoiceService.createInvoiceForMsp(any())).thenReturn(invoiceResponse);
        when(mspInvoiceRepository.save(any(MspInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(invoiceGenerator.generateInvoicePdf(anyString(), anyString(), anyDouble(), any(Date.class)))
                .thenReturn(null);
        lenient().doNothing().when(notificationServiceClient)
                .sendWelcomeEmailNotification(anyString(), anyString(), anyString(), anyString(), anyString());
        lenient().doNothing().when(notificationServiceClient)
                .sendWelcomeEmailNotification(anyString(), anyString(), anyString(), anyString(), anyString(), anyList());

        stubVatBillingService();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubVatBillingService() {
        setField(mspUserService, "billingServiceUrl", "http://billing");

        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        dataNode.put("defaultVatRate", 0.0);
        dataNode.put("regionBased", false);
        ObjectNode responseJson = JsonNodeFactory.instance.objectNode();
        responseJson.set("data", dataNode);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(responseJson));
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set field: " + fieldName, e);
        }
    }

    private MspOnboardingRequestDto buildOnboardingRequest(String industry, String subIndustry) {
        OrganizationInfoForMspDto organization = OrganizationInfoForMspDto.builder()
                .organizationName("MSP Org")
                .contactEmail("contact@msp.com")
                .phoneNumber("+1234567890")
                .country("United States")
                .stateProvince("California")
                .timeZone("America/Los_Angeles")
                .language("English")
                .industry(industry)
                .subIndustry(subIndustry)
                .domain("msp.com")
                .organizationSize("Medium")
                .streetAddress("123 Main St")
                .city("Los Angeles")
                .mspAdminEmail(ADMIN_EMAIL)
                .build();

        BillingInfoForMspDto billing = BillingInfoForMspDto.builder()
                .billingEmail("billing@msp.com")
                .billingName("Billing Contact")
                .build();

        ProductSelectionDto productSelection = ProductSelectionDto.builder()
                .productId("product-id")
                .packageId("package-id")
                .licenseCount(10)
                .pricePerLicense(100.0)
                .validityPeriod(12)
                .validityUnit(ValidityUnit.MONTH)
                .build();

        InvoiceDetailsDto invoice = InvoiceDetailsDto.builder()
                .subtotal(1000.0)
                .totalAmount(1000.0)
                .vatAmount(0.0)
                .discountAmount(0.0)
                .discountPercentage(0.0)
                .build();

        return MspOnboardingRequestDto.builder()
                .organization(organization)
                .billing(billing)
                .productSelections(List.of(productSelection))
                .invoice(invoice)
                .build();
    }

    private MspUser sampleMspUser() {
        return MspUser.builder()
                .id(MSP_ID)
                .mspId("MSP-00000001")
                .organizationName("MSP Org")
                .contactEmail("contact@msp.com")
                .mspAdminEmail(ADMIN_EMAIL)
                .phoneNumber("+1234567890")
                .country("United States")
                .stateProvince("California")
                .status(MspStatus.PENDING.name())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
