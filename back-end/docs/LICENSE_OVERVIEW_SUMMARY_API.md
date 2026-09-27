# License Overview Summary API (Frontend)

Six KPI cards on the License Overview dashboard: Total Licenses, Assigned Users, Available Seats, Utilization Rate, Expiring Soon, Active Products.

**Endpoint:** `GET /api/v1/client/admin/license-overview-summary`

**Auth:** Same as other Registration client-admin APIs (gateway JWT).

This is the **org seat pool** (`client_products` in Registration), not the user × product assignment grid (`GET /api/v1/client/license-assignments`).

Seat totals are sums of stored `licenseCount` and `usedLicenseCount` on **ACTIVE** `client_products`. Simulation unique-campaign usage from `/license-statistics` is **not** applied here.

---

## Query params

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `clientAdminId` | string | No | Org to summarize. Defaults to the token `clientAdminId`, then `userId`. |

CLIENT_ADMIN callers can omit the param. MSP / Aspire Admin should pass `clientAdminId`.

---

## Card mapping

| Card | Fields | Notes |
|------|--------|-------|
| Total Licenses | `totalLicenses` | Sum of `licenseCount` on ACTIVE client products. |
| Assigned Users | `assignedUsers`, `assignedUsersPercent` | Sum of stored `usedLicenseCount`, and `% of totalLicenses`. |
| Available Seats | `availableSeats`, `availableSeatsPercent` | Unused purchased seats. |
| Utilization Rate | `utilizationRate`, `utilizationChangeVsLastMonth` | Same percent as assigned users. Delta is **percentage points** vs 30 days ago (e.g. `3.6` means +3.6 pts). |
| Expiring Soon | `expiringSoon`, `expiringSoonDays` | Count of ACTIVE product-packages whose `expiryDate` is in `[now, now + 30 days]`. |
| Active Products | `activeProducts` | Distinct `productId` values with ACTIVE licenses. |

Percents are rounded to **1 decimal**.

### Utilization vs last month

`utilizationChangeVsLastMonth` = current utilization − utilization as of 30 days ago.

- Last-month **total** = sum of `licenseCount` on ACTIVE products with `assignedAt == null` or `assignedAt` on or before that instant.
- Last-month **used** = `user_licence` rows with status `ASSIGNED`, `issueDate <= asOf`, and not expired at `asOf`.

Current assigned seats use stored `usedLicenseCount`, so the 30-day delta can differ from `/license-statistics` for simulation products.

---

## Example request

```http
GET /api/v1/client/admin/license-overview-summary?clientAdminId=b1c2d3e4-f5a6-7890-abcd-ef1234567890
```

## Success response

```json
{
  "message": "License overview summary retrieved successfully",
  "statusCode": 200,
  "data": {
    "totalLicenses": 995,
    "assignedUsers": 850,
    "assignedUsersPercent": 85.4,
    "availableSeats": 145,
    "availableSeatsPercent": 14.6,
    "utilizationRate": 85.4,
    "utilizationChangeVsLastMonth": 3.6,
    "expiringSoon": 12,
    "expiringSoonDays": 30,
    "activeProducts": 6
  }
}
```

| Field | Type | Example | UI |
|-------|------|---------|----|
| `totalLicenses` | int | `995` | “All purchased licenses” |
| `assignedUsers` | int | `850` | Primary number |
| `assignedUsersPercent` | number | `85.4` | “85.4% of total licenses” |
| `availableSeats` | int | `145` | Primary number |
| `availableSeatsPercent` | number | `14.6` | “14.6% of total licenses” |
| `utilizationRate` | number | `85.4` | “85.4%” |
| `utilizationChangeVsLastMonth` | number | `3.6` | “↑ 3.6% vs last month” (sign from the number) |
| `expiringSoon` | int | `12` | Count |
| `expiringSoonDays` | int | `30` | “Within next 30 days” |
| `activeProducts` | int | `6` | “With active licenses” |

Zero purchased licenses: all counts and percents are `0`.

## Errors

| Status | When |
|--------|------|
| `400` | `clientAdminId` missing and cannot be taken from the token |
| `404` | Client admin not found |
| `500` | Unexpected failure |
