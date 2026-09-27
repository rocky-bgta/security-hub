# Activity Logging with AOP - Usage Guide

## Overview
The Activity Logging system uses Aspect-Oriented Programming (AOP) to automatically log user activities to the `ActivityLog` table. This eliminates the need for manual logging calls in service or controller methods.

## Components

1. **@LogActivity Annotation**: Custom annotation to mark methods for activity logging
2. **ActivityLoggingAspect**: AOP aspect that intercepts annotated methods and logs activities

## Usage Examples

### Example 1: Client Admin Creation

```java
@RestController
public class ClientAdminControllerImpl implements ClientAdminController {
    
    @Override
    @LogActivity(
            activityType = ActivityType.CLIENT_CREATED,
            description = "Created client admin: #{#requestDto.organizationName}"
    )
    public ResponseEntity<ApiResponseDto<ClientOnboardingResponseDto>> onboardClientAdmin(
            ClientOnboardingRequestDto requestDto) {
        ClientOnboardingResponseDto response = clientAdminService.processClientOnboarding(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Client admin onboarded successfully", 201, response));
    }
}
```

**What gets logged:**
- Activity Type: `CLIENT_CREATED`
- Description: "Created client admin: [organization name from request]"
- User ID: Extracted from `CurrentUserContext`
- User Type: Extracted from `CurrentUserContext`
- IP Address: Extracted from HTTP request headers
- Status: `SUCCESS` if method completes, `FAILED` if exception occurs

### Example 2: Client Admin Update

```java
@Override
@LogActivity(
        activityType = ActivityType.CLIENT_UPDATED,
        description = "Updated client admin: #{#clientAdminId}",
        newValueExpression = "#{#updateRequestDto.organizationName != null ? #updateRequestDto.organizationName : #clientAdminId}"
)
public ResponseEntity<ApiResponseDto<ClientAdminWithProductsResponseDto>> updateClientAdminById(
        String clientAdminId, 
        ClientAdminUpdateRequestDto updateRequestDto) {
    // ... implementation
}
```

**What gets logged:**
- Activity Type: `CLIENT_UPDATED`
- Description: "Updated client admin: [clientAdminId]"
- New Value: Organization name from update request (if provided)
- User ID, User Type, IP Address: Automatically extracted
- Status: `SUCCESS` or `FAILED` based on execution result

## Annotation Parameters

### `activityType` (Required)
The type of activity being performed. Must be one of the values from `ActivityType` enum:
- `CLIENT_CREATED`
- `CLIENT_UPDATED`
- `CLIENT_STATUS_CHANGED`
- `USER_CREATED`
- `USER_UPDATED`
- etc.

### `description` (Optional)
Description of the activity. Supports SpEL (Spring Expression Language) expressions to reference method parameters.

**SpEL Examples:**
- `"Created client admin: #{#requestDto.organizationName}"` - References a field in the request DTO
- `"Updated user: #{#userId}"` - References a method parameter
- `"Status changed to: #{#statusUpdateDto.getStatus()}"` - Calls a method on a parameter

### `oldValueExpression` (Optional)
SpEL expression to extract the old value for update operations. Useful for tracking what changed.

**Example:**
```java
oldValueExpression = "#{#clientAdminId}" // Use clientAdminId as old value
```

### `newValueExpression` (Optional)
SpEL expression to extract the new value for update operations.

**Example:**
```java
newValueExpression = "#{#updateRequestDto.organizationName != null ? #updateRequestDto.organizationName : 'N/A'}"
```

### `logOnEntry` (Optional, default: false)
If `true`, logs an activity entry before method execution. Useful for tracking method start times.

## How It Works

1. **Method Execution**: When a method annotated with `@LogActivity` is called, the AOP aspect intercepts it
2. **Context Extraction**: The aspect extracts:
   - User context from `UserCurrentContextService`
   - IP address from HTTP request
   - Method parameters for SpEL evaluation
3. **SpEL Evaluation**: Evaluates description, oldValue, and newValue expressions
4. **Method Execution**: Proceeds with the actual method execution
5. **Activity Logging**: After execution (or on exception), logs the activity with:
   - Success status if method completes normally
   - Failed status if exception occurs
   - Error message appended to description on failure

## Benefits

1. **Separation of Concerns**: Logging logic is separated from business logic
2. **Consistency**: All activities are logged in the same format
3. **Automatic**: No need to manually call logging methods
4. **Error Handling**: Automatically logs failures with error messages
5. **Flexible**: Supports SpEL expressions for dynamic descriptions and values

## Testing

### Test Case 1: Client Admin Creation

**Request:**
```http
POST /api/v1/client/admin/onboard
Content-Type: application/json
CurrentContext: [base64 encoded user context]

{
  "organizationName": "Tech Solutions Inc.",
  "contactEmail": "contact@techsolutions.com",
  ...
}
```

**Expected Activity Log:**
```json
{
  "activityType": "CLIENT_CREATED",
  "description": "Created client admin: Tech Solutions Inc.",
  "userId": "[current user ID]",
  "userType": "CLIENT_ADMIN",
  "activityStatus": "SUCCESS",
  "ipAddress": "192.168.1.100",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### Test Case 2: Client Admin Update

**Request:**
```http
PUT /api/v1/client/admin/{clientAdminId}
Content-Type: application/json
CurrentContext: [base64 encoded user context]

{
  "organizationName": "Updated Tech Solutions Inc.",
  ...
}
```

**Expected Activity Log:**
```json
{
  "activityType": "CLIENT_UPDATED",
  "description": "Updated client admin: client-admin-123",
  "userId": "[current user ID]",
  "userType": "CLIENT_ADMIN",
  "activityStatus": "SUCCESS",
  "newValue": "Updated Tech Solutions Inc.",
  "ipAddress": "192.168.1.100",
  "createdAt": "2024-01-15T10:35:00Z"
}
```

### Test Case 3: Failed Operation

If an exception occurs during method execution, the activity log will have:
- `activityStatus`: `FAILED`
- `description`: Original description + " - Failed: [error message]"

## Notes

- The aspect uses `@Async` on the `ActivityLogService.logActivity()` method, so logging doesn't block the main execution
- If user context cannot be retrieved, `userType` defaults to `SYSTEM_USER`
- If IP address cannot be retrieved, it will be `null` (not an error)
- SpEL expression evaluation failures are logged as warnings but don't break the main flow

