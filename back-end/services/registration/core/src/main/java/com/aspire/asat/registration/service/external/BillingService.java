package com.aspire.asat.registration.service.external;

import com.aspire.asat.registration.data.coupon.CouponResponseDTO;
import com.aspire.asat.registration.data.mspUser.request.CreditCreateRequestDTO;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for handling billing-related external service calls for MSP onboarding.
 * This service encapsulates all interactions with the billing service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${service.billing.url}")
    private String billingServiceUrl;

    /**
     * Creates a credit for the MSP user if credit information is provided.
     *
     * @param clientId The client ID for whom the credit is being created
     * @param creditAmount The amount of credit to be created
     * @param reason The reason for creating the credit
     * @param creditStartDate The start date for the credit (optional, defaults to current date)
     * @param creditEndDate The end/expiration date for the credit (optional, defaults to 1 year from start)
     * @return The ID of the created credit
     * @throws RegistrationServiceException if credit creation fails
     */
    public String creatCreditForMsp(String clientId, Double creditAmount, String reason,
                                    Date creditStartDate, Date creditEndDate) {
        log.info("Creating credit for client: {} with amount: {}, start date: {}, end date: {}", 
                clientId, creditAmount, creditStartDate, creditEndDate);
        
        CreditCreateRequestDTO creditRequestDTO = buildCreditRequestDTO(
                clientId, creditAmount, reason, creditStartDate, creditEndDate);
        
        try {
            JsonNode creditResponse = webClient.post()
                    .uri(billingServiceUrl + "/credit/create")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(creditRequestDTO)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(json -> {
                        if (json.get("data") == null || json.get("data").isNull()) {
                            throw new RegistrationServiceException("Credit creation failed: empty response");
                        }
                        return json;
                    })
                    .block();

            String creditId = creditResponse.get("data").get("id").asText();
            log.info("Credit created successfully for MSP: {}", creditId);
            return creditId;
            
        } catch (Exception e) {
            log.error("Failed to create credit for client: {}", clientId, e);
            throw new RegistrationServiceException("Credit creation failed", e);
        }
    }

    /**
     * Builds the credit creation request DTO using provided dates or defaults.
     *
     * @param clientId The client ID
     * @param creditAmount The credit amount
     * @param reason The reason for the credit
     * @param creditStartDate The start date (optional, defaults to current date)
     * @param creditEndDate The end date (optional, defaults to 1 year from start date)
     * @return The built CreditCreateRequestDTO
     */
    private CreditCreateRequestDTO buildCreditRequestDTO(String clientId, Double creditAmount, String reason,
                                                         java.util.Date creditStartDate, java.util.Date creditEndDate) {
        CreditCreateRequestDTO creditRequestDTO = new CreditCreateRequestDTO();
        creditRequestDTO.setClientId(clientId);
        creditRequestDTO.setCreditAmount(creditAmount);
        creditRequestDTO.setReason(reason);
        creditRequestDTO.setAddedBy("system-onboarding");

        // Use provided end date, or default to 1 year from start date, or 1 year from now
        if (creditEndDate != null) {
            creditRequestDTO.setExpirationDate(creditEndDate);
        } else if (creditStartDate != null) {
            // If start date is provided but end date is not, set end date to 1 year from start
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(creditStartDate);
            calendar.add(Calendar.YEAR, 1);
            creditRequestDTO.setExpirationDate(calendar.getTime());
        } else {
            // Default: 1 year from now
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 1);
            creditRequestDTO.setExpirationDate(calendar.getTime());
        }

        return creditRequestDTO;
    }

    /**
     * Gets coupon details by coupon code from billing service
     *
     * @param couponCode The coupon code to look up
     * @return CouponResponseDTO containing coupon details
     * @throws RegistrationServiceException if coupon retrieval fails
     */
    public CouponResponseDTO getCouponByCode(String couponCode) {
        log.info("Fetching coupon details for code: {}", couponCode);
        
        try {
            CouponResponseDTO couponResponse = webClient.get()
                    .uri(billingServiceUrl + "/coupon/code/" + couponCode)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(json -> {
                        // Handle both wrapped (data) and direct response formats
                        JsonNode dataNode = json.get("data");
                        JsonNode targetNode = (dataNode != null && !dataNode.isNull()) ? dataNode : json;
                        
                        if (targetNode == null || targetNode.isNull()) {
                            throw new RegistrationServiceException("Coupon retrieval failed: empty response");
                        }
                        try {
                            return objectMapper.treeToValue(targetNode, CouponResponseDTO.class);
                        } catch (Exception e) {
                            throw new RegistrationServiceException("Error parsing coupon response", e);
                        }
                    })
                    .block();

            log.info("Coupon retrieved successfully: {}", couponResponse.getCode());
            return couponResponse;

        } catch (WebClientResponseException e) {
            String billingMessage = extractBillingErrorMessage(e.getResponseBodyAsString());
            log.error("Billing coupon lookup failed. Status: {}, Message: {}",
                    e.getStatusCode(), billingMessage);
            throw new RegistrationServiceException(
                    billingMessage != null && !billingMessage.isBlank()
                            ? billingMessage
                            : "Coupon retrieval failed: " + e.getStatusCode(),
                    e);
        } catch (RegistrationServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve coupon for code: {}", couponCode, e);
            throw new RegistrationServiceException("Coupon retrieval failed: " + e.getMessage(), e);
        }
    }

    private String extractBillingErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            if (node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception e) {
            log.debug("Could not parse billing error response: {}", e.getMessage());
        }
        return responseBody;
    }

    /**
     * Updates an existing credit for the MSP user.
     *
     * @param creditId The ID of the credit to update
     * @param creditAmount The new credit amount (optional, pass null to keep existing)
     * @param reason The reason for the update (optional, pass null to keep existing)
     * @param creditStartDate The new start date (optional, pass null to keep existing)
     * @param creditEndDate The new end date (optional, pass null to keep existing)
     * @throws RegistrationServiceException if credit update fails
     */
    public void updateCreditForMsp(String creditId, java.math.BigDecimal creditAmount, String reason,
                                   Date creditStartDate, Date creditEndDate) {
        log.info("Updating credit: {} with amount: {}, reason: {}, start date: {}, end date: {}",
                creditId, creditAmount, reason, creditStartDate, creditEndDate);

        try {
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("creditId", creditId);

            if (creditAmount != null) {
                updateRequest.put("creditAmount", creditAmount.doubleValue());
            }
            if (reason != null) {
                updateRequest.put("reason", reason);
            }
            if (creditStartDate != null) {
                updateRequest.put("startDate", creditStartDate);
            }
            if (creditEndDate != null) {
                updateRequest.put("expirationDate", creditEndDate);
            }

            webClient.put()
                    .uri(billingServiceUrl + "/credit/" + creditId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(updateRequest)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            log.info("Credit updated successfully: {}", creditId);

        } catch (Exception e) {
            log.error("Failed to update credit: {}", creditId, e);
            throw new RegistrationServiceException("Credit update failed: " + e.getMessage(), e);
        }
    }

}
