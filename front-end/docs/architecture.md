# ASAT V2 Frontend — Module Architecture

This document describes the overall architecture of **ASAT-V2-FRONTEND**, what each module is responsible for, how modules communicate, and why **Yarn** and **Tailwind CSS** are used.

For local run steps, see [`setup_local.md`](./setup_local.md).

---

## 1. What this project is

ASAT V2 Frontend is the web UI for the Aspire Security Awareness Training platform. It is a **micro-frontend (MFE)** application: one **host** shell plus several independently built **remote** modules that are composed at runtime via **Vite Module Federation** (`@originjs/vite-plugin-federation`).

Users always open the host:

```text
http://localhost:5000
```

The host lazy-loads page UI from remotes (for example login from `home-module`, User Report from `miscellaneous-module`). API traffic goes through the host’s `/gateway` proxy to the backend gateway (local default: `http://localhost:7030`).

---

## 2. Overall architecture

```text
Browser
  └── host-module (:5000)                    App shell, routing, providers, /gateway proxy
        ├── remotes (build + serve / preview; each exposes assets/remoteEntry.js)
        │     ├── home-module (:5010)         Auth, layouts, shared providers/hooks
        │     ├── user-module (:5020)         Users, MSP/client onboarding & lists
        │     ├── content-module (:5030)      Courses, packages, exams, certificates
        │     ├── billing-module (:5050)      Invoices, payments, credits, licenses
        │     ├── phishing-module (:5070)     Phishing / smishing / vishing / deepfake
        │     ├── account-module (:5090)      Profile, security, branding, notifications
        │     └── miscellaneous-module (:5140) Admin metadata, reports, support, policies
        │
        └── /gateway/*  ──proxy──►  Backend API Gateway (:7030)
                                      ├── /registration/api/...
                                      ├── /cms/api/...
                                      ├── /billing/api/...
                                      ├── /universal/api/...
                                      └── other backend services
```

### Design principles

| Principle | How it shows up |
|-----------|-----------------|
| Independent deployability | Each module has its own `package.json`, Vite config, `yarn.lock`, and CDN path |
| Runtime composition | Host declares remotes; pages use `React.lazy(() => import('module-name/Export'))` |
| Shared runtime | `react`, `react-dom`, `react-router-dom`, `react-toastify`, `recharts` are federation **singletons** |
| Shared auth/API surface | Remotes consume `home-module` providers/hooks (`AuthProvider`, `useAPI`, layouts, etc.) |
| Thin host pages | Host route files wrap remotes with `ErrorBoundaryWrapper`; business UI lives in remotes |

### Federation rule (important)

`@originjs/vite-plugin-federation` only emits a real `assets/remoteEntry.js` in **build** output.

| Role | Local script | Why |
|------|--------------|-----|
| Remotes | `yarn deploy` or `yarn build` + `yarn serve` | Host must fetch JavaScript `remoteEntry.js` |
| Host | `yarn dev` | Dev server + `/gateway` proxy to backend |

Never run `yarn dev` / `npm run dev` on a remote when the host will load it — Vite dev serves HTML at that path and the host page stays blank.

---

## 3. Module responsibilities

### 3.1 `host-module` (port `5000`) — App shell

**Purpose:** Single entry URL, top-level routing, layout chrome wiring, and API proxy. It does **not** own most feature UI.

**Responsible for:**

- Vite app served at `:5000`
- Declaring all remotes in `vite.config.ts`
- Route trees under `src/routes/` (`AppRouter`, `UserRouter`, `ReportRouter`, …)
- Thin page wrappers that lazy-import remotes (e.g. `pages/auth/Login.tsx` → `home-module/Login`)
- Proxying `/gateway` → backend gateway
- Production host Docker/ECR shell that loads CDN-hosted remotes

**Does not own:** Login form implementation, user/content/billing business screens (those live in remotes).

---

### 3.2 `home-module` (port `5010`) — Auth & shared platform

**Purpose:** Authentication UX and the shared platform layer other remotes depend on.

**Responsible for:**

- Login, MFA setup/verify, reset/set password
- Role-based layouts (`AspireAdminLayout`, `ClientAdminLayout`, `MspLayout`, …)
- Federated providers: `AuthProvider`, `APIClientProvider`, `StoreProvider`
- Federated hooks: `useAuth`, `useAPI`, `useStore`, `useUploader`
- Shared editors (`TextEditor`, `HtmlEditor`), security/role helpers
- Dashboard shells by role

**Why it matters:** Minimum local stack for login is **home + host**. Without `home-module`’s `remoteEntry.js`, auth and shared providers fail.

---

### 3.3 `user-module` (port `5020`) — Users & tenancy

**Purpose:** User lifecycle and organization onboarding for Aspire Admin, MSP, and Client Admin.

**Responsible for:**

- All users / system users lists
- Bulk import, sync, suspend, activity logs
- Aspire Admin onboarding, analytics, reports
- MSP list/edit/profile/licenses/onboarding
- Client Admin list/edit/self-onboarding/buy product/suspend/activity

---

### 3.4 `content-module` (port `5030`) — Learning content

**Purpose:** Products, packages, courses, exams, and certificates consumed by learners and admins.

**Responsible for:**

- Product / package / sub-package / license views
- Course lists, chapters, content viewing, bookmarks
- Exams (take exam, results, library, analytics, settings)
- Certificate history, templates, issued certificates
- Client-user dashboard pieces and related account shortcuts used from content flows

---

### 3.5 `billing-module` (port `5050`) — Billing & payments

**Purpose:** Commercial workflows — payments, credits, VAT, tiers, and license management UIs.

**Responsible for:**

- Pending payments, payment history/report, success/failure pages
- Client and MSP payment / license screens
- Aspire billing history & analytics
- Coupons, credits, VAT, tier settings

---

### 3.6 `phishing-module` (port `5070`) — Simulation & breach

**Purpose:** Security simulation product surface (phishing, smishing, vishing, deepfake) plus breach monitoring.

**Responsible for:**

- Domains, sender profiles, email/SMS templates, landing pages
- Campaign create/list/details and risk/report dashboards
- Smishing / vishing / deepfake flows
- Breach dashboard & recipient breaches
- Simulation configuration (SMS/voice servers, AI providers, etc.)

---

### 3.7 `account-module` (port `5090`) — Account & preferences

**Purpose:** Logged-in account, security, branding, and notification configuration.

**Responsible for:**

- Personal information, password update
- Security settings
- Notification settings / role-wise settings / templates
- Global settings
- Branding management

---

### 3.8 `miscellaneous-module` (port `5140`) — Admin, reports, support

**Purpose:** Cross-cutting admin configuration, reporting, policies, knowledge hub, and support tickets.

**Responsible for:**

| Area | Examples |
|------|----------|
| Access control | Roles, permissions, menus |
| CMS / registration metadata | Categories, tags, compliance, content types, countries, states, industries, org size/type, languages, time zones, departments, MSP types |
| Reports | User, access, content, product, support ticket, billing/payment, certificates, licenses, performance, phishing content, user activity/risk |
| Policies | Policy list/request/assigned/requested, policy types |
| Knowledge hub | Resources, categories, analytics |
| Engagement | Leaderboard, poll/survey, latest news & categories |
| Support | Tickets (pending/resolved), ticket types |
| Billing admin helpers | Billing actions, next steps, user ranges, credit reasons, net terms, suspend reasons |

API path constants for this module live in `miscellaneous-module/src/routes/APIEndpoints.ts` and call backend domains such as `/registration`, `/cms`, `/billing`, and `/universal` through the gateway.

---

## 4. How a page is loaded (runtime flow)

Example: open `/auth/login`.

1. Browser hits **host** `:5000`.
2. Host `AppRouter` renders the Login page wrapper.
3. Wrapper does `lazy(() => import('home-module/Login'))`.
4. Federation loads `VITE_HOME_MODULE_URL/assets/remoteEntry.js`.
5. Remote React component mounts inside the host.
6. API calls use `VITE_API_BASE_URL` (typically `/gateway/...`), proxied by host Vite (or served via gateway in deployed envs).

Same pattern for feature pages, e.g. User Report → `miscellaneous-module/UserReport`.

---

## 5. Inside each module (common source layout)

Every module follows a similar Vite + React + TypeScript layout:

```text
<module>/
  package.json          # scripts: dev | build | deploy | serve | lint | format
  vite.config.ts        # federation name, remotes, exposes, shared deps
  tailwind.config.js    # Tailwind theme / content paths
  yarn.lock             # deterministic installs (CI/CDN)
  src/
    main.tsx            # standalone entry (when served alone)
    pages/              # route-level screens (often federated exposes)
    features/           # feature-specific UI pieces
    components/         # shared UI within the module
    providers/          # React context providers
    hooks/              # reusable hooks
    routes/             # AppRouter, path constants, APIEndpoints
    services/           # API/service helpers (where present)
    models/             # TypeScript types/models
    schemas/            # validation schemas (e.g. Zod)
    utils/              # helpers, constants, role/security utilities
    styles/             # extra CSS when needed
    index.css           # Tailwind entry (@tailwind base/components/utilities)
```

**Host-specific:** `host-module/src/routes/` is the central route registry that stitches remotes into one app.

**Remote-specific:** each remote’s `vite.config.ts` `exposes` map is the public contract other apps may import (e.g. `miscellaneous-module/Roles`).

---

## 6. Backend touchpoints (frontend view)

The frontend does not call microservices by host/port directly in local/dev host mode. It uses gateway-prefixed paths, for example:

| Prefix | Typical domain |
|--------|----------------|
| `/registration/api/...` | Roles, menus, clients, departments, dropdown metadata, user reports |
| `/cms/api/...` | Categories, tags, products/packages, certificates, content reports |
| `/billing/api/...` | Invoice actions, payment/subscription reports |
| `/universal/api/...` | Policies, knowledge hub, support tickets, polls, news, leaderboards |

Exact endpoint maps are maintained per module under `src/routes/APIEndpoints.ts`.

---

## 7. Why Yarn was introduced

The repo standardizes on **Yarn Classic (v1.22.22)** for module installs and builds.

**Reasons in this project:**

1. **CI/CD contract** — `.github/workflows/frontend-deploy.yml` enables Yarn via Corepack, caches each module’s `yarn.lock`, and builds with Yarn.
2. **CDN sync contract** — `scripts/sync-cdn.ps1` runs `yarn install --frozen-lockfile` then `yarn build` for every module before S3 sync. Frozen lockfile installs fail if dependencies drift.
3. **Deterministic, per-module lockfiles** — There is no root monorepo workspace; each of the 8 modules has its own `yarn.lock`. Yarn keeps installs reproducible across local machines, GitHub Actions, and CDN publish.
4. **Avoid npm/yarn lockfile fights** — Mixing package managers produces conflicting `package-lock.json` / `yarn.lock` trees. Prefer Yarn only so CI and local builds resolve the same graph.

Local note from `setup_local.md`: npm can still run scripts in a pinch, but use **one package manager consistently per module**. For deploy parity with CI, use Yarn.

```bash
# Typical per-module flow
cd home-module
yarn install
yarn deploy    # remotes
# host:
cd ../host-module
yarn install
yarn dev
```

---

## 8. Why Tailwind CSS was introduced

All modules use **Tailwind CSS v3** (with PostCSS, Autoprefixer, and usually `tailwindcss-animate`).

**Reasons in this project:**

1. **Consistent UI across MFEs** — Eight separately built remotes still share utility-class styling and theme tokens (colors, spacing, typography), so federated pages look like one product.
2. **Faster UI delivery** — Utility classes reduce writing/maintaining large custom CSS files for tables, forms, modals, and report layouts.
3. **Design-system friendly** — Remotes combine Tailwind with Radix primitives, `class-variance-authority`, `clsx`, and `tailwind-merge` (`cn()` helpers) for composable component variants.
4. **Tooling already wired** — `eslint-plugin-tailwindcss`, `prettier-plugin-tailwindcss`, and `format` scripts keep class order and usage consistent.
5. **Smaller unused CSS surface** — Tailwind scans each module’s `content` paths (`./src/**/*.{js,ts,jsx,tsx}`) and only emits used utilities into that module’s CSS bundle (`cssCodeSplit: false` keeps federation CSS loading predictable).

Theme customization lives in each module’s `tailwind.config.js` (brand colors, accordion animations, fonts such as Outfit on host, etc.). Entry CSS typically includes:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

---

## 9. Technology stack (shared)

| Layer | Choice |
|-------|--------|
| UI | React 18 + TypeScript |
| Bundler | Vite 6 |
| MFE | `@originjs/vite-plugin-federation` |
| Routing | `react-router-dom` v6 |
| Styling | Tailwind CSS 3 + PostCSS |
| Forms / validation | React Hook Form + Zod (feature modules) |
| Charts | Recharts (shared singleton) |
| Package manager (standard) | Yarn Classic 1.22.22 |
| Node | `>= 18` (CI uses 20) |

---

## 10. Deploy shape (high level)

1. Each module is built with Yarn.
2. Module `dist` assets (including `remoteEntry.js`) sync to S3/CloudFront under env prefixes (`dev` / `staging` / `prod`).
3. Host image is built/pushed (ECR) and deployed; it loads remotes from CDN URLs configured via env (`VITE_*_MODULE_URL`).

This keeps feature teams able to ship a remote independently while the host remains the stable composition layer.

---

## 11. Quick reference — ports & roles

| # | Module | Port | Role |
|---|--------|-----:|------|
| 1 | `home-module` | 5010 | Auth, layouts, shared providers/hooks |
| 2 | `user-module` | 5020 | Users, MSP/client management |
| 3 | `content-module` | 5030 | Courses, packages, exams, certificates |
| 4 | `billing-module` | 5050 | Payments, credits, licenses |
| 5 | `phishing-module` | 5070 | Simulation campaigns & breach |
| 6 | `account-module` | 5090 | Account, branding, notifications |
| 7 | `miscellaneous-module` | 5140 | Admin metadata, reports, support, policies |
| 8 | `host-module` | 5000 | Shell, routes, gateway proxy |

**Login minimum:** start `home-module` (deploy/serve), then `host-module` (`dev`), open `http://localhost:5000/auth/login`.
