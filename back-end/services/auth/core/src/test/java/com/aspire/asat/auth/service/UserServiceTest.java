package com.aspire.asat.auth.service;

import com.aspire.asat.auth.dto.PurchaseProductDto;
import com.aspire.asat.auth.dto.UserDetailsResponse;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.logger.ServiceLogger;
import com.aspire.asat.auth.mapper.UserMapper;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ServiceLogger logger;

    private UserService userService;

    private UUID userId;
    private AspireUser clientAdmin;

    @BeforeEach
    void setUp() throws Exception {
        userService = spy(new UserService(userRepository, userMapper, restTemplate));
        userService.setLogger(logger);
        setField("billingServiceUrl", "http://billing");
        setField("cmsServiceUrl", "http://cms");

        userId = UUID.randomUUID();
        clientAdmin = AspireUser.builder()
                .userId(userId)
                .username("admin@example.com")
                .email("admin@example.com")
                .userType(UserType.CLIENT_ADMIN.getValue())
                .status("ACTIVE")
                .build();

        when(userMapper.mapToResponse(any(), nullable(Boolean.class), nullable(List.class), nullable(List.class)))
                .thenAnswer(invocation -> UserDetailsResponse.builder()
                        .pendingPayment(invocation.getArgument(1))
                        .clientProductTags(invocation.getArgument(2))
                        .purchaseProducts(invocation.getArgument(3))
                        .build());
    }

    @Test
    void getCurrentUserDetails_ClientAdminWithAssignedProducts_MapsPurchaseProductsAndTags() {
        stubCurrentUser(clientAdmin);
        UserService.AssignedProductTagDto phishing = assignedProduct("prod-1", "Phishing Awareness", List.of("phishing", "awareness"));
        UserService.AssignedProductTagDto security = assignedProduct("prod-2", "Security Essentials", List.of("security", "phishing"));
        stubCmsAssignedProducts(userId.toString(), List.of(phishing, security));

        UserDetailsResponse response = userService.getCurrentUserDetails();

        assertNotNull(response.getPurchaseProducts());
        assertEquals(2, response.getPurchaseProducts().size());
        assertEquals("prod-1", response.getPurchaseProducts().get(0).getProductId());
        assertEquals("Phishing Awareness", response.getPurchaseProducts().get(0).getProductName());
        assertEquals("prod-2", response.getPurchaseProducts().get(1).getProductId());
        assertEquals("Security Essentials", response.getPurchaseProducts().get(1).getProductName());
        assertEquals(List.of("phishing", "awareness", "security"), response.getClientProductTags());
        verifyMapperPurchaseProducts(2);
    }

    @Test
    void getCurrentUserDetails_NonClientAdmin_PurchaseProductsNullAndCmsNotCalled() {
        AspireUser endUser = AspireUser.builder()
                .userId(UUID.randomUUID())
                .username("user@example.com")
                .email("user@example.com")
                .userType(UserType.USER.getValue())
                .clientAdminId(userId.toString())
                .status("ACTIVE")
                .build();
        stubCurrentUser(endUser);

        UserDetailsResponse response = userService.getCurrentUserDetails();

        assertNull(response.getPurchaseProducts());
        assertNull(response.getClientProductTags());
        verify(restTemplate, never()).exchange(
                contains("/products/assigned/tags"),
                any(HttpMethod.class),
                any(),
                any(ParameterizedTypeReference.class)
        );
        verify(userMapper).mapToResponse(eq(endUser), isNull(), isNull(), isNull());
    }

    @Test
    void getCurrentUserDetails_ClientAdminEmptyCmsData_ReturnsEmptyLists() {
        stubCurrentUser(clientAdmin);
        stubCmsAssignedProducts(userId.toString(), List.of());

        UserDetailsResponse response = userService.getCurrentUserDetails();

        assertNotNull(response.getPurchaseProducts());
        assertTrue(response.getPurchaseProducts().isEmpty());
        assertNotNull(response.getClientProductTags());
        assertTrue(response.getClientProductTags().isEmpty());
    }

    @Test
    void getCurrentUserDetails_ClientAdminCmsFailure_ReturnsEmptyLists() {
        stubCurrentUser(clientAdmin);
        when(restTemplate.exchange(
                eq(cmsUrl(userId.toString())),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("CMS unavailable"));

        UserDetailsResponse response = userService.getCurrentUserDetails();

        assertNotNull(response.getPurchaseProducts());
        assertTrue(response.getPurchaseProducts().isEmpty());
        assertNotNull(response.getClientProductTags());
        assertTrue(response.getClientProductTags().isEmpty());
    }

    private void stubCurrentUser(AspireUser user) {
        CurrentUserContext context = CurrentUserContext.builder()
                .username(user.getUsername())
                .userType(user.getUserType())
                .build();
        doReturn(context).when(userService).getCurrentUserContext();
        when(userRepository.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
    }

    @SuppressWarnings("unchecked")
    private void stubCmsAssignedProducts(String clientAdminId, List<UserService.AssignedProductTagDto> products) {
        UserService.CmsAssignedProductTagsApiResponse body = new UserService.CmsAssignedProductTagsApiResponse();
        body.setData(products);
        when(restTemplate.exchange(
                eq(cmsUrl(clientAdminId)),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }

    private void verifyMapperPurchaseProducts(int expectedSize) {
        ArgumentCaptor<List<PurchaseProductDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(userMapper).mapToResponse(eq(clientAdmin), nullable(Boolean.class), any(), captor.capture());
        assertEquals(expectedSize, captor.getValue().size());
    }

    private static UserService.AssignedProductTagDto assignedProduct(String productId, String productName, List<String> tags) {
        UserService.AssignedProductTagDto dto = new UserService.AssignedProductTagDto();
        dto.setProductId(productId);
        dto.setProductName(productName);
        dto.setTags(tags);
        return dto;
    }

    private static String cmsUrl(String clientAdminId) {
        return "http://cms/products/assigned/tags?clientAdminId=" + clientAdminId;
    }

    private void setField(String name, String value) throws Exception {
        Field field = UserService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(userService, value);
    }
}
