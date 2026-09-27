# ASAT Notification System Implementation

This document provides a comprehensive guide to the enhanced notification system implemented for the ASAT platform.

## Overview

The ASAT notification system has been enhanced to support:
- **25+ Notification Types** across 8 categories
- **Multi-Channel Delivery** (Email, In-App, SMS, Push)
- **Admin Control** over notification settings
- **Template Management** with customization support
- **Priority Implementation** for 6 core notification types
- **MongoDB Database** for scalable data storage
- **Automatic Data Initialization** with test data
- **Branding Configuration** from properties file

## Core Components

### 1. Notification Types

The system supports the following notification categories:

#### User Management
- `NEW_USER_REGISTERED` - Sent when a new user registers
- `USER_PROFILE_UPDATED` - Sent when a user profile is updated
- `USER_SUSPENDED` - Sent when a user account is suspended/deactivated

#### Content Management
- `NEW_COURSE_CREATED` - Sent when a new course is created
- `COURSE_UPDATED` - Sent when a course is updated
- `COURSE_ASSIGNED` - Sent when a course is assigned to a user
- `COURSE_COMPLETION` - Sent when a user completes a course

#### Package Management
- `NEW_PACKAGE_CREATED` - Sent when a new package is created
- `PACKAGE_ASSIGNED` - Sent when a package is assigned to a user
- `PACKAGE_EXPIRY` - Sent when a package is about to expire
- `CAMPAIGN_ACTIVITY` - Sent for campaign activities

#### Payment Management
- `PAYMENT_SUCCESS` - Sent when a payment is successful
- `PAYMENT_FAILURE` - Sent when a payment fails
- `PENDING_PAYMENT` - Sent for pending payments
- `REFUND_REQUESTED` - Sent when a refund is requested

#### Policy Management
- `NEW_POLICY_CREATED` - Sent when a new policy is created
- `POLICY_UPDATED` - Sent when a policy is updated
- `POLICY_COMPLIANCE_REMINDER` - Sent for compliance reminders

#### Certificate Management
- `CERTIFICATE_ISSUED` - Sent when a certificate is issued
- `LEADERBOARD_UPDATE` - Sent when leaderboard is updated
- `CERTIFICATE_EXPIRY` - Sent when certificate is about to expire
- `CERTIFICATE_REVOKED` - Sent when certificate is revoked

#### System/Operational
- `SYSTEM_HEALTH_ALERTS` - Sent for system health alerts
- `SECURITY_ALERTS` - Sent for security alerts
- `UPDATES_PATCHES` - Sent for system updates
- `FEATURE_UPDATE_CHANGE` - Sent for feature updates

#### General
- `REMINDER_PENDING_TASKS` - Sent for task reminders
- `SCHEDULED_MAINTENANCE` - Sent for maintenance notifications
- `HELP_DESK_TICKET_UPDATES` - Sent for ticket updates
- `SUBSCRIPTION_RENEWAL_REMINDER` - Sent for renewal reminders

### 2. Notification Channels

- **EMAIL** - HTML email notifications via AWS SES/SMTP
- **IN_APP** - In-app notifications stored in MongoDB
- **SMS** - SMS notifications (future implementation)
- **PUSH** - Push notifications (future implementation)

### 3. Priority Implementation

The following 6 notification types are prioritized for initial implementation:
1. `NEW_USER_REGISTERED`
2. `USER_SUSPENDED`
3. `COURSE_ASSIGNED`
4. `COURSE_COMPLETION`
5. `PACKAGE_ASSIGNED`
6. `CERTIFICATE_ISSUED`

## API Usage

### Send Notification

```http
POST /notification/api/v1/send-notification
Content-Type: application/json

{
  "to": "user@example.com",
  "userId": "user123",
  "notificationType": "NEW_USER_REGISTERED",
  "channels": ["EMAIL", "IN_APP"],
  "templateModel": {
    "userName": "John Doe",
    "email": "user@example.com",
    "tempPassword": "temp123",
    "signupDate": "January 15, 2025"
  },
  "priority": "HIGH"
}
```

**Note**: Branding properties (`companyName`, `supportEmail`, `loginUrl`, `logoUrl`, `baseUrl`) are automatically injected from configuration.

### Get User Notifications (Paginated)

```http
GET /notification/api/v1/notification-management/in-app?offset=0&pageSize=20
```

**Note**: The `userId` is automatically extracted from the JWT token in the request headers.

**Response:**
```json
{
  "message": "User notifications retrieved successfully",
  "statusCode": 200,
  "data": {
    "offset": 0,
    "pageSize": 20,
    "total": 5,
    "items": [
      {
        "id": "notification-001",
        "userId": "user123",
        "title": "Welcome to ASAT Platform",
        "message": "Your account has been created successfully",
        "isRead": false,
        "createdAt": "2025-01-23T10:00:00"
      }
    ]
  }
}
```

### Get Unread Notifications (Paginated)

```http
GET /notification/api/v1/notification-management/in-app/unread?offset=0&pageSize=20
```

### Get Unread Count

```http
GET /notification/api/v1/notification-management/in-app/count
```

### Mark Notification as Read

```http
PUT /notification/api/v1/notification-management/in-app/{notificationId}/read
```

### Mark All as Read

```http
PUT /notification/api/v1/notification-management/in-app/read-all
```

### Get Notification Types

```http
GET /notification/api/v1/notification-management/types
```

### Get Priority Notification Types

```http
GET /notification/api/v1/notification-management/types/priority
```

## Admin APIs

### Dynamic Notification Settings Management (Paginated)

```http
GET /notification/api/v1/admin/notification-settings?offset=0&pageSize=20
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
        "description": "Settings for new user registration",
        "emailEnabled": true,
        "inAppEnabled": true,
        "smsEnabled": false,
        "pushEnabled": false
      }
    ]
  }
}
```

### Dynamic Action for Settings

```http
POST /notification/api/v1/admin/notification-settings/action
Content-Type: application/json

{
  "action": "ENABLE",
  "notificationType": "COURSE_ASSIGNED"
}
```

**Available Actions**: `CREATE`, `UPDATE`, `ENABLE`, `DISABLE`, `ENABLE_CHANNEL`, `DISABLE_CHANNEL`, `DELETE`

**Single Active Constraint**: Only one notification type can be enabled at a time.

### Dynamic Notification Templates Management (Paginated)

```http
GET /notification/api/v1/admin/notification-templates?offset=0&pageSize=20
```

### Dynamic Action for Templates

```http
POST /notification/api/v1/admin/notification-templates/action
Content-Type: application/json

{
  "action": "ACTIVATE",
  "templateId": "template-001"
}
```

**Available Actions**: `CREATE`, `UPDATE`, `ACTIVATE`, `DEACTIVATE`, `SET_DEFAULT`, `PREVIEW`, `DELETE`

**Single Active Constraint**: Only one template can be active per notification type and channel combination.

## Database Implementation

### MongoDB Collections

The system uses MongoDB with the following collections:

1. **notification_settings** - Stores notification type configurations
2. **notification_templates** - Stores email and in-app templates
3. **in_app_notifications** - Stores in-app notification messages
4. **notification_history** - Stores notification delivery history

### Automatic Data Initialization

The system automatically initializes test data on startup:

- **25+ Notification Settings** - All notification types configured
- **12 Notification Templates** - Email and In-App templates for priority types
- **5 Sample In-App Notifications** - Ready-to-test notifications

Configuration:
```yaml
notification:
  data:
    initialize-on-startup: true
    skip-if-data-exists: true
```

## Template System

### Template Processing

Templates use simple string replacement with `{{variableName}}` format:

```html
<p>Dear {{userName}},</p>
<p><strong>Email:</strong> {{email}}</p>
<p><strong>Temporary Password:</strong> {{tempPassword}}</p>
<a href="{{loginUrl}}">Login to Your Account</a>
```

### Branding Configuration

Branding properties are automatically injected from `application.yml`:

```yaml
notification:
  branding:
    company-name: "ASAT Learning Platform"
    support-email: "support@securityawarenesstraining.ai"
    login-url: "https://portal.aspireelearning.com/auth/login"
    logo-url: "https://aspiretss.s3.us-east-1.amazonaws.com/public/Image.png"
    base-url: "https://portal.aspireelearning.com"
```

## Configuration

### Application Properties

```yaml
# MongoDB Configuration
spring:
  data:
    mongodb:
      uri: mongodb://user:password@localhost:27017/test?authSource=admin

# Notification Branding
notification:
  branding:
    company-name: "ASAT Learning Platform"
    support-email: "support@securityawarenesstraining.ai"
    login-url: "https://portal.aspireelearning.com/auth/login"
    logo-url: "https://aspiretss.s3.us-east-1.amazonaws.com/public/Image.png"
    base-url: "https://portal.aspireelearning.com"

# AWS SES Configuration
aws:
  ses:
    sender-email: "info@aspiretss.com"
    smtp:
      host: "email-smtp.us-east-1.amazonaws.com"
      port: 587
      username: "your-smtp-username"
      password: "your-smtp-password"
```

## Error Handling

The system includes comprehensive error handling:

- **Template Processing Errors** - Clear error messages with fallback
- **Channel Delivery Failures** - Logged and can be retried
- **Missing Settings** - Default settings are auto-initialized
- **Invalid Notification Types** - Validation prevents invalid requests
- **Database Connection Issues** - Proper error handling and logging

## Testing

### Test Data

The system includes comprehensive test data for immediate testing:

#### Email Testing with YOPmail
- `newuser@yopmail.com` - For new user registration tests
- `student@yopmail.com` - For course assignment tests
- `graduate@yopmail.com` - For certificate tests
- `testuser@yopmail.com` - For general testing

Access YOPmail inboxes at: https://yopmail.com

#### Sample Test Payloads

**NEW_USER_REGISTERED Email:**
```json
{
  "to": "newuser@yopmail.com",
  "userId": "user-001",
  "subject": "Welcome to ASAT Platform!",
  "notificationType": "NEW_USER_REGISTERED",
  "channels": ["EMAIL"],
  "templateModel": {
    "userName": "John Doe",
    "email": "newuser@yopmail.com",
    "tempPassword": "TempPass123!",
    "signupDate": "2025-01-23"
  }
}
```

**COURSE_ASSIGNED Email:**
```json
{
  "to": "student@yopmail.com",
  "userId": "user-002",
  "subject": "New Course Assigned",
  "notificationType": "COURSE_ASSIGNED",
  "channels": ["EMAIL"],
  "templateModel": {
    "userName": "Jane Smith",
    "courseName": "Cybersecurity Fundamentals",
    "courseCode": "CS-101",
    "dueDate": "February 15, 2025",
    "assignedBy": "Dr. Security Expert",
    "estimatedDuration": "4 weeks",
    "courseDescription": "Learn the fundamentals of cybersecurity.",
    "courseUrl": "https://portal.aspireelearning.com/courses/CS-101"
  }
}
```

**Multi-Channel Notification:**
```json
{
  "to": "testuser@yopmail.com",
  "userId": "user-006",
  "subject": "Multi-Channel Test",
  "notificationType": "COURSE_ASSIGNED",
  "channels": ["EMAIL", "IN_APP"],
  "templateModel": {
    "userName": "Test User",
    "courseName": "Test Course",
    "courseCode": "TEST-001",
    "dueDate": "March 1, 2025",
    "assignedBy": "Test Instructor",
    "estimatedDuration": "2 weeks",
    "courseDescription": "This is a test course for multi-channel notification testing.",
    "courseUrl": "https://portal.aspireelearning.com/courses/TEST-001"
  }
}
```

### Manual Testing

1. **Start the Notification Service**:
```bash
cd /Users/mr/asat/ASAT-V2-BACKEND
./gradlew :services:notification:service:bootRun
```

2. **Test Email Notification**:
```bash
curl -X POST http://localhost:5656/notification/api/v1/send-notification \
  -H "Content-Type: application/json" \
  -d '{
    "to": "newuser@yopmail.com",
    "userId": "user-001",
    "subject": "Welcome to ASAT Platform!",
    "notificationType": "NEW_USER_REGISTERED",
    "channels": ["EMAIL"],
    "templateModel": {
      "userName": "John Doe",
      "email": "newuser@yopmail.com",
      "tempPassword": "TempPass123!",
      "signupDate": "2025-01-23"
    }
  }'
```

3. **Check In-App Notifications**:
```bash
curl http://localhost:5656/notification/api/v1/notification-management/in-app/user-001
```

## Monitoring and Logging

All notification activities are logged with appropriate levels:

```java
log.info("🚀 Starting notification system data initialization...");
log.info("📊 Initializing notification settings...");
log.info("✅ Initialized {} notification settings", settings.size());
log.error("Failed to deliver notification via {}: {}", channel, e.getMessage(), e);
```

## Future Enhancements

1. **SMS Integration** - Add SMS delivery capability
2. **Push Notifications** - Implement push notification service
3. **Admin Panel** - Create web interface for notification management
4. **Analytics** - Add notification delivery analytics
5. **Scheduling** - Support for scheduled notifications
6. **User Preferences** - Allow users to customize notification preferences
7. **Template Editor** - Visual template editor for admins
8. **Notification History** - Detailed delivery tracking and reporting

## Support

For questions or issues with the notification system:

1. Check the logs for error messages
2. Verify notification settings are enabled
3. Ensure templates exist for the notification type
4. Check MongoDB connection and data initialization
5. Verify AWS SES configuration for email delivery
6. Contact the development team for assistance

---

**Note**: This implementation provides a complete notification system with MongoDB storage, automatic data initialization, branding configuration, and comprehensive testing capabilities.
