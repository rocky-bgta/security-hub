# User Setup From Empty MongoDB

This guide explains how to bootstrap platform users when MongoDB is empty, including the services and Swagger URLs needed for Aspire Admin/System User creation and email OTP login verification.

## 1. Required services

For creating and logging in an Aspire Admin/System User, run:

```text
MongoDB
Redis
Registration service
Auth service
Notification service
```

CMS and Billing are not required only for Aspire Admin/System User creation.

Service URLs:

```text
Registration Swagger:
http://localhost:9090/registration/swagger-ui.html

Auth Swagger:
http://localhost:9093/auth/swagger-ui.html

Notification Swagger:
http://localhost:5656/notification/swagger-ui.html

Gateway Swagger, optional:
http://localhost:7030/swagger-ui.html
```

## 2. Empty database bootstrap

Start MongoDB first, then start the Registration service.

On startup, Registration initializes:

```text
System roles
Default Aspire MSP
Default Super Admin
```

The default Super Admin is created from Registration local config:

```text
email: superadmin01@yopmail.com
password: 123456789
```

Source:

```text
services/registration/service/src/main/resources/application-local.yml
services/registration/core/src/main/java/com/aspire/asat/registration/utils/SuperAdminInitializationService.java
```

## 3. Login as Super Admin

Open Auth Swagger:

```text
http://localhost:9093/auth/swagger-ui.html
```

Call:

```text
POST /auth/api/v1/auth/login
```

Use:

```text
username/email: superadmin01@yopmail.com
password: 123456789
```

Use the returned access token as the bearer token for Registration APIs.

## 4. Create Aspire Admin/System User

Open Registration Swagger:

```text
http://localhost:9090/registration/swagger-ui.html
```

First get available role IDs:

```text
GET /registration/api/v1/role
```

Then create the admin user:

```text
POST /registration/api/v1/system/user
```

Swagger section:

```text
System User Management
```

Example request shape:

```json
{
  "firstName": "Aspire",
  "lastName": "Admin",
  "email": "aspireadmin@example.com",
  "companyName": "Aspire",
  "designation": "Platform Admin",
  "department": "Security",
  "country": "Bangladesh",
  "zipCode": "1200",
  "supervisorName": "Super Admin",
  "roleIds": [
    "ROLE_ID_FROM_GET_ROLE"
  ]
}
```

Current implementation note:

```text
The Swagger description says this creates an ASPIRE_ADMIN user, but the service currently saves userType = SYSTEM_USER.
Several admin authorization paths treat SYSTEM_USER like Aspire Admin.
```

The Registration service generates a temporary password and sends it through Notification.

## 5. Email OTP/MFA login verification

Email OTP for login belongs to the Auth service, not Registration signup.

Make sure Auth MFA is enabled:

```text
MFA_ENABLED=true
MFA_EMAIL_ENABLED=true
```

Then use Auth Swagger:

```text
http://localhost:9093/auth/swagger-ui.html
```

Flow:

```text
1. POST /auth/api/v1/auth/login
2. If MFA is required, take tempToken from the login response
3. POST /auth/api/v1/auth/mfa/generate-otp
4. Read sessionId from the OTP response
5. Check the email inbox for the OTP
6. POST /auth/api/v1/auth/mfa/verify-otp
7. Use the returned final access token
```

MFA endpoints:

```text
POST /auth/api/v1/auth/mfa/generate-otp
POST /auth/api/v1/auth/mfa/verify-otp
```

## 6. Important distinction

Do not use trial or buy-now signup to create Aspire Admin users.

These Registration endpoints are for Client Admin signup:

```text
POST /registration/api/v1/trial/signup
POST /registration/api/v1/trial/verify-email
POST /registration/api/v1/trial/create-password

POST /registration/api/v1/trial/buy-now/signup
POST /registration/api/v1/trial/verify-email
POST /registration/api/v1/trial/buy-now/create-password
```

Those flows create client-side accounts, not Aspire Admin/System User accounts.
