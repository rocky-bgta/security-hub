# Email Sending Analysis: Notification Service & Registration Service

## Overview

This document provides a comprehensive analysis of how emails are sent to users through the integration between the **Registration Service** and **Notification Service** in the ASAT platform.

---

## Architecture Overview

```
┌─────────────────────┐         ┌──────────────────────┐         ┌─────────────────────┐
│  Registration       │         │   Notification      │         │   AWS SES/SMTP     │
│  Service            │────────▶│   Service           │────────▶│   Email Provider   │
│                     │  HTTP   │                     │  SMTP    │                     │
└─────────────────────┘         └──────────────────────┘         └─────────────────────┘
       │                                  │
       │                                  │
       ▼                                  ▼
┌─────────────────────┐         ┌──────────────────────┐
│  Registration       │         │   AWS SQS Queue      │
│  Notification       │         │   (Message Queue)    │
│  Client             │         │                      │
└─────────────────────┘         └──────────────────────┘
```

---

## 1. Registration Service - Email Sending Flow

### 1.1 Components

#### **RegistrationNotificationClient**
**Location**: `services/registration/core/src/main/java/com/aspire/asat/registration/client/RegistrationNotificationClient.java`

**Purpose**: Registration-service-specific wrapper around the common `NotificationClient` that provides convenient methods for registration-related notifications.

**Key Methods**:
- `sendWelcomeEmailNotification()` - Sends welcome emails after user registration
- `sendProductReassignmentNotification()` - Sends notifications when products are reassigned
- `sendNewUserNotificationToAdmin()` - Notifies admin when a new user registers
- `sendPackageAssignedNotification()` - Notifies users and admins about package assignments
- `sendBulkImportSummaryNotification()` - Sends summary after bulk user import

**Example Usage**:
```java
// In ClientAdminServiceImpl.java
notificationServiceClient.sendWelcomeEmailNotification(
    userEmail, userId, clientAdminId, userName, password, attachments
);
```

#### **NotificationClient (Common Library)**
**Location**: `common/core/src/main/java/com/aspire/asat/common/client/NotificationClient.java`

**Purpose**: Common interface used by all services to send notifications.

**Key Methods**:
- `sendMultiChannelNotification()` - Sends via multiple channels (EMAIL + IN_APP)
- `sendCustomChannelNotification()` - Sends via specified channels with attachments
- `sendEmailNotification()` - Sends email only
- `sendInAppNotification()` - Sends in-app notification only

#### **NotificationClientImpl**
**Location**: `common/core/src/main/java/com/aspire/asat/common/client/impl/NotificationClientImpl.java`

**Purpose**: Implementation that makes HTTP REST calls to the Notification Service.

**Configuration**:
- `notification.service.url`: Base URL of notification service (default: `http://asat-notification-service`)
- `notification.service.api-path`: API path (default: `/notification/api/v1`)

**Flow**:
1. Builds `NotificationRequestDto` with notification details
2. Makes HTTP POST request to `/notification/api/v1/send-notification`
3. Returns `true` if successful (2xx status), `false` otherwise

---

## 2. Notification Service - Email Processing Flow

### 2.1 Entry Point: REST API

**Endpoint**: `POST /notification/api/v1/send-notification`

**Controller**: `NotificationController` → `NotificationControllerImpl`

**Request DTO**: `NotificationRequestDto`
```java
{
    "to": "user@example.com",
    "userId": "user-123",
    "clientAdminId": "client-admin-456",  // Optional, for client-specific settings
    "notificationType": "WELCOME_EMAIL",
    "channels": ["EMAIL", "IN_APP"],
    "templateModel": {
        "userName": "John Doe",
        "email": "user@example.com",
        "password": "temp123"
    },
    "attachments": [...]  // Optional
}
```

### 2.2 Notification Delivery Service

**Location**: `services/notification/core/src/main/java/com/aspire/asat/notification/service/NotificationDeliveryService.java`

**Responsibilities**:
1. **Validate Notification Settings**: Checks if notification type is enabled (globally and client-specific)
2. **Check Channel Preferences**: Verifies if channels are enabled for the notification type
3. **Route to Channel Handlers**: Delegates to specific channel delivery methods

**Key Method**: `deliverNotification(NotificationRequestDto request, String logId)`

**Flow**:
```
1. Check if notification type is enabled
   ├─ Global settings check
   └─ Client-specific settings check (if clientAdminId provided)

2. For each requested channel:
   ├─ Check if channel is enabled
   ├─ EMAIL → deliverEmailNotification()
   ├─ IN_APP → deliverInAppNotification()
   ├─ SMS → deliverSmsNotification() (not implemented)
   └─ PUSH → deliverPushNotification() (not implemented)
```

### 2.3 Email Delivery Process

**Method**: `deliverEmailNotification(NotificationRequestDto request, String logId)`

**Steps**:

1. **Get Email Template**:
   ```java
   Optional<NotificationTemplate> templateOpt = templateService.getTemplate(
       request.getNotificationType(), NotificationChannel.EMAIL
   );
   ```

2. **Enrich Template Model**:
   - Merges user-provided `templateModel` with branding properties
   - Adds: `companyName`, `supportEmail`, `loginUrl`, `logoUrl`, `baseUrl`, `currentYear`

3. **Generate Subject**:
   ```java
   String subject = templateService.generateSubject(template, enrichedModel);
   ```

4. **Create EmailDto**:
   ```java
   EmailDto emailDto = new EmailDto();
   emailDto.setTo(request.getTo());
   emailDto.setSubject(subject);
   emailDto.setTemplateId(template.getId());
   emailDto.setTemplateModel(enrichedModel);
   emailDto.setAttachments(request.getAttachments());
   ```

5. **Queue Email**:
   ```java
   producerService.queueEmailNotification(emailDto);
   ```

### 2.4 Message Queue (AWS SQS)

**Producer**: `NotificationProducerServiceImpl`

**Location**: `services/notification/core/src/main/java/com/aspire/asat/notification/service/impl/NotificationProducerServiceImpl.java`

**Process**:
1. Converts `EmailDto` to JSON string
2. Sends message to AWS SQS queue
3. Returns queued `EmailDto`

**Configuration**:
- `aws.sqs.queue-url`: SQS queue URL
- `aws.sqs.queue-name`: SQS queue name

**Why SQS?**:
- **Asynchronous Processing**: Decouples email sending from API response
- **Reliability**: Messages are persisted and can be retried
- **Scalability**: Multiple consumers can process messages
- **Error Handling**: Failed messages go to DLQ (Dead Letter Queue)

### 2.5 Message Consumer (SQS Listener)

**Location**: `services/notification/core/src/main/java/com/aspire/asat/notification/listener/SqsListener.java`

**Process**:
1. **Polling**: Scheduled task runs every 2 seconds (`@Scheduled(fixedDelay = 2000)`)
2. **Receive Messages**: Fetches up to 10 messages from SQS queue
3. **Process**: Deserializes JSON to `EmailDto` and calls `ConsumerService.processEmailRequest()`
4. **Delete**: Removes message from queue after successful processing

**Error Handling**:
- **Parsing Errors**: Message not deleted (will retry)
- **Processing Errors**: Message not deleted (will retry)
- **Success**: Message deleted from queue

### 2.6 Email Consumer Service

**Location**: `services/notification/core/src/main/java/com/aspire/asat/notification/service/impl/ConsumerServiceImpl.java`

**Method**: `processEmailRequest(EmailDto emailRequest)`

**Steps**:

1. **Extract Log ID** (for tracking):
   ```java
   String logId = extractLogId(emailRequest);
   ```

2. **Fetch Template**:
   ```java
   NotificationTemplate template = fetchTemplate(emailRequest, logId);
   ```

3. **Generate HTML Body**:
   ```java
   String htmlBody = generateHtmlBody(template, emailRequest, logId);
   ```

4. **Send Email**:
   ```java
   if (hasAttachments) {
       smtpEmailSender.sendEmailWithAttachments(emailRequest);
   } else {
       smtpEmailSender.sendEmailWithTemplate(...);
   }
   ```

5. **Update Status**:
   ```java
   updateNotificationStatus(logId, NotificationChannel.EMAIL, ChannelStatus.SUCCESS, null);
   ```

### 2.7 SMTP Email Sender

**Location**: `services/notification/core/src/main/java/com/aspire/asat/notification/service/impl/SmtpEmailSenderImpl.java`

**Purpose**: Actual email sending via AWS SES SMTP.

**Configuration**:
```yaml
aws:
  ses:
    sender-email: "info@aspiretss.com"
    smtp:
      host: "email-smtp.us-east-1.amazonaws.com"
      port: 587
      username: "your-smtp-username"
      password: "your-smtp-password"
      from-name: "ASAT Platform"
```

**Methods**:

1. **`sendEmail()`**: Basic email sending
   - Creates SMTP session
   - Builds `MimeMessage` with HTML content
   - Connects to AWS SES SMTP
   - Sends email
   - Closes connection

2. **`sendEmailWithTemplate()`**: Template-based email
   - Fetches template from database
   - Generates HTML from template with variables
   - Calls `sendEmail()`

3. **`sendEmailWithAttachments()`**: Email with file attachments
   - Generates HTML from template (if provided)
   - Downloads attachments from S3
   - Creates multipart message
   - Attaches files
   - Sends email

**Attachment Handling**:
- Attachments are stored in S3
- `AttachmentDto` contains: `bucketName`, `objectKey`
- Files are downloaded from S3 and attached to email

---

## 3. Template System

### 3.1 Template Storage

**Collection**: `notification_templates` (MongoDB)

**Model**: `NotificationTemplate`

**Fields**:
- `id`: Template ID
- `notificationType`: Type of notification (e.g., `WELCOME_EMAIL`)
- `channel`: Channel type (`EMAIL`, `IN_APP`, `SMS`, `PUSH`)
- `subjectTemplate`: Email subject template
- `htmlTemplate`: HTML body template
- `isActive`: Whether template is active
- `isDefault`: Whether template is default
- `organizationId`: For multi-tenant customization

### 3.2 Template Processing

**Service**: `NotificationTemplateService`

**Method**: `generateHtmlContent(NotificationTemplate template, Map<String, Object> model)`

**Process**:
1. Gets HTML template string
2. Replaces variables: `{{variableName}}` → actual value
3. Returns processed HTML

**Example Template**:
```html
<h1>Welcome {{userName}}!</h1>
<p>Your email: {{email}}</p>
<p>Temporary password: {{password}}</p>
<a href="{{loginUrl}}">Login</a>
```

**Example Model**:
```java
{
    "userName": "John Doe",
    "email": "john@example.com",
    "password": "temp123",
    "loginUrl": "https://portal.aspireelearning.com/auth/login"
}
```

**Result**:
```html
<h1>Welcome John Doe!</h1>
<p>Your email: john@example.com</p>
<p>Temporary password: temp123</p>
<a href="https://portal.aspireelearning.com/auth/login">Login</a>
```

### 3.3 Branding Properties

**Configuration**: `application.yml`
```yaml
notification:
  branding:
    company-name: "ASAT Learning Platform"
    support-email: "support@securityawarenesstraining.ai"
    login-url: "https://portal.aspireelearning.com/auth/login"
    logo-url: "https://aspiretss.s3.us-east-1.amazonaws.com/public/Image.png"
    base-url: "https://portal.aspireelearning.com"
```

**Auto-Injection**: These properties are automatically added to every template model in `enrichTemplateModel()`.

---

## 4. Notification Settings & Preferences

### 4.1 Global Settings

**Collection**: `notification_settings` (MongoDB)

**Purpose**: Control which notification types and channels are enabled globally.

### 4.2 Client-Specific Settings

**Collection**: `client_notification_settings` (MongoDB)

**Purpose**: Allow clients to override global settings.

**Flow**:
1. Check global settings first
2. If client-specific settings exist, use them
3. If not, use global settings

**Example**:
- Global: `WELCOME_EMAIL` enabled
- Client A: `WELCOME_EMAIL` disabled (overrides global)
- Client B: No setting (uses global = enabled)

---

## 5. Complete Email Flow Example

### Scenario: User Registration Welcome Email

**Step 1: Registration Service**
```java
// In EndUserServiceImpl.java
notificationClient.sendWelcomeEmailNotification(
    userEmail,      // "user@example.com"
    userId,         // "user-123"
    clientAdminId,  // "client-admin-456"
    userName,       // "John Doe"
    password        // "temp123"
);
```

**Step 2: NotificationClient (Common)**
```java
// Builds NotificationRequestDto
NotificationRequestDto requestDto = NotificationRequestDto.builder()
    .to("user@example.com")
    .userId("user-123")
    .clientAdminId("client-admin-456")
    .notificationType(NotificationType.WELCOME_EMAIL)
    .channels(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP))
    .templateModel(Map.of(
        "userName", "John Doe",
        "email", "user@example.com",
        "password", "temp123"
    ))
    .build();

// Makes HTTP POST to notification service
POST /notification/api/v1/send-notification
```

**Step 3: Notification Service - Delivery**
```java
// NotificationDeliveryService.deliverNotification()
1. Check if WELCOME_EMAIL is enabled (global + client-specific)
2. Check if EMAIL channel is enabled
3. Call deliverEmailNotification()
```

**Step 4: Email Template Processing**
```java
// NotificationDeliveryService.deliverEmailNotification()
1. Get EMAIL template for WELCOME_EMAIL
2. Enrich template model with branding properties
3. Generate subject from template
4. Create EmailDto
5. Queue email via SQS
```

**Step 5: Message Queue**
```java
// NotificationProducerServiceImpl.queueEmailNotification()
1. Convert EmailDto to JSON
2. Send to AWS SQS queue
3. Return immediately (async)
```

**Step 6: SQS Consumer**
```java
// SqsListener.pollMessages() (runs every 2 seconds)
1. Receive messages from SQS (up to 10)
2. For each message:
   - Deserialize to EmailDto
   - Call ConsumerService.processEmailRequest()
   - Delete message on success
```

**Step 7: Email Generation & Sending**
```java
// ConsumerServiceImpl.processEmailRequest()
1. Fetch template from database
2. Generate HTML body with variables
3. Call SmtpEmailSender.sendEmailWithTemplate()
```

**Step 8: SMTP Sending**
```java
// SmtpEmailSenderImpl.sendEmailWithTemplate()
1. Fetch template
2. Generate HTML: "Welcome {{userName}}!" → "Welcome John Doe!"
3. Create MimeMessage
4. Connect to AWS SES SMTP (email-smtp.us-east-1.amazonaws.com:587)
5. Send email
6. Close connection
```

**Step 9: Email Delivered**
```
✅ Email received by user@example.com
```

---

## 6. Key Features

### 6.1 Multi-Channel Support
- **EMAIL**: HTML emails via AWS SES SMTP
- **IN_APP**: Stored in MongoDB, retrieved via API
- **SMS**: Placeholder (not implemented)
- **PUSH**: Placeholder (not implemented)

### 6.2 Template Management
- Templates stored in MongoDB
- Variable replacement: `{{variableName}}`
- Branding properties auto-injected
- Support for multi-tenant customization

### 6.3 Attachment Support
- Attachments stored in S3
- Downloaded and attached to emails
- Supports multiple attachments per email

### 6.4 Client-Specific Preferences
- Global notification settings
- Client-level overrides
- Per-channel preferences
- Per-notification-type preferences

### 6.5 Asynchronous Processing
- SQS message queue for decoupling
- Non-blocking API responses
- Retry mechanism for failed messages
- Dead Letter Queue (DLQ) for poison messages

### 6.6 Notification History
- All notifications logged in `notification_history` collection
- Tracks delivery status per channel
- Status: `SUCCESS`, `FAILED`, `SKIPPED`

---

## 7. Configuration

### 7.1 Registration Service

**application.yml**:
```yaml
notification:
  service:
    url: http://asat-notification-service
    api-path: /notification/api/v1
```

### 7.2 Notification Service

**application.yml**:
```yaml
# AWS SES SMTP
aws:
  ses:
    sender-email: "info@aspiretss.com"
    smtp:
      host: "email-smtp.us-east-1.amazonaws.com"
      port: 587
      username: "${AWS_SES_SMTP_USERNAME}"
      password: "${AWS_SES_SMTP_PASSWORD}"
      from-name: "ASAT Platform"

# AWS SQS
aws:
  sqs:
    queue-url: "${AWS_SQS_QUEUE_URL}"
    queue-name: "${AWS_SQS_QUEUE_NAME}"

# MongoDB
spring:
  data:
    mongodb:
      uri: "${MONGODB_URI}"

# Branding
notification:
  branding:
    company-name: "ASAT Learning Platform"
    support-email: "support@securityawarenesstraining.ai"
    login-url: "https://portal.aspireelearning.com/auth/login"
    logo-url: "https://aspiretss.s3.us-east-1.amazonaws.com/public/Image.png"
    base-url: "https://portal.aspireelearning.com"
```

---

## 8. Error Handling

### 8.1 Registration Service
- **HTTP Failures**: Logged, returns `false`
- **Exceptions**: Caught and logged, doesn't break registration flow

### 8.2 Notification Service
- **Template Not Found**: Returns 400 error
- **SMTP Failures**: Logged, message stays in queue for retry
- **SQS Failures**: Message not deleted, will retry
- **Parsing Errors**: Message not deleted, goes to DLQ after max retries

---

## 9. Monitoring & Logging

### 9.1 Log Points

**Registration Service**:
- `log.info("Sending welcome email notification for user: {}")`
- `log.error("Failed to send welcome email notification: {}")`

**Notification Service**:
- `log.info("Delivering email notification for type: {}")`
- `log.info("Email notification queued successfully")`
- `log.info("Processing email request for template '{}' to '{}'")`
- `log.info("Email sent successfully via SMTP to: {}")`
- `log.error("Failed to send email via SMTP: {}")`

### 9.2 Status Tracking

- **Notification History**: All notifications logged with status
- **Channel Status**: Per-channel delivery status tracked
- **Error Messages**: Stored in notification history for debugging

---

## 10. Common Use Cases

### 10.1 Welcome Email After Registration
```java
// Registration Service
notificationServiceClient.sendWelcomeEmailNotification(
    email, userId, clientAdminId, userName, password
);
```

### 10.2 Welcome Email with Invoice Attachment
```java
// Registration Service
List<AttachmentDto> attachments = List.of(
    AttachmentDto.builder()
        .bucketName("invoice-bucket")
        .objectKey("invoices/invoice-123.pdf")
        .build()
);

notificationServiceClient.sendWelcomeEmailNotification(
    email, userId, clientAdminId, userName, password, attachments
);
```

### 10.3 Product Reassignment Notification
```java
// Registration Service
Map<String, Object> templateModel = new HashMap<>();
templateModel.put("userName", clientAdmin.getEmail());
templateModel.put("logoUrl", clientAdmin.getLogoUrl());

notificationServiceClient.sendProductReassignmentNotification(
    clientAdmin.getEmail(),
    clientAdmin.getId(),
    clientAdmin.getId(),
    templateModel,
    attachments
);
```

### 10.4 Package Assignment Notification
```java
// Registration Service
PackageAssignmentNotificationDto dto = PackageAssignmentNotificationDto.builder()
    .userEmail("user@example.com")
    .userId("user-123")
    .userName("John Doe")
    .adminEmail("admin@example.com")
    .adminId("admin-456")
    .adminName("Admin User")
    .clientAdminId("client-admin-789")
    .packageName("Cybersecurity Fundamentals")
    .packageDetails("Learn cybersecurity basics")
    .assignmentDate("2025-01-23")
    .build();

notificationServiceClient.sendPackageAssignedNotification(dto);
```

---

## 11. Best Practices

### 11.1 Template Design
- Use clear variable names: `{{userName}}` not `{{u}}`
- Include branding properties in templates
- Test templates with sample data
- Keep HTML simple and email-client compatible

### 11.2 Error Handling
- Always catch exceptions in registration service
- Don't break user flow if email fails
- Log errors for debugging
- Use DLQ for poison messages

### 11.3 Performance
- Use async methods (`@Async`) for non-critical emails
- Batch notifications when possible
- Monitor SQS queue depth
- Scale consumers based on load

### 11.4 Security
- Never log passwords in templates
- Validate email addresses
- Sanitize template variables
- Use HTTPS for API calls

---

## 12. Troubleshooting

### 12.1 Email Not Received
1. Check notification settings (enabled?)
2. Check channel preferences (EMAIL enabled?)
3. Check template exists and is active
4. Check SMTP configuration
5. Check SQS queue for stuck messages
6. Check notification history for errors

### 12.2 Template Variables Not Replaced
1. Verify variable names match exactly
2. Check template model contains all variables
3. Check template processing logs
4. Verify template format: `{{variableName}}`

### 12.3 SMTP Connection Failures
1. Verify AWS SES credentials
2. Check SMTP host and port
3. Verify sender email is verified in SES
4. Check network connectivity
5. Review AWS SES sending limits

### 12.4 SQS Messages Not Processed
1. Check SQS listener is running
2. Verify queue URL configuration
3. Check consumer service logs
4. Verify message format matches EmailDto
5. Check DLQ for failed messages

---

## Conclusion

The email sending system in ASAT is a robust, scalable, and flexible solution that:

- **Decouples** email sending from business logic
- **Supports** multiple channels and templates
- **Provides** client-specific customization
- **Ensures** reliability through message queuing
- **Tracks** all notifications for monitoring

The integration between Registration Service and Notification Service follows microservices best practices with clear separation of concerns, asynchronous processing, and comprehensive error handling.

