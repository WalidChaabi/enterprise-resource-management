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
  emailNotifications: boolean;
  pushNotifications: boolean;
  projectUpdates: boolean;
  taskAssignments: boolean;
}

export interface NotificationPreferencesUpdate {
  emailNotifications?: boolean;
  pushNotifications?: boolean;
  projectUpdates?: boolean;
  taskAssignments?: boolean;
}

export interface BillingSettings {
  plan: string;
  status: string;
  nextBillingDate?: string;
}
