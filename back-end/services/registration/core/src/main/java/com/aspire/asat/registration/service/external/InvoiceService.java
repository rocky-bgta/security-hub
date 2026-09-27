package com.aspire.asat.registration.service.external;

import com.aspire.asat.common.constants.InternalServiceAuthConstants;
import com.aspire.asat.registration.data.invoice.InvoiceRequestDTO;
import com.aspire.asat.registration.data.invoice.InvoiceResponseDTO;
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

/**
 * Service responsible for handling invoice-related external service calls.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${service.billing.url}")
    private String billingServiceUrl;

    @Value("${internal.service.api-key}")
    private String internalServiceApiKey;

    /**
     * Creates an invoice via the billing service.
     *
     * @param invoiceRequestDTO The invoice request containing all necessary details
     * @return The created invoice response with invoice ID and details
     * @throws RegistrationServiceException if invoice creation fails
     */
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO invoiceRequestDTO) {
        log.info("Creating invoice via billing service for client ID: {}", invoiceRequestDTO.getClientAdminId());

        try {
            InvoiceResponseDTO invoiceResponse = webClient.post()
                    .uri(billingServiceUrl + "/invoice/create")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(InternalServiceAuthConstants.HEADER_NAME, internalServiceApiKey)
                    .bodyValue(invoiceRequestDTO)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(this::parseInvoiceResponse)
                    .block();

            log.info("Invoice created successfully: {}", invoiceResponse.getId());
            return invoiceResponse;

        } catch (WebClientResponseException e) {
            String billingMessage = extractBillingErrorMessage(e.getResponseBodyAsString());
            log.error("Billing invoice creation failed. Status: {}, Message: {}",
                    e.getStatusCode(), billingMessage);
            throw new RegistrationServiceException(
                    billingMessage != null && !billingMessage.isBlank()
                            ? billingMessage
                            : "Invoice creation failed: " + e.getStatusCode(),
                    e);
        } catch (RegistrationServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create invoice for client ID: {}", invoiceRequestDTO.getClientAdminId(), e);
            throw new RegistrationServiceException("Invoice creation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates an invoice for MSP onboarding.
     */
    public InvoiceResponseDTO createInvoiceForMsp(InvoiceRequestDTO invoiceRequestDTO) {
        return createInvoice(invoiceRequestDTO);
    }

    private InvoiceResponseDTO parseInvoiceResponse(JsonNode json) {
        JsonNode dataNode = json.get("data");
        if (dataNode == null || dataNode.isNull()) {
            throw new RegistrationServiceException("Invoice creation failed: empty response");
        }
        try {
            return objectMapper.treeToValue(dataNode, InvoiceResponseDTO.class);
        } catch (Exception e) {
            throw new RegistrationServiceException("Error parsing invoice response", e);
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
}
