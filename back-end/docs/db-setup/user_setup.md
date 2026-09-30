# User Setup From Empty Local MongoDB

This guide is for local setup when MongoDB is empty and email delivery may not work. The local profile now prints OTP values to the service console, so you can complete signup/MFA even when Notification cannot deliver emails.

## 1. Local services

Run these for user setup:

```text
MongoDB
Redis
Registration service
Auth service
Notification service
```

Swagger URLs:

```text
Registration:
http://localhost:9090/registration/swagger-ui.html

Auth:
http://localhost:9093/auth/swagger-ui.html

Notification:
http://localhost:5656/notification/swagger-ui.html
```

Recommended local Mongo env:

```text
MONGODB_URI=mongodb://localhost:27017
SPRING_PROFILES_ACTIVE=local
```

Registration/Auth local profiles must point to local Notification:

```text
notification.service.url=http://localhost:5656
notification.service.api-path=/notification/api/v1
client.notification.url=http://localhost:5656/notification
```

After changing these values, restart the affected service. Spring will not reload them into an already-running Registration/Auth process.

## 2. OTP From Console

For local profile, OTP is printed in the running service console.

Registration signup OTP appears in Registration console:

```text
LOCAL SIGNUP EMAIL OTP for client.admin@example.com: 123456
```

Auth MFA email OTP appears in Auth console:

```text
LOCAL MFA EMAIL OTP for superadmin01@yopmail.com: 123456
```

Notification local mode also has SMTP fallback-to-console enabled. If SMTP credentials are missing, test emails are logged instead of failing.

## 3. Bootstrap Super Admin

Start Registration service first. It auto-creates system roles, default MSP, and the default Super Admin.

Default Super Admin:

```text
email: superadmin01@yopmail.com
password: 123456789
```

There is no Swagger payload needed for the first Super Admin. It is created by startup code.

## 4. Login As Super Admin

Open Auth Swagger:

```text
POST /auth/api/v1/auth/login
```

Payload:

```json
{
  "username": "superadmin01@yopmail.com",
  "password": "123456789",
  "deviceInfo": {
    "platformType": "WEB",
    "platformInfo": "GOOGLE CHROME",
    "platformVersion": "1001.0.1.1",
    "deviceIdentifier": "local-swagger-device-001",
    "appLanguage": "ENGLISH",
    "appVersion": "1.0.1"
  }
}
```

If Auth requires MFA, use these endpoints.

Generate email OTP:

```text
POST /auth/api/v1/auth/mfa/generate-otp
```

Payload:

```json
{
  "method": "EMAIL",
  "tempToken": "PASTE_TEMP_TOKEN_FROM_LOGIN_RESPONSE"
}
```

Read the OTP from Auth console, then verify:

```text
POST /auth/api/v1/auth/mfa/verify-otp
```

Payload:

```json
{
  "method": "EMAIL",
  "code": "PASTE_6_DIGIT_OTP_FROM_AUTH_CONSOLE",
  "sessionId": "PASTE_SESSION_ID_FROM_GENERATE_OTP_RESPONSE",
  "tempToken": "PASTE_TEMP_TOKEN_FROM_LOGIN_RESPONSE"
}
```

Copy the final `accessToken` from the login or MFA response and authorize Registration Swagger with:

```text
Bearer PASTE_ACCESS_TOKEN
```

## 5. Get Role IDs

Open Registration Swagger and call:

```text
GET /registration/api/v1/role
```

Find these role records and copy their `id` values:

```text
SUPER_ADMIN
ASPIRE_ADMIN
CLIENT_ADMIN
```

Use the copied role ID in the payloads below. The create system user endpoint validates role IDs, not role names.

If MongoDB shows the role ID like this:

```text
id: ObjectId('6ab69faf78418f1e6010b95f')
```

paste only the hex value:

```json
"6ab69faf78418f1e6010b95f"
```

Do not paste `ObjectId(...)`.

## 6. Create Aspire Admin

Use Registration Swagger:

```text
POST /registration/api/v1/system/user
```

Payload:

```json
{
  "firstName": "Aspire",
  "lastName": "Admin",
  "email": "aspire.admin.local@example.com",
  "companyName": "Aspire",
  "designation": "Platform Admin",
  "department": "Security",
  "country": "Bangladesh",
  "zipCode": "1200",
  "supervisorName": "Super Admin",
  "roleIds": [
    "PASTE_ASPIRE_ADMIN_ROLE_ID"
  ]
}
```

Current implementation note: this endpoint stores `userType = SYSTEM_USER`, even when the assigned role is `ASPIRE_ADMIN`. Admin authorization paths treat `SYSTEM_USER` as platform-admin capable.

The service generates a temporary password and attempts to send it through Notification. If email delivery is unavailable, check the Registration/Notification logs.

## 7. Create Another Super Admin-Like System User

The first real Super Admin is auto-created at startup. If you need another platform user with the `SUPER_ADMIN` role, use:

```text
POST /registration/api/v1/system/user
```

Payload:

```json
{
  "firstName": "Second",
  "lastName": "Superadmin",
  "email": "second.superadmin.local@example.com",
  "companyName": "Aspire",
  "designation": "Super Admin",
  "department": "Platform",
  "country": "Bangladesh",
  "zipCode": "1200",
  "supervisorName": "System",
  "roleIds": [
    "PASTE_SUPER_ADMIN_ROLE_ID"
  ]
}
```

Implementation note: this still creates a `SYSTEM_USER` record with the `SUPER_ADMIN` role ID, not a bootstrap `userType = SUPER_ADMIN` record.

## 8. Create Trial Product And Package In CMS

Trial Client Admin password creation requires at least one product/package selection. If MongoDB is empty, create a trial product and package first.

Run CMS service and open:

```text
http://localhost:5050/cms/swagger-ui.html
```

Use CMS Swagger:

```text
POST /cms/api/v1/products
```

Payload:

```json
{
  "productName": "Security Awareness Training",
  "productDescription": "Local trial product for testing",
  "productStatus": "ENABLED",
  "thumbnailUrl": "",
  "tags": [
    "Security",
    "Training"
  ],
  "packages": [
    {
      "packageName": "Free Trial",
      "productId": "TEMP",
      "price": 0,
      "yearlyPrice": 0,
      "packageStatus": "ENABLED",
      "isTrial": true,
      "showInSite": true,
      "isPriceRange": false
    }
  ],
  "isTrial": true,
  "showInSite": true,
  "displayOrder": 1
}
```

From the response, copy:

```text
data.productId
data.packages[0].id
```

Use these copied values as:

```text
PASTE_TRIAL_PRODUCT_ID
PASTE_TRIAL_PACKAGE_ID
```

The package request uses `"productId": "TEMP"` only to satisfy create-product validation. CMS replaces it with the real product ID when it saves the package.

If CMS returns this message:

```json
{
  "message": "Product name 'Security Awareness Training' already exists."
}
```

do not create another product. Reuse the existing product and package IDs.

Use CMS Swagger:

```text
GET /cms/api/v1/products/{id}
```

For the current local database shown in MongoDB, use:

```text
GET /cms/api/v1/products/982bcfdc-8d64-477a-b1bc-b973caf366ca
```

From the response, use the `FREE TRIAL` package:

```text
productId = 982bcfdc-8d64-477a-b1bc-b973caf366ca
packageId = 16bae6b2-6de6-461f-b571-7f5a0de3c1ce
```

Ready-to-copy `productsData` for this local MongoDB:

```json
"productsData": [
  {
    "productId": "982bcfdc-8d64-477a-b1bc-b973caf366ca",
    "packageId": "16bae6b2-6de6-461f-b571-7f5a0de3c1ce",
    "productName": "Security Awareness Training",
    "packageName": "FREE TRIAL"
  }
]
```

## 9. Seed Two Trial Topics In CMS

Trial SubPackage creation requires at least two topics mapped to the selected product/package. If this error appears:

```text
Failed to create trial subPackage
SubPackage must have at least 2 topics
```

seed the minimum CMS topic data below.

Create category:

```text
POST /cms/api/v1/categories
```

```json
{
  "categoryName": "Local Security",
  "description": "Local setup category",
  "sortOrder": 1
}
```

Create compliance:

```text
POST /cms/api/v1/compliances
```

```json
{
  "complianceName": "Local Compliance",
  "acronym": "LOCAL",
  "description": "Local setup compliance",
  "sortOrder": 1
}
```

Create content type:

```text
POST /cms/api/v1/content-types
```

```json
{
  "typeName": "Article",
  "description": "Local setup content type",
  "sortOrder": 1
}
```

For the current local MongoDB, these IDs were created:

```text
categoryId = 90f1a919-7765-47bf-8ab0-f09b5ada8a12
complianceId = 78e304f2-bb42-4bc6-a6ca-685df03a1e04
contentTypeId = 3b337387-4296-4c02-9de3-0aa8dffab88f
```

Important: public catalog topics are auto-created as `DISABLED` by the current CMS implementation. For local setup, include `"clientId": "LOCAL_TRIAL_SETUP"` in the topic payload. That makes the topic private and allows CMS to store the requested `"status": "ENABLED"`, which the trial SubPackage lookup requires.

Create first topic:

```text
POST /cms/api/v1/topics
```

```json
{
  "topicName": "Local Trial Enabled Topic 1",
  "categoryIds": [
    "90f1a919-7765-47bf-8ab0-f09b5ada8a12"
  ],
  "countryIds": [
    "LOCAL"
  ],
  "complianceIds": [
    "78e304f2-bb42-4bc6-a6ca-685df03a1e04"
  ],
  "contentTypeId": "3b337387-4296-4c02-9de3-0aa8dffab88f",
  "durationMinutes": 5,
  "description": "Enabled private topic for local trial setup",
  "thumbnailUrl": "",
  "productPackages": [
    {
      "productId": "982bcfdc-8d64-477a-b1bc-b973caf366ca",
      "productName": "Security Awareness Training",
      "packageIds": [
        "16bae6b2-6de6-461f-b571-7f5a0de3c1ce"
      ]
    }
  ],
  "chapterIds": [],
  "totalContentCount": 0,
  "createdBy": "LOCAL_SETUP",
  "status": "ENABLED",
  "clientId": "LOCAL_TRIAL_SETUP"
}
```

Create second topic:

```json
{
  "topicName": "Local Trial Enabled Topic 2",
  "categoryIds": [
    "90f1a919-7765-47bf-8ab0-f09b5ada8a12"
  ],
  "countryIds": [
    "LOCAL"
  ],
  "complianceIds": [
    "78e304f2-bb42-4bc6-a6ca-685df03a1e04"
  ],
  "contentTypeId": "3b337387-4296-4c02-9de3-0aa8dffab88f",
  "durationMinutes": 5,
  "description": "Second enabled private topic for local trial setup",
  "thumbnailUrl": "",
  "productPackages": [
    {
      "productId": "982bcfdc-8d64-477a-b1bc-b973caf366ca",
      "productName": "Security Awareness Training",
      "packageIds": [
        "16bae6b2-6de6-461f-b571-7f5a0de3c1ce"
      ]
    }
  ],
  "chapterIds": [],
  "totalContentCount": 0,
  "createdBy": "LOCAL_SETUP",
  "status": "ENABLED",
  "clientId": "LOCAL_TRIAL_SETUP"
}
```

For the current local MongoDB, these enabled topic IDs were created:

```text
topic1 = 7cd74bbb-4be6-4cfa-95bd-5a009e0789e3
topic2 = 6d527e7f-4d62-4921-a7ad-784572f44468
```

After creating the two enabled topics, retry trial `assign-products` or `create-password`.

## 10. Create Client Admin With Trial Signup

Use this when you need a normal `CLIENT_ADMIN` account.

Step 1:

```text
POST /registration/api/v1/trial/signup
```

Payload:

```json
{
  "email": "client.admin.local@example.com",
  "phoneNumber": "+8801784669597",
  "phoneCode": "+880",
  "firstName": "Client",
  "lastName": "Admin",
  "companyName": "Local Client Company",
  "numberOfEmployees": "1-10 employees",
  "howDidYouHearAboutUs": "Local testing"
}
```

This step sends OTP and stores signup data in Registration memory. It does not create the Client Admin in MongoDB yet.

Read the OTP from Registration console:

```text
LOCAL SIGNUP EMAIL OTP for client.admin.local@example.com: 123456
```

Step 2:

```text
POST /registration/api/v1/trial/verify-email
```

Payload:

```json
{
  "email": "client.admin.local@example.com",
  "verificationCode": "PASTE_6_DIGIT_OTP_FROM_REGISTRATION_CONSOLE"
}
```

Step 3:

```text
POST /registration/api/v1/trial/create-password
```

Use this payload:

```json
{
  "email": "client.admin.local@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "productsData": [
    {
      "productId": "982bcfdc-8d64-477a-b1bc-b973caf366ca",
      "packageId": "16bae6b2-6de6-461f-b571-7f5a0de3c1ce",
      "productName": "Security Awareness Training",
      "packageName": "FREE TRIAL"
    }
  ]
}
```

If you get this error:

```text
At least one product is required
```

then `productsData` is missing or empty in `create-password`, and no product was stored in the original signup request. Create the CMS trial product/package first, then pass `productsData` in `create-password`.

After successful password creation, Client Admin data is saved in MongoDB:

```text
aspire_user
client_admins
client_products
end_user_packages
```

If `create-password` returns this error:

```text
A trial account already exists for this email. Use assign-products to add additional trial products.
```

then that email already exists in `aspire_user`. If `client_products` is still missing, the account was partially created during an earlier failed local attempt. Use a new email, delete the partial local records, or call:

```text
POST /registration/api/v1/trial/assign-products
```

Payload:

```json
{
  "email": "client.admin.local@example.com",
  "productsData": [
    {
      "productId": "982bcfdc-8d64-477a-b1bc-b973caf366ca",
      "packageId": "16bae6b2-6de6-461f-b571-7f5a0de3c1ce",
      "productName": "Security Awareness Training",
      "packageName": "FREE TRIAL"
    }
  ]
}
```

## 11. Create Client Admin With Buy Now Signup

Use this when you need a `CLIENT_ADMIN` account without trial product assignment during password creation.

Step 1:

```text
POST /registration/api/v1/trial/buy-now/signup
```

Payload:

```json
{
  "email": "buy.now.client.local@example.com",
  "phoneNumber": "+8801784669597",
  "phoneCode": "+880",
  "companyName": "Buy Now Client Company",
  "numberOfEmployees": "25-50 employees",
  "howDidYouHearAboutUs": "Local testing"
}
```

Read the OTP from Registration console.

Step 2:

```text
POST /registration/api/v1/trial/verify-email
```

Payload:

```json
{
  "email": "buy.now.client.local@example.com",
  "verificationCode": "PASTE_6_DIGIT_OTP_FROM_REGISTRATION_CONSOLE"
}
```

Step 3:

```text
POST /registration/api/v1/trial/buy-now/create-password
```

Payload:

```json
{
  "email": "buy.now.client.local@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "productId": "PASTE_PRODUCT_ID_OPTIONAL",
  "productName": "Security Awareness Training",
  "packageId": "PASTE_PACKAGE_ID_OPTIONAL",
  "packageName": "Silver",
  "userRangeId": "PASTE_USER_RANGE_ID_OPTIONAL"
}
```

Product fields are tracking fields for buy-now signup. Purchased license creation happens later in onboarding/buy-now processing.

## 12. Important Rules

Use `/system/user` for platform admin-style users:

```text
ASPIRE_ADMIN role
SUPER_ADMIN role
ADMIN role
```

Use trial or buy-now signup for:

```text
CLIENT_ADMIN users
```

Do not use trial or buy-now signup to create Aspire Admin users.
