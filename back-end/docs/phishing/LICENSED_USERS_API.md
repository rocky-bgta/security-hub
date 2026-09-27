# Licensed Users APIs (Frontend)

Campaign-scoped APIs that list and count users already allocated in `phishing_user_licence` for a campaign’s `clientAdminId` + `productPackageId`.

## FE notes

- Auth: same as other phishing campaign APIs (client from token).
- Scope: reads **only** `phishing_user_licence` for this campaign’s package assignment.
- **Active only:** list, department-counts, and group-counts return rows with `active: true`. Inactive licensed users still occupy seats (Add-licence exclude / allocate unchanged).
- List item `id` = Registration `userId` (not the licence document `_id`).
- Pagination: `offset` is a **page index** → skip = `offset * pageSize` (same as Registration `/end-user`).
- Fields not stored on licence rows are always `null`: `lastLoginAt`, `profilePicture`, `phoneCode`, `countryCode`.
- `riskGroup` is snapshotted from Registration at **allocate-licence** time (users licensed before this change may have `null`).
- Add-licence modal still uses existing Registration GETs unchanged.
- Department catalog for filters still comes from Registration (`GET /departments?active=true...`).

Base path: `/api/v1/phishing/campaigns`

---

## 1. List licensed users

```http
GET /api/v1/phishing/campaigns/{campaignId}/licensed-users
```

### Path params

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `campaignId` | string | Yes | Campaign ID |

### Query params

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| `search` | string | No | — | Case-insensitive match on `firstName`, `lastName`, `email` |
| `departments` | string[] | No | — | Repeatable; match `departmentName` |
| `riskGroup` | enum[] | No | — | Repeatable; match snapshot `riskGroup` (`HIGH_RISK`, `MEDIUM_RISK`, `LOW_RISK`, `CRITICAL_RISK`) |
| `offset` | int | No | `0` | Page index (not raw skip) |
| `pageSize` | int | No | `10` | Page size |

### Example request

```http
GET /api/v1/phishing/campaigns/{campaignId}/licensed-users?search=alice&departments=HR&departments=IT&riskGroup=HIGH_RISK&riskGroup=CRITICAL_RISK&offset=0&pageSize=10
```

### Success response (`ApiResponseDto`)

```json
{
  "message": "Licensed users retrieved successfully",
  "statusCode": 200,
  "data": {
    "offset": 0,
    "pageSize": 10,
    "total": 25,
    "items": [
      {
        "id": "user-123",
        "firstName": "Alice",
        "lastName": "Smith",
        "fullName": "Alice Smith",
        "email": "alice@example.com",
        "phoneNumber": "+15551234567",
        "phoneCode": null,
        "department": "HR",
        "organizationName": "Acme",
        "organizationDomain": "acme.com",
        "countryName": "United States",
        "countryCode": null,
        "status": "ACTIVE",
        "clientAdminId": "client-admin-1",
        "riskGroup": "HIGH_RISK",
        "lastLoginAt": null,
        "profilePicture": null,
        "isRiskProfileExist": false
      }
    ]
  }
}
```

### List item field mapping

| Field | Source / notes |
|-------|----------------|
| `id` | `userId` |
| `firstName` | snapshot |
| `lastName` | snapshot |
| `fullName` | derived from first + last |
| `email` | snapshot |
| `phoneNumber` | snapshot |
| `department` | `departmentName` |
| `organizationName` | snapshot |
| `organizationDomain` | snapshot |
| `countryName` | snapshot |
| `clientAdminId` | snapshot |
| `isRiskProfileExist` | snapshot |
| `status` | `"ACTIVE"` if `active=true`, else `"INACTIVE"` |
| `phoneCode` | always `null` |
| `countryCode` | always `null` |
| `riskGroup` | snapshot from Registration `AspireUser.riskGroup` at allocate time; `null` for legacy rows |
| `lastLoginAt` | always `null` |
| `profilePicture` | always `null` |

---

## 2. Department counts

```http
GET /api/v1/phishing/campaigns/{campaignId}/licensed-users/department-counts
```

### Path params

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `campaignId` | string | Yes | Campaign ID |

### Success response (`ApiResponseDto`)

```json
{
  "message": "Licensed user department counts retrieved successfully",
  "statusCode": 200,
  "data": [
    { "departmentName": "HR", "userCount": 12 },
    { "departmentName": "IT", "userCount": 8 },
    { "departmentName": "", "userCount": 1 }
  ]
}
```

Blank/null department may appear as one bucket with `departmentName: ""`.

---

## 3. Risk group counts

```http
GET /api/v1/phishing/campaigns/{campaignId}/licensed-users/group-counts
```

Same response shape as Registration `GET /api/v1/departments/risk-group-user-counts`.

### Path params

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `campaignId` | string | Yes | Campaign ID |

### Success response (`ApiResponseDto`)

```json
{
  "message": "Risk group user counts retrieved successfully",
  "statusCode": 200,
  "data": [
    { "riskGroup": "MEDIUM_RISK", "userCount": 3 },
    { "riskGroup": "LOW_RISK", "userCount": 1 }
  ]
}
```

Counts group snapshotted `riskGroup` on licence rows (`HIGH_RISK`, `MEDIUM_RISK`, `LOW_RISK`, `CRITICAL_RISK`). Legacy rows without a snapshot may appear as one bucket with `"riskGroup": null`.

Bind chip `riskGroup` to list filter: `GET .../licensed-users?riskGroup=HIGH_RISK`.

---

## Errors

| Case | Typical behavior |
|------|------------------|
| Campaign not found / wrong client | Not found (404-style); `data` is `null` |
| Campaign missing `productPackageId` | Validation error; `data` is `null` |

---

## Related (unchanged)

| Use case | API |
|----------|-----|
| Add-licence modal user list | `GET /api/v1/end-user?productPackageId={productPackageId}` (Registration; excludes licensed users for that package) |
| Department catalog / filters | Existing Registration departments API |
| Allocate seats | `POST /api/v1/phishing/campaigns/{campaignId}/allocate-licence` |
| Persist campaign audience | `PUT /api/v1/phishing/campaigns/{campaignId}/step/6` |
