# Dynamic Admin Notification APIs Testing Guide

This guide provides comprehensive testing examples for the new dynamic admin notification APIs with single active constraints.

## Base URL
```
http://localhost:5656/notification/api/v1/admin
```

## Key Features

### ✅ **Single Active Constraint**
- **Settings**: Only one notification type can be enabled at a time
- **Templates**: Only one template can be active per notification type and channel combination

### ✅ **Dynamic Action Management**
- Single endpoint handles all actions through `action` parameter
- Reduces API complexity and maintenance overhead
- Consistent request/response format

## Dynamic Notification Settings API

### Get All Settings with Pagination

```bash
curl -X GET "http://localhost:5656/notification/api/v1/admin/notification-settings?offset=0&pageSize=20" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "message": "Notification settings retrieved successfully",
  "statusCode": 200,
  "data": {
    "offset": 0,
    "pageSize": 20,
    "total": 1,
    "items": [
      {
        "id": "settings-001",
        "notificationType": "NEW_USER_REGISTERED",
        "enabled": true,
        "description": "Settings for new user registration notifications",
        "emailEnabled": true,
        "inAppEnabled": true,
        "smsEnabled": false,
        "pushEnabled": false,
        "defaultEmailTemplateId": "template-001",
        "defaultInAppTemplateId": "template-002",
        "createdAt": "2025-01-23T10:00:00",
        "updatedAt": "2025-01-23T10:00:00"
      }
    ]
  }
}
```

### Dynamic Action Endpoint: `POST /api/v1/admin/notification-settings/action`

#### 1. CREATE Action - Create New Settings

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "CREATE",
    "notificationType": "NEW_POLICY_CREATED",
    "enabled": true,
    "description": "Settings for new policy creation notifications",
    "emailEnabled": true,
    "inAppEnabled": true,
    "smsEnabled": false,
    "pushEnabled": false,
    "defaultEmailTemplateId": "policy-email-template",
    "defaultInAppTemplateId": "policy-inapp-template"
  }'
```

**Response:**
```json
{
  "message": "Notification settings created successfully",
  "statusCode": 200,
  "data": {
    "id": "settings-001",
    "notificationType": "NEW_POLICY_CREATED",
    "enabled": true,
    "description": "Settings for new policy creation notifications",
    "emailEnabled": true,
    "inAppEnabled": true,
    "smsEnabled": false,
    "pushEnabled": false,
    "defaultEmailTemplateId": "policy-email-template",
    "defaultInAppTemplateId": "policy-inapp-template",
    "createdAt": "2025-01-23T10:00:00",
    "updatedAt": "2025-01-23T10:00:00"
  }
}
```

#### 2. UPDATE Action - Update Existing Settings

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "UPDATE",
    "notificationType": "NEW_USER_REGISTERED",
    "description": "Updated settings for new user registration",
    "emailEnabled": true,
    "inAppEnabled": true,
    "smsEnabled": true,
    "pushEnabled": false
  }'
```

#### 3. ENABLE Action - Enable Notification Type (Disables All Others)

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "ENABLE",
    "notificationType": "COURSE_ASSIGNED"
  }'
```

**Response:**
```json
{
  "message": "Notification type enabled successfully (all others disabled)",
  "statusCode": 200,
  "data": {
    "id": "settings-002",
    "notificationType": "COURSE_ASSIGNED",
    "enabled": true,
    "description": "Settings for course assignment notifications",
    "emailEnabled": true,
    "inAppEnabled": true,
    "smsEnabled": false,
    "pushEnabled": false,
    "createdAt": "2025-01-23T10:00:00",
    "updatedAt": "2025-01-23T10:05:00"
  }
}
```

#### 4. DISABLE Action - Disable Notification Type

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "DISABLE",
    "notificationType": "NEW_USER_REGISTERED"
  }'
```

#### 5. ENABLE_CHANNEL Action - Enable Specific Channel

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "ENABLE_CHANNEL",
    "notificationType": "NEW_USER_REGISTERED",
    "channel": "SMS"
  }'
```

#### 6. DISABLE_CHANNEL Action - Disable Specific Channel

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "DISABLE_CHANNEL",
    "notificationType": "NEW_USER_REGISTERED",
    "channel": "EMAIL"
  }'
```

#### 7. DELETE Action - Delete/Disable Settings

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "DELETE",
    "notificationType": "NEW_USER_REGISTERED"
  }'
```

## Dynamic Notification Templates API

### Get All Templates with Pagination

```bash
curl -X GET "http://localhost:5656/notification/api/v1/admin/notification-templates?offset=0&pageSize=20" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "message": "Notification templates retrieved successfully",
  "statusCode": 200,
  "data": {
    "offset": 0,
    "pageSize": 20,
    "total": 1,
    "items": [
      {
        "id": "template-001",
        "notificationType": "NEW_USER_REGISTERED",
        "channel": "EMAIL",
        "templateName": "Welcome Email Template",
        "subjectTemplate": "Welcome to {{companyName}}",
        "htmlTemplate": "<html>...</html>",
        "isActive": true,
        "isDefault": false,
        "createdAt": "2025-01-23T10:00:00",
        "updatedAt": "2025-01-23T10:00:00"
      }
    ]
  }
}
```

### Dynamic Action Endpoint: `POST /api/v1/admin/notification-templates/action`

#### 1. CREATE Action - Create New Template

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "CREATE",
    "notificationType": "NEW_POLICY_CREATED",
    "channel": "EMAIL",
    "templateName": "New Policy Created Email Template",
    "subjectTemplate": "New Policy: {{policyName}}",
    "htmlTemplate": "<html><body><h1>New Policy Created</h1><p>Dear {{userName}},</p><p>A new policy \"{{policyName}}\" has been created and is now available.</p><p>Please review the policy details and ensure compliance.</p><p>Best regards,<br>{{companyName}} Team</p></body></html>",
    "textTemplate": "New Policy: {{policyName}}\n\nDear {{userName}},\n\nA new policy \"{{policyName}}\" has been created and is now available.\n\nPlease review the policy details and ensure compliance.\n\nBest regards,\n{{companyName}} Team",
    "titleTemplate": "New Policy Created",
    "messageTemplate": "A new policy \"{{policyName}}\" has been created and is now available.",
    "isActive": true,
    "isDefault": false
  }'
```

#### 2. UPDATE Action - Update Existing Template

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "UPDATE",
    "templateId": "template-001",
    "templateName": "Updated Welcome Email Template",
    "subjectTemplate": "Welcome to {{companyName}} - {{userName}}",
    "htmlTemplate": "<html><body><h1>Welcome to {{companyName}}!</h1><p>Dear {{userName}},</p><p>Your account has been successfully created.</p><p><strong>Email:</strong> {{email}}</p><p><strong>Temporary Password:</strong> {{tempPassword}}</p><p>Please login and change your password.</p><a href=\"{{loginUrl}}\">Login to Your Account</a></body></html>",
    "isActive": true,
    "isDefault": true
  }'
```

#### 3. ACTIVATE Action - Activate Template (Deactivates Others)

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "ACTIVATE",
    "templateId": "template-001"
  }'
```

**Response:**
```json
{
  "message": "Template activated successfully (all others for same type/channel deactivated)",
  "statusCode": 200,
  "data": {
    "id": "template-001",
    "notificationType": "NEW_USER_REGISTERED",
    "channel": "EMAIL",
    "templateName": "Welcome Email Template",
    "subjectTemplate": "Welcome to {{companyName}}",
    "htmlTemplate": "<html>...</html>",
    "isActive": true,
    "isDefault": false,
    "createdAt": "2025-01-23T10:00:00",
    "updatedAt": "2025-01-23T10:05:00"
  }
}
```

#### 4. DEACTIVATE Action - Deactivate Template

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "DEACTIVATE",
    "templateId": "template-001"
  }'
```

#### 5. SET_DEFAULT Action - Set Template as Default (Activates and Deactivates Others)

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "SET_DEFAULT",
    "templateId": "template-001"
  }'
```

**Response:**
```json
{
  "message": "Template set as default successfully (all others for same type/channel deactivated)",
  "statusCode": 200,
  "data": {
    "id": "template-001",
    "notificationType": "NEW_USER_REGISTERED",
    "channel": "EMAIL",
    "templateName": "Welcome Email Template",
    "subjectTemplate": "Welcome to {{companyName}}",
    "htmlTemplate": "<html>...</html>",
    "isActive": true,
    "isDefault": true,
    "createdAt": "2025-01-23T10:00:00",
    "updatedAt": "2025-01-23T10:05:00"
  }
}
```

#### 6. PREVIEW Action - Preview Template

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "PREVIEW",
    "templateId": "template-001"
  }'
```

**Response:**
```json
{
  "message": "Template preview generated successfully",
  "statusCode": 200,
  "data": "<html><body><h1>Welcome to ASAT Learning Platform!</h1><p>Dear John Doe,</p><p>Your account has been successfully created.</p><p><strong>Email:</strong> john.doe@example.com</p><p><strong>Temporary Password:</strong> TempPass123!</p><p>Please login and change your password.</p><a href=\"https://portal.aspireelearning.com/auth/login\">Login to Your Account</a></body></html>"
}
```

#### 7. DELETE Action - Delete/Deactivate Template

```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "DELETE",
    "templateId": "template-001"
  }'
```

## Single Active Constraint Testing

### Test Scenario 1: Settings Single Active Constraint

1. **Enable first notification type:**
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "ENABLE",
    "notificationType": "NEW_USER_REGISTERED"
  }'
```

2. **Enable second notification type (should disable the first):**
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "ENABLE",
    "notificationType": "COURSE_ASSIGNED"
  }'
```

**Expected Result:** Only `COURSE_ASSIGNED` should be enabled, `NEW_USER_REGISTERED` should be automatically disabled.

### Test Scenario 2: Templates Single Active Constraint

1. **Create and activate first template:**
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "CREATE",
    "notificationType": "NEW_USER_REGISTERED",
    "channel": "EMAIL",
    "templateName": "Template 1",
    "subjectTemplate": "Welcome Template 1",
    "htmlTemplate": "<h1>Template 1</h1>",
    "isActive": true
  }'
```

2. **Create and activate second template for same type/channel:**
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "CREATE",
    "notificationType": "NEW_USER_REGISTERED",
    "channel": "EMAIL",
    "templateName": "Template 2",
    "subjectTemplate": "Welcome Template 2",
    "htmlTemplate": "<h1>Template 2</h1>",
    "isActive": true
  }'
```

**Expected Result:** Only Template 2 should be active, Template 1 should be automatically deactivated.

## Error Handling Examples

### 1. Invalid Action
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "INVALID_ACTION",
    "notificationType": "NEW_USER_REGISTERED"
  }'
```

**Response:**
```json
{
  "message": "Invalid action: INVALID_ACTION",
  "statusCode": 400,
  "data": null
}
```

### 2. Missing Required Fields
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "CREATE",
    "templateName": "Test Template"
  }'
```

**Response:**
```json
{
  "message": "Notification type, channel, and template name are required for create action",
  "statusCode": 400,
  "data": null
}
```

### 3. Template Not Found
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "ACTIVATE",
    "templateId": "non-existent-id"
  }'
```

**Response:**
```json
{
  "message": "Template not found with ID: non-existent-id",
  "statusCode": 404,
  "data": null
}
```

## Complete Workflow Example

### Step 1: Create Settings
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-settings/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "CREATE",
    "notificationType": "CERTIFICATE_ISSUED",
    "enabled": true,
    "description": "Settings for certificate issuance notifications",
    "emailEnabled": true,
    "inAppEnabled": true,
    "smsEnabled": false,
    "pushEnabled": false
  }'
```

### Step 2: Create Email Template
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "CREATE",
    "notificationType": "CERTIFICATE_ISSUED",
    "channel": "EMAIL",
    "templateName": "Certificate Issued Email",
    "subjectTemplate": "Congratulations! Your Certificate is Ready",
    "htmlTemplate": "<html><body><h1>🎉 Congratulations!</h1><p>Dear {{userName}},</p><p>You have successfully earned a certificate for completing <strong>{{courseName}}</strong>.</p><p><strong>Certificate ID:</strong> {{certificateId}}</p><p><strong>Score:</strong> {{score}}%</p><p>Your certificate is now available for download.</p><a href=\"{{certificateUrl}}\">Download Certificate</a></body></html>",
    "isActive": true,
    "isDefault": true
  }'
```

### Step 3: Create In-App Template
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "CREATE",
    "notificationType": "CERTIFICATE_ISSUED",
    "channel": "IN_APP",
    "templateName": "Certificate Issued In-App",
    "titleTemplate": "Certificate Earned!",
    "messageTemplate": "Congratulations! You have earned a certificate for {{courseName}}.",
    "isActive": true,
    "isDefault": true
  }'
```

### Step 4: Preview Email Template
```bash
curl -X POST "http://localhost:5656/notification/api/v1/admin/notification-templates/action" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "PREVIEW",
    "templateId": "{email-template-id}"
  }'
```

## Benefits of Dynamic APIs

### ✅ **Simplified API Management**
- Single endpoint per resource type
- Consistent request/response format
- Reduced API surface area

### ✅ **Single Active Constraint**
- Prevents configuration conflicts
- Ensures clear active state
- Automatic cleanup of conflicting settings

### ✅ **Flexible Action Handling**
- Easy to add new actions
- Centralized action logic
- Better error handling

### ✅ **Improved Developer Experience**
- Fewer endpoints to remember
- Consistent patterns
- Better documentation

## Migration from Individual APIs

The original individual APIs are still available for backward compatibility, but the dynamic APIs are recommended for new implementations:

**Old Way (Multiple Endpoints):**
```bash
# Enable notification type
curl -X PATCH "/api/v1/admin/notification-settings/type/NEW_USER_REGISTERED/enable"

# Enable channel
curl -X PATCH "/api/v1/admin/notification-settings/type/NEW_USER_REGISTERED/channel/EMAIL/enable"

# Activate template
curl -X PATCH "/api/v1/admin/notification-templates/template-001/activate"
```

**New Way (Single Dynamic Endpoint):**
```bash
# Enable notification type
curl -X POST "/api/v1/admin/notification-settings/action" -d '{"action": "ENABLE", "notificationType": "NEW_USER_REGISTERED"}'

# Enable channel
curl -X POST "/api/v1/admin/notification-settings/action" -d '{"action": "ENABLE_CHANNEL", "notificationType": "NEW_USER_REGISTERED", "channel": "EMAIL"}'

# Activate template
curl -X POST "/api/v1/admin/notification-templates/action" -d '{"action": "ACTIVATE", "templateId": "template-001"}'
```

## Testing Checklist

- [ ] All actions work correctly
- [ ] Single active constraint enforced for settings
- [ ] Single active constraint enforced for templates
- [ ] Error handling works for invalid requests
- [ ] Template preview generates correct content
- [ ] Channel enable/disable works correctly
- [ ] Default template setting works
- [ ] Template activation/deactivation works
- [ ] Migration from individual APIs works
- [ ] Swagger documentation is accurate

## Notes

- All dynamic APIs require proper authentication (implement as needed)
- Single active constraint ensures only one setting/template is active at a time
- Template preview uses sample data if none provided
- Deletion actually deactivates resources for data integrity
- All responses follow the standard ApiResponseDto format
- Swagger documentation is available at: `http://localhost:5656/notification/swagger-ui.html`
