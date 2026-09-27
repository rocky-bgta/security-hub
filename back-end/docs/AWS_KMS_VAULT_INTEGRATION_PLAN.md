# AWS KMS Vault Integration Plan

## Overview
This document outlines the step-by-step plan to integrate AWS KMS vault with SSM Parameter Store across all Spring Boot applications in the ASAT-V2-BACKEND project.

## Architecture Flow
```
Main Method → SSM Parameter Initializer → Fetch Parameters by Profile → Decrypt with KMS → Load into Application Context
```

## Prerequisites
- AWS SDK v2 is already included (version 2.33.11)
- Applications use Spring profiles (local, dev, uat, prod)
- Common module exists at `common/core`

---

## Phase 1: Add AWS SDK Dependencies

### 1.1 Update Root build.gradle
Add SSM and KMS SDK dependencies to the root `build.gradle`:

**Location:** `build.gradle`

**Changes:**
- Add `awsSdkSsm` and `awsSdkKms` to the `libraries.core` section

```gradle
awsSdkSsm: "software.amazon.awssdk:ssm:${versions.core.awsSdk}",
awsSdkKms: "software.amazon.awssdk:kms:${versions.core.awsSdk}",
```

---

## Phase 2: Create Common Infrastructure (Common Module)

### 2.1 Create SSM Parameter Initializer
**Location:** `common/core/src/main/java/com/aspire/asat/common/config/aws/SsmParameterInitializer.java`

**Purpose:** 
- Initialize SSM client
- Fetch parameters based on active Spring profile
- Decrypt parameters using KMS
- Load decrypted values into Spring Environment

**Key Features:**
- Accepts ApplicationContext to get active profile
- Fetches parameters from SSM Parameter Store path: `/asat/{profile}/{service-name}/`
- Decrypts SecureString parameters using KMS
- Adds decrypted values to Spring Environment before application starts

### 2.2 Create KMS Service
**Location:** `common/core/src/main/java/com/aspire/asat/common/service/aws/KmsService.java`

**Purpose:**
- Decrypt encrypted values using AWS KMS
- Handle KMS client initialization
- Provide decryption utility methods

### 2.3 Create SSM Service
**Location:** `common/core/src/main/java/com/aspire/asat/common/service/aws/SsmService.java`

**Purpose:**
- Fetch parameters from SSM Parameter Store
- Handle parameter path resolution based on profile
- Retrieve both String and SecureString parameters

### 2.4 Create Configuration Properties
**Location:** `common/core/src/main/java/com/aspire/asat/common/config/aws/AwsSsmConfig.java`

**Purpose:**
- Configuration properties for SSM parameter paths
- KMS key ID configuration
- AWS region configuration

### 2.5 Update Common Core build.gradle
**Location:** `common/core/build.gradle`

**Changes:**
- Add AWS SSM and KMS SDK dependencies

```gradle
implementation project.rootProject.ext.libraries.core.awsSdkSsm
implementation project.rootProject.ext.libraries.core.awsSdkKms
```

---

## Phase 3: Integration per Spring Boot Application

### Applications to Integrate (11 total):

1. **AuthApplication** - `services/auth/service/src/main/java/com/aspire/asat/auth/AuthApplication.java`
2. **BillingApplication** - `services/billing/service/src/main/java/com/aspire/asat/billing/BillingApplication.java`
3. **CmsApplication** - `services/cms/service/src/main/java/com/aspire/asat/cms/CmsApplication.java`
4. **CourseApplication** - `services/course/service/src/main/java/com/aspire/course/CourseApplication.java`
5. **GatewayApplication** - `services/gateway/service/src/main/java/com/aspire/asat/gateway/GatewayApplication.java`
6. **NotificationApplication** - `services/notification/service/src/main/java/com/aspire/asat/notification/NotificationApplication.java`
7. **PhishingApplication** - `services/phishing/service/src/main/java/com/aspire/asat/phishing/PhishingApplication.java`
8. **RegistrationApplication** - `services/registration/service/src/main/java/com/aspire/asat/registration/RegistrationApplication.java`
9. **SesameApplication** - `services/sesame/service/src/main/java/com/aspire/sesame/SesameApplication.java`
10. **UniversalApplication** - `services/universal/service/src/main/java/com/aspire/asat/universal/UniversalApplication.java`
11. **VpsApplication** - `services/vps/service/src/main/java/com/aspire/asat/vps/VpsApplication.java`

### 3.1 Update Each Application's Main Method

**Pattern for each application:**

```java
public static void main(String[] args) {
    // Create SpringApplication instance
    SpringApplication app = new SpringApplication(ApplicationName.class);
    
    // Create ConfigurableApplicationContext
    ConfigurableApplicationContext context = app.run(args);
    
    // Initialize SSM Parameters after context is created but before beans are fully initialized
    SsmParameterInitializer.initialize(context);
    
    // Continue with normal startup
}
```

**OR** (Better approach - initialize before context creation):

```java
public static void main(String[] args) {
    // Create SpringApplication instance
    SpringApplication app = new SpringApplication(ApplicationName.class);
    
    // Set initializers to load SSM parameters before context creation
    app.addInitializers(new SsmParameterInitializer());
    
    // Run application
    app.run(args);
}
```

### 3.2 Update Each Service's build.gradle

**For each service module** (`services/{service-name}/service/build.gradle`):

Add dependency on common/core (if not already present):
```gradle
implementation project(':common:core')
```

---

## Phase 4: SSM Parameter Store Structure

### 4.1 Parameter Path Convention

Parameters should be stored in SSM Parameter Store with the following path structure:

```
/asat/{profile}/{service-name}/{parameter-name}
```

**Examples:**
- `/asat/dev/auth-service/database-password`
- `/asat/prod/registration-service/jwt-secret`
- `/asat/uat/billing-service/api-key`

### 4.2 Service Name Mapping

| Application Class | Service Name | Parameter Path Prefix |
|------------------|--------------|----------------------|
| AuthApplication | `auth-service` | `/asat/{profile}/auth-service/` |
| BillingApplication | `billing-service` | `/asat/{profile}/billing-service/` |
| CmsApplication | `cms-service` | `/asat/{profile}/cms-service/` |
| CourseApplication | `course-service` | `/asat/{profile}/course-service/` |
| GatewayApplication | `gateway-service` | `/asat/{profile}/gateway-service/` |
| NotificationApplication | `notification-service` | `/asat/{profile}/notification-service/` |
| PhishingApplication | `phishing-service` | `/asat/{profile}/phishing-service/` |
| RegistrationApplication | `registration-service` | `/asat/{profile}/registration-service/` |
| SesameApplication | `sesame-service` | `/asat/{profile}/sesame-service/` |
| UniversalApplication | `universal-service` | `/asat/{profile}/universal-service/` |
| VpsApplication | `vps-service` | `/asat/{profile}/vps-service/` |

---

## Phase 5: Implementation Details

### 5.1 SSM Parameter Initializer Flow

1. **Get Active Profile** from ApplicationContext
2. **Determine Service Name** from application class or configuration
3. **Build Parameter Path** prefix: `/asat/{profile}/{service-name}/`
4. **Fetch All Parameters** under that path from SSM
5. **Decrypt SecureString Parameters** using KMS
6. **Add to Spring Environment** as properties
7. **Properties are available** via `@Value` or `@ConfigurationProperties`

### 5.2 Error Handling

- If SSM is unavailable, log warning and continue (for local development)
- If KMS decryption fails, log error and fail startup
- If profile is not set, use default profile or fail gracefully

### 5.3 Local Development Support

- Support for local profile that may skip SSM/KMS
- Fallback to application.yml properties if SSM unavailable
- Environment variable override support

---

## Phase 6: Configuration Properties

### 6.1 Application Properties

Add to each service's `application.yml`:

```yaml
aws:
  ssm:
    enabled: ${AWS_SSM_ENABLED:true}
    parameter-path-prefix: /asat
    kms-key-id: ${AWS_KMS_KEY_ID:}
  region: ${AWS_REGION:us-east-1}
```

### 6.2 Environment Variables

- `AWS_SSM_ENABLED` - Enable/disable SSM integration (default: true)
- `AWS_KMS_KEY_ID` - KMS Key ID for decryption
- `AWS_REGION` - AWS region for SSM/KMS
- `AWS_ACCESS_KEY_ID` - AWS access key (if not using IAM role)
- `AWS_SECRET_ACCESS_KEY` - AWS secret key (if not using IAM role)

---

## Phase 7: Testing Strategy

### 7.1 Unit Tests
- Test SSM parameter fetching
- Test KMS decryption
- Test parameter path resolution
- Test profile-based parameter loading

### 7.2 Integration Tests
- Test with mock SSM responses
- Test with actual AWS credentials (in test environment)
- Test parameter loading into Spring context

### 7.3 Local Testing
- Test with local profile (skip SSM)
- Test with mock SSM client
- Test fallback mechanisms

---

## Phase 8: Implementation Order

### Step 1: Common Infrastructure (Week 1)
1. ✅ Add AWS SSM and KMS SDK dependencies to root build.gradle
2. ✅ Create SsmParameterInitializer class
3. ✅ Create KmsService class
4. ✅ Create SsmService class
5. ✅ Create AwsSsmConfig class
6. ✅ Update common/core build.gradle
7. ✅ Write unit tests for common components

### Step 2: Integration - Core Services (Week 2)
1. ✅ AuthApplication
2. ✅ RegistrationApplication
3. ✅ GatewayApplication
4. ✅ NotificationApplication

### Step 3: Integration - Business Services (Week 3)
1. ✅ BillingApplication
2. ✅ CmsApplication
3. ✅ UniversalApplication
4. ✅ PhishingApplication

### Step 4: Integration - Remaining Services (Week 4)
1. ✅ VpsApplication
2. ✅ SesameApplication
3. ✅ CourseApplication

### Step 5: Documentation & Testing (Week 5)
1. ✅ Update README with SSM/KMS setup instructions
2. ✅ Create parameter store setup guide
3. ✅ Integration testing across all services
4. ✅ Performance testing

---

## Phase 9: Migration Strategy

### 9.1 Gradual Migration
- Start with non-critical services
- Test thoroughly before migrating production services
- Keep application.yml as fallback during migration

### 9.2 Parameter Migration Checklist
- [ ] Identify all sensitive properties in application.yml files
- [ ] Create corresponding SSM parameters for each environment
- [ ] Test parameter loading in dev environment
- [ ] Verify decryption works correctly
- [ ] Update application.yml to remove sensitive values (keep placeholders)
- [ ] Deploy to UAT and verify
- [ ] Deploy to production

---

## Phase 10: Security Considerations

### 10.1 IAM Roles
- Use IAM roles for EC2/ECS instances instead of access keys
- Grant minimum required permissions:
  - `ssm:GetParameter`
  - `ssm:GetParameters`
  - `ssm:GetParametersByPath`
  - `kms:Decrypt`

### 10.2 Parameter Store Best Practices
- Use SecureString type for sensitive values
- Enable parameter encryption at rest
- Use KMS customer-managed keys
- Implement parameter versioning
- Regular rotation of KMS keys

### 10.3 Access Control
- Restrict SSM parameter access by IAM policies
- Use parameter path-based access control
- Implement least privilege principle

---

## Phase 11: Monitoring & Logging

### 11.1 Logging
- Log parameter fetch operations (without sensitive values)
- Log KMS decryption operations
- Log errors and warnings
- Use structured logging

### 11.2 Metrics
- Track SSM parameter fetch latency
- Track KMS decryption latency
- Monitor parameter fetch failures
- Alert on decryption failures

---

## Appendix A: Example Implementation

### Example: SSM Parameter Initializer

```java
public class SsmParameterInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        String activeProfile = getActiveProfile(applicationContext);
        String serviceName = getServiceName(applicationContext);
        String parameterPath = String.format("/asat/%s/%s/", activeProfile, serviceName);
        
        Map<String, String> parameters = fetchParameters(parameterPath);
        decryptParameters(parameters);
        
        addToEnvironment(applicationContext, parameters);
    }
}
```

### Example: Usage in Application

```java
@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AuthApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}
```

---

## Appendix B: Parameter Store Setup Script

### AWS CLI Commands Example

```bash
# Create SecureString parameter
aws ssm put-parameter \
  --name "/asat/dev/auth-service/database-password" \
  --value "my-secret-password" \
  --type "SecureString" \
  --key-id "alias/asat-kms-key" \
  --region us-east-1

# Get parameter (for testing)
aws ssm get-parameter \
  --name "/asat/dev/auth-service/database-password" \
  --with-decryption \
  --region us-east-1
```

---

## Questions & Considerations

1. **KMS Key Management**: Should we use a single KMS key for all services or separate keys per service?
2. **Parameter Caching**: Should we cache parameters or fetch on every startup?
3. **Parameter Refresh**: Do we need runtime parameter refresh capability?
4. **Multi-Region**: How to handle multi-region deployments?
5. **Cost Optimization**: Consider SSM Parameter Store costs and KMS API call costs

---

## Next Steps

1. Review and approve this plan
2. Set up AWS SSM Parameter Store structure
3. Create KMS keys
4. Begin Phase 1 implementation
5. Test with one service first (recommend AuthApplication or GatewayApplication)

