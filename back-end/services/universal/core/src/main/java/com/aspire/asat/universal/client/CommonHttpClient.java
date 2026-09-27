package com.aspire.asat.universal.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Common HTTP Client for making service-to-service calls
 * Supports optional token authentication and various HTTP methods
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonHttpClient {

    private final WebClient webClient;
    
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Performs a GET request
     *
     * @param url          The target URL
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T get(String url, Class<T> responseType, String token) {
        return executeRequest(url, HttpMethod.GET, null, responseType, token, null);
    }

    /**
     * Performs a GET request without token
     *
     * @param url          The target URL
     * @param responseType The expected response class type
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T get(String url, Class<T> responseType) {
        return get(url, responseType, null);
    }

    /**
     * Performs a GET request with query parameters
     *
     * @param url          The target URL
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param queryParams  Query parameters
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T get(String url, Class<T> responseType, String token, Map<String, String> queryParams) {
        return executeRequest(url, HttpMethod.GET, null, responseType, token, queryParams);
    }

    /**
     * Performs a POST request
     *
     * @param url          The target URL
     * @param requestBody  The request body
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T post(String url, Object requestBody, Class<T> responseType, String token) {
        return executeRequest(url, HttpMethod.POST, requestBody, responseType, token, null);
    }

    /**
     * Performs a POST request without token
     *
     * @param url          The target URL
     * @param requestBody  The request body
     * @param responseType The expected response class type
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T post(String url, Object requestBody, Class<T> responseType) {
        return post(url, requestBody, responseType, null);
    }

    /**
     * Performs a PUT request
     *
     * @param url          The target URL
     * @param requestBody  The request body
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T put(String url, Object requestBody, Class<T> responseType, String token) {
        return executeRequest(url, HttpMethod.PUT, requestBody, responseType, token, null);
    }

    /**
     * Performs a PUT request without token
     *
     * @param url          The target URL
     * @param requestBody  The request body
     * @param responseType The expected response class type
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T put(String url, Object requestBody, Class<T> responseType) {
        return put(url, requestBody, responseType, null);
    }

    /**
     * Performs a PATCH request
     *
     * @param url          The target URL
     * @param requestBody  The request body
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T patch(String url, Object requestBody, Class<T> responseType, String token) {
        return executeRequest(url, HttpMethod.PATCH, requestBody, responseType, token, null);
    }

    /**
     * Performs a PATCH request without token
     *
     * @param url          The target URL
     * @param requestBody  The request body
     * @param responseType The expected response class type
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T patch(String url, Object requestBody, Class<T> responseType) {
        return patch(url, requestBody, responseType, null);
    }

    /**
     * Performs a DELETE request
     *
     * @param url          The target URL
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T delete(String url, Class<T> responseType, String token) {
        return executeRequest(url, HttpMethod.DELETE, null, responseType, token, null);
    }

    /**
     * Performs a DELETE request without token
     *
     * @param url          The target URL
     * @param responseType The expected response class type
     * @param <T>          The response type
     * @return The response object
     */
    public <T> T delete(String url, Class<T> responseType) {
        return delete(url, responseType, null);
    }

    /**
     * Core method to execute HTTP requests
     *
     * @param url          The target URL
     * @param method       The HTTP method
     * @param requestBody  The request body (optional)
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param queryParams  Optional query parameters
     * @param <T>          The response type
     * @return The response object
     */
    private <T> T executeRequest(String url, HttpMethod method, Object requestBody, 
                                  Class<T> responseType, String token, Map<String, String> queryParams) {
        try {
            log.info("Making {} request to: {}", method, url);
            
            WebClient.RequestBodySpec requestSpec = webClient
                    .method(method)
                    .uri(uriBuilder -> {
                        uriBuilder.path(url);
                        if (queryParams != null && !queryParams.isEmpty()) {
                            queryParams.forEach(uriBuilder::queryParam);
                        }
                        return uriBuilder.build();
                    })
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            // Add authorization token if provided
            if (token != null && !token.isEmpty()) {
                requestSpec.header(HttpHeaders.AUTHORIZATION, token.startsWith("Bearer ") ? token : "Bearer " + token);
                log.debug("Added authorization header to request");
            }

            // Add request body for POST, PUT, PATCH methods
            WebClient.ResponseSpec responseSpec;
            if (requestBody != null && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {
                responseSpec = requestSpec.bodyValue(requestBody).retrieve();
            } else {
                responseSpec = requestSpec.retrieve();
            }

            T response = responseSpec
                    .bodyToMono(responseType)
                    .timeout(DEFAULT_TIMEOUT)
                    .block();

            log.info("Successfully received response from: {}", url);
            return response;

        } catch (WebClientResponseException e) {
            log.error("HTTP error {} while calling {}: {}", e.getStatusCode(), url, e.getResponseBodyAsString());
            throw new HttpClientException(
                    String.format("HTTP %s error calling %s: %s", e.getStatusCode(), url, e.getMessage()),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            log.error("Error while calling {}: {}", url, e.getMessage(), e);
            throw new HttpClientException(
                    String.format("Error calling %s: %s", url, e.getMessage()),
                    500,
                    e.getMessage()
            );
        }
    }

    /**
     * Performs an async GET request
     *
     * @param url          The target URL
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param <T>          The response type
     * @return Mono of the response object
     */
    public <T> Mono<T> getAsync(String url, Class<T> responseType, String token) {
        return executeRequestAsync(url, HttpMethod.GET, null, responseType, token, null);
    }

    /**
     * Performs an async POST request
     *
     * @param url          The target URL
     * @param requestBody  The request body
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param <T>          The response type
     * @return Mono of the response object
     */
    public <T> Mono<T> postAsync(String url, Object requestBody, Class<T> responseType, String token) {
        return executeRequestAsync(url, HttpMethod.POST, requestBody, responseType, token, null);
    }

    /**
     * Core method to execute async HTTP requests
     *
     * @param url          The target URL
     * @param method       The HTTP method
     * @param requestBody  The request body (optional)
     * @param responseType The expected response class type
     * @param token        Optional authentication token
     * @param queryParams  Optional query parameters
     * @param <T>          The response type
     * @return Mono of the response object
     */
    private <T> Mono<T> executeRequestAsync(String url, HttpMethod method, Object requestBody,
                                             Class<T> responseType, String token, Map<String, String> queryParams) {
        log.info("Making async {} request to: {}", method, url);

        WebClient.RequestBodySpec requestSpec = webClient
                .method(method)
                .uri(uriBuilder -> {
                    uriBuilder.path(url);
                    if (queryParams != null && !queryParams.isEmpty()) {
                        queryParams.forEach(uriBuilder::queryParam);
                    }
                    return uriBuilder.build();
                })
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        // Add authorization token if provided
        if (token != null && !token.isEmpty()) {
            requestSpec.header(HttpHeaders.AUTHORIZATION, token.startsWith("Bearer ") ? token : "Bearer " + token);
        }

        // Add request body for POST, PUT, PATCH methods
        WebClient.ResponseSpec responseSpec;
        if (requestBody != null && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {
            responseSpec = requestSpec.bodyValue(requestBody).retrieve();
        } else {
            responseSpec = requestSpec.retrieve();
        }

        return responseSpec
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .doOnSuccess(response -> log.info("Successfully received async response from: {}", url))
                .doOnError(error -> log.error("Error in async request to {}: {}", url, error.getMessage()));
    }
}

