# Local Home Module Login Setup

This note documents the local setup used to make the home frontend login successfully with:

```text
username: superadmin01@yopmail.com
password: 123456789
```

## Frontend Call Flow

Frontend path:

```text
C:\Git-Repo\aspire\ASAT-V2-FRONTEND\home-module
```

Login page:

```text
src/pages/auth/Login.tsx
```

The login API endpoint is defined in:

```text
src/routes/APIEndpoints.ts
LOGIN: /auth/api/v1/auth/login
```

The frontend base URL comes from:

```text
src/utils/Constants.ts
BASE_URL = import.meta.env.VITE_API_BASE_URL
```

For local login, `home-module/.env` must point to the local gateway:

```env
VITE_API_BASE_URL=http://localhost:7030/gateway
```

So the browser calls:

```text
http://localhost:7030/gateway/auth/api/v1/auth/login
```

Gateway rewrites that to the auth service:

```text
http://localhost:9093/auth/api/v1/auth/login
```

## Backend Services Needed

For login plus the first dashboard render, run these services:

```text
Redis         : 6379
gateway       : 7030
auth          : 9093
registration  : 9090
cms           : 5050
notification  : 5656
phishing      : 5002
```

Minimum for login only:

```text
Redis
auth
gateway
```

Minimum for login and no-spinner dashboard shell:

```text
Redis
auth
registration
gateway
```

Recommended for the visible home dashboard:

```text
Redis
auth
registration
cms
notification
phishing
gateway
```

Other pages may need more services:

```text
billing        : 6060
universal      : 9099
breach         : 6080
```

## Backend Modifications Made

### Auth Service

File:

```text
services/auth/core/src/main/java/com/aspire/asat/auth/service/AccessTokenService.java
```

Changes:

- Added a local-profile-only fallback password for the seeded superadmin user.
- Default local fallback credentials:

```text
auth.local-superadmin.email=superadmin01@yopmail.com
auth.local-superadmin.password=123456789
```

- Added fallback role names from `aspireUser.userType` when the DB role lookup returns an empty list.

Reason:

- The DB user existed, but the password did not match `123456789`.
- The login response originally had `roleNames: []`, and the frontend depends on a role name to route/render correctly.

### Registration Service

File:

```text
services/registration/core/src/main/java/com/aspire/asat/registration/service/impl/RolePermissionServiceImpl.java
```

Changes:

- Fixed the superadmin role-permission response when `aspireUser.roles` is empty.
- Previously the code did:

```java
aspireUser.getRoles().get(0)
```

- That caused:

```text
Index 0 out of bounds for length 0
```

- Now it falls back to the `SUPER_ADMIN` role id from the role repository, or the role name if the role record is not available.

Reason:

- After login, the frontend calls:

```text
/registration/api/v1/role-permissions/superadmin01@yopmail.com
```

- Without this fix, registration returned `400`, and the dashboard stayed on the loader.

### Gateway Service

File:

```text
services/gateway/service/src/main/resources/application-local.yml
```

Important local route changes:

```yaml
auth route -> ${AUTH_SERVICE_URL:http://localhost:9093}
registration route -> ${REGISTRATION_SERVICE_URL:http://localhost:9090}
```

Reason:

- The local gateway config had Docker service names like `asat-auth-service`.
- When running Gradle services directly on Windows, gateway must route to `localhost`.

### Home Frontend

File:

```text
C:\Git-Repo\aspire\ASAT-V2-FRONTEND\home-module\.env
```

Change:

```env
VITE_API_BASE_URL=http://localhost:7030/gateway
```

File:

```text
C:\Git-Repo\aspire\ASAT-V2-FRONTEND\home-module\src\providers\StoreProvider.tsx
```

Change:

- Made branding handling null-safe:

```ts
companyName: response.data?.companyName || 'Aspire Tech'
logoFilePath: response.data?.logoFilePath || ''
```

Reason:

- Registration branding API can return a successful response with `data: null`.
- The old code tried to read `response.data.companyName`, causing the app shell to fail after login.

## Run Commands

Run backend commands from:

```powershell
cd C:\Git-Repo\aspire\ASAT-V2-BACKEND
```

Common local environment:

```powershell
$env:SPRING_PROFILES_ACTIVE='local'
$env:MONGODB_URI='mongodb://admin:P4ssw0rd9x2016@15.204.246.10:28395/?authSource=admin'
$env:REDIS_HOST='127.0.0.1'
```

Start auth:

```powershell
.\gradlew.bat :services:auth:service:bootRun
```

Start registration:

```powershell
.\gradlew.bat :services:registration:service:bootRun
```

Start CMS:

```powershell
$env:BILLING_SERVICE_URL='http://localhost:6060/billing/api/v1'
$env:PHISHING_SERVICE_URL='http://localhost:5002/phishing/api/v1'
.\gradlew.bat :services:cms:service:bootRun
```

Start notification:

```powershell
.\gradlew.bat :services:notification:service:bootRun
```

Start phishing:

```powershell
$env:REGISTRATION_SERVICE_URL='http://localhost:9090/registration/api/v1'
$env:NOTIFICATION_SERVICE_URL='http://localhost:5656'
.\gradlew.bat :services:phishing:service:bootRun
```

Start gateway:

```powershell
$env:AUTH_SERVICE_URL='http://localhost:9093'
$env:REGISTRATION_SERVICE_URL='http://localhost:9090'
$env:AC_ALLOW_ORIGINS='http://172.28.160.1:5010,http://localhost:5010,http://127.0.0.1:5010'
.\gradlew.bat :services:gateway:service:bootRun
```

If you want to run gateway only with the local routes needed for home login/dashboard, use this override:

```powershell
$env:SPRING_APPLICATION_JSON='{"spring":{"cloud":{"gateway":{"routes":[{"id":"local-auth","uri":"http://localhost:9093","predicates":["Path=/gateway/auth/**"],"filters":[{"name":"RewritePath","args":{"regexp":"/gateway/(?<segment>.*)","replacement":"/${segment}"}}]},{"id":"local-registration","uri":"http://localhost:9090","predicates":["Path=/gateway/registration/**"],"filters":[{"name":"RewritePath","args":{"regexp":"/gateway/(?<segment>.*)","replacement":"/${segment}"}}]},{"id":"local-cms","uri":"http://localhost:5050","predicates":["Path=/gateway/cms/**"],"filters":[{"name":"RewritePath","args":{"regexp":"/gateway/(?<segment>.*)","replacement":"/${segment}"}}]},{"id":"local-notification","uri":"http://localhost:5656","predicates":["Path=/gateway/notification/**"],"filters":[{"name":"RewritePath","args":{"regexp":"/gateway/(?<segment>.*)","replacement":"/${segment}"}}]},{"id":"local-phishing","uri":"http://localhost:5002","predicates":["Path=/gateway/phishing/**"],"filters":[{"name":"RewritePath","args":{"regexp":"/gateway/(?<segment>.*)","replacement":"/${segment}"}}]}]}}}}'
.\gradlew.bat :services:gateway:service:bootRun
```

Run frontend from:

```powershell
cd C:\Git-Repo\aspire\ASAT-V2-FRONTEND\home-module
```

Then:

```powershell
npm install
npm run dev
```

Open:

```text
http://localhost:5010/auth/login
```

## Verify Running Ports

```powershell
Get-NetTCPConnection -LocalPort 5002,5010,5050,5656,7030,9090,9093 -ErrorAction SilentlyContinue |
  Where-Object State -eq Listen |
  Select-Object LocalPort,OwningProcess |
  Sort-Object LocalPort
```

Expected:

```text
5002  phishing
5010  home frontend
5050  cms
5656  notification
7030  gateway
9090  registration
9093  auth
```

## Successful Verification

Verified login through the actual frontend:

```text
http://localhost:5010/auth/login
```

Credentials:

```text
superadmin01@yopmail.com / 123456789
```

Observed results:

```text
POST /gateway/auth/api/v1/auth/login                                 200
GET  /gateway/auth/api/v1/auth/user-details                          200
GET  /gateway/registration/api/v1/branding                           200
GET  /gateway/registration/api/v1/role-permissions/superadmin...      200
GET  /gateway/registration/api/v1/dropdown/countries                  200
GET  /gateway/registration/api/v1/dashboard/user-status-counts        200
GET  /gateway/registration/api/v1/msp?offset=0&pageSize=4             200
GET  /gateway/registration/api/v1/client/admin/list?offset=0...       200
```

Dashboard rendered with:

```text
Super Admin
Dashboard
...
```

Spinner count:

```text
0
```
