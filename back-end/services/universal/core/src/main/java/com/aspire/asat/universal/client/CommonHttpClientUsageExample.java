package com.aspire.asat.universal.client;

/**
 * Example usage of CommonHttpClient
 * 
 * This class demonstrates how to use the CommonHttpClient to make service-to-service calls
 * 
 * Usage Examples:
 * 
 * 1. Simple GET request without token:
 * ```java
 * @Autowired
 * private CommonHttpClient httpClient;
 * 
 * UserResponse user = httpClient.get(
 *     "http://user-service/api/users/123",
 *     UserResponse.class
 * );
 * ```
 * 
 * 2. GET request with token:
 * ```java
 * String token = "your-jwt-token";
 * UserResponse user = httpClient.get(
 *     "http://user-service/api/users/123",
 *     UserResponse.class,
 *     token
 * );
 * ```
 * 
 * 3. GET request with query parameters:
 * ```java
 * Map<String, String> params = new HashMap<>();
 * params.put("page", "1");
 * params.put("size", "10");
 * 
 * PagedResponse response = httpClient.get(
 *     "http://user-service/api/users",
 *     PagedResponse.class,
 *     token,
 *     params
 * );
 * ```
 * 
 * 4. POST request with token:
 * ```java
 * CreateUserRequest request = new CreateUserRequest();
 * request.setName("John Doe");
 * request.setEmail("john@example.com");
 * 
 * UserResponse response = httpClient.post(
 *     "http://user-service/api/users",
 *     request,
 *     UserResponse.class,
 *     token
 * );
 * ```
 * 
 * 5. POST request without token:
 * ```java
 * LoginRequest loginRequest = new LoginRequest();
 * loginRequest.setUsername("user@example.com");
 * loginRequest.setPassword("password");
 * 
 * LoginResponse response = httpClient.post(
 *     "http://auth-service/api/login",
 *     loginRequest,
 *     LoginResponse.class
 * );
 * ```
 * 
 * 6. PUT request with token:
 * ```java
 * UpdateUserRequest request = new UpdateUserRequest();
 * request.setName("Jane Doe");
 * 
 * UserResponse response = httpClient.put(
 *     "http://user-service/api/users/123",
 *     request,
 *     UserResponse.class,
 *     token
 * );
 * ```
 * 
 * 7. DELETE request with token:
 * ```java
 * DeleteResponse response = httpClient.delete(
 *     "http://user-service/api/users/123",
 *     DeleteResponse.class,
 *     token
 * );
 * ```
 * 
 * 8. Async GET request:
 * ```java
 * Mono<UserResponse> userMono = httpClient.getAsync(
 *     "http://user-service/api/users/123",
 *     UserResponse.class,
 *     token
 * );
 * 
 * userMono.subscribe(
 *     user -> log.info("User: {}", user),
 *     error -> log.error("Error: {}", error.getMessage())
 * );
 * ```
 * 
 * 9. Async POST request:
 * ```java
 * CreateUserRequest request = new CreateUserRequest();
 * request.setName("John Doe");
 * 
 * Mono<UserResponse> responseMono = httpClient.postAsync(
 *     "http://user-service/api/users",
 *     request,
 *     UserResponse.class,
 *     token
 * );
 * ```
 * 
 * 10. Error handling:
 * ```java
 * try {
 *     UserResponse user = httpClient.get(
 *         "http://user-service/api/users/123",
 *         UserResponse.class,
 *         token
 *     );
 * } catch (HttpClientException e) {
 *     log.error("HTTP error {}: {}", e.getStatusCode(), e.getMessage());
 *     log.error("Response body: {}", e.getResponseBody());
 * }
 * ```
 * 
 * Note: The token parameter is optional. If provided, it will be automatically 
 * formatted as a Bearer token in the Authorization header.
 */
public class CommonHttpClientUsageExample {
    // This is a documentation class - no implementation needed
}

