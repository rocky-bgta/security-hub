# Security Awareness Training Product Setup

This guide explains how to set up the local MongoDB data for the pricing screen that shows:

- Free Trial
- Silver
- Gold
- Platinum
- Diamond
- User ranges such as `15-24 users`, `1000-2999 users`, and `10000+ users`
- Monthly and annual package pricing
- Package-specific feature lists

Use CMS first. Registration and Billing are only needed later when testing client onboarding, buy-now, invoices, or payment flows.

## 1. Start Local MongoDB

```powershell
docker run -d --name asat-local-mongo -p 27017:27017 mongo:7
```

Optional, only needed if you run Auth, Gateway, or the full backend flow:

```powershell
docker run -d --name asat-local-redis -p 6379:6379 redis:7.2-alpine
```

## 2. Run CMS Service Locally

Run from the repository root:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
$env:MONGODB_URI="mongodb://localhost:27017"
.\gradlew.bat :services:cms:service:bootRun
```

CMS Swagger:

```text
http://localhost:5050/cms/swagger-ui.html
```

Main CMS endpoints:

```text
POST /cms/api/v1/features
GET  /cms/api/v1/features

POST /cms/api/v1/user-ranges
GET  /cms/api/v1/user-ranges

POST /cms/api/v1/products
GET  /cms/api/v1/products
GET  /cms/api/v1/products/{id}
PUT  /cms/api/v1/products/{id}

POST /cms/api/v1/package-range-pricing/bulk
GET  /cms/api/v1/package-range-pricing/package/{packageId}
GET  /cms/api/v1/package-range-pricing/package/{packageId}/range?rangeId={userRangeId}
PUT  /cms/api/v1/package-range-pricing/{id}
DELETE /cms/api/v1/package-range-pricing/{id}
```

## 3. Create Features

Swagger endpoint:

```text
POST /cms/api/v1/features
```

Example payload:

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

Create the features needed by each card:

```text
Ticketing and Support
IT Security Policies
Usage Limits (5 Users)
Basic Support (FAQs-email)
Basic Customization Options
Bulk User Provisioning
Time-Limited Access (30 days)
Certification
100+ Content Library
Gamified Learning
Autonomous Campaigns
Culture Analytics
API-Integrations
Custom Branding
Elite Support
AD Integrations
AI-Recommended Training
150+ Content Library
Episodes Gamification
Everything In Silver
Ticketing and 8x5 Support
AI Personalization
USB Baiting Simulations
AD/SSO/SAML Integration
Ticketing and 12x7 Support
Custom Course Creation
Everything In Gold
200+ Content Library
Unlimited Phishing Security Tests
Role-Based Training
KillPhish AI
Phish Alert Button
Smishing Simulations
Callback Phishing
Ticketing and 24x5 Support
300+ Content Library
Everything In Platinum
Vishing Simulations
```

Copy each returned feature `id`; those IDs are used when creating packages.

## 4. Create User Ranges

Swagger endpoint:

```text
POST /cms/api/v1/user-ranges
```

Example payload:

```json
{
  "rangeName": "15-24 users",
  "minUsers": 15,
  "maxUsers": 24,
  "description": "15 to 24 users",
  "isDefault": false
}
```

Create all ranges:

```text
15-24 users
25-50 users
51-99 users
100-499 users
500-999 users
1000-2999 users
3000-4999 users
5000-9999 users
10000+ users
```

For `10000+ users`, use `maxUsers: null`:

```json
{
  "rangeName": "10000+ users",
  "minUsers": 10000,
  "maxUsers": null,
  "description": "10000 or more users",
  "isDefault": false
}
```

Copy each returned user range `id`; those IDs are used in range pricing.

## 5. Create Product With Free Trial And Paid Packages

Swagger endpoint:

```text
POST /cms/api/v1/products
```

Important enum values:

```text
productStatus: ENABLED or DISABLED
packageStatus: ENABLED or DISABLED
featureStatus: ENABLED or DISABLED
availability: FREE, PREMIUM, or RESTRICTED
```

The product API supports a maximum of 5 packages, so the setup fits exactly:

```text
FREE TRIAL
Silver
Gold
Platinum
Diamond
```

Example payload:

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
        { "id": "FEATURE_TICKETING_SUPPORT_ID", "name": "Ticketing and Support" },
        { "id": "FEATURE_IT_SECURITY_POLICIES_ID", "name": "IT Security Policies" },
        { "id": "FEATURE_USAGE_LIMITS_ID", "name": "Usage Limits (5 Users)" },
        { "id": "FEATURE_BASIC_SUPPORT_ID", "name": "Basic Support (FAQs-email)" },
        { "id": "FEATURE_BASIC_CUSTOMIZATION_ID", "name": "Basic Customization Options" },
        { "id": "FEATURE_BULK_USER_PROVISIONING_ID", "name": "Bulk User Provisioning" },
        { "id": "FEATURE_TIME_LIMITED_ACCESS_ID", "name": "Time-Limited Access (30 days)" },
        { "id": "FEATURE_CERTIFICATION_ID", "name": "Certification" }
      ],
      "price": 0,
      "yearlyPrice": 0,
      "packageStatus": "ENABLED",
      "isTrial": true,
      "showInSite": true,
      "isPriceRange": false
    },
    {
      "packageName": "Silver",
      "productId": "TEMP",
      "features": [
        { "id": "FEATURE_100_CONTENT_LIBRARY_ID", "name": "100+ Content Library" },
        { "id": "FEATURE_GAMIFIED_LEARNING_ID", "name": "Gamified Learning" },
        { "id": "FEATURE_AUTONOMOUS_CAMPAIGNS_ID", "name": "Autonomous Campaigns" },
        { "id": "FEATURE_CULTURE_ANALYTICS_ID", "name": "Culture Analytics" },
        { "id": "FEATURE_API_INTEGRATIONS_ID", "name": "API-Integrations" },
        { "id": "FEATURE_CUSTOM_BRANDING_ID", "name": "Custom Branding" },
        { "id": "FEATURE_ELITE_SUPPORT_ID", "name": "Elite Support" },
        { "id": "FEATURE_AD_INTEGRATIONS_ID", "name": "AD Integrations" }
      ],
      "price": 1.5,
      "yearlyPrice": 18,
      "packageStatus": "ENABLED",
      "isTrial": false,
      "showInSite": true,
      "isPriceRange": true,
      "rangePricing": [
        {
          "userRangeId": "RANGE_15_24_ID",
          "pricePerUser": 1.5,
          "yearlyPricePerUser": 18
        },
        {
          "userRangeId": "RANGE_1000_2999_ID",
          "pricePerUser": 1.5,
          "yearlyPricePerUser": 18
        }
      ]
    },
    {
      "packageName": "Gold",
      "productId": "TEMP",
      "features": [
        { "id": "FEATURE_AI_RECOMMENDED_TRAINING_ID", "name": "AI-Recommended Training" },
        { "id": "FEATURE_150_CONTENT_LIBRARY_ID", "name": "150+ Content Library" },
        { "id": "FEATURE_EPISODES_GAMIFICATION_ID", "name": "Episodes Gamification" },
        { "id": "FEATURE_EVERYTHING_IN_SILVER_ID", "name": "Everything In Silver" },
        { "id": "FEATURE_TICKETING_8X5_SUPPORT_ID", "name": "Ticketing and 8x5 Support" },
        { "id": "FEATURE_AI_PERSONALIZATION_ID", "name": "AI Personalization" },
        { "id": "FEATURE_USB_BAITING_SIMULATIONS_ID", "name": "USB Baiting Simulations" }
      ],
      "price": 2,
      "yearlyPrice": 24,
      "packageStatus": "ENABLED",
      "isTrial": false,
      "showInSite": true,
      "isPriceRange": true,
      "rangePricing": [
        {
          "userRangeId": "RANGE_15_24_ID",
          "pricePerUser": 2,
          "yearlyPricePerUser": 24
        }
      ]
    },
    {
      "packageName": "Platinum",
      "productId": "TEMP",
      "features": [
        { "id": "FEATURE_AD_SSO_SAML_ID", "name": "AD/SSO/SAML Integration" },
        { "id": "FEATURE_TICKETING_12X7_SUPPORT_ID", "name": "Ticketing and 12x7 Support" },
        { "id": "FEATURE_CUSTOM_COURSE_CREATION_ID", "name": "Custom Course Creation" },
        { "id": "FEATURE_EVERYTHING_IN_GOLD_ID", "name": "Everything In Gold" },
        { "id": "FEATURE_200_CONTENT_LIBRARY_ID", "name": "200+ Content Library" },
        { "id": "FEATURE_UNLIMITED_PHISHING_TESTS_ID", "name": "Unlimited Phishing Security Tests" },
        { "id": "FEATURE_ROLE_BASED_TRAINING_ID", "name": "Role-Based Training" }
      ],
      "price": 2.25,
      "yearlyPrice": 27,
      "packageStatus": "ENABLED",
      "isTrial": false,
      "showInSite": true,
      "isPriceRange": true,
      "rangePricing": [
        {
          "userRangeId": "RANGE_15_24_ID",
          "pricePerUser": 2.25,
          "yearlyPricePerUser": 27
        }
      ]
    },
    {
      "packageName": "Diamond",
      "productId": "TEMP",
      "features": [
        { "id": "FEATURE_KILLPHISH_AI_ID", "name": "KillPhish AI" },
        { "id": "FEATURE_PHISH_ALERT_BUTTON_ID", "name": "Phish Alert Button" },
        { "id": "FEATURE_SMISHING_SIMULATIONS_ID", "name": "Smishing Simulations" },
        { "id": "FEATURE_CALLBACK_PHISHING_ID", "name": "Callback Phishing" },
        { "id": "FEATURE_TICKETING_24X5_SUPPORT_ID", "name": "Ticketing and 24x5 Support" },
        { "id": "FEATURE_300_CONTENT_LIBRARY_ID", "name": "300+ Content Library" },
        { "id": "FEATURE_EVERYTHING_IN_PLATINUM_ID", "name": "Everything In Platinum" },
        { "id": "FEATURE_VISHING_SIMULATIONS_ID", "name": "Vishing Simulations" }
      ],
      "price": 2.92,
      "yearlyPrice": 35.04,
      "packageStatus": "ENABLED",
      "isTrial": false,
      "showInSite": true,
      "isPriceRange": true,
      "rangePricing": [
        {
          "userRangeId": "RANGE_15_24_ID",
          "pricePerUser": 2.92,
          "yearlyPricePerUser": 35.04
        }
      ]
    }
  ]
}
```

The `productId` inside package objects can be sent as `TEMP`; the CMS service replaces it with the newly created product ID when saving.

## 6. Monthly And Annual Toggle Mapping

The frontend toggle should use these CMS fields:

```text
Monthly fixed package price: price
Annual fixed package price: yearlyPrice

Monthly range price per user: pricePerUser
Annual range price per user: yearlyPricePerUser
```

For example:

```text
Silver monthly: 1.50 per user/month
Silver annual: 18.00 per user/year

Gold monthly: 2.00 per user/month
Gold annual: 24.00 per user/year
```

If annual pricing has a discount, set `yearlyPrice` or `yearlyPricePerUser` to the discounted annual value instead of `monthly * 12`.

## 7. Verify Data

Use Swagger:

```text
GET /cms/api/v1/products
GET /cms/api/v1/user-ranges
GET /cms/api/v1/features
```

Expected result:

- One product named `Security Awareness Training`
- Five packages under that product
- Free Trial has `isTrial: true`
- Silver, Gold, Platinum, and Diamond have `isPriceRange: true`
- Paid packages include `rangePricingResponse`
- Features are returned inside each package by ID and name

## 8. Full Purchase Flow Services

Only run these after CMS setup when testing onboarding, buy-now, invoices, or payments:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
$env:MONGODB_URI="mongodb://localhost:27017"

.\gradlew.bat :services:registration:service:bootRun
.\gradlew.bat :services:billing:service:bootRun
.\gradlew.bat :services:auth:service:bootRun
.\gradlew.bat :services:notification:service:bootRun
```

Swagger URLs:

```text
CMS:          http://localhost:5050/cms/swagger-ui.html
Registration: http://localhost:9090/registration/swagger-ui.html
Billing:      http://localhost:6060/billing/swagger-ui.html
Auth:         http://localhost:9093/auth/swagger-ui.html
Notification: http://localhost:5656/notification/swagger-ui.html
```

Useful Registration endpoints after product setup:

```text
POST /registration/api/v1/client/admin/onboard
POST /registration/api/v1/client/admin/buy-now
GET  /registration/api/v1/client/admin/products/assigned/{clientAdminId}
GET  /registration/api/v1/client/admin/product/package/detail/{id}
GET  /registration/api/v1/client/admin/license-statistics
GET  /registration/api/v1/client/admin/license-overview-summary
```

For the pricing screen setup, CMS is enough. Create features, create user ranges, then create the `Security Awareness Training` product with Free Trial, Silver, Gold, Platinum, and Diamond packages.
