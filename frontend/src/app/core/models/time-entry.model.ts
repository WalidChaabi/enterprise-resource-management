/**
 * Time Entry models matching backend DTOs
 */

export enum TimeEntryStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export interface TimeEntry {
  id: string;
  projectId: string;
  projectName?: string;
  taskId?: string;
  taskTitle?: string;
  employeeId: string;
  employeeName?: string;
  /** ISO date (YYYY-MM-DD) — alias for entryDate */
  date: string;
  entryDate: string;
  startTime?: string;
  endTime?: string;
  /** Duration in hours — alias for durationHours */
  hours: number;
  durationHours: number;
  description?: string;
  status: TimeEntryStatus;
  approvedById?: string;
  approvedByName?: string;
  rejectedReason?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface TimeEntryCreateDTO {
  projectId: string;
  entryDate: string;
  startTime: string;
  endTime: string;
  description?: string;
}

export interface TimeEntryUpdateDTO {
  entryDate?: string;
  startTime?: string;
  endTime?: string;
  description?: string;
}

export type TimeEntryStatusFilter = TimeEntryStatus | 'ALL';
