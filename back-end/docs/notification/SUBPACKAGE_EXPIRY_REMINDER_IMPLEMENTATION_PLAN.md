# SubPackage Expiry Reminder Implementation Plan

## Overview
Implement scheduled notifications for SubPackage expiry reminders at 50%, 20%, 10% of validity period, and on expiry date. The system will send email notifications to users and their configured secondary email lists based on the reminder percentage.

## Current State Analysis

### ✅ What Already Exists

1. **UserSubPackage Model** (`services/cms/core/src/main/java/com/aspire/asat/cms/model/UserSubPackage.java`)
   - ✅ `assignedDate` (LocalDate) - When subpackage was assigned
   - ✅ `expiryDate` (LocalDate) - Explicit expiry date
   - ✅ `status` (String) - IN_PROGRESS, NOT_STARTED, COMPLETED, EXAM
   - ✅ `secondaryEmails` (List<String>) - For 50% reminder
   - ✅ `thirdLevelEmails` (List<String>) - For 20% reminder  
   - ✅ `fourthHREmails` (List<String>) - For 10% reminder
   - ✅ `validity` (String) - Validity period (e.g., "365 Days")

2. **Notification Infrastructure**
   - ✅ Notification service with `@EnableScheduling`
   - ✅ `CmsNotificationClient` in CMS service for sending notifications
   - ✅ Email sending via SMTP/SES
   - ✅ Template-based notification system

3. **Scheduler Pattern**
   - ✅ Examples: `RefreshTokenCleanupScheduler`, `UserActivityServiceImpl` with `@Scheduled`
   - ✅ Pattern: `@Scheduled(fixedRate = milliseconds)` or `@Scheduled(fixedDelay = milliseconds)`

### ❌ What Needs to be Added

1. **New Notification Type**: `SUBPACKAGE_EXPIRY_REMINDER`
2. **Scheduler Component**: `SubPackageExpiryReminderScheduler` in CMS service
3. **Repository Method**: Query UserSubPackage by expiry date range
4. **Service Method**: Calculate reminder dates and send notifications
5. **User Email Retrieval**: Method to get user email from userId
6. **Reminder Tracking**: Track which reminders have been sent (to avoid duplicates)

## Architecture Decision

### Service Location: CMS Service
**Rationale:**
- CMS service owns `UserSubPackage` repository and data
- CMS service already has `CmsNotificationClient` for sending notifications
- Keeps data access and business logic together
- Reduces cross-service dependencies

### Scheduler Configuration
- **Default**: Runs every day at midnight (00:00:00) using cron expression
- **Configurable**: Can be customized via properties using cron expression format

## Implementation Details

### 1. Reminder Calculation Logic

```java
// Calculate reminder dates based on assignedDate and validity period
LocalDate assignedDate = userSubPackage.getAssignedDate();
LocalDate expiryDate = userSubPackage.getExpiryDate();

// Calculate total days in validity period
long totalDays = ChronoUnit.DAYS.between(assignedDate, expiryDate);

// Calculate reminder dates
LocalDate reminder50Percent = assignedDate.plusDays((long) (totalDays * 0.5));
LocalDate reminder20Percent = assignedDate.plusDays((long) (totalDays * 0.8));  // 20% remaining = 80% elapsed
LocalDate reminder10Percent = assignedDate.plusDays((long) (totalDays * 0.9));  // 10% remaining = 90% elapsed
LocalDate expiryReminder = expiryDate;
```

**Note**: The user mentioned "50%, 25%, and on expiry date" but also "50%, 20%, 10%". Based on the email list mappings (50%→secondaryEmails, 20%→thirdLevelEmails, 10%→fourthHREmails), we'll use **50%, 20%, 10%** as the percentages.

### 2. Reminder Tracking

To avoid sending duplicate reminders, we need to track which reminders have been sent. Options:

**Option A**: Add fields to `UserSubPackage` model
- `reminder50PercentSent` (boolean)
- `reminder20PercentSent` (boolean)  
- `reminder10PercentSent` (boolean)
- `reminderExpirySent` (boolean)

**Option B**: Create a separate `SubPackageReminderLog` collection
- Track userId, subPackageId, reminderType, sentDate

**Recommendation**: Option A (simpler, no new collection)

### 3. Notification Type

Add new notification type to `NotificationType` enum:
```java
SUBPACKAGE_EXPIRY_REMINDER("subpackage_expiry_reminder", "SubPackage Expiry Reminder",
    NotificationCategory.PACKAGE_MANAGEMENT, true)
```

### 4. Email Recipients Logic

Based on reminder percentage:
- **50% reminder**: userEmail + secondaryEmails
- **20% reminder**: userEmail + thirdLevelEmails
- **10% reminder**: userEmail + fourthHREmails
- **Expiry reminder**: userEmail + all email lists (secondaryEmails + thirdLevelEmails + fourthHREmails)

### 5. Query Logic

The scheduler needs to:
1. Find UserSubPackage records where:
   - `status != "COMPLETED"` (skip completed subpackages)
   - `expiryDate` is in the future (not expired)
   - Current date matches one of the reminder dates (50%, 20%, 10%, expiry)
   - Corresponding reminder flag is `false` (not sent yet)

2. For each matching record:
   - Calculate which reminder should be sent
   - Get user email (from registration service or stored in UserSubPackage)
   - Send notification to appropriate email lists
   - Mark reminder as sent

### 6. User Email Retrieval

**Option A**: Add `userEmail` field to `UserSubPackage` (set during assignment)
**Option B**: Call Registration service to get user email on-demand

**Recommendation**: Option A (better performance, avoids service calls)

## File Structure

### New Files to Create

1. **Scheduler**
   - `services/cms/core/src/main/java/com/aspire/asat/cms/scheduler/SubPackageExpiryReminderScheduler.java`

2. **Service** (if needed)
   - `services/cms/core/src/main/java/com/aspire/asat/cms/service/SubPackageExpiryReminderService.java`
   - `services/cms/core/src/main/java/com/aspire/asat/cms/service/impl/SubPackageExpiryReminderServiceImpl.java`

3. **Repository Methods** (add to existing)
   - `services/cms/core/src/main/java/com/aspire/asat/cms/repository/UserSubPackageRepository.java`

### Files to Modify

1. **UserSubPackage Model**
   - Add reminder tracking fields
   - Add `userEmail` field (optional, for performance)

2. **NotificationType Enum**
   - Add `SUBPACKAGE_EXPIRY_REMINDER`

3. **CmsApplication**
   - Add `@EnableScheduling` if not present

4. **ClientUserOperationServiceImpl**
   - Set `userEmail` when assigning subpackage (if we add the field)
   - Initialize reminder flags to `false`

5. **CmsNotificationClient**
   - Add method to send subpackage expiry reminder notifications

6. **Notification Templates** (in notification service)
   - Create email template for `SUBPACKAGE_EXPIRY_REMINDER`

## Implementation Steps

### Phase 1: Data Model Updates

1. ✅ Add reminder tracking fields to `UserSubPackage`:
   - `reminder50PercentSent` (boolean, default false)
   - `reminder20PercentSent` (boolean, default false)
   - `reminder10PercentSent` (boolean, default false)
   - `reminderExpirySent` (boolean, default false)
   - `userEmail` (String, optional but recommended)

2. ✅ Update assignment logic in `ClientUserOperationServiceImpl.assignSubPackagesToUsers()`:
   - Set all reminder flags to `false`
   - Set `userEmail` if we add the field

### Phase 2: Notification Type

3. ✅ Add `SUBPACKAGE_EXPIRY_REMINDER` to `NotificationType` enum

4. ✅ Create notification template in notification service (if templates are managed separately)

### Phase 3: Repository Methods

5. ✅ Add repository methods to query UserSubPackage by date range:
   ```java
   // Find subpackages that need reminder on a specific date
   List<UserSubPackage> findSubPackagesNeedingReminder(
       LocalDate checkDate, 
       List<String> excludeStatuses
   );
   ```

### Phase 4: Scheduler Implementation

6. ✅ Create `SubPackageExpiryReminderScheduler`:
   - `@Scheduled(cron = "0 0 0 * * ?")` runs every day at midnight
   - Make cron expression configurable via properties
   - Query for subpackages needing reminders
   - Calculate reminder dates and match against current date
   - Send notifications via `CmsNotificationClient`
   - Update reminder flags

7. ✅ Add `@EnableScheduling` to `CmsApplication` if not present

### Phase 5: Notification Sending

8. ✅ Add method to `CmsNotificationClient`:
   ```java
   public boolean sendSubPackageExpiryReminder(
       String userEmail,
       List<String> additionalEmails,
       String userId,
       String subPackageName,
       LocalDate expiryDate,
       int reminderPercentage
   )
   ```

9. ✅ Build notification template model with:
   - User name
   - SubPackage name
   - Expiry date
   - Reminder percentage (e.g., "50% of validity period remaining")
   - Days remaining

### Phase 6: Testing

10. ✅ Unit tests for reminder date calculations
11. ✅ Integration tests for scheduler
12. ✅ Test email sending with different reminder percentages
13. ✅ Test duplicate prevention (should not send same reminder twice)

## Configuration

### Application Properties

Add to `services/cms/service/src/main/resources/application.yml`:

```yaml
subpackage:
  expiry:
    reminder:
      enabled: true
      scheduler:
        cron: "0 0 0 * * ?"  # Run every day at midnight (00:00:00)
      timezone: UTC
```

## Edge Cases to Handle

1. **Subpackage already completed**: Skip all reminders
2. **Subpackage already expired**: Skip reminders (only send expiry reminder if not expired)
3. **Multiple reminders on same day**: Handle gracefully (check each reminder flag separately)
4. **User email not found**: Log error, skip notification
5. **Empty email lists**: Still send to user email
6. **Scheduler running multiple times**: Reminder flags prevent duplicates
7. **Time zone handling**: Use UTC for consistency
8. **Date calculation edge cases**: Handle leap years, month boundaries correctly

## Database Indexing

Consider adding index on `expiryDate` for better query performance:
```java
@Indexed
private LocalDate expiryDate;
```

Already exists in UserSubPackage model ✅

## Notification Template Variables

The notification template should support:
- `userName` - User's full name
- `subPackageName` - Name of the subpackage
- `expiryDate` - When the subpackage expires
- `daysRemaining` - Days until expiry
- `reminderPercentage` - Percentage of validity remaining (50%, 20%, 10%, or "Expiring Today")
- `assignedDate` - When subpackage was assigned
- `validityPeriod` - Total validity period (e.g., "365 Days")

## Future Enhancements

1. **Configurable reminder percentages** via admin panel
2. **Custom reminder schedules** per client admin
3. **In-app notifications** in addition to emails
4. **SMS notifications** for critical reminders
5. **Reminder history** dashboard for admins
6. **Bulk reminder resend** capability

## Notes

- The user mentioned "50%, 25%, and on expiry date" but the email list mappings suggest 50%, 20%, 10%. The plan uses 50%, 20%, 10% as specified in the email list requirements.
- If 25% is required instead of 20%, we can adjust the calculation.
- The scheduler runs daily at midnight to check for reminder dates, and uses flags to prevent duplicate sends.

