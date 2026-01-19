# 🔐 Roles & Permissions - Complete Reference

## 📋 Overview

The system uses **Role-Based Access Control (RBAC)** with 5 main roles. Each role has specific permissions and can perform certain actions within the organization.

---

## 👥 Roles Defined

1. **ORG_ADMIN** - Organization Administrator
2. **HR_ADMIN** - Human Resources Administrator  
3. **PROJECT_MANAGER** - Project Manager
4. **DEPARTMENT_MANAGER** - Department Manager
5. **EMPLOYEE** - Standard Employee

---

## 🎯 Role Details & Permissions

### 1. ORG_ADMIN (Organization Administrator)

**Description:** Full administrative access to the entire organization.

**Permissions:**
- ✅ **Full access** to all modules
- ✅ **Create/Update/Delete** any resource
- ✅ **Manage roles** and role assignments
- ✅ **Hard delete** projects
- ✅ **Archive** projects
- ✅ **Approve/Reject** time entries and leave requests
- ✅ **View all** employees, projects, departments, reports

**Actions:**
- **Employees:** Create, Read, Update, Delete (all employees)
- **Projects:** Create, Read, Update, Archive, Hard Delete, Add/Remove Members
- **Departments:** Create, Read, Update, Delete
- **Roles:** Create, Read, Update, Delete
- **User Roles:** Assign, Remove, List
- **Tasks:** Create, Read, Update, Delete
- **Time Entries:** Create, Read, Update, Delete, Submit, Approve, Reject
- **Leave Requests:** Create, Read, Update, Delete, Approve, Reject
- **Reports:** View all reports and dashboard
- **Search:** Full search access
- **Export:** CSV/PDF export

**Restrictions:**
- ❌ None (full access)

---

### 2. HR_ADMIN (Human Resources Administrator)

**Description:** Manages HR-related resources: employees, departments, roles, leave requests.

**Permissions:**
- ✅ **Manage employees** (create, update, deactivate)
- ✅ **Manage departments** (create, update, deactivate)
- ✅ **Manage roles** (view only, cannot create/update/delete)
- ✅ **Assign roles** to employees
- ✅ **Approve/Reject** leave requests
- ✅ **Approve/Reject** time entries
- ✅ **View reports** and analytics
- ✅ **View all** employees and departments

**Actions:**
- **Employees:** Create, Read, Update, Delete (all employees)
- **Departments:** Create, Read, Update, Delete
- **Roles:** Read only (list, get by ID)
- **User Roles:** Assign, Remove, List
- **Time Entries:** Create, Read, Update, Delete, Submit, Approve, Reject
- **Leave Requests:** Create, Read, Update, Delete, Approve, Reject
- **Reports:** View all reports and dashboard
- **Search:** Full search access
- **Export:** CSV/PDF export

**Restrictions:**
- ❌ Cannot create/update/delete roles (ORG_ADMIN only)
- ❌ Cannot manage projects (PROJECT_MANAGER only)
- ❌ Cannot hard delete projects (ORG_ADMIN only)

---

### 3. PROJECT_MANAGER

**Description:** Manages projects, tasks, time tracking, and project resources.

**Permissions:**
- ✅ **Manage projects** (create, update, add/remove members)
- ✅ **Manage tasks** (create, read, update, delete)
- ✅ **Approve/Reject** time entries
- ✅ **View reports** and analytics
- ✅ **View projects** and project members
- ✅ **Search** projects, tasks, time entries

**Actions:**
- **Projects:** Create, Read, Update, Add/Remove Members
- **Tasks:** Create, Read, Update, Delete
- **Time Entries:** Create, Read, Update, Delete, Submit, Approve, Reject
- **Reports:** View all reports and dashboard
- **Search:** Full search access
- **Export:** CSV/PDF export

**Restrictions:**
- ❌ Cannot archive/delete projects (ORG_ADMIN only)
- ❌ Cannot manage employees (HR_ADMIN only)
- ❌ Cannot manage departments (HR_ADMIN only)
- ❌ Cannot approve/reject leave requests (HR_ADMIN, DEPARTMENT_MANAGER only)
- ❌ Cannot manage roles (ORG_ADMIN only)

---

### 4. DEPARTMENT_MANAGER

**Description:** Manages employees in their own department and approves leave requests.

**Permissions:**
- ✅ **View employees** in their department only
- ✅ **Update employees** in their department (limited fields)
- ✅ **View departments** (their own department)
- ✅ **View roles** (read only)
- ✅ **View user roles** (list roles assigned to employees)
- ✅ **Approve/Reject** leave requests
- ✅ **View reports** (limited to their department)

**Actions:**
- **Employees:** Read, Update (department employees only, limited fields)
- **Departments:** Read (their own department)
- **Roles:** Read only (list, get by ID)
- **User Roles:** List only (cannot assign/remove)
- **Leave Requests:** Create, Read, Update, Delete, Approve, Reject
- **Reports:** View reports (department-scoped)
- **Search:** Full search access
- **Export:** CSV/PDF export

**Restrictions:**
- ❌ Cannot create employees (HR_ADMIN, ORG_ADMIN only)
- ❌ Cannot deactivate employees (HR_ADMIN, ORG_ADMIN only)
- ❌ Cannot manage projects (PROJECT_MANAGER only)
- ❌ Cannot manage tasks (PROJECT_MANAGER only)
- ❌ Cannot approve/reject time entries (PROJECT_MANAGER, HR_ADMIN, ORG_ADMIN only)
- ❌ Cannot create/update/delete departments (HR_ADMIN, ORG_ADMIN only)
- ❌ Cannot assign/remove roles (HR_ADMIN, ORG_ADMIN only)

---

### 5. EMPLOYEE

**Description:** Standard employee with basic access to their own data and project tasks.

**Permissions:**
- ✅ **View own** employee profile
- ✅ **Create/Update** own time entries
- ✅ **Submit** time entries for approval
- ✅ **Create/Update** own leave requests
- ✅ **Cancel** own leave requests
- ✅ **View/Update** tasks assigned to them
- ✅ **Search** (limited to accessible resources)

**Actions:**
- **Employees:** Read (own profile only)
- **Tasks:** Create, Read, Update (assigned tasks)
- **Time Entries:** Create, Read, Update, Delete, Submit (own entries only)
- **Leave Requests:** Create, Read, Update, Delete, Cancel (own requests only)
- **Search:** Limited search access
- **Export:** CSV/PDF export (own data)

**Restrictions:**
- ❌ Cannot approve/reject time entries
- ❌ Cannot approve/reject leave requests
- ❌ Cannot delete tasks (PROJECT_MANAGER, ORG_ADMIN only)
- ❌ Cannot manage projects
- ❌ Cannot manage employees
- ❌ Cannot view reports (PROJECT_MANAGER, HR_ADMIN, ORG_ADMIN only)
- ❌ Cannot view other employees' data
- ❌ Cannot view other employees' time entries
- ❌ Cannot view other employees' leave requests

---

## 📊 Permission Matrix

### Employee Management

| Action                         | ORG_ADMIN | HR_ADMIN | PROJECT_MANAGER | DEPARTMENT_MANAGER | EMPLOYEE |
|--------------------------------|-----------|----------|-----------------|--------------------|----------|
| Create Employee                | ✅        | ✅      | ❌              | ❌                | ❌       |
| View All Employees             | ✅        | ✅      | ❌              | ❌                | ❌       |
| View Department Employees      | ✅        | ✅      | ❌              | ✅                | ❌       |
| View Own Profile               | ✅        | ✅      | ✅              | ✅                | ✅       |
| Update All Employees           | ✅        | ✅      | ❌              | ❌                | ❌       |
| Update Department Employees    | ✅        | ✅      | ❌              | ⚠️ Limited        | ❌       |
| Deactivate Employee            | ✅        | ✅      | ❌              | ❌                | ❌       |


### Project Management

| Action                    | ORG_ADMIN | HR_ADMIN | PROJECT_MANAGER | DEPARTMENT_MANAGER | EMPLOYEE |
|---------------------------|-----------|----------|-----------------|--------------------|----------|
| Create Project            | ✅        | ❌      | ✅              | ❌                | ❌       |
| View Projects             | ✅        | ❌      | ✅              | ❌                | ❌       |
| Update Project            | ✅        | ❌      | ✅              | ❌                | ❌       |
| Archive Project           | ✅        | ❌      | ❌              | ❌                | ❌       |
| Hard Delete Project       | ✅        | ❌      | ❌              | ❌                | ❌       |
| Add / Remove Members      | ✅        | ❌      | ✅              | ❌                | ❌       |


### Task Management

| Action                 | ORG_ADMIN | HR_ADMIN | PROJECT_MANAGER | DEPARTMENT_MANAGER | EMPLOYEE     |
|------------------------|-----------|----------|-----------------|--------------------|---------- ---|
| Create Task            | ✅        | ❌      | ✅              | ❌                | ✅          |
| View Tasks             | ✅        | ❌      | ✅              | ❌                | ⚠️ Assigned |
| Update Task            | ✅        | ❌      | ✅              | ❌                | ⚠️ Assigned |
| Delete Task            | ✅        | ❌      | ✅              | ❌                | ❌          |


### Time Entry Management

| Action                   | ORG_ADMIN | HR_ADMIN | PROJECT_MANAGER | DEPARTMENT_MANAGER | EMPLOYEE                    |
|--------------------------|-----------|----------|-----------------|--------------------|-----------------------------|
| Create Time Entry        | ✅        | ✅      | ✅              | ❌                | ⚠️ Own                     |
| View All Entries         | ✅        | ✅      | ✅              | ❌                | ❌                         |
| View Own Entries         | ✅        | ✅      | ✅              | ❌                | ✅                         |
| Update Time Entry        | ✅        | ✅      | ✅              | ❌                | ⚠️ Own (Draft / Submitted) |
| Submit Time Entry        | ✅        | ✅      | ✅              | ❌                | ⚠️ Own                     |
| Approve Time Entry       | ✅        | ✅      | ✅              | ❌                | ❌                         |
| Reject Time Entry        | ✅        | ✅      | ✅              | ❌                | ❌                         |
| Delete Time Entry        | ✅        | ✅      | ✅              | ❌                | ⚠️ Own (Draft)             |

### Leave Request Management

| Action                    | ORG_ADMIN | HR_ADMIN | PROJECT_MANAGER | DEPARTMENT_MANAGER | EMPLOYEE                    |
|---------------------------|-----------|----------|-----------------|--------------------|-----------------------------|
| Create Leave Request      | ✅        | ✅      | ❌              | ✅                | ⚠️ Own                      |
| View All Requests         | ✅        | ✅      | ❌              | ⚠️ Department     | ❌                          |
| View Own Requests         | ✅        | ✅      | ❌              | ✅                | ✅                          |
| Update Leave Request      | ✅        | ✅      | ❌              | ❌                | ⚠️ Own (Pending)            |
| Approve Leave Request     | ✅        | ✅      | ❌              | ✅                | ❌                          |
| Reject Leave Request      | ✅        | ✅      | ❌              | ✅                | ❌                          |
| Cancel Leave Request      | ✅        | ✅      | ❌              | ❌                | ⚠️ Own                      |
| Delete Leave Request      | ✅        | ✅      | ❌              | ❌                | ⚠️ Own (Pending / Rejected) |


### Department Management

| Action               | ORG_ADMIN | HR_ADMIN | PROJECT_MANAGER | DEPARTMENT_MANAGER | EMPLOYEE |
|----------------------|-----------|----------|-----------------|------------------- |----------|
| Create Department    | ✅       | ✅       | ❌              | ❌                | ❌       |
| View All Departments | ✅       | ✅       | ❌              | ❌                | ❌       |
| View Own Department  | ✅       | ✅       | ❌              | ✅                | ❌       |
| Update Department    | ✅       | ✅       | ❌              | ❌                | ❌       |
| Delete Department    | ✅       | ✅       | ❌              | ❌                | ❌       |

### Role Management

| Action          | ORG_ADMIN | HR_ADMIN | PROJECT_MANAGER | DEPARTMENT_MANAGER | EMPLOYEE |
|-----------------|-----------|----------|-----------------|------------------- |----------|
| Create Role     | ✅       | ❌       | ❌             | ❌                | ❌        |
| View Roles      | ✅       | ✅       | ❌             | ✅                | ❌        |
| Update Role     | ✅       | ❌       | ❌             | ❌                | ❌        |
| Delete Role     | ✅       | ❌       | ❌             | ❌                | ❌        |
| Assign Role     | ✅       | ✅       | ❌             | ❌                | ❌        |
| Remove Role     | ✅       | ✅       | ❌             | ❌                | ❌        |
| View User Roles | ✅       | ✅       | ❌             | ✅                | ❌        |

### Reporting & Analytics

| Action                   | ORG_ADMIN | HR_ADMIN | PROJECT_MANAGER | DEPARTMENT_MANAGER | EMPLOYEE |
|--------------------------|-----------|----------|-----------------|-------------------|---------- |
| View Dashboard           | ✅       | ✅       | ✅             | ❌                | ❌       |
| View Project Stats       | ✅       | ✅       | ✅             | ❌                | ❌       |
| View Time Reports        | ✅       | ✅       | ✅             | ❌                | ❌       |
| View Leave Reports       | ✅       | ✅       | ✅             | ✅(department)    | ❌       |
| View Resource Allocation | ✅       | ✅       | ✅             | ❌                | ❌       |

### Search & Export

| Action        | ORG_ADMIN | HR_ADMIN | PROJECT_MANAGER | DEPARTMENT_MANAGER | EMPLOYEE       |
|-------------- |-----------|----------|-----------------|------------------- |----------------|
| Global Search | ✅       | ✅       | ✅              | ✅                | ✅ (limited)  |
| Export to CSV | ✅       | ✅       | ✅              | ✅                | ✅ (own data) |
| Export to PDF | ✅       | ✅       | ✅              | ✅                | ✅ (own data) |

---

## 🔒 Special Access Rules

### Department Managers
- Can only view/update employees in **their own department**
- Can approve/reject leave requests for **their department employees**
- Cannot access other departments' data

### Employees
- Can only view/update **their own** time entries, leave requests, and profile
- Can create tasks but cannot delete them
- Cannot view reports or analytics

### Project Managers
- Can manage projects and tasks
- Can approve/reject time entries
- Cannot manage employees or departments

---

## 📝 Notes

1. **Multi-tenant Isolation:** All roles are scoped to their organization. Users can only access data from their own organization.

2. **Role Assignment:** Only `ORG_ADMIN` and `HR_ADMIN` can assign roles to employees.

3. **Role Creation:** Only `ORG_ADMIN` can create, update, or delete roles.

4. **Hard Delete:** Only `ORG_ADMIN` can permanently delete projects (hard delete). Others can only archive.

5. **Self-Service:** Employees can manage their own time entries and leave requests, but cannot approve/reject them.

6. **Department Scope:** `DEPARTMENT_MANAGER` has limited access scoped to their department only.

---

## 🎯 Quick Reference

**Most Powerful:** `ORG_ADMIN` - Full access to everything

**HR Operations:** `HR_ADMIN` - Manages employees, departments, leave requests

**Project Operations:** `PROJECT_MANAGER` - Manages projects, tasks, time tracking

**Department Operations:** `DEPARTMENT_MANAGER` - Manages department employees, approves leave

**Standard User:** `EMPLOYEE` - Self-service for time entries and leave requests

---

**Last Updated:** 2024-01-14
