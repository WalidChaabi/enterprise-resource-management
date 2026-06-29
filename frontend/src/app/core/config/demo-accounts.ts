export interface DemoAccount {
  email: string;
  password: string;
  labelKey: string;
}

/** Dev/demo logins — password is always password123 */
export const DEMO_ACCOUNTS: DemoAccount[] = [
  { email: 'admin@demo.com', password: 'password123', labelKey: 'auth.login.demoAccounts.admin' },
  { email: 'hr@demo.com', password: 'password123', labelKey: 'auth.login.demoAccounts.hr' },
  { email: 'pm@demo.com', password: 'password123', labelKey: 'auth.login.demoAccounts.pm' },
  { email: 'employee@demo.com', password: 'password123', labelKey: 'auth.login.demoAccounts.employee' },
  { email: 'manager@demo.com', password: 'password123', labelKey: 'auth.login.demoAccounts.manager' },
];

export const DEMO_PASSWORD = 'password123';
