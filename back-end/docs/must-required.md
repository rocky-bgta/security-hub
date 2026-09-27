# ICC Talk — Must-Required Capabilities for ASAT Vishing

**Audience:** ICC Communication Limited (ICC Talk) technical team  
**Purpose:** Minimum API/platform capabilities needed to integrate ICC Talk as a voice provider for ASAT vishing (security-awareness simulated voice calls), equivalent to our current Twilio flow.

---

## Business context (one paragraph)

ASAT places **outbound** simulated phishing calls to employee phone numbers. After answer we **play a scripted audio** (pre-generated cloned voice), **capture the recipient’s DTMF and/or speech**, then **end the call** with a short closing message. We need programmatic call control and HTTPS callbacks into our backend—not a manual dialer or fixed IVR-only menus.

---

## Must-have (blocking)

Without these, we cannot integrate ICC Talk for production vishing.

### 1. Outbound call API

| Requirement | Detail |
|-------------|--------|
| Programmatic dial | REST (or documented SDK) to create an outbound voice call |
| Addressing | `To` (destination) and `From` / CLI (caller ID) |
| Correlation ID | Ability to pass our opaque ID (e.g. `trackingId`) and receive it on **all** callbacks |
| Auth | Documented authentication (API key / OAuth / mTLS / IP allowlist) |
| Call identifier | Stable call/session ID returned on create |
| Errors | Documented HTTP status codes and error body |

### 2. Media playback after answer

| Requirement | Detail |
|-------------|--------|
| Play audio | Play our file from a **public HTTPS URL** (prefer S3/CDN presigned URL) **or** accepted upload + play-by-id |
| Formats | Document supported codecs (e.g. MP3 / WAV) and limits (size, duration) |
| Timing | Audio starts promptly after answer (document expected latency) |

> We already synthesize cloned-voice audio before dial. **Play-from-HTTPS-URL is strongly preferred.** Inline TTS is optional, not required.

### 3. Input gather (DTMF minimum)

| Requirement | Detail |
|-------------|--------|
| DTMF capture | Collect digits during/after playback |
| Configurable gather | Digit count and/or finish key (e.g. `#`), timeout, empty-result handling |
| Result delivery | Push gather result to **our HTTPS webhook** (digits + correlation / call ID) |

Speech-to-text gather is **desired** (see Should-have). **DTMF alone is the must-have MVP.**

### 4. Call-status webhooks

| Requirement | Detail |
|-------------|--------|
| Event push | HTTPS callbacks for call lifecycle |
| Minimum events | At least: ringing (or equivalent), answered / in-progress, completed, no-answer, busy/failed/canceled |
| Payload | Call status, duration (on complete), our correlation ID, their call ID |
| Per-call or configurable URL | Prefer **per-call** callback URLs (we use this today); fixed account URL acceptable if correlation ID is reliable |
| Reliability | Document retries and expected HTTP 2xx acknowledgment |

### 5. Call control after gather

| Requirement | Detail |
|-------------|--------|
| Closing prompt | Ability to play/say a short closing message after gather |
| Hangup | Explicit end of call |

### 6. Webhook security

| Requirement | Detail |
|-------------|--------|
| Authenticity | Verifiable callbacks (HMAC signature, shared secret, mTLS, or documented IP allowlist) |
| Validation guide | Written steps for verifying webhook authenticity |

### 7. Bangladesh telephony fit

| Requirement | Detail |
|-------------|--------|
| Local numbers / CLI | Document how BD destination numbers and outbound CLI work |
| Number format | Supported formats (E.164 and/or local) |
| Compliance | Confirmation that **security-awareness / simulated training calls** are an allowed use case under ICC / BTRC rules |
| DND / quiet hours | Any mandatory do-not-call or time-window constraints |

### 8. Integration enablement

| Requirement | Detail |
|-------------|--------|
| Sandbox / UAT | Test account, test numbers, non-production credentials |
| API documentation | Create-call, IVR/play/gather, webhook schemas, error codes |
| Sample payloads | At least: create-call request/response; status webhook; DTMF gather webhook |

---

## Should-have (strongly preferred, not MVP blockers)

| Capability | Why |
|------------|-----|
| Speech gather / ASR with transcript on webhook | Matches Twilio speech path (`SpeechResult`); Bangla + English preferred |
| Combined DTMF + speech in one gather step | Matches our `BOTH` interaction mode |
| Signature header on every webhook (HMAC) | Cleaner than IP allowlist alone |
| Call recording + delivery URL | Optional analytics/audit |
| Concurrent dial limits documented | Campaign blast planning |
| OpenAPI / Postman collection | Faster integration |

---

## Nice-to-have

- Inline TTS (Say) if Play URL fails  
- Status events: `initiated`, `answered` granularity beyond minimum  
- Webhook replay / debug console  
- SLA / support channel for production incidents  

---

## Explicitly out of scope for this ask

- SMS / WhatsApp  
- Inbound customer-support IVR product only (without outbound API)  
- Replacing our TTS / voice-clone pipeline (we keep generating audio)  

---

## Reference flow we need to support

```
1. ASAT API → ICC Talk: Create outbound call (To, From, correlationId, instruction/callback URLs)
2. ICC Talk → callee: Dial
3. ICC Talk → ASAT: Status webhooks (ringing → answered → …)
4. On answer: Play our HTTPS audio
5. Gather DTMF (and ideally speech) → ICC Talk → ASAT webhook with digits/transcript
6. Closing message → Hangup
7. Final status webhook (completed / no-answer / failed) + duration
```

---

## Artifacts to receive from ICC Talk

1. Outbound call API spec  
2. Play / gather / hangup control model (URL-driven instructions **or** action API)  
3. Webhook event list + JSON/XML sample payloads  
4. Webhook authentication guide  
5. Sandbox credentials + test numbers  
6. Media format matrix  
7. BD CLI / compliance note for training simulations  
8. Rate limits and pricing unit (per minute / per connect) — commercial, but needed for planning  

---

## Acceptance criteria (integration-ready)

ICC Talk is **integration-ready for ASAT vishing** when we can, in sandbox:

1. Place an outbound call with a correlation ID  
2. Play our MP3/WAV from HTTPS after answer  
3. Capture DTMF and receive it on our webhook  
4. Receive terminal status (`completed` / `no-answer` / `failed`) with duration when applicable  
5. Verify webhook authenticity using their documented method  

Speech gather can follow in a second phase if DTMF MVP works.

---

**Document owner:** ASAT Backend — Phishing / Vishing  
**Related current provider:** Twilio (`Calls.create` + TwiML Gather/Play + status callbacks)
