# First Campaign Check API (Frontend)

Returns whether the authenticated client has any **other** campaign for a given `productPackageId`.

## Why `campaignId` is required after Step 1

Step 1 **creates** a draft campaign with `productPackageId`. If you call `is-first` with only the package id, that draft is counted and `data` is always `false`.

Pass the current campaign id so the backend ignores that draft.

## Contract

After Step 1 (recommended):

```http
GET /api/v1/phishing/campaigns/is-first?productPackageId={productPackageId}&campaignId={campaignId}
```

Before create (optional, no draft yet):

```http
GET /api/v1/phishing/campaigns/is-first?productPackageId={productPackageId}
```

Do **not** send `clientAdminId`. Client scope comes from auth context.

| Param | Required | Description |
|--------|----------|-------------|
| `productPackageId` | Yes | `ClientProduct.id` |
| `campaignId` | After Step 1 | Current draft id to exclude |

### Success — first campaign (only this draft, or none)

```json
{
  "message": "First campaign check completed successfully",
  "statusCode": 200,
  "data": true
}
```

### Success — not first (another campaign exists for this package)

```json
{
  "message": "First campaign check completed successfully",
  "statusCode": 200,
  "data": false
}
```

## Semantics

- `data: true` — no **other** campaigns (any status, including DRAFT) for this client + package
- `data: false` — at least one other campaign exists for this client + package
- Blank/missing `productPackageId` → validation error
