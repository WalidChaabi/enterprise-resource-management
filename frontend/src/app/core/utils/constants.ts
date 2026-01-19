/**
 * Application-wide constants
 */

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login'
  },
  ORGANIZATIONS: (orgId: string) => `/api/v1/organizations/${orgId}`,
  EMPLOYEES: (orgId: string) => `/api/v1/organizations/${orgId}/employees`,
  PROJECTS: (orgId: string) => `/api/v1/organizations/${orgId}/projects`,
  DEPARTMENTS: (orgId: string) => `/api/v1/organizations/${orgId}/departments`,
  ROLES: (orgId: string) => `/api/v1/organizations/${orgId}/roles`,
  TASKS: (orgId: string, projectId: string) => 
    `/api/v1/organizations/${orgId}/projects/${projectId}/tasks`,
  TIME_ENTRIES: (orgId: string) => `/api/v1/organizations/${orgId}/time-entries`,
  LEAVE_REQUESTS: (orgId: string) => `/api/v1/organizations/${orgId}/leave-requests`,
  REPORTS: (orgId: string) => `/api/v1/organizations/${orgId}/reports`,
  SEARCH: (orgId: string) => `/api/v1/organizations/${orgId}/search`,
  RESOURCE_ALLOCATION: (orgId: string) => `/api/v1/organizations/${orgId}/reports/resource-allocation`,
  // New endpoints
  NOTIFICATIONS: '/api/notifications',
  SETTINGS: '/api/settings',
  ORGANIZATIONS_SWITCHER: '/api/organizations',
  UI_CONFIG: '/api/config/ui'
};

export const STORAGE_KEYS = {
  TOKEN: 'access_token',
  USER: 'current_user',
  ORGANIZATION_ID: 'organization_id'
};

export const ROLES = {
  ORG_ADMIN: 'ORG_ADMIN',
  HR_ADMIN: 'HR_ADMIN',
  PROJECT_MANAGER: 'PROJECT_MANAGER',
  DEPARTMENT_MANAGER: 'DEPARTMENT_MANAGER',
  EMPLOYEE: 'EMPLOYEE'
} as const;

export type Role = typeof ROLES[keyof typeof ROLES];
