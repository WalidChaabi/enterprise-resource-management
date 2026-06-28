import { ROLES, type Role } from '../../core/utils/constants';

export interface NavItem {
  label: string;
  route: string[];
  icon: string;
  section: 'core' | 'people' | 'insights';
  requiredRoles?: Role[];
  indicator?: {
    count: number;
    priority: 'high' | 'medium' | 'low';
  };
}

export interface NavSection {
  id: 'core' | 'people' | 'insights';
  label: string;
  items: NavItem[];
}

export interface CreateAction {
  label: string;
  route: string[];
  icon: string;
}

export const SIDEBAR_NAV_SECTIONS: NavSection[] = [
  { id: 'core', label: 'nav.sections.core', items: [] },
  { id: 'people', label: 'nav.sections.people', items: [] },
  { id: 'insights', label: 'nav.sections.insights', items: [] },
];

export const SIDEBAR_NAV_ITEMS: NavItem[] = [
  {
    label: 'nav.dashboard',
    route: ['/dashboard'],
    icon: 'M3 10.5L12 3l9 7.5V21a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1v-10.5z',
    section: 'core',
  },
  {
    label: 'nav.projects',
    route: ['/projects'],
    icon: 'M3 7a2 2 0 0 1 2-2h5l2 2h7a2 2 0 0 1 2 2v7a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7z',
    section: 'core',
    requiredRoles: [ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER],
  },
  {
    label: 'nav.employees',
    route: ['/employees'],
    icon: 'M16 11a4 4 0 1 1-8 0 4 4 0 0 1 8 0zM4 20a6 6 0 0 1 16 0',
    section: 'people',
    requiredRoles: [ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER],
  },
  {
    label: 'nav.departments',
    route: ['/departments'],
    icon: 'M4 4h7v7H4V4zm9 0h7v7h-7V4zM4 13h7v7H4v-7zm9 0h7v7h-7v-7z',
    section: 'people',
    requiredRoles: [ROLES.HR_ADMIN, ROLES.ORG_ADMIN],
  },
  {
    label: 'nav.timeEntries',
    route: ['/time-entries'],
    icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
    section: 'core',
    requiredRoles: [ROLES.EMPLOYEE, ROLES.PROJECT_MANAGER, ROLES.ORG_ADMIN],
  },
  {
    label: 'nav.leaveRequests',
    route: ['/leave-requests'],
    icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z',
    section: 'people',
  },
  {
    label: 'nav.reports',
    route: ['/reports'],
    icon: 'M4 4h16v16H4V4zm4 10v2m4-6v6m4-4v4',
    section: 'insights',
    requiredRoles: [ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER, ROLES.HR_ADMIN],
  },
];

export const SIDEBAR_CREATE_ACTIONS: Record<string, CreateAction> = {
  '/projects': {
    label: 'projects.newProject',
    route: ['/projects', 'new'],
    icon: 'M12 4v16m8-8H4',
  },
  '/employees': {
    label: 'employees.newEmployee',
    route: ['/employees', 'new'],
    icon: 'M12 4v16m8-8H4',
  },
  '/time-entries': {
    label: 'timeEntries.newTimeEntry',
    route: ['/time-entries', 'new'],
    icon: 'M12 4v16m8-8H4',
  },
  '/leave-requests': {
    label: 'leaveRequests.newLeaveRequest',
    route: ['/leave-requests', 'new'],
    icon: 'M12 4v16m8-8H4',
  },
};
