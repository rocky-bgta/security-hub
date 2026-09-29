# Aspire Security Platform --- Claude Code Project Context

> **Purpose of this file**
>
> This file consolidates the business context, backend architecture,
> verified workflows, important APIs, data relationships, runtime
> dependencies, and implementation details discussed while analyzing the
> Aspire project. It is intended to be placed at the repository root so
> Claude Code can understand the project before making changes.
>
> **Accuracy rule:** Treat items marked **Needs Verification** as
> unresolved. Do not invent endpoints, DTO fields, collections, service
> calls, or business rules. When code conflicts with this document,
> current source code is authoritative.

------------------------------------------------------------------------

## 1. Business Purpose

Aspire is a multi-tenant security-awareness platform. Organizations
purchase security-awareness products/packages and assign
training/content to their employees/end users.

The discussed business areas include:

-   Security Awareness Training.
-   Phishing/security-awareness related products and simulations.
-   Client organization onboarding.
-   Product/package catalog and user-range based pricing.
-   License purchase, invoice and payment.
-   End-user creation and training assignment.
-   Training progress tracking.
-   Administrative license visibility.
-   MSP-based client ownership/sales model.

### Main actors

#### Aspire Admin / Super Admin

Platform-level administrator.

Typical responsibilities:

-   Manage or view platform-wide catalog/client information.
-   View clients broadly.
-   View global license distribution.
-   Create/manage catalog data where authorization requires platform
    administration.
-   Inspect individual client license usage.

#### Client Admin

Administrator of a customer organization.

Typical responsibilities:

-   Sign up and verify account.
-   Complete organization/billing onboarding.
-   Select/buy products/packages/licenses.
-   Create/manage organization end users.
-   Select topics from purchased packages.
-   Create client-specific SubPackages.
-   Assign SubPackages/training to end users.
-   View organization-scoped data.

Client-owned data is primarily scoped using `clientAdminId`.

#### End User

An employee/learner belonging to a Client Admin organization.

Typical responsibilities:

-   Receive assigned security-awareness training.
-   Consume Topic → Chapter → Content.
-   Accumulate training/progress information.

#### MSP

Managed Service Provider / reseller layer.

-   MSPs can manage/sell Aspire products to clients.
-   The system uses an MSP concept even for direct purchases.
-   Direct/self-service clients can use the default/virtual MSP
    represented in code by `DefaultMspData`.

------------------------------------------------------------------------

## 2. Backend Service Responsibilities

### Auth Service

Primary responsibility: authentication/security.

Known flow:

-   Username/password login.
-   Temporary token generation.
-   MFA/OTP generation and verification.
-   Final access/refresh token issuance.
-   Refresh token flow.

Known base path:

`/api/v1/auth`

Important endpoints discussed:

-   `POST /api/v1/auth/login`
-   `POST /api/v1/auth/mfa/generate-otp`
-   `POST /api/v1/auth/mfa/verify-otp`
-   `POST /api/v1/auth/refresh`

Email MFA login sequence:

1.  Call `/login` with username/password/device information.
2.  Receive `tempToken`.
3.  Call `/mfa/generate-otp` with `method=EMAIL` and `tempToken`.
4.  Receive `sessionId`; OTP is sent to email.
5.  Call `/mfa/verify-otp` with OTP, `sessionId`, and `tempToken`.
6.  Receive final authentication tokens.

Relevant code discussed:

-   `LoginController.java`
-   `MfaController.java`
-   `WebApiUrlConstants.java`
-   `LoginWithPasswordRequest`
-   `GenerateOtpRequest`
-   `VerifyOtpRequest`
-   `DeviceInfoRequest`

### Registration Service

Registration owns a large part of client lifecycle and license
assignment business logic.

Responsibilities discussed:

-   Trial signup.
-   Buy-now signup/account creation.
-   Email OTP verification for signup.
-   Client Admin persistence.
-   Client onboarding/buy-now organization and billing data.
-   Creating `ClientProduct` records.
-   Invoice request delegation to Billing.
-   End-user creation.
-   SubPackage assignment to users.
-   License-capacity validation.
-   `UserLicence` creation.
-   `usedLicenseCount` update.
-   License activation after Billing confirms full payment.

Important classes discussed:

-   `TrialSignupController`
-   `TrialSignupService`
-   `TrialSignupServiceImpl`
-   `ClientAdminController`
-   `ClientAdminControllerImpl`
-   `ClientAdminServiceImpl`
-   `EndUserController`
-   `EndUserControllerImpl`
-   `EndUserServiceImpl`
-   `RegistrationNotificationClient`

### CMS Service

CMS owns catalog/training structure and learner-side course/progress
structures.

Responsibilities discussed:

-   Products.
-   Packages.
-   User ranges.
-   Package-range pricing.
-   Topics.
-   Client SubPackages.
-   Mapping selected topics into client SubPackages.
-   Client product/package views/replicas.
-   CMS-side user SubPackage/progress records after Registration
    succeeds.
-   Topic/chapter/content learning structure.

Important controllers/classes discussed:

-   `ClientProductController`
-   `TopicController`
-   `SubPackageController`
-   `ClientUserOperationServiceImpl`
-   `WebApiUrlConstants`

### Billing Service

Responsibilities discussed:

-   Invoice/payment domain.
-   Online payment entry.
-   Stripe Checkout session creation.
-   Stripe webhook processing.
-   Payment status updates.
-   Invoice paid-state calculation.
-   Triggering Registration license activation after full payment.

Important classes discussed:

-   `PaymentController`
-   `PaymentServiceImpl`
-   `InvoiceService`
-   `PaymentSourceDTO`
-   `PaymentResponseDTO`

### Notification Service

Responsibilities include sending email/multi-channel notifications.

Registration uses notification clients for signup/welcome-related
communication.

Important caution:

-   Some notification wrapper methods catch/log exceptions rather than
    propagating them.
-   Do not assume every notification failure aborts the parent business
    transaction without checking the exact call path.

### Other services

The repository also contains services such as Phishing, Breach
Detection, Universal and VPS. They are part of the wider platform but
were not analyzed to the same depth in this discussion.

------------------------------------------------------------------------

## 3. Main Data Model / Collections

The project uses MongoDB rather than a single relational database.
Services have their own persistence boundaries/collections.

### ASPIRE_USER

Important fields discussed:

-   `id`
-   `userId`
-   `email`
-   `username`
-   `userType`
-   `status`
-   `clientAdminId`
-   `mspId`
-   `roles[]`
-   `department`
-   `companyName`

### ROLE

-   `id`
-   `roleName`
-   `accessLevel`
-   `status`
-   `systemRole`

### ROLE_PERMISSION

-   `id`
-   `roleId`
-   `roleName`
-   `menuPermissions[]`

### CLIENT_ADMIN

Important fields discussed:

-   `id`
-   `clientAdminId`
-   `organizationName`
-   `email`
-   `mspId`
-   `status`
-   `roleIds[]`
-   `clientProductIds[]`
-   `country`
-   `domain`

Additional organization/billing onboarding data exists in DTO/domain
code; inspect current entities before modifying persistence.

### MSP_USER

-   `id`
-   `mspId`
-   `organizationName`
-   `contactEmail`
-   `status`
-   `clientProductIds[]`
-   `roleIds[]`

### CLIENT_PRODUCT

Critical license entity.

Important fields:

-   `id`
-   `clientAdminId`
-   `productId`
-   `packageId`
-   `licenseCount`
-   `usedLicenseCount`
-   `licenseStatus`
-   `mspId`

Interpretation:

-   `licenseCount` = purchased/allocated user-seat capacity.
-   `usedLicenseCount` = consumed/assigned user licenses.
-   Available capacity is generally derived as
    `licenseCount - usedLicenseCount`.

### USER_LICENCE

Tracks user-level license assignment.

Important fields:

-   `id`
-   `userId`
-   `clientAdminId`
-   `productId`
-   `packageId`
-   `licenceStatus`

### END_USER_PACKAGE

Tracks end-user package/SubPackage assignment/progress.

Important fields:

-   `id`
-   `userId`
-   `clientAdminId`
-   `productId`
-   `subPackageId`
-   `progress`
-   `status`
-   `active`

### DEPARTMENT

-   `id`
-   `clientAdminId`
-   `name`
-   `active`
-   `isSystemDefined`

### Main client scoping key

The most important cross-entity client ownership key is:

`clientAdminId`

It is used to correlate organization-owned users, products/licenses,
user licenses, end-user package/progress records, departments and other
client-specific data.

------------------------------------------------------------------------

## 4. Product Catalog Model

The catalog structure clarified during the discussion is:

``` text
Product
  └── Package
        └── User Range pricing
```

Example:

``` text
Security Awareness Training
  ├── Silver
  ├── Gold
  ├── Platinum
  └── Diamond
```

User ranges can include:

-   15--24 users
-   25--50 users
-   51--99 users
-   100--499 users
-   500--999 users
-   1000--2999 users
-   3000--4999 users
-   5000--9999 users
-   10000+ users

A package can have pricing associated with a user range. Therefore
pricing should be understood as a **Package + User Range** combination,
rather than simply a single product price.

During client signup/onboarding/purchase, the client can select a
package and user count/range. That selection feeds license capacity,
pricing, invoice and payment processing.

### CMS APIs for catalog setup

Known APIs discussed:

#### User ranges

-   `POST /cms/api/v1/user-ranges`
-   `GET /cms/api/v1/user-ranges`
-   `PUT /cms/api/v1/user-ranges/{id}`
-   `DELETE /cms/api/v1/user-ranges/{id}`

Example create request:

``` json
{
  "rangeName": "1000-2999 users",
  "minUsers": 1000,
  "maxUsers": 2999,
  "description": "1000 to 2999 users",
  "isDefault": false
}
```

For open-ended range:

``` json
{
  "rangeName": "10000+ users",
  "minUsers": 10000,
  "maxUsers": null,
  "description": "10000 or more users",
  "isDefault": false
}
```

#### Products/packages

-   `POST /cms/api/v1/products`
-   `GET /cms/api/v1/products`
-   `GET /cms/api/v1/products/{id}`
-   `PUT /cms/api/v1/products/{id}`
-   `DELETE /cms/api/v1/products/{id}`

Product creation can include nested packages.

An observed validation oddity: package rows inside product creation may
still require a nonblank `productId`, even though the service later
replaces it with the newly created product ID. A temporary placeholder
such as `"productId": "TEMP"` may therefore be needed for Swagger
testing. **Re-check current DTO validation before relying on this
workaround.**

#### Package-range pricing

-   `POST /cms/api/v1/package-range-pricing/bulk`
-   `GET /cms/api/v1/package-range-pricing/package/{packageId}`
-   `GET /cms/api/v1/package-range-pricing/package/{packageId}/range?rangeId={userRangeId}`
-   `PUT /cms/api/v1/package-range-pricing/{id}`
-   `DELETE /cms/api/v1/package-range-pricing/{id}`

Typical catalog creation order:

1.  Create User Ranges.
2.  Create Product and Packages.
3.  Capture generated product/package IDs.
4.  Create Package + User Range pricing.

For catalog creation alone, CMS is the primary application service
discussed.

------------------------------------------------------------------------

## 5. Client Signup and Verification

### Trial signup

`TrialSignupServiceImpl` maintains temporary signup/OTP state using
in-memory maps:

-   `otpStorage`
-   `signupDataStorage`
-   `verifiedEmails`
-   equivalent buy-now temporary maps

The implementation comment indicates this is temporary/in-memory storage
and could use Redis in production.

Important: OTP itself in this implementation is not persisted to
MongoDB.

Trial signup high-level flow:

1.  Submit account details.
2.  Check existing user/domain.
3.  Store temporary signup information.
4.  Generate/send verification code.
5.  Verify email OTP and expiry.
6.  Create password.
7.  Create `AspireUser`.
8.  Create `ClientAdmin`.
9.  Link `AspireUser` with `clientAdminId` and `mspId`.
10. Assign trial products.
11. Create relevant client/product/package structures.
12. Notify CMS as required.
13. Send welcome notification.

Trial assignment observed defaults included a trial period and a small
trial license allocation (previously observed as 5 in the inspected
code).

### Buy-now signup

High-level behavior discussed:

1.  Submit Buy Now account details.
2.  Duplicate user check.
3.  Store temporary signup information.
4.  Generate/send OTP.
5.  Verify email using common verification flow.
6.  Create password.
7.  Create `AspireUser` with `CLIENT_ADMIN` type.
8.  Create `ClientAdmin` using default MSP.
9.  Initial ClientAdmin status observed as `PENDING`.
10. Store selected product/package/user-range IDs/names for tracking.
11. Do **not** treat these tracking fields as proof that an active
    purchased `ClientProduct` already exists.
12. Later onboarding/buy-now processing creates purchase/license records
    and invoice.

------------------------------------------------------------------------

## 6. Client Onboarding / Buy-Now After Account Verification

A Client Admin does not simply sign up and immediately obtain a fully
active purchased-license dashboard state.

The discussed onboarding/purchase phase collects additional organization
and billing information and prepares the purchase/invoice.

### Registration endpoints

Known endpoints:

-   `POST /api/v1/client/admin/onboard`
-   `POST /api/v1/client/admin/buy-now`

The existing-client buy-now flow uses:

`ClientAdminControllerImpl → clientAdminService.processBuyNow(requestDto)`

### BuyNowRequestDto structure

Discussed high-level fields:

-   `clientAdminId`
-   `organization`
-   `billing`
-   `mspId`
-   `mspName`
-   `productSelections`
-   `invoice`

### OrganizationInfoDto

Fields discussed:

-   `organizationType`
-   `timeZone`
-   `language`
-   `industry`
-   `subIndustryId`
-   `organizationSize`
-   `country`
-   `stateProvince`
-   `streetAddress`
-   `city`
-   `zipPostalCode`

The UI can prefill some known account/contact information and allow
editing, but front-end behavior is outside the backend scope of this
document.

### BillingInfoDto

Contains billing information used during onboarding/purchase. Inspect
current DTO source for exact fields before constructing production
requests.

### ProductSelectionDto

Carries selected product/package/license information. Inspect current
source for exact request schema.

### InvoiceDetailsDto

Carries invoice-related information. Inspect current source for exact
schema.

### `processBuyNow` responsibilities discussed

`ClientAdminServiceImpl.processBuyNow`:

1.  Finds existing ClientAdmin.
2.  Updates organization information.
3.  Updates billing information.
4.  Applies authoritative pricing.
5.  Creates `ClientProduct` records.
6.  Builds invoice request.
7.  Calls Billing service to create invoice.
8.  Updates CMS dashboard.
9.  Sends invoice email.

Product/license creation includes logic equivalent to:

``` java
clientProduct.setLicenseCount(selection.getLicenseCount());
clientProduct.setProductId(selection.getProductId());
clientProduct.setPackageId(selection.getPackageId());
clientProduct.setLicenseStatus("PENDING");
```

Therefore the purchased product/license is initially **PENDING** before
successful full payment.

------------------------------------------------------------------------

## 7. Invoice and Stripe Payment Flow

### Responsibility split

Registration owns client onboarding/buy-now orchestration and delegates
invoice creation/payment handling to Billing.

Billing owns Stripe Checkout/payment handling.

### Invoice creation

Registration calls Billing invoice creation through its invoice
client/service.

Discussed call:

`POST {billingServiceUrl}/invoice/create`

### Online payment entry

Billing endpoint discussed:

`POST /api/v1/payment/online-payment-entry`

The payment source DTO uses:

``` text
method = STRIPE
```

Billing creates the Stripe Checkout session in:

`PaymentServiceImpl.processStripePayment`

Stripe session creation uses logic equivalent to:

`Session.create(paramsBuilder.build())`

### Checkout URL

`PaymentServiceImpl.saveOnlinePayment` returns a `PaymentResponseDTO`
containing:

-   payment ID
-   Stripe `checkoutUrl`
-   success message

This means backend testing can be performed without the application
front end:

1.  Call the Billing online-payment API from Swagger/Postman.
2.  Read the returned Stripe `checkoutUrl`.
3.  Open that URL in a normal browser.
4.  Complete Stripe Checkout.
5.  Stripe invokes the backend webhook.

Postman/Swagger creates the payment/session; Stripe's hosted UI is
opened in the browser.

### Stripe success/cancel URLs

Backend creates the Checkout session with URLs similar to:

-   Success: `{frontEndUrl}/payment-success?...`
-   Cancel: `{frontEndUrl}/payment-failed`

Backend does not itself implement the final browser navigation from the
front-end success page to the dashboard.

For backend-only testing, focus on payment state, webhook processing,
invoice state and license activation rather than UI dashboard redirect.

------------------------------------------------------------------------

## 8. Stripe Webhook and License Activation

### Webhook endpoint

Known endpoint:

`POST /api/v1/payment/stripe/webhook/payment`

### Billing processing

Flow:

``` text
PaymentController
  → PaymentServiceImpl.processStripeWebhook
```

Stripe events discussed:

-   `checkout.session.completed` → `handleCheckoutSessionCompleted`
-   `payment_intent.succeeded` → `handlePaymentIntentSucceeded`

Both ultimately lead to:

`PaymentServiceImpl.updatePaymentStatus`

On successful payment:

1.  `payment.status` becomes `SUCCESS`.
2.  Billing calls invoice-payment reconciliation/update logic.
3.  It determines whether the invoice is fully paid.
4.  If fully paid, `activateEntityAfterPayment(...)` runs.
5.  For client invoices, Billing calls `activateClientAdmin(clientId)`.

### Billing → Registration activation call

Billing calls Registration:

`PUT {registrationUrl}/client/admin/activate-license/{clientId}`

Registration endpoint:

`PUT /api/v1/client/admin/activate-license/{clientId}`

Registration service:

`ClientAdminServiceImpl.activateLicense`

Observed state changes:

``` text
ClientAdmin.status → ACTIVE

ClientProduct.licenseStatus:
PENDING → ACTIVE
```

Activation occurs after the invoice becomes fully paid, not merely
because a Checkout session was created.

------------------------------------------------------------------------

## 9. Database Changes During Payment

The discussed payment/activation path affects multiple service-owned
records.

### Billing side

Payment record:

-   Payment status becomes `SUCCESS` after successful processing.

Invoice:

-   Invoice/payment state is updated.
-   When payments satisfy the invoice amount, invoice becomes fully
    paid/`PAID` according to Billing logic.

### Registration side

After Billing invokes activation:

ClientAdmin:

-   status becomes `ACTIVE`.

ClientProduct:

-   pending purchased products/licenses become `ACTIVE`.

Conceptual state transition:

``` text
Before payment:
ClientAdmin      = PENDING (where applicable)
ClientProduct    = PENDING
Invoice          = unpaid/pending
Payment          = pending

After fully successful payment:
Payment          = SUCCESS
Invoice          = PAID
ClientAdmin      = ACTIVE
ClientProduct    = ACTIVE
```

Inspect current entities/enums before relying on literal status strings
outside the already observed code.

------------------------------------------------------------------------

## 10. Purchased Product → Package → Topics → SubPackage

A major business distinction:

**Client Admin does not normally create platform catalog Topics during
the post-purchase assignment flow.**

Platform/Aspire catalog administration creates Products, Packages and
Topics. Client Admin consumes purchased catalog content.

Correct client flow:

``` text
Purchased Product
  → Purchased Package
  → Existing Topics for Product + Package
  → Client selects Topics
  → Client creates SubPackage
  → Client assigns SubPackage to End Users
```

### Client product APIs

Known CMS endpoints:

`GET /api/v1/client-product/{clientAdminId}`

Returns unique products assigned to the Client Admin.

`GET /api/v1/client-product/packages?productId={productId}`

Uses current client context and returns packages assigned to the Client
Admin for the selected product.

### Topics for selected Product + Package

Exact endpoint discussed:

`GET /api/v1/topics/product/package/topics`

Query parameters:

-   `productId` required
-   `packageId` required
-   `search` optional
-   `offset` default `0`
-   `pageSize` default `10`
-   `sortBy` default `createdAt`
-   `order` default `desc`

Controller method:

`TopicController.getTopicsByProductAndPackage(...)`

Response:

`ApiResponseDto<AllResponseDto<List<TopicMinimalDto>>>`

Purpose:

Return minimal topic information filtered by selected Product and
Package.

### Topic administration

`POST /api/v1/topics`

was observed as a platform-admin operation for roles such as:

-   `ASPIRE_ADMIN`
-   `SUPER_ADMIN`
-   `SYSTEM_USER`

Therefore do not insert Topic creation into the normal Client Admin
purchased-package assignment workflow.

------------------------------------------------------------------------

## 11. SubPackage Creation

Known endpoint:

`POST /api/v1/sub-packages`

Other SubPackage endpoints discussed:

-   `POST /api/v1/sub-packages/trial`
-   `GET /api/v1/sub-packages/{id}`
-   `GET /api/v1/sub-packages/{id}/assigned-users`
-   `GET /api/v1/sub-packages`
-   `GET /api/v1/sub-packages/exists?...`
-   `PUT /api/v1/sub-packages/{id}`
-   `DELETE /api/v1/sub-packages/{id}`
-   `GET /api/v1/sub-packages/client/user-subpackage?...`
-   `POST /api/v1/sub-packages/by-package-ids`

`SubPackageRequestDto` was discussed as containing fields including:

-   `productId`
-   `packageId`
-   `productPackageId`
-   `clientId`
-   `clientAdminId`
-   selected `topicId[]`

A minimum topic-count validation was previously observed (at least two
topics). **Re-open the current DTO before generating an exact production
request body.**

------------------------------------------------------------------------

## 12. End User Creation

Registration endpoint:

`POST /api/v1/end-user`

Purpose:

Create a new End User under a Client Admin.

Other useful Registration End User APIs discussed:

-   `GET /api/v1/end-user/{userId}`
-   `PUT /api/v1/end-user`
-   `GET /api/v1/end-user?clientAdminId=...`
-   `GET /api/v1/end-user/users?clientAdminId=...`
-   `GET /api/v1/end-user/unassigned?clientAdminId=...&subPackageId=...`
-   `GET /api/v1/end-user/{userId}/sub-packages`
-   `GET /api/v1/end-user/{userId}/user-data`
-   `GET /api/v1/end-user/notification-data?userId=...`

Bulk onboarding endpoints discussed:

-   `POST /api/v1/end-user/bulk-import/validate`
-   `GET /api/v1/end-user/bulk-import/{sessionId}/users`
-   `PUT /api/v1/end-user/bulk-import/{sessionId}/users`
-   `POST /api/v1/end-user/bulk-import/{sessionId}/onboard`

------------------------------------------------------------------------

## 13. SubPackage Assignment and License Consumption

### Assignment endpoint

Registration:

`POST /api/v1/end-user/assign-sub-package`

Flow:

``` text
EndUserController
  → EndUserControllerImpl
  → endUserService.assignSubPackageToUsers(...)
  → EndUserServiceImpl
```

### Critical license methods

Methods identified in `EndUserServiceImpl`:

-   `assignSubPackageToUsers`
-   `validateLicenseAvailabilityForBulkAssignment`
-   `validateLicenseLimit`
-   `createUserLicence`

### Bulk license validation

`validateLicenseAvailabilityForBulkAssignment` reads the relevant
ClientProduct license capacity and existing license usage.

Core calculation:

``` text
availableLicenses = licenseCount - existingLicenceCount
```

Assignment is blocked when the requested user assignments exceed
available license capacity.

### Per-user safety validation

`validateLicenseLimit` performs an additional per-user safety check
before saving assignment/license state.

### UserLicence creation and used count

`createUserLicence` creates the user-level license record and updates
ClientProduct usage.

Observed logic:

``` java
clientProduct.setUsedLicenseCount(existingLicenceCount + 1);
```

Therefore:

``` text
licenseCount = total purchased capacity
usedLicenseCount = consumed user seats
remaining = licenseCount - usedLicenseCount
```

Example:

``` text
Purchased capacity = 24
Used = 1
Available = 23
```

### CMS after Registration succeeds

CMS does not own the license-capacity enforcement.

After Registration validates/creates the assignment/license, CMS creates
the course/progress-side records through logic associated with
`ClientUserOperationServiceImpl`.

Conceptual ownership:

``` text
Registration:
  license validation
  UserLicence
  EndUserPackage/assignment business state
  ClientProduct.usedLicenseCount

CMS:
  learning/SubPackage progress representation
```

### Important business rule

The intended business understanding discussed is that license
consumption is user-seat based, not topic-count based. Assigning several
pieces of content/SubPackages to the same licensed user should not
blindly consume multiple seats.

**When modifying this behavior, inspect the current
duplicate/existing-license checks in `EndUserServiceImpl` rather than
relying only on this business statement.**

------------------------------------------------------------------------

## 14. Aspire Admin License Visibility

There are two distinct views and they must not be mixed.

### Global Aspire Admin dashboard

Endpoint:

`GET /api/v1/dashboard/license-distribution`

Flow discussed:

``` text
DashboardController
  → DashboardControllerImpl
  → DashboardServiceImpl.getLicenseDistribution
```

For Aspire Admin, no MSP ID is resolved, so global data is loaded using:

`mspProductRepository.findAll()`

`calculateLicenseDistribution` calculates values conceptually as:

``` text
totalAllocated += licenseCount
totalAvailable += licenseCount - usedLicenseCount   // non-expired
totalActive += usedLicenseCount
totalExpired += unused expired capacity
```

Response DTO:

`LicenseDistributionResponseDto`

Fields:

-   `totalAvailable`
-   `totalAllocated`
-   `totalActive`
-   `totalExpired`

Important:

**Global Aspire Admin license distribution uses `msp_products`, not the
individual client-specific `client_products` aggregation path.**

### Specific Client Admin license overview

Endpoint:

`GET /api/v1/client/admin/license-overview-summary?clientAdminId={clientAdminId}`

Implementation:

`ClientAdminServiceImpl.getLicenseOverviewSummary`

It reads ACTIVE `client_products` and calculates:

``` text
totalLicenses += product.licenseCount
assignedUsers += product.usedLicenseCount
availableSeats = totalLicenses - assignedUsers
```

Example:

``` text
Total licenses = 24
Assigned/used = 5
Available = 19
```

This is distinct from the global MSP-product based dashboard
distribution.

------------------------------------------------------------------------

## 15. Recommended Backend-Only Swagger/Postman Purchase Test

A full exact request body should always be generated from current DTO
definitions. The known sequence is:

``` text
1. Client signup/account details
2. Email verification
3. Password/account creation
4. Client onboarding / buy-now organization + billing + product selection
5. Registration creates PENDING ClientProduct and invoice request
6. Billing creates invoice
7. Call Billing online-payment endpoint with STRIPE
8. Receive Stripe checkoutUrl
9. Open checkoutUrl in browser
10. Complete Stripe payment
11. Stripe calls Billing webhook
12. Billing marks payment SUCCESS
13. Billing updates invoice and verifies it is fully paid
14. Billing calls Registration activate-license
15. Registration activates ClientAdmin / ClientProduct
16. Verify active purchased product/license
```

A dedicated application front end is not required to test the backend
through payment. The browser is needed only to display Stripe's hosted
Checkout UI.

------------------------------------------------------------------------

## 16. Post-Purchase Training Setup Sequence

After a purchased license is active:

``` text
1. GET /api/v1/client-product/{clientAdminId}
   → obtain/select productId

2. GET /api/v1/client-product/packages?productId={productId}
   → obtain/select purchased packageId

3. GET /api/v1/topics/product/package/topics
      ?productId={productId}
      &packageId={packageId}
      &offset=0
      &pageSize=10
      &sortBy=createdAt
      &order=desc
   → select existing topic IDs

4. POST /api/v1/sub-packages
   → create client-specific SubPackage from selected topics

5. GET /api/v1/end-user/unassigned
      ?clientAdminId={clientAdminId}
      &subPackageId={subPackageId}
   → choose eligible users

6. POST /api/v1/end-user/assign-sub-package
   → Registration validates/consumes license and assigns SubPackage

7. GET /api/v1/end-user/{userId}/sub-packages
   → verify learner assignment
```

Do not replace this with "Client Admin creates Package → creates Topic".
That is not the normal post-purchase flow.

------------------------------------------------------------------------

## 17. Runtime Infrastructure and Service Ports

The discussed Docker configuration includes Redis and multiple services.

### Redis

-   Image: `redis:7.2-alpine`
-   Default port: `6379`
-   Persistence command discussed: `redis-server --appendonly yes`

Many application services receive `REDIS_HOST`.

Important distinction:

-   Docker `depends_on` indicates container startup
    ordering/declaration.
-   It does **not** prove a specific business API makes a runtime HTTP
    call to every dependent service.
-   For flow-specific runtime requirements, inspect the actual code
    path/configuration.

### MongoDB

MongoDB is used across services with service-specific persistence
boundaries.

The Docker setup discussed uses `MONGODB_URI` configuration.

### Known service ports / Swagger URLs

CMS:

`http://localhost:5050/cms/swagger-ui.html`

Registration:

`http://localhost:9090/registration/swagger-ui.html`

Billing:

`http://localhost:6060/billing/swagger-ui.html`

Auth:

`http://localhost:9093/auth/swagger-ui.html`

Notification:

`http://localhost:5656/notification/swagger-ui.html`

Gateway:

`http://localhost:7030/swagger-ui.html`

CMS API docs:

`http://localhost:5050/cms/v3/api-docs`

### Catalog-only setup

For creating:

-   user ranges
-   Security Awareness Training product
-   Silver/Gold/Platinum/Diamond packages
-   range pricing

the main application service discussed is CMS, together with its
required infrastructure such as MongoDB and any startup-required
Redis/config dependencies.

### Full onboarding/payment flow

Depending on the exact path, expect to need:

-   Registration
-   Billing
-   Auth
-   Notification
-   CMS where onboarding/product/dashboard synchronization requires it
-   MongoDB
-   Redis
-   Stripe configuration for actual Checkout testing

Always inspect current configuration and downstream calls before
declaring the minimal runtime set.

------------------------------------------------------------------------

## 18. Trial-Specific Behavior

Known facts:

-   Product/package DTOs include trial-related fields such as `isTrial`.
-   Trial signup logic exists.
-   Trial SubPackage creation exists.
-   Trial signup can assign trial products.
-   Previously inspected trial assignment code used a trial license
    allocation and trial validity period.

### Needs Verification: Free Trial catalog row

It has **not** been conclusively verified whether the visible "Free
Trial" option in the pricing/catalog UI:

1.  is automatically seeded/default-created,
2.  is created manually through CMS catalog APIs,
3.  is synthesized by an API from trial configuration, or
4.  is represented by a normal Package with `isTrial=true`.

Before implementing or seeding Free Trial, inspect:

-   CMS startup/seed/migration code.
-   Product/Package initialization.
-   `isTrial` handling.
-   Trial package lookup logic.
-   UI/API used to load the pricing table.

Do not create duplicate Free Trial records until this is confirmed.

------------------------------------------------------------------------

## 19. Important Cross-Service Boundaries

### Registration vs CMS

Registration owns:

-   account/client lifecycle
-   Client Admin organization/billing orchestration
-   ClientProduct purchase/license records
-   End User creation
-   license validation
-   UserLicence
-   license consumption

CMS owns:

-   product/package/topic catalog
-   SubPackages
-   topic selection/mapping
-   learning/progress-side representations

### Registration vs Billing

Registration:

-   orchestrates client purchase/onboarding
-   prepares purchase/product/license state
-   asks Billing to create invoice
-   receives activation call after full payment

Billing:

-   owns invoice/payment processing
-   Stripe Checkout
-   webhook
-   payment success
-   invoice paid-state
-   activation trigger

### Billing vs Front End

Billing creates Stripe success/cancel URLs and returns Checkout URL.

Final UI navigation from `/payment-success` to the application dashboard
is a front-end concern and is intentionally outside the backend workflow
described here.

------------------------------------------------------------------------

## 20. Coding and Debugging Guidance for Claude Code

When working in this repository:

1.  **Trace controller → implementation → service → repository/entity →
    downstream client.** Do not infer behavior from endpoint names
    alone.

2.  **Respect service ownership.** Do not move license enforcement into
    CMS simply because CMS creates progress records.

3.  **Use `clientAdminId` carefully.** It is a major tenant/client
    scoping key. Any query that drops this boundary can expose
    cross-client data.

4.  **Differentiate global and client-specific license reporting.**
    Global Aspire Admin distribution and individual Client Admin
    overview use different data paths.

5.  **Do not confuse `licenseCount` with remaining seats.**
    `licenseCount` is total capacity. Remaining capacity is derived from
    usage.

6.  **Payment session creation is not license activation.** Activation
    requires successful payment and fully paid invoice processing.

7.  **Do not activate PENDING ClientProduct early.** Billing webhook →
    fully paid invoice → Registration activation is the known activation
    boundary.

8.  **Do not create catalog Topics during ordinary Client Admin training
    setup.** Client Admin selects existing package Topics and creates a
    SubPackage.

9.  **Check duplicate-license logic before modifying seat consumption.**
    Business intent is user-seat based.

10. **Do not assume Docker `depends_on` equals flow dependency.** Check
    actual runtime calls and startup configuration.

11. **For Swagger/Postman examples, inspect DTO validation first.**
    Required annotations may produce non-obvious request requirements.

12. **When changing payment code, preserve idempotency.** Stripe can
    deliver multiple related events such as Checkout completion and
    PaymentIntent success. Inspect existing safeguards before changing
    status/activation logic.

13. **When changing Mongo documents, inspect collection ownership.**
    Similar data can exist as replicas in another service; do not treat
    a replica as the authoritative write model without checking code.

------------------------------------------------------------------------

## 21. Known / Discussed Important APIs

### Auth

``` text
POST /api/v1/auth/login
POST /api/v1/auth/mfa/generate-otp
POST /api/v1/auth/mfa/verify-otp
POST /api/v1/auth/refresh
```

### Registration --- Client

``` text
POST /api/v1/client/admin/onboard
POST /api/v1/client/admin/buy-now
PUT  /api/v1/client/admin/activate-license/{clientId}

GET  /api/v1/client/admin/products/assigned/{clientAdminId}
GET  /api/v1/client/admin/product/package/detail/{id}
GET  /api/v1/client/admin/license-statistics
GET  /api/v1/client/admin/license-overview-summary
```

Check whether `clientAdminId` is a required query parameter for the
current version of the overview/statistics endpoints.

### Registration --- End User

``` text
POST /api/v1/end-user
GET  /api/v1/end-user/{userId}
PUT  /api/v1/end-user
GET  /api/v1/end-user
GET  /api/v1/end-user/users
GET  /api/v1/end-user/unassigned
POST /api/v1/end-user/assign-sub-package
GET  /api/v1/end-user/{userId}/sub-packages
```

### Billing

``` text
POST /api/v1/payment/online-payment-entry
POST /api/v1/payment/stripe/webhook/payment
```

Invoice creation is delegated to Billing through its invoice-create
endpoint/client. Confirm current controller prefix when calling it
directly.

### CMS --- Client Products

``` text
GET /api/v1/client-product/{clientAdminId}
GET /api/v1/client-product/packages?productId={productId}
```

### CMS --- Topics

``` text
GET  /api/v1/topics/product/package/topics
POST /api/v1/topics
```

Remember: `POST /topics` is platform catalog administration, not
ordinary Client Admin training setup.

### CMS --- SubPackages

``` text
POST   /api/v1/sub-packages
POST   /api/v1/sub-packages/trial
GET    /api/v1/sub-packages/{id}
GET    /api/v1/sub-packages/{id}/assigned-users
GET    /api/v1/sub-packages
PUT    /api/v1/sub-packages/{id}
DELETE /api/v1/sub-packages/{id}
```

### CMS --- Catalog

``` text
POST   /api/v1/user-ranges
GET    /api/v1/user-ranges
PUT    /api/v1/user-ranges/{id}
DELETE /api/v1/user-ranges/{id}

POST   /api/v1/products
GET    /api/v1/products
GET    /api/v1/products/{id}
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}

POST   /api/v1/package-range-pricing/bulk
GET    /api/v1/package-range-pricing/package/{packageId}
GET    /api/v1/package-range-pricing/package/{packageId}/range
PUT    /api/v1/package-range-pricing/{id}
DELETE /api/v1/package-range-pricing/{id}
```

### Dashboard

``` text
GET /api/v1/dashboard/license-distribution
```

------------------------------------------------------------------------

## 22. End-to-End Business Flow Summary

``` text
PLATFORM CATALOG SETUP
Aspire Admin
   ↓
User Ranges
   ↓
Product: Security Awareness Training
   ↓
Packages: Silver / Gold / Platinum / Diamond
   ↓
Package + User Range Pricing
   ↓

CLIENT ACQUISITION
Client signs up
   ↓
Email OTP verification
   ↓
Password/account creation
   ↓
Client Admin / default MSP association
   ↓

CLIENT PURCHASE / ONBOARDING
Organization information
   +
Billing information
   +
Product / Package / User Range / License selection
   ↓
Registration processBuyNow
   ↓
ClientProduct = PENDING
   ↓
Invoice created in Billing
   ↓
Online payment entry
   ↓
Stripe Checkout URL
   ↓
Browser completes Stripe payment
   ↓
Stripe webhook
   ↓
Payment = SUCCESS
   ↓
Invoice fully PAID?
   ↓ yes
Billing → Registration activate-license
   ↓
ClientAdmin = ACTIVE
ClientProduct = ACTIVE
   ↓

TRAINING CONFIGURATION
Client Admin selects purchased Product
   ↓
Selects purchased Package
   ↓
Loads existing Topics for Product + Package
   ↓
Selects Topics
   ↓
Creates SubPackage
   ↓
Creates/selects End Users
   ↓
Assigns SubPackage
   ↓
Registration validates license availability
   ↓
UserLicence created
ClientProduct.usedLicenseCount updated
   ↓
CMS creates learning/progress representation
   ↓

LICENSE REPORTING
Client-specific:
client_products
licenseCount - usedLicenseCount
   ↓
Aspire Admin can inspect client license overview

Global:
msp_products
   ↓
Aspire Admin license-distribution dashboard
```

------------------------------------------------------------------------

## 23. Needs Verification

The following should be verified directly against current source before
implementation decisions:

1.  **Free Trial catalog creation**
    -   automatic seed vs manual package vs generated trial option.
2.  **Exact current request bodies**
    -   `BuyNowRequestDto`
    -   `BillingInfoDto`
    -   `ProductSelectionDto`
    -   `InvoiceDetailsDto`
    -   `SubPackageRequestDto`
    -   `SubPackageAssignRequest`
    -   payment request DTOs.
3.  **Exact Mongo collection names**
    -   Verify every `@Document(collection=...)` rather than assuming
        entity-name-to-collection mapping.
4.  **Same user assigned to multiple SubPackages**
    -   Business intent is one user seat, but inspect exact
        existing-license checks for product/package boundaries and edge
        cases.
5.  **Unassign/deactivate/delete behavior**
    -   Verify when and how `usedLicenseCount` decreases and whether
        `UserLicence` is deactivated/deleted.
6.  **Refund/payment reversal**
    -   Verify whether refunds or failed post-payment states deactivate
        licenses.
7.  **Webhook idempotency**
    -   Verify duplicate Stripe event handling and whether both
        `checkout.session.completed` and `payment_intent.succeeded` can
        trigger the same activation path safely.
8.  **Notification failure semantics**
    -   Determine which notification calls are best-effort and which are
        transaction-critical.
9.  **Redis requirement per flow**
    -   Redis is part of the runtime infrastructure, but inspect
        Registration/Auth/common configuration to determine exactly
        which startup/runtime features require it.
10. **Aspire Admin creation/onboarding**
    -   Separate platform-admin creation flow was not fully traced.
11. **Frontend dashboard redirect**
    -   Intentionally outside backend scope; backend only supplies
        Stripe success/cancel URLs.
12. **Phishing and other product-specific workflows**
    -   The wider platform contains Phishing and other security modules,
        but their detailed business/API flows were not exhaustively
        analyzed in this discussion.

------------------------------------------------------------------------

## 24. Working Rule for Future Claude Sessions

Before modifying a business-critical flow, Claude should:

``` text
1. Locate the exact controller endpoint.
2. Open the request/response DTOs.
3. Trace the service implementation.
4. Identify authoritative repository/entity writes.
5. Identify cross-service calls.
6. Check tenant/client scoping.
7. Check status transitions.
8. Check duplicate/idempotency behavior.
9. Check license implications.
10. Check tests and update/add tests.
```

If this file and current code disagree, **use current code and update
this document if appropriate**.

Do not convert a "Needs Verification" item into a fact without source
evidence.

------------------------------------------------------------------------

## 25. Quick Mental Model

For coding purposes, remember Aspire as four connected cores:

``` text
AUTH
Who is the user and can they access the operation?

REGISTRATION
Who is the client/user, what did the client buy,
and how many licensed users can be assigned?

BILLING
Was the invoice actually paid, and when can the
purchased license become active?

CMS
What training/catalog content exists, which topics
are grouped into the client's SubPackage, and what
learning/progress data is created?
```

The most important end-to-end invariant is:

> **A Client Admin should only consume active purchased capacity, and a
> user assignment should only become learning/progress state after
> license validation succeeds.**
