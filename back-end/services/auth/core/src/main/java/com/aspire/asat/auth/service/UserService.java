package com.aspire.asat.auth.service;

import com.aspire.asat.auth.dto.PurchaseProductDto;
import com.aspire.asat.auth.dto.UserDetailsResponse;
import com.aspire.asat.auth.dto.enums.ResponseMessage;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.exception.ResourceNotFoundException;
import com.aspire.asat.auth.mapper.UserMapper;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class UserService extends BaseService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RestTemplate restTemplate;

    @Value("${service.billing.url}")
    private String billingServiceUrl;

    @Value("${service.cms.url}")
    private String cmsServiceUrl;

    public UserDetailsResponse getCurrentUserDetails() {
        // Get current user context from JWT token
        CurrentUserContext currentUserContext = getCurrentUserContext();

        // Find the user by username from context
        Optional<AspireUser> userFromDb = userRepository.findByUsernameIgnoreCase(currentUserContext.getUsername());
        if (userFromDb.isEmpty()) {
            throw new ResourceNotFoundException(ResponseMessage.USER_NOT_FOUND.getResponseMessage());
        }

        AspireUser aspireUser = userFromDb.get();
        Boolean pendingPayment = null;
        List<String> clientProductTags = null;
        List<PurchaseProductDto> purchaseProducts = null;

        if (UserType.CLIENT_ADMIN.getValue().equals(aspireUser.getUserType())) {
            String clientAdminId = aspireUser.getUserId().toString();
            pendingPayment = checkPendingPaymentStatus(clientAdminId);
            List<AssignedProductTagDto> assignedProducts = fetchAssignedProducts(aspireUser);
            purchaseProducts = mapPurchaseProducts(assignedProducts);
            clientProductTags = collectClientProductTags(assignedProducts);
        }

        return userMapper.mapToResponse(aspireUser, pendingPayment, clientProductTags, purchaseProducts);
    }

    /**
     * Call billing service API to check if client admin has only PENDING invoices
     * @param clientAdminId The client admin ID
     * @return Boolean: true if only PENDING invoices, false if has PAID invoices, null if call fails
     */
    private Boolean checkPendingPaymentStatus(String clientAdminId) {
        try {
            String url = billingServiceUrl + "/invoice/check-pending-only/" + clientAdminId;
            logger.info("Calling billing service to check pending payment status for client admin: {}", clientAdminId);

            ParameterizedTypeReference<BillingApiResponse> responseType = 
                new ParameterizedTypeReference<BillingApiResponse>() {};
            
            ResponseEntity<BillingApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                responseType
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean hasOnlyPending = response.getBody().getData();
                logger.info("Billing service response for client admin {}: hasOnlyPending = {}", clientAdminId, hasOnlyPending);
                return hasOnlyPending;
            } else {
                logger.warn("Billing service returned non-success status for client admin: {}", clientAdminId);
                return null;
            }
        } catch (RestClientException e) {
            logger.error("Error calling billing service to check pending payment status for client admin: {}. Error: {}", 
                clientAdminId, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while checking pending payment status for client admin: {}. Error: {}", 
                clientAdminId, e.getMessage(), e);
            return null;
        }
    }

    private List<AssignedProductTagDto> fetchAssignedProducts(AspireUser aspireUser) {
        String clientAdminId = resolveClientAdminId(aspireUser);
        if (clientAdminId == null || clientAdminId.isBlank()) {
            return List.of();
        }

        try {
            String url = cmsServiceUrl + "/products/assigned/tags?clientAdminId=" + clientAdminId;
            ParameterizedTypeReference<CmsAssignedProductTagsApiResponse> responseType =
                    new ParameterizedTypeReference<CmsAssignedProductTagsApiResponse>() {};

            ResponseEntity<CmsAssignedProductTagsApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    responseType
            );

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null
                    || response.getBody().getData() == null) {
                return List.of();
            }

            return response.getBody().getData();
        } catch (Exception exception) {
            logger.warn("Unable to fetch assigned product tags for user: {}",
                    aspireUser.getUserId(), exception);
            return List.of();
        }
    }

    private List<PurchaseProductDto> mapPurchaseProducts(List<AssignedProductTagDto> assignedProducts) {
        if (assignedProducts == null || assignedProducts.isEmpty()) {
            return List.of();
        }
        return assignedProducts.stream()
                .map(product -> PurchaseProductDto.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .build())
                .toList();
    }

    private List<String> collectClientProductTags(List<AssignedProductTagDto> assignedProducts) {
        if (assignedProducts == null || assignedProducts.isEmpty()) {
            return List.of();
        }

        Set<String> uniqueTags = new LinkedHashSet<>();
        for (AssignedProductTagDto product : assignedProducts) {
            List<String> productTags = product.getTags();
            if (productTags == null || productTags.isEmpty()) {
                continue;
            }
            for (String tag : productTags) {
                if (tag != null && !tag.isBlank()) {
                    uniqueTags.add(tag.trim());
                }
            }
        }
        return new ArrayList<>(uniqueTags);
    }

    private String resolveClientAdminId(AspireUser aspireUser) {
        if (UserType.CLIENT_ADMIN.getValue().equals(aspireUser.getUserType())) {
            return aspireUser.getUserId() != null ? aspireUser.getUserId().toString() : null;
        }

        if (aspireUser.getClientAdminId() != null && !aspireUser.getClientAdminId().isBlank()) {
            return aspireUser.getClientAdminId();
        }

        return null;
    }

    /**
     * Inner class to represent billing service API response
     */
    @Data
    @NoArgsConstructor
    private static class BillingApiResponse {
        private String message;
        private int statusCode;
        private Boolean data;
    }

    @Data
    @NoArgsConstructor
    static class CmsAssignedProductTagsApiResponse {
        private String message;
        private int statusCode;
        private List<AssignedProductTagDto> data;
    }

    @Data
    @NoArgsConstructor
    static class AssignedProductTagDto {
        private String productId;
        private String productName;
        private List<String> tags;
    }
}
