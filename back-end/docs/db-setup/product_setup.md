# Security Awareness Training Product Setup Payloads

This file is a copy-paste guide for Swagger.

Use direct CMS Swagger in local environment:

```text
http://localhost:5050/cms/swagger-ui.html
```

For direct local CMS calls, no token is required in the current code. If you call through Gateway, a Bearer token may be required depending on Gateway JWT settings.

## Local Service Setup

Start MongoDB:

```powershell
docker run -d --name asat-local-mongo -p 27017:27017 mongo:7
```

If the container already exists:

```powershell
docker start asat-local-mongo
```

Run CMS:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
$env:MONGODB_URI="mongodb://localhost:27017"
.\gradlew.bat :services:cms:service:bootRun
```

## API Order

Create data in this order:

1. Create features: `POST /cms/api/v1/features`
2. Create user ranges: `POST /cms/api/v1/user-ranges`
3. Create product with packages: `POST /cms/api/v1/products`

Important: after creating each feature and range, copy the response `data.id`. Use those IDs in the final product payload.

## 1. Create Free Trial Features

Endpoint:

```text
POST /cms/api/v1/features
```

### Free Trial Feature 1

```json
{
  "featureName": "Ticketing and Support",
  "featureDescription": "Ticketing and support access",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "FREE",
  "thumbnailUrl": ""
}
```

### Free Trial Feature 2

```json
{
  "featureName": "IT Security Policies",
  "featureDescription": "Access to IT security policy resources",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "FREE",
  "thumbnailUrl": ""
}
```

### Free Trial Feature 3

```json
{
  "featureName": "Usage Limits (5 Users)",
  "featureDescription": "Free trial usage limited to 5 users",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "FREE",
  "thumbnailUrl": ""
}
```

### Free Trial Feature 4

```json
{
  "featureName": "Basic Support (FAQs-email)",
  "featureDescription": "Basic support through FAQs and email",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "FREE",
  "thumbnailUrl": ""
}
```

### Free Trial Feature 5

```json
{
  "featureName": "Basic Customization Options",
  "featureDescription": "Basic customization options for trial users",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "FREE",
  "thumbnailUrl": ""
}
```

### Free Trial Feature 6

```json
{
  "featureName": "Bulk User Provisioning",
  "featureDescription": "Bulk user provisioning support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "FREE",
  "thumbnailUrl": ""
}
```

### Free Trial Feature 7

```json
{
  "featureName": "Time-Limited Access (30 days)",
  "featureDescription": "30-day free trial access",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "FREE",
  "thumbnailUrl": ""
}
```

### Free Trial Feature 8

```json
{
  "featureName": "Certification",
  "featureDescription": "Certification access for completed training",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "FREE",
  "thumbnailUrl": ""
}
```

## 2. Create Silver Features

Endpoint:

```text
POST /cms/api/v1/features
```

### Silver Feature 1

```json
{
  "featureName": "100+ Content Library",
  "featureDescription": "Access to 100+ security awareness training content items",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Silver Feature 2

```json
{
  "featureName": "Gamified Learning",
  "featureDescription": "Gamified learning experience for training engagement",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Silver Feature 3

```json
{
  "featureName": "Autonomous Campaigns",
  "featureDescription": "Autonomous campaign support for security awareness training",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Silver Feature 4

```json
{
  "featureName": "Culture Analytics",
  "featureDescription": "Security culture analytics and reporting",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Silver Feature 5

```json
{
  "featureName": "API-Integrations",
  "featureDescription": "API integration support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Silver Feature 6

```json
{
  "featureName": "Custom Branding",
  "featureDescription": "Custom branding support for the organization",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Silver Feature 7

```json
{
  "featureName": "Elite Support",
  "featureDescription": "Elite support access",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Silver Feature 8

```json
{
  "featureName": "AD Integrations",
  "featureDescription": "Active Directory integration support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

## 3. Create Gold Features

Endpoint:

```text
POST /cms/api/v1/features
```

### Gold Feature 1

```json
{
  "featureName": "AI-Recommended Training",
  "featureDescription": "AI-recommended training paths and content",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Gold Feature 2

```json
{
  "featureName": "150+ Content Library",
  "featureDescription": "Access to 150+ security awareness training content items",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Gold Feature 3

```json
{
  "featureName": "Episodes Gamification",
  "featureDescription": "Episode-based gamification for training",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Gold Feature 4

```json
{
  "featureName": "Everything In Silver",
  "featureDescription": "Includes all Silver package features",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Gold Feature 5

```json
{
  "featureName": "Ticketing and 8x5 Support",
  "featureDescription": "Ticketing support available 8x5",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Gold Feature 6

```json
{
  "featureName": "AI Personalization",
  "featureDescription": "AI personalization for training content and recommendations",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Gold Feature 7

```json
{
  "featureName": "USB Baiting Simulations",
  "featureDescription": "USB baiting simulation support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Gold Feature 8

```json
{
  "featureName": "AD/SSO Integration",
  "featureDescription": "Active Directory and SSO integration support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

## 4. Create Platinum Features

Endpoint:

```text
POST /cms/api/v1/features
```

### Platinum Feature 1

```json
{
  "featureName": "AD/SSO/SAML Integration",
  "featureDescription": "Active Directory, SSO, and SAML integration support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Platinum Feature 2

```json
{
  "featureName": "Ticketing and 12x7 Support",
  "featureDescription": "Ticketing support available 12x7",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Platinum Feature 3

```json
{
  "featureName": "Custom Course Creation",
  "featureDescription": "Custom course creation support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Platinum Feature 4

```json
{
  "featureName": "Everything In Gold",
  "featureDescription": "Includes all Gold package features",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Platinum Feature 5

```json
{
  "featureName": "200+ Content Library",
  "featureDescription": "Access to 200+ security awareness training content items",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Platinum Feature 6

```json
{
  "featureName": "Unlimited Phishing Security Tests",
  "featureDescription": "Unlimited phishing security test support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Platinum Feature 7

```json
{
  "featureName": "Role-Based Training",
  "featureDescription": "Role-based training paths and assignments",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

## 5. Create Diamond Features

Endpoint:

```text
POST /cms/api/v1/features
```

### Diamond Feature 1

```json
{
  "featureName": "KillPhish AI",
  "featureDescription": "KillPhish AI capability",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Diamond Feature 2

```json
{
  "featureName": "Phish Alert Button",
  "featureDescription": "Phish alert button support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Diamond Feature 3

```json
{
  "featureName": "Smishing Simulations",
  "featureDescription": "Smishing simulation support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Diamond Feature 4

```json
{
  "featureName": "Callback Phishing",
  "featureDescription": "Callback phishing simulation support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Diamond Feature 5

```json
{
  "featureName": "Ticketing and 24x5 Support",
  "featureDescription": "Ticketing support available 24x5",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Diamond Feature 6

```json
{
  "featureName": "300+ Content Library",
  "featureDescription": "Access to 300+ security awareness training content items",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Diamond Feature 7

```json
{
  "featureName": "Everything In Platinum",
  "featureDescription": "Includes all Platinum package features",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

### Diamond Feature 8

```json
{
  "featureName": "Vishing Simulations",
  "featureDescription": "Vishing simulation support",
  "featureStatus": "ENABLED",
  "packageIds": [],
  "availability": "PREMIUM",
  "thumbnailUrl": ""
}
```

## 6. Create User Ranges

Endpoint:

```text
POST /cms/api/v1/user-ranges
```

After creating each range, copy the response `data.id`. Use those IDs in the final product payload.

### User Range 1

```json
{
  "rangeName": "15-24 users",
  "minUsers": 15,
  "maxUsers": 24,
  "description": "15 to 24 users",
  "isDefault": false
}
```

### User Range 2

```json
{
  "rangeName": "25-50 users",
  "minUsers": 25,
  "maxUsers": 50,
  "description": "25 to 50 users",
  "isDefault": false
}
```

### User Range 3

```json
{
  "rangeName": "51-99 users",
  "minUsers": 51,
  "maxUsers": 99,
  "description": "51 to 99 users",
  "isDefault": false
}
```

### User Range 4

```json
{
  "rangeName": "100-499 users",
  "minUsers": 100,
  "maxUsers": 499,
  "description": "100 to 499 users",
  "isDefault": false
}
```

### User Range 5

```json
{
  "rangeName": "500-999 users",
  "minUsers": 500,
  "maxUsers": 999,
  "description": "500 to 999 users",
  "isDefault": false
}
```

### User Range 6

```json
{
  "rangeName": "1000-2999 users",
  "minUsers": 1000,
  "maxUsers": 2999,
  "description": "1000 to 2999 users",
  "isDefault": false
}
```

### User Range 7

```json
{
  "rangeName": "3000-4999 users",
  "minUsers": 3000,
  "maxUsers": 4999,
  "description": "3000 to 4999 users",
  "isDefault": false
}
```

### User Range 8

```json
{
  "rangeName": "5000-9999 users",
  "minUsers": 5000,
  "maxUsers": 9999,
  "description": "5000 to 9999 users",
  "isDefault": false
}
```

### User Range 9

```json
{
  "rangeName": "10000+ users",
  "minUsers": 10000,
  "maxUsers": null,
  "description": "10000 or more users",
  "isDefault": false
}
```

## 7. Create Product With Packages

Endpoint:

```text
POST /cms/api/v1/products
```

Before pasting this payload, replace all placeholder IDs:

- `FREE_TRIAL_TICKETING_AND_SUPPORT_ID`
- `SILVER_100_CONTENT_LIBRARY_ID`
- `RANGE_15_24_ID`
- etc.

You get these IDs from the `data.id` field returned by the feature and user-range create APIs.

```json
{
  "productName": "Security Awareness Training",
  "productDescription": "Security Awareness Training product",
  "productStatus": "ENABLED",
  "thumbnailUrl": "",
  "tags": ["Security", "Training"],
  "isTrial": false,
  "showInSite": true,
  "displayOrder": 1,
  "packages": [
    {
      "packageName": "FREE TRIAL",
      "productId": "TEMP",
      "features": [
        { "id": "FREE_TRIAL_TICKETING_AND_SUPPORT_ID", "name": "Ticketing and Support" },
        { "id": "FREE_TRIAL_IT_SECURITY_POLICIES_ID", "name": "IT Security Policies" },
        { "id": "FREE_TRIAL_USAGE_LIMITS_ID", "name": "Usage Limits (5 Users)" },
        { "id": "FREE_TRIAL_BASIC_SUPPORT_ID", "name": "Basic Support (FAQs-email)" },
        { "id": "FREE_TRIAL_BASIC_CUSTOMIZATION_ID", "name": "Basic Customization Options" },
        { "id": "FREE_TRIAL_BULK_USER_PROVISIONING_ID", "name": "Bulk User Provisioning" },
        { "id": "FREE_TRIAL_TIME_LIMITED_ACCESS_ID", "name": "Time-Limited Access (30 days)" },
        { "id": "FREE_TRIAL_CERTIFICATION_ID", "name": "Certification" }
      ],
      "price": 0,
      "yearlyPrice": 0,
      "packageStatus": "ENABLED",
      "basePackageId": null,
      "isTrial": true,
      "showInSite": true,
      "isPriceRange": false
    },
    {
      "packageName": "Silver",
      "productId": "TEMP",
      "features": [
        { "id": "SILVER_100_CONTENT_LIBRARY_ID", "name": "100+ Content Library" },
        { "id": "SILVER_GAMIFIED_LEARNING_ID", "name": "Gamified Learning" },
        { "id": "SILVER_AUTONOMOUS_CAMPAIGNS_ID", "name": "Autonomous Campaigns" },
        { "id": "SILVER_CULTURE_ANALYTICS_ID", "name": "Culture Analytics" },
        { "id": "SILVER_API_INTEGRATIONS_ID", "name": "API-Integrations" },
        { "id": "SILVER_CUSTOM_BRANDING_ID", "name": "Custom Branding" },
        { "id": "SILVER_ELITE_SUPPORT_ID", "name": "Elite Support" },
        { "id": "SILVER_AD_INTEGRATIONS_ID", "name": "AD Integrations" }
      ],
      "price": 1.5,
      "yearlyPrice": 18,
      "packageStatus": "ENABLED",
      "basePackageId": null,
      "isTrial": false,
      "showInSite": true,
      "isPriceRange": true,
      "rangePricing": [
        { "userRangeId": "RANGE_15_24_ID", "pricePerUser": 1.5, "yearlyPricePerUser": 18 },
        { "userRangeId": "RANGE_25_50_ID", "pricePerUser": 1.5, "yearlyPricePerUser": 18 },
        { "userRangeId": "RANGE_51_99_ID", "pricePerUser": 1.5, "yearlyPricePerUser": 18 },
        { "userRangeId": "RANGE_100_499_ID", "pricePerUser": 1.5, "yearlyPricePerUser": 18 },
        { "userRangeId": "RANGE_500_999_ID", "pricePerUser": 1.5, "yearlyPricePerUser": 18 },
        { "userRangeId": "RANGE_1000_2999_ID", "pricePerUser": 1.5, "yearlyPricePerUser": 18 },
        { "userRangeId": "RANGE_3000_4999_ID", "pricePerUser": 1.5, "yearlyPricePerUser": 18 },
        { "userRangeId": "RANGE_5000_9999_ID", "pricePerUser": 1.5, "yearlyPricePerUser": 18 },
        { "userRangeId": "RANGE_10000_PLUS_ID", "pricePerUser": 1.5, "yearlyPricePerUser": 18 }
      ]
    },
    {
      "packageName": "Gold",
      "productId": "TEMP",
      "features": [
        { "id": "GOLD_AI_RECOMMENDED_TRAINING_ID", "name": "AI-Recommended Training" },
        { "id": "GOLD_150_CONTENT_LIBRARY_ID", "name": "150+ Content Library" },
        { "id": "GOLD_EPISODES_GAMIFICATION_ID", "name": "Episodes Gamification" },
        { "id": "GOLD_EVERYTHING_IN_SILVER_ID", "name": "Everything In Silver" },
        { "id": "GOLD_TICKETING_8X5_SUPPORT_ID", "name": "Ticketing and 8x5 Support" },
        { "id": "GOLD_AI_PERSONALIZATION_ID", "name": "AI Personalization" },
        { "id": "GOLD_USB_BAITING_SIMULATIONS_ID", "name": "USB Baiting Simulations" },
        { "id": "GOLD_AD_SSO_INTEGRATION_ID", "name": "AD/SSO Integration" }
      ],
      "price": 2,
      "yearlyPrice": 24,
      "packageStatus": "ENABLED",
      "basePackageId": null,
      "isTrial": false,
      "showInSite": true,
      "isPriceRange": true,
      "rangePricing": [
        { "userRangeId": "RANGE_15_24_ID", "pricePerUser": 2, "yearlyPricePerUser": 24 },
        { "userRangeId": "RANGE_25_50_ID", "pricePerUser": 2, "yearlyPricePerUser": 24 },
        { "userRangeId": "RANGE_51_99_ID", "pricePerUser": 2, "yearlyPricePerUser": 24 },
        { "userRangeId": "RANGE_100_499_ID", "pricePerUser": 2, "yearlyPricePerUser": 24 },
        { "userRangeId": "RANGE_500_999_ID", "pricePerUser": 2, "yearlyPricePerUser": 24 },
        { "userRangeId": "RANGE_1000_2999_ID", "pricePerUser": 2, "yearlyPricePerUser": 24 },
        { "userRangeId": "RANGE_3000_4999_ID", "pricePerUser": 2, "yearlyPricePerUser": 24 },
        { "userRangeId": "RANGE_5000_9999_ID", "pricePerUser": 2, "yearlyPricePerUser": 24 },
        { "userRangeId": "RANGE_10000_PLUS_ID", "pricePerUser": 2, "yearlyPricePerUser": 24 }
      ]
    },
    {
      "packageName": "Platinum",
      "productId": "TEMP",
      "features": [
        { "id": "PLATINUM_AD_SSO_SAML_ID", "name": "AD/SSO/SAML Integration" },
        { "id": "PLATINUM_TICKETING_12X7_SUPPORT_ID", "name": "Ticketing and 12x7 Support" },
        { "id": "PLATINUM_CUSTOM_COURSE_CREATION_ID", "name": "Custom Course Creation" },
        { "id": "PLATINUM_EVERYTHING_IN_GOLD_ID", "name": "Everything In Gold" },
        { "id": "PLATINUM_200_CONTENT_LIBRARY_ID", "name": "200+ Content Library" },
        { "id": "PLATINUM_UNLIMITED_PHISHING_SECURITY_TESTS_ID", "name": "Unlimited Phishing Security Tests" },
        { "id": "PLATINUM_ROLE_BASED_TRAINING_ID", "name": "Role-Based Training" }
      ],
      "price": 2.25,
      "yearlyPrice": 27,
      "packageStatus": "ENABLED",
      "basePackageId": null,
      "isTrial": false,
      "showInSite": true,
      "isPriceRange": true,
      "rangePricing": [
        { "userRangeId": "RANGE_15_24_ID", "pricePerUser": 2.25, "yearlyPricePerUser": 27 },
        { "userRangeId": "RANGE_25_50_ID", "pricePerUser": 2.25, "yearlyPricePerUser": 27 },
        { "userRangeId": "RANGE_51_99_ID", "pricePerUser": 2.25, "yearlyPricePerUser": 27 },
        { "userRangeId": "RANGE_100_499_ID", "pricePerUser": 2.25, "yearlyPricePerUser": 27 },
        { "userRangeId": "RANGE_500_999_ID", "pricePerUser": 2.25, "yearlyPricePerUser": 27 },
        { "userRangeId": "RANGE_1000_2999_ID", "pricePerUser": 2.25, "yearlyPricePerUser": 27 },
        { "userRangeId": "RANGE_3000_4999_ID", "pricePerUser": 2.25, "yearlyPricePerUser": 27 },
        { "userRangeId": "RANGE_5000_9999_ID", "pricePerUser": 2.25, "yearlyPricePerUser": 27 },
        { "userRangeId": "RANGE_10000_PLUS_ID", "pricePerUser": 2.25, "yearlyPricePerUser": 27 }
      ]
    },
    {
      "packageName": "Diamond",
      "productId": "TEMP",
      "features": [
        { "id": "DIAMOND_KILLPHISH_AI_ID", "name": "KillPhish AI" },
        { "id": "DIAMOND_PHISH_ALERT_BUTTON_ID", "name": "Phish Alert Button" },
        { "id": "DIAMOND_SMISHING_SIMULATIONS_ID", "name": "Smishing Simulations" },
        { "id": "DIAMOND_CALLBACK_PHISHING_ID", "name": "Callback Phishing" },
        { "id": "DIAMOND_TICKETING_24X5_SUPPORT_ID", "name": "Ticketing and 24x5 Support" },
        { "id": "DIAMOND_300_CONTENT_LIBRARY_ID", "name": "300+ Content Library" },
        { "id": "DIAMOND_EVERYTHING_IN_PLATINUM_ID", "name": "Everything In Platinum" },
        { "id": "DIAMOND_VISHING_SIMULATIONS_ID", "name": "Vishing Simulations" }
      ],
      "price": 2.92,
      "yearlyPrice": 35.04,
      "packageStatus": "ENABLED",
      "basePackageId": null,
      "isTrial": false,
      "showInSite": true,
      "isPriceRange": true,
      "rangePricing": [
        { "userRangeId": "RANGE_15_24_ID", "pricePerUser": 2.92, "yearlyPricePerUser": 35.04 },
        { "userRangeId": "RANGE_25_50_ID", "pricePerUser": 2.92, "yearlyPricePerUser": 35.04 },
        { "userRangeId": "RANGE_51_99_ID", "pricePerUser": 2.92, "yearlyPricePerUser": 35.04 },
        { "userRangeId": "RANGE_100_499_ID", "pricePerUser": 2.92, "yearlyPricePerUser": 35.04 },
        { "userRangeId": "RANGE_500_999_ID", "pricePerUser": 2.92, "yearlyPricePerUser": 35.04 },
        { "userRangeId": "RANGE_1000_2999_ID", "pricePerUser": 2.92, "yearlyPricePerUser": 35.04 },
        { "userRangeId": "RANGE_3000_4999_ID", "pricePerUser": 2.92, "yearlyPricePerUser": 35.04 },
        { "userRangeId": "RANGE_5000_9999_ID", "pricePerUser": 2.92, "yearlyPricePerUser": 35.04 },
        { "userRangeId": "RANGE_10000_PLUS_ID", "pricePerUser": 2.92, "yearlyPricePerUser": 35.04 }
      ]
    }
  ]
}
```

## 7C. Ready Product Payload From Current Local DB

Endpoint:

```text
POST /cms/api/v1/products
```

Use this payload with the feature IDs and user range IDs currently created in your local DB.

Important:

```text
productId: "TEMP"
```

This is only a placeholder. The backend creates the real product ID and replaces `TEMP` on each package.

```json
{
  "productName": "Security Awareness Training",
  "productDescription": "Security Awareness Training product",
  "productStatus": "ENABLED",
  "thumbnailUrl": "",
  "tags": ["Security", "Training"],
  "isTrial": false,
  "showInSite": true,
  "displayOrder": 1,
  "packages": [
    {
      "packageName": "FREE TRIAL",
      "productId": "TEMP",
      "features": [
        {
          "id": "0d36e416-af11-41c5-a182-a8a0bb67c18f",
          "name": "Ticketing and Support"
        },
        {
          "id": "b8aeb244-1951-4a1c-b670-a12e017d63de",
          "name": "IT Security Policies"
        },
        {
          "id": "c88f3b2c-7e7f-4441-9f9e-2d615977cb91",
          "name": "Usage Limits (5 Users)"
        },
        {
          "id": "a4c16f78-2496-40cb-b255-d0dabb834c4e",
          "name": "Basic Support (FAQs-email)"
        },
        {
          "id": "be49aa6d-3c72-4e81-8012-69b600717a58",
          "name": "Basic Customization Options"
        },
        {
          "id": "21c0ccda-1324-44b0-9a3e-a243ef3ece15",
          "name": "Bulk User Provisioning"
        },
        {
          "id": "773e5f65-d210-44bd-b455-822fd399a8b9",
          "name": "Time-Limited Access (30 days)"
        },
        {
          "id": "0e05eeba-5508-44c5-bf7c-468ad26b0a33",
          "name": "Certification"
        }
      ],
      "price": 0,
      "yearlyPrice": 0,
      "packageStatus": "ENABLED",
      "basePackageId": null,
      "isTrial": true,
      "showInSite": true,
      "isPriceRange": false
    },
    {
      "packageName": "Silver",
      "productId": "TEMP",
      "features": [
        {
          "id": "0dc13140-9f3a-4672-9cf8-a6b915d7aebc",
          "name": "100+ Content Library"
        },
        {
          "id": "443600dc-ed60-498f-be0b-54f80a465b3e",
          "name": "Gamified Learning"
        },
        {
          "id": "a3a7412c-5b99-4cb2-b673-27490a3afd1f",
          "name": "Autonomous Campaigns"
        },
        {
          "id": "c18b9cac-d79d-4847-80eb-191b024f7b61",
          "name": "Culture Analytics"
        },
        {
          "id": "73f928d1-f134-43be-ab2a-334a133e5c98",
          "name": "API-Integrations"
        },
        {
          "id": "86051a29-fbf6-4921-ad14-f9b091713143",
          "name": "Custom Branding"
        },
        {
          "id": "e2bf0cff-b315-4588-b538-f6312e422369",
          "name": "Elite Support"
        },
        {
          "id": "640bf6cf-78b8-4b43-8d1c-2902afe6ec3d",
          "name": "AD Integrations"
        }
      ],
      "price": 1.5,
      "yearlyPrice": 18,
      "packageStatus": "ENABLED",
      "basePackageId": null,
      "isTrial": false,
      "showInSite": true,
      "isPriceRange": true,
      "rangePricing": [
        {
          "userRangeId": "0a44942e-b40a-4f56-bb7d-fc9286700996",
          "pricePerUser": 1.5,
          "yearlyPricePerUser": 18
        },
        {
          "userRangeId": "ce10f94a-40cc-4a63-987b-c079f7069be9",
          "pricePerUser": 1.5,
          "yearlyPricePerUser": 18
        },
        {
          "userRangeId": "8c0714f5-e395-4eb2-90a7-8e8ae8b2a593",
          "pricePerUser": 1.5,
          "yearlyPricePerUser": 18
        },
        {
          "userRangeId": "01978169-54d0-49a4-a997-ce1e9fe503b4",
          "pricePerUser": 1.5,
          "yearlyPricePerUser": 18
        }
      ]
    }
  ]
}
```

Price fields:

```text
pricePerUser: 1.5
Means $1.50 per user per month.

yearlyPricePerUser: 18
Means $18.00 per user per year.

$1.50 x 12 months = $18.00
```

After successful creation, copy these from the response:

```text
data.id = generated product id
data.packages[].id = generated package ids
```

## 8. Verify

Endpoints:

```text
GET /cms/api/v1/features
GET /cms/api/v1/user-ranges
GET /cms/api/v1/products
```

Expected product result:

- Product name: `Security Awareness Training`
- Packages: `FREE TRIAL`, `Silver`, `Gold`, `Platinum`, `Diamond`
- Free Trial: `isTrial: true`
- Paid packages: `isPriceRange: true`
- Paid package pricing returned in `rangePricingResponse`
