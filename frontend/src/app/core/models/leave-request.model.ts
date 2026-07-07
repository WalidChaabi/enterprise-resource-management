/**
 * Leave Request models matching backend DTOs
 */

export enum LeaveRequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED'
}

export type LeaveRequestStatusFilter = 'ALL' | LeaveRequestStatus;

export enum LeaveType {
  VACATION = 'VACATION',
  SICK = 'SICK',
  PERSONAL = 'PERSONAL',
  UNPAID = 'UNPAID',
  OTHER = 'OTHER'
}

export interface LeaveRequest {
  id: string;
  employeeId: string;
  employeeName?: string;
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  days: number;
  reason?: string;
  status: LeaveRequestStatus;
  approvedById?: string;
  approvedByName?: string;
  rejectedReason?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface LeaveRequestCreateDTO {
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  reason?: string;
}

export interface LeaveRequestUpdateDTO {
  leaveType?: LeaveType;
  startDate?: string;
  endDate?: string;
  reason?: string;
}
