/**
 * User Role models matching backend DTOs
 */

export interface UserRole {
  id: string;
  userId: string;
  employeeId?: string;
  roleId: string;
  roleName: string;
  roleCode: string;
  organizationId: string;
  assignedAt?: string;
  assignedById?: string;
  assignedByName?: string;
}

export interface UserRoleAssignDTO {
  userId: string;
  roleId: string;
}

export interface Role {
  id: string;
  name: string;
  code: string;
  description?: string;
  organizationId: string;
}
