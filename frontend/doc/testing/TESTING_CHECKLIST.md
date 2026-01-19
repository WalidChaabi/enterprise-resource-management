# Testing Checklist

This document outlines what has been implemented and what to test.

## ✅ Completed Features

### 1. Department Management
**Location:** `/departments`

**What to test:**
- [ ] Navigate to Departments page
- [ ] View list of departments (if any exist)
- [ ] Click on a department to view details
- [ ] Check department information display (name, description, manager, employee count)
- [ ] Verify empty state message when no departments exist

**Files:**
- `src/app/core/models/department.model.ts`
- `src/app/core/services/department.service.ts`
- `src/app/features/departments/department-list/`
- `src/app/features/departments/department-detail/`

---

### 2. Task Management
**Location:** `/projects/:id` (Tasks section at bottom)

**What to test:**
- [ ] Navigate to a project detail page
- [ ] Scroll down to see Tasks section
- [ ] View list of tasks for the project
- [ ] Click on a task to view task details
- [ ] Check task information (name, status, priority, assignee, due date)
- [ ] Verify task status badges display correctly
- [ ] Verify priority badges display correctly
- [ ] Check empty state when no tasks exist

**Files:**
- `src/app/core/models/task.model.ts`
- `src/app/core/services/task.service.ts`
- `src/app/features/projects/task-list/`
- `src/app/features/projects/task-detail/`

---

### 3. Time Entry Management
**Location:** `/time-entries`

**What to test:**
- [ ] Navigate to Time Entries page
- [ ] View list of time entries
- [ ] Check time entry information (date, project, hours, status)
- [ ] Verify status badges display correctly
- [ ] Check empty state when no time entries exist
- [ ] Test pagination if multiple pages exist

**Files:**
- `src/app/core/models/time-entry.model.ts`
- `src/app/core/services/time-entry.service.ts`
- `src/app/features/time-entries/time-entry-list/`

---

### 4. Leave Request Management
**Location:** `/leave-requests`

**What to test:**
- [ ] Navigate to Leave Requests page
- [ ] View list of leave requests
- [ ] Check leave request information (employee, type, dates, days, status)
- [ ] Verify status badges display correctly
- [ ] Check empty state when no leave requests exist
- [ ] Test pagination if multiple pages exist

**Files:**
- `src/app/core/models/leave-request.model.ts`
- `src/app/core/services/leave-request.service.ts`
- `src/app/features/leave-requests/leave-request-list/`

---

### 5. Search & Export
**Location:** `/search` (also accessible via search icon in header)

**What to test:**
- [ ] Click search icon in header or navigate to `/search`
- [ ] Enter a search query (minimum 2 characters)
- [ ] Click "Search" button
- [ ] View search results
- [ ] Test entity type filters (Employee, Project, Task, etc.)
- [ ] Click on a search result to navigate to detail page
- [ ] Test CSV export button
- [ ] Test PDF export button
- [ ] Verify empty state when no results found
- [ ] Test pagination if multiple pages of results

**Files:**
- `src/app/core/models/search.model.ts`
- `src/app/core/services/search.service.ts`
- `src/app/features/search/search-bar/`
- `src/app/features/search/search-results/`
- `src/app/features/search/search-page/`

---

### 6. User Role Assignment
**Location:** `/employees/:id` (Roles section)

**What to test:**
- [ ] Navigate to an employee detail page
- [ ] Scroll down to "Roles" card section
- [ ] View currently assigned roles (displayed as badges)
- [ ] Select a role from the dropdown
- [ ] Click "Assign" button to assign a role
- [ ] Verify role appears in assigned roles list
- [ ] Click X button on a role badge to remove it
- [ ] Verify role is removed from the list
- [ ] Check that only unassigned roles appear in dropdown
- [ ] Verify "All roles assigned" message when applicable

**Files:**
- `src/app/core/models/user-role.model.ts`
- `src/app/core/services/user-role.service.ts`
- `src/app/shared/components/role-picker/`
- `src/app/features/employees/employee-detail/` (updated)

---

## 🔍 General Testing

### Navigation
- [ ] All routes are accessible
- [ ] Navigation links work correctly
- [ ] Back button works
- [ ] Route guards are functioning (if applicable)

### UI/UX
- [ ] Dark mode toggle works
- [ ] Language switcher works (EN/FR)
- [ ] All translations are displayed correctly
- [ ] Loading states display correctly
- [ ] Error messages display correctly
- [ ] Empty states display correctly
- [ ] Responsive design works on different screen sizes

### Error Handling
- [ ] Network errors are handled gracefully
- [ ] 401 errors redirect to login
- [ ] 404 errors show appropriate messages
- [ ] Form validation errors display correctly

### API Integration
- [ ] All API calls use correct endpoints
- [ ] Authentication tokens are included in requests
- [ ] Organization ID is correctly retrieved and used
- [ ] Error responses are parsed and displayed

---

## 🐛 Known Issues / Limitations

1. **Forms Not Implemented**: Create/Edit forms for most entities are not yet implemented. Only list and detail views are available.

2. **Approval Workflows**: Time entry and leave request approval workflows are not yet implemented in the UI (services support them, but no UI components).

3. **Pagination**: Some list components may not have full pagination implemented yet.

4. **Search Filters**: Advanced search filters beyond entity type are not yet implemented.

---

## 📝 Testing Notes

- Make sure the backend is running and accessible
- Ensure you're logged in with appropriate credentials
- Check browser console for any errors
- Test with different user roles if possible
- Verify API responses match expected data structures

---

## 🚀 Next Steps After Testing

If everything works correctly, consider:
1. Adding create/edit forms for entities
2. Implementing approval workflows
3. Adding more advanced search filters
4. Implementing Role Management (admin feature)
5. Adding data visualization/charts
6. Performance optimization

---

**Last Updated:** 2024-01-14
