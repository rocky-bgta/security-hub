# QA — Phishing Licence Distribution

| Field | Value |
|-------|--------|
| Module | Phishing Campaign — Licence Distribution |
| Env | Dev / Staging |
| Phishing base | `/gateway/phishing/api/v1/phishing/campaigns` |
| Registration base | `/gateway/registration/api/v1` |
| Collection | `phishing_user_licence` |

---

## 1. Scope

| In scope | Out of scope |
|----------|--------------|
| First-campaign check | Live sync of `riskGroup` after allocate |
| Allocate licence (all audience types) | Backfill of legacy `groupIds` |
| Add-licence Individual (exclude licensed) | Full campaign launch E2E beyond licence steps |
| Licensed users list + filters | |
| Department counts | |
| Risk group counts (`/group-counts`) | |

---

## 2. Preconditions

| ID | Requirement |
|----|-------------|
| P1 | Client admin with **ACTIVE** product package (`productPackageId` = `ClientProduct.id`) and known seat count |
| P2 | Draft campaign (Step 1) linked to that `productPackageId` |
| P3 | End users with mixed departments and `riskGroup` (`HIGH_RISK`, `MEDIUM_RISK`, `LOW_RISK`, `CRITICAL_RISK`) |
| P4 | Valid client-admin auth token |

---

## 3. Acceptance criteria

| ID | Criteria |
|----|----------|
| AC-01 | Allocate inserts unique licences keyed by `{ clientAdminId, productPackageId, userId }` |
| AC-02 | New licence rows snapshot Registration `riskGroup` (and profile fields) |
| AC-03 | Existing licensed users consume **0** new seats on re-allocate |
| AC-04 | Over-seat selection with `confirm=false` returns confirmation and **does not** insert |
| AC-05 | Over-seat selection with `confirm=true` inserts only remaining seats |
| AC-06 | Add-licence Individual (`GET /end-user?productPackageId=`) returns only **unlicensed** users for that package |
| AC-07 | Licensed list supports search, departments, `riskGroup`, and page-index pagination |
| AC-08 | `/licensed-users/group-counts` returns Registration-shaped `{ riskGroup, userCount }` from `phishing_user_licence` |
| AC-09 | Department counts aggregate by `departmentName` on licence rows |
| AC-10 | Licence APIs are scoped to authenticated client + campaign package |
| AC-11 | Allocate allowed only for **DRAFT** campaigns with valid ACTIVE package |

---

## 4. Test cases

### 4.1 First-campaign check

`GET /campaigns/is-first?productPackageId={id}&campaignId={optional}`

| TC ID | Title | Steps | Expected result | Priority |
|-------|--------|--------|-----------------|----------|
| FC-01 | First package | No other campaigns → call with `productPackageId` only | `200`, `data: true` | P0 |
| FC-02 | After Step 1 with campaignId | Create draft → call with `productPackageId` + `campaignId` | `200`, `data: true` (draft excluded) | P0 |
| FC-03 | After Step 1 without campaignId | Same draft → omit `campaignId` | `200`, `data: false` | P0 |
| FC-04 | Second campaign | Second campaign same package → is-first with new `campaignId` | `200`, `data: false` | P1 |
| FC-05 | Missing productPackageId | Call without param | Validation error | P1 |

---

### 4.2 Allocate licence

`POST /campaigns/{campaignId}/allocate-licence`

| TC ID | Title | Steps | Expected result | Priority |
|-------|--------|--------|-----------------|----------|
| AL-01 | Allocate within seats | Select N users ≤ remaining, `confirm=true` | `newLicenseAllocatedCount = N`; seats used ↑; N docs in `phishing_user_licence` | P0 |
| AL-02 | Snapshot riskGroup | Allocate user with known Registration `riskGroup` | Doc has same `riskGroup` (e.g. `HIGH_RISK`) | P0 |
| AL-03 | Snapshot profile | Allocate any user | Doc has `email`, `firstName`, `lastName`, `departmentName`, org fields | P1 |
| AL-04 | Re-allocate same users | Allocate same set again | `existingLicensedUserCount > 0`; no duplicate unique key; no extra seats | P0 |
| AL-05 | Mix existing + new | Half licensed, half new | Only new consume seats; `userIds` includes both | P0 |
| AL-06 | Over limit preview | Select > remaining, `confirm=false` | `requiresConfirmation: true`; **no** insert; message + proposed `userIds` | P0 |
| AL-07 | Over limit confirm | Same selection, `confirm=true` | Inserts only remaining; used = `licenseCount` | P0 |
| AL-08 | Empty audience | Empty selection | Validation error | P1 |
| AL-09 | Non-draft campaign | Call on launched/paused | Validation error | P0 |
| AL-10 | Missing package on campaign | Campaign without `productPackageId` | Validation error | P1 |
| AL-11 | Inactive / expired package | Package not ACTIVE or expired | Validation error | P1 |
| AL-12 | Audience types | ALL / DEPARTMENTS / GROUPS / INDIVIDUAL | Correct users licensed (INDIVIDUAL = primary FE path) | P1 |
| AL-13 | Cross-client | Token Client A, campaign Client B | Not found / forbidden | P0 |

---

### 4.3 Add-licence Individual (exclude licensed)

`GET /end-user?clientAdminId={id}&productPackageId={id}&offset=0&pageSize=10`

| TC ID | Title | Steps | Expected result | Priority |
|-------|--------|--------|-----------------|----------|
| EU-01 | Exclude licensed | Call with `productPackageId` | Only unlicensed users for that package | P0 |
| EU-02 | After allocate | Allocate user → refresh list with package | User absent; `total` ↓ | P0 |
| EU-03 | Without package | Omit `productPackageId` | Licensed users still listed | P0 |
| EU-04 | Pagination | Page unlicensed list | Stable pages; `total` = unlicensed count | P1 |
| EU-05 | Filters + package | search / departments / riskGroup + package | Filters apply on unlicensed set | P1 |
| EU-06 | `/end-user/users` | Resolve audience without package | Full ACTIVE org list (no exclude) | P1 |

---

### 4.4 Licensed users list

`GET /campaigns/{campaignId}/licensed-users`

| TC ID | Title | Steps | Expected result | Priority |
|-------|--------|--------|-----------------|----------|
| LU-01 | List after allocate | Allocate → list | User in `data.items`; `id` = Registration `userId`; `message` + `statusCode` present | P0 |
| LU-02 | riskGroup in item | Allocate HIGH_RISK user | `items[].riskGroup = HIGH_RISK` | P0 |
| LU-03 | Pagination | `offset=0,1`, `pageSize=10` | Page-index skip; `total` correct | P0 |
| LU-04 | Search | `search=` name/email | Case-insensitive match on first/last/email | P1 |
| LU-05 | Department filter | `departments=Intelligence` | Only matching `departmentName` | P1 |
| LU-06 | Risk filter single | `riskGroup=HIGH_RISK` | Only HIGH_RISK; `total` matches | P0 |
| LU-07 | Risk filter multi | `riskGroup=HIGH_RISK&riskGroup=CRITICAL_RISK` | OR of both | P1 |
| LU-08 | Combined filters | search + departments + riskGroup | Intersection | P1 |
| LU-09 | Invalid riskGroup | `riskGroup=INVALID` | `400` | P1 |
| LU-10 | Wrong campaign/client | Invalid or other-client id | Not found; `data` null | P0 |
| LU-11 | Legacy null riskGroup | Old row without `riskGroup` | Visible with no filter; **excluded** when `riskGroup=` set | P2 |

---

### 4.5 Department counts

`GET /campaigns/{campaignId}/licensed-users/department-counts`

| TC ID | Title | Steps | Expected result | Priority |
|-------|--------|--------|-----------------|----------|
| DC-01 | After allocate | Licence users in Intelligence | `{ "departmentName": "Intelligence", "userCount": N }` | P0 |
| DC-02 | Multiple depts | Mix HR / IT | Separate buckets; sum = licensed total | P1 |
| DC-03 | Empty package | No licences | `data: []` | P2 |

---

### 4.6 Risk group counts

`GET /campaigns/{campaignId}/licensed-users/group-counts`  
*(Same shape as Registration `GET /departments/risk-group-user-counts`)*

| TC ID | Title | Steps | Expected result | Priority |
|-------|--------|--------|-----------------|----------|
| RC-01 | After HIGH_RISK allocate | Allocate one HIGH_RISK user | `message: "Risk group user counts retrieved successfully"`; `data: [{ "riskGroup": "HIGH_RISK", "userCount": 1 }]` | P0 |
| RC-02 | Multiple risk groups | Licence mixed riskGroups | One row per group; counts correct | P0 |
| RC-03 | Response shape | Inspect JSON | Fields `riskGroup` + `userCount` only (no `groupId`) | P0 |
| RC-04 | Chip → list | Chip HIGH_RISK → list `?riskGroup=HIGH_RISK` | List count matches chip | P0 |
| RC-05 | Empty package | No licences | `data: []` | P2 |
| RC-06 | Legacy null | Old row without riskGroup | Optional `{ "riskGroup": null, "userCount": N }` | P2 |

---

### 4.7 End-to-end (UI)

| TC ID | Title | Steps | Expected result | Priority |
|-------|--------|--------|-----------------|----------|
| E2E-01 | Happy path | Step 1 → Add licence → allocate → Licensed tab | Seats update; user on Licensed; gone from Add-licence | P0 |
| E2E-02 | Confirm continue | Select > remaining → confirm → Continue | Only remaining seats allocated | P0 |
| E2E-03 | Confirm cancel | Over-limit → Cancel | No new licences | P1 |
| E2E-04 | Licensed filters | Dept + risk chips + search | Table + total update together | P1 |
| E2E-05 | Step 6 after allocate | Save audience (Step 6) | Works; allocate alone does not create recipients | P1 |

---

### 4.8 Regression

| TC ID | Title | Expected result | Priority |
|-------|--------|-----------------|----------|
| RG-01 | Department-counts shape | Still `{ departmentName, userCount }` | P1 |
| RG-02 | Allocate seat math | Dedupe / confirm / remaining unchanged | P0 |
| RG-03 | List without riskGroup param | All licensed users (incl. null riskGroup) | P1 |
| RG-04 | `/end-user` without package | Full org list unchanged | P1 |
| RG-05 | Campaign steps / launch | Unaffected by licence APIs | P1 |

---

## 5. API quick reference

| API | Method | Pass when |
|-----|--------|-----------|
| `/campaigns/is-first` | GET | Correct `true`/`false` with/without `campaignId` |
| `/campaigns/{id}/allocate-licence` | POST | Insert + `riskGroup` on Mongo doc |
| `/end-user?productPackageId=` | GET | Excludes licensed for package |
| `/campaigns/{id}/licensed-users` | GET | Filters + pagination work |
| `/campaigns/{id}/licensed-users/department-counts` | GET | Dept buckets from licence rows |
| `/campaigns/{id}/licensed-users/group-counts` | GET | `{ riskGroup, userCount }` from licence rows |

---

## 6. Exit criteria

| Gate | Requirement |
|------|-------------|
| P0 | All **P0** cases pass |
| Critical | No open Blocker / Critical defects |
| Sign-off | AC-01 … AC-11 verified |
| Must-pass set | AL-01, AL-02, AL-06, AL-07, EU-01, EU-02, LU-01, LU-02, LU-06, RC-01, RC-03, RC-04, E2E-01 |

---

## 7. Defect logging (standard)

| Field | Example |
|-------|---------|
| Title | `[Licence] group-counts returns empty after allocate` |
| Env / Build | Dev · build # |
| TC ID | RC-01 |
| Steps | Repro steps |
| Expected | Registration-shaped non-empty `data` |
| Actual | `data: []` |
| Evidence | Request URL, response JSON, Mongo licence doc screenshot |
