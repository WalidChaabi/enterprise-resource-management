/**
 * Department models matching backend DTOs
 */

export interface Department {
  id: string;
  name: string;
  description?: string;
  organizationId: string;
  managerId?: string;
  managerName?: string;
  employeeCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface DepartmentCreateDTO {
  name: string;
  description?: string;
  managerId?: string;
}

export interface DepartmentUpdateDTO {
  name?: string;
  description?: string;
  managerId?: string;
}
