export interface EmployeeCapacityRow {
  employeeId: string;
  employeeName: string;
  employeeCode?: string;
  jobTitle?: string;
  loggedHours: number;
  capacityHoursPerWeek: number;
  utilizationPercentage: number;
  allocationStatus: 'AVAILABLE' | 'NORMAL' | 'OVERALLOCATED' | 'UNDERUTILIZED';
  onLeaveThisWeek: boolean;
  leaveDaysThisWeek: number;
  leaveSummary?: string;
}

export interface CapacityDashboard {
  weekStart: string;
  weekEnd: string;
  employees: EmployeeCapacityRow[];
  totalEmployees: number;
  overallocatedEmployees: number;
  availableEmployees: number;
  underutilizedEmployees: number;
  onLeaveEmployees: number;
}
