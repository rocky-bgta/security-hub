package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.PhishingServiceClient;
import com.aspire.asat.registration.data.subpackage.SubPackageAssignRequest;
import com.aspire.asat.registration.mapper.AspireUserMapper;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.model.EndUserPackage;
import com.aspire.asat.registration.model.UserLicence;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.UserLicenceRepository;
import com.aspire.asat.registration.repository.UserSuspendReasonRepository;
import com.aspire.asat.registration.repository.custom.EndUserRepositoryCustom;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.DepartmentService;
import com.aspire.asat.registration.service.support.SimulationProductResolver;
import com.aspire.asat.registration.service.support.UserSessionInvalidationHelper;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndUserServiceImplAssignSubPackageTest {

    private static final String USER_ID = "user-1";
    private static final String SUB_PACKAGE_ID = "sub-package-1";
    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String PRODUCT_ID = "product-1";
    private static final String PACKAGE_ID = "package-1";
    private static final String PRODUCT_PACKAGE_ID = "product-package-1";

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EndUserPackageRepository endUserPackageRepository;
    @Mock private AspireUserService aspireUserService;
    @Mock private DepartmentService departmentService;
    @Mock private RoleRepository roleRepository;
    @Mock private ClientProductRepository clientProductRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private WebClient webClient;
    @Mock private UserLicenceRepository userLicenceRepository;
    @Mock private UserCurrentContextService currentContextService;
    @Mock private RegistrationNotificationClient notificationClient;
    @Mock private AspireUserRepository aspireUserRepository;
    @Mock private ClientAdminRepository clientAdminRepository;
    @Mock private MspUsersRepository mspUsersRepository;
    @Mock private UserSuspendReasonRepository userSuspendReasonRepository;
    @Mock private MongoTemplate mongoTemplate;
    @Mock private EndUserRepositoryCustom endUserRepositoryCustom;
    @Mock private UserSessionInvalidationHelper userSessionInvalidationHelper;
    @Mock private com.aspire.asat.registration.repository.dropdown.TimezoneRepository timezoneRepository;
    @Mock private PhishingServiceClient phishingServiceClient;
    @Mock private AspireUserMapper aspireUserMapper;
    @Mock private SimulationProductResolver simulationProductResolver;

    @InjectMocks
    private EndUserServiceImpl endUserService;

    @BeforeEach
    void setUp() {
        lenient().when(simulationProductResolver.isSimulationProduct(anyString())).thenReturn(false);
    }

    @Test
    void assignSubPackageToUsers_existingAssignment_skipsCreatingRecordsAndNotifications() {
        when(clientProductRepository.findById(PRODUCT_PACKAGE_ID)).thenReturn(Optional.of(activeClientProduct()));
        when(userLicenceRepository.countByClientAdminIdAndProductIdAndPackageId(
                CLIENT_ADMIN_ID, PRODUCT_ID, PACKAGE_ID)).thenReturn(0L);
        when(endUserPackageRepository.existsByUserIdAndSubPackageId(USER_ID, SUB_PACKAGE_ID)).thenReturn(true);
        stubCmsAssignmentWebClient();

        endUserService.assignSubPackageToUsers(buildRequest(List.of(USER_ID), true));

        verify(endUserPackageRepository, never()).save(any());
        verify(userLicenceRepository, never()).save(any());
        verify(notificationClient, never()).sendPackageAssignedNotification(any(), anyBoolean());
    }

    @Test
    void assignSubPackageToUsers_simulationProduct_skipsUserLicenceAndUsedCount() {
        when(simulationProductResolver.isSimulationProduct(PRODUCT_ID)).thenReturn(true);
        when(clientProductRepository.findById(PRODUCT_PACKAGE_ID)).thenReturn(Optional.of(activeClientProduct()));
        when(endUserPackageRepository.existsByUserIdAndSubPackageId(USER_ID, SUB_PACKAGE_ID)).thenReturn(false);
        when(endUserPackageRepository.save(any(EndUserPackage.class))).thenAnswer(inv -> inv.getArgument(0));
        stubCmsAssignmentWebClient();

        endUserService.assignSubPackageToUsers(buildRequest(List.of(USER_ID), false));

        verify(endUserPackageRepository).save(any(EndUserPackage.class));
        verify(userLicenceRepository, never()).save(any());
        verify(clientProductRepository, never()).save(any());
        verify(userLicenceRepository, never()).countByClientAdminIdAndProductIdAndPackageId(
                anyString(), anyString(), anyString());
        verify(webClient).post();
    }

    @Test
    void assignSubPackageToUsers_nonSimulationProduct_createsUserLicence() {
        ClientProduct product = activeClientProduct();
        when(clientProductRepository.findById(PRODUCT_PACKAGE_ID)).thenReturn(Optional.of(product));
        when(userLicenceRepository.countByClientAdminIdAndProductIdAndPackageId(
                CLIENT_ADMIN_ID, PRODUCT_ID, PACKAGE_ID)).thenReturn(0L);
        when(endUserPackageRepository.existsByUserIdAndSubPackageId(USER_ID, SUB_PACKAGE_ID)).thenReturn(false);
        when(endUserPackageRepository.save(any(EndUserPackage.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userLicenceRepository.existsByUserIdAndPackageId(USER_ID, PACKAGE_ID)).thenReturn(false);
        when(userLicenceRepository.save(any(UserLicence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(inv -> inv.getArgument(0));
        stubCmsAssignmentWebClient();

        endUserService.assignSubPackageToUsers(buildRequest(List.of(USER_ID), false));

        verify(userLicenceRepository).save(any(UserLicence.class));
        verify(clientProductRepository).save(eq(product));
        verify(endUserPackageRepository).save(any(EndUserPackage.class));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void stubCmsAssignmentWebClient() {
        WebClient.RequestBodyUriSpec bodyUriSpec = org.mockito.Mockito.mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = org.mockito.Mockito.mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec headersSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(bodyUriSpec);
        when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
    }

    private static ClientProduct activeClientProduct() {
        return ClientProduct.builder()
                .id(PRODUCT_PACKAGE_ID)
                .clientAdminId(CLIENT_ADMIN_ID)
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .licenseStatus("ACTIVE")
                .licenseCount(10)
                .usedLicenseCount(10)
                .expiryDate(Instant.now().plusSeconds(30L * 24 * 60 * 60))
                .build();
    }

    private static SubPackageAssignRequest buildRequest(List<String> userIds, boolean enableEmail) {
        SubPackageAssignRequest.CompletionDays completionDays = new SubPackageAssignRequest.CompletionDays();
        completionDays.setDurationUnit(SubPackageAssignRequest.DurationUnit.DAYS);
        completionDays.setDurationValue(30);

        SubPackageAssignRequest.SubPackageData data = new SubPackageAssignRequest.SubPackageData();
        data.setSubPackageId(SUB_PACKAGE_ID);
        data.setUserIdList(new ArrayList<>(userIds));
        data.setProductId(PRODUCT_ID);
        data.setPackageId(PACKAGE_ID);
        data.setProductPackageId(PRODUCT_PACKAGE_ID);
        data.setClientAdminId(CLIENT_ADMIN_ID);
        data.setStatus("ASSIGNED");
        data.setValidFor(30L);
        data.setEnableFirstUserNotificationEmail(enableEmail);
        data.setCompletionDays(completionDays);
        data.setSubPackageName("Security Course");
        data.setProductName("SAT");

        SubPackageAssignRequest request = new SubPackageAssignRequest();
        request.setSubPackageData(List.of(data));
        return request;
    }
}
