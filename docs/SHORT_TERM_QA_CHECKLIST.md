# Short-Term Release — Manual QA Checklist

Use before merging the short-term work. Check each item on **desktop (≥ md)** and spot-check on **mobile**.

**Test users:** `admin@demo.com` / `password` (dev only)

---

## ST-A — Engineering

- [ ] `cd backend && ./mvnw test` — all tests pass
- [ ] `cd frontend && npm ci && npm run build` — production build succeeds
- [ ] CI pipeline runs backend tests on PR (verify after push)

---

## ST-B — Settings (`/settings/*`)

- [ ] **Profile** — load, edit name, save, values persist after refresh
- [ ] **Organization** — load and save without console/network errors
- [ ] **Roles & permissions** — list loads; role changes save if applicable
- [ ] **Billing** — plan name, amount, currency display correctly (not `undefined`)
- [ ] **Notifications** — email / in-app toggles save and reload correctly
- [ ] Switch **EN ↔ FR** on settings pages — labels update

---

## ST-C — Data tables

For each list: **Employees, Departments, Projects, Leave requests, Time entries**

- [ ] Table loads without errors
- [ ] Column sort works
- [ ] Pagination works (if dataset large enough)
- [ ] Row actions open / navigate correctly
- [ ] Empty state renders when no data

**Leave requests**

- [ ] Approve / reject from row actions (manager role)
- [ ] Reject dialog requires reason where applicable

**Time entries**

- [ ] Approve / reject from row actions

**Projects**

- [ ] Archive action visible for `ORG_ADMIN`
- [ ] Archive confirm dialog → project removed or marked archived

---

## ST-D — Notifications

- [ ] Approve a **leave request** → notification appears in header inbox
- [ ] Reject a **leave request** → employee gets notification
- [ ] Approve / reject **time entry** → notification created
- [ ] Assign **task** → assignee gets notification
- [ ] Mark all read clears unread state

---

## ST-E — Reporting (`/reporting`)

- [ ] **Overview** tab loads dashboard metrics
- [ ] **Time** tab — date filter changes data
- [ ] **Leave** tab — balances and upcoming leave tables load
- [ ] No broken API calls in network tab
- [ ] EN/FR labels on reporting UI

---

## ST-F — i18n & a11y

- [ ] Tab to **skip link** → focus moves to main content
- [ ] Open a **dialog** → Tab cycles inside; Escape closes
- [ ] Language switcher (EN/FR) on key flows: login, dashboard, settings, reporting

---

## ST-G — Production demo guard

- [ ] Dev build (`environment.ts`) — demo access block visible on login
- [ ] Prod build (`ng build --configuration=production`) — demo UI **hidden**
- [ ] `showDemoAccess: false` in `environment.prod.ts`

---

## ST-H — P1 completeness

### Password reset

- [ ] Login → **Forgot password** link works
- [ ] Submit email on `/forgot-password` — success message (no email enumeration)
- [ ] Dev: copy token from **backend logs**
- [ ] `/reset-password?token=…` — set new password, success
- [ ] Login with new password works
- [ ] Invalid / expired token shows error

### Leave balance

- [ ] Leave list shows balance cards (when API returns data)
- [ ] Reporting leave tab shows same balance data

### Project archive

- [ ] Admin can archive from project list row menu
- [ ] Non-admin does not see archive (or gets 403)

---

## Layout (pre-push polish, not audit phase)

- [ ] **Desktop** — account menu only in **sidebar footer** (not header)
- [ ] **Mobile** — account avatar in **header**; sidebar footer user block hidden
- [ ] Sidebar footer menu: Account, Billing, Notifications, Logout all visible
- [ ] Sidebar footer pinned to bottom of sidebar

---

## Sign-off

| Role | Name | Date | Pass? |
|------|------|------|-------|
| Dev | | | |
| QA / Product | | | |
