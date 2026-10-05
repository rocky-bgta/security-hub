# ASAT V2 Frontend — Local Setup Guide

This guide runs the **federated** frontend against a **local backend gateway** (`http://localhost:7030`), with Mongo data on the remote server used by the backend.

Complete backend first: `ASAT-V2-BACKEND/docs/local-setup/setup_local.md`

Typical repo path:

```text
C:\Git-Repo\aspire\ASAT-V2-FRONTEND
```

**Login minimum:** ▶ **1 home** → wait until build/serve finishes → ▶ **8 host** → open:

```text
http://localhost:5000/auth/login
```

---

## 0. Run each module with ▶ (one by one)

### Table ▶ buttons (IntelliJ / WebStorm)

The ▶ next to a module name in the table below comes from **IDE run configurations** in `.idea/runConfigurations/`.

| Config name | Runs | Notes |
|-------------|------|--------|
| `home-module` … `miscellaneous-module` | `npm run deploy` | = `build` + `preview`/`serve` (real `remoteEntry.js`) |
| `host-module` | `npm run dev` | App shell + `/gateway` proxy |

If you only see ▶ on `host-module`, reload the IDE / reopen this file so the new configs under `.idea/runConfigurations/` are picked up.

Click ▶ on each module **in order** (home first … host last). Leave each run window open.

### Rules

| Wrong | Right |
|-------|--------|
| Only `host-module` | ▶ `home-module` first, then ▶ `host-module` last |
| `npm run dev` on a remote | Remotes use `deploy` (build + preview) |
| ▶ host before home is ready | Wait until home is listening on **5010** |
| Opening `:5000` with no home | Blank login / `remoteEntry.js` 404 |

### What each module serves — click ▶ in the Module column

| # | Module | Port | Serves |
|---|--------|-----:|--------|
| 1 | `home-module` | `5010` | **Login** at `/auth/login`, MFA, reset/set password, auth/store providers, layouts, dashboard shell |
| 2 | `user-module` | `5020` | **Users** — management, onboarding, bulk import, sync, user lists/analytics |
| 3 | `content-module` | `5030` | **Content** — packages, courses, content library, exams, certificates, knowledge, policies |
| 4 | `billing-module` | `5050` | **Billing** — invoices, payments, credits, licenses (port may clash with CMS) |
| 5 | `phishing-module` | `5070` | **Phishing** — phishing / smishing / vishing / deepfake campaigns & templates |
| 6 | `account-module` | `5090` | **Account** — profile, security, branding, notification preferences |
| 7 | `miscellaneous-module` | `5140` | **Reports / admin** — User Report, clients/MSP, menus, settings, support |
| 8 | `host-module` | `5000` | **App shell** at `:5000`, routing, `/gateway` proxy — loads remotes into pages |

**Required for login:** ▶ `home-module` then ▶ `host-module`. Start 2–7 only when you need that area in the UI.

---

### Click ▶ — modules (same pattern for each)

Each block is one module. Click ▶ in order. Scripts: `docs/scripts/start-*-module.ps1`.

**1 — `home-module`** — port `5010`  
Serves: **Login** at `/auth/login`, MFA, reset/set password, auth/store providers, layouts, dashboard shell. **Required.**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\start-home-module.ps1"
```

**2 — `user-module`** — port `5020`  
Serves: **Users** — management, onboarding, bulk import, sync, user lists/analytics. Optional until User menus.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\start-user-module.ps1"
```

**3 — `content-module`** — port `5030`  
Serves: **Content** — packages, courses, content library, exams, certificates, knowledge, policies. Optional until Content menus.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\start-content-module.ps1"
```

**4 — `billing-module`** — port `5050`  
Serves: **Billing** — invoices, payments, credits, licenses. Optional until Billing. Port may clash with CMS.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\start-billing-module.ps1"
```

**5 — `phishing-module`** — port `5070`  
Serves: **Phishing** — phishing / smishing / vishing / deepfake campaigns & templates. Optional until Phishing menus.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\start-phishing-module.ps1"
```

**6 — `account-module`** — port `5090`  
Serves: **Account** — profile, security, branding, notification preferences. Optional until Account menus.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\start-account-module.ps1"
```

**7 — `miscellaneous-module`** — port `5140`  
Serves: **Reports / admin** — User Report, clients/MSP, menus, settings, support. Optional until Reports / admin extras.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\start-miscellaneous-module.ps1"
```

**8 — `host-module`** — port `5000` — **click last**  
Serves: **App shell** at `:5000`, routing, `/gateway` proxy — loads remotes into pages. **Required.** Wait until home is on `5010` first.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\start-host-module.ps1"
```

---

### Helpers (optional ▶)

**Check which remotes are ready**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\check-frontend.ps1"
```

**Stop frontend node processes on FE ports**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Git-Repo\aspire\ASAT-V2-FRONTEND\docs\scripts\stop-frontend.ps1"
```

Then open `http://localhost:5000/auth/login` (Ctrl+F5). Backend (Redis, Auth, Registration, Gateway) must already be up.

---

## 1. Prerequisites

| Tool | Notes |
|------|--------|
| Node.js | `>= 18` |
| npm or yarn | Either works; use the same package manager per module |
| Backend | Redis + Auth + Registration + Gateway running (see backend guide) |
| Browser | Always use host: `http://localhost:5000` — not home `:5010` for the app UI |

### Federation rule (do not skip)

`@originjs/vite-plugin-federation` only writes a real `assets/remoteEntry.js` into **build output**.

| Module type | Allowed scripts | Port |
|-------------|-----------------|------|
| **Remotes** (home, miscellaneous, user, content, billing, phishing, account) | `npm run build` then `npm run serve` — or `npm run deploy` | see §0 |
| **Host only** | `npm run dev` | 5000 |

**Never** use `npm run dev` / `yarn dev` for a remote if the host will load it. Dev serves the SPA HTML at `/assets/remoteEntry.js` → host gets HTML → blank page or “Failed to fetch dynamically imported module”.

Host uses `npm run dev` so Vite can proxy `/gateway` → `http://localhost:7030`.

---

## 2. Architecture (what talks to what)

```text
Browser
  -> http://localhost:5000          host-module (Vite DEV)
        |-- /gateway/*  proxy ----> http://localhost:7030  (backend gateway)
        |-- loads remotes (MUST be build + serve / preview)
              home-module          http://localhost:5010/assets/remoteEntry.js
              miscellaneous-module http://localhost:5140/assets/remoteEntry.js
              user / content / ... other ports when those pages are used
```

Login UI is **not** implemented in host. Host lazy-loads `home-module/Login` from:

`host-module/src/pages/auth/Login.tsx` → `import('home-module/Login')`

That import needs `http://localhost:5010/assets/remoteEntry.js` available as **JavaScript**.

Login axios `BASE_URL` is baked from **home-module** `VITE_API_BASE_URL` at **home build** time — changing home `.env` requires a rebuild.

---

## 3. What must change (env + code)

### 3.1 Host — `host-module/.env.local`

Do **not** leave a second `VITE_API_BASE_URL` pointing at remote or `:7030` if you want the Vite proxy.

```env
VITE_PUBLIC_URL=http://localhost:5000
VITE_HOME_MODULE_URL=http://localhost:5010
VITE_USER_MODULE_URL=http://localhost:5020
VITE_CONTENT_MODULE_URL=http://localhost:5030
VITE_MISCELLANEOUS_MODULE_URL=http://localhost:5140
VITE_BILLING_MODULE_URL=http://localhost:5050
VITE_ACCOUNT_MODULE_URL=http://localhost:5090
VITE_PHISHING_MODULE_URL=http://localhost:5070
VITE_API_BASE_URL=/gateway
```

Notes:

- `/gateway` is relative. Host Vite proxies it to `http://localhost:7030` (`host-module/vite.config.ts`).
- **Port 5040** is often taken by Windows `svchost`. Miscellaneous uses **5140** locally. If you keep 5040 in env, User Report shows *Error! Something went wrong when loading remote app.*
- Host `.env` may still point at CDN URLs; **`.env.local` overrides** for local work — keep local URLs here.

### 3.2 Home — `home-module/.env`

Only **one** `VITE_API_BASE_URL`. Last duplicate key wins in dotenv.

```env
VITE_PUBLIC_URL=http://localhost:5010
VITE_CONTENT_MODULE_URL=http://localhost:5030
VITE_PHISHING_MODULE_URL=http://localhost:5070
VITE_TINYMCE_URL=http://localhost:5000

VITE_API_BASE_URL=/gateway
VITE_STORAGE_SECRET=U2FsdGVkX1+yskVjqoe803ZhO0eGB2Zlkw5r6RIu/ztlKbQfJ5fGJMc+qA1WADFe
VITE_APP_VERSION=1.0.0
```

After any `.env` change: **rebuild** home (`npm run build`) then `npm run serve`. Restarting `serve` alone is not enough if `dist` was built with old env.

Do **not** use `npm run dev` on home for host federation. Dev may proxy `/gateway`, but it does **not** expose a usable federated `remoteEntry.js` for the host.

### 3.3 Miscellaneous — `miscellaneous-module/.env`

User Report (`/report-management/user-report`) is a remote from this module.

```env
VITE_PUBLIC_URL=http://localhost:5140
VITE_HOME_MODULE_URL=http://localhost:5010
VITE_API_BASE_URL=/gateway
```

`miscellaneous-module/package.json` scripts must use port **5140**:

```json
"dev": "vite --port 5140 --strictPort --host",
"serve": "vite preview --port 5140 --strictPort --host"
```

Still use `build` + `serve` for host federation (not `dev`).

### 3.4 Other remotes (when you open those menus)

Point API at local gateway. Prefer `/gateway` if that module is loaded through host (same origin as host). If a remote is opened as its own origin, use `http://localhost:7030/gateway` **or** `/gateway` plus that module’s Vite proxy.

Examples already in repo:

- `user-module/.env.local` — `VITE_API_BASE_URL=http://localhost:7030/gateway`
- `content-module/.env.local` — same

| Module | Default Vite port | Typical `VITE_PUBLIC_URL` |
|--------|-------------------|---------------------------|
| user-module | 5020 | `http://localhost:5020` |
| content-module | 5030 | `http://localhost:5030` |
| billing-module | 5050 | `http://localhost:5050` (clashes with CMS backend 5050) |
| phishing-module | 5070 | `http://localhost:5070` |
| account-module | 5090 | `http://localhost:5090` |

Same federation rule: `build` + `serve` (or `deploy`) so `remoteEntry.js` is real JavaScript, not HTML.

### 3.5 Code already required in this repo (verify present)

These fixes are required for local + remote Mongo:

| File | Change |
|------|--------|
| `home-module/src/providers/StoreProvider.tsx` | Fetch `/registration/api/v1/role-permissions` **without** appending email. Parallel fetch user-details vs menus. Expose `isStoreReady`. Unblock UI after user-details (menus can take ~40s). |
| `home-module/src/models/Context.ts` | `isStoreReady` on store context |
| `home-module/src/components/layouts/BaseLayout.tsx` | Spinner until `isStoreReady && userInfo.userId`, **not** `menus.length === 0` |
| `host-module/src/components/layout/BaseLayout.tsx` | Same spinner rule |

If `BaseLayout` still waits on `menus.length === 0`, the page stays on a black spinner after login.

---

## 4. Frontend services to run

Use **§0** — one ▶ per module. Summary:

| Order | Module | Port | Serves | Required? |
|------:|--------|-----:|--------|-----------|
| 1 | `home-module` | `5010` | **Login** at `/auth/login`, MFA, providers, layouts, dashboard shell | Yes |
| 2 | `user-module` | `5020` | **Users** — management, onboarding, bulk import, sync | When using User menus |
| 3 | `content-module` | `5030` | **Content** — packages, courses, exams, certificates | When using Content menus |
| 4 | `billing-module` | `5050` | **Billing** — invoices, payments, credits, licenses | When using Billing |
| 5 | `phishing-module` | `5070` | **Phishing** — phishing / smishing / vishing / deepfake | When using Phishing menus |
| 6 | `account-module` | `5090` | **Account** — profile, security, branding | When using Account menus |
| 7 | `miscellaneous-module` | `5140` | **Reports / admin** — User Report, clients/MSP, settings | When using Reports etc. |
| 8 | `host-module` | `5000` | **App shell** at `:5000`, routing, `/gateway` proxy | Yes — always last |

If a remote is down after login, host often shows: **Error! Something went wrong when loading remote app.**  
If **home** is down at login time, you usually get a **blank page** + console 404 on `5010/assets/remoteEntry.js`.

---

## 5. Step-by-step: start frontend

Do this **after** backend Redis, Auth, Registration, and Gateway are up.

**Easiest path:** §0 ▶ home, then ▶ host (and any other remotes you need). Steps below are the same commands without `Start-Process`.

### Step 1 — Confirm backend

```text
http://localhost:7030/gateway/auth/api/v1/auth/login   (POST must work)
```

### Step 2 — Confirm ports are free

```powershell
foreach ($p in 5000, 5010, 5140) {
  $c = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
  if ($c) { "BUSY $p pid=$($c.OwningProcess)" } else { "FREE $p" }
}
```

If 5010/5000/5140 are busy with old Node, stop those Node PIDs. Do **not** kill `svchost` on 5040.

If something is already listening on 5010 from a previous `yarn dev` / `npm run dev`, stop it and restart with `build` + `serve` — otherwise you keep getting HTML or 404 for `remoteEntry.js`.

### Step 3 — Home module (login + providers) — required

Prefer §0 ▶ **1 — home-module**. Manual equivalent:

```powershell
cd C:\Git-Repo\aspire\ASAT-V2-FRONTEND\home-module
npm install
npm run build
npm run serve
```

Keep this terminal open. Leave it running; do not close it when starting host.

**Mandatory check — remote must be JS, not HTML / not 404:**

```powershell
$r = Invoke-WebRequest http://localhost:5010/assets/remoteEntry.js
$r.StatusCode
$r.Headers['Content-Type']
$r.Content.Substring(0, [Math]::Min(80, $r.Content.Length))
```

Expect:

- Status `200`
- Content-Type containing `javascript`
- Body starting with JS (`const` / `var` / `import`…), **not** `<!doctype html>`

| Result | Meaning | Fix |
|--------|---------|-----|
| Connection refused | Nothing on 5010 | Run home `build` + `serve` |
| 404 | Wrong process or incomplete build | Rebuild; use `serve`/`deploy`, not `dev` |
| 200 + HTML | You ran `dev` | Stop process; `npm run build` then `npm run serve` |
| 200 + JS | OK — start host | Continue to Step 5 (or Step 4 if you need User Report) |

### Step 4 — Miscellaneous module (User Report) — optional for login

Prefer §0 ▶ **7 — miscellaneous-module**. Manual equivalent:

```powershell
cd C:\Git-Repo\aspire\ASAT-V2-FRONTEND\miscellaneous-module
npm install
npm run build
npm run serve
```

Check:

```powershell
Invoke-WebRequest http://localhost:5140/assets/remoteEntry.js
```

Expect JS 200. Skip this step if you only need login/dashboard.

### Step 5 — Host module

Prefer §0 ▶ **8 — host-module** (last). Manual equivalent:

```powershell
cd C:\Git-Repo\aspire\ASAT-V2-FRONTEND\host-module
npm install
npm run dev
```

Keep this terminal open. Env is `host-module/.env.local` (must point `VITE_HOME_MODULE_URL` at `http://localhost:5010`).

### Step 6 — Optional remotes

Use §0 ▶ blocks **2–6** for any extra module, or:

```powershell
cd C:\Git-Repo\aspire\ASAT-V2-FRONTEND\<module>
npm install
# set .env / .env.local as in section 3
npm run build
npm run serve
```

### Step 7 — Open the app

1. Confirm home preflight (Step 3) still passes.
2. Hard-refresh: `http://localhost:5000/auth/login` (Ctrl+F5).
3. Login: `siddik-aspire-admin@yopmail.com` / `Asat@123` (or your user).
4. With backend `MFA_ENABLED=false`, you should **not** see “Enter Verification Code”.
5. Dashboard should appear after user-details (a few seconds). Sidebar menus may fill later.
6. Open **Report Management → User Report**: `http://localhost:5000/report-management/user-report` (needs miscellaneous on 5140).

---

## 6. Restart rules

| You changed | Restart / rebuild |
|-------------|-------------------|
| `home-module/.env` | `npm run build` + `npm run serve` again |
| `home-module` source used by login/layouts | Rebuild + `serve` home; hard-refresh host |
| `host-module/.env.local` | Restart `npm run dev` |
| `miscellaneous-module/.env` | Rebuild + `serve` |
| StoreProvider / BaseLayout | Rebuild **home** (and restart host if host BaseLayout changed) |
| Backend MFA / Mongo | Restart **auth** (backend); FE only refresh |

---

## 7. Verify from the browser / Network tab

Before login works, Network tab should show:

```text
GET http://localhost:5010/assets/remoteEntry.js   200   javascript
```

Successful password login:

```text
POST http://localhost:5000/gateway/auth/api/v1/auth/login
-> 200, accessToken present, mfaVerificationRequired not true
```

Then:

```text
GET /gateway/auth/api/v1/auth/user-details          200
GET /gateway/registration/api/v1/branding           200
GET /gateway/registration/api/v1/role-permissions   200 (may be slow)
```

User Report:

```text
GET http://localhost:5140/assets/remoteEntry.js     200 javascript
GET /gateway/registration/api/v1/reports/user-summary?...  200
```

Host must **not** request `http://localhost:7030/...` from the browser if using `/gateway` (that is cross-origin and can 403 OPTIONS). Requests should be same-origin `http://localhost:5000/gateway/...`.

---

## 8. Troubleshooting

| Symptom | Cause | Fix |
|---------|--------|-----|
| **Blank white page** on `/auth/login` + console `404` / `Failed to fetch dynamically imported module` for `http://localhost:5010/assets/remoteEntry.js` | Home not running, or home started with `dev` | ▶ stop helper, then ▶ **1 home**, wait, ▶ **8 host** |
| Login “Request failed” | FE hit local gateway with auth down, or `/gateway` override vs remote mix | Backend auth+gateway up; single `VITE_API_BASE_URL=/gateway`; rebuild home |
| Login works on `dev.aspireelearning.com` only | Home env still `/gateway` to broken local stack, or still pointing at remote inconsistently | Align env; rebuild remotes |
| Black spinner after login | `BaseLayout` waits for menus; `role-permissions` slow/fail | Use `isStoreReady` layout; registration SSM off + Mongo URI |
| MFA / verification code | Auth `mfa.enabled=true` | Backend `MFA_ENABLED=false`, restart auth |
| “loading remote app” on User Report | Misc not served / wrong port / 5040 occupied | ▶ **7 miscellaneous** on **5140**; host `VITE_MISCELLANEOUS_MODULE_URL=http://localhost:5140` |
| `remoteEntry.js` is HTML | `vite` **dev** instead of preview of build | Stop `dev`; use §0 ▶ remote (`build` + `serve`) |
| `remoteEntry.js` 404 while something listens on 5010 | Stale/`dev` process or build never produced `dist/assets/remoteEntry.js` | Kill PID on 5010; rebuild; `serve` again |
| Duplicate `VITE_API_BASE_URL` in home `.env` | Last line wins | Keep only `/gateway` |
| Dashboard CMS 500 | CMS not running on 5050 | Start CMS or ignore widget errors |

---

## 9. Start order recap

```text
BACKEND (see backend setup_local.md)
  Redis -> Auth :9093 -> Registration :9090 -> Gateway :7030

FRONTEND — §0 one ▶ per module (in order)
  1) home-module           build + serve   :5010   Login / providers / layouts   REQUIRED
  2) user-module           build + serve   :5020   Users                         optional
  3) content-module        build + serve   :5030   Packages / courses / exams    optional
  4) billing-module        build + serve   :5050   Billing                       optional
  5) phishing-module       build + serve   :5070   Phishing simulations          optional
  6) account-module        build + serve   :5090   Account / branding            optional
  7) miscellaneous-module  build + serve   :5140   Reports / clients / settings  optional
  8) host-module           npm run dev     :5000   Shell + /gateway proxy        REQUIRED last

BROWSER
  http://localhost:5000/auth/login
```

| Wrong | Right |
|-------|--------|
| Only `host-module` running | ▶ 1 home, then ▶ 8 host |
| Combined start of everything | ▶ each module one by one in §0 |
| `dev` on remotes | `build` + `serve` on remotes; `dev` only on host |

**Do not** use `https://dev.aspireelearning.com/gateway` in local module env if you intend to exercise **local** backend.
