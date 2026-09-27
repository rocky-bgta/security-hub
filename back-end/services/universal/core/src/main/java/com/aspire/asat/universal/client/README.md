# Common HTTP Client

A reusable HTTP client for making service-to-service calls within the Universal service.

## Features

- ✅ Support for all HTTP methods (GET, POST, PUT, PATCH, DELETE)
- ✅ Optional authentication token support
- ✅ Query parameters support
- ✅ Synchronous and asynchronous request execution
- ✅ Configurable timeouts and connection settings
- ✅ Request/Response logging
- ✅ Custom error handling with detailed exception information
- ✅ Automatic Bearer token formatting

## Components

### 1. CommonHttpClient
The main client class that provides methods for making HTTP requests.

**Location:** `com.aspire.asat.universal.client.CommonHttpClient`

### 2. HttpClientConfig
Configuration class for WebClient with custom timeout and connection settings.

**Location:** `com.aspire.asat.universal.client.config.HttpClientConfig`

### 3. HttpClientException
Custom exception class for handling HTTP client errors.

**Location:** `com.aspire.asat.universal.client.HttpClientException`

### 4. ExternalServiceClient
Example service showing how to use the CommonHttpClient.

**Location:** `com.aspire.asat.universal.client.service.ExternalServiceClient`

## Configuration

The HTTP client can be configured via the `HttpClientConfig` class. Default settings:

- **Connection Timeout:** 10 seconds
- **Read Timeout:** 30 seconds
- **Write Timeout:** 30 seconds
- **Max In-Memory Size:** 10MB

## Usage

### Dependency Injection

```java
@Service
@RequiredArgsConstructor
public class YourService {
    private final CommonHttpClient httpClient;
    
    // Your service methods...
}
```

### Basic GET Request

```java
// Without token
UserResponse user = httpClient.get(
    "http://user-service/api/users/123",
    UserResponse.class
);

// With token
UserResponse user = httpClient.get(
    "http://user-service/api/users/123",
    UserResponse.class,
    "your-jwt-token"
);
```

### GET Request with Query Parameters

```java
Map<String, String> params = new HashMap<>();
params.put("page", "1");
params.put("size", "10");

PagedResponse response = httpClient.get(
    "http://user-service/api/users",
    PagedResponse.class,
    token,
    params
);
```

### POST Request

```java
CreateUserRequest request = new CreateUserRequest();
request.setName("John Doe");
request.setEmail("john@example.com");

UserResponse response = httpClient.post(
    "http://user-service/api/users",
    request,
    UserResponse.class,
    token  // Can be null if token is not required
);
```

### PUT Request

```java
UpdateUserRequest request = new UpdateUserRequest();
request.setName("Jane Doe");

UserResponse response = httpClient.put(
    "http://user-service/api/users/123",
    request,
    UserResponse.class,
    token
);
```

### PATCH Request

```java
PatchUserRequest request = new PatchUserRequest();
request.setEmail("newemail@example.com");

UserResponse response = httpClient.patch(
    "http://user-service/api/users/123",
    request,
    UserResponse.class,
    token
);
```

### DELETE Request

```java
DeleteResponse response = httpClient.delete(
    "http://user-service/api/users/123",
    DeleteResponse.class,
    token
);
```

### Async Requests

```java
// Async GET
Mono<UserResponse> userMono = httpClient.getAsync(
    "http://user-service/api/users/123",
    UserResponse.class,
    token
);

userMono.subscribe(
    user -> log.info("User: {}", user),
    error -> log.error("Error: {}", error.getMessage())
);

// Async POST
Mono<UserResponse> responseMono = httpClient.postAsync(
    "http://user-service/api/users",
    createRequest,
    UserResponse.class,
    token
);
```

### Error Handling

```java
try {
    UserResponse user = httpClient.get(
        "http://user-service/api/users/123",
        UserResponse.class,
        token
    );
} catch (HttpClientException e) {
    log.error("HTTP error {}: {}", e.getStatusCode(), e.getMessage());
    log.error("Response body: {}", e.getResponseBody());
    
    // Handle specific status codes
    if (e.getStatusCode() == 404) {
        // Handle not found
    } else if (e.getStatusCode() == 401) {
        // Handle unauthorized
    }
}
```

## Creating Service-Specific Clients

It's recommended to create service-specific client classes that wrap the CommonHttpClient:

```java
@Service
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final CommonHttpClient httpClient;
    
    @Value("${services.user-service.base-url}")
    private String baseUrl;
    
    public UserResponse getUserById(String userId, String token) {
        String url = baseUrl + "/api/users/" + userId;
        return httpClient.get(url, UserResponse.class, token);
    }
    
    public UserResponse createUser(CreateUserRequest request, String token) {
        String url = baseUrl + "/api/users";
        return httpClient.post(url, request, UserResponse.class, token);
    }
    
    // Other user service methods...
}
```

## Configuration Properties

Add service URLs to your `application.yml`:

```yaml
services:
  user-service:
    base-url: http://user-service:8080
  auth-service:
    base-url: http://auth-service:8080
  # Add other services...
```

## Token Handling

The client automatically formats tokens as Bearer tokens:

```java
// These are equivalent:
httpClient.get(url, responseType, "your-token");
httpClient.get(url, responseType, "Bearer your-token");
```

Both will result in the header: `Authorization: Bearer your-token`

## Best Practices

1. **Use Service-Specific Clients:** Create wrapper services for each external service you call
2. **Configure Base URLs:** Use application properties for service URLs
3. **Handle Errors Gracefully:** Always catch and handle `HttpClientException`
4. **Use Appropriate Types:** Use specific DTOs instead of generic Maps when possible
5. **Log Appropriately:** The client logs at INFO and DEBUG levels - configure as needed
6. **Consider Async:** Use async methods for non-blocking operations when appropriate
7. **Token Management:** Pass tokens from the request context or use a token provider service

## Example DTO Classes

```java
@Data
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String createdAt;
}

@Data
public class CreateUserRequest {
    @NotBlank
    private String name;
    @Email
    private String email;
}
```

## Logging

The client provides detailed logging:

- **INFO:** Request start and successful responses
- **DEBUG:** Request/response headers and status codes
- **ERROR:** HTTP errors and exceptions

Configure logging levels in `application.yml`:

```yaml
logging:
  level:
    com.aspire.asat.universal.client: DEBUG
```

## Testing

Example test using Mockito:

```java
@ExtendWith(MockitoExtension.class)
class YourServiceTest {
    
    @Mock
    private CommonHttpClient httpClient;
    
    @InjectMocks
    private YourService yourService;
    
    @Test
    void testGetUser() {
        UserResponse expectedUser = new UserResponse();
        expectedUser.setId("123");
        
        when(httpClient.get(anyString(), eq(UserResponse.class), anyString()))
            .thenReturn(expectedUser);
        
        UserResponse result = yourService.getUser("123", "token");
        
        assertEquals("123", result.getId());
    }
}
```

