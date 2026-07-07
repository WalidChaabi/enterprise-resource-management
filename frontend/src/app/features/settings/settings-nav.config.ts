export interface SettingsNavItem {
  id: string;
  route: string;
  labelKey: string;
}

export const SETTINGS_NAV_ITEMS: SettingsNavItem[] = [
  { id: 'profile', route: '/settings/profile', labelKey: 'settings.tabs.profile' },
  { id: 'security', route: '/settings/security', labelKey: 'settings.tabs.security' },
  { id: 'organization', route: '/settings/organization', labelKey: 'settings.tabs.organization' },
  { id: 'notifications', route: '/settings/notifications', labelKey: 'settings.tabs.notifications' },
  { id: 'roles', route: '/settings/roles', labelKey: 'settings.tabs.roles' },
  { id: 'billing', route: '/settings/billing', labelKey: 'settings.tabs.billing' },
  { id: 'audit', route: '/settings/audit', labelKey: 'settings.tabs.audit' },
  { id: 'privacy', route: '/settings/privacy', labelKey: 'settings.tabs.privacy' },
  { id: 'webhooks', route: '/settings/webhooks', labelKey: 'settings.tabs.webhooks' },
];

export function settingsTabIdFromUrl(url: string): string {
  const item = SETTINGS_NAV_ITEMS.find(
    (entry) => url === entry.route || url.startsWith(`${entry.route}/`),
  );
  return item?.id ?? 'profile';
}
