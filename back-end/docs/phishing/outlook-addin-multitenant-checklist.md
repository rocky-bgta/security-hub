# Outlook Add-in: Multi-tenant deployment checklist (one page)

## Target model

- Single add-in codebase + single hosted URL domain.
- One manifest template.
- Per customer Microsoft 365 tenant installs the add-in.
- Backend maps reports to tenant using tracking ID (and optional tenant marker).

## Pre-deployment prerequisites

- Public HTTPS URL is live and reachable:
  - `/gateway/phishing/outlook-addin/taskpane.html`
  - `/gateway/phishing/outlook-addin/icon-16.png`
  - `/gateway/phishing/outlook-addin/icon-32.png`
  - `/gateway/phishing/outlook-addin/icon-80.png`
- Gateway allows public access:
  - `/gateway/phishing/outlook-addin/**`
  - `/gateway/phishing/t/**`
- Manifest values are production-ready:
  - `AppDomain` is domain only
  - taskpane/icon URLs are full HTTPS URLs
  - unique add-in ID (`<Id>`) is stable

## Tenant onboarding checklist (repeat per organization)

1. Confirm customer M365 admin contact and test users.
2. Share deployment package:
   - `manifest.xml`
   - validation URLs
   - rollback steps
3. Customer admin uploads add-in:
   - Microsoft 365 Admin Center -> Integrated apps -> Upload custom app.
4. Assign users/groups (pilot first, then all users).
5. Validate in Outlook Web:
   - add-in button visible on opened message.
   - taskpane loads without auth prompts.
6. Send one test campaign email.
7. Click **Report Phishing** in add-in.
8. Verify backend event:
   - `EMAIL_REPORTED` recorded
   - campaign stats updated
   - no auth/cors/gateway errors
9. Sign off and move tenant to production rollout.

## Operational controls

- Maintain tenant onboarding registry:
  - tenant name
  - deployment date
  - manifest version
  - pilot users
  - status (pilot/live/paused)
- Version manifest with semantic version increments.
- Publish release notes for each add-in update.
- Keep rollback manifest version available.

## Recommended improvements to current implementation

1. Add tenant-aware marker in email instrumentation:
   - Keep `ASAT_REPORT_URL`
   - Add `ASAT_TENANT_ID` marker for diagnostics.
2. Add metadata header on report call:
   - `X-ASAT-Report-Source: outlook-addin` (already present in JS).
3. Add optional dedicated endpoint:
   - `POST /api/v1/phishing/report/outlook`
   - payload: `trackingId`, `source`, `clientInfo`, `tenantHint`.
4. Add idempotency guard in report handling:
   - prevent duplicate increments for repeated clicks.
5. Add monitoring dashboard:
   - report success rate by tenant
   - unmatched report rate
   - taskpane load failures.

## Production go-live gate

- Taskpane and icons publicly reachable from customer network.
- Add-in visible for pilot users in customer tenant.
- 3 successful report events from pilot users.
- No critical errors for 48 hours.
