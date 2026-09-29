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

## 8. Create Client Admin With Trial Signup

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
  "howDidYouHearAboutUs": "Local testing",
  "productsData": [
    {
      "productId": "PASTE_TRIAL_PRODUCT_ID_OR_REMOVE_PRODUCTS_DATA",
      "packageId": "PASTE_TRIAL_PACKAGE_ID_OR_REMOVE_PRODUCTS_DATA",
      "productName": "Security Awareness Training",
      "packageName": "Free Trial"
    }
  ]
}
```

If you do not have product/package IDs yet, remove the entire `productsData` field for the original signup flow:

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

Payload without product assignment:

```json
{
  "email": "client.admin.local@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!"
}
```

Payload with legacy single-product trial assignment:

```json
{
  "email": "client.admin.local@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "productId": "PASTE_TRIAL_PRODUCT_ID",
  "subPackageId": "PASTE_TRIAL_PACKAGE_ID",
  "productName": "Security Awareness Training",
  "subPackageName": "Free Trial"
}
```

Payload with multi-product trial assignment:

```json
{
  "email": "client.admin.local@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "productsData": [
    {
      "productId": "PASTE_TRIAL_PRODUCT_ID",
      "packageId": "PASTE_TRIAL_PACKAGE_ID",
      "productName": "Security Awareness Training",
      "packageName": "Free Trial"
    }
  ]
}
```

## 9. Create Client Admin With Buy Now Signup

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

## 10. Important Rules

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
