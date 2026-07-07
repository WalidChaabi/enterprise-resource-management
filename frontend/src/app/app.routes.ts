import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';
import { ROLES } from './core/utils/constants';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => 
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'signup',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/signup/signup.component').then(m => m.SignupComponent)
  },
  {
    path: 'forgot-password',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
  },
  {
    path: 'reset-password',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
  },
  {
    path: 'login/mfa',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/mfa-verify/mfa-verify.component').then(m => m.MfaVerifyComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => 
      import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => 
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'employees',
        canActivate: [roleGuard(ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER)],
        loadChildren: () => 
          import('./features/employees/employees.routes').then(m => m.routes)
      },
      {
        path: 'projects',
        canActivate: [roleGuard(ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER)],
        loadChildren: () => 
          import('./features/projects/projects.routes').then(m => m.routes)
      },
      {
        path: 'departments',
        canActivate: [roleGuard(ROLES.HR_ADMIN, ROLES.ORG_ADMIN)],
        loadChildren: () => 
          import('./features/departments/departments.routes').then(m => m.routes)
      },
      {
        path: 'reports',
        canActivate: [roleGuard(ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER, ROLES.HR_ADMIN)],
        loadChildren: () => 
          import('./features/reporting/reporting.routes').then(m => m.routes)
      },
      {
        path: 'time-entries',
        canActivate: [roleGuard(ROLES.EMPLOYEE, ROLES.PROJECT_MANAGER, ROLES.ORG_ADMIN)],
        loadChildren: () => 
          import('./features/time-entries/time-entries.routes').then(m => m.routes)
      },
      {
        path: 'leave-requests',
        canActivate: [roleGuard(ROLES.EMPLOYEE, ROLES.HR_ADMIN, ROLES.ORG_ADMIN)],
        loadChildren: () => 
          import('./features/leave-requests/leave-requests.routes').then(m => m.routes)
      },
      {
        path: 'search',
        loadChildren: () => 
          import('./features/search/search.routes').then(m => m.routes)
      },
      {
        path: 'settings',
        loadComponent: () =>
          import('./features/settings/settings.component').then(m => m.SettingsComponent),
        loadChildren: () =>
          import('./features/settings/settings.routes').then(m => m.routes)
      },
      {
        path: '',
        redirectTo: '/dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
