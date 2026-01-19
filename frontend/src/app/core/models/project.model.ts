/**
 * Project models matching backend DTOs
 */

export enum ProjectStatus {
  PLANNING = 'PLANNING',
  ACTIVE = 'ACTIVE',
  ON_HOLD = 'ON_HOLD',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface Project {
  id: string;
  name: string;
  description?: string;
  status: ProjectStatus;
  startDate?: string;
  endDate?: string;
  deadline?: string;
  budget?: number;
  cost?: number;
  managerName?: string;
  membersCount: number;
}

export interface ProjectCreateDTO {
  name: string;
  description?: string;
  status?: ProjectStatus;
  startDate?: string;
  deadline?: string;
  budget?: number;
  managerId?: string;
  memberIds?: string[];
}

export interface ProjectUpdateDTO {
  name?: string;
  description?: string;
  status?: ProjectStatus;
  endDate?: string;
  cost?: number;
}
