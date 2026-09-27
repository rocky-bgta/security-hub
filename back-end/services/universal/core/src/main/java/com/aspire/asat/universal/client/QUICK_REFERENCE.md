# Common HTTP Client - Quick Reference

## Created Files

### Core Components
1. **CommonHttpClient.java** - Main HTTP client with support for all HTTP methods
   - Location: `services/universal/core/src/main/java/com/aspire/asat/universal/client/CommonHttpClient.java`
   - Methods: GET, POST, PUT, PATCH, DELETE (sync and async)
   - Optional token authentication

2. **HttpClientConfig.java** - WebClient configuration with timeouts and logging
   - Location: `services/universal/core/src/main/java/com/aspire/asat/universal/client/config/HttpClientConfig.java`
   - Configures connection, read, and write timeouts
   - Adds request/response logging filters

3. **HttpClientException.java** - Custom exception for HTTP errors
   - Location: `services/universal/core/src/main/java/com/aspire/asat/universal/client/HttpClientException.java`
   - Includes status code and response body

### Examples & Documentation
4. **ExternalServiceClient.java** - Example service client implementation
   - Location: `services/universal/core/src/main/java/com/aspire/asat/universal/client/service/ExternalServiceClient.java`

5. **CommonHttpClientUsageExample.java** - Code examples
   - Location: `services/universal/core/src/main/java/com/aspire/asat/universal/client/CommonHttpClientUsageExample.java`

6. **README.md** - Comprehensive documentation
   - Location: `services/universal/core/src/main/java/com/aspire/asat/universal/client/README.md`

## Quick Usage

### Inject the Client
```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final CommonHttpClient httpClient;
}
```

### Make Requests

**Without Token:**
```java
UserResponse user = httpClient.get("http://service/api/users/123", UserResponse.class);
```

**With Token:**
```java
UserResponse user = httpClient.get("http://service/api/users/123", UserResponse.class, token);
```

**POST Request:**
```java
UserResponse created = httpClient.post("http://service/api/users", request, UserResponse.class, token);
```

**With Query Params:**
```java
Map<String, String> params = Map.of("page", "1", "size", "10");
PagedResponse data = httpClient.get("http://service/api/users", PagedResponse.class, token, params);
```

## Features

✅ All HTTP methods (GET, POST, PUT, PATCH, DELETE)
✅ Optional token authentication (auto-formats as Bearer token)
✅ Query parameters support
✅ Sync and async execution
✅ Configurable timeouts
✅ Request/Response logging
✅ Detailed error handling
✅ 10MB max in-memory size
✅ 30-second default timeout

## Dependencies

Already included in `universal/core/build.gradle`:
- `spring-boot-starter-webflux` ✅
- `lombok` ✅
- `spring-boot-starter-web` ✅

## Next Steps

1. Use the client in your services
2. Create service-specific client wrappers (see `ExternalServiceClient.java` example)
3. Configure service URLs in `application.yml`
4. Handle `HttpClientException` appropriately in your error handling

## Configuration Example

Add to your `application.yml`:
```yaml
services:
  your-service:
    base-url: http://your-service:8080

logging:
  level:
    com.aspire.asat.universal.client: DEBUG  # For detailed HTTP logging
```

