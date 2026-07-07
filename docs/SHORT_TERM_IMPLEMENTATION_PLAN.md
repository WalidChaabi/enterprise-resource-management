# ERM — Short-Term Implementation Plan (0–3 months)

**Source:** [PRODUCT_AUDIT_INTERNATIONAL_SAAS.md](./PRODUCT_AUDIT_INTERNATIONAL_SAAS.md) § Short term  
**Goal:** Product feels **finished** for early paying customers in one geography  
**Duration:** ~6–8 weeks (1 developer) / ~4–5 weeks (2 developers)

**QA before release:** [SHORT_TERM_QA_CHECKLIST.md](./SHORT_TERM_QA_CHECKLIST.md) · **Pilot gate:** [PILOT_QA_CHECKLIST.md](./PILOT_QA_CHECKLIST.md)  
**Push / PR split:** [SHORT_TERM_PUSH_GUIDE.md](./SHORT_TERM_PUSH_GUIDE.md)

---

## Overview

| Phase | Focus | Duration | Priority | Status |
|-------|--------|----------|----------|--------|
| ST-A | Green CI + security tests | 3–5 days | P0 | ✅ Backend green · CI verify pending |
| ST-B | Settings API alignment | 3–4 days | P0 | ✅ Code done · QA pending |
| ST-C | Unify data tables | 5–7 days | P0 | ✅ All five lists |
| ST-D | Notification pipeline | 4–5 days | P0 | ✅ Done |
| ST-E | Reporting UI | 5–7 days | P0 | ✅ Done |
| ST-F | i18n + a11y baseline | 5–7 days | P0/P1 | ✅ Baseline done |
| ST-G | Production demo creds guard | 1–2 days | P0 | ✅ Done |
| ST-H | P1 completeness | 7–10 days | P1 | ✅ Done |

**Code complete:** yes (uncommitted). **Release ready:** after QA checklist + frontend build in CI.

---

## Phase ST-A — Engineering trust

**Goal:** CI fails only on real regressions.

| ID | Task | Status |
|----|------|--------|
| ST-A.1 | Fix integration tests | ✅ |
| ST-A.2 | SecurityTestHelper for JWT/roles | Optional |
| ST-A.3 | `./mvnw test` in CI | ⏳ Verify on push |
| ST-A.4 | Frontend `npm test` | ⏳ Pending |
| ST-A.5 | ESLint in CI | ⏳ Pending |

---

## Phase ST-B — Settings API alignment

**Goal:** Settings screens load and save without field mismatches.

| ID | Task | Status |
|----|------|--------|
| ST-B.1 | Align notification prefs (`emailEnabled`, `inAppEnabled`) | ✅ |
| ST-B.2 | Align billing (`planName`, `amount`, `currency`) | ✅ |
| ST-B.3 | i18n for notification settings | ✅ |
| ST-B.4 | Manual QA all `/settings/*` routes | ⏳ [Checklist](./SHORT_TERM_QA_CHECKLIST.md#st-b--settings-settings) |

---

## Phase ST-C — Unify data tables

| Screen | Status |
|--------|--------|
| Employees | ✅ data-table |
| Departments | ✅ data-table |
| Projects | ✅ data-table |
| Leave requests | ✅ data-table + row actions |
| Time entries | ✅ data-table + row actions |

Shared primitives: `shared/components/data-table/*`, per-entity `*-data-table` + `*-row-actions`.

---

## Phase ST-D — Notification pipeline

| Producer | Event |
|----------|--------|
| `LeaveRequestService` | Approve / reject |
| `TimeEntryService` | Approve / reject |
| `TaskService` | Task assign |

`NotificationService.createInAppNotification(user, title, description)` — ✅ wired.

---

## Phase ST-E — Reporting UI

- `report.model.ts` + extended `reporting.service.ts`
- Dashboard tabs: **Overview / Time / Leave**
- Date filters and data tables on reporting dashboard

---

## Phase ST-F — i18n & a11y

- EN/FR: settings, reporting, auth flows, nav user menu, tables
- Skip-to-content link (`main-layout`)
- Dialog focus trap (`dialog.component.ts`)

---

## Phase ST-G — Production security hygiene

| ID | Task | Status |
|----|------|--------|
| ST-G.1 | `environment.showDemoAccess` flag | ✅ |
| ST-G.2 | Hide demo UI in production build | ✅ |

---

## Phase ST-H — P1 completeness

| Feature | Status |
|---------|--------|
| Password reset (V15 migration, API, forgot/reset pages) | ✅ |
| Project archive (admin row action + confirm) | ✅ |
| Leave balance (leave list cards + reporting tab) | ✅ |

---

## Execution order (completed)

1. ~~ST-A + ST-B + ST-G~~ — Sprint 1
2. ~~ST-C~~ — all data tables
3. ~~ST-D~~ — notification producers
4. ~~ST-E + ST-F~~ — reporting + i18n/a11y
5. ~~ST-H~~ — password reset, archive, leave balance

---

## Out of scope (moved to medium term)

See [MEDIUM_TERM_IMPLEMENTATION_PLAN.md](./MEDIUM_TERM_IMPLEMENTATION_PLAN.md): Stripe billing, self-serve signup, SSO/MFA, GDPR, capacity/utilization dashboard, webhooks, and related items.

---

## Related docs

- [MEDIUM_TERM_IMPLEMENTATION_PLAN.md](./MEDIUM_TERM_IMPLEMENTATION_PLAN.md) — commercial SaaS & differentiation (MT-A–MT-L)

- [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) — technical/engineering phases
- [PRODUCT_AUDIT_INTERNATIONAL_SAAS.md](./PRODUCT_AUDIT_INTERNATIONAL_SAAS.md) — product audit
- [SHORT_TERM_QA_CHECKLIST.md](./SHORT_TERM_QA_CHECKLIST.md) — pre-release QA
- [SHORT_TERM_PUSH_GUIDE.md](./SHORT_TERM_PUSH_GUIDE.md) — commit/PR split
