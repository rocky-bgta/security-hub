package com.aspire.asat.cms.client.service;

import com.aspire.asat.cms.dto.exam.ClientAdminCountryAndMsp;
import com.aspire.asat.cms.dto.registration.RegistrationClientProductDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client service to communicate with Registration service for ClientAdmin data
 * Caches ClientAdmin information to reduce API calls
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAdminServiceClient {

    @Value("${service.registration.url}")
    private String registrationServiceUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // Cache for ClientAdmin data (key: clientAdminId, value: ClientAdmin data as Map)
    private final Map<String, Map<String, Object>> clientAdminCache = new ConcurrentHashMap<>();

    /**
     * Fetch all ClientAdmins from Registration service and cache them
     * Populates clientAdminCache with id as key
     * If cache already has data, returns existing cache without fetching
     * 
     * @return Map of cached ClientAdmins (id -> ClientAdmin data)
     */
    public Map<String, Map<String, Object>> fetchAndCacheAllClientAdmins() {
        return fetchAndCacheAllClientAdmins(false);
    }

    /**
     * Fetch all ClientAdmins from Registration service and cache them
     * Populates clientAdminCache with id as key
     * If cache already has data and forceRefresh is false, returns existing cache without fetching
     * 
     * @param forceRefresh if true, clears cache and fetches fresh data; if false, returns existing cache if available
     * @return Map of cached ClientAdmins (id -> ClientAdmin data)
     */
    public Map<String, Map<String, Object>> fetchAndCacheAllClientAdmins(boolean forceRefresh) {
        // Check if cache already has data and forceRefresh is false
        if (!forceRefresh && !clientAdminCache.isEmpty()) {
            log.info("ClientAdmin cache already has {} entries. Returning existing cache. Use forceRefresh=true to refresh.", 
                    clientAdminCache.size());
            return clientAdminCache;
        }

        // Clear cache if force refresh is requested
        if (forceRefresh) {
            log.info("Force refresh requested. Clearing existing cache.");
            clientAdminCache.clear();
        }

        try {
            log.info("Fetching all ClientAdmins from Registration service to populate clientAdminCache");

            int maxPageSize = 10000; // Large page size to fetch all at once
            int currentOffset = 0;
            boolean hasMore = true;
            int totalFetched = 0;

            while (hasMore) {
                String url = registrationServiceUrl + "/client/admin/list?offset=" + currentOffset + "&pageSize=" + maxPageSize;
                log.debug("Fetching ClientAdmins from: {}", url);

                JsonNode response = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block();

                if (response == null || response.get("data") == null) {
                    log.warn("No data field in registration service response");
                    break;
                }

                JsonNode dataNode = response.get("data");
                JsonNode clientAdminsNode = dataNode.get("clientAdmins");

                if (clientAdminsNode == null || !clientAdminsNode.isArray()) {
                    log.warn("Missing or invalid clientAdmins array in registration service response");
                    break;
                }

                // Parse and cache each ClientAdmin
                for (JsonNode clientAdminNode : clientAdminsNode) {
                    String id = clientAdminNode.get("id") != null ? clientAdminNode.get("id").asText() : null;
                    if (id != null && !id.isBlank()) {
                        Map<String, Object> clientAdminData = parseClientAdminToMap(clientAdminNode);
                        clientAdminCache.put(id, clientAdminData);
                        totalFetched++;
                        log.debug("Cached ClientAdmin: {}", id);
                    }
                }

                // Check if there are more pages
                JsonNode hasNextNode = dataNode.get("hasNext");
                hasMore = hasNextNode != null && hasNextNode.asBoolean(false);
                currentOffset += maxPageSize;

                log.debug("Fetched {} ClientAdmins in this batch. Has more: {}", 
                        clientAdminsNode.size(), hasMore);
            }

            log.info("Successfully cached {} ClientAdmins in clientAdminCache", totalFetched);
            return clientAdminCache;

        } catch (WebClientResponseException e) {
            log.error("Error fetching all ClientAdmins from Registration service. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Unexpected error fetching all ClientAdmins from Registration service", e);
        }

        return clientAdminCache;
    }

    /**
     * Resolve client admin IDs belonging to an MSP from the cached ClientAdmin list.
     *
     * @param mspId MSP user/org ID
     * @return list of client admin IDs (possibly empty)
     */
    public List<String> getClientAdminIdsByMspId(String mspId) {
        if (mspId == null || mspId.isBlank()) {
            return List.of();
        }
        Map<String, Map<String, Object>> all = fetchAndCacheAllClientAdmins(false);
        List<String> ids = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : all.entrySet()) {
            Object cachedMspId = entry.getValue() != null ? entry.getValue().get("mspId") : null;
            if (cachedMspId != null && mspId.equals(cachedMspId.toString())) {
                ids.add(entry.getKey());
            }
        }
        log.info("Resolved {} client admin IDs for mspId={}", ids.size(), mspId);
        return ids;
    }

    /**
     * Get ClientAdmin from cache by id
     * If cache is empty, automatically fetches all ClientAdmins first
     *
     * @param id The ClientAdmin ID
     * @return ClientAdmin data as Map or null if not found
     */
    public Map<String, Object> getClientAdminFromCache(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.warn("ClientAdmin ID is null or empty");
            return null;
        }

        // If cache is empty, fetch and populate it
        if (clientAdminCache.isEmpty()) {
            log.info("ClientAdmin cache is empty, fetching all ClientAdmins from Registration service");
            fetchAndCacheAllClientAdmins(false);
        }

        Map<String, Object> clientAdmin = clientAdminCache.get(id);
        if (clientAdmin == null) {
            log.debug("ClientAdmin with id {} not found in cache, attempting to refresh cache", id);
            // Try refreshing cache once (force refresh)
            fetchAndCacheAllClientAdmins(true);
            clientAdmin = clientAdminCache.get(id);
        }

        return clientAdmin;
    }

    /**
     * Check if ClientAdmin exists in cache by id
     * Does not trigger automatic fetch if cache is empty
     *
     * @param id The ClientAdmin ID
     * @return true if ClientAdmin exists in cache, false otherwise
     */
    public boolean isClientAdminCached(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return clientAdminCache.containsKey(id);
    }

    /**
     * Check if cache is populated (has any entries)
     *
     * @return true if cache has entries, false if empty
     */
    public boolean isCachePopulated() {
        return !clientAdminCache.isEmpty();
    }

    /**
     * Clear the ClientAdmin cache
     */
    public void clearCache() {
        clientAdminCache.clear();
        log.info("ClientAdmin cache cleared");
    }

    /**
     * Get the current cache size
     *
     * @return Number of ClientAdmins in cache
     */
    public int getCacheSize() {
        return clientAdminCache.size();
    }


    /**
     * Fetch ClientAdmin by ID from Registration service and return only countryId and mspId.
     * Makes a REST call to /registration/api/v1/client/admin/{clientAdminId}
     *
     * @param clientAdminId The client admin ID
     * @return ClientAdminCountryAndMsp containing countryId and mspId, or null if not found/error
     */
    public ClientAdminCountryAndMsp getClientAdminCountryAndMsp(String clientAdminId) {
        if (clientAdminId == null || clientAdminId.isBlank()) {
            log.warn("Client admin ID is null or empty");
            return null;
        }

        try {
            String url = registrationServiceUrl + "/client/admin/" + clientAdminId;
            log.debug("Fetching ClientAdmin countryId and mspId from registration service: {}", url);

            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data field in registration service response for clientAdminId: {}", clientAdminId);
                return null;
            }

            JsonNode dataNode = response.get("data");
            
            // Extract countryId - check for "country" field (which is used as countryId in the codebase)
            String countryId = null;
            if (dataNode.get("country") != null && !dataNode.get("country").isNull()) {
                countryId = dataNode.get("country").asText();
            } else if (dataNode.get("countryId") != null && !dataNode.get("countryId").isNull()) {
                countryId = dataNode.get("countryId").asText();
            }
            
            // Extract mspId
            String mspId = null;
            if (dataNode.get("mspId") != null && !dataNode.get("mspId").isNull()) {
                mspId = dataNode.get("mspId").asText();
            }

            log.debug("Fetched countryId: {} and mspId: {} for clientAdminId: {}", countryId, mspId, clientAdminId);
            return new ClientAdminCountryAndMsp(countryId, mspId);

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Client admin not found in registration service: {}", clientAdminId);
            return null;
        } catch (WebClientResponseException e) {
            log.error("Error calling registration service for clientAdminId: {}. Status: {}, Response: {}", 
                    clientAdminId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching ClientAdmin countryId and mspId for clientAdminId: {}", clientAdminId, e);
            return null;
        }
    }

    /**
     * Parses a ClientAdmin JsonNode to a Map for caching.
     * Includes all fields from ClientAdminWithProductsResponseDto
     */
    private Map<String, Object> parseClientAdminToMap(JsonNode clientAdminNode) {
        Map<String, Object> map = new HashMap<>();
        
        if (clientAdminNode.get("id") != null) map.put("id", clientAdminNode.get("id").asText());
        if (clientAdminNode.get("email") != null) map.put("email", clientAdminNode.get("email").asText());
        if (clientAdminNode.get("organizationName") != null) map.put("organizationName", clientAdminNode.get("organizationName").asText());
        if (clientAdminNode.get("contactEmail") != null) map.put("contactEmail", clientAdminNode.get("contactEmail").asText());
        if (clientAdminNode.get("phoneNumber") != null) map.put("phoneNumber", clientAdminNode.get("phoneNumber").asText());
        if (clientAdminNode.get("billingName") != null) map.put("billingName", clientAdminNode.get("billingName").asText());
        if (clientAdminNode.get("billingEmail") != null) map.put("billingEmail", clientAdminNode.get("billingEmail").asText());
        if (clientAdminNode.get("billingUseSameAsOrganizationAddress") != null) 
            map.put("billingUseSameAsOrganizationAddress", clientAdminNode.get("billingUseSameAsOrganizationAddress").asBoolean());
        if (clientAdminNode.get("billingStreetAddress") != null) map.put("billingStreetAddress", clientAdminNode.get("billingStreetAddress").asText());
        if (clientAdminNode.get("billingStreetAddressLine2") != null) map.put("billingStreetAddressLine2", clientAdminNode.get("billingStreetAddressLine2").asText());
        if (clientAdminNode.get("billingCity") != null) map.put("billingCity", clientAdminNode.get("billingCity").asText());
        if (clientAdminNode.get("billingZipPostalCode") != null) map.put("billingZipPostalCode", clientAdminNode.get("billingZipPostalCode").asText());
        if (clientAdminNode.get("billingCountry") != null) map.put("billingCountry", clientAdminNode.get("billingCountry").asText());
        if (clientAdminNode.get("billingStateProvince") != null) map.put("billingStateProvince", clientAdminNode.get("billingStateProvince").asText());
        if (clientAdminNode.get("mspId") != null) map.put("mspId", clientAdminNode.get("mspId").asText());
        if (clientAdminNode.get("country") != null) map.put("country", clientAdminNode.get("country").asText());
        if (clientAdminNode.get("countryCode") != null) map.put("countryCode", clientAdminNode.get("countryCode").asText());
        if (clientAdminNode.get("state") != null) map.put("state", clientAdminNode.get("state").asText());
        if (clientAdminNode.get("stateCode") != null) map.put("stateCode", clientAdminNode.get("stateCode").asText());
        if (clientAdminNode.get("timeZone") != null) map.put("timeZone", clientAdminNode.get("timeZone").asText());
        if (clientAdminNode.get("language") != null) map.put("language", clientAdminNode.get("language").asText());
        if (clientAdminNode.get("industry") != null) map.put("industry", clientAdminNode.get("industry").asText());
        if (clientAdminNode.get("domain") != null) map.put("domain", clientAdminNode.get("domain").asText());
        if (clientAdminNode.get("organizationSize") != null) map.put("organizationSize", clientAdminNode.get("organizationSize").asText());
        if (clientAdminNode.get("organizationType") != null) map.put("organizationType", clientAdminNode.get("organizationType").asText());
        if (clientAdminNode.get("streetAddress") != null) map.put("streetAddress", clientAdminNode.get("streetAddress").asText());
        if (clientAdminNode.get("streetAddressLine2") != null) map.put("streetAddressLine2", clientAdminNode.get("streetAddressLine2").asText());
        if (clientAdminNode.get("city") != null) map.put("city", clientAdminNode.get("city").asText());
        if (clientAdminNode.get("zipPostalCode") != null) map.put("zipPostalCode", clientAdminNode.get("zipPostalCode").asText());
        if (clientAdminNode.get("logoUrl") != null) map.put("logoUrl", clientAdminNode.get("logoUrl").asText());
        if (clientAdminNode.get("status") != null) {
            JsonNode statusNode = clientAdminNode.get("status");
            // Handle both string and object status
            if (statusNode.isTextual()) {
                map.put("status", statusNode.asText());
            } else if (statusNode.isObject() && statusNode.get("name") != null) {
                map.put("status", statusNode.get("name").asText());
            }
        }
        if (clientAdminNode.get("createdAt") != null) {
            try {
                String createdAtStr = clientAdminNode.get("createdAt").asText();
                map.put("createdAt", createdAtStr);
                // Also parse as Instant for easier use
                map.put("createdAtInstant", Instant.parse(createdAtStr));
            } catch (Exception e) {
                log.debug("Failed to parse createdAt: {}", clientAdminNode.get("createdAt").asText());
            }
        }
        if (clientAdminNode.get("clientAdminId") != null) map.put("clientAdminId", clientAdminNode.get("clientAdminId").asText());
        if (clientAdminNode.get("creditId") != null) map.put("creditId", clientAdminNode.get("creditId").asText());
        if (clientAdminNode.get("tierId") != null) map.put("tierId", clientAdminNode.get("tierId").asText());
        if (clientAdminNode.get("mspType") != null) map.put("mspType", clientAdminNode.get("mspType").asText());
        if (clientAdminNode.get("mspAdminEmail") != null) map.put("mspAdminEmail", clientAdminNode.get("mspAdminEmail").asText());
        if (clientAdminNode.get("netDays") != null) map.put("netDays", clientAdminNode.get("netDays").asInt());
        if (clientAdminNode.get("department") != null) map.put("department", clientAdminNode.get("department").asText());
        
        return map;
    }

    /**
     * Fetch ACTIVE client products for a client admin from Registration service.
     * Calls GET /client/admin/products?clientAdminId={clientAdminId}
     */
    public List<RegistrationClientProductDto> getClientProductsByClientAdminId(String clientAdminId) {
        if (clientAdminId == null || clientAdminId.isBlank()) {
            log.warn("Client admin ID is null or empty");
            return List.of();
        }

        try {
            String url = registrationServiceUrl + "/client/admin/products?clientAdminId=" + clientAdminId.trim();
            log.info("Fetching client products from registration service for clientAdminId: {}", clientAdminId);

            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data in registration response for client products, clientAdminId: {}", clientAdminId);
                return List.of();
            }

            JsonNode productsNode = response.get("data").get("clientProductDTOS");
            if (productsNode == null || productsNode.isNull()) {
                log.warn("No clientProductDTOS in registration response for clientAdminId: {}", clientAdminId);
                return List.of();
            }

            List<RegistrationClientProductDto> products = objectMapper.convertValue(
                    productsNode,
                    new TypeReference<List<RegistrationClientProductDto>>() {});

            log.info("Retrieved {} client products from registration for clientAdminId: {}",
                    products != null ? products.size() : 0, clientAdminId);
            return products != null ? products : List.of();
        } catch (WebClientResponseException e) {
            log.error("Registration service error fetching client products for clientAdminId: {}. Status: {}, Response: {}",
                    clientAdminId, e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error fetching client products from registration for clientAdminId: {}", clientAdminId, e);
            return List.of();
        }
    }
}

