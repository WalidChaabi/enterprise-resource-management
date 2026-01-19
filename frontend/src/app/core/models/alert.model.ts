/**
 * Alert types for intelligent alerts
 */
export enum AlertType {
  PROJECT_WITHOUT_RESOURCES = 'PROJECT_WITHOUT_RESOURCES',
  EMPLOYEE_OVERALLOCATED = 'EMPLOYEE_OVERALLOCATED',
  PROJECT_AT_RISK = 'PROJECT_AT_RISK',
  PROJECT_UNDERSTAFFED = 'PROJECT_UNDERSTAFFED',
  PROJECT_DEADLINE_APPROACHING = 'PROJECT_DEADLINE_APPROACHING',
  PROJECT_OVER_BUDGET = 'PROJECT_OVER_BUDGET'
}

/**
 * Alert severity levels
 */
export type AlertSeverity = 'HIGH' | 'MEDIUM' | 'LOW';

/**
 * Alert DTO matching backend structure
 */
export interface Alert {
  type: AlertType;
  severity: AlertSeverity;
  message: string;
  projectId?: string;
  projectName?: string;
  employeeId?: string;
  employeeName?: string;
  details?: string;
}
