# SMS short links — nginx updates

Smishing SMS uses `https://{tracking-domain}/{10-char-code}`.  
That must reach phishing `GET /t/s/{shortCode}`, which **302**s to `/t/phish/{trackingId}`.

Two nginx hosts are involved. Do **not** copy blocks between them.

```text
Phone → https://online-banking.tech/01k7Qm2Nxp
      → Landing nginx  (01→dev / 10→staging / 11→prod proxy)
      → ASAT nginx     (/gateway/phishing → gateway → phishing)
      → 302 → .../t/phish/{trackingId}
```

**Important:** In nginx, `$1` in `rewrite` comes from the **rewrite** regex, not the `location` capture. Always capture the code again in the rewrite pattern.

---

## Multi-env: encode environment in the short code

Landing nginx (`15.204.246.8`) routes **all three** envs by path prefix on `/dev|staging|gateway/phishing/`.  
A bare path cannot tell which MongoDB owns the code, so the **first 2 characters of the short code** encode the environment:

| Env | Prefix | Example URL | Landing rewrite target |
|-----|--------|-------------|------------------------|
| Development | `01` | `https://{domain}/01k7Qm2Nxp` | `/dev/gateway/phishing/t/s/{fullCode}` |
| Staging | `10` | `https://{domain}/10k7Qm2Nxp` | `/staging/gateway/phishing/t/s/{fullCode}` |
| Production | `11` | `https://{domain}/11k7Qm2Nxp` | `/gateway/phishing/t/s/{fullCode}` |

- Total public code length: **10** (2-digit prefix + 8-char random suffix).
- Persist and resolve the **full 10-character** `shortCode` (do not strip the prefix).
- Backend: `shortener.env-prefix` is `01` / `10` / `11` per Spring profile.

Do **not** cascade “try all envs on 404” (cross-env collision risk).

---

## 1. Landing page nginx (`15.204.246.8`)

**Required.** Put **all three** blocks in the same `listen 443 ssl` server (above `location /`).

### Development (`01…`)

```nginx
location ~ "^/(01[A-Za-z0-9]{8})$" {
    rewrite "^/(01[A-Za-z0-9]{8})$" /dev/gateway/phishing/t/s/$1 last;
}
```

### Staging (`10…`)

```nginx
location ~ "^/(10[A-Za-z0-9]{8})$" {
    rewrite "^/(10[A-Za-z0-9]{8})$" /staging/gateway/phishing/t/s/$1 last;
}
```

### Production (`11…`)

```nginx
location ~ "^/(11[A-Za-z0-9]{8})$" {
    rewrite "^/(11[A-Za-z0-9]{8})$" /gateway/phishing/t/s/$1 last;
}
```

Reload:

```bash
docker compose exec nginx nginx -t && docker compose exec nginx nginx -s reload
```

Full reference: [landing-server-nginx.conf](landing-server-nginx.conf).

---

## 2. ASAT platform nginx

**Optional hardening** if a 10-char short code or `/dev|staging/.../t/s/{code}` URI hits the platform host directly.  
On each platform host, `/gateway/` has **no** env path prefix.

### Dev (`dev.aspireelearning.com`)

```nginx
location ~ "^/((?:01|10|11)[A-Za-z0-9]{8})$" {
    rewrite "^/((?:01|10|11)[A-Za-z0-9]{8})$" /gateway/phishing/t/s/$1 last;
}

location ~ "^/dev/gateway/phishing/t/s/((?:01|10|11)[A-Za-z0-9]{8})$" {
    rewrite "^/dev/gateway/phishing/t/s/((?:01|10|11)[A-Za-z0-9]{8})$" /gateway/phishing/t/s/$1 last;
}
```

### Staging (`staging.aspireelearning.com`)

```nginx
location ~ "^/((?:01|10|11)[A-Za-z0-9]{8})$" {
    rewrite "^/((?:01|10|11)[A-Za-z0-9]{8})$" /gateway/phishing/t/s/$1 last;
}

location ~ "^/staging/gateway/phishing/t/s/((?:01|10|11)[A-Za-z0-9]{8})$" {
    rewrite "^/staging/gateway/phishing/t/s/((?:01|10|11)[A-Za-z0-9]{8})$" /gateway/phishing/t/s/$1 last;
}
```

### Production (`portal.securityawarenesstraining.ai`)

```nginx
location ~ "^/((?:01|10|11)[A-Za-z0-9]{8})$" {
    rewrite "^/((?:01|10|11)[A-Za-z0-9]{8})$" /gateway/phishing/t/s/$1 last;
}
```

Existing `location /gateway/` proxies to `asat-gateway-service`.

Reload:

```bash
docker compose exec nginx nginx -t && docker compose exec nginx nginx -s reload
```

Repo example (dev): [nginx.conf](../nginx.conf).

---

## What not to do

| Mistake | Result |
|---------|--------|
| One bare 8-char rewrite for all envs | Cannot route three envs; legacy codes only |
| Cascade try-all-envs on 404 | Possible **cross-env code collision** → wrong landing |
| `rewrite ^ /.../t/s/$1 last;` (no capture on rewrite) | Empty code → gateway JSON 404 Length 0 |
| Strip `01`/`10`/`11` before phishing lookup | Mongo miss (full code is stored) |
| Strip `/dev` or `/staging` on the **landing** server after rewriting | Path no longer matches the env proxy location → 404 |

---

## Quick verify

```bash
# Dev code
curl -skI https://online-banking.tech/01{eight} | grep -iE '^(HTTP|Location|Content-Type|Content-Length)'
curl -skI https://online-banking.tech/dev/gateway/phishing/t/s/01{eight} | grep -iE '^(HTTP|Location|Content-Type|Content-Length)'

# Staging / prod
curl -skI https://online-banking.tech/10{eight} | grep -iE '^(HTTP|Location|Content-Type|Content-Length)'
curl -skI https://online-banking.tech/11{eight} | grep -iE '^(HTTP|Location|Content-Type|Content-Length)'

# Unknown → 404, Content-Length 49 (phishing HTML), not JSON Length 0
curl -skI https://online-banking.tech/01XXXXXXXX | grep -iE '^(HTTP|Content-Type|Content-Length)'
```
