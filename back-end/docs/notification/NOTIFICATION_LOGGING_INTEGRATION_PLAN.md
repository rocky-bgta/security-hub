# Notification Logging Integration Plan

## Overview

This document outlines the plan to integrate comprehensive logging of all notifications sent from the notification service. The logging system will track notification delivery attempts, successes, failures, and provide audit trails for all notification activities.

## Objectives

1. **Track All Notifications**: Log every notification sent through the system
2. **Delivery Status**: Record success/failure status for each channel
3. **Audit Trail**: Maintain a complete history of notification activities
4. **Analytics Support**: Enable reporting and analytics on notification patterns
5. **Troubleshooting**: Provide detailed logs for debugging delivery issues
6. **Compliance**: Support compliance requirements with notification audit logs

## Current Architecture

### Notification Flow

1. **Entry Point**: `NotificationControllerImpl.sendNotification()`
2. **Delivery Service**: `NotificationDeliveryService.deliverNotification()`
3. **Channel Delivery**:
   - **Email**: `deliverEmailNotification()` → SQS Queue → `SqsListener` → `ConsumerService` → `SmtpEmailSender`
   - **In-App**: `deliverInAppNotification()` → `InAppNotificationService.createNotification()`
   - **SMS**: Placeholder (future)
   - **Push**: Placeholder (future)

### Current Logging

- Basic SLF4J logging at various points
- No persistent storage of notification logs
- No centralized notification history tracking

## Implementation Plan

### Phase 1: Data Model & Repository

#### 1.1 Create NotificationHistory Model

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/model/NotificationHistory.java`

**Fields**:
- `id` (String) - Unique identifier
- `notificationType` (NotificationType) - Type of notification
- `userId` (String) - Target user ID (optional)
- `recipientEmail` (String) - Email recipient (optional)
- `recipientPhone` (String) - Phone number for SMS (optional)
- `channels` (List<NotificationChannel>) - Channels attempted
- `channelStatuses` (Map<NotificationChannel, ChannelStatus>) - Status per channel
- `subject` (String) - Email subject (if applicable)
- `templateId` (String) - Template used
- `templateModel` (String) - JSON string of template variables
- `clientAdminId` (String) - Client admin ID (if applicable)
- `priority` (NotificationPriority) - Notification priority
- `requestMetadata` (String) - JSON string of additional request metadata
- `deliveryStatus` (DeliveryStatus) - Overall delivery status (SUCCESS, PARTIAL, FAILED)
- `errorMessage` (String) - Error message if failed
- `sentAt` (LocalDateTime) - When notification was sent
- `completedAt` (LocalDateTime) - When all channels completed
- `createdAt` (LocalDateTime) - Record creation time
- `updatedAt` (LocalDateTime) - Last update time

**ChannelStatus Enum**:
- `PENDING` - Queued but not yet sent
- `SUCCESS` - Successfully delivered
- `FAILED` - Delivery failed
- `SKIPPED` - Skipped (e.g., channel disabled)

**DeliveryStatus Enum**:
- `SUCCESS` - All channels succeeded
- `PARTIAL` - Some channels succeeded, some failed
- `FAILED` - All channels failed
- `PENDING` - Still processing

#### 1.2 Create NotificationHistoryRepository

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/repository/NotificationHistoryRepository.java`

**Methods**:
- `findByUserId(String userId, Pageable pageable)` - Get user's notification history
- `findByNotificationType(NotificationType type, Pageable pageable)` - Get by type
- `findByDeliveryStatus(DeliveryStatus status, Pageable pageable)` - Get by status
- `findBySentAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable)` - Get by date range
- `findByRecipientEmail(String email, Pageable pageable)` - Get by email
- `countByNotificationTypeAndSentAtBetween(NotificationType type, LocalDateTime start, LocalDateTime end)` - Count by type and date
- `countByDeliveryStatusAndSentAtBetween(DeliveryStatus status, LocalDateTime start, LocalDateTime end)` - Count by status and date

**Indexes**:
- `userId` + `sentAt` (descending)
- `notificationType` + `sentAt` (descending)
- `deliveryStatus` + `sentAt` (descending)
- `recipientEmail` + `sentAt` (descending)
- `sentAt` (descending)

### Phase 2: Logging Service

#### 2.1 Create NotificationHistoryService

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/service/NotificationHistoryService.java`

**Methods**:
- `createNotificationLog(NotificationRequestDto request)` - Create initial log entry
- `updateChannelStatus(String logId, NotificationChannel channel, ChannelStatus status, String errorMessage)` - Update channel status
- `finalizeNotificationLog(String logId, DeliveryStatus status)` - Mark notification as complete
- `getNotificationHistory(String logId)` - Get single log entry
- `getUserNotificationHistory(String userId, Pageable pageable)` - Get user history
- `getNotificationHistoryByType(NotificationType type, Pageable pageable)` - Get by type
- `getNotificationHistoryByStatus(DeliveryStatus status, Pageable pageable)` - Get by status
- `getNotificationHistoryByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable)` - Get by date range
- `getNotificationStatistics(LocalDateTime start, LocalDateTime end)` - Get statistics

#### 2.2 Create NotificationStatistics DTO

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/dto/NotificationStatisticsDto.java`

**Fields**:
- `totalSent` (long)
- `totalSuccess` (long)
- `totalFailed` (long)
- `totalPartial` (long)
- `byType` (Map<NotificationType, TypeStatistics>)
- `byChannel` (Map<NotificationChannel, ChannelStatistics>)
- `successRate` (double)
- `period` (DateRange)

### Phase 3: Integration Points

#### 3.1 Controller Integration

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/controller/impl/NotificationControllerImpl.java`

**Changes**:
- Create log entry when notification request is received
- Update log entry with final status after delivery completes
- Handle errors and update log accordingly

#### 3.2 Delivery Service Integration

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/service/NotificationDeliveryService.java`

**Changes**:
- Create initial log entry before processing
- Update channel status after each channel delivery attempt
- Finalize log entry after all channels processed
- Handle exceptions and update log with error details

#### 3.3 Email Consumer Integration

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/service/impl/ConsumerServiceImpl.java`

**Changes**:
- Update email channel status after successful send
- Update email channel status on failure
- Link email delivery to notification log (via correlation ID or metadata)

#### 3.4 In-App Notification Integration

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/service/InAppNotificationService.java`

**Changes**:
- Update in-app channel status after creation
- Link in-app notification to notification log

### Phase 4: API Endpoints

#### 4.1 Create NotificationHistoryController

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/controller/NotificationHistoryController.java`

**Endpoints**:
- `GET /api/v1/notification-history` - Get notification history (paginated, with filters)
- `GET /api/v1/notification-history/{id}` - Get single notification log
- `GET /api/v1/notification-history/user/{userId}` - Get user's notification history
- `GET /api/v1/notification-history/type/{type}` - Get by notification type
- `GET /api/v1/notification-history/statistics` - Get notification statistics
- `GET /api/v1/notification-history/statistics/by-type` - Get statistics by type
- `GET /api/v1/notification-history/statistics/by-channel` - Get statistics by channel

**Query Parameters**:
- `offset` (int) - Pagination offset
- `pageSize` (int) - Page size
- `notificationType` (NotificationType) - Filter by type
- `deliveryStatus` (DeliveryStatus) - Filter by status
- `startDate` (LocalDateTime) - Start date filter
- `endDate` (LocalDateTime) - End date filter
- `userId` (String) - Filter by user ID
- `recipientEmail` (String) - Filter by email

### Phase 5: Admin APIs

#### 5.1 Admin Notification History Controller

**File**: `services/notification/core/src/main/java/com/aspire/asat/notification/controller/admin/AdminNotificationHistoryController.java`

**Endpoints**:
- `GET /api/v1/admin/notification-history` - Admin view of all notifications
- `GET /api/v1/admin/notification-history/statistics` - Admin statistics dashboard
- `GET /api/v1/admin/notification-history/export` - Export notification history (CSV/JSON)
- `GET /api/v1/admin/notification-history/failed` - Get failed notifications for review

### Phase 6: Error Handling & Resilience

#### 6.1 Async Logging

- Use `@Async` for logging operations to avoid blocking notification delivery
- Implement retry mechanism for log writes
- Use transactional boundaries appropriately

#### 6.2 Error Handling

- Logging failures should not break notification delivery
- Use try-catch around all logging operations
- Fallback to file logging if database logging fails

### Phase 7: Performance Optimization

#### 7.1 Batch Logging

- Batch multiple channel status updates when possible
- Use bulk write operations for high-volume scenarios

#### 7.2 Indexing Strategy

- Create appropriate indexes for common query patterns
- Monitor query performance and adjust indexes as needed

#### 7.3 Data Retention

- Implement data retention policy (e.g., keep logs for 90 days)
- Create scheduled job to archive/delete old logs
- Consider archiving to S3 for long-term storage

### Phase 8: Testing

#### 8.1 Unit Tests

- Test NotificationHistoryService methods
- Test repository queries
- Test DTO mappings

#### 8.2 Integration Tests

- Test notification logging end-to-end
- Test API endpoints
- Test error scenarios

#### 8.3 Performance Tests

- Test logging performance under load
- Test query performance with large datasets

## Database Schema

### Collection: `notification_history`

```javascript
{
  "_id": "uuid",
  "notification_type": "NEW_USER_REGISTERED",
  "user_id": "user123",
  "recipient_email": "user@example.com",
  "recipient_phone": null,
  "channels": ["EMAIL", "IN_APP"],
  "channel_statuses": {
    "EMAIL": "SUCCESS",
    "IN_APP": "SUCCESS"
  },
  "subject": "Welcome to ASAT Platform",
  "template_id": "template-001",
  "template_model": "{\"userName\":\"John Doe\",\"email\":\"user@example.com\"}",
  "client_admin_id": "client123",
  "priority": "HIGH",
  "request_metadata": "{\"source\":\"registration-service\"}",
  "delivery_status": "SUCCESS",
  "error_message": null,
  "sent_at": ISODate("2025-01-23T10:00:00Z"),
  "completed_at": ISODate("2025-01-23T10:00:05Z"),
  "created_at": ISODate("2025-01-23T10:00:00Z"),
  "updated_at": ISODate("2025-01-23T10:00:05Z")
}
```

### Indexes

```javascript
db.notification_history.createIndex({ "user_id": 1, "sent_at": -1 });
db.notification_history.createIndex({ "notification_type": 1, "sent_at": -1 });
db.notification_history.createIndex({ "delivery_status": 1, "sent_at": -1 });
db.notification_history.createIndex({ "recipient_email": 1, "sent_at": -1 });
db.notification_history.createIndex({ "sent_at": -1 });
db.notification_history.createIndex({ "client_admin_id": 1, "sent_at": -1 });
```

## Configuration

### Application Properties

```yaml
notification:
  history:
    enabled: true
    async-logging: true
    retention-days: 90
    batch-size: 100
    archive-enabled: false
    archive-to-s3: false
```

## Implementation Order

1. **Phase 1**: Data Model & Repository (Foundation)
2. **Phase 2**: Logging Service (Core functionality)
3. **Phase 3**: Integration Points (Wire up logging)
4. **Phase 4**: API Endpoints (Expose data)
5. **Phase 5**: Admin APIs (Admin features)
6. **Phase 6**: Error Handling (Resilience)
7. **Phase 7**: Performance Optimization (Scale)
8. **Phase 8**: Testing (Quality assurance)

## Success Criteria

1. ✅ All notifications are logged to database
2. ✅ Channel-level status tracking works correctly
3. ✅ API endpoints return accurate data
4. ✅ Logging doesn't impact notification delivery performance
5. ✅ Statistics and analytics are accurate
6. ✅ Error handling prevents logging failures from breaking notifications
7. ✅ Performance is acceptable under load
8. ✅ Data retention policy is implemented

## Future Enhancements

1. **Real-time Dashboard**: WebSocket updates for notification statistics
2. **Alerting**: Alert on high failure rates
3. **Machine Learning**: Predict notification delivery success
4. **Advanced Analytics**: User engagement metrics, delivery time analysis
5. **Notification Templates**: Track template performance
6. **A/B Testing**: Support for testing different notification strategies

## Notes

- Logging should be non-blocking and not impact notification delivery performance
- Consider using event sourcing pattern for audit trail
- Implement proper data privacy controls (GDPR compliance)
- Consider encryption for sensitive data in logs
- Plan for log data growth and archiving strategy

