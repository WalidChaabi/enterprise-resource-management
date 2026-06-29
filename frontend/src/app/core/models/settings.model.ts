export interface ShortcutDefinition {
  key: string;
  action: string;
}

export interface ProfileSettings {
  firstName: string;
  lastName: string;
  email: string;
}

export interface ProfileSettingsUpdate {
  firstName?: string;
  lastName?: string;
}

export interface OrganizationSettings {
  name: string;
  description?: string;
}

export interface OrganizationSettingsUpdate {
  name?: string;
  description?: string;
}

export interface NotificationPreferences {
  emailEnabled: boolean;
  inAppEnabled: boolean;
}

export interface NotificationPreferencesUpdate {
  emailEnabled?: boolean;
  inAppEnabled?: boolean;
}

export interface BillingSettings {
  planName: string;
  status: string;
  nextBillingDate?: string;
  trialEndsAt?: string;
  amount?: number;
  currency?: string;
  seatCount?: number;
  portalAvailable?: boolean;
  checkoutAvailable?: boolean;
}

export interface BillingPortalResponse {
  url: string;
}

export interface Entitlements {
  planCode: string;
  planName: string;
  entitlements: string[];
  seatLimit?: number | null;
  seatCount: number;
  upgradeRequired: boolean;
}
