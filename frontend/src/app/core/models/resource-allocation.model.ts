/**
 * Employee allocation information
 */
export interface EmployeeAllocation {
  employeeId: string;
  employeeName: string;
  employeeCode?: string;
  jobTitle?: string;
  active: boolean;
  currentProjectsCount: number;
  currentProjectIds: string[];
  currentProjectNames: string[];
  totalHoursThisWeek: number;
  capacityHoursPerWeek: number;
  utilizationPercentage: number;
  allocationStatus: 'AVAILABLE' | 'NORMAL' | 'OVERALLOCATED' | 'UNDERUTILIZED';
  availableFrom?: string;
  notes?: string;
}

/**
 * Project allocation information
 */
export interface ProjectAllocation {
  projectId: string;
  projectName: string;
  projectStatus?: string;
  startDate?: string;
  endDate?: string;
  deadline?: string;
  assignedMembersCount: number;
  assignedMemberIds: string[];
  assignedMemberNames: string[];
  requiredHoursPerWeek: number;
  allocatedHoursPerWeek: number;
  gapHours: number;
  budget?: number;
  cost?: number;
  budgetUtilizationPercentage: number;
  allocationStatus: 'WELL_STAFFED' | 'UNDERSTAFFED' | 'OVERSTAFFED' | 'NO_RESOURCES';
  hasRecentTimeEntries: boolean;
  daysSinceLastTimeEntry: number;
  deadlineApproaching: boolean;
  isOverBudget: boolean;
}

/**
 * Complete resource allocation view
 */
export interface ResourceAllocation {
  employees: EmployeeAllocation[];
  projects: ProjectAllocation[];
  totalEmployees: number;
  availableEmployees: number;
  overallocatedEmployees: number;
  underutilizedEmployees: number;
  totalProjects: number;
  wellStaffedProjects: number;
  understaffedProjects: number;
  projectsWithoutResources: number;
}
