/**
 * Employee models matching backend DTOs
 */

export interface Employee {
  id: string;
  userId?: string;
  employeeCode?: string;
  jobTitle?: string;
  active: boolean;
  email?: string;
  firstName?: string;
  lastName?: string;
  departmentId?: string;
  departmentName?: string;
  departmentCode?: string;
  organizationId: string;
  organizationName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface EmployeeCreateDTO {
  userId: string;
  employeeCode?: string;
  jobTitle?: string;
  departmentId?: string;
}

export interface EmployeeUpdateDTO {
  employeeCode?: string;
  jobTitle?: string;
  departmentId?: string;
  active?: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
