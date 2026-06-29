# Short-Term Work — Push & PR Guide

Split the current working tree into **three reviewable PRs**. Commit in this order so each PR builds on the previous.

---

## PR 1 — UI shell & design system

**Branch suggestion:** `feat/ui-shell-shadcn`

**Scope:** Layout, sidebar, shared UI primitives, Tailwind tokens, Lucide icons. No product-feature logic beyond layout wiring.

### Files (indicative)

```
frontend/components.json
frontend/package.json, package-lock.json
frontend/tailwind.config.js
frontend/src/index.html
frontend/src/styles.scss
frontend/src/app/app.config.ts
frontend/src/app/shared/components/ui/**
frontend/src/app/shared/icons/**
frontend/src/app/shared/utils/floating-panel.*
frontend/src/app/layout/**
frontend/src/app/shared/components/breadcrumb/**
frontend/src/app/shared/components/help-menu/**
frontend/src/app/shared/components/keyboard-shortcuts/**
frontend/src/app/shared/components/notification-menu/**
frontend/src/app/shared/components/org-switcher/**
frontend/src/app/shared/components/quick-action-menu/**
frontend/src/app/shared/components/user-menu/**
frontend/src/app/shared/components/role-picker/**
(Delete) frontend/src/app/shared/components/profile-menu/**
```

### Commit message

```
feat(frontend): adopt shadcn sidebar shell and shared UI primitives

Introduce sidebar provider/panel, design tokens, Lucide icons, and reusable
dropdown/table/dialog components. Consolidate account access into UserMenu
(sidebar footer on desktop, toolbar on mobile) and remove duplicate header
profile menu.
```

### PR title

`feat(frontend): shadcn sidebar shell and design system`

### PR body

```markdown
## Summary
- Migrate main layout to shadcn-style sidebar (collapsible, mobile drawer)
- Add shared UI kit: table, dropdown, popover, date-picker, dialog focus trap
- Unify account menu: sidebar footer (desktop) + compact header trigger (mobile)
- Premium dark-theme tokens and Lucide icon registry

## Test plan
- [ ] Sidebar collapse/expand and mobile drawer
- [ ] User menu: all items visible; desktop footer / mobile header split
- [ ] Dialog focus trap and skip link
- [ ] `npm run build` passes
```

---

## PR 2 — Short-term product features

**Branch suggestion:** `feat/short-term-audit-sprint`

**Base:** PR 1 merged (or same branch stacked)

**Scope:** All ST-A–ST-H product/backend work.

### Files (indicative)

```
backend/** (auth reset, notifications, tests, V15 migration)
frontend/src/app/features/** (tables, reporting, auth, settings, lists)
frontend/src/app/core/**
frontend/src/app/app.routes.ts
frontend/src/environments/**
frontend/src/assets/i18n/**
docs/SHORT_TERM_IMPLEMENTATION_PLAN.md
docs/SHORT_TERM_QA_CHECKLIST.md
```

### Commit message

```
feat: complete short-term product audit sprint (ST-A–ST-H)

Align settings with API, unify list tables, wire in-app notifications,
extend reporting UI, add password reset and project archive, hide demo
login in production, and baseline i18n/a11y improvements.
```

### PR title

`feat: short-term international SaaS audit (settings, tables, reporting, auth)`

### PR body

```markdown
## Summary
- **ST-B:** Notification + billing settings aligned with backend DTOs
- **ST-C:** TanStack data tables for employees, departments, projects, leave, time
- **ST-D:** In-app notifications on leave/time/task events
- **ST-E:** Reporting dashboard tabs (overview, time, leave)
- **ST-F:** EN/FR strings, skip link, dialog focus trap
- **ST-G:** `showDemoAccess` false in production
- **ST-H:** Password reset flow, project archive, leave balance cards

## Test plan
- [ ] `./mvnw test`
- [ ] `npm run build`
- [ ] Complete [SHORT_TERM_QA_CHECKLIST.md](./SHORT_TERM_QA_CHECKLIST.md)
```

---

## PR 3 — Product audit doc (optional)

**Branch suggestion:** `docs/product-audit`

```
docs/PRODUCT_AUDIT_INTERNATIONAL_SAAS.md
```

### Commit message

```
docs: add international SaaS product audit and short-term plan
```

---

## Quick split commands (manual)

Git does not auto-split. Use interactive staging per PR:

```powershell
# After PR1 branch created
git add <pr1-files>
git commit -m "$(cat <<'EOF'
feat(frontend): adopt shadcn sidebar shell and shared UI primitives
EOF
)"

# PR2: stage remaining feature files
git add <pr2-files>
git commit -m "$(cat <<'EOF'
feat: complete short-term product audit sprint (ST-A–ST-H)
EOF
)"
```

Or use a single PR if review bandwidth is limited — note the larger diff in the PR description.

---

## Pre-push verification

```powershell
cd backend; ./mvnw test
cd ../frontend; npm ci; npm run build
```

Walk through [SHORT_TERM_QA_CHECKLIST.md](./SHORT_TERM_QA_CHECKLIST.md).

---

## Still open after merge

| Item | Phase |
|------|-------|
| Frontend unit tests in CI | ST-A.4 |
| ESLint in CI | ST-A.5 |
| Manual settings QA sign-off | ST-B.4 |
| Medium-term audit items (Stripe, SSO, capacity dashboard) | See product audit |
