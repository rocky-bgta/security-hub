# Notification Role Templates Migration Guide

This document describes the one-time MongoDB migration that creates role-specific
notification templates on the canonical notification types, so Aspire Admin, MSP
and Client Admin recipients can receive different copy from end users for the same
`NotificationType`.

**Service:** `notification-service`  
**Database:** MongoDB (`notification`)  
**Migration script (EMAIL/IN_APP):** [`notification-templates-role-variants.js`](../services/notification/core/src/main/resources/migrations/notification-templates-role-variants.js) (22 rows)

Role-scoped SMS for the SMS-matrix types is seeded only by
[`NotificationDataInitializer`](../services/notification/core/src/main/java/com/aspire/asat/notification/utils/NotificationDataInitializer.java)
when `notification.data.initialize-on-startup` is enabled (no separate mongosh script).

---

## Overview

A template is identified by `(notification_type, channel, recipient_role)`.
`recipient_role = null` is the **base** (role-agnostic) template; a row with a role
**overrides** it for that role only. Lookup prefers the role-specific row and falls
back to the base.

This migration creates **up to 22** role variants by copying admin-addressed content
from the legacy sibling types that already exist in the collection (for example
`NEW_USER_REGISTERED` → `WELCOME_EMAIL` for CLIENT_ADMIN / MSP / ASPIRE_ADMIN).

Combinations the role settings matrix disables are intentionally omitted:

| Type | Skipped |
|------|---------|
| `WELCOME_EMAIL` | IN_APP / MSP (`MSP_IN_APP_OFF_TYPES`) |
| `COURSE_COMPLETION_USER` | all MSP channels (`MSP_DISABLED_TYPES`) |
| `PACKAGE_ASSIGNED_AND_USER_CREDENTIAL` | all MSP channels (`MSP_DISABLED_TYPES`) |

The script also:

1. Clears `is_default` on any role-specific row (default means the base template).
2. Creates a unique index on `(notification_type, channel, recipient_role)`.

Fresh installs get the same 22 rows from
[`NotificationDataInitializer`](../services/notification/core/src/main/java/com/aspire/asat/notification/utils/NotificationDataInitializer.java)
via insert-only seeding (`NotificationTemplateSeedSync`).

---

## Prerequisites

| Requirement | Notes |
|-------------|-------|
| `mongosh` | MongoDB Shell 1.x+ |
| MongoDB access | Read/write on the `notification` database |
| Source templates present | Base rows for the legacy sibling types listed in the mapping below |

---

## Run

```bash
MIGRATIONS_DIR="services/notification/core/src/main/resources/migrations"

# Local
mongosh "mongodb://localhost:27017/notification" \
  --file "$MIGRATIONS_DIR/notification-templates-role-variants.js"

# With auth
mongosh "mongodb://<user>:<password>@<host>:27017/notification?authSource=admin" \
  --file "$MIGRATIONS_DIR/notification-templates-role-variants.js"
```

Safe to re-run: existing `(notification_type, channel, recipient_role)` rows are skipped.

Expected console output on a clean backup (approximate):

```
Phase 1 (role variants): inserted=22 skippedExisting=0 missingSource=0
Phase 2 (normalize is_default): matched=... modified=...
Phase 3 (unique index): created uniq_notification_type_channel_recipient_role
Migration complete. inserted=22 skippedExisting=0 missingSource=0
```

If some CLIENT_ADMIN variants already exist (for example from an earlier seed),
`skippedExisting` will be non-zero and `inserted` lower accordingly.

---

## Mapping (22 rows)

| Target type | Channel | Role | Source type | Source channel |
|-------------|---------|------|-------------|----------------|
| `WELCOME_EMAIL` | EMAIL | CLIENT_ADMIN | `NEW_USER_REGISTERED` | EMAIL |
| `WELCOME_EMAIL` | EMAIL | MSP | `NEW_USER_REGISTERED` | EMAIL |
| `WELCOME_EMAIL` | EMAIL | ASPIRE_ADMIN | `NEW_USER_REGISTERED` | EMAIL |
| `WELCOME_EMAIL` | IN_APP | CLIENT_ADMIN | `NEW_USER_REGISTERED` | IN_APP |
| `WELCOME_EMAIL` | IN_APP | ASPIRE_ADMIN | `NEW_USER_REGISTERED` | IN_APP |
| `USER_PASSWORD_CHANGE` | EMAIL | MSP | `USER_PASSWORD_CHANGE_ADMIN` | EMAIL |
| `USER_PASSWORD_CHANGE` | EMAIL | ASPIRE_ADMIN | `USER_PASSWORD_CHANGE_ADMIN` | EMAIL |
| `USER_PASSWORD_CHANGE` | IN_APP | MSP | `USER_PASSWORD_CHANGE_ADMIN` | IN_APP |
| `USER_PASSWORD_CHANGE` | IN_APP | ASPIRE_ADMIN | `USER_PASSWORD_CHANGE_ADMIN` | IN_APP |
| `COURSE_COMPLETION_USER` | EMAIL | ASPIRE_ADMIN | `COURSE_COMPLETION` | EMAIL |
| `COURSE_COMPLETION_USER` | IN_APP | ASPIRE_ADMIN | `COURSE_COMPLETION` | IN_APP |
| `CERTIFICATE_ISSUED` | EMAIL | MSP | `CERTIFICATE_ISSUED_ADMIN` | EMAIL |
| `CERTIFICATE_ISSUED` | EMAIL | ASPIRE_ADMIN | `CERTIFICATE_ISSUED_ADMIN` | EMAIL |
| `PACKAGE_ASSIGNED_USER` | EMAIL | MSP | `PACKAGE_ASSIGNED` | EMAIL |
| `PACKAGE_ASSIGNED_USER` | EMAIL | ASPIRE_ADMIN | `PACKAGE_ASSIGNED` | EMAIL |
| `PACKAGE_ASSIGNED_USER` | IN_APP | MSP | `PACKAGE_ASSIGNED` | IN_APP |
| `PACKAGE_ASSIGNED_USER` | IN_APP | ASPIRE_ADMIN | `PACKAGE_ASSIGNED` | IN_APP |
| `PACKAGE_ASSIGNED_AND_USER_CREDENTIAL` | EMAIL | ASPIRE_ADMIN | `PACKAGE_ASSIGNED` | EMAIL |
| `PACKAGE_ASSIGNED_AND_USER_CREDENTIAL` | IN_APP | ASPIRE_ADMIN | `PACKAGE_ASSIGNED` | IN_APP |
| `USER_SUSPENSION` | EMAIL | CLIENT_ADMIN | `USER_SUSPENDED` | EMAIL |
| `USER_SUSPENSION` | EMAIL | MSP | `USER_SUSPENDED` | EMAIL |
| `USER_SUSPENSION` | EMAIL | ASPIRE_ADMIN | `USER_SUSPENDED` | EMAIL |

Content fields copied from the source: `subject_template`, `html_template`,
`text_template`, `title_template`, `message_template`. New rows are inserted with
`is_active: true`, `is_default: false`, `organization_id: null`.

---

## Verification

```javascript
// Count by recipient_role
db.notification_templates.aggregate([
  { $group: { _id: "$recipient_role", count: { $sum: 1 } } },
  { $sort: { _id: 1 } }
])

// List the migrated role variants
db.notification_templates.find(
  {
    notification_type: {
      $in: [
        "WELCOME_EMAIL",
        "USER_PASSWORD_CHANGE",
        "COURSE_COMPLETION_USER",
        "CERTIFICATE_ISSUED",
        "PACKAGE_ASSIGNED_USER",
        "PACKAGE_ASSIGNED_AND_USER_CREDENTIAL",
        "USER_SUSPENSION",
      ],
    },
    recipient_role: { $ne: null },
  },
  {
    notification_type: 1,
    channel: 1,
    recipient_role: 1,
    template_name: 1,
    is_default: 1,
    _id: 0,
  }
).sort({ notification_type: 1, channel: 1, recipient_role: 1 })

// Confirm unique index
db.notification_templates.getIndexes()
```

Admin API (after the notification service is running):

```bash
curl -s "http://localhost:5656/notification/api/v1/admin/notification-templates/variants?notificationType=WELCOME_EMAIL&channel=EMAIL"
```

Expect the base template plus one entry per role: `CUSTOM` for CLIENT_ADMIN / MSP /
ASPIRE_ADMIN and `INHERITS_BASE` for USER.

---

## Rollback

```javascript
// Delete only the rows this migration inserts (by identity key).
// Does NOT delete CLIENT_ADMIN rows that predated this migration for
// USER_PASSWORD_CHANGE / COURSE_COMPLETION_USER / CERTIFICATE_ISSUED /
// PACKAGE_ASSIGNED_USER / PACKAGE_ASSIGNED_AND_USER_CREDENTIAL — remove those
// keys from the filter if you also want them gone.
db.notification_templates.deleteMany({
  $or: [
    { notification_type: "WELCOME_EMAIL", recipient_role: { $in: ["CLIENT_ADMIN", "MSP", "ASPIRE_ADMIN"] } },
    { notification_type: "USER_PASSWORD_CHANGE", recipient_role: { $in: ["MSP", "ASPIRE_ADMIN"] } },
    { notification_type: "COURSE_COMPLETION_USER", recipient_role: "ASPIRE_ADMIN" },
    { notification_type: "CERTIFICATE_ISSUED", recipient_role: { $in: ["MSP", "ASPIRE_ADMIN"] } },
    { notification_type: "PACKAGE_ASSIGNED_USER", recipient_role: { $in: ["MSP", "ASPIRE_ADMIN"] } },
    { notification_type: "PACKAGE_ASSIGNED_AND_USER_CREDENTIAL", recipient_role: "ASPIRE_ADMIN" },
    { notification_type: "USER_SUSPENSION", recipient_role: { $in: ["CLIENT_ADMIN", "MSP", "ASPIRE_ADMIN"] } },
  ],
})

db.notification_templates.dropIndex("uniq_notification_type_channel_recipient_role")
```

> **Note:** Dropping the index does not restore `is_default: true` on any pre-existing
> role variants that phase 2 cleared. Re-set those manually if needed.

---

## SMS role variants (27 rows, seeder only)

With `notification.data.initialize-on-startup: true`,
[`NotificationDataInitializer`](../services/notification/core/src/main/java/com/aspire/asat/notification/utils/NotificationDataInitializer.java)
insert-only seeds role SMS for the 9 types in `SMS_ENABLED_GLOBAL_TYPES` ×
`CLIENT_ADMIN` / `MSP` / `ASPIRE_ADMIN`. USER keeps the base SMS via lookup
fallback (no USER role SMS rows).

Admin SMS uses short third-person / alert copy (not a blind copy of the
end-user base SMS). Restart (or first boot) of the notification service is enough
to pick up missing rows; existing templates are never overwritten.

| Type | Roles |
|------|--------|
| `USER_SUSPENDED` | CLIENT_ADMIN, MSP, ASPIRE_ADMIN |
| `USER_SUSPENSION` | CLIENT_ADMIN, MSP, ASPIRE_ADMIN |
| `PACKAGE_EXPIRY` | CLIENT_ADMIN, MSP, ASPIRE_ADMIN |
| `SUBPACKAGE_EXPIRY_REMINDER` | CLIENT_ADMIN, MSP, ASPIRE_ADMIN |
| `PAYMENT_FAILURE` | CLIENT_ADMIN, MSP, ASPIRE_ADMIN |
| `PENDING_PAYMENT` | CLIENT_ADMIN, MSP, ASPIRE_ADMIN |
| `SUBSCRIPTION_RENEWAL_REMINDER` | CLIENT_ADMIN, MSP, ASPIRE_ADMIN |
| `SYSTEM_HEALTH_ALERTS` | CLIENT_ADMIN, MSP, ASPIRE_ADMIN |
| `SECURITY_ALERTS` | CLIENT_ADMIN, MSP, ASPIRE_ADMIN |

### Verify

```javascript
db.notification_templates.countDocuments({
  channel: "SMS",
  recipient_role: { $ne: null },
})
// expect 27
```

---

## Related

- Admin workflow for creating further role variants at runtime:
  `CREATE_ROLE_VARIANT` / `DELETE` on
  `POST /api/v1/admin/notification-templates/action`
- Role settings matrix seeder:
  `NotificationDataInitializer.initializeRoleSettings()`
