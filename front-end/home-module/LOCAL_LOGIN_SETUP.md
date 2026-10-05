# Home Module Local Login Setup

This note documents the frontend changes needed to run `home-module` locally and login through the local backend gateway.

## Local Frontend Path

```text
C:\Git-Repo\aspire\ASAT-V2-FRONTEND\home-module
```

## Required Frontend `.env`

File:

```text
home-module/.env
```

For local development, use local module URLs and local gateway:

```env
VITE_PUBLIC_URL=http://localhost:5010
VITE_CONTENT_MODULE_URL=http://localhost:5030
VITE_PHISHING_MODULE_URL=http://localhost:5070
VITE_TINYMCE_URL=http://localhost:5000

VITE_API_BASE_URL=http://localhost:7030/gateway
VITE_STORAGE_SECRET=U2FsdGVkX1+yskVjqoe803ZhO0eGB2Zlkw5r6RIu/ztlKbQfJMc+qA1WADFe
VITE_APP_VERSION=1.0.0
```

The important line is:

```env
VITE_API_BASE_URL=http://localhost:7030/gateway
```

Without this, the frontend calls the remote dev gateway instead of your local backend.

## Login API Flow

Login page:

```text
src/pages/auth/Login.tsx
```

Login endpoint constant:

```text
src/routes/APIEndpoints.ts
LOGIN: /auth/api/v1/auth/login
```

Base URL:

```text
src/utils/Constants.ts
BASE_URL = import.meta.env.VITE_API_BASE_URL
```

Final local browser request:

```text
http://localhost:7030/gateway/auth/api/v1/auth/login
```

Gateway forwards it to:

```text
http://localhost:9093/auth/api/v1/auth/login
```

## Frontend Code Change Made

File:

```text
src/providers/StoreProvider.tsx
```

Change made:

```ts
setBrandingInfo({
  companyName: response.data?.companyName || 'Aspire Tech',
  logoFilePath: response.data?.logoFilePath || '',
});
```

Reason:

The registration branding API can return:

```json
{
  "message": "...",
  "statusCode": 200,
  "data": null
}
```

The old code used:

```ts
response.data.companyName
```

That caused a frontend runtime error after login and the page stayed on the spinner. The null-safe version lets the dashboard load with the default branding.

## Backend Services Required By Home Module

For login and initial dashboard render, these backend services should be running:

```text
gateway       : http://localhost:7030
auth          : http://localhost:9093
registration  : http://localhost:9090
cms           : http://localhost:5050
notification  : http://localhost:5656
phishing      : http://localhost:5002
redis         : localhost:6379
```

Minimum for login only:

```text
gateway
auth
redis
```

Minimum to avoid the post-login spinner:

```text
gateway
auth
registration
redis
```

Recommended for the dashboard:

```text
gateway
auth
registration
cms
notification
phishing
redis
```

## Run Frontend

From:

```powershell
cd C:\Git-Repo\aspire\ASAT-V2-FRONTEND\home-module
```

Install dependencies if needed:

```powershell
npm install
```

Start:

```powershell
npm run dev
```

Open:

```text
http://localhost:5010/auth/login
```

Use:

```text
username: superadmin01@yopmail.com
password: 123456789
```

## Expected Successful Requests

After login, the frontend should receive `200` responses for:

```text
POST /gateway/auth/api/v1/auth/login
GET  /gateway/auth/api/v1/auth/user-details
GET  /gateway/registration/api/v1/branding
GET  /gateway/registration/api/v1/role-permissions/superadmin01@yopmail.com
GET  /gateway/registration/api/v1/dropdown/countries
GET  /gateway/registration/api/v1/dashboard/user-status-counts
GET  /gateway/registration/api/v1/msp?offset=0&pageSize=4
GET  /gateway/registration/api/v1/client/admin/list?offset=0&pageSize=4
```

Expected UI:

```text
Super Admin
Dashboard
```

The full-screen loader should disappear.
