# AWS SSM Parameter Store – Property Mapping

Your app loads SSM parameters from path: **`/asat/{profile}/{serviceName}/`**  
Each parameter under that path is keyed by the **property name** (the part after the path).  
So the **full SSM parameter name** = `/asat/{profile}/{serviceName}/{propertyKey}`.

Example: for registration-service on profile `dev`, store MongoDB URI in SSM as:
- **SSM name:** `/asat/dev/asat-service/spring.data.mongodb.uri`
- **SSM value:** `mongodb://user:pass@host:27017/registration?authSource=admin`

---

## Service name → SSM path segment

| Service (app)     | spring.application.name | SSM serviceName   |
|-------------------|-------------------------|-------------------|
| Registration      | asat                    | asat-service      |
| Auth              | auth-service            | auth-service      |
| Notification      | notification-service    | notification-service |
| Gateway           | gateway-service         | gateway-service   |
| Phishing          | phishing-service        | phishing-service  |
| CMS               | asat                    | asat-service      |
| Billing           | asat                    | asat-service      |

Use the **SSM serviceName** in the path: `/asat/{profile}/{serviceName}/{propertyKey}`.

---

## 1. Registration service (`asat-service`)

### Local

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://user:password@asat-mongodb:27017/registration?authSource=admin | /asat/local/asat-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | registration | /asat/local/asat-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/local/asat-service/aws.region |
| aws.credentials.access-key | (your access key) | /asat/local/asat-service/aws.credentials.access-key |
| aws.credentials.secret-key | (your secret key) | /asat/local/asat-service/aws.credentials.secret-key |
| aws.s3.bucket-name | asatv2-media-bucket | /asat/local/asat-service/aws.s3.bucket-name |
| azure.storage.connection-string | DefaultEndpointsProtocol=https;AccountName=...;AccountKey=${AZURE_STORAGE_ACCOUNT_KEY}
| azure.storage.container-name | strgblobstandardasatv2 | /asat/local/asat-service/azure.storage.container-name |
| microsoft.entra.client-id | (client-id) | /asat/local/asat-service/microsoft.entra.client-id |
| microsoft.entra.client-secret | (client-secret) | /asat/local/asat-service/microsoft.entra.client-secret |
| microsoft.entra.tenant-id | common | /asat/local/asat-service/microsoft.entra.tenant-id |

### Dev

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://docdbadmin:***@staging-docdb-cluster... | /asat/dev/asat-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | registration | /asat/dev/asat-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/dev/asat-service/aws.region |
| aws.credentials.access-key | (dev access key) | /asat/dev/asat-service/aws.credentials.access-key |
| aws.credentials.secret-key | (dev secret key) | /asat/dev/asat-service/aws.credentials.secret-key |
| aws.s3.bucket-name | asatv2-media-bucket | /asat/dev/asat-service/aws.s3.bucket-name |
| azure.storage.connection-string | (Azure connection string) | /asat/dev/asat-service/azure.storage.connection-string |
| azure.storage.container-name | strgblobstandardasatv2 | /asat/dev/asat-service/azure.storage.container-name |
| internal.service.api-key | (shared secret — see Common section) | /asat/common/dev/internal.service.api-key |

### Staging

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| internal.service.api-key | (shared secret — see Common section) | /asat/common/staging/internal.service.api-key |

### Prod

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://aspiredba:***@15.204.231.26:28395/?authSource=admin | /asat/prod/asat-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | registration | /asat/prod/asat-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/prod/asat-service/aws.region |
| aws.credentials.access-key | (prod access key) | /asat/prod/asat-service/aws.credentials.access-key |
| aws.credentials.secret-key | (prod secret key) | /asat/prod/asat-service/aws.credentials.secret-key |
| aws.s3.bucket-name | asatv2-media-bucket | /asat/prod/asat-service/aws.s3.bucket-name |
| azure.storage.connection-string | (Azure connection string) | /asat/prod/asat-service/azure.storage.connection-string |
| azure.storage.container-name | strgblobstandardasatv2 | /asat/prod/asat-service/azure.storage.container-name |
| internal.service.api-key | (shared secret — see Common section) | /asat/common/prod/internal.service.api-key |

---

## 2. Auth service (`auth-service`)

### Local

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://user:password@asat-mongodb:27017/registration?authSource=admin | /asat/local/auth-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | registration | /asat/local/auth-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/local/auth-service/aws.region |
| aws.credentials.access-key | (your access key) | /asat/local/auth-service/aws.credentials.access-key |
| aws.credentials.secret-key | (your secret key) | /asat/local/auth-service/aws.credentials.secret-key |
| aws.s3.bucket-name | asatv2-media-bucket | /asat/local/auth-service/aws.s3.bucket-name |
| azure.storage.connection-string | (Azure connection string) | /asat/local/auth-service/azure.storage.connection-string |
| azure.storage.container-name | strgblobstandardasatv2 | /asat/local/auth-service/azure.storage.container-name |
| jwt.secret | (JWT signing secret) | /asat/local/auth-service/jwt.secret |
| redis.host | 127.0.0.1 | /asat/local/auth-service/redis.host |
| redis.port | 6379 | /asat/local/auth-service/redis.port |
| redis.password | (optional) | /asat/local/auth-service/redis.password |
| mfa.encryption-key | (MFA TOTP encryption key) | /asat/local/auth-service/mfa.encryption-key |

### Dev

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://docdbadmin:***@staging-docdb-cluster... | /asat/dev/auth-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | registration | /asat/dev/auth-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/dev/auth-service/aws.region |
| aws.credentials.access-key | (dev access key) | /asat/dev/auth-service/aws.credentials.access-key |
| aws.credentials.secret-key | (dev secret key) | /asat/dev/auth-service/aws.credentials.secret-key |
| aws.s3.bucket-name | asatv2-media-bucket | /asat/dev/auth-service/aws.s3.bucket-name |
| azure.storage.connection-string | (Azure connection string) | /asat/dev/auth-service/azure.storage.connection-string |
| azure.storage.container-name | strgblobstandardasatv2 | /asat/dev/auth-service/azure.storage.container-name |
| jwt.secret | (JWT signing secret) | /asat/dev/auth-service/jwt.secret |
| redis.host | asatv2-redis....cache.amazonaws.com | /asat/dev/auth-service/redis.host |
| redis.port | 6379 | /asat/dev/auth-service/redis.port |
| redis.password | (if set) | /asat/dev/auth-service/redis.password |
| mfa.encryption-key | (MFA TOTP encryption key) | /asat/dev/auth-service/mfa.encryption-key |

### Prod

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://aspiredba:***@15.204.231.26:28395/?authSource=admin | /asat/prod/auth-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | registration | /asat/prod/auth-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/prod/auth-service/aws.region |
| aws.credentials.access-key | (prod access key) | /asat/prod/auth-service/aws.credentials.access-key |
| aws.credentials.secret-key | (prod secret key) | /asat/prod/auth-service/aws.credentials.secret-key |
| aws.s3.bucket-name | asatv2-media-bucket | /asat/prod/auth-service/aws.s3.bucket-name |
| azure.storage.connection-string | (Azure connection string) | /asat/prod/auth-service/azure.storage.connection-string |
| azure.storage.container-name | strgblobstandardasatv2 | /asat/prod/auth-service/azure.storage.container-name |
| jwt.secret | (JWT signing secret) | /asat/prod/auth-service/jwt.secret |
| redis.host | (prod Redis host) | /asat/prod/auth-service/redis.host |
| redis.port | 6379 | /asat/prod/auth-service/redis.port |
| redis.password | (prod Redis password) | /asat/prod/auth-service/redis.password |
| mfa.encryption-key | (MFA TOTP encryption key) | /asat/prod/auth-service/mfa.encryption-key |

---

## 3. Notification service (`notification-service`)

### Local / Dev

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://user:password@asat-mongodb:27017/notification?authSource=admin | /asat/local/notification-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | notification | /asat/local/notification-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/local/notification-service/aws.region |
| aws.credentials.access-key | (your access key) | /asat/local/notification-service/aws.credentials.access-key |
| aws.credentials.secret-key | (your secret key) | /asat/local/notification-service/aws.credentials.secret-key |
| aws.ses.smtp.username | (SES SMTP access key) | /asat/local/notification-service/aws.ses.smtp.username |
| aws.ses.smtp.password | (SES SMTP secret) | /asat/local/notification-service/aws.ses.smtp.password |
| sms.providers.bangladesh.account-sid | (Twilio SID) | /asat/local/notification-service/sms.providers.bangladesh.account-sid |
| sms.providers.bangladesh.auth-token | (Twilio token) | /asat/local/notification-service/sms.providers.bangladesh.auth-token |
| sms.providers.bangladesh.from-number | +18302894745 | /asat/local/notification-service/sms.providers.bangladesh.from-number |
| phone-call.providers.twilio.account-sid | (Twilio SID) | /asat/local/notification-service/phone-call.providers.twilio.account-sid |
| phone-call.providers.twilio.auth-token | (Twilio token) | /asat/local/notification-service/phone-call.providers.twilio.auth-token |

### Prod

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://aspiredba:***@15.204.231.26:28395/?authSource=admin | /asat/prod/notification-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | notification | /asat/prod/notification-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/prod/notification-service/aws.region |
| aws.credentials.access-key | (prod access key) | /asat/prod/notification-service/aws.credentials.access-key |
| aws.credentials.secret-key | (prod secret key) | /asat/prod/notification-service/aws.credentials.secret-key |
| aws.ses.smtp.username | (prod SES SMTP key) | /asat/prod/notification-service/aws.ses.smtp.username |
| aws.ses.smtp.password | (prod SES SMTP secret) | /asat/prod/notification-service/aws.ses.smtp.password |

Use the same SSM name pattern with `dev` for dev: `/asat/dev/notification-service/{propertyKey}`.

---

## 4. Gateway service (`gateway-service`)

### Local

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| jwt.secret | (JWT verification secret) | /asat/local/gateway-service/jwt.secret |
| redis.host | localhost | /asat/local/gateway-service/redis.host |
| redis.port | 6379 | /asat/local/gateway-service/redis.port |
| redis.password | (optional) | /asat/local/gateway-service/redis.password |

Use `dev` / `prod` in path for dev and prod: `/asat/dev/gateway-service/...`, `/asat/prod/gateway-service/...`.

---

## 5. Phishing service (`phishing-service`)

### Local

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://user:password@asat-mongodb:27017/phishing?authSource=admin | /asat/local/phishing-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | phishing | /asat/local/phishing-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/local/phishing-service/aws.region |
| aws.credentials.access-key | (your access key) | /asat/local/phishing-service/aws.credentials.access-key |
| aws.credentials.secret-key | (your secret key) | /asat/local/phishing-service/aws.credentials.secret-key |

### Dev

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | (dev MongoDB URI) | /asat/dev/phishing-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | phishing | /asat/dev/phishing-service/spring.data.mongodb.database |
| aws.region | us-east-1 | /asat/dev/phishing-service/aws.region |
| aws.credentials.access-key | (dev access key) | /asat/dev/phishing-service/aws.credentials.access-key |
| aws.credentials.secret-key | (dev secret key) | /asat/dev/phishing-service/aws.credentials.secret-key |

---

## 6. CMS service (uses `asat` → `asat-service`)

### Local

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://user:password@asat-mongodb:27017/cms?authSource=admin | /asat/local/asat-service/spring.data.mongodb.uri |
| spring.data.mongodb.database | cms | /asat/local/asat-service/spring.data.mongodb.database |
| azure.storage.connection-string | (Azure connection string) | /asat/local/asat-service/azure.storage.connection-string |
| azure.storage.container-name | strgblobstandardasatv2 | /asat/local/asat-service/azure.storage.container-name |
| aws.region | us-east-1 | /asat/local/asat-service/aws.region |
| aws.accessKeyId | (access key) | /asat/local/asat-service/aws.accessKeyId |
| aws.secretAccessKey | (secret key) | /asat/local/asat-service/aws.secretAccessKey |
| aws.s3.bucket-name | asatv2-media-bucket | /asat/local/asat-service/aws.s3.bucket-name |

**Note:** Registration and CMS both use `spring.application.name: asat`, so they share the same SSM path prefix `/asat/{profile}/asat-service/`. Use different parameter names or separate apps/profiles if you need different values per service.

### Dev

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://docdbadmin:***@staging-docdb-cluster... | /asat/dev/asat-service/spring.data.mongodb.uri |
| aws.accessKeyId | (dev access key) | /asat/dev/asat-service/aws.accessKeyId |
| aws.secretAccessKey | (dev secret key) | /asat/dev/asat-service/aws.secretAccessKey |

### Prod

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://aspiredba:***@15.204.231.26:28395/?authSource=admin | /asat/prod/asat-service/spring.data.mongodb.uri |
| aws.accessKeyId | (prod access key) | /asat/prod/asat-service/aws.accessKeyId |
| aws.secretAccessKey | (prod secret key) | /asat/prod/asat-service/aws.secretAccessKey |

---

## 7. Billing service (uses `asat` → `asat-service`)

### Local / Dev / Prod

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| spring.data.mongodb.uri | mongodb://.../paymentModule?authSource=admin | /asat/local/asat-service/spring.data.mongodb.uri (or dev/prod) |
| spring.data.mongodb.database | paymentModule | /asat/local/asat-service/spring.data.mongodb.database |
| azure.storage.connection-string | (Azure connection string) | /asat/local/asat-service/azure.storage.connection-string |
| azure.storage.container-name | strgblobstandardasatv2 | /asat/local/asat-service/azure.storage.container-name |
| aws.region | us-east-1 | /asat/local/asat-service/aws.region |
| aws.credentials.access-key | (access key) | /asat/local/asat-service/aws.credentials.access-key |
| aws.credentials.secret-key | (secret key) | /asat/local/asat-service/aws.credentials.secret-key |
| aws.s3.bucket-name | asatv2-media-bucket | /asat/local/asat-service/aws.s3.bucket-name |
| stripe.secret-key | sk_test_... or sk_live_... | /asat/local/asat-service/stripe.secret-key |
| stripe.public-key | pk_test_... or pk_live_... | /asat/local/asat-service/stripe.public-key |
| stripe.webhook-secret | whsec_... | /asat/local/asat-service/stripe.webhook-secret |
| paypal.client-id | (PayPal client id) | /asat/local/asat-service/paypal.client-id |
| paypal.secret | (PayPal secret) | /asat/local/asat-service/paypal.secret |
| internal.service.api-key | (shared secret — see Common section) | /asat/common/{profile}/internal.service.api-key |

Replace `local` with `dev` or `prod` in the path for dev/prod. Same path collision note as CMS: billing also uses `asat-service` path.

---

## 8. Common (shared across services)

`SsmParameterInitializer` loads parameters from **`/asat/common/{profile}/`** first, then service-specific paths under `/asat/{profile}/{serviceName}/`. Service-specific values override common values when the same property key exists in both paths.

### Internal service API key (registration ↔ billing)

Used for service-to-service auth on billing invoice create/update (`X-Internal-Service-Key`). **Store once per environment** in the common path so both registration (`asat-service`) and billing (`billing-service`) receive the same value.

| Profile | SSM parameter name | Property key | Used by | Type |
|---------|-------------------|--------------|---------|------|
| dev | `/asat/common/dev/internal.service.api-key` | `internal.service.api-key` | registration, billing | SecureString |
| staging | `/asat/common/staging/internal.service.api-key` | `internal.service.api-key` | registration, billing | SecureString |
| prod | `/asat/common/prod/internal.service.api-key` | `internal.service.api-key` | registration, billing | SecureString |

**Env override:** `INTERNAL_SERVICE_API_KEY` (optional; takes precedence over SSM when set in the pod/CI).

**Billing startup:** When `internal.service.auth-enabled: true`, billing fails to start if the API key is blank.

**Deploy:** Redeploy billing and registration together after creating or rotating the key. Local dev uses `application-local.yml` default (`local-dev-internal-service-key`).

#### Create parameters (ops runbook)

Generate a unique secret per environment (do not commit values to git):

```bash
openssl rand -base64 32
```

Dev example:

```bash
aws ssm put-parameter \
  --name "/asat/common/dev/internal.service.api-key" \
  --value "<generated-secret>" \
  --type SecureString \
  --overwrite
```

Repeat for `staging` and `prod` with **different** values:

```bash
aws ssm put-parameter \
  --name "/asat/common/staging/internal.service.api-key" \
  --value "<generated-secret>" \
  --type SecureString \
  --overwrite

aws ssm put-parameter \
  --name "/asat/common/prod/internal.service.api-key" \
  --value "<generated-secret>" \
  --type SecureString \
  --overwrite
```

### Other common parameters

| YAML property key | Example value | SSM parameter name (store this in SSM) |
|-------------------|---------------|----------------------------------------|
| app.files.presignTtlSeconds | 900 | /asat/common/{profile}/app.files.presignTtlSeconds |
| app.aws.bucket | asatv2-media-bucket | /asat/common/{profile}/app.aws.bucket |
| app.aws.region | us-east-1 | /asat/common/{profile}/app.aws.region |
| app.aws.accessKeyId | (access key) | /asat/common/{profile}/app.aws.accessKeyId |
| app.aws.secretAccessKey | (secret key) | /asat/common/{profile}/app.aws.secretAccessKey |
| app.aws.cloudFront.keyPairId | KK6RXRIIK7ENS | /asat/common/{profile}/app.aws.cloudFront.keyPairId |
| app.azures.storage.connection-string | (Azure) | /asat/common/{profile}/app.azures.storage.connection-string |
| app.azures.storage.container-name | (container) | /asat/common/{profile}/app.azures.storage.container-name |
| notification.service.url | http://asat-notification-service:5656 | /asat/common/{profile}/notification.service.url |
| notification.service.api-path | /notification/api/v1 | /asat/common/{profile}/notification.service.api-path |

Replace `{profile}` with `local`, `dev`, `staging`, or `prod`.

---

## Summary: SSM naming rule

- **Full SSM parameter name** = `/asat` + `/{profile}` + `/{serviceName}` + `/{propertyKey}`
- **profile**: `local`, `dev`, or `prod`
- **serviceName**: `asat-service`, `auth-service`, `notification-service`, `gateway-service`, `phishing-service` (see table above)
- **propertyKey**: exact YAML property key with dots (e.g. `spring.data.mongodb.uri`, `aws.credentials.access-key`)

Store **sensitive** values (DB URIs, AWS keys, JWT secret, Stripe/PayPal/Twilio/Azure keys) as **SecureString** in SSM and optionally use KMS (your app supports `aws.ssm.kms-enabled`).
