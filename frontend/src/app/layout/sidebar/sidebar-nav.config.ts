import { LucideIconData, Activity, BarChart3, Building2, Calendar, Clock, FolderKanban, LayoutDashboard, Users } from 'lucide-angular';
import { ROLES, type Role } from '../../core/utils/constants';

export interface NavItem {
  label: string;
  route: string[];
  icon: LucideIconData;
  section: 'core' | 'people' | 'insights';
  requiredRoles?: Role[];
  requiredEntitlements?: string[];
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

export const SIDEBAR_NAV_SECTIONS: NavSection[] = [
  { id: 'core', label: 'nav.sections.core', items: [] },
  { id: 'people', label: 'nav.sections.people', items: [] },
  { id: 'insights', label: 'nav.sections.insights', items: [] },
];

export const SIDEBAR_NAV_ITEMS: NavItem[] = [
  {
    label: 'nav.dashboard',
    route: ['/dashboard'],
    icon: LayoutDashboard,
    section: 'core',
  },
  {
    label: 'nav.projects',
    route: ['/projects'],
    icon: FolderKanban,
    section: 'core',
    requiredRoles: [ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER],
  },
  {
    label: 'nav.employees',
    route: ['/employees'],
    icon: Users,
    section: 'people',
    requiredRoles: [ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER],
  },
  {
    label: 'nav.departments',
    route: ['/departments'],
    icon: Building2,
    section: 'people',
    requiredRoles: [ROLES.HR_ADMIN, ROLES.ORG_ADMIN],
  },
  {
    label: 'nav.timeEntries',
    route: ['/time-entries'],
    icon: Clock,
    section: 'core',
    requiredRoles: [ROLES.EMPLOYEE, ROLES.PROJECT_MANAGER, ROLES.ORG_ADMIN],
  },
  {
    label: 'nav.leaveRequests',
    route: ['/leave-requests'],
    icon: Calendar,
    section: 'people',
  },
  {
    label: 'nav.reports',
    route: ['/reports'],
    icon: BarChart3,
    section: 'insights',
    requiredRoles: [ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER, ROLES.HR_ADMIN],
    requiredEntitlements: ['REPORTING'],
  },
  {
    label: 'nav.capacity',
    route: ['/reports', 'capacity'],
    icon: Activity,
    section: 'insights',
    requiredRoles: [ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER, ROLES.HR_ADMIN],
    requiredEntitlements: ['REPORTING'],
  },
];
