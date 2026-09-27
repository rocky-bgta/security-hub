# Risk Profile & Scoring Logic – Implementation Plan

This document outlines the file-by-file changes required to implement the updated **4-class risk classification** and the **three scoring scenarios** (Training Only, Phishing Only, Combined).

---

## 1. Risk Profile Classification (Global)

| Score Range | Class     | Description |
|------------|-----------|-------------|
| 0–20       | **Low**   | User is compliant and vigilant. |
| 21–50      | **Medium**| User requires attention (missing training or minor phishing errors). |
| 51–70      | **High**  | User is a high security vulnerability. |
| 71–100     | **Critical** | User poses an imminent security threat. |

**Rule:** `riskLevel` / `riskGroup` must be derived from the numeric `riskScore` using these bands everywhere.

---

## 2. Scoring Logic Summary

| Scenario | Context | Training Component | Phishing Component | Total |
|----------|---------|--------------------|--------------------|-------|
| **A** (Training only) | Compliance only | 0–100 (see below) | — | = training |
| **B** (Phishing only) | Behavior only | — | 0–100 (see below) | = phishing |
| **C** (Both) | Full platform | Max 25 pts | Max 75 pts | = training pts + phishing pts |

### Scenario A – Training points (0–100)

| Training Status            | Points |
|---------------------------|--------|
| Completed Training        | 0      |
| Ongoing Training          | 40     |
| Not Completed / Started   | 70     |
| Overdue > 30 Days         | 100    |

### Scenario B – Phishing points (0–100)

| User Action                      | Points |
|----------------------------------|--------|
| Did not open / Never clicked     | 0      |
| Did not open but reported        | 0      |
| Opened but reported              | 10     |
| Opened                           | 20     |
| Clicked on link                  | 75     |
| Submitted information            | 100    |

### Scenario C – Combined (25 / 75 split)

- **Training (max 25):** Completed = 0, Ongoing = 15, Not Completed = 20, Overdue > 30 = 25.
- **Phishing (max 75):** Same actions, scaled: 0, 0, 10, 30, 60, 75.
- **Total** = training points + phishing points (max 100).

---

## 3. File-by-File Update Plan

### 3.1 Phishing Module

#### 3.1.1 Risk level from score (new utility)

- **New file (recommended):**  
  `services/phishing/core/src/main/java/com/aspire/asat/phishing/utils/RiskScoreUtils.java`  
  - Add `RiskLevel fromScore(double riskScore)`:
    - `score <= 20` → `LOW`
    - `21 <= score <= 50` → `MEDIUM`
    - `51 <= score <= 70` → `HIGH`
    - `71 <= score <= 100` → `CRITICAL`
  - Use this everywhere a numeric score must be converted to a risk level.

#### 3.1.2 Phishing points (Scenario B & C)

- **File:** `services/phishing/core/.../utils/PhishingRiskScorePoints.java`
  - **Current:** `OPENED = 35.0`, others: NONE=0, OPENED_BUT_REPORTED=10, CLICKED=75, DATA_SUBMITTED=100.
  - **Change for Scenario B (Phishing-only):**
    - Set `OPENED = 20.0` (was 35).
    - Keep: NONE=0, REPORTED_WITHOUT_OPEN=0, OPENED_BUT_REPORTED=10, CLICKED=75, DATA_SUBMITTED=100.
  - **Note:** For Scenario C, Phishing service can either:
    - Store 0–100 and scale when combining (see 3.1.5), or
    - Introduce a “combined mode” and store/apply 0–75 scaled values; recommended is to keep storing 0–100 and scale in the combined formula.

#### 3.1.3 UserRiskProfile entity

- **File:** `services/phishing/core/.../model/UserRiskProfile.java`
  - **Remove or deprecate:** `calculateRiskLevel()` that uses counts (dataSubmissions, linksClicked, etc.) and overwrites `riskScore`.
  - **Add:** `setRiskLevelFromScore()` that sets `this.riskLevel = RiskScoreUtils.fromScore(this.riskScore)` (using the new utility).
  - **Ensure:** Whenever `riskScore` is set (in this class or in services), call `setRiskLevelFromScore()` so classification stays in sync with the 4 bands.

#### 3.1.4 TrainingRiskScoreServiceImpl (Scenario A/B/C and weights)

- **File:** `services/phishing/core/.../service/impl/TrainingRiskScoreServiceImpl.java`
  - **Weights:** Change defaults from 30/70 to **25/75** (training / phishing). Use:
    - `@Value("${risk-score.training-weight:0.25}")`
    - `@Value("${risk-score.phishing-weight:0.75}")`
  - **Combined (Scenario C):**  
    When both `isTrainingEnabled` and `isPhishingEnabled` are true:
    - Map **training** 0–100 to 0–25:
      - 0 → 0, “ongoing” (e.g. 40) → 15, “not completed” (e.g. 70) → 20, 100 → 25 (e.g. helper `trainingPointsTo25(trainingRiskScore)`).
    - Map **phishing** 0–100 to 0–75:
      - 0→0, 10→10, 20→30, 75→60, 100→75 (e.g. helper `phishingPointsTo75(phishingRiskScore)`).
    - `riskScore = trainingPointsTo25(trainingRiskScore) + phishingPointsTo75(phishingRiskScore)`.
  - **Scenario A (training only):** When only training is enabled, `riskScore = trainingRiskScore` (0–100).
  - **Scenario B (phishing only):** When only phishing is enabled, `riskScore = phishingRiskScore` (0–100).
  - After computing `riskScore`, set `profile.setRiskLevel(RiskScoreUtils.fromScore(profile.getRiskScore()))` (or call `profile.setRiskLevelFromScore()` if implemented on entity).

#### 3.1.5 TrackingServiceImpl (phishing score → overall score & level)

- **File:** `services/phishing/core/.../service/impl/TrackingServiceImpl.java`
  - **Current:** Only updates `UserRiskProfile.phishingRiskScore` (average of recipient scores).
  - **Change:** After updating `phishingRiskScore`, recalculate **overall** `riskScore` and `riskLevel`:
    - If only phishing enabled: `riskScore = phishingRiskScore`, then set `riskLevel` from score (e.g. `RiskScoreUtils.fromScore(riskScore)`).
    - If both enabled: use the same combined formula as in `TrainingRiskScoreServiceImpl` (25/75 with 0–25 and 0–75 mapping).
    - If only training enabled: leave overall score/level as-is (training sync will set it).
  - Persist updated `riskScore` and `riskLevel` on `UserRiskProfile`.

#### 3.1.6 AnalyticsServiceImpl (recalculate risk)

- **File:** `services/phishing/core/.../service/impl/AnalyticsServiceImpl.java`
  - **Current:** Calls `profile.calculateRiskLevel()` which is count-based.
  - **Change:** Replace with logic that:
    - Recomputes `riskScore` from stored `trainingRiskScore` and `phishingRiskScore` using the same scenario rules (A/B/C) as in 3.1.4 and 3.1.5.
    - Then sets `riskLevel` from `riskScore` via `RiskScoreUtils.fromScore(riskScore)` (or `profile.setRiskLevelFromScore()`).

#### 3.1.7 UserRiskProfileServiceImpl

- **File:** `services/phishing/core/.../service/impl/UserRiskProfileServiceImpl.java`
  - When saving/updating a profile with explicit `riskScore` (e.g. from Registration or API), after setting `riskScore`, set `riskLevel` from score using the new utility so the 4-class rule is always applied.

#### 3.1.8 Configuration

- **File:** `services/phishing/service/src/main/resources/application.yml`
  - Under `risk-score`, set:
    - `training-weight: 0.25`
    - `phishing-weight: 0.75`

#### 3.1.9 API / DTOs (Phishing)

- **File:** `services/phishing/api/.../dto/enums/RiskLevel.java`
  - **Already has:** LOW, MEDIUM, HIGH, CRITICAL. No change to enum values; ensure all usages derive level from score bands above.
- **File:** `services/phishing/api/.../dto/response/UserRiskDistributionDto.java`  
  - Already has low/medium/high/critical counts; no structural change if risk levels are computed correctly elsewhere.

#### 3.1.10 Report & CSV

- **File:** `services/phishing/core/.../service/impl/ReportServiceImpl.java`
  - No change to risk level enum; ensure data comes from profiles whose `riskLevel` is set from score (so no code change if 3.1.3–3.1.7 are done).

---

### 3.2 CMS Module (Training)

#### 3.2.1 Training status → points (Scenario A & C)

- **File:** `services/cms/core/.../model/UserSubPackage.java`
  - **Method:** `setRiskScoreFromStatus()`
  - **Current:** NOT_STARTED=100, COMPLETED=0, EXAM/IN_PROGRESS=40.
  - **Change to Scenario A:**
    - COMPLETED → **0**
    - EXAM / IN_PROGRESS (Ongoing) → **40**
    - NOT_STARTED (Not Completed / Started) → **70**
    - **Overdue > 30 days** → **100**
  - **Overdue rule:** If `status` is NOT_STARTED or IN_PROGRESS and `expiryDate != null` and `expiryDate.plusDays(30).isBefore(LocalDate.now())`, set riskScore = 100; otherwise NOT_STARTED = 70, EXAM/IN_PROGRESS = 40, COMPLETED = 0.
  - **Scenario C (combined):** CMS may need to send a “combined-mode” score (0–25) to Phishing, or Phishing will map 0–100 training to 0–25. Plan: keep sending 0–100 from CMS; Phishing maps to 0–25 when both modules are enabled (see 3.1.4).

#### 3.2.2 Sync service (optional scaling for Scenario C)

- **File:** `services/cms/core/.../service/impl/TrainingRiskScoreSyncServiceImpl.java`
  - **Current:** Sends average of `UserSubPackage.riskScore` (0–100) to Phishing.
  - **Option A (recommended):** Keep sending 0–100; Phishing applies 0–25 mapping when in combined mode (no change here).
  - **Option B:** If client is “combined mode”, compute average in 0–25 scale (e.g. map each status to 0/15/20/25 and average). Requires client/scenario flag; more invasive.

#### 3.2.3 User Risk Analysis (CMS dashboard)

- **File:** `services/cms/api/.../dto/enums/RiskCategory.java`
  - **Current:** Progress-based: SAFE (90+%), LOW_RISK (70+), AVERAGE_RISK (50+), HIGH_RISK (&lt;50).
  - **Options:**
    - **Keep as-is** for “training progress” view (separate from the 4-class risk score).
    - **Or** add a second view that uses the same 4 bands (LOW / MEDIUM / HIGH / CRITICAL) if CMS later receives or computes a 0–100 risk score (e.g. from Phishing or from a local score). If you add it, add `RiskCategory.fromRiskScore(double score)` mapping 0–20→LOW, 21–50→MEDIUM, 51–70→HIGH, 71–100→CRITICAL, and use it where you show “risk by score”.
- **File:** `services/cms/core/.../service/impl/UserRiskAnalysisServiceImpl.java`
  - If you keep progress-based categories, no change. If you introduce score-based 4-class, use the new mapping where appropriate (e.g. new endpoint or optional parameter).
- **File:** `services/cms/api/.../dto/clientDashboard/UserRiskDetailDto.java`
  - Uses `RiskCategory`; only change if you add score-based category (e.g. second field or rename).

#### 3.2.4 ClientUserOperationServiceImpl

- **File:** `services/cms/core/.../service/user_operations/ClientUserOperationServiceImpl.java`
  - All call sites that call `userSubPackage.setRiskScoreFromStatus()` or `userPackage.setRiskScoreFromStatus()` remain; ensure `UserSubPackage.setRiskScoreFromStatus()` uses the new logic including overdue (so pass or use `expiryDate` in the model). If `setRiskScoreFromStatus()` needs `expiryDate`, it’s already on the entity; no signature change if it reads `this.expiryDate`.

---

### 3.3 User Registration Module

#### 3.3.1 RiskGroup enum (4th class)

- **Files:**
  - `services/registration/api/.../data/enums/RiskGroup.java`
  - `services/auth/api/.../dto/enums/RiskGroup.java`
- **Change:** Add **CRITICAL_RISK** so that filter/APIs support four bands.
  - Values: `LOW_RISK`, `MEDIUM_RISK`, `HIGH_RISK`, `CRITICAL_RISK`.
- **Mapping:** When displaying or syncing from Phishing `RiskLevel` or from a numeric score:
  - LOW → LOW_RISK, MEDIUM → MEDIUM_RISK, HIGH → HIGH_RISK, CRITICAL → CRITICAL_RISK.

#### 3.3.2 Where RiskGroup is set

- **Files:**  
  `EndUserServiceImpl`, `SystemUserServiceImpl`, `TrialSignupServiceImpl`
- **Current:** Default or explicit `RiskGroup.HIGH_RISK` in places.
- **Change:** If Registration ever receives risk score or risk level from Phishing (e.g. on sync or when loading user), set `riskGroup` from score using the same bands (0–20→LOW_RISK, …, 71–100→CRITICAL_RISK). If there is no sync today, only add CRITICAL_RISK and use it where you need to show/filter “Critical” users.

#### 3.3.3 DTOs and controllers

- **Files:**  
  `EndUserResponseDTO`, `AspireUserDto`, `AspireUserCreateRequestDto`, `SystemUserResponseDTO`, `EndUserController` / `EndUserControllerImpl`
- **Change:** Support the new enum value `CRITICAL_RISK` (already if they use the enum). Add to any filter dropdowns or API docs (e.g. Swagger) for `riskGroup` filter.

#### 3.3.4 Phishing request/response DTOs (Registration)

- **Files:**  
  `registration/api/.../data/phishing/request/UserRiskProfileSaveRequestDto.java`,  
  `registration/api/.../data/phishing/response/UserRiskSummaryDto.java`,  
  `registration/api/.../data/phishing/enums/RiskLevel.java`
- **RiskLevel** already has LOW, MEDIUM, HIGH, CRITICAL; no enum change. Ensure that when Registration displays or forwards Phishing risk data, it uses the score-based level from Phishing.

---

### 3.4 Auth Module

- **File:** `auth/api/.../dto/enums/RiskGroup.java`
  - Add `CRITICAL_RISK` (keep in sync with Registration).
- **Files:** `UserDetailsResponse`, `AccessTokenResponse`, `UserMapper`, `AspireUser` (auth entity)
  - Support the new enum value; no structural change if they already use `RiskGroup`.

---

### 3.5 Other / Shared

- **RegistrationServiceClient (Phishing):** No change to risk scoring; it only calls Registration APIs (e.g. list users by risk group). Ensure Registration exposes CRITICAL_RISK in filters if needed.
- **PhishingClient (CMS):** No change to request DTO; CMS still sends `clientAdminId` and `riskScore` (0–100). Phishing applies scenario logic.
- **Excel / reports:** Any export that shows “risk category” or “risk level” should use the 4-class rule from score (handled once profile’s `riskLevel` is set correctly in Phishing).

---

## 4. Implementation Order (Suggested)

1. **Phishing:** Add `RiskScoreUtils.fromScore()`, update `PhishingRiskScorePoints` (OPENED = 20), then `UserRiskProfile.setRiskLevelFromScore()` and remove/deprecate count-based `calculateRiskLevel()`.
2. **Phishing:** Update `TrainingRiskScoreServiceImpl` (25/75, Scenario A/B/C, set risk level from score).
3. **Phishing:** Update `TrackingServiceImpl` to set overall `riskScore` and `riskLevel` when phishing score changes.
4. **Phishing:** Update `AnalyticsServiceImpl` recalculation to use score-based level.
5. **Phishing:** Config and `UserRiskProfileServiceImpl` as above.
6. **CMS:** Update `UserSubPackage.setRiskScoreFromStatus()` (0/40/70/100 + overdue > 30 days).
7. **Registration + Auth:** Add `CRITICAL_RISK` to `RiskGroup` and support it in filters/DTOs.
8. **Optional:** CMS RiskCategory score-based 4-class if product wants it; otherwise leave progress-based as-is.

---

## 5. Summary Table

| Area            | File(s) | Main change |
|----------------|---------|-------------|
| Phishing        | New `RiskScoreUtils` | `fromScore(double)` → LOW/MEDIUM/HIGH/CRITICAL by bands |
| Phishing        | `PhishingRiskScorePoints` | OPENED = 20 |
| Phishing        | `UserRiskProfile` | `setRiskLevelFromScore()`, remove count-based `calculateRiskLevel()` |
| Phishing        | `TrainingRiskScoreServiceImpl` | 25/75, Scenario A/B/C, set riskLevel from score |
| Phishing        | `TrackingServiceImpl` | Recalc overall riskScore & riskLevel when phishing updates |
| Phishing        | `AnalyticsServiceImpl` | Recalc from score; set level via utility |
| Phishing        | `UserRiskProfileServiceImpl` | Set riskLevel from score when saving |
| Phishing        | `application.yml` | training-weight: 0.25, phishing-weight: 0.75 |
| CMS             | `UserSubPackage` | setRiskScoreFromStatus: 0/40/70/100 + overdue>30 → 100 |
| CMS             | `RiskCategory` / `UserRiskAnalysisServiceImpl` | Optional: score-based 4-class |
| Registration    | `RiskGroup` (api) | Add CRITICAL_RISK |
| Auth            | `RiskGroup` (api) | Add CRITICAL_RISK |
| Registration    | EndUser list/filter, DTOs | Support CRITICAL_RISK in filter and responses |

This plan keeps the 4-class classification consistent and implements all three scoring scenarios (Training only, Phishing only, Combined 25/75) across Phishing, CMS, and Registration/Auth.

---

## 6. Verification Checklist (recheck calculations)

### 4-class classification (RiskScoreUtils.fromScore)
| Score   | Class    | Spec  | Code (LOW_MAX=20, MEDIUM_MAX=50, HIGH_MAX=70) |
|---------|----------|-------|------------------------------------------------|
| 0–20    | Low      | ✓     | score ≤ 20 → LOW ✓                             |
| 21–50   | Medium   | ✓     | score ≤ 50 → MEDIUM ✓                          |
| 51–70   | High     | ✓     | score ≤ 70 → HIGH ✓                            |
| 71–100  | Critical | ✓     | else → CRITICAL ✓                              |

### Scenario A – Training only (UserSubPackage + CMS sync)
| Status           | Spec pts | Code (UserSubPackage.setRiskScoreFromStatus) |
|------------------|----------|---------------------------------------------|
| Completed        | 0        | COMPLETED → 0.0 ✓                           |
| Ongoing          | 40       | EXAM, IN_PROGRESS → 40.0 ✓                   |
| Not Completed    | 70       | NOT_STARTED → 70.0 ✓                        |
| Overdue > 30 days| 100      | expiryDate+30 &lt; today → 100.0 ✓           |

### Scenario B – Phishing only (PhishingRiskScorePoints)
| User action              | Spec pts | Code constant / value |
|--------------------------|----------|------------------------|
| Did not open / never clicked | 0   | NONE = 0 ✓             |
| Did not open but reported   | 0   | REPORTED_WITHOUT_OPEN = 0 ✓ |
| Opened but reported        | 10  | OPENED_BUT_REPORTED = 10 ✓  |
| Opened                     | 20  | OPENED = 20 ✓               |
| Clicked on link            | 75  | CLICKED = 75 ✓              |
| Submitted information      | 100 | DATA_SUBMITTED = 100 ✓      |

### Scenario C – Combined (RiskScoreUtils.trainingPointsTo25 + phishingPointsTo75)
| Training (CMS sends) | Spec pts | trainingPointsTo25 |
|----------------------|----------|--------------------|
| 0 (completed)        | 0        | ≤20 → 0 ✓          |
| 40 (ongoing)         | 15       | ≤50 → 15 ✓         |
| 70 (not completed)   | 20       | ≤85 → 20 ✓         |
| 100 (overdue)        | 25       | else → 25 ✓        |

| Phishing (raw 0–100) | Spec pts | phishingPointsTo75 |
|----------------------|----------|--------------------|
| 0                    | 0        | ≤0 → 0 ✓           |
| 10 (opened+reported) | 10       | ≤15 → 10 ✓         |
| 20 (opened)          | 30       | ≤50 → 30 ✓         |
| 75 (clicked)         | 60       | ≤87 → 60 ✓         |
| 100 (submitted)      | 75       | else → 75 ✓        |

**Total** = training points (max 25) + phishing points (max 75) = max 100 ✓
