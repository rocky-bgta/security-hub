# Smishing (SMS Campaign) Migration Guide

This document describes all database migrations, configuration changes, and deployment steps required to enable **SMS / smishing campaigns** in the phishing service.

**Service:** `phishing-service`  
**Database:** MongoDB (`phishing`)  
**Migration scripts location:** [`services/phishing/core/src/main/resources/migrations/`](../services/phishing/core/src/main/resources/migrations/)

---

## Overview

The smishing feature adds:

- SMS campaign types (`SMISHING_SIMULATION`, `SMISHING_WITH_TRAINING`)
- Campaign delivery channel (`EMAIL` | `SMS`)
- SMS templates (`templateType`, `smsBody` on `email_templates`)
- SMS server configuration CRUD (`sms_server_configurations`)
- SMS delivery tracking (`campaign_sms_deliveries`, `configuration_audit_logs`)
- Separate SQS delivery queues (`phishing-campaign-emails-*` and `phishing-campaign-sms-*`)
- AES-256-GCM credential encryption for sender profiles and SMS API secrets

**Existing email campaigns are not broken.** Legacy records without new fields default to `EMAIL` channel and `EMAIL` template type at runtime and via migration scripts.

---

## Prerequisites

| Requirement | Notes |
|-------------|-------|
| `mongosh` | MongoDB Shell 1.x+ |
| MongoDB access | Read/write on `phishing` database |
| `PHISHING_CREDENTIALS_ENCRYPTION_KEY` | Base64-encoded 32-byte AES key (required in non-local prod/staging) |
| SQS queue | Existing campaign email queue per environment (no separate SMS queue) |
| API gateway | Route `/api/v1/phishing/sms-server-configurations/**` to phishing service |
| Twilio / REST SMS provider | Credentials configured per client via SMS server APIs |

### Generate encryption key

```bash
# Produces a 32-byte key, Base64-encoded
openssl rand -base64 32
```

Set as environment variable or SSM parameter:

```
PHISHING_CREDENTIALS_ENCRYPTION_KEY=<base64-32-byte-key>
```

Mapped in [`application.yml`](../services/phishing/service/src/main/resources/application.yml):

```yaml
phishing:
  credentials:
    encryption-key: ${PHISHING_CREDENTIALS_ENCRYPTION_KEY:}
```

> **Note:** Decrypt supports legacy Base64-encoded sender profile passwords. New saves use `ENC:` + AES-256-GCM. Re-save sender profiles or SMS server configs after setting a stable encryption key in production.

---

## Migration run order

Run scripts **in this order** against the target environment database:

| # | Script | Purpose |
|---|--------|---------|
| 1 | `campaigns-channel-default-email.js` | Backfill `channel: EMAIL` on existing campaigns |
| 2 | `email-templates-template-type-default-email.js` | Backfill `templateType: EMAIL` on existing templates |
| 3 | `sms-server-configurations-indexes.js` | Create indexes for new SMS collections |
| 4 | `payload-types-channel-default-email.js` | Backfill `channel: EMAIL` on existing `payload_types` documents |
| 5 | `short-urls-indexes.js` | Unique indexes on `short_urls` for per-recipient SMS short links |

### Connection string examples

```bash
MIGRATIONS_DIR="services/phishing/core/src/main/resources/migrations"

# Local
mongosh "mongodb://localhost:27017/phishing" --file "$MIGRATIONS_DIR/campaigns-channel-default-email.js"
mongosh "mongodb://localhost:27017/phishing" --file "$MIGRATIONS_DIR/email-templates-template-type-default-email.js"
mongosh "mongodb://localhost:27017/phishing" --file "$MIGRATIONS_DIR/sms-server-configurations-indexes.js"
mongosh "mongodb://localhost:27017/phishing" --file "$MIGRATIONS_DIR/payload-types-channel-default-email.js"
mongosh "mongodb://localhost:27017/phishing" --file "$MIGRATIONS_DIR/short-urls-indexes.js"

# Dev / Staging / Prod — use MONGODB_URI from environment
mongosh "$MONGODB_URI" --file "$MIGRATIONS_DIR/campaigns-channel-default-email.js"
mongosh "$MONGODB_URI" --file "$MIGRATIONS_DIR/email-templates-template-type-default-email.js"
mongosh "$MONGODB_URI" --file "$MIGRATIONS_DIR/sms-server-configurations-indexes.js"
mongosh "$MONGODB_URI" --file "$MIGRATIONS_DIR/payload-types-channel-default-email.js"
mongosh "$MONGODB_URI" --file "$MIGRATIONS_DIR/short-urls-indexes.js"
```

---

## MongoDB migration scripts

### 1. `campaigns-channel-default-email.js`

**Collection:** `campaigns`  
**Action:** Sets `channel: "EMAIL"` on documents missing the field.

```javascript
db.campaigns.updateMany(
  { channel: { $exists: false } },
  { $set: { channel: "EMAIL" } }
);
```

**Why:** Existing email campaigns created before smishing must be treated as `EMAIL` channel for wizard validation, launch routing, and reporting.

---

### 2. `email-templates-template-type-default-email.js`

**Collection:** `email_templates`  
**Action:** Sets `templateType: "EMAIL"` on documents missing the field.

```javascript
db.email_templates.updateMany(
  { templateType: { $exists: false } },
  { $set: { templateType: "EMAIL" } }
);
```

**Why:** Step 2 template selection validates `templateType` against campaign channel. Legacy templates are email-only.

---

### 3. `sms-server-configurations-indexes.js`

**Collections:** `sms_server_configurations`, `campaign_sms_deliveries`  
**Action:** Ensures compound indexes.

```javascript
db.sms_server_configurations.createIndex(
  { clientId: 1, isDefault: 1 },
  { name: "client_default_idx" }
);

db.sms_server_configurations.createIndex(
  { clientId: 1, name: 1 },
  { name: "client_name_idx", unique: true }
);

db.campaign_sms_deliveries.createIndex(
  { campaignId: 1, recipientId: 1 },
  { name: "campaign_recipient_idx", unique: true }
);
```

**Why:** Supports default-server lookup, unique server names per client, and per-recipient SMS delivery records.

> `configuration_audit_logs` index (`client_entity_idx`) is created automatically by Spring Data on first write.

---

## Schema changes (existing collections)

### `campaigns`

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `channel` | `EMAIL` \| `SMS` | `EMAIL` | Delivery channel |
| `smsServerConfigurationId` | String | — | Step 4 for SMS campaigns (replaces sender profile) |

**Unchanged for email:** `senderProfileId`, `emailTemplateId`, wizard steps 1–9 endpoints.

### `email_templates`

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `templateType` | `EMAIL` \| `SMS` | `EMAIL` | Template content type |
| `smsBody` | String | — | Plain-text SMS body (max 160 chars after placeholder replacement) |

SMS templates use `smsBody` instead of `emailSubject` / `emailBody`. Filter list API: `GET /email-templates?templateType=EMAIL|SMS`.

### `campaign_recipients` (embedded fields)

| Field | Type | Description |
|-------|------|-------------|
| `phoneNumber` | String | Already present; used for SMS recipient resolution |
| `smsSentAt` | Instant | SMS send timestamp |
| `smsDeliveredAt` | Instant | SMS delivery timestamp (webhook / provider callback) |

### `campaigns.stats` (embedded `CampaignStats`)

| Field | Type | Description |
|-------|------|-------------|
| `smsSent` | int | SMS messages sent |
| `smsDelivered` | int | SMS messages delivered |
| `smsFailed` | int | SMS send failures |

Email counters (`emailsSent`, `emailsOpened`, etc.) are unchanged.

---

## New collections

### `sms_server_configurations`

SMS provider credentials and routing per client.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `clientId` | String | Yes | Tenant ID |
| `name` | String | Yes | Unique per client |
| `provider` | `TWILIO` \| `GENERIC_REST` | Yes | Provider implementation |
| `apiKey` | String | Yes | Encrypted at rest (`ENC:...`) |
| `apiSecret` | String | Yes | Encrypted at rest |
| `senderId` | String | No | Sender ID / from number |
| `baseUrl` | String | No | Required for `GENERIC_REST` |
| `isDefault` | boolean | No | Default server for client |
| `status` | `ACTIVE` \| `INACTIVE` | No | Default `ACTIVE` |
| `providerMetadata` | Map | No | Provider-specific settings |

### `campaign_sms_deliveries`

Per-recipient SMS delivery audit trail.

| Field | Type | Description |
|-------|------|-------------|
| `campaignId` | String | Campaign reference |
| `recipientId` | String | Recipient reference |
| `recipient` | String | Phone number |
| `provider` | `TWILIO` \| `GENERIC_REST` | Provider used |
| `messageId` | String | Provider message ID |
| `status` | `PENDING` \| `SENT` \| `DELIVERED` \| `FAILED` \| `BOUNCED` | Delivery status |
| `sentAt` / `deliveredAt` / `clickedAt` | Instant | Timestamps |

### `configuration_audit_logs`

Audit trail for SMS server configuration changes.

| Field | Type | Description |
|-------|------|-------------|
| `clientId` | String | Tenant |
| `entityType` | `SMS_SERVER` | Entity type |
| `entityId` | String | Configuration ID |
| `action` | String | e.g. CREATE, UPDATE, DELETE |
| `actor` | String | User who made the change |
| `beforeSnapshot` / `afterSnapshot` | Object | Change diff |
| `timestamp` | Instant | When the change occurred |

### `short_urls`

Per-recipient shortened SMS links. `shortCode` is unique; one row per `trackingId`.

| Field | Type | Description |
|-------|------|-------------|
| `shortCode` | String | 10-char public path segment: env prefix (`01`/`10`/`11`) + 8 random (unique) |
| `trackingId` | String | Recipient tracking token (unique) |
| `originalUrl` | String | Existing `/t/phish/{trackingId}` landing URL |
| `shortUrl` | String | Public `https://{host}/{shortCode}` |
| `campaignId` / `recipientId` / `clientId` | String | Ownership |

---

## New enums

### Campaign types

| Value | Channel | Description |
|-------|---------|-------------|
| `SIMULATED_PHISHING` | EMAIL | Existing |
| `PHISHING_WITH_TRAINING` | EMAIL | Existing |
| `SMISHING_SIMULATION` | SMS | New |
| `SMISHING_WITH_TRAINING` | SMS | New |

### Activity types (email activities collection)

- `SMS_SENT`
- `SMS_DELIVERED`
- `SMS_FAILED`

---

## Application configuration

### SQS — separate delivery queues per channel

Email and SMS campaigns use **dedicated SQS queues** for full runtime isolation. Email delivery is unaffected by SMS traffic.

| Profile | Email queue | SMS queue |
|---------|-------------|-----------|
| local | `phishing-campaign-emails-queue` | `phishing-campaign-sms-queue` |
| dev | `phishing-campaign-emails-dev-queue` | `phishing-campaign-sms-dev-queue` |
| staging | `phishing-campaign-emails-staging-queue` | `phishing-campaign-sms-staging-queue` |
| prod | `phishing-campaign-emails` | `phishing-campaign-sms-prod-queue` |

Properties:

- Email: `aws.sqs.campaign-email-queue-url` → `CampaignEmailProducer` / `CampaignEmailListener`
- SMS: `aws.sqs.campaign-sms-queue-url` → `CampaignSmsProducer` / `CampaignSmsListener`

Override SMS queue URL per environment: `CAMPAIGN_SMS_SQS_QUEUE_URL`

**Provision** both queue + DLQ pairs per environment. IAM must allow `sqs:SendMessage`, `sqs:ReceiveMessage`, and `sqs:DeleteMessage` on both queues.

Email consumer tuning:

```yaml
aws.sqs.campaign-email-consumer:
  max-messages-per-receive: 10
  wait-time-seconds: 20
  max-receive-cycles-per-poll: 20
  visibility-timeout-seconds: 180
  poll-fixed-delay-ms: 2000
  scheduler-pool-size: 1
```

SMS consumer tuning:

```yaml
aws.sqs.campaign-sms-consumer:
  max-messages-per-receive: 10
  wait-time-seconds: 20
  max-receive-cycles-per-poll: 20
  visibility-timeout-seconds: 180
  poll-fixed-delay-ms: 2000
  scheduler-pool-size: 1
```

### SQS message formats

**Email (unchanged):**

```json
{
  "campaignId": "...",
  "recipientId": "...",
  "toEmail": "user@example.com",
  "smtpHost": "...",
  "subject": "...",
  "htmlBody": "..."
}
```

**SMS (new):**

```json
{
  "channel": "SMS",
  "campaignId": "...",
  "recipientId": "...",
  "clientId": "...",
  "trackingId": "...",
  "toPhone": "+1...",
  "messageBody": "...",
  "smsServerConfigurationId": "...",
  "campaignName": "..."
}
```

---

## New API endpoints

Base path: `/phishing/api/v1/phishing` (includes `server.servlet.context-path`)

### SMS server configuration

| Method | Path | Description |
|--------|------|-------------|
| GET | `/sms-server-configurations` | List configurations |
| GET | `/sms-server-configurations/{id}` | Get by ID |
| POST | `/sms-server-configurations` | Create |
| PUT | `/sms-server-configurations/{id}` | Update |
| DELETE | `/sms-server-configurations/{id}` | Delete |
| GET | `/sms-server-configurations/default` | Get default server |
| POST | `/sms-server-configurations/{id}/set-default` | Set as default |
| POST | `/sms-server-configurations/{id}/test` | Send test SMS |

### Campaign wizard (additive)

| Method | Path | Description |
|--------|------|-------------|
| PUT | `/campaigns/{id}/step/4/sms-server` | Step 4 for SMS campaigns |

**Campaign response alias (SMS):** `GET /campaigns` and `GET /campaigns/{id}` mirror `smsServerConfigurationId` / `smsServerConfigurationName` into `senderProfileId` / `senderProfileName` when `channel` is `SMS`, so the step-4 UI can reuse existing email fields. Email campaigns return real sender profile values unchanged.

**Unchanged for email:**

- `PUT /campaigns/{id}/step/4` — sender profile (email step 4)
- All other wizard steps and launch endpoints

### SMS short-link redirect (public tracking prefix)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/t/s/{shortCode}` | 302 to the original `/t/phish/{trackingId}` URL |

Already covered by gateway public URLs `/gateway/phishing/t/**`. No `public-urls.json` change.

### Campaign list filter

```
GET /campaigns?channel=EMAIL
GET /campaigns?channel=SMS
```

Omitting `channel` returns all campaigns (backward compatible).

---

## Deployment checklist

1. **Set** `PHISHING_CREDENTIALS_ENCRYPTION_KEY` in SSM / secrets manager for each environment.
2. **Run** MongoDB migrations (order above) against `phishing` database.
3. **Deploy** phishing service with smishing code.
4. **Verify** API gateway routes for `/sms-server-configurations/**`.
5. **Provision** SQS `phishing-campaign-sms-*` queue + DLQ per environment; confirm `phishing-campaign-emails-*` queue + DLQ exists.
6. **Set** `CAMPAIGN_SMS_SQS_QUEUE_URL` in deployment configs (or rely on profile defaults).
7. **Create** at least one SMS server configuration per client before launching SMS campaigns.
8. **Smoke test:**
   - Existing email campaign launch still works (messages on email queue only)
   - New SMS campaign: wizard steps 1 → 9, launch, message appears on `phishing-campaign-sms-*` queue

---

## Post-migration verification

```javascript
// No campaigns without channel
db.campaigns.countDocuments({ channel: { $exists: false } })
// Expected: 0

// No templates without templateType
db.email_templates.countDocuments({ templateType: { $exists: false } })
// Expected: 0

// Indexes exist
db.sms_server_configurations.getIndexes()
db.campaign_sms_deliveries.getIndexes()

// Sample email campaign unchanged
db.campaigns.findOne({ channel: "EMAIL" }, { campaignName: 1, senderProfileId: 1, channel: 1 })
```

---

## Rollback notes

| Change | Rollback impact |
|--------|-----------------|
| `channel` / `templateType` backfill | Safe to leave; fields are ignored by old code if rolled back |
| New collections | Can remain empty; no impact on email-only deployments |
| Separate SQS queues | Email and SMS queues are independent; rolling back SMS code leaves email queue unaffected |
| Encryption key | **Do not rotate** without re-encrypting stored credentials |

If rolling back the application only (keeping DB migrations):

- Email campaigns continue to work (`channel: EMAIL` or missing field handled by old listener as email-only).
- SMS-specific data in `sms_server_configurations` and `campaign_sms_deliveries` is inert until smishing code is redeployed.

---

## SMS URL shortener

> **Nginx checklist (ASAT + landing):** [sms-short-link-nginx.md](sms-short-link-nginx.md)

Smishing SMS bodies use a root-path short link on the campaign's verified tracking domain. Codes are **10 characters**: a 2-digit environment prefix + 8-character random suffix.

| Env | Prefix | Example |
|-----|--------|---------|
| Development | `01` | `https://online-banking.tech/01k7Qm2Nxp` |
| Staging | `10` | `https://online-banking.tech/10k7Qm2Nxp` |
| Production | `11` | `https://online-banking.tech/11k7Qm2Nxp` |

Config: `shortener.env-prefix` (`01` / `10` / `11`) and `shortener.code-length` (random suffix, default `8`). The **full 10-character** value is stored as `shortCode` and resolved via `GET /t/s/{shortCode}` (302 to `/t/phish/{trackingId}`).

No new gateway public-url patterns are required. The redirect lives under the existing public tracking prefix `/gateway/phishing/t/**`.

### 5. `short-urls-indexes.js`

**Collection:** `short_urls`  
**Action:** Unique indexes on `shortCode` and `trackingId`.

```javascript
db.short_urls.createIndex({ shortCode: 1 }, { name: "short_code_idx", unique: true });
db.short_urls.createIndex({ trackingId: 1 }, { name: "tracking_id_idx", unique: true });
```

### Landing-server nginx (required companion change)

Custom tracking domains CNAME to the landing server (`15.204.246.8`). That nginx routes all three envs by path prefix; short links are routed by the code’s first two digits (`01` → DEV, `10` → staging, `11` → prod). See [sms-short-link-nginx.md](sms-short-link-nginx.md) and [landing-server-nginx.conf](landing-server-nginx.conf).

Legacy **8-character** codes (without env prefix) will not match the new landing rules; re-launch SMS after deploy.

```bash
docker compose exec nginx nginx -t && docker compose exec nginx nginx -s reload
curl -skI https://online-banking.tech/01{eight} | grep -iE '^(HTTP|Location|Content-Type|Content-Length)'
```

---

## Email campaign backward compatibility

| Area | Impact |
|------|--------|
| Wizard API | Same endpoints; `channel` optional on step 1 (defaults to `EMAIL`) |
| Step 4 | Email still uses sender profile endpoint |
| SQS producer | `CampaignEmailProducer` unchanged message format |
| SQS consumer | `CampaignEmailListener` processes email queue only; SMS uses `CampaignSmsListener` |
| Frontend | No changes required for existing email campaign UI |
| Reporting | Email metrics use existing `emailsSent` / open / click counters |

---

## Related files

| Area | Path |
|------|------|
| Migration scripts | `services/phishing/core/src/main/resources/migrations/` |
| SMS entities | `services/phishing/core/src/main/java/com/aspire/asat/phishing/model/SmsServerConfiguration.java` |
| Delivery orchestration | `services/phishing/core/src/main/java/com/aspire/asat/phishing/delivery/` |
| SQS email listener | `services/phishing/core/src/main/java/com/aspire/asat/phishing/listener/CampaignEmailListener.java` |
| SQS SMS listener | `services/phishing/core/src/main/java/com/aspire/asat/phishing/listener/CampaignSmsListener.java` |
| Credential encryption | `services/phishing/core/src/main/java/com/aspire/asat/phishing/service/support/CredentialEncryptionService.java` |
| Profile YAML | `services/phishing/service/src/main/resources/application-*.yml` |
