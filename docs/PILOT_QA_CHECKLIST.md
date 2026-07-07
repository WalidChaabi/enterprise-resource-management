# Pilot Release — QA Checklist (Option A)

**Goal:** Ship a **pilot-ready baseline** — short-term + medium-term features validated on staging before first paying pilots.

**Stack under test:** `http://localhost:4200` (Docker) · Backend `http://localhost:8080`

**Test users (dev/demo only):**

| Role | Email | Password |
|------|-------|----------|
| Org admin | `admin@demo.com` | `password123` |
| Employee | `employee@demo.com` | `password123` |

Also see [SHORT_TERM_QA_CHECKLIST.md](./SHORT_TERM_QA_CHECKLIST.md) for detailed ST items.

---

## Automated gate (run before manual QA)

| Check | Command | Pass? |
|-------|---------|-------|
| Backend unit + integration tests | `cd backend && ./mvnw test` | ✅ 84 tests, 0 failures (2026-07-02) |
| Frontend production build | `cd frontend && npm ci && npm run build -- --configuration=production` | ✅ Docker `docker compose build front` |
| E2E smoke (login + auth guard) | `cd e2e && npm ci && npx playwright install chromium && npm test` | ✅ 2 passed (2026-07-02) |
| Docker stack healthy | `cd docker && docker compose ps` — all `healthy` | ✅ erm-db, erm-backend, erm-front |

**Last automated run:** 2026-07-02

---

## Part 1 — Short-term (ST-A through ST-H)

Use [SHORT_TERM_QA_CHECKLIST.md](./SHORT_TERM_QA_CHECKLIST.md). Summary sign-off:

| Phase | Area | Pass? | Notes |
|-------|------|-------|-------|
| ST-A | CI / tests / build | ☐ | |
| ST-B | Settings (`/settings/*`) | ☐ | |
| ST-C | Data tables (5 lists) | ☐ | |
| ST-D | Notifications inbox | ☐ | |
| ST-E | Reporting (`/reports`) | ☐ | |
| ST-F | i18n & a11y | ☐ | |
| ST-G | Prod demo guard (`showDemoAccess: false`) | ☐ | |
| ST-H | Password reset, leave balance, archive | ☐ | |
| — | Layout (sidebar / mobile header) | ☐ | |

---

## Part 2 — Medium-term pilot flows

### MT-A — Signup & org provisioning

- [ ] `/signup` wizard completes (account → org → terms)
- [ ] New org lands on dashboard with session
- [ ] Duplicate email returns clear error (409)
- [ ] `erm.signup.enabled=false` blocks signup (if tested)

### MT-B — Billing (Stripe optional on staging)

- [ ] `/settings/billing` shows plan name, amount, currency (no `undefined`)
- [ ] Seat count displayed matches active employees
- [ ] **Seat limit:** create employees until limit → `SEAT_LIMIT_REACHED` with actionable message
- [ ] “Manage subscription” opens Stripe portal when `STRIPE_*` configured
- [ ] Webhook idempotency (Stripe CLI) — optional for pilot

### MT-C — Entitlements

- [ ] Trial org: advanced reports return upgrade prompt / `UPGRADE_REQUIRED`
- [ ] Pro org: reports + webhooks accessible
- [ ] Sidebar hides premium nav when not entitled

### MT-D — SSO & MFA

- [ ] Password login still works (`admin@demo.com`)
- [ ] MFA enroll + verify flow (security settings)
- [ ] Org “Require MFA” blocks login until verified
- [ ] SSO buttons on login (when `OAUTH2_ENABLED=true`) — Azure/Google smoke

### MT-E — Audit log

- [ ] `/settings/audit` loads events
- [ ] Create/update employee → event appears within ~5s
- [ ] CSV export downloads with expected columns

### MT-F — GDPR privacy

- [ ] `/settings/privacy` — export my data (ZIP download)
- [ ] Erasure request → admin approve/reject flow
- [ ] Erasure anonymizes PII; audit entry created

### MT-G — Email (when `MAIL_ENABLED=true`)

- [ ] Approve leave → email + in-app notification
- [ ] User with `emailEnabled=false` gets in-app only

### MT-H — Webhooks (Pro entitlement)

- [ ] `/settings/webhooks` — create endpoint, secret shown once
- [ ] Approve leave → signed POST to test URL (`X-ERM-Signature`)
- [ ] Failed delivery appears in log with retry

### MT-I — Capacity wedge

- [ ] `/reports/capacity` — week navigation, utilization chart
- [ ] Leave overlay visible on employee rows
- [ ] Dashboard home shows capacity widgets (PM/admin)
- [ ] `/reports` — Overview, Time, Leave tabs load without freeze

---

## Part 3 — Regression spot-checks

- [ ] Login / logout / session refresh
- [ ] Org switcher (if multi-org user)
- [ ] EN ↔ FR on dashboard, settings, reports
- [ ] Hard refresh on deep routes (`/reports`, `/employees`) — no 404
- [ ] Mobile spot-check: login, dashboard welcome spacing, leave list

---

## Blockers log

| # | Severity | Area | Description | Status |
|---|----------|------|-------------|--------|
| 1 | | | | |

---

## Pilot baseline sign-off

| Milestone | Owner | Date | Done? |
|-----------|-------|------|-------|
| Automated gates green | Dev | | ☐ |
| ST manual QA complete | Dev/QA | | ☐ |
| MT manual QA complete | Dev/QA | | ☐ |
| Blockers resolved or accepted | Product | | ☐ |
| Git tag `pilot-v0.1` (or similar) | Dev | | ☐ |
| Staging deploy documented | DevOps | | ☐ |

**Tag command (after commit):**

```bash
git tag -a pilot-v0.1 -m "Pilot baseline: ST + MT core QA signed off"
git push origin pilot-v0.1
```

---

## Related docs

| Document | Purpose |
|----------|---------|
| [SHORT_TERM_QA_CHECKLIST.md](./SHORT_TERM_QA_CHECKLIST.md) | Detailed ST manual steps |
| [MEDIUM_TERM_IMPLEMENTATION_PLAN.md](./MEDIUM_TERM_IMPLEMENTATION_PLAN.md) | Feature scope & status |
| [SHORT_TERM_PUSH_GUIDE.md](./SHORT_TERM_PUSH_GUIDE.md) | PR split guidance |
