package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.cms.response.SubPackageResponseDto;
import com.aspire.asat.registration.data.enums.NextDirection;
import com.aspire.asat.registration.data.enums.OnboardBy;
import com.aspire.asat.registration.data.trial.request.AssignTrialProductsRequestDto;
import com.aspire.asat.registration.data.trial.request.EmailVerificationRequestDto;
import com.aspire.asat.registration.data.trial.request.PasswordCreationRequestDto;
import com.aspire.asat.registration.data.trial.request.ProductsData;
import com.aspire.asat.registration.data.trial.request.TrialSignupRequestDto;
import com.aspire.asat.registration.data.trial.response.EmailVerificationResponseDto;
import com.aspire.asat.registration.data.trial.response.TrialSignupResponseDto;
import com.aspire.asat.registration.exception.DomainConflictException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.model.EndUserPackage;
import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.custom.ClientProductRepositoryCustom;
import com.aspire.asat.registration.service.AspireUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialSignupServiceImplTest {

    private static final String EMAIL = "trail-asat-v5@yopmail.com";
    private static final String SAT_PRODUCT = "sat-product-id";
    private static final String SAT_PACKAGE = "sat-package-id";
    private static final String PHISH_PRODUCT = "phish-product-id";
    private static final String PHISH_PACKAGE = "phish-package-id";
    private static final String PASSWORD = "Tr1al!SecureX9";

    @Mock private AspireUserRepository aspireUserRepository;
    @Mock private ClientAdminRepository clientAdminRepository;
    @Mock private ClientProductRepository clientProductRepository;
    @Mock private ClientProductRepositoryCustom clientProductRepositoryCustom;
    @Mock private EndUserPackageRepository endUserPackageRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleRepository roleRepository;
    @Mock private NotificationClient notificationClient;
    @Mock private WebClient webClient;
    @Mock private CmsServiceClient cmsServiceClient;
    @Mock private AspireUserService aspireUserService;
    @Mock private RegistrationNotificationClient registrationNotificationClient;
    @Mock private MessageService messageService;

    @InjectMocks
    private TrialSignupServiceImpl trialSignupService;

    @BeforeEach
    void setUp() {
        setField(trialSignupService, "trialPeriodDays", 30);
        setField(trialSignupService, "otpValiditySeconds", 300);
        setField(trialSignupService, "allowTrialOnboardingWithExistingDomain", false);
        setField(trialSignupService, "aspireAdminEmail", "admin@aspire.com");
        setField(trialSignupService, "cmsServiceUrl", "http://cms");
    }

    @Test
    void submitAccountDetails_NewUser_SendsOtpAndFlagsFalse() {
        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());
        when(clientAdminRepository.findByEmailDomainRegex(anyString())).thenReturn(List.of());
        when(notificationClient.sendEmailNotification(anyString(), any(), any())).thenReturn(true);

        EmailVerificationResponseDto response = trialSignupService.submitAccountDetails(signupRequest(
                List.of(product(SAT_PRODUCT, SAT_PACKAGE), product(PHISH_PRODUCT, PHISH_PACKAGE))));

        assertFalse(response.getVerified());
        assertEquals("Verification code sent to your email", response.getMessage());
        assertEquals(NextDirection.VERIFY, response.getNextDirection());
        assertEquals(2, response.getProductsData().size());
        assertFalse(response.getProductsData().get(0).getExistingTrail());
        assertFalse(response.getProductsData().get(1).getExistingTrail());
        verify(notificationClient).sendEmailNotification(eq(EMAIL), any(), any());
    }

    @Test
    void submitAccountDetails_LegacyRequestWithoutProductsData_SendsOtpAndRejectsExistingEmail() {
        when(aspireUserRepository.existsByUsernameIgnoreCase(EMAIL)).thenReturn(false);
        when(clientAdminRepository.findByEmailDomainRegex(anyString())).thenReturn(List.of());
        when(notificationClient.sendEmailNotification(anyString(), any(), any())).thenReturn(true);

        EmailVerificationResponseDto response = trialSignupService.submitAccountDetails(signupRequest(null));

        assertFalse(response.getVerified());
        assertEquals("Verification code sent to your email", response.getMessage());
        assertEquals(NextDirection.VERIFY, response.getNextDirection());
        assertEquals(EMAIL, response.getEmail());
        assertEquals(null, response.getProductsData());
        verify(notificationClient).sendEmailNotification(eq(EMAIL), any(), any());

        when(aspireUserRepository.existsByUsernameIgnoreCase(EMAIL)).thenReturn(true);
        ResourceAlreadyExistsException ex = assertThrows(ResourceAlreadyExistsException.class, () ->
                trialSignupService.submitAccountDetails(signupRequest(null)));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(notificationClient, times(1)).sendEmailNotification(eq(EMAIL), any(), any());
    }

    @Test
    void submitAccountDetails_ExistingUserMixedProducts_SkipsOtp() {
        AspireUser existing = existingTrialUser();
        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(existing));
        when(clientProductRepository.findByClientAdminIdAndProductIdAndPackageId(
                existing.getClientAdminId(), SAT_PRODUCT, SAT_PACKAGE))
                .thenReturn(activeProduct(SAT_PRODUCT, SAT_PACKAGE));
        when(clientProductRepository.findByClientAdminIdAndProductIdAndPackageId(
                existing.getClientAdminId(), PHISH_PRODUCT, PHISH_PACKAGE))
                .thenReturn(null);

        EmailVerificationResponseDto response = trialSignupService.submitAccountDetails(signupRequest(
                List.of(product(SAT_PRODUCT, SAT_PACKAGE), product(PHISH_PRODUCT, PHISH_PACKAGE))));

        assertTrue(response.getProductsData().get(0).getExistingTrail());
        assertFalse(response.getProductsData().get(1).getExistingTrail());
        assertEquals("You already have a trial account for one or more selected products", response.getMessage());
        assertEquals(NextDirection.ONBOARD, response.getNextDirection());
        verify(notificationClient, never()).sendEmailNotification(anyString(), any(), any());
    }

    @Test
    void submitAccountDetails_ExistingUserAllProducts_SkipsOtp() {
        AspireUser existing = existingTrialUser();
        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(existing));
        when(clientProductRepository.findByClientAdminIdAndProductIdAndPackageId(
                existing.getClientAdminId(), SAT_PRODUCT, SAT_PACKAGE))
                .thenReturn(activeProduct(SAT_PRODUCT, SAT_PACKAGE));

        EmailVerificationResponseDto response = trialSignupService.submitAccountDetails(
                signupRequest(List.of(product(SAT_PRODUCT, SAT_PACKAGE))));

        assertTrue(response.getProductsData().get(0).getExistingTrail());
        assertEquals(NextDirection.ONBOARD, response.getNextDirection());
        verify(notificationClient, never()).sendEmailNotification(anyString(), any(), any());
    }

    @Test
    void submitAccountDetails_ExistingUserAllNewProducts_SendsOtpWithOnboard() {
        AspireUser existing = existingTrialUser();
        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(existing));
        when(clientProductRepository.findByClientAdminIdAndProductIdAndPackageId(
                existing.getClientAdminId(), PHISH_PRODUCT, PHISH_PACKAGE))
                .thenReturn(null);
        when(notificationClient.sendEmailNotification(anyString(), any(), any())).thenReturn(true);

        EmailVerificationResponseDto response = trialSignupService.submitAccountDetails(
                signupRequest(List.of(product(PHISH_PRODUCT, PHISH_PACKAGE))));

        assertFalse(response.getProductsData().get(0).getExistingTrail());
        assertEquals(NextDirection.ONBOARD, response.getNextDirection());
        assertEquals("Verification code sent to your email", response.getMessage());
        verify(notificationClient).sendEmailNotification(eq(EMAIL), any(), any());
    }

    @Test
    void submitAccountDetails_DifferentEmailSameDomain_ThrowsDomainConflict() {
        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());
        when(clientAdminRepository.findByEmailDomainRegex(anyString()))
                .thenReturn(List.of(ClientAdmin.builder().id("other-admin").email("alice@yopmail.com").build()));

        assertThrows(DomainConflictException.class, () ->
                trialSignupService.submitAccountDetails(signupRequest(List.of(product(PHISH_PRODUCT, PHISH_PACKAGE)))));
        verify(notificationClient, never()).sendEmailNotification(eq(EMAIL), any(), any());
    }

    @Test
    void createPassword_NewUserTwoProducts_CreatesUserAndBothClientProducts() {
        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());
        when(clientAdminRepository.findByEmailDomainRegex(anyString())).thenReturn(List.of());
        when(notificationClient.sendEmailNotification(anyString(), any(), any())).thenReturn(true);
        when(messageService.get(anyString())).thenReturn("Email verified successfully!");

        List<ProductsData> products = List.of(
                product(SAT_PRODUCT, SAT_PACKAGE, "Security Awareness Training"),
                product(PHISH_PRODUCT, PHISH_PACKAGE, "Phishing Simulation"));
        trialSignupService.submitAccountDetails(signupRequest(products));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> templateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(notificationClient).sendEmailNotification(eq(EMAIL), any(), templateCaptor.capture());
        String otp = String.valueOf(templateCaptor.getValue().get("otp"));

        trialSignupService.verifyEmail(EmailVerificationRequestDto.builder()
                .email(EMAIL)
                .verificationCode(otp)
                .build());

        when(aspireUserRepository.existsByUsernameIgnoreCase(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");
        when(roleRepository.findByRoleName("CLIENT_ADMIN"))
                .thenReturn(Role.builder().id("role-ca").roleName("CLIENT_ADMIN").build());
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientAdminRepository.existsByEmail(EMAIL)).thenReturn(false);

        AtomicReference<ClientAdmin> savedAdmin = new AtomicReference<>();
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(invocation -> {
            ClientAdmin admin = invocation.getArgument(0);
            if (admin.getClientProductIds() == null) {
                admin.setClientProductIds(new ArrayList<>());
            }
            savedAdmin.set(admin);
            return admin;
        });
        when(clientAdminRepository.findById(anyString())).thenAnswer(invocation -> Optional.ofNullable(savedAdmin.get()));
        when(clientProductRepository.findByClientAdminIdAndProductIdAndPackageId(anyString(), anyString(), anyString()))
                .thenReturn(null);
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(endUserPackageRepository.save(any(EndUserPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cmsServiceClient.createTrialSubPackage(any()))
                .thenReturn(SubPackageResponseDto.builder().id("sp-1").name("Security Awareness Training Trial").build())
                .thenReturn(SubPackageResponseDto.builder().id("sp-2").name("Phishing Simulation Trial").build());

        TrialSignupResponseDto response = trialSignupService.createPassword(PasswordCreationRequestDto.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .confirmPassword(PASSWORD)
                .productsData(products)
                .build());

        assertTrue(response.getTrialPackage().contains("Security Awareness Training"));
        assertTrue(response.getTrialPackage().contains("Phishing Simulation"));
        assertEquals(2, response.getProductsData().size());
        assertFalse(response.getProductsData().get(0).getExistingTrail());
        verify(clientProductRepository, times(2)).save(any(ClientProduct.class));
        verify(cmsServiceClient, times(2)).createTrialSubPackage(any());
        verify(aspireUserRepository, times(2)).save(any(AspireUser.class));
    }

    @Test
    void createPassword_LegacyProductIdAndSubPackageId_AssignsSingleProduct() {
        when(aspireUserRepository.existsByUsernameIgnoreCase(EMAIL)).thenReturn(false);
        when(clientAdminRepository.findByEmailDomainRegex(anyString())).thenReturn(List.of());
        when(notificationClient.sendEmailNotification(anyString(), any(), any())).thenReturn(true);
        when(messageService.get(anyString())).thenReturn("Email verified successfully!");

        trialSignupService.submitAccountDetails(signupRequest(null));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> templateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(notificationClient).sendEmailNotification(eq(EMAIL), any(), templateCaptor.capture());
        String otp = String.valueOf(templateCaptor.getValue().get("otp"));
        trialSignupService.verifyEmail(EmailVerificationRequestDto.builder()
                .email(EMAIL)
                .verificationCode(otp)
                .build());

        when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");
        when(roleRepository.findByRoleName("CLIENT_ADMIN"))
                .thenReturn(Role.builder().id("role-ca").roleName("CLIENT_ADMIN").build());
        when(aspireUserRepository.save(any(AspireUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientAdminRepository.existsByEmail(EMAIL)).thenReturn(false);
        AtomicReference<ClientAdmin> savedAdmin = new AtomicReference<>();
        when(clientAdminRepository.save(any(ClientAdmin.class))).thenAnswer(invocation -> {
            ClientAdmin admin = invocation.getArgument(0);
            if (admin.getClientProductIds() == null) {
                admin.setClientProductIds(new ArrayList<>());
            }
            savedAdmin.set(admin);
            return admin;
        });
        when(clientAdminRepository.findById(anyString())).thenAnswer(invocation -> Optional.ofNullable(savedAdmin.get()));
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(endUserPackageRepository.save(any(EndUserPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cmsServiceClient.createTrialSubPackage(any()))
                .thenReturn(SubPackageResponseDto.builder().id("sp-legacy").name("A-SAT Trial").build());

        TrialSignupResponseDto response = trialSignupService.createPassword(PasswordCreationRequestDto.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .confirmPassword(PASSWORD)
                .productId(SAT_PRODUCT)
                .subPackageId(SAT_PACKAGE)
                .build());

        assertEquals("Security Awareness Training", response.getTrialPackage());
        assertEquals("30 Days", response.getTrialPeriod());
        assertEquals("Up to 5 users", response.getLicenses());
        assertEquals(null, response.getProductsData());
        verify(clientProductRepository, times(1)).save(any(ClientProduct.class));
        verify(cmsServiceClient, times(1)).createTrialSubPackage(any());
    }

    @Test
    void createPassword_ExistingUser_Rejected() {
        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());
        when(clientAdminRepository.findByEmailDomainRegex(anyString())).thenReturn(List.of());
        when(notificationClient.sendEmailNotification(anyString(), any(), any())).thenReturn(true);
        when(messageService.get(anyString())).thenReturn("Email verified successfully!");

        trialSignupService.submitAccountDetails(signupRequest(List.of(product(SAT_PRODUCT, SAT_PACKAGE))));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> templateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(notificationClient).sendEmailNotification(eq(EMAIL), any(), templateCaptor.capture());
        String otp = String.valueOf(templateCaptor.getValue().get("otp"));
        trialSignupService.verifyEmail(EmailVerificationRequestDto.builder()
                .email(EMAIL)
                .verificationCode(otp)
                .build());

        when(aspireUserRepository.existsByUsernameIgnoreCase(EMAIL)).thenReturn(true);

        ResourceAlreadyExistsException ex = assertThrows(ResourceAlreadyExistsException.class, () ->
                trialSignupService.createPassword(PasswordCreationRequestDto.builder()
                        .email(EMAIL)
                        .password(PASSWORD)
                        .confirmPassword(PASSWORD)
                        .productsData(List.of(product(SAT_PRODUCT, SAT_PACKAGE)))
                        .build()));
        assertTrue(ex.getMessage().contains("assign-products"));
    }

    @Test
    void assignTrialProducts_MixedList_AssignsOnlyNewProduct() {
        AspireUser existing = existingTrialUser();
        ClientAdmin admin = ClientAdmin.builder()
                .id(existing.getClientAdminId())
                .email(EMAIL)
                .onboardBy(OnboardBy.TRIAL)
                .clientProductIds(new ArrayList<>())
                .build();

        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(existing));
        when(clientAdminRepository.findById(existing.getClientAdminId())).thenReturn(Optional.of(admin));
        when(clientProductRepository.findByClientAdminIdAndProductIdAndPackageId(
                existing.getClientAdminId(), SAT_PRODUCT, SAT_PACKAGE))
                .thenReturn(activeProduct(SAT_PRODUCT, SAT_PACKAGE));
        when(clientProductRepository.findByClientAdminIdAndProductIdAndPackageId(
                existing.getClientAdminId(), PHISH_PRODUCT, PHISH_PACKAGE))
                .thenReturn(null);
        when(clientProductRepository.save(any(ClientProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(endUserPackageRepository.save(any(EndUserPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cmsServiceClient.createTrialSubPackage(any()))
                .thenReturn(SubPackageResponseDto.builder().id("sp-phish").name("Phishing Simulation Trial").build());

        TrialSignupResponseDto response = trialSignupService.assignTrialProducts(AssignTrialProductsRequestDto.builder()
                .email(EMAIL)
                .productsData(List.of(
                        product(SAT_PRODUCT, SAT_PACKAGE, "Security Awareness Training"),
                        product(PHISH_PRODUCT, PHISH_PACKAGE, "Phishing Simulation")))
                .build());

        assertEquals("Phishing Simulation", response.getTrialPackage());
        assertEquals(2, response.getProductsData().size());
        assertTrue(response.getProductsData().get(0).getExistingTrail());
        assertFalse(response.getProductsData().get(1).getExistingTrail());
        verify(clientProductRepository, times(1)).save(any(ClientProduct.class));
        verify(cmsServiceClient, times(1)).createTrialSubPackage(any());
    }

    @Test
    void assignTrialProducts_AllAlreadyAssigned_Throws() {
        AspireUser existing = existingTrialUser();
        ClientAdmin admin = ClientAdmin.builder()
                .id(existing.getClientAdminId())
                .email(EMAIL)
                .onboardBy(OnboardBy.TRIAL)
                .build();
        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(existing));
        when(clientAdminRepository.findById(existing.getClientAdminId())).thenReturn(Optional.of(admin));
        when(clientProductRepository.findByClientAdminIdAndProductIdAndPackageId(
                existing.getClientAdminId(), SAT_PRODUCT, SAT_PACKAGE))
                .thenReturn(activeProduct(SAT_PRODUCT, SAT_PACKAGE));

        assertThrows(ResourceAlreadyExistsException.class, () ->
                trialSignupService.assignTrialProducts(AssignTrialProductsRequestDto.builder()
                        .email(EMAIL)
                        .productsData(List.of(product(SAT_PRODUCT, SAT_PACKAGE)))
                        .build()));
        verify(clientProductRepository, never()).save(any(ClientProduct.class));
    }

    @Test
    void assignTrialProducts_AccountNotFound_Throws() {
        when(aspireUserRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                trialSignupService.assignTrialProducts(AssignTrialProductsRequestDto.builder()
                        .email(EMAIL)
                        .productsData(List.of(product(PHISH_PRODUCT, PHISH_PACKAGE)))
                        .build()));
    }

    private TrialSignupRequestDto signupRequest(List<ProductsData> productsData) {
        return TrialSignupRequestDto.builder()
                .email(EMAIL)
                .phoneNumber("+13619557331")
                .phoneCode("+1")
                .firstName("Judith")
                .lastName("Christian")
                .companyName("Pugh and Finley Plc")
                .numberOfEmployees("1-10 employees")
                .howDidYouHearAboutUs("word-of-mouth")
                .productsData(productsData)
                .build();
    }

    private ProductsData product(String productId, String packageId) {
        return product(productId, packageId, null);
    }

    private ProductsData product(String productId, String packageId, String productName) {
        return ProductsData.builder()
                .productId(productId)
                .packageId(packageId)
                .productName(productName)
                .build();
    }

    private AspireUser existingTrialUser() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        return AspireUser.builder()
                .id(userId)
                .userId(userId)
                .email(EMAIL)
                .username(EMAIL)
                .firstName("Judith")
                .lastName("Christian")
                .companyName("Pugh and Finley Plc")
                .clientAdminId(userId.toString())
                .isTrial(true)
                .build();
    }

    private ClientProduct activeProduct(String productId, String packageId) {
        ClientProduct product = new ClientProduct();
        product.setId(UUID.randomUUID().toString());
        product.setProductId(productId);
        product.setPackageId(packageId);
        product.setLicenseStatus("ACTIVE");
        return product;
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
}
