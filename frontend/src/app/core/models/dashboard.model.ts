import { Alert } from './alert.model';

/**
 * Dashboard DTO matching backend structure
 */
export interface Dashboard {
  // Projects
  totalProjects: number;
  activeProjects: number;
  completedProjects: number;
  projectsOnHold: number;
  
  // Time Tracking
  totalHoursThisMonth: number;
  totalHoursThisWeek: number;
  pendingTimeEntries: number;
  approvedTimeEntries: number;
  
  // Leave Management
  pendingLeaveRequests: number;
  approvedLeaveRequests: number;
  upcomingLeaveRequests: number;
  
  // Employees
  totalEmployees: number;
  activeEmployees: number;
  
  // Budget
  totalBudget: number;
  totalCost: number;
  budgetUtilization: number;
  
  // Intelligent Alerts
  alerts: Alert[];
}
