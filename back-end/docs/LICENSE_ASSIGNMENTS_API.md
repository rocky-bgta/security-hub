# License Assignments API (Frontend)

Paginated list of **user × product** license assignments for a client organization. Powers the License Assignments table (search, Package, Product, Department, Status, assigned-date range).

**Base path:** `/api/v1/client/license-assignments`

**Auth:** Same as other CMS client APIs (gateway JWT + permissions).  
CLIENT_ADMIN is always scoped to their org. MSP users are scoped to `clientAdminIds` in context (optional `clientAdminId` / `mspId` query params).

This is **not** the org seat pool (`GET /api/v1/client/admin/products/assigned/{clientAdminId}`). Each row is one assignment from CMS `user_subpackages` (training and simulation products).

Assign User / Bulk Assign stay on Registration: `POST /api/v1/end-user/assign-sub-package`.

---

## List assignments

```http
GET /api/v1/client/license-assignments
```

### Query params

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| `clientAdminId` | string | No | — | Scope for admin/MSP. Ignored for CLIENT_ADMIN (uses token). |
| `mspId` | string | No | — | Treat the caller as MSP-scoped when set. |
| `search` | string | No | — | User **name or email** (resolved via Registration). |
| `packageId` | string | No | — | Parent package ID (Gold / Silver dropdown). |
| `productId` | string | No | — | Product ID. |
| `department` | string | No | — | Department name (case-insensitive exact match on AspireUser). |
| `status` | string | No | — | `ACTIVE`, `EXPIRING`, `EXPIRED`, or `COMPLETE`. |
| `fromDate` | string | No | — | Inclusive assigned-date from (`yyyy-MM-dd`; also `yyyy/MM/dd` or ISO-8601). |
| `toDate` | string | No | — | Inclusive assigned-date to. |
| `offset` | int | No | `0` | **Page index** (skip = `offset * pageSize`). |
| `pageSize` | int | No | `10` | Max `100`. |

`fromDate` / `toDate` are the **More Filters** date range. `toDate` must be on or after `fromDate`.

### Status

Filter uses uppercase values. Row `status` is the display bucket (title case):

| Filter | Row value | Meaning |
|--------|-----------|---------|
| `ACTIVE` | `Active` | In window, not complete, not yet 80% elapsed |
| `EXPIRING` | `Expiring` | Not complete; 80% of assigned→expiry elapsed |
| `EXPIRED` | `Expired` | Not complete; `expiryDate` before today |
| `COMPLETE` | `Complete` | Enrollment `status=COMPLETED` |

### Pagination

Same envelope as other admin lists. `offset` is a page index, not a skip count.

```json
{
  "offset": 0,
  "pageSize": 10,
  "total": 6,
  "items": []
}
```

Footer copy: `Showing {offset * pageSize + 1} to {min((offset + 1) * pageSize, total)} of {total}`.

### Example request

```http
GET /api/v1/client/license-assignments?search=john&packageId=pkg-gold&productId=prod-sat&department=Human%20Resources&status=ACTIVE&fromDate=2026-01-01&toDate=2026-12-31&offset=0&pageSize=10
```

### Success response

```json
{
  "message": "License assignments retrieved successfully",
  "statusCode": 200,
  "data": {
    "offset": 0,
    "pageSize": 10,
    "total": 6,
    "items": [
      {
        "userId": "user-1",
        "fullName": "John Smith",
        "email": "john.smith@crosstewart.com",
        "department": "Human Resources",
        "packageId": "pkg-gold",
        "packageName": "Gold",
        "productId": "prod-sat",
        "productName": "Security Awareness Training",
        "subPackageId": "sub-1",
        "subPackageName": "Security Awareness Training",
        "assignedDate": "2026-04-26",
        "expiryDate": "2027-04-26",
        "status": "Active"
      }
    ]
  }
}
```

`packageId` is `sub_packages.packageId`. `packageName` is the CMS `product_packages.name` for that id; if no product package exists, the API falls back to CMS `bundles.bundleName`, then `"N/A"`.

`subPackageId` / `subPackageName` are not shown on the current mock; keep them for unassign / detail later.

### Errors

| HTTP | When |
|------|------|
| 400 | Invalid date format, or `toDate` before `fromDate` |
| 500 | Unexpected failure (including Registration user lookup) |

---

## Filter dropdowns (existing APIs)

Do **not** call this list API to populate dropdowns.

| UI control | API |
|------------|-----|
| Package | Assigned packages for the client (`GET /api/v1/packages/client/assigned-packages`) |
| Product | Assigned products (`GET /api/v1/products/assigned?clientAdminId=...`) |
| Department | `GET /api/v1/departments` |
| Status | Hardcoded: All, Active, Expiring, Expired, Complete |

---

## Notes for FE

- One user with two products appears as **two rows**.
- Reset clears all query params and reloads `offset=0`.
- Name/email search and department hit Registration first; if no users match, the table is empty (`total: 0`).

---

## Export assignments

Downloads **all** matching rows (same filters as the list; pagination is ignored). Cap is 1,000,000 rows (1,000 pages × 1,000).

```http
GET /api/v1/client/license-assignments/export
```

### Query params

Same filters as the list (`clientAdminId`, `mspId`, `search`, `packageId`, `productId`, `department`, `status`, `fromDate`, `toDate`), plus:

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| `format` | string | No | `csv` | `csv`, `xls`, or `xlsx` |

### Example requests

```http
GET /api/v1/client/license-assignments/export?format=csv&status=ACTIVE
GET /api/v1/client/license-assignments/export?format=xls&department=Finance
```

### Response

Binary file download (`Content-Disposition: attachment`).

| format | Content-Type | Filename |
|--------|----------------|----------|
| `csv` | `text/csv; charset=UTF-8` | `license-assignments.csv` |
| `xls` | `application/vnd.ms-excel` | `license-assignments.xls` |
| `xlsx` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | `license-assignments.xlsx` |

CSV is UTF-8 with BOM so Excel opens it correctly.

### Columns (same as the table)

`User Name`, `Email`, `Department`, `Package`, `Product`, `Assigned Date`, `Expiry Date`, `Status`

Dates are `yyyy-MM-dd`. Status is the display value (`Active`, `Expiring`, `Expired`, `Complete`).

### Errors

| HTTP | When |
|------|------|
| 400 | Invalid date, `toDate` before `fromDate`, or unsupported `format` |
| 500 | Unexpected failure |

Use `window.open` / `fetch` + blob with the same auth headers as other CMS downloads. Do not expect a JSON envelope.
