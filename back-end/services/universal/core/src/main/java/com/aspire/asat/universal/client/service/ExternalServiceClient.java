package com.aspire.asat.universal.client.service;

import com.aspire.asat.universal.client.CommonHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Example service demonstrating how to use CommonHttpClient
 * This can be used as a template for creating service-specific clients
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalServiceClient {

    private final CommonHttpClient httpClient;

    // Example: Configure base URLs in application.yml
    @Value("${external.service.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Example: Fetch user data from another service
     * 
     * @param userId The user ID to fetch
     * @param token Optional authentication token
     * @return User data as a Map
     */
    public Map<String, Object> getUserById(String userId, String token) {
        String url = baseUrl + "/api/users/" + userId;
        return httpClient.get(url, Map.class, token);
    }

    /**
     * Example: Create a new resource in another service
     * 
     * @param requestBody The request payload
     * @param token Optional authentication token
     * @return Response data as a Map
     */
    public Map<String, Object> createResource(Object requestBody, String token) {
        String url = baseUrl + "/api/resources";
        return httpClient.post(url, requestBody, Map.class, token);
    }

    /**
     * Example: Update a resource in another service
     * 
     * @param resourceId The resource ID to update
     * @param requestBody The request payload
     * @param token Optional authentication token
     * @return Response data as a Map
     */
    public Map<String, Object> updateResource(String resourceId, Object requestBody, String token) {
        String url = baseUrl + "/api/resources/" + resourceId;
        return httpClient.put(url, requestBody, Map.class, token);
    }

    /**
     * Example: Delete a resource in another service
     * 
     * @param resourceId The resource ID to delete
     * @param token Optional authentication token
     * @return Response data as a Map
     */
    public Map<String, Object> deleteResource(String resourceId, String token) {
        String url = baseUrl + "/api/resources/" + resourceId;
        return httpClient.delete(url, Map.class, token);
    }

    /**
     * Example: Search with query parameters
     * 
     * @param queryParams Search parameters
     * @param token Optional authentication token
     * @return Search results as a Map
     */
    public Map<String, Object> search(Map<String, String> queryParams, String token) {
        String url = baseUrl + "/api/search";
        return httpClient.get(url, Map.class, token, queryParams);
    }
}

