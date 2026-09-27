# Outlook Add-in AppSource readiness checklist

## Goal

Publish one Outlook add-in for multi-tenant customer adoption via AppSource (or marketplace distribution), with safe tenant-aware phishing report tracking.

## A) AppSource readiness checklist

### 1. Product and identity setup

- Finalize add-in identity:
  - stable manifest `<Id>` (do not rotate per customer).
  - production `Version` strategy (`major.minor.patch`).
- Finalize publisher metadata:
  - provider name, support URL, privacy URL, terms URL.
- Ensure support and legal pages are public HTTPS.

### 2. Hosting and endpoints

- Host add-in assets on globally reachable HTTPS:
  - `/outlook-addin/taskpane.html`
  - `/outlook-addin/taskpane.js`
  - `/outlook-addin/taskpane.css`
  - `/outlook-addin/icon-16.png`, `32`, `64`, `80`, `128`.
- Ensure TLS is valid (no self-signed certs).
- Ensure static files are publicly reachable without login prompts.

### 3. Manifest quality

- `AppDomain` includes production domain.
- `SourceLocation` and `Taskpane.Url` use production URLs.
- Icon URLs resolve and match required sizes.
- `Permissions` minimal and justified (`ReadItem` for current flow).
- Validate manifest using Microsoft validator tools before submission.

### 4. Security and compliance

- Add signed report token (HMAC/JWT) in report URL or marker.
- Add replay protection (expiry + nonce/idempotency).
- Minimize collected data (no unnecessary message body persistence).
- Add abuse controls:
  - rate limiting on report endpoint
  - request validation
  - structured audit logs
- Document privacy/data retention behavior.

### 5. Multi-tenant runtime controls

- Tenant isolation enforced in backend.
- Report events tagged with tenant and source (`outlook-addin`).
- Tenant onboarding status gate (enabled/disabled).
- Per-tenant monitoring and alerting.

### 6. Testing and certification prep

- Outlook Web + New Outlook + Desktop test pass.
- Smoke tests in at least 2 external tenants.
- Fail-path tests:
  - tracking URL missing
  - invalid signature
  - expired token
  - duplicate report.
- Prepare AppSource submission artifacts:
  - screenshots
  - test account instructions (if requested)
  - support contact
  - release notes.

## B) Customer approval flow (per organization)

1. **Discovery**
   - Customer admin receives app listing + capabilities.
2. **Security review**
   - Customer reviews permission scope (`ReadItem`), privacy policy, data flow.
3. **Tenant admin consent**
   - Admin acquires/approves add-in in M365 tenant.
4. **Pilot assignment**
   - Assign to security team / pilot group first.
5. **Pilot validation**
   - Verify add-in button visibility, taskpane load, successful report events.
6. **Production rollout**
   - Assign to all target users/groups.
7. **Post-go-live monitoring**
   - Track success/error/unmatched metrics; support any tenant-specific exceptions.

## C) Changes needed from your current implementation

Your current implementation is a good MVP. For AppSource-grade multi-tenant rollout, apply these upgrades:

### 1. Report URL hardening (required)

- Current: report URL extracted from body/marker and called directly.
- Change needed:
  - include signed token in report URL marker (tenantId, campaignId, trackingId, exp, nonce).
  - backend verifies signature and expiry before `recordReport`.

### 2. Dedicated add-in report endpoint (recommended)

- Current: add-in calls `GET /t/report/{trackingId}`.
- Change needed:
  - add `POST /api/v1/phishing/report/outlook` for add-in-originated reports.
  - payload: `token`, `source`, `clientType`, `messageContext`.
  - keep `/t/report/*` for in-email report links if needed.

### 3. Duplicate-safe reporting (required)

- Current: status transitions exist, but metric inflation risk should be explicitly guarded.
- Change needed:
  - idempotency key per report event.
  - ensure campaign `emailsReported` increments only once per recipient.

### 4. Tenant observability (required)

- Add dashboards:
  - report success rate by tenant
  - invalid/expired token count
  - unmatched tracking count
  - taskpane request failure rate.

### 5. Manifest operational process (required)

- Keep one production manifest template in repo.
- Versioned release process:
  - update `Version`
  - deploy assets
  - validate URLs
  - publish release notes.

## D) Go-live gate (AppSource-ready)

- All checklist items in sections A and C complete.
- Customer pilot flow validated in at least one external tenant.
- Security sign-off on token model and data handling.
- Support playbook prepared for tenant onboarding and incident response.
