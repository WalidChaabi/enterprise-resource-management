export interface MfaEnrollConfirm {
  secret: string;
  code: string;
}

export interface SecuritySettingsUpdate {
  ssoEnabled?: boolean;
  requireMfa?: boolean;
  ssoProvider?: string;
}

export type { MfaSetupResponse, MfaBackupCodesResponse, SecuritySettings } from './auth.model';
