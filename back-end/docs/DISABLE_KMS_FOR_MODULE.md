# How to Disable AWS KMS for a Specific Module

## Overview
The AWS KMS integration is enabled by default for all modules. However, you can disable KMS decryption for specific modules if they don't require encrypted parameter decryption.

## Method 1: Using application.yml (Recommended)

To disable KMS for a specific module, add the following configuration to that module's `application.yml` file:

### Location
`services/{service-name}/service/src/main/resources/application.yml`

### Configuration

```yaml
aws:
  ssm:
    enabled: ${AWS_SSM_ENABLED:true}
    kms-enabled: false  # Disable KMS for this module
    parameter-path-prefix: ${AWS_SSM_PARAMETER_PATH_PREFIX:/asat}
    kms-key-id: ${AWS_KMS_KEY_ID:}
    region: ${AWS_REGION:}
    fail-on-error: ${AWS_SSM_FAIL_ON_ERROR:false}
  region: ${AWS_REGION:us-east-1}
```

### Example: Disable KMS for Gateway Service

Edit `services/gateway/service/src/main/resources/application.yml`:

```yaml
# AWS SSM Parameter Store Configuration
aws:
  ssm:
    enabled: ${AWS_SSM_ENABLED:true}
    kms-enabled: false  # Gateway service doesn't need KMS decryption
    parameter-path-prefix: ${AWS_SSM_PARAMETER_PATH_PREFIX:/asat}
    kms-key-id: ${AWS_KMS_KEY_ID:}
    region: ${AWS_REGION:}
    fail-on-error: ${AWS_SSM_FAIL_ON_ERROR:false}
  region: ${AWS_REGION:us-east-1}
```

## Method 2: Using Environment Variables

You can also disable KMS using environment variables:

```bash
export AWS_SSM_KMS_ENABLED=false
```

Or in your deployment configuration (Kubernetes, Docker, etc.):

```yaml
env:
  - name: AWS_SSM_KMS_ENABLED
    value: "false"
```

## Method 3: Using Profile-Specific Configuration

For environment-specific disabling, create a profile-specific configuration file:

### Example: Disable KMS for Local Profile Only

Create `services/{service-name}/service/src/main/resources/application-local.yml`:

```yaml
aws:
  ssm:
    kms-enabled: false  # Disable KMS for local development
```

This way, KMS will be disabled only when running with `local` profile, but enabled for other environments (dev, uat, prod).

## Behavior When KMS is Disabled

When `kms-enabled: false`:

1. **SSM Parameters are Still Fetched**: The module will still fetch parameters from AWS SSM Parameter Store
2. **No Decryption**: SecureString parameters will NOT be decrypted using KMS
3. **Raw Values**: Parameters will be used as-is from SSM (SSM automatically decrypts SecureString parameters when fetched with `withDecryption: true`, but this uses SSM's default encryption key, not KMS)
4. **No KMS Client**: The KMS client will not be initialized, reducing resource usage
5. **Logging**: The application will log: `"KMS is disabled, returning value as-is"`

## Use Cases for Disabling KMS

Consider disabling KMS for a module if:

1. **Non-Sensitive Parameters**: The module only uses non-sensitive configuration values
2. **Performance**: You want to reduce startup time and avoid KMS API calls
3. **Cost Optimization**: You want to reduce KMS API call costs
4. **Local Development**: You're running locally without KMS access
5. **Testing**: You're running integration tests without KMS setup

## Complete Example: Disable KMS for Sesame Service

### Step 1: Edit application.yml

File: `services/sesame/service/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: sesame-service
  profiles:
    active: dev
  main:
    allow-bean-definition-overriding: true

server:
  port: 9010

# AWS SSM Parameter Store Configuration
aws:
  ssm:
    enabled: ${AWS_SSM_ENABLED:true}
    kms-enabled: false  # Sesame service doesn't need KMS decryption
    parameter-path-prefix: ${AWS_SSM_PARAMETER_PATH_PREFIX:/asat}
    kms-key-id: ${AWS_KMS_KEY_ID:}
    region: ${AWS_REGION:}
    fail-on-error: ${AWS_SSM_FAIL_ON_ERROR:false}
  region: ${AWS_REGION:us-east-1}
```

### Step 2: Verify Configuration

After making the change, restart the application. You should see in the logs:

```
INFO  - KMS service is disabled - decryption will be skipped
INFO  - KMS decryption is disabled for this module
```

## Important Notes

1. **SSM Still Required**: Even with KMS disabled, SSM Parameter Store is still used to fetch parameters
2. **Parameter Types**: If you disable KMS, make sure your SSM parameters are stored as `String` type, not `SecureString` with KMS encryption
3. **Security**: Only disable KMS if the parameters don't contain sensitive information
4. **Default Behavior**: By default, `kms-enabled` is `true` for all modules
5. **Per-Module Configuration**: Each module can independently enable/disable KMS

## Troubleshooting

### Issue: Module still trying to use KMS after disabling

**Solution**: 
- Check that `kms-enabled: false` is correctly set in `application.yml`
- Verify there are no environment variables overriding the setting
- Check profile-specific configuration files (`application-{profile}.yml`)

### Issue: Parameters not loading after disabling KMS

**Solution**:
- Ensure SSM is still enabled: `aws.ssm.enabled: true`
- Check that parameters exist in SSM Parameter Store
- Verify AWS credentials and region configuration

## Related Configuration

- **Enable/Disable SSM Entirely**: Set `aws.ssm.enabled: false` to completely disable SSM parameter loading
- **Fail on Error**: Set `aws.ssm.fail-on-error: true` to fail startup if SSM/KMS is unavailable
- **Parameter Path**: Customize `aws.ssm.parameter-path-prefix` if using a different path structure

