# Bulk User Onboarding API (Frontend)

Session-based bulk import flow for client admins. Upload and validate a CSV/Excel file, review/edit invalid users, then onboard.

**Base path:** `/api/v1/end-user`

**Auth:** Same as other end-user APIs (gateway JWT + permissions).

**Important:** Do **not** use the legacy `POST /api/v1/end-user/import` for this UI flow. That endpoint still creates users immediately in one shot.

---

## Flow overview

```
Step 1  POST /bulk-import/validate
           → upload file, get importSessionId + valid/invalid pages
Step 2  GET  /bulk-import/{sessionId}/users
           → paginate valid or invalid lists
Step 3  PUT  /bulk-import/{sessionId}/users
           → fix invalid rows, revalidate
Step 4  POST /bulk-import/{sessionId}/onboard
           → create all currently valid users (sync)
```

- Session is stored in MongoDB and expires after **2 hours**.
- After Step 4 succeeds, the session is marked `COMPLETED` and cannot be reused.
- If the session expires or is missing → show re-upload UI.

### CSV / Excel columns (order-based)

Header row is skipped. Column order:

| Index | Field | Required |
|------:|-------|----------|
| 0 | firstName | Yes |
| 1 | lastName | No |
| 2 | email | Yes |
| 3 | phoneNumber | Yes |
| 4 | department | No |
| 5 | countryCode | No |

Accepted file types: `.csv`, `.xls`, `.xlsx`

### Failure reason codes (tooltip / status)

| Code | Meaning |
|------|---------|
| `invalid_email_format` | Email format is invalid |
| `invalid_domain` | Email domain does not match client admin company domain |
| `duplicate_email` | Same email appears more than once in this file/session |
| `email_already_exists` | Email already exists in the system |
| `missing_required_field` | Missing firstName, email, or phoneNumber |
| `creation_failed` | Create failed during onboard (Step 4) |

### Pagination

| Param | Default | Max | Notes |
|-------|---------|-----|-------|
| `offset` | `0` | — | Number of items to **skip** (0, 10, 20…) |
| `pageSize` | `10` | `100` | Page size |

Envelope pagination object:

```json
{
  "offset": 0,
  "pageSize": 10,
  "total": 24,
  "items": []
}
```

---

## Step 1 — Validate file

Upload and validate only. **Does not create users.**

```http
POST /api/v1/end-user/bulk-import/validate
Content-Type: multipart/form-data
```

### Form fields

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `file` | file | Yes | CSV / XLS / XLSX |
| `clientAdminId` | string | Yes | Client admin ID |
| `offset` | int | No | Default `0` |
| `pageSize` | int | No | Default `10` |

### Sample response `200`

```json
{
  "message": "File uploaded successfully and validated.",
  "statusCode": 200,
  "data": {
    "importSessionId": "66f1a2b3c4d5e6f789012345",
    "totalValid": 24,
    "totalInvalid": 6,
    "validUsers": {
      "offset": 0,
      "pageSize": 10,
      "total": 24,
      "items": [
        {
          "rowIndex": 1,
          "firstName": "John",
          "lastName": "Doe",
          "fullName": "John Doe",
          "email": "john.doe@company.com",
          "phoneNumber": "1234567890",
          "countryCode": "US",
          "department": "IT"
        },
        {
          "rowIndex": 2,
          "firstName": "Jane",
          "lastName": "Smith",
          "fullName": "Jane Smith",
          "email": "jane.smith@company.com",
          "phoneNumber": "9876543210",
          "countryCode": "US",
          "department": "HR"
        }
      ]
    },
    "invalidUsers": {
      "offset": 0,
      "pageSize": 10,
      "total": 6,
      "items": [
        {
          "rowIndex": 5,
          "firstName": "Bad",
          "lastName": "Email",
          "fullName": "Bad Email",
          "email": "not-an-email",
          "phoneNumber": "1111111111",
          "countryCode": "US",
          "department": "IT",
          "failureReason": "invalid_email_format"
        },
        {
          "rowIndex": 8,
          "firstName": "Dup",
          "lastName": "User",
          "fullName": "Dup User",
          "email": "exists@company.com",
          "phoneNumber": "2222222222",
          "countryCode": "US",
          "department": "Sales",
          "failureReason": "email_already_exists"
        },
        {
          "rowIndex": 12,
          "firstName": "Other",
          "lastName": "Domain",
          "fullName": "Other Domain",
          "email": "user@other.com",
          "phoneNumber": "3333333333",
          "countryCode": "US",
          "department": "Finance",
          "failureReason": "invalid_domain"
        }
      ]
    }
  }
}
```

### FE notes (Step 1)

- Store `importSessionId` for Steps 2–4.
- Use `totalValid` / `totalInvalid` for the summary cards.
- `failureReason` is only present on invalid rows (`null`/omitted on valid rows).
- Show tooltip text from `failureReason` (map codes to user-friendly labels).

---

## Step 2 — List users (paginate)

```http
GET /api/v1/end-user/bulk-import/{sessionId}/users?status=VALID&offset=0&pageSize=10
```

### Path params

| Name | Type | Required |
|------|------|----------|
| `sessionId` | string | Yes (`importSessionId` from Step 1) |

### Query params

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| `status` | enum | Yes | — | `VALID` or `INVALID` |
| `offset` | int | No | `0` | Skip count |
| `pageSize` | int | No | `10` | Page size |

### Sample response `200` (`status=INVALID`)

```json
{
  "message": "Bulk import users retrieved successfully",
  "statusCode": 200,
  "data": {
    "offset": 0,
    "pageSize": 10,
    "total": 6,
    "items": [
      {
        "rowIndex": 5,
        "firstName": "Bad",
        "lastName": "Email",
        "fullName": "Bad Email",
        "email": "not-an-email",
        "phoneNumber": "1111111111",
        "countryCode": "US",
        "department": "IT",
        "failureReason": "invalid_email_format"
      },
      {
        "rowIndex": 9,
        "firstName": "Ann",
        "lastName": "Lee",
        "fullName": "Ann Lee",
        "email": "ann.lee@company.com",
        "phoneNumber": "4444444444",
        "countryCode": "US",
        "department": "IT",
        "failureReason": "duplicate_email"
      }
    ]
  }
}
```

### Sample response `200` (`status=VALID`)

```json
{
  "message": "Bulk import users retrieved successfully",
  "statusCode": 200,
  "data": {
    "offset": 10,
    "pageSize": 10,
    "total": 24,
    "items": [
      {
        "rowIndex": 15,
        "firstName": "Sam",
        "lastName": "Wilson",
        "fullName": "Sam Wilson",
        "email": "sam.wilson@company.com",
        "phoneNumber": "5555555555",
        "countryCode": "US",
        "department": "Ops"
      }
    ]
  }
}
```

---

## Step 3 — Update / fix invalid users

Send only the rows the admin edited. Identify each row by `rowIndex`.

```http
PUT /api/v1/end-user/bulk-import/{sessionId}/users?offset=0&pageSize=10
Content-Type: application/json
```

### Path params

| Name | Type | Required |
|------|------|----------|
| `sessionId` | string | Yes |

### Query params

| Name | Default | Description |
|------|---------|-------------|
| `offset` | `0` | Skip for returned pages |
| `pageSize` | `10` | Page size for returned pages |

### Sample request body

```json
{
  "users": [
    {
      "rowIndex": 5,
      "firstName": "Bad",
      "lastName": "Email",
      "email": "fixed.user@company.com",
      "phoneNumber": "1111111111",
      "countryCode": "US",
      "department": "IT"
    },
    {
      "rowIndex": 12,
      "firstName": "Other",
      "lastName": "Domain",
      "email": "other.fixed@company.com",
      "phoneNumber": "3333333333",
      "countryCode": "US",
      "department": "Finance"
    }
  ]
}
```

Only include fields you are changing (plus required `rowIndex`). Unsent fields keep previous session values.

### Sample response `200`

Same shape as Step 1 `data` (updated totals + refreshed pages):

```json
{
  "message": "Bulk import users updated successfully",
  "statusCode": 200,
  "data": {
    "importSessionId": "66f1a2b3c4d5e6f789012345",
    "totalValid": 26,
    "totalInvalid": 4,
    "validUsers": {
      "offset": 0,
      "pageSize": 10,
      "total": 26,
      "items": [
        {
          "rowIndex": 5,
          "firstName": "Bad",
          "lastName": "Email",
          "fullName": "Bad Email",
          "email": "fixed.user@company.com",
          "phoneNumber": "1111111111",
          "countryCode": "US",
          "department": "IT"
        }
      ]
    },
    "invalidUsers": {
      "offset": 0,
      "pageSize": 10,
      "total": 4,
      "items": [
        {
          "rowIndex": 8,
          "firstName": "Dup",
          "lastName": "User",
          "fullName": "Dup User",
          "email": "exists@company.com",
          "phoneNumber": "2222222222",
          "countryCode": "US",
          "department": "Sales",
          "failureReason": "email_already_exists"
        }
      ]
    }
  }
}
```

### FE notes (Step 3)

- Map to UI **Save Changes**.
- After save, refresh both tables from response totals/pages (or re-call Step 2).
- Rows that become valid no longer have `failureReason`.

---

## Step 4 — Onboard users

Synchronous. Creates all **currently valid** session users. Remaining invalid rows are counted as failed. Per-user create errors are skipped and included in `failedUsers`.

```http
POST /api/v1/end-user/bulk-import/{sessionId}/onboard
```

### Path params

| Name | Type | Required |
|------|------|----------|
| `sessionId` | string | Yes |

### Sample response `200`

```json
{
  "message": "Bulk import completed",
  "statusCode": 200,
  "data": {
    "totalUsers": 30,
    "successful": 26,
    "failed": 4,
    "failedUsers": [
      {
        "email": "exists@company.com",
        "fullName": "Dup User",
        "reason": "email_already_exists"
      },
      {
        "email": "ann.lee@company.com",
        "fullName": "Ann Lee",
        "reason": "duplicate_email"
      },
      {
        "email": "broken@company.com",
        "fullName": "Broken User",
        "reason": "creation_failed"
      },
      {
        "email": "still-bad",
        "fullName": "Still Bad",
        "reason": "invalid_email_format"
      }
    ]
  }
}
```

### FE notes (Step 4)

- Show progress UI while the request is in flight (no progress polling API).
- Use `successful` / `failed` / `failedUsers` for the completion summary screen.
- Backend also sends the existing bulk-import summary notification to the client admin (email + in-app).
- After success, session cannot be reused — start over with Step 1 for another import.

---

## Error examples

### Empty / unsupported file (Step 1)

```json
{
  "message": "Import file is empty",
  "statusCode": 400,
  "data": null
}
```

```json
{
  "message": "Only CSV and Excel files (.csv, .xls, .xlsx) are supported",
  "statusCode": 400,
  "data": null
}
```

### Session expired / completed

```json
{
  "message": "Import session has expired. Please re-upload the file.",
  "statusCode": 400,
  "data": null
}
```

```json
{
  "message": "Import session is no longer available. Please re-upload the file.",
  "statusCode": 400,
  "data": null
}
```

### Session not found

```json
{
  "message": "Import session not found",
  "statusCode": 404,
  "data": null
}
```

> Exact error envelope may vary slightly based on the registration global exception handler; always surface `message` to the admin.

---

## Suggested UI mapping

| UI screen | API |
|-----------|-----|
| Upload CSV | Step 1 `POST .../validate` |
| Validation summary cards | Step 1 `totalValid` / `totalInvalid` |
| Review tables + pagination | Step 2 `GET .../users` |
| Edit invalid users → Save Changes | Step 3 `PUT .../users` |
| Onboard Users (progress) | Step 4 `POST .../onboard` (wait for response) |
| Completion summary | Step 4 `successful` / `failed` / `failedUsers` |

---

## Suggested FE label map for `failureReason`

| Code | Suggested UI label |
|------|--------------------|
| `invalid_email_format` | Invalid email format |
| `invalid_domain` | Email domain must match company domain |
| `duplicate_email` | Duplicate email |
| `email_already_exists` | Email already exists |
| `missing_required_field` | Missing required field |
| `creation_failed` | Failed to create user |
