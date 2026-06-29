export interface TimeReport {
  id: string;
  name: string;
  totalHours: number;
  entriesCount: number;
  averageHoursPerEntry: number;
}

export interface LeaveBalance {
  employeeId: string;
  employeeName: string;
  employeeEmail: string;
  totalDaysUsed: number;
  totalDaysPending: number;
  totalDaysApproved: number;
  totalDaysThisYear: number;
}

export interface LeaveReport {
  employeeId: string;
  employeeName: string;
  employeeEmail: string;
  leaveRequestId: string;
  leaveType: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  status: string;
}

export interface ProjectStats {
  projectId: string;
  projectName: string;
  status: string;
  startDate?: string;
  deadline?: string;
  endDate?: string;
  budget?: number;
  cost?: number;
  budgetUtilization?: number;
  membersCount: number;
  tasksCount: number;
  totalHoursLogged: number;
  completionPercentage: number;
}

export type ReportingTab = 'overview' | 'time' | 'leave';
