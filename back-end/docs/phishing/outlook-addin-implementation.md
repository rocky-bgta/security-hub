# Outlook Add-in integration for phishing reporting

This document explains how to deploy and test the Outlook add-in scaffold added in this repository and connect it with the phishing module report tracking endpoint.

## 1) What was implemented

- Add-in web assets:
  - `services/phishing/service/src/main/resources/static/outlook-addin/taskpane.html`
  - `services/phishing/service/src/main/resources/static/outlook-addin/taskpane.js`
  - `services/phishing/service/src/main/resources/static/outlook-addin/taskpane.css`
  - `services/phishing/service/src/main/resources/static/outlook-addin/manifest.xml`
- Phishing email instrumentation hardening:
  - `CampaignEmailProducer` now always embeds a hidden `ASAT_REPORT_URL` marker with the `/t/report/{trackingId}` URL when missing from template content.
- KnowBe4-inspired report UX and telemetry:
  - Add-in now asks user confirmation before submitting report.
  - Add-in posts message context (`subject`, `from`, `internetMessageId`, headers when available, host diagnostics) to `/t/report/{trackingId}/addin`.
  - Backend stores that metadata under `EMAIL_REPORTED` activity metadata.
  - Add-in falls back to legacy `GET /t/report/{trackingId}` for backward compatibility.

The add-in reads the currently opened message body, extracts report URL, then calls:

`GET {trackingBaseUrl}/t/report/{trackingId}`

Current preferred flow:

`POST {trackingBaseUrl}/t/report/{trackingId}/addin` with JSON metadata payload

Legacy fallback:

`GET {trackingBaseUrl}/t/report/{trackingId}`

This records `EMAIL_REPORTED` through existing `TrackingService.recordReport(...)`.

## 2) Outlook / Azure keys and secrets required

For the implemented flow above (public report URL with tracking ID), **no Outlook-side client secret is required**.

You only need:

1. **Add-in manifest ID (GUID)**
   - The `<Id>` in `manifest.xml`.
   - Generate unique GUID per environment if needed.

2. **Public HTTPS host URL**
   - Replace `https://YOUR_PUBLIC_PHISHING_HOST` in `manifest.xml`.
   - Must be reachable by Outlook Web/Desktop clients.

3. **M365 admin deployment permission**
   - Admin center access to deploy integrated apps/add-ins tenant-wide or to pilot users.

Optional (future secure API mode with user identity):
- Azure App Registration
- Application (client) ID
- Client secret or certificate
- Exposed API scope / audience
- Token validation on phishing API

These are only needed if you change add-in flow to call an authenticated API endpoint instead of tracking URL.

## 3) Manifest configuration steps

1. Copy `manifest.xml` from static folder.
2. Replace all `https://YOUR_PUBLIC_PHISHING_HOST` values.
3. Ensure icon URLs exist (or update to valid image paths).
4. Validate manifest:
   - [https://learn.microsoft.com/office/dev/add-ins/testing/troubleshoot-manifest](https://learn.microsoft.com/office/dev/add-ins/testing/troubleshoot-manifest)
5. Deploy using Microsoft 365 Admin Center:
   - Settings -> Integrated apps -> Upload custom app -> Office Add-in

## 4) Backend prerequisites

1. `tracking.base-url` must point to your phishing service public URL.
2. Campaign templates should include `{{REPORT_URL}}` when possible.
3. Even if template misses it, backend now injects hidden `ASAT_REPORT_URL` marker.
4. CORS/proxy should allow Outlook clients to reach `/t/report/{trackingId}`.

## 5) End-to-end test plan

### A. Smoke test (single user)

1. Deploy add-in to a pilot test user.
2. Create a phishing campaign and send to that user.
3. Open received email in Outlook Web.
4. Open add-in -> click **Report Email**.
5. Confirm add-in message: "Reported successfully" and tracking ID displayed.
6. Verify backend:
   - recipient status set to `REPORTED`
   - `reportedAt` populated
   - activity log contains `EMAIL_REPORTED`
   - campaign stats `emailsReported` incremented

### B. Negative tests

1. Non-campaign email:
   - add-in should fail gracefully with "Tracking URL not found..."
2. Broken tracking URL:
   - add-in should show HTTP error status.
3. Duplicate report click:
   - status should remain stable and no incorrect metric inflation.

### C. Regression tests

1. Existing open/click/submission tracking still works.
2. Risk score update rule for `REPORTED` is correct.
3. Data-submitted recipients remain immutable for later report updates (existing behavior).

## 6) Production hardening recommendations

1. Add a signed token in report URL (prevent random tracking-id replay).
2. Add explicit event-source metadata (`outlook-addin`) in backend activity metadata.
3. Add rate limiting for `/t/report/*`.
4. Add telemetry dashboard:
   - report success %
   - report errors
   - unmatched/invalid report attempts

## 7) Multi-tenant deployment strategy (single codebase)

For multi-organization rollout, use a single hosted add-in implementation but deploy the manifest per Microsoft 365 tenant.

- Keep one add-in codebase and one public host:
  - `https://dev.aspireelearning.com/gateway/phishing/outlook-addin/*`
- Keep one manifest template in source control.
- Install manifest separately in each customer tenant (admin center).
- Track tenant onboarding and manifest version per tenant.

See the one-page operational checklist:

- `docs/phishing/outlook-addin-multitenant-checklist.md`

For AppSource-oriented rollout and customer approval flow:

- `docs/phishing/outlook-addin-appsource-checklist.md`
