# ASAT Notification System - Comprehensive Test Data Guide

## Overview

The ASAT Notification System includes an automatic data initializer that runs on service startup to populate the MongoDB database with comprehensive test data for all 25+ notification types. This guide provides detailed test data for each notification type to ensure thorough testing coverage.

## Data Initializer Service

### Service Details
- **Class**: `NotificationDataInitializer`
- **Location**: `services/notification/core/src/main/java/com/aspire/asat/notification/service/NotificationDataInitializer.java`
- **Type**: Spring Boot `CommandLineRunner` - executes automatically on startup
- **Configuration**: Controlled via `application.yml` properties

### Configuration Properties

```yaml
notification:
  data:
    initialize-on-startup: true    # Enable/disable data initialization
    skip-if-data-exists: true     # Skip if data already exists
```

## Complete Test Data for All Notification Types

### 1. User Management Notifications

#### NEW_USER_REGISTERED
**Purpose**: Admin notification when a new user registers
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "New User Registration Alert",
  "notificationType": "NEW_USER_REGISTERED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "John Doe",
    "userEmail": "john.doe@example.com",
    "timestamp": "2025-01-23 10:30:00"
  }
}
```

#### WELCOME_EMAIL
**Purpose**: Welcome email sent to new users after registration
**Test Data**:
```json
{
  "to": "john.doe@yopmail.com",
  "userId": "user-001",
  "subject": "Welcome to ASAT Platform!",
  "notificationType": "WELCOME_EMAIL",
  "channels": ["EMAIL", "IN_APP"],
  "templateModel": {
    "userName": "John Doe",
    "email": "john.doe@yopmail.com",
    "platformFeatures": "Course Management, Progress Tracking, Certificates, Leaderboards",
    "supportContact": "support@asat-platform.com",
    "loginUrl": "https://portal.aspireelearning.com/auth/login",
    "currentYear": "2025"
  }
}
```

#### USER_PROFILE_UPDATED
**Purpose**: Admin notification when a user profile is updated
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "User Profile Update Alert",
  "notificationType": "USER_PROFILE_UPDATED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Jane Smith",
    "updatedFields": "Email, Phone Number, Department",
    "timestamp": "2025-01-23 14:15:00"
  }
}
```

#### USER_SUSPENDED
**Purpose**: Admin notification when user account is suspended
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "User Account Suspension Alert",
  "notificationType": "USER_SUSPENDED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Bob Wilson",
    "reason": "Policy violation - unauthorized access attempts",
    "timestamp": "2025-01-23 16:45:00"
  }
}
```

### 2. Course Management Notifications

#### NEW_COURSE_CREATED
**Purpose**: Admin notification when new course is created
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "New Course Created Alert",
  "notificationType": "NEW_COURSE_CREATED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "courseTitle": "Advanced Cybersecurity Fundamentals",
    "courseDescription": "Comprehensive course covering advanced cybersecurity concepts, threat analysis, and defense strategies.",
    "permissions": "All authenticated users"
  }
}
```

#### COURSE_UPDATED
**Purpose**: Admin notification when course is updated
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Course Update Alert",
  "notificationType": "COURSE_UPDATED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "courseTitle": "Data Science Fundamentals",
    "updatedFields": "Course content, Duration, Prerequisites",
    "timestamp": "2025-01-23 11:20:00"
  }
}
```

#### COURSE_ASSIGNED
**Purpose**: Admin notification when course is assigned to user
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Course Assignment Alert",
  "notificationType": "COURSE_ASSIGNED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Alice Johnson",
    "courseTitle": "Machine Learning Basics",
    "assignmentDate": "2025-01-23"
  }
}
```

#### COURSE_COMPLETION
**Purpose**: Admin notification when user completes course
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Course Completion Alert",
  "notificationType": "COURSE_COMPLETION",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Mike Davis",
    "courseTitle": "Cloud Computing Essentials",
    "timestamp": "2025-01-23 15:30:00"
  }
}
```

### 3. Package Management Notifications

#### NEW_PACKAGE_CREATED
**Purpose**: Admin notification when new package is created
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "New Package Created Alert",
  "notificationType": "NEW_PACKAGE_CREATED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "packageName": "Enterprise Security Suite",
    "contents": "5 Security Courses, 3 Certifications, 1 Workshop",
    "pricing": "$299.99 per user"
  }
}
```

#### PACKAGE_ASSIGNED
**Purpose**: Admin notification when a course is assigned to a user
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Course Assignment Notification - ASAT Platform",
  "notificationType": "PACKAGE_ASSIGNED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Sarah Wilson",
    "packageName": "Professional Development Course",
    "packageDetails": "Professional Development Course - 10 modules",
    "assignmentDate": "23 January 2025"
  }
}
```

#### PACKAGE_ASSIGNED_USER
**Purpose**: User notification when a course is assigned to them
**Test Data**:
```json
{
  "to": "sarah.wilson@yopmail.com",
  "userId": "user-001",
  "subject": "Course Assignment Notification - Professional Development Course",
  "notificationType": "PACKAGE_ASSIGNED_USER",
  "channels": ["EMAIL", "IN_APP"],
  "templateModel": {
    "userName": "Sarah Wilson",
    "packageName": "Professional Development Course",
    "packageDetails": "Professional Development Course - 10 modules",
    "assignmentDate": "23 January 2025"
  }
}
```

#### PACKAGE_EXPIRY
**Purpose**: Admin notification when package is about to expire
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Package Expiry Alert",
  "notificationType": "PACKAGE_EXPIRY",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Tom Brown",
    "packageName": "Premium Learning Package",
    "expirationDate": "2025-02-15"
  }
}
```

#### CAMPAIGN_ACTIVITY
**Purpose**: Admin notification for campaign activities
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Campaign Activity Alert",
  "notificationType": "CAMPAIGN_ACTIVITY",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "campaignType": "Q1 Learning Initiative",
    "userEngagement": "High - 85% completion rate",
    "timestamp": "2025-01-23 12:00:00"
  }
}
```

### 4. Payment Management Notifications

#### PAYMENT_SUCCESS
**Purpose**: Admin notification when payment succeeds
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Payment Success Alert",
  "notificationType": "PAYMENT_SUCCESS",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Emma Taylor",
    "amount": "$199.99",
    "method": "Credit Card",
    "timestamp": "2025-01-23 09:15:00"
  }
}
```

#### PAYMENT_FAILURE
**Purpose**: Admin notification when payment fails
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Payment Failure Alert",
  "notificationType": "PAYMENT_FAILURE",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "David Lee",
    "failureReason": "Insufficient funds",
    "amount": "$149.99",
    "timestamp": "2025-01-23 13:45:00"
  }
}
```

#### PENDING_PAYMENT
**Purpose**: Admin notification for pending payments
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Pending Payment Alert",
  "notificationType": "PENDING_PAYMENT",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Lisa Chen",
    "amount": "$99.99",
    "dueDate": "2025-01-30"
  }
}
```

#### REFUND_REQUESTED
**Purpose**: Admin notification when refund is requested
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Refund Request Alert",
  "notificationType": "REFUND_REQUESTED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Robert Kim",
    "refundReason": "Course content not as described",
    "amount": "$79.99"
  }
}
```

### 5. Policy Management Notifications

#### NEW_POLICY_CREATED
**Purpose**: Admin notification when new policy is created
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "New Policy Created Alert",
  "notificationType": "NEW_POLICY_CREATED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "policyTitle": "Data Privacy and Protection Policy",
    "policyDescription": "Comprehensive policy outlining data handling, storage, and protection procedures.",
    "approvalStatus": "Approved by Legal Team"
  }
}
```

#### POLICY_UPDATED
**Purpose**: Admin notification when policy is updated
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Policy Update Alert",
  "notificationType": "POLICY_UPDATED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "policyTitle": "Acceptable Use Policy",
    "updatedFields": "Section 3.2, Section 4.1, Appendix A",
    "timestamp": "2025-01-23 08:30:00"
  }
}
```

#### POLICY_COMPLIANCE_REMINDER
**Purpose**: Admin notification for policy compliance reminders
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Policy Compliance Reminder",
  "notificationType": "POLICY_COMPLIANCE_REMINDER",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "policyTitle": "Security Awareness Policy",
    "reminderDate": "2025-01-30"
  }
}
```

### 6. Certificate Management Notifications

#### CERTIFICATE_ISSUED
**Purpose**: User notification when certificate is issued
**Test Data**:
```json
{
  "to": "user@yopmail.com",
  "userId": "user-001",
  "subject": "🎓 Your Certificate Has Been Issued – Advanced Network Security",
  "notificationType": "CERTIFICATE_ISSUED",
  "channels": ["EMAIL", "IN_APP"],
  "templateModel": {
    "userName": "Jennifer Martinez",
    "courseTitle": "Advanced Network Security",
    "issueDate": "2025-01-23"
  }
}
```

#### CERTIFICATE_ISSUED_ADMIN
**Purpose**: Admin notification when certificate is issued to a user
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Certificate Issued - ASAT Platform",
  "notificationType": "CERTIFICATE_ISSUED_ADMIN",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Jennifer Martinez",
    "courseTitle": "Advanced Network Security",
    "issueDate": "2025-01-23"
  }
}
```

#### LEADERBOARD_UPDATE
**Purpose**: Admin notification when leaderboard updates
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Leaderboard Update Alert",
  "notificationType": "LEADERBOARD_UPDATE",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "topPerformers": "Alex Johnson (95%), Maria Garcia (92%), Kevin Wang (90%)",
    "timestamp": "2025-01-23 17:00:00"
  }
}
```

#### CERTIFICATE_EXPIRY
**Purpose**: Admin notification when certificate expires
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Certificate Expiry Alert",
  "notificationType": "CERTIFICATE_EXPIRY",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Chris Anderson",
    "certificateTitle": "Cybersecurity Fundamentals Certificate",
    "expirationDate": "2025-03-15"
  }
}
```

### 7. System/Operational Notifications

#### SYSTEM_DOWNTIME
**Purpose**: Admin notification for system downtime
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "System Downtime Alert",
  "notificationType": "SYSTEM_DOWNTIME",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "reason": "Scheduled maintenance - database optimization",
    "startTime": "2025-01-24 02:00:00",
    "endTime": "2025-01-24 04:00:00"
  }
}
```

#### SECURITY_ALERTS
**Purpose**: Admin notification for security alerts
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Security Alert",
  "notificationType": "SECURITY_ALERTS",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "threatType": "Multiple failed login attempts",
    "userName": "Unknown User",
    "timestamp": "2025-01-23 18:30:00"
  }
}
```

#### UPDATES_PATCHES
**Purpose**: Admin notification for system updates
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "System Updates Available",
  "notificationType": "UPDATES_PATCHES",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "version": "v2.1.3",
    "releaseDate": "2025-01-23"
  }
}
```

#### FEATURE_UPDATE_CHANGE
**Purpose**: Admin notification for feature updates
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Feature Update Alert",
  "notificationType": "FEATURE_UPDATE_CHANGE",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "featureName": "Advanced Analytics Dashboard",
    "changeDetails": "Added real-time progress tracking and performance metrics"
  }
}
```

### 8. General Notifications

#### REMINDER_PENDING_TASKS
**Purpose**: Admin notification for overdue tasks
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Overdue Tasks Reminder",
  "notificationType": "REMINDER_PENDING_TASKS",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "taskType": "Course Review",
    "taskDescription": "Review and approve 15 pending course submissions",
    "dueDate": "2025-01-20"
  }
}
```

#### SCHEDULED_MAINTENANCE
**Purpose**: Admin notification for scheduled maintenance
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Scheduled Maintenance Alert",
  "notificationType": "SCHEDULED_MAINTENANCE",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "maintenanceType": "Database Optimization",
    "scheduledDate": "2025-01-25 01:00:00"
  }
}
```

#### HELP_DESK_TICKET_UPDATES
**Purpose**: Admin notification for help desk tickets
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Help Desk Ticket Update",
  "notificationType": "HELP_DESK_TICKET_UPDATES",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "ticketId": "HD-2025-001234",
    "status": "Resolved",
    "resolutionDate": "2025-01-23 16:45:00"
  }
}
```

#### SUBSCRIPTION_RENEWAL_REMINDER
**Purpose**: Admin notification for subscription renewals
**Test Data**:
```json
{
  "to": "admin@yopmail.com",
  "userId": "admin-001",
  "subject": "Subscription Renewal Reminder",
  "notificationType": "SUBSCRIPTION_RENEWAL_REMINDER",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "System Administrator",
    "subscriptionName": "Enterprise Plan",
    "renewalDate": "2025-02-15"
  }
}
```

## Multi-Channel Testing

### Email + In-App Notification Example
```json
{
  "to": "testuser@yopmail.com",
  "userId": "user-006",
  "subject": "Multi-Channel Test",
  "notificationType": "COURSE_ASSIGNED",
  "channels": ["EMAIL", "IN_APP"],
  "templateModel": {
    "adminName": "System Administrator",
    "userName": "Test User",
    "courseTitle": "Test Course",
    "assignmentDate": "2025-01-23"
  }
}
```

## YOPmail Testing Addresses

### Recommended Test Email Addresses
- `admin@yopmail.com` - For admin notifications
- `testuser@yopmail.com` - For general testing
- `student@yopmail.com` - For course-related tests
- `graduate@yopmail.com` - For certificate tests
- `payment@yopmail.com` - For payment-related tests
- `policy@yopmail.com` - For policy-related tests
- `system@yopmail.com` - For system notifications

### Accessing YOPmail Inboxes
1. Go to [https://yopmail.com](https://yopmail.com)
2. Enter the email address in the inbox field
3. Click "Check Inbox" to view received emails
4. Emails appear instantly for testing purposes

## Error Testing Scenarios

### 1. Invalid Email Format
```json
{
  "to": "invalid-email-format",
  "userId": "admin-001",
  "subject": "Test Subject",
  "notificationType": "NEW_USER_REGISTERED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "Test Admin",
    "userName": "Test User",
    "userEmail": "test@example.com",
    "timestamp": "2025-01-23 10:30:00"
  }
}
```
**Expected Response**: 400 Bad Request with validation error

### 2. Missing Required Fields
```json
{
  "to": "test@yopmail.com",
  "subject": "Test Subject",
  "notificationType": "COURSE_ASSIGNED",
  "channels": ["EMAIL"],
  "templateModel": {
    "adminName": "Test Admin"
  }
}
```
**Expected Response**: 400 Bad Request with validation error

### 3. Disabled Notification Type
To test this, disable a notification type in the database:
```javascript
db.notification_settings.updateOne(
  { "notificationType": "NEW_USER_REGISTERED" },
  { $set: { "enabled": false } }
)
```
**Expected Response**: 500 Internal Server Error with delivery failure message

## Testing Workflow

### 1. Start the Notification Service
```bash
cd /Users/mr/asat/ASAT-V2-BACKEND
./gradlew :services:notification:service:bootRun
```

### 2. Verify Data Initialization
Check the application logs for initialization messages:
```
🚀 Starting notification system data initialization...
📊 Initializing notification settings...
✅ Initialized 25+ notification settings
📝 Initializing notification templates...
✅ Initialized 50+ notification templates
📱 Initializing sample in-app notifications...
✅ Initialized 5 sample in-app notifications
✅ Notification system data initialization completed successfully!
```

### 3. Test Each Notification Type
Use the provided test data for each notification type to ensure:
- Templates render correctly
- Email delivery works
- In-app notifications are created
- Multi-channel notifications work
- Error handling is proper

### 4. Verify Email Delivery
- Check YOPmail inboxes for received emails
- Verify template rendering and content
- Test with different email addresses

### 5. Test In-App Notifications
- Use the notification management APIs
- Verify notification creation and retrieval
- Test read/unread functionality

## Database Collections

The data initializer creates the following MongoDB collections:

1. **notification_settings** - Stores notification type configurations (25+ records)
2. **notification_templates** - Stores email and in-app templates (50+ records)
3. **in_app_notifications** - Stores in-app notification messages (5 sample records)
4. **notification_history** - Stores notification delivery history (empty initially)

## Production Considerations

### For Production Deployment
1. Set `initialize-on-startup: false` in production
2. Use proper database migration scripts
3. Implement proper data seeding strategies
4. Consider using environment-specific configurations

### Data Management
- The initializer only runs once per deployment
- Use `skip-if-data-exists: true` to prevent duplicate data
- Monitor collection sizes and performance
- Implement proper backup strategies

## Troubleshooting

### Common Issues

1. **MongoDB Connection Issues**
   - Verify MongoDB is running
   - Check connection string in `application.yml`
   - Ensure proper authentication

2. **Data Not Initializing**
   - Check `initialize-on-startup` setting
   - Verify MongoDB permissions
   - Check application logs for errors

3. **Template Processing Errors**
   - Verify template syntax
   - Check template model data
   - Monitor template rendering logs

4. **Email Delivery Issues**
   - Check SMTP configuration
   - Verify email service credentials
   - Test with YOPmail addresses

## Next Steps

1. **Test all notification types** using the provided test data
2. **Verify email delivery** using YOPmail addresses
3. **Test in-app notifications** using the management APIs
4. **Implement additional templates** as needed
5. **Configure production settings** for deployment

The notification system is now ready for comprehensive testing with realistic data and templates for all 25+ notification types!