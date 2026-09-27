---
name: ""
overview: ""
todos: []
isProject: false
---

# Migrate Properties to AWS SSM Parameter Store

## Overview

Move sensitive application properties from `application-*.yml` into **AWS Systems Manager Parameter Store**. Your app already loads parameters from SSM via [SsmParameterInitializer](common/core/src/main/java/com/aspire/asat/common/config/aws/SsmParameterInitializer.java) and [SsmService](common/core/src/main/java/com/aspire/asat/common/service/aws/SsmService.java); no code changes are required. You only need to **create the parameters in AWS** and **remove or redact** values from YAML.

---

## How SSM loading works today

- **Path pattern:** `{prefix}/{profile}/{service-name}/` (e.g. `/asat/dev/auth-service/`).
- **Config:** `aws.ssm.parameter-path-prefix` (default `/asat`), from `application.yml`.
- **Behavior:** At startup, the app fetches all parameters under that path. Each parameter’s **full name** is like `/asat/dev/auth-service/spring.data.mongodb.uri`. The part **after the path** becomes the Spring property key (e.g. `spring.data.mongodb.uri`). Those key-value pairs are added as a high-priority `MapPropertySource`, so `@Value("${spring.data.mongodb.uri}")` resolves from SSM when the parameter exists.
- **Encryption:** Use **SecureString** for sensitive parameters; the app calls `withDecryption(true)` and can use KMS decryption if you store KMS-encrypted values.

---

## Parameter path and naming convention

- **Path:** `/asat/{profile}/{service-name}/`
  - Examples: `/asat/dev/auth-service/`, `/asat/prod/notification-service/`
- **Full parameter name:** `{path}{property-key}` (no extra slash between path and key; SSM allows dots in names)
  - Example: `/asat/dev/auth-service/spring.data.mongodb.uri`
- **Parameter name (key):** Use the **exact Spring property key** (e.g. `spring.data.mongodb.uri`, `jwt.secret`). Dots are allowed in SSM parameter names.

**Parameter type:**

- **SecureString** for all secrets (passwords, connection strings, API keys, JWT secret, etc.).
- **String** only for non-sensitive values if you choose to move them (e.g. URLs).

---

## Properties to migrate (by service)

Use these as the **parameter name** (suffix under the path). Create one parameter per line under `/asat/{profile}/{service-name}/`.

### Shared (multiple services)


| Parameter name (under path)       | Used in                                                                          | Type         |
| --------------------------------- | -------------------------------------------------------------------------------- | ------------ |
| `spring.data.mongodb.uri`         | auth, registration, notification, phishing, billing, cms, universal, vps, sesame | SecureString |
| `aws.credentials.access-key`      | auth, registration, notification, billing, cms, etc.                             | SecureString |
| `aws.credentials.secret-key`      | same                                                                             | SecureString |
| `azure.storage.connection-string` | auth, registration, billing, cms                                                 | SecureString |
| `redis.password`                  | auth, gateway                                                                    | SecureString |


Where a service uses `aws.accessKeyId` / `aws.secretAccessKey` (e.g. cms), use those exact keys: `aws.accessKeyId`, `aws.secretAccessKey`.

### Auth service (`/asat/{profile}/auth-service/`)


| Parameter name       | Type         |
| -------------------- | ------------ |
| `jwt.secret`         | SecureString |
| `mfa.encryption-key` | SecureString |


### Notification service (`/asat/{profile}/notification-service/`)


| Parameter name                            | Type                   |
| ----------------------------------------- | ---------------------- |
| `aws.ses.smtp.username`                   | SecureString           |
| `aws.ses.smtp.password`                   | SecureString           |
| `sms.providers.bangladesh.account-sid`    | SecureString           |
| `sms.providers.bangladesh.auth-token`     | SecureString           |
| `sms.providers.bangladesh.from-number`    | String or SecureString |
| `sms.providers.north-america.account-sid` | SecureString           |
| `sms.providers.north-america.auth-token`  | SecureString           |
| `sms.providers.north-america.from-number` | String or SecureString |
| `sms.providers.default.account-sid`       | SecureString           |
| `sms.providers.default.auth-token`        | SecureString           |
| `sms.providers.default.from-number`       | String or SecureString |
| `phone-call.providers.twilio.account-sid` | SecureString           |
| `phone-call.providers.twilio.auth-token`  | SecureString           |
| `phone-call.providers.twilio.from-number` | String or SecureString |
| `phone-call.providers.default.api-key`    | SecureString           |
| `phone-call.providers.default.api-secret` | SecureString           |


### Billing service (`/asat/{profile}/billing-service/`)


| Parameter name          | Type         |
| ----------------------- | ------------ |
| `stripe.secret-key`     | SecureString |
| `stripe.public-key`     | SecureString |
| `stripe.webhook-secret` | SecureString |
| `paypal.client-id`      | SecureString |
| `paypal.secret`         | SecureString |


### Gateway service (`/asat/{profile}/gateway-service/`)


| Parameter name   | Type              |
| ---------------- | ----------------- |
| `redis.host`     | String (optional) |
| `redis.port`     | String (optional) |
| `redis.password` | SecureString      |


### Registration, CMS, Universal, VPS, Sesame, Phishing, Course

- Use the **shared** parameters above (Mongo URI, AWS credentials, Azure connection string) under each service’s path.
- Add any service-specific secrets (e.g. API keys) as additional parameters under that path.

---

## Tags (SSM Parameter Store)

Parameter Store supports tags. Recommended for each parameter (or at least for the path hierarchy if you use a script):


| Tag key       | Example value           |
| ------------- | ----------------------- |
| `Environment` | `dev`, `prod`           |
| `Service`     | `auth-service`          |
| `Application` | `asat`                  |
| `ManagedBy`   | `manual` or `terraform` |


---

## Steps to migrate

### 1. Create parameters in AWS

**Option A – AWS Console**

1. Sign in to AWS Console (your IAM user).
2. Go to **Systems Manager** → **Parameter Store**.
3. For each parameter:
  - **Create parameter**
  - **Name:** full path + key, e.g. `asat/dev/auth-service/spring.data.mongodb.uri` (no leading slash in the Console name field; if the UI shows path as `/asat/...`, use that).
  - **Type:** SecureString (for secrets); optionally choose a KMS key or use default.
  - **Value:** the secret value.
  - **Tags:** Environment, Service, Application, ManagedBy.
4. Repeat for every parameter in the tables above, for each `{profile}` and `{service-name}` you use (e.g. `dev`, `prod`).

**Option B – AWS CLI**

```bash
# Example: auth-service dev
aws ssm put-parameter \
  --name "/asat/dev/auth-service/spring.data.mongodb.uri" \
  --type "SecureString" \
  --value "mongodb://user:pass@host:27017/..." \
  --region us-east-1

aws ssm put-parameter \
  --name "/asat/dev/auth-service/jwt.secret" \
  --type "SecureString" \
  --value "your-jwt-secret" \
  --region us-east-1
```

Repeat for all parameters and environments. You can script this from a key-value file or template.

### 2. IAM permissions

Ensure the IAM user/role used by the app has:

- `ssm:GetParametersByPath` on path `/asat/*` (or the prefix you use).
- `ssm:GetParameter` (for single-parameter fetch if used).
- If you use custom KMS encryption for SecureString: `kms:Decrypt` on that key (your existing KMS setup likely already covers this).

### 3. Clean up application YAML

- In `application-dev.yml`, `application-local.yml`, `application-prod.yml`, etc.:
  - **Remove** or **redact** the default values for any property now stored in SSM (e.g. replace `uri: ${MONGODB_URI:mongodb://...}` with `uri: ${MONGODB_URI:}` or rely on SSM-only by removing the line and ensuring the property is only in SSM).
- Keep non-sensitive config (ports, URLs, feature flags) in YAML; move only secrets to SSM.
- Ensure `aws.ssm.enabled` is `true` (or set via env) for environments that use SSM.

### 4. Verify

- Run the app with the same profile (e.g. `dev`) and confirm it starts and uses SSM (e.g. DB connection works, JWT validation works).
- Confirm no secrets remain in YAML for the migrated keys.

---

## Quick reference: parameter full names (examples for dev)

- `/asat/dev/auth-service/spring.data.mongodb.uri`
- `/asat/dev/auth-service/aws.credentials.access-key`
- `/asat/dev/auth-service/aws.credentials.secret-key`
- `/asat/dev/auth-service/azure.storage.connection-string`
- `/asat/dev/auth-service/redis.password`
- `/asat/dev/auth-service/jwt.secret`
- `/asat/dev/auth-service/mfa.encryption-key`
- `/asat/dev/registration-service/spring.data.mongodb.uri`
- `/asat/dev/registration-service/aws.credentials.access-key`
- `/asat/dev/registration-service/aws.credentials.secret-key`
- `/asat/dev/notification-service/spring.data.mongodb.uri`
- `/asat/dev/notification-service/aws.ses.smtp.username`
- `/asat/dev/notification-service/aws.ses.smtp.password`
- … (same pattern for billing, gateway, cms, etc., and for `prod` instead of `dev`)

No code changes are required; once these parameters exist and the app has SSM permissions, it will load them at startup and override any values still in YAML.