/**
 * Authentication models matching backend DTOs
 */

export interface AuthRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId: string;
  roles: string[];
}

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId: string;
  roles: string[];
  active: boolean;
  departmentId?: string;
  departmentName?: string;
}

export interface ErrorResponse {
  code: string;
  details: string[];
}
