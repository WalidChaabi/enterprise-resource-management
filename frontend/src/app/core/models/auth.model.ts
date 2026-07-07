/**
 * Authentication models matching backend DTOs
 */

export interface AuthRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  organizationName: string;
  acceptTerms: boolean;
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

export interface LoginResponse {
  mfaRequired: boolean;
  mfaToken?: string;
  session?: AuthResponse;
}

export interface MfaVerifyRequest {
  mfaToken: string;
  code: string;
}

export interface SsoProvidersResponse {
  providers: string[];
}

export interface SecuritySettings {
  ssoEnabled: boolean;
  requireMfa: boolean;
  ssoProvider?: string;
  userMfaEnabled: boolean;
  unusedBackupCodes: number;
  availableSsoProviders: string[];
}

export interface MfaSetupResponse {
  secret: string;
  qrCodeUri: string;
  issuer: string;
  accountName: string;
}

export interface MfaBackupCodesResponse {
  codes: string[];
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
