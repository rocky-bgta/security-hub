package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.common.service.files.FileService;
import com.aspire.asat.common.util.InvoiceGenerator;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.client.service.PhishingServiceClient;
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
import com.aspire.asat.registration.data.clientAdmin.response.ClientAdminListResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientAdminWithProductsResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductsPaginatedResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.EmailTemplateValidationResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.LicenseOverviewSummaryDto;
import com.aspire.asat.registration.data.clientAdmin.response.LicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.OrganizationLicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsFullProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import com.aspire.asat.registration.data.cms.response.CmsProductWithSinglePackageResponseDto;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.data.phishing.response.CampaignLicenseUsageDto;
import com.aspire.asat.registration.exception.ClientActivationException;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.ClientProduct;
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
import com.aspire.asat.registration.repository.dropdown.LanguageRepository;
import com.aspire.asat.registration.repository.dropdown.OrganizationSizeRepository;
import com.aspire.asat.registration.repository.dropdown.StateRepository;
import com.aspire.asat.registration.repository.dropdown.SubIndustryRepository;
import com.aspire.asat.registration.repository.dropdown.TimezoneRepository;
import com.aspire.asat.registration.repository.metadata.OrganizationTypeRepository;
import com.aspire.asat.registration.repository.msp.MspProductRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.ActivityLogService;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.dropdown.SuspendReasonService;
import com.aspire.asat.registration.service.pricing.ProductPriceResolver;
import com.aspire.asat.registration.service.support.SimulationProductResolver;
import com.aspire.asat.registration.service.support.UserSessionInvalidationHelper;
import com.aspire.asat.registration.service.external.BillingService;
import com.aspire.asat.registration.service.external.InvoiceService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientAdminServiceImplTest {

    private static final String CLIENT_ADMIN_ID = "b1c2d3e4-f5a6-7890-abcd-ef1234567890";
    private static final String MSP_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String ADMIN_EMAIL = "admin@client.com";
    private static final String PRODUCT_ID = "product-id-1";
    private static final String PACKAGE_ID = "package-id-1";
    private static final String CLIENT_PRODUCT_ID = "client-product-id-1";
    private static final UUID END_USER_ID = UUID.fromString("c1c2c3d4-e5f6-7890-abcd-ef1234567891");

    @Mock private ClientAdminRepository clientAdminRepository;
    @Mock private ClientProductRepository clientProductRepository;
    @Mock private UserLicenceRepository userLicenceRepository;
    @Mock private AspireUserRepository aspireUserRepository;
    @Mock private ClientAdminRepositoryCustom clientAdminRepositoryCustom;
    @Mock private ClientProductRepositoryCustom clientProductRepositoryCustom;
    @Mock private CountryRepository countryRepository;
    @Mock private StateRepository stateRepository;
    @Mock private TimezoneRepository timezoneRepository;
    @Mock private LanguageRepository languageRepository;
    @Mock private IndustryRepository industryRepository;
    @Mock private SubIndustryRepository subIndustryRepository;
    @Mock private OrganizationSizeRepository organizationSizeRepository;
    @Mock private OrganizationTypeRepository organizationTypeRepository;
    @Mock private WebClient webClient;
    @Mock private ObjectMapper objectMapper;
    @Mock private FileService fileService;
    @Mock private RegistrationNotificationClient notificationServiceClient;
    @Mock private AspireUserService aspireUserService;
    @Mock private CmsServiceClient cmsServiceClient;
    @Mock private MspUsersRepository mspUsersRepository;
    @Mock private MspProductRepository mspProductRepository;
    @Mock private UserCurrentContextService userCurrentContextService;
    @Mock private ActivityLogService activityLogService;
    @Mock private HttpServletRequest httpServletRequest;
    @Mock private UserLoginHistoryRepository userLoginHistoryRepository;
    @Mock private InvoiceGenerator invoiceGenerator;
    @Mock private BillingService billingService;
    @Mock private InvoiceService invoiceService;
    @Mock private UserSuspendReasonRepository userSuspendReasonRepository;
    @Mock private SuspendReasonService suspendReasonService;
    @Mock private UserSessionInvalidationHelper userSessionInvalidationHelper;
    @Mock private SimulationProductResolver simulationProductResolver;
    @Mock private PhishingServiceClient phishingServiceClient;

    private ProductPriceResolver productPriceResolver;

    @InjectMocks
    private ClientAdminServiceImpl clientAdminService;

    @BeforeEach
    void setUp() {
        productPriceResolver = new ProductPriceResolver(cmsServiceClient);
        setField(clientAdminService, "productPriceResolver", productPriceResolver);
        setField(clientAdminService, "cmsServiceUrl", "http://cms");
        setField(clientAdminService, "billingServiceUrl", "http://billing");
        setField(clientAdminService, "logInUrl", "http://login");
        lenient().when(userCurrentContextService.getCurrentUserContext()).thenReturn(adminUserContext());
        lenient().when(simulationProductResolver.isSimulationProduct(anyString())).thenReturn(false);
        lenient().when(simulationProductResolver.getSimulationProductIds()).thenReturn(Set.of());
        lenient().when(clientProductRepository.findByClientAdminIdAndLicenseStatus(anyString(), eq("ACTIVE")))
                .thenReturn(Collections.emptyList());
        stubFlatPackagePrice(PRODUCT_ID, PACKAGE_ID, 100.0);
    }

    private void stubFlatPackagePrice(String productId, String packageId, double price) {
        CmsPackageDto cmsPackage = CmsPackageDto.builder().id(packageId).price(price).build();
        CmsFullProductResponseDto product = CmsFullProductResponseDto.builder()
                .productId(productId)
                .packages(List.of(cmsPackage))
                .build();
        lenient().when(cmsServiceClient.getFullProductFromCache(productId)).thenReturn(product);
    }

    // --- validateTemplate ---

    @Test
    void validateTemplate_returnsValidWhenAllPlaceholdersPresent() {
        String template = "{{USERNAME}} {{TEMP_PASSWORD}} {{INVOICE_ID}} {{CLIENT_NAME}} {{PORTAL_LINK}} {{PAYMENT_METHOD}}";

        EmailTemplateValidationResponseDto result = clientAdminService.validateTemplate(template);

        assertTrue(result.isValid());
        assertTrue(result.getMissingPlaceholders().isEmpty());
    }

    @Test
    void validateTemplate_returnsInvalidWhenPlaceholdersMissing() {
        EmailTemplateValidationResponseDto result = clientAdminService.validateTemplate("Hello {{USERNAME}}");

        assertFalse(result.isValid());
        assertFalse(result.getMissingPlaceholders().isEmpty());
    }

    // --- doesUsernameExist ---

    @Test
    void doesUsernameExist_returnsTrueWhenEmailExists() {
        when(clientAdminRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)).thenReturn(true);

        assertTrue(clientAdminService.doesUsernameExist(ADMIN_EMAIL));
    }

    @Test
    void doesUsernameExist_returnsFalseWhenEmailDoesNotExist() {
        when(clientAdminRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)).thenReturn(false);

        assertFalse(clientAdminService.doesUsernameExist(ADMIN_EMAIL));
    }

    // --- processClientOnboarding ---

    @Test
    void processClientOnboarding_throwsWhenRequestIsNull() {
        assertThrows(RegistationValidationException.class, () -> clientAdminService.processClientOnboarding(null));
    }

    @Test
    void processClientOnboarding_throwsWhenMspIdMissing() {
        ClientOnboardingRequestDto request = buildOnboardingRequest();
        request.setMspId(null);

        assertThrows(RegistationValidationException.class, () -> clientAdminService.processClientOnboarding(request));
    }

    @Test
    void processClientOnboarding_throwsWhenEmailAlreadyExists() {
        ClientOnboardingRequestDto request = buildOnboardingRequest();
        when(clientAdminRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> clientAdminService.processClientOnboarding(request));
    }

    // --- processBuyNow ---

    @Test
    void processBuyNow_throwsWhenRequestIsNull() {
        assertThrows(RegistationValidationException.class, () -> clientAdminService.processBuyNow(null));
    }

    @Test
    void processBuyNow_throwsWhenClientAdminNotFound() {
        BuyNowRequestDto request = buildBuyNowRequest();
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class, () -> clientAdminService.processBuyNow(request));
    }

    // --- activateLicense ---

    @Test
    void activateLicense_throwsWhenClientAdminNotFound() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(ClientActivationException.class, () -> clientAdminService.activateLicense(CLIENT_ADMIN_ID));
    }

    @Test
    void activateLicense_activatesPendingProductsAndUpdatesTrialFlag() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setStatus(AdminStatus.PENDING);
        ClientProduct pendingProduct = sampleClientProduct("PENDING");
        AspireUser aspireUser = sampleAspireUser(true);

        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clientProductRepository.findByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(List.of(pendingProduct));
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clientProductRepositoryCustom.getUniqueProductCountByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(1);
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, null))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(10, 3));
        when(aspireUserRepository.findByUserId(UUID.fromString(CLIENT_ADMIN_ID))).thenReturn(Optional.of(aspireUser));
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(inv -> inv.getArgument(0));
        stubCmsDashboardWebClient();

        clientAdminService.activateLicense(CLIENT_ADMIN_ID);

        assertEquals(AdminStatus.ACTIVE, clientAdmin.getStatus());
        assertEquals("ACTIVE", pendingProduct.getLicenseStatus());
        assertFalse(aspireUser.getIsTrial());
        verify(aspireUserService).updateAspireUser(eq(CLIENT_ADMIN_ID), eq(UserType.CLIENT_ADMIN), any(ClientAdmin.class));
    }

    // --- deactivateLicense ---

    @Test
    void deactivateLicense_setsPendingAdminInactiveAndProductsInactive() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setStatus(AdminStatus.PENDING);
        ClientProduct pendingProduct = sampleClientProduct("PENDING");
        AspireUser aspireUser = sampleAspireUser(false);
        aspireUser.setStatus("PENDING");

        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clientProductRepository.findById(CLIENT_PRODUCT_ID)).thenReturn(Optional.of(pendingProduct));
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aspireUserRepository.findByUserId(UUID.fromString(CLIENT_ADMIN_ID))).thenReturn(Optional.of(aspireUser));
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.deactivateLicense(CLIENT_ADMIN_ID, List.of(CLIENT_PRODUCT_ID));

        assertEquals(AdminStatus.INACTIVE, clientAdmin.getStatus());
        assertEquals("INACTIVE", pendingProduct.getLicenseStatus());
        assertEquals(AdminStatus.INACTIVE.name(), aspireUser.getStatus());
        verify(userSessionInvalidationHelper).logoutUsersIfRestrictive(eq(AdminStatus.INACTIVE.name()), anyList());
    }

    @Test
    void deactivateLicense_leavesActiveAdminStatusUnchanged() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setStatus(AdminStatus.ACTIVE);
        ClientProduct pendingProduct = sampleClientProduct("PENDING");

        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientProductRepository.findById(CLIENT_PRODUCT_ID)).thenReturn(Optional.of(pendingProduct));
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.deactivateLicense(CLIENT_ADMIN_ID, List.of(CLIENT_PRODUCT_ID));

        assertEquals(AdminStatus.ACTIVE, clientAdmin.getStatus());
        assertEquals("INACTIVE", pendingProduct.getLicenseStatus());
        verify(clientAdminRepository, never()).save(any(ClientAdmin.class));
        verify(aspireUserRepository, never()).save(any(AspireUser.class));
    }

    // --- expireLicenseDueToUnpaidInvoice ---

    @Test
    void expireLicenseDueToUnpaidInvoice_setsProductsExpiredAndPendingAdminInactiveWithCascade() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setStatus(AdminStatus.PENDING);
        ClientProduct pendingProduct = sampleClientProduct("PENDING");
        AspireUser aspireUser = sampleAspireUser(false);
        aspireUser.setStatus("PENDING");
        AspireUser activeEndUser = AspireUser.builder()
                .id(END_USER_ID)
                .userId(END_USER_ID)
                .status("ACTIVE")
                .build();

        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clientProductRepository.findById(CLIENT_PRODUCT_ID)).thenReturn(Optional.of(pendingProduct));
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aspireUserRepository.findByUserId(UUID.fromString(CLIENT_ADMIN_ID))).thenReturn(Optional.of(aspireUser));
        when(aspireUserRepository.findByClientAdminIdAndStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(activeEndUser));
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.expireLicenseDueToUnpaidInvoice(CLIENT_ADMIN_ID, List.of(CLIENT_PRODUCT_ID));

        assertEquals("EXPIRED", pendingProduct.getLicenseStatus());
        assertEquals(AdminStatus.INACTIVE, clientAdmin.getStatus());
        assertEquals("INACTIVE", activeEndUser.getStatus());
        assertTrue(activeEndUser.getIsAdminInactive());
        verify(userSessionInvalidationHelper).logoutUsersIfRestrictive(eq("INACTIVE"), anyList());
    }

    @Test
    void expireLicenseDueToUnpaidInvoice_leavesActiveAdminUnchanged() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setStatus(AdminStatus.ACTIVE);
        ClientProduct activeProduct = sampleClientProduct("ACTIVE");

        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientProductRepository.findById(CLIENT_PRODUCT_ID)).thenReturn(Optional.of(activeProduct));
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.expireLicenseDueToUnpaidInvoice(CLIENT_ADMIN_ID, List.of(CLIENT_PRODUCT_ID));

        assertEquals(AdminStatus.ACTIVE, clientAdmin.getStatus());
        assertEquals("EXPIRED", activeProduct.getLicenseStatus());
        verify(clientAdminRepository, never()).save(any(ClientAdmin.class));
        verify(aspireUserRepository, never()).save(any(AspireUser.class));
    }

    // --- findProductIdsByClientAdminId ---

    @Test
    void findProductIdsByClientAdminId_returnsActiveProductIds() {
        ClientProduct product = sampleClientProduct("ACTIVE");
        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(product));

        List<String> result = clientAdminService.findProductIdsByClientAdminId(CLIENT_ADMIN_ID);

        assertEquals(1, result.size());
        assertEquals(PRODUCT_ID, result.get(0));
    }

    // --- findEndUsersByClientAdminId ---

    @Test
    void findEndUsersByClientAdminId_returnsClientAdminIds() {
        ClientAdmin client = sampleClientAdmin();
        when(clientAdminRepository.findIdsOnlyByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(List.of(client));

        List<String> result = clientAdminService.findEndUsersByClientAdminId(CLIENT_ADMIN_ID);

        assertEquals(1, result.size());
        assertEquals(CLIENT_ADMIN_ID, result.get(0));
    }

    // --- findAllClientProductsByClientAdminId ---

    @Test
    void findAllClientProductsByClientAdminId_returnsAllActiveProducts() {
        ClientProduct product = sampleClientProduct("ACTIVE");
        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(product));

        assertEquals(1, clientAdminService.findAllClientProductsByClientAdminId(CLIENT_ADMIN_ID, null).size());
    }

    @Test
    void findAllClientProductsByClientAdminId_filtersByProductId() {
        ClientProduct product = sampleClientProduct("ACTIVE");
        when(clientProductRepository.findByClientAdminIdAndProductIdAndLicenseStatus(CLIENT_ADMIN_ID, PRODUCT_ID, "ACTIVE"))
                .thenReturn(List.of(product));

        assertEquals(1, clientAdminService.findAllClientProductsByClientAdminId(CLIENT_ADMIN_ID, PRODUCT_ID).size());
    }

    // --- updateUsedLicenseCount ---

    @Test
    void updateUsedLicenseCount_updatesActiveProduct() {
        ClientProduct product = sampleClientProduct("ACTIVE");
        when(clientProductRepository.findByClientAdminIdAndProductIdAndLicenseStatus(CLIENT_ADMIN_ID, PRODUCT_ID, "ACTIVE"))
                .thenReturn(List.of(product));
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.updateUsedLicenseCount(CLIENT_ADMIN_ID, PRODUCT_ID, 5);

        assertEquals(5, product.getUsedLicenseCount());
        verify(clientProductRepository).save(product);
    }

    @Test
    void updateUsedLicenseCount_throwsWhenActiveProductNotFound() {
        when(clientProductRepository.findByClientAdminIdAndProductIdAndLicenseStatus(CLIENT_ADMIN_ID, PRODUCT_ID, "ACTIVE"))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> clientAdminService.updateUsedLicenseCount(CLIENT_ADMIN_ID, PRODUCT_ID, 5));
    }

    // --- updateUsedLicenseCountByClientProductId ---

    @Test
    void updateUsedLicenseCountByClientProductId_updatesActiveProduct() {
        ClientProduct product = sampleClientProduct("ACTIVE");
        when(clientProductRepository.findById(CLIENT_PRODUCT_ID)).thenReturn(Optional.of(product));
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.updateUsedLicenseCountByClientProductId(CLIENT_PRODUCT_ID, 10);

        assertEquals(10, product.getUsedLicenseCount());
        verify(clientProductRepository).save(product);
    }

    @Test
    void updateUsedLicenseCountByClientProductId_throwsWhenNotFound() {
        when(clientProductRepository.findById(CLIENT_PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> clientAdminService.updateUsedLicenseCountByClientProductId(CLIENT_PRODUCT_ID, 10));
    }

    @Test
    void updateUsedLicenseCountByClientProductId_throwsWhenNotActive() {
        ClientProduct product = sampleClientProduct("PENDING");
        when(clientProductRepository.findById(CLIENT_PRODUCT_ID)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class,
                () -> clientAdminService.updateUsedLicenseCountByClientProductId(CLIENT_PRODUCT_ID, 10));
        verify(clientProductRepository, never()).save(any());
    }

    // --- reassignProductsToClient ---

    @Test
    void reassignProductsToClient_throwsWhenRequestIsNull() {
        assertThrows(RegistationValidationException.class, () -> clientAdminService.reassignProductsToClient(null));
    }

    @Test
    void reassignProductsToClient_throwsWhenClientAdminNotFound() {
        ClientProductAssignment request = buildReassignmentRequest();
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class, () -> clientAdminService.reassignProductsToClient(request));
    }

    @Test
    void reassignProductsToClient_throwsWhenMspIdMismatch() {
        ClientProductAssignment request = buildReassignmentRequest();
        request.setMspId("other-msp-id");
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setMspId(MSP_ID);
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));

        assertThrows(RegistrationServiceException.class, () -> clientAdminService.reassignProductsToClient(request));
    }

    @Test
    void reassignProductsToClient_throwsWhenMspUserProvidesWrongMspId() {
        ClientProductAssignment request = buildReassignmentRequest();
        request.setMspId("other-msp-id");
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setMspId(MSP_ID);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspUserContext());
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));

        RegistrationServiceException ex = assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.reassignProductsToClient(request));
        assertTrue(ex.getMessage().contains("logged-in MSP user"));
    }

    @Test
    void reassignProductsToClient_throwsWhenInsufficientMspLicenses() {
        ClientProductAssignment request = buildReassignmentRequest();
        request.setMspId(MSP_ID);
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setMspId(MSP_ID);
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));

        MspProduct mspProduct = MspProduct.builder()
                .mspId(MSP_ID)
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .licenseCount(10)
                .usedLicenseCount(8)
                .licenseStatus("ACTIVE")
                .build();
        when(mspProductRepository.findByMspIdAndProductIdAndPackageId(MSP_ID, PRODUCT_ID, PACKAGE_ID))
                .thenReturn(mspProduct);

        RegistationValidationException ex = assertThrows(RegistationValidationException.class,
                () -> clientAdminService.reassignProductsToClient(request));
        assertTrue(ex.getMessage().contains("Insufficient MSP licenses"));
    }

    @Test
    void reassignProductsToClient_throwsWhenMspProductNotAssigned() {
        ClientProductAssignment request = buildReassignmentRequest();
        request.setMspId(MSP_ID);
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setMspId(MSP_ID);
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(mspProductRepository.findByMspIdAndProductIdAndPackageId(MSP_ID, PRODUCT_ID, PACKAGE_ID))
                .thenReturn(null);

        RegistationValidationException ex = assertThrows(RegistationValidationException.class,
                () -> clientAdminService.reassignProductsToClient(request));
        assertTrue(ex.getMessage().contains("does not have the selected product/package"));
    }

    // --- listClientAdminsWithProducts ---

    @Test
    void listClientAdminsWithProducts_filtersByMspIdForMspUser() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspUserContext());
        ClientAdmin clientAdmin = sampleClientAdmin();
        Page<ClientAdmin> page = new PageImpl<>(List.of(clientAdmin));
        when(clientAdminRepositoryCustom.findClientAdminsWithFilters(any(), eq(MSP_ID), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(clientProductRepository.findByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(Collections.emptyList());

        ClientAdminListResponseDto result = clientAdminService.listClientAdminsWithProducts(new ClientAdminListRequestDto());

        assertEquals(1, result.getClientAdmins().size());
        assertEquals(clientAdmin.getOrganizationName(), result.getClientAdmins().get(0).getOrganizationName());
    }

    @Test
    void listClientAdminsWithProducts_usesRequestMspIdForNonMspUser() {
        ClientAdminListRequestDto requestDto = new ClientAdminListRequestDto();
        requestDto.setMspId("request-msp-id");
        Page<ClientAdmin> page = new PageImpl<>(Collections.emptyList());
        when(clientAdminRepositoryCustom.findClientAdminsWithFilters(any(), eq("request-msp-id"), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        ClientAdminListResponseDto result = clientAdminService.listClientAdminsWithProducts(requestDto);

        assertNotNull(result);
        assertTrue(result.getClientAdmins().isEmpty());
    }

    // --- getClientAdminDetailedById ---

    @Test
    void getClientAdminDetailedById_returnsDetailsWhenFound() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientProductRepository.findByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(Collections.emptyList());

        assertNotNull(clientAdminService.getClientAdminDetailedById(CLIENT_ADMIN_ID));
    }

    @Test
    void getClientAdminDetailedById_throwsWhenNotFound() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getClientAdminDetailedById(CLIENT_ADMIN_ID));
    }

    // --- updateClientAdminById ---

    @Test
    void updateClientAdminById_updatesFieldsWhenFound() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminUserContext());
        ClientAdmin clientAdmin = sampleClientAdmin();
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        java.util.concurrent.atomic.AtomicReference<String> savedLogoUrl = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<String> syncedLogoUrl = new java.util.concurrent.atomic.AtomicReference<>();
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(inv -> {
            ClientAdmin saved = inv.getArgument(0);
            savedLogoUrl.set(saved.getLogoUrl());
            return saved;
        });
        org.mockito.Mockito.doAnswer(inv -> {
            ClientAdmin synced = inv.getArgument(2);
            syncedLogoUrl.set(synced.getLogoUrl());
            return null;
        }).when(aspireUserService).updateAspireUser(eq(CLIENT_ADMIN_ID), eq(UserType.CLIENT_ADMIN), any(ClientAdmin.class));
        when(clientProductRepository.findByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(Collections.emptyList());

        ClientAdminUpdateRequestDto updateRequest = ClientAdminUpdateRequestDto.builder()
                .organizationName("Updated Org")
                .logoUrl("https://cdn.example.com/logo.png")
                .build();

        ClientAdminWithProductsResponseDto result = clientAdminService.updateClientAdminById(CLIENT_ADMIN_ID, updateRequest);

        assertEquals("Updated Org", result.getOrganizationName());
        assertNull(savedLogoUrl.get());
        assertEquals("https://cdn.example.com/logo.png", syncedLogoUrl.get());
        verify(mspUsersRepository, never()).save(any(MspUser.class));
    }

    @Test
    void updateClientAdminById_updatesMspUserWhenCurrentUserIsMsp() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspUserContext());
        MspUser mspUser = MspUser.builder()
                .id(MSP_ID)
                .mspId(MSP_ID)
                .organizationName("MSP Org")
                .status("ACTIVE")
                .build();
        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(mspUser));
        java.util.concurrent.atomic.AtomicReference<String> savedLogoUrl = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<String> syncedLogoUrl = new java.util.concurrent.atomic.AtomicReference<>();
        when(mspUsersRepository.save(any(MspUser.class))).thenAnswer(inv -> {
            MspUser saved = inv.getArgument(0);
            savedLogoUrl.set(saved.getLogoUrl());
            return saved;
        });
        org.mockito.Mockito.doAnswer(inv -> {
            MspUser synced = inv.getArgument(2);
            syncedLogoUrl.set(synced.getLogoUrl());
            return null;
        }).when(aspireUserService).updateAspireUser(eq(MSP_ID), eq(UserType.MSP), any(MspUser.class));

        ClientAdminUpdateRequestDto updateRequest = ClientAdminUpdateRequestDto.builder()
                .organizationName("Updated MSP Org")
                .logoUrl("https://cdn.example.com/msp-logo.png")
                .build();

        ClientAdminWithProductsResponseDto result = clientAdminService.updateClientAdminById(MSP_ID, updateRequest);

        assertEquals("Updated MSP Org", result.getOrganizationName());
        assertNull(savedLogoUrl.get());
        assertEquals("https://cdn.example.com/msp-logo.png", syncedLogoUrl.get());
        verify(clientAdminRepository, never()).save(any(ClientAdmin.class));
    }

    @Test
    void updateClientAdminById_throwsWhenNotFound() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminUserContext());
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.updateClientAdminById(CLIENT_ADMIN_ID, new ClientAdminUpdateRequestDto()));
    }

    // --- getAssignedClientProducts ---

    @Test
    void getAssignedClientProducts_returnsPaginatedActiveProducts() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        ClientProduct activeProduct = sampleClientProduct("ACTIVE");
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientProductRepository.findByClientAdminIdAndLicenseStatusIn(CLIENT_ADMIN_ID, List.of("ACTIVE")))
                .thenReturn(List.of(activeProduct));
        when(cmsServiceClient.getProductPackageDetails(PRODUCT_ID, PACKAGE_ID))
                .thenReturn(CmsProductWithSinglePackageResponseDto.builder().productId(PRODUCT_ID).build());

        ClientProductsPaginatedResponseDto result =
                clientAdminService.getAssignedClientProducts(CLIENT_ADMIN_ID, null, 0, 10, "displayOrder", "asc");

        assertEquals(1, result.getItems().size());
        assertEquals(CLIENT_ADMIN_ID, result.getClientAdminId());
        assertEquals(1L, result.getTotal());
        verify(clientProductRepository).findByClientAdminIdAndLicenseStatusIn(CLIENT_ADMIN_ID, List.of("ACTIVE"));
    }

    @Test
    void getAssignedClientProducts_throwsWhenSortFieldInvalid() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getAssignedClientProducts(CLIENT_ADMIN_ID, null, 0, 10, "invalidField", "asc"));
    }

    // --- getAssignedActiveAndPendingClientProducts ---

    @Test
    void getAssignedActiveAndPendingClientProducts_returnsPaginatedActiveAndPendingProducts() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        ClientProduct activeProduct = sampleClientProduct("ACTIVE");
        ClientProduct pendingProduct = sampleClientProduct("PENDING");
        pendingProduct.setId("client-product-id-2");
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientProductRepository.findByClientAdminIdAndLicenseStatusIn(CLIENT_ADMIN_ID, List.of("ACTIVE", "PENDING")))
                .thenReturn(List.of(activeProduct, pendingProduct));
        when(cmsServiceClient.getProductPackageDetails(PRODUCT_ID, PACKAGE_ID))
                .thenReturn(CmsProductWithSinglePackageResponseDto.builder().productId(PRODUCT_ID).build());

        ClientProductsPaginatedResponseDto result =
                clientAdminService.getAssignedActiveAndPendingClientProducts(
                        CLIENT_ADMIN_ID, null, 0, 10, "displayOrder", "asc");

        assertEquals(2, result.getItems().size());
        assertEquals(CLIENT_ADMIN_ID, result.getClientAdminId());
        assertEquals(2L, result.getTotal());
        verify(clientProductRepository).findByClientAdminIdAndLicenseStatusIn(CLIENT_ADMIN_ID, List.of("ACTIVE", "PENDING"));
    }

    @Test
    void getAssignedActiveAndPendingClientProducts_includesPendingProductsOnlyWhenReturnedByRepository() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        ClientProduct pendingProduct = sampleClientProduct("PENDING");
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientProductRepository.findByClientAdminIdAndLicenseStatusIn(CLIENT_ADMIN_ID, List.of("ACTIVE", "PENDING")))
                .thenReturn(List.of(pendingProduct));
        when(cmsServiceClient.getProductPackageDetails(PRODUCT_ID, PACKAGE_ID))
                .thenReturn(CmsProductWithSinglePackageResponseDto.builder().productId(PRODUCT_ID).build());

        ClientProductsPaginatedResponseDto result =
                clientAdminService.getAssignedActiveAndPendingClientProducts(
                        CLIENT_ADMIN_ID, null, 0, 10, "displayOrder", "asc");

        assertEquals(1, result.getItems().size());
        assertEquals("PENDING", result.getItems().get(0).getLicenseStatus());
    }

    @Test
    void getAssignedActiveAndPendingClientProducts_throwsWhenSortFieldInvalid() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getAssignedActiveAndPendingClientProducts(
                        CLIENT_ADMIN_ID, null, 0, 10, "invalidField", "asc"));
    }

    // --- getLicenseStatistics ---

    @Test
    void getLicenseStatistics_returnsCalculatedStatistics() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, null))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(100, 40));
        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(
                        ClientProduct.builder().productId(PRODUCT_ID).usedLicenseCount(40).licenseCount(100).build()));

        LicenseStatisticsResponseDto result = clientAdminService.getLicenseStatistics(CLIENT_ADMIN_ID);

        assertEquals(100, result.getLicenseCount());
        assertEquals(40, result.getUsedLicenseCount());
        assertEquals(60, result.getAvailableLicenseCount());
        assertEquals(40.0, result.getUtilizationPercentage());
        verify(phishingServiceClient, never()).getCampaignLicenseUsage(anyString());
    }

    @Test
    void getLicenseStatistics_throwsWhenClientAdminNotFound() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getLicenseStatistics(CLIENT_ADMIN_ID));
    }

    @Test
    void getLicenseStatistics_withProductId_filtersByProduct() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, PRODUCT_ID))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(50, 20));
        when(simulationProductResolver.isSimulationProduct(PRODUCT_ID)).thenReturn(false);

        LicenseStatisticsResponseDto result = clientAdminService.getLicenseStatistics(CLIENT_ADMIN_ID, PRODUCT_ID);

        assertEquals(50, result.getLicenseCount());
        assertEquals(20, result.getUsedLicenseCount());
        assertEquals(30, result.getAvailableLicenseCount());
        assertEquals(40.0, result.getUtilizationPercentage());
        verify(clientProductRepositoryCustom).getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, PRODUCT_ID);
        verify(phishingServiceClient, never()).getCampaignLicenseUsage(anyString());
    }

    @Test
    void getLicenseStatistics_withProductId_returnsZerosWhenNoMatch() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, PRODUCT_ID))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(0, 0));
        when(simulationProductResolver.isSimulationProduct(PRODUCT_ID)).thenReturn(false);

        LicenseStatisticsResponseDto result = clientAdminService.getLicenseStatistics(CLIENT_ADMIN_ID, PRODUCT_ID);

        assertEquals(0, result.getLicenseCount());
        assertEquals(0, result.getUsedLicenseCount());
        assertEquals(0, result.getAvailableLicenseCount());
        assertEquals(0.0, result.getUtilizationPercentage());
    }

    @Test
    void getLicenseStatistics_simulationProduct_usesUniqueCampaignUsers() {
        String phishingProductId = "phishing-product-id";
        String productPackageId = "pp-silver-1";
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, phishingProductId))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(15, 99));
        when(simulationProductResolver.isSimulationProduct(phishingProductId)).thenReturn(true);
        when(clientProductRepository.findByClientAdminIdAndProductIdAndLicenseStatus(
                CLIENT_ADMIN_ID, phishingProductId, "ACTIVE"))
                .thenReturn(List.of(ClientProduct.builder()
                        .id(productPackageId)
                        .productId(phishingProductId)
                        .licenseCount(15)
                        .build()));
        when(phishingServiceClient.getCampaignLicenseUsage(CLIENT_ADMIN_ID))
                .thenReturn(List.of(
                        CampaignLicenseUsageDto.builder()
                                .productPackageId(productPackageId)
                                .uniqueUserCount(10)
                                .build(),
                        CampaignLicenseUsageDto.builder()
                                .productPackageId("other-product-pp")
                                .uniqueUserCount(99)
                                .build()));

        LicenseStatisticsResponseDto result =
                clientAdminService.getLicenseStatistics(CLIENT_ADMIN_ID, phishingProductId);

        assertEquals(15, result.getLicenseCount());
        assertEquals(10, result.getUsedLicenseCount());
        assertEquals(5, result.getAvailableLicenseCount());
        assertEquals(66.67, result.getUtilizationPercentage());
    }

    @Test
    void getLicenseStatistics_allProducts_mixesSeatAndCampaignUsage() {
        String phishingProductId = "phishing-product-id";
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, null))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(115, 50));
        when(simulationProductResolver.getSimulationProductIds()).thenReturn(Set.of(phishingProductId));
        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(
                        ClientProduct.builder().productId(PRODUCT_ID).usedLicenseCount(40).licenseCount(100).build(),
                        ClientProduct.builder().productId(phishingProductId).usedLicenseCount(99).licenseCount(15).build()));
        when(phishingServiceClient.getCampaignLicenseUsage(CLIENT_ADMIN_ID))
                .thenReturn(List.of(
                        CampaignLicenseUsageDto.builder()
                                .productPackageId("pp-silver-1")
                                .uniqueUserCount(10)
                                .build()));

        LicenseStatisticsResponseDto result = clientAdminService.getLicenseStatistics(CLIENT_ADMIN_ID);

        assertEquals(115, result.getLicenseCount());
        assertEquals(50, result.getUsedLicenseCount()); // 40 seat + 10 unique campaign
        assertEquals(65, result.getAvailableLicenseCount());
    }

    @Test
    void getLicenseStatistics_simulationProduct_phishingFailurePropagates() {
        String phishingProductId = "phishing-product-id";
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, phishingProductId))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(15, 0));
        when(simulationProductResolver.isSimulationProduct(phishingProductId)).thenReturn(true);
        when(phishingServiceClient.getCampaignLicenseUsage(CLIENT_ADMIN_ID))
                .thenThrow(new IllegalStateException("phishing down"));

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getLicenseStatistics(CLIENT_ADMIN_ID, phishingProductId));
    }

    @Test
    void getLicenseStatistics_simulationProduct_noCampaigns_returnsZeroUsed() {
        String phishingProductId = "phishing-product-id";
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, phishingProductId))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(15, 0));
        when(simulationProductResolver.isSimulationProduct(phishingProductId)).thenReturn(true);
        when(phishingServiceClient.getCampaignLicenseUsage(CLIENT_ADMIN_ID))
                .thenReturn(Collections.emptyList());

        LicenseStatisticsResponseDto result =
                clientAdminService.getLicenseStatistics(CLIENT_ADMIN_ID, phishingProductId);

        assertEquals(15, result.getLicenseCount());
        assertEquals(0, result.getUsedLicenseCount());
        assertEquals(15, result.getAvailableLicenseCount());
    }

    // --- getLicenseOverviewSummary ---

    @Test
    void getLicenseOverviewSummary_matchingKpiCards_returnsRoundedPercentsAndMomDelta() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(
                        ClientProduct.builder()
                                .productId(PRODUCT_ID)
                                .licenseCount(995)
                                .usedLicenseCount(850)
                                .assignedAt(Instant.parse("2020-01-01T00:00:00Z"))
                                .expiryDate(Instant.now().plus(10, ChronoUnit.DAYS))
                                .build(),
                        ClientProduct.builder()
                                .productId("product-id-2")
                                .licenseCount(0)
                                .usedLicenseCount(0)
                                .assignedAt(Instant.parse("2020-01-01T00:00:00Z"))
                                .expiryDate(Instant.now().plus(20, ChronoUnit.DAYS))
                                .build()));
        when(userLicenceRepository.countAssignedAsOf(eq(CLIENT_ADMIN_ID), any(Instant.class))).thenReturn(814L);

        LicenseOverviewSummaryDto result = clientAdminService.getLicenseOverviewSummary(CLIENT_ADMIN_ID);

        assertEquals(995, result.getTotalLicenses());
        assertEquals(850, result.getAssignedUsers());
        assertEquals(85.4, result.getAssignedUsersPercent());
        assertEquals(145, result.getAvailableSeats());
        assertEquals(14.6, result.getAvailableSeatsPercent());
        assertEquals(85.4, result.getUtilizationRate());
        assertEquals(3.6, result.getUtilizationChangeVsLastMonth());
        assertEquals(2, result.getExpiringSoon());
        assertEquals(30, result.getExpiringSoonDays());
        assertEquals(2, result.getActiveProducts());
        verify(clientProductRepositoryCustom, never()).getLicenseStatisticsByClientAdminId(anyString(), any());
        verify(phishingServiceClient, never()).getCampaignLicenseUsage(anyString());
    }

    @Test
    void getLicenseOverviewSummary_zeroLicenses_returnsZeros() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(Collections.emptyList());

        LicenseOverviewSummaryDto result = clientAdminService.getLicenseOverviewSummary(CLIENT_ADMIN_ID);

        assertEquals(0, result.getTotalLicenses());
        assertEquals(0, result.getAssignedUsers());
        assertEquals(0.0, result.getAssignedUsersPercent());
        assertEquals(0, result.getAvailableSeats());
        assertEquals(0.0, result.getAvailableSeatsPercent());
        assertEquals(0.0, result.getUtilizationRate());
        assertEquals(0.0, result.getUtilizationChangeVsLastMonth());
        assertEquals(0, result.getExpiringSoon());
        assertEquals(30, result.getExpiringSoonDays());
        assertEquals(0, result.getActiveProducts());
        verify(userLicenceRepository, never()).countAssignedAsOf(anyString(), any(Instant.class));
    }

    @Test
    void getLicenseOverviewSummary_recentlyAssignedProducts_lastMonthUtilizationIsZero() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepository.findByClientAdminIdAndLicenseStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(ClientProduct.builder()
                        .productId(PRODUCT_ID)
                        .licenseCount(100)
                        .usedLicenseCount(40)
                        .assignedAt(Instant.now())
                        .build()));

        LicenseOverviewSummaryDto result = clientAdminService.getLicenseOverviewSummary(CLIENT_ADMIN_ID);

        assertEquals(40.0, result.getUtilizationRate());
        assertEquals(40.0, result.getUtilizationChangeVsLastMonth());
        assertEquals(1, result.getActiveProducts());
        verify(userLicenceRepository, never()).countAssignedAsOf(anyString(), any(Instant.class));
    }

    @Test
    void getLicenseOverviewSummary_throwsWhenClientAdminNotFound() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getLicenseOverviewSummary(CLIENT_ADMIN_ID));
    }

    // --- getUniqueProductCountByClientAdminId ---

    @Test
    void getUniqueProductCountByClientAdminId_returnsCount() {
        when(clientProductRepositoryCustom.getUniqueProductCountByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(3);

        assertEquals(3, clientAdminService.getUniqueProductCountByClientAdminId(CLIENT_ADMIN_ID));
    }

    // --- getProductPackageDetailById ---

    @Test
    void getProductPackageDetailById_returnsDetailsWhenFound() {
        ClientProduct product = sampleClientProduct("ACTIVE");
        when(clientProductRepository.findById(CLIENT_PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cmsServiceClient.getProductPackageDetails(PRODUCT_ID, PACKAGE_ID))
                .thenReturn(CmsProductWithSinglePackageResponseDto.builder().productId(PRODUCT_ID).build());

        assertNotNull(clientAdminService.getProductPackageDetailById(CLIENT_PRODUCT_ID));
    }

    @Test
    void getProductPackageDetailById_throwsWhenNotFound() {
        when(clientProductRepository.findById(CLIENT_PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getProductPackageDetailById(CLIENT_PRODUCT_ID));
    }

    // --- updateClientDashboardInCmsService ---

    @Test
    void updateClientDashboardInCmsService_postsDashboardDataToCms() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        when(clientProductRepositoryCustom.getUniqueProductCountByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(2);
        when(clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID, null))
                .thenReturn(new ClientProductRepositoryCustom.LicenseStatistics(20, 5));
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientProductRepository.findByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(List.of(sampleClientProduct("ACTIVE")));
        stubCmsDashboardWebClient();

        clientAdminService.updateClientDashboardInCmsService(CLIENT_ADMIN_ID);

        verify(webClient).post();
    }

    // --- getMspIdByClientAdminId ---

    @Test
    void getMspIdByClientAdminId_returnsMspIdWhenPresent() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setMspId(MSP_ID);
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));

        assertEquals(MSP_ID, clientAdminService.getMspIdByClientAdminId(CLIENT_ADMIN_ID));
    }

    @Test
    void getMspIdByClientAdminId_throwsWhenMspIdMissing() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        clientAdmin.setMspId(null);
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getMspIdByClientAdminId(CLIENT_ADMIN_ID));
    }

    // --- getClientsOrMspsByCountry ---

    @Test
    void getClientsOrMspsByCountry_returnsClientsWhenIsClientTrue() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        when(clientAdminRepository.findByCountry("US")).thenReturn(List.of(clientAdmin));

        assertEquals(1, clientAdminService.getClientsOrMspsByCountry("US", true).size());
        verify(mspUsersRepository, never()).findByCountry(anyString());
    }

    @Test
    void getClientsOrMspsByCountry_returnsMspsWhenIsClientFalse() {
        MspUser mspUser = MspUser.builder()
                .id(MSP_ID)
                .organizationName("MSP Org")
                .mspAdminEmail("msp@example.com")
                .build();
        when(mspUsersRepository.findByCountry("US")).thenReturn(List.of(mspUser));

        assertEquals(1, clientAdminService.getClientsOrMspsByCountry("US", false).size());
    }

    // --- getOrganizationLicenseStatistics ---

    @Test
    void getOrganizationLicenseStatistics_returnsStatisticsWhenClientExists() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientProductRepositoryCustom.getOrganizationLicenseStatisticsByClientAdminId(CLIENT_ADMIN_ID))
                .thenReturn(OrganizationLicenseStatistics.builder()
                        .totalAvailableLicenses(50)
                        .totalAllocatedLicenses(40)
                        .totalActiveLicenses(35)
                        .totalExpiredLicenses(5)
                        .build());

        OrganizationLicenseStatisticsResponseDto result =
                clientAdminService.getOrganizationLicenseStatistics(CLIENT_ADMIN_ID);

        assertEquals(50, result.getTotalAvailableLicenses());
        assertEquals(40, result.getTotalAllocatedLicenses());
    }

    @Test
    void getOrganizationLicenseStatistics_throwsWhenClientAdminNotFound() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getOrganizationLicenseStatistics(CLIENT_ADMIN_ID));
    }

    // --- getClientsByMspId / getClientsDropdownForMsp ---

    @Test
    void getClientsByMspId_returnsDropdownItems() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        Page<ClientAdmin> page = new PageImpl<>(List.of(clientAdmin));
        when(clientAdminRepositoryCustom.findClientAdminsWithFilters(
                eq(null), eq(MSP_ID), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        assertEquals(1, clientAdminService.getClientsByMspId(MSP_ID).size());
        assertEquals(clientAdmin.getOrganizationName(), clientAdminService.getClientsByMspId(MSP_ID).get(0).getOrganizationName());
    }

    @Test
    void getClientsDropdownForMsp_returnsFilteredClients() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        Page<ClientAdmin> page = new PageImpl<>(List.of(clientAdmin));
        when(clientAdminRepositoryCustom.findClientAdminsWithFilters(
                eq("acme"), eq(MSP_ID), eq(null), eq(null), eq("country-1"), eq("state-1"), any(Pageable.class)))
                .thenReturn(page);

        List<com.aspire.asat.registration.data.clientAdmin.response.ClientDropdownDto> result =
                clientAdminService.getClientsDropdownForMsp(MSP_ID, "country-1", "state-1", "acme");

        assertEquals(1, result.size());
        assertEquals(CLIENT_ADMIN_ID, result.get(0).getId());
    }

    @Test
    void getClientsDropdownForMsp_throwsWhenMspUserAccessesAnotherMsp() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspUserContext());

        RegistrationServiceException ex = assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.getClientsDropdownForMsp("other-msp-id", null, null, null));
        assertTrue(ex.getMessage().contains("Access denied"));
    }

    @Test
    void getClientsDropdownForMsp_throwsWhenMspIdMissing() {
        assertThrows(RegistationValidationException.class,
                () -> clientAdminService.getClientsDropdownForMsp("  ", null, null, null));
    }

    // --- updateClientAdminStatus ---

    @Test
    void updateClientAdminStatus_throwsWhenStatusIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> clientAdminService.updateClientAdminStatus(CLIENT_ADMIN_ID, null));
    }

    @Test
    void updateClientAdminStatus_deactivatesActiveUsersWhenInactive() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        AspireUser clientAdminUser = sampleAspireUser(false);
        AspireUser activeEndUser = AspireUser.builder()
                .id(END_USER_ID)
                .status("ACTIVE")
                .build();

        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aspireUserRepository.findByUserId(UUID.fromString(CLIENT_ADMIN_ID))).thenReturn(Optional.of(clientAdminUser));
        when(aspireUserRepository.findByClientAdminIdAndStatus(CLIENT_ADMIN_ID, "ACTIVE"))
                .thenReturn(List.of(activeEndUser));
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.updateClientAdminStatus(CLIENT_ADMIN_ID, AdminStatus.INACTIVE);

        assertEquals(AdminStatus.INACTIVE, clientAdmin.getStatus());
        assertEquals("INACTIVE", activeEndUser.getStatus());
        assertTrue(activeEndUser.getIsAdminInactive());
        verify(userSessionInvalidationHelper).logoutUsersIfRestrictive(eq("INACTIVE"), anyList());
    }

    @Test
    void updateClientAdminStatus_reactivatesUsersWhenActive() {
        ClientAdmin clientAdmin = sampleClientAdmin();
        AspireUser clientAdminUser = sampleAspireUser(false);
        AspireUser inactiveEndUser = AspireUser.builder()
                .id(END_USER_ID)
                .status("INACTIVE")
                .isAdminInactive(true)
                .build();

        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aspireUserRepository.findByUserId(UUID.fromString(CLIENT_ADMIN_ID))).thenReturn(Optional.of(clientAdminUser));
        when(aspireUserRepository.findByClientAdminIdAndIsAdminInactive(CLIENT_ADMIN_ID, true))
                .thenReturn(List.of(inactiveEndUser));
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.updateClientAdminStatus(CLIENT_ADMIN_ID, AdminStatus.ACTIVE);

        assertEquals(AdminStatus.ACTIVE, clientAdmin.getStatus());
        assertEquals("ACTIVE", inactiveEndUser.getStatus());
        assertFalse(inactiveEndUser.getIsAdminInactive());
        verify(userSessionInvalidationHelper, never()).logoutUsersIfRestrictive(eq("ACTIVE"), any());
    }

    // --- updateAspireUserStatus ---

    @Test
    void updateAspireUserStatus_throwsWhenStatusInvalid() {
        UserSuspendRequestDto request = UserSuspendRequestDto.builder().status("INVALID").build();

        assertThrows(IllegalArgumentException.class,
                () -> clientAdminService.updateAspireUserStatus(CLIENT_ADMIN_ID, request));
    }

    @Test
    void updateAspireUserStatus_throwsWhenUserNotFound() {
        UserSuspendRequestDto request = UserSuspendRequestDto.builder().status("SUSPEND").build();
        when(aspireUserRepository.findByUserId(UUID.fromString(CLIENT_ADMIN_ID))).thenReturn(Optional.empty());

        assertThrows(RegistrationServiceException.class,
                () -> clientAdminService.updateAspireUserStatus(CLIENT_ADMIN_ID, request));
    }

    @Test
    void updateAspireUserStatus_suspendsClientAdminAndAssociatedUsers() {
        UserSuspendRequestDto request = UserSuspendRequestDto.builder()
                .status("SUSPEND")
                .suspendReason("Policy violation")
                .build();
        AspireUser clientAdminUser = sampleAspireUser(false);
        clientAdminUser.setUserType(UserType.CLIENT_ADMIN.name());
        AspireUser endUser = AspireUser.builder()
                .id(END_USER_ID)
                .userId(UUID.randomUUID())
                .email("user@client.com")
                .status("ACTIVE")
                .isAdminInactive(false)
                .build();

        when(aspireUserRepository.findByUserId(UUID.fromString(CLIENT_ADMIN_ID))).thenReturn(Optional.of(clientAdminUser));
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aspireUserRepository.findByClientAdminIdAndIsAdminInactive(CLIENT_ADMIN_ID, false))
                .thenReturn(List.of(clientAdminUser, endUser));
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userSuspendReasonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.updateAspireUserStatus(CLIENT_ADMIN_ID, request);

        assertEquals("SUSPEND", clientAdminUser.getStatus());
        assertEquals("SUSPEND", endUser.getStatus());
        assertTrue(endUser.getIsAdminInactive());
        verify(userSuspendReasonRepository).save(any());
        verify(userSessionInvalidationHelper).logoutUsersIfRestrictive(eq("SUSPEND"), anyList());
    }

    @Test
    void updateAspireUserStatus_reactivatesSuspendedUsersWhenActive() {
        UserSuspendRequestDto request = UserSuspendRequestDto.builder().status("ACTIVE").build();
        AspireUser clientAdminUser = sampleAspireUser(false);
        AspireUser suspendedUser = AspireUser.builder()
                .id(END_USER_ID)
                .status("SUSPEND")
                .isAdminInactive(true)
                .build();

        when(aspireUserRepository.findByUserId(UUID.fromString(CLIENT_ADMIN_ID))).thenReturn(Optional.of(clientAdminUser));
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(sampleClientAdmin()));
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aspireUserRepository.findByClientAdminIdAndStatusAndIsAdminInactive(CLIENT_ADMIN_ID, "SUSPEND", true))
                .thenReturn(List.of(suspendedUser));
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(inv -> inv.getArgument(0));

        clientAdminService.updateAspireUserStatus(CLIENT_ADMIN_ID, request);

        assertEquals("ACTIVE", clientAdminUser.getStatus());
        assertEquals("ACTIVE", suspendedUser.getStatus());
        assertFalse(suspendedUser.getIsAdminInactive());
    }

    // --- helpers ---

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubCmsDashboardWebClient() {
        WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
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

    private static CurrentUserContext adminUserContext() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserId("admin-user-id");
        context.setUserType(UserType.ASPIRE_ADMIN.name());
        return context;
    }

    private static CurrentUserContext clientAdminUserContext() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserId(CLIENT_ADMIN_ID);
        context.setUserType(UserType.CLIENT_ADMIN.name());
        return context;
    }

    private static CurrentUserContext mspUserContext() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserId(MSP_ID);
        context.setUserType(UserType.MSP.name());
        return context;
    }

    private ClientOnboardingRequestDto buildOnboardingRequest() {
        OrganizationInfoDto organization = OrganizationInfoDto.builder()
                .organizationName("Client Org")
                .organizationType("org-type-id")
                .contactEmail("contact@client.com")
                .phoneNumber("+1234567890")
                .country("country-id")
                .domain("client.com")
                .organizationSize("Medium")
                .adminEmail(ADMIN_EMAIL)
                .streetAddress("123 Main St")
                .city("New York")
                .zipPostalCode("10001")
                .build();

        BillingInfoDto billing = BillingInfoDto.builder()
                .billingEmail("billing@client.com")
                .billingName("Billing Contact")
                .country("country-id")
                .build();

        ProductSelectionDto productSelection = ProductSelectionDto.builder()
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .licenseCount(10)
                .pricePerLicense(100.0)
                .validityPeriod(12)
                .validityUnit(ValidityUnit.MONTH)
                .build();

        InvoiceDetailsDto invoice = InvoiceDetailsDto.builder()
                .subtotal(12000.0)
                .totalAmount(12000.0)
                .vatAmount(0.0)
                .discountAmount(0.0)
                .discountPercentage(0.0)
                .build();

        return ClientOnboardingRequestDto.builder()
                .organization(organization)
                .billing(billing)
                .productSelections(List.of(productSelection))
                .invoice(invoice)
                .mspId(MSP_ID)
                .mspName("MSP Org")
                .build();
    }

    private BuyNowRequestDto buildBuyNowRequest() {
        ProductSelectionDto productSelection = ProductSelectionDto.builder()
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .licenseCount(5)
                .pricePerLicense(50.0)
                .validityPeriod(12)
                .validityUnit(ValidityUnit.MONTH)
                .build();

        InvoiceDetailsDto invoice = InvoiceDetailsDto.builder()
                .subtotal(3000.0)
                .totalAmount(3000.0)
                .vatAmount(0.0)
                .discountAmount(0.0)
                .discountPercentage(0.0)
                .build();

        return BuyNowRequestDto.builder()
                .clientAdminId(CLIENT_ADMIN_ID)
                .productSelections(List.of(productSelection))
                .invoice(invoice)
                .build();
    }

    private ClientProductAssignment buildReassignmentRequest() {
        ProductSelectionDto productSelection = ProductSelectionDto.builder()
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .licenseCount(5)
                .pricePerLicense(50.0)
                .validityPeriod(12)
                .validityUnit(ValidityUnit.MONTH)
                .build();

        InvoiceDetailsDto invoice = InvoiceDetailsDto.builder()
                .subtotal(3000.0)
                .totalAmount(3000.0)
                .vatAmount(0.0)
                .discountAmount(0.0)
                .discountPercentage(0.0)
                .build();

        return ClientProductAssignment.builder()
                .clientAdminId(CLIENT_ADMIN_ID)
                .productSelections(List.of(productSelection))
                .invoice(invoice)
                .build();
    }

    private ClientAdmin sampleClientAdmin() {
        return ClientAdmin.builder()
                .id(CLIENT_ADMIN_ID)
                .email(ADMIN_EMAIL)
                .organizationName("Client Org")
                .country("country-id")
                .status(AdminStatus.ACTIVE)
                .mspId(MSP_ID)
                .createdAt(Instant.now())
                .build();
    }

    private ClientProduct sampleClientProduct(String licenseStatus) {
        return ClientProduct.builder()
                .id(CLIENT_PRODUCT_ID)
                .clientAdminId(CLIENT_ADMIN_ID)
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .licenseCount(10)
                .usedLicenseCount(2)
                .pricePerLicense(100.0)
                .totalPrice(1000.0)
                .validityPeriod(12)
                .validityUnit(ValidityUnit.MONTH.name())
                .assignedAt(Instant.now())
                .licenseStatus(licenseStatus)
                .build();
    }

    private AspireUser sampleAspireUser(boolean isTrial) {
        UUID clientAdminUuid = UUID.fromString(CLIENT_ADMIN_ID);
        return AspireUser.builder()
                .id(clientAdminUuid)
                .userId(clientAdminUuid)
                .email(ADMIN_EMAIL)
                .userType(UserType.CLIENT_ADMIN.name())
                .status("ACTIVE")
                .isTrial(isTrial)
                .build();
    }
}
