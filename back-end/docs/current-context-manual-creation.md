# CurrentContext Manual Creation Guide

This document provides a comprehensive guide for manually creating CurrentContext for users in the ASAT system. This is useful for testing, debugging, or creating context for specific scenarios.

## Overview

CurrentContext is a JSON object that contains user information and is Base64 encoded for transmission in HTTP headers. It's used by the Gateway service to pass user context to downstream services.

## CurrentContext Structure

### JSON Schema

```json
{
  "userId": "string (UUID)",
  "tokenId": "string (UUID)", 
  "clientAdminId": "string (optional)",
  "email": "string (email address)",
  "username": "string (email address)",
  "phoneNumber": "string (optional)",
  "userType": "string (optional)",
  "userStatus": "string (optional)",
  "coRelationId": "string (optional)",
  "fullName": "string (user's full name)",
  "scope": "array of strings (optional)",
  "userRoleList": "array of strings (optional)"
}
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `userId` | String (UUID) | ✅ Yes | Unique identifier for the user |
| `tokenId` | String (UUID) | ✅ Yes | Unique identifier for the current session token |
| `clientAdminId` | String | ❌ No | ID of the client admin (empty for regular users) |
| `email` | String | ✅ Yes | User's email address |
| `username` | String | ✅ Yes | Username (usually same as email) |
| `phoneNumber` | String | ❌ No | User's phone number |
| `userType` | String | ❌ No | Type of user (e.g., "USER", "ADMIN") |
| `userStatus` | String | ❌ No | Status of the user (e.g., "ACTIVE", "INACTIVE") |
| `coRelationId` | String | ❌ No | Correlation ID for tracking |
| `fullName` | String | ✅ Yes | User's full name |
| `scope` | Array of Strings | ❌ No | User's permissions/scopes |
| `userRoleList` | Array of Strings | ❌ No | User's roles (e.g., ["ADMIN", "USER"]) |

## Sample Data

### Example 1: Super Admin User

**JSON Object:**
```json
{
  "userId": "9cb0fa49-4e37-4b1e-9ba1-0624150f818b",
  "tokenId": "9cb0fa49-4e37-4b1e-9ba1-0624150f818b",
  "clientAdminId": "",
  "email": "superadmin01@yopmail.com",
  "username": "superadmin01@yopmail.com",
  "phoneNumber": null,
  "userType": null,
  "userStatus": null,
  "coRelationId": null,
  "fullName": "Super Admin",
  "scope": null,
  "userRoleList": ["SUPER_ADMIN"]
}
```

**Base64 Encoded:**
```
eyJ1c2VySWQiOiI5Y2IwZmE0OS00ZTM3LTRiMWUtOWJhMS0wNjI0MTUwZjgxOGIiLCJjbGllbnRBZG1pbklkIjoiIiwiZW1haWwiOiJzdXBlcmFkbWluMDFAeW9wbWFpbC5jb20iLCJ1c2VybmFtZSI6InN1cGVyYWRtaW4wMUB5b3BtYWlsLmNvbSIsInBob25lTnVtYmVyIjpudWxsLCJ1c2VyVHlwZSI6bnVsbCwidXNlclN0YXR1cyI6bnVsbCwiY29SZWxhdGlvbklkIjpudWxsLCJmdWxsTmFtZSI6IlN1cGVyIEFkbWluIiwic2NvcGUiOm51bGwsInVzZXJSb2xlTGlzdCI6WyJTVVBFUl9BRE1JTiJdfQ==
```

### Example 2: Regular User

**JSON Object:**
```json
{
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "tokenId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "clientAdminId": "client-admin-123",
  "email": "john.doe@example.com",
  "username": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "userType": "USER",
  "userStatus": "ACTIVE",
  "coRelationId": "req-12345",
  "fullName": "John Doe",
  "scope": ["read:profile", "write:profile"],
  "userRoleList": ["USER"]
}
```

**Base64 Encoded:**
```
eyJ1c2VySWQiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJ0b2tlbklkIjoiYTFhMmMzZDQtZTVmNi03ODkwLWFiY2QtZWYxMjM0NTY3ODkwIiwiY2xpZW50QWRtaW5JZCI6ImNsaWVudC1hZG1pbi0xMjMiLCJlbWFpbCI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwidXNlcm5hbWUiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsInBob25lTnVtYmVyIjoiKzEyMzQ1Njc4OTAiLCJ1c2VyVHlwZSI6IlVTRVIiLCJ1c2VyU3RhdHVzIjoiQUNUSVZFIiwiY29SZWxhdGlvbklkIjoicmVxLTEyMzQ1IiwiZnVsbE5hbWUiOiJKb2huIERvZSIsInNjb3BlIjpbInJlYWQ6cHJvZmlsZSIsIndyaXRlOnByb2ZpbGUiXSwidXNlclJvbGVMaXN0IjpbIlVTRVIiXX0=
```

## Manual Creation Process

### Step 1: Create JSON Object

1. **Generate UUIDs**: Use a UUID generator for `userId` and `tokenId`
2. **Set Required Fields**: Ensure `userId`, `tokenId`, `email`, `username`, and `fullName` are populated
3. **Set Optional Fields**: Fill in other fields as needed for your use case
4. **Validate JSON**: Ensure the JSON is valid

### Step 2: Base64 Encode

#### Using Online Tools (Recommended)
1. Copy your complete JSON object
2. Go to a Base64 encoder website (e.g., base64encode.org, base64.guru)
3. Paste the JSON string into the input field
4. Click "Encode" or "Convert"
5. Copy the Base64 encoded string from the output field
6. Verify the encoded string looks like: `eyJ1c2VySWQiOiI5Y2IwZmE0OS00ZTM3LTRiMWUtOWJhMS0wNjI0MTUwZjgxOGIi...`

### Step 3: Use in HTTP Headers

Add the Base64 encoded string to your HTTP request headers:

```http
POST /api/v1/cms/some-endpoint
Content-Type: application/json
CurrentContext: eyJ1c2VySWQiOiI5Y2IwZmE0OS00ZTM3LTRiMWUtOWJhMS0wNjI0MTUwZjgxOGIiLCJjbGllbnRBZG1pbklkIjoiIiwiZW1haWwiOiJzdXBlcmFkbWluMDFAeW9wbWFpbC5jb20iLCJ1c2VybmFtZSI6InN1cGVyYWRtaW4wMUB5b3BtYWlsLmNvbSIsInBob25lTnVtYmVyIjpudWxsLCJ1c2VyVHlwZSI6bnVsbCwidXNlclN0YXR1cyI6bnVsbCwiY29SZWxhdGlvbklkIjpudWxsLCJmdWxsTmFtZSI6IlN1cGVyIEFkbWluIiwic2NvcGUiOm51bGx9
Authorization: Bearer your-jwt-token

{
  "your": "request body"
}
```

## Testing Scenarios

### Scenario 1: Super Admin Testing
Use the super admin context to test admin-only endpoints and verify full system access.

### Scenario 2: Regular User Testing
Use a regular user context to test user-specific functionality and verify proper access controls.

### Scenario 3: Client Admin Testing
Use a client admin context to test organization-level functionality and user management features.

### Scenario 4: Invalid Context Testing
Test with malformed JSON or invalid Base64 to verify error handling.

## Common Use Cases

### 1. API Testing
- **Postman**: Add `CurrentContext` header to requests
- **curl**: Use `-H "CurrentContext: <base64-string>"`
- **Automated Tests**: Include context in test setup

### 2. Development/Debugging
- **Local Development**: Create context for specific user scenarios
- **Debugging**: Use known context to reproduce issues
- **Feature Testing**: Test different user roles and permissions

### 3. Integration Testing
- **Service Integration**: Test inter-service communication
- **End-to-End Testing**: Verify complete user flows
- **Performance Testing**: Test with different user contexts

## Validation and Verification

### JSON Validation
1. Copy your JSON string
2. Go to a JSON validator website (e.g., jsonlint.com)
3. Paste the JSON and click "Validate JSON"
4. Fix any syntax errors if validation fails

### Base64 Decoding Verification
1. Copy your Base64 encoded string
2. Go to a Base64 decoder website (e.g., base64decode.org)
3. Paste the Base64 string and click "Decode"
4. Verify the decoded content matches your original JSON

### Header Testing
```bash
# Test with curl
curl -X POST "https://your-api.com/endpoint" \
  -H "Content-Type: application/json" \
  -H "CurrentContext: your-base64-string" \
  -H "Authorization: Bearer your-token" \
  -d '{"test": "data"}'
```

## Troubleshooting

### Common Issues

1. **Invalid JSON**: Ensure proper JSON formatting
2. **Base64 Encoding**: Verify encoding is correct
3. **Header Name**: Use exact header name `CurrentContext`
4. **UUID Format**: Ensure UUIDs are valid format
5. **Null Values**: Use `null` (not `"null"`) for empty fields

### Error Messages

- **"Invalid CurrentContext"**: Check JSON validity and Base64 encoding
- **"Unauthorized access"**: Verify user permissions and roles
- **"Token validation failed"**: Check tokenId and userId consistency

## Security Considerations

1. **Token Validation**: Ensure tokenId matches userId for security
2. **Role Verification**: Verify userRoleList contains appropriate roles
3. **Scope Validation**: Check scope permissions match user requirements
4. **Client Admin**: Verify clientAdminId for organization-level access

## Best Practices

1. **Use Valid UUIDs**: Generate proper UUIDs for userId and tokenId
2. **Consistent Data**: Ensure email and username match
3. **Appropriate Roles**: Set userRoleList based on testing needs
4. **Clean JSON**: Remove unnecessary fields to keep context minimal
5. **Test Different Scenarios**: Create contexts for various user types

## Tools and Resources

- **UUID Generator**: https://www.uuidgenerator.net/
- **Base64 Encoder**: https://www.base64encode.org/
- **JSON Validator**: https://jsonlint.com/
- **Postman**: For API testing with headers
- **curl**: For command-line testing

---

*This documentation is maintained for the ASAT system. For questions or updates, please contact the development team.*

**Base64 Encoded:Atik**
```
SUPER_ADMIN
ewogICJ1c2VySWQiOiAiOWNiMGZhNDktNGUzNy00YjFlLTliYTEtMDYyNDE1MGY4MThiIiwKICAidG9rZW5JZCI6ICI4MWVmOWZiYS0wZDgyLTQ0YTItOGI0NC04ZTUzODljMzk5MjMiLAogICJjbGllbnRBZG1pbklkIjogIiIsCiAgImVtYWlsIjogImF0aWsxOTA2QHlvcG1haWwuY29tIiwKICAidXNlcm5hbWUiOiAiYXRpazE5MDZAeW9wbWFpbC5jb20iLAogICJwaG9uZU51bWJlciI6IG51bGwsCiAgInVzZXJUeXBlIjogIlNVUEVSX0FETUlOIiwKICAidXNlclN0YXR1cyI6ICJBQ1RJVkUiLAogICJjb1JlbGF0aW9uSWQiOiBudWxsLAogICJmdWxsTmFtZSI6ICJTdXBlciBBZG1pbiIsCiAgInNjb3BlIjogW10sCiAgInVzZXJSb2xlTGlzdCI6IFsiU1VQRVJfQURNSU4iXQp9

ASPIER_ADMIN
ewogICJ1c2VySWQiOiAiOWNiMGZhNDktNGUzNy00YjFlLTliYTEtMDYyNDE1MGY4MThiIiwKICAidG9rZW5JZCI6ICI4MWVmOWZiYS0wZDgyLTQ0YTItOGI0NC04ZTUzODljMzk5MjMiLAogICJjbGllbnRBZG1pbklkIjogIiIsCiAgImVtYWlsIjogImF0aWsxOTA2QHlvcG1haWwuY29tIiwKICAidXNlcm5hbWUiOiAiYXRpazE5MDYxQHlvcG1haWwuY29tIiwKICAicGhvbmVOdW1iZXIiOiBudWxsLAogICJ1c2VyVHlwZSI6ICJBU1BJUkVfQURNSU4iLAogICJ1c2VyU3RhdHVzIjogIkFDVElWRSIsCiAgImNvUmVsYXRpb25JZCI6ICJyZXEtMjAyNTEwMjEtYXNwaXJlMDAxIiwKICAiZnVsbE5hbWUiOiAiU3VwZXIgQWRtaW4iLAogICJzY29wZSI6IFtdLAogICJ1c2VyUm9sZUxpc3QiOiBbIkFTUElSRV9BRE1JTiJdCn0=


client_ADMIN
ewogICJ1c2VySWQiOiAiNDQ5MDNhNmUtYmMyMS00MWIzLTk4MTctYTFiN2VmYmNmOGY5IiwKICAidG9rZW5JZCI6ICJhOWMzZTFkNC01ZjY3LTRiODktYmMyMS05OGUzZmJiOGQ3NzEiLAogICJjbGllbnRBZG1pbklkIjogIiIsCiAgImVtYWlsIjogImRldi5tZGFidWJha2thckBnbWFpbC5jb20iLAogICJ1c2VybmFtZSI6ICJkZXYubWRhYnViYWtrYXJAZ21haWwuY29tIiwKICAicGhvbmVOdW1iZXIiOiAiODgwMjI2NDcyNTAwMSIsCiAgInVzZXJUeXBlIjogIkNMSUVOVF9BRE1JTiIsCiAgInVzZXJTdGF0dXMiOiAiUEVORElORyIsCiAgImNvUmVsYXRpb25JZCI6ICJyZXEtMjAyNTEwMjEtYWJ1YmFra2FyMDAxIiwKICAiZnVsbE5hbWUiOiAiQWJ1IEJha2thciIsCiAgInNjb3BlIjogW10sCiAgInVzZXJSb2xlTGlzdCI6IFsiQ0xJRU5UX0FETUlOIl0KfQ==


Client_USER
ewogICJ1c2VySWQiOiAiNzkyMzQyODktNDM5MC00OWEyLTgwMmMtMDJkMDg1ZTZkNjc4IiwKICAidG9rZW5JZCI6ICJiODJhMWY2ZS03YzNhLTRjOTAtOWYzMS0yYWQ5YzhlODdmMjIiLAogICJjbGllbnRBZG1pbklkIjogIjQ0OTAzYTZlLWJjMjEtNDFiMy05ODE3LWExYjdlZmJjZjhmOSIsCiAgImVtYWlsIjogInNham9sQGFzcGlyZXRzcy5jb20iLAogICJ1c2VybmFtZSI6ICJzYWpvbEBhc3BpcmV0c3MuY29tIiwKICAicGhvbmVOdW1iZXIiOiAiKzg4MDE1MjEzOTQwNzkiLAogICJ1c2VyVHlwZSI6ICJVU0VSIiwKICAidXNlclN0YXR1cyI6ICJBQ1RJVkUiLAogICJjb1JlbGF0aW9uSWQiOiAicmVxLTIwMjUxMDIxLXNham9sMDAxIiwKICAiZnVsbE5hbWUiOiAiU2Fqb2wiLAogICJzY29wZSI6IFtdLAogICJ1c2VyUm9sZUxpc3QiOiBbIlVTRVIiXQp9

```