# Angular Frontend Setup Guide

## Prerequisites

- Node.js 18+ and npm 9+
- Angular CLI 17+
- Backend running on `http://localhost:8080`

---

## Step 1: Create Angular Project

```bash
# Install Angular CLI globally (if not already installed)
npm install -g @angular/cli

# Create new Angular project
ng new enterprise-resource-management-frontend

# When prompted:
# - Would you like to add Angular routing? → Yes
# - Which stylesheet format would you like to use? → SCSS
# - Do you want to enable Server-Side Rendering (SSR)? → No (for now)

cd enterprise-resource-management-frontend
```

---

## Step 2: Install Required Dependencies

```bash
# Core dependencies
npm install @angular/common@^17.0.0
npm install @angular/forms@^17.0.0
npm install @angular/router@^17.0.0

# HTTP client (already included, but ensure it's imported)
# No additional install needed

# UI libraries (optional - choose one)
npm install @angular/material@^17.0.0 @angular/cdk@^17.0.0
# OR
npm install primeng primeicons
# OR
npm install ng-bootstrap bootstrap

# JWT handling
npm install @auth0/angular-jwt

# Date handling
npm install date-fns

# UUID handling (if needed)
npm install uuid
npm install --save-dev @types/uuid
```

---

## Step 3: Project Structure

Create the following structure:

```
src/app/
├── core/
│   ├── models/
│   │   ├── user.model.ts
│   │   ├── organization.model.ts
│   │   ├── employee.model.ts
│   │   ├── project.model.ts
│   │   ├── task.model.ts
│   │   ├── department.model.ts
│   │   ├── role.model.ts
│   │   ├── time-entry.model.ts
│   │   └── leave-request.model.ts
│   ├── services/
│   │   ├── auth.service.ts
│   │   ├── api.service.ts
│   │   ├── employee.service.ts
│   │   ├── project.service.ts
│   │   ├── department.service.ts
│   │   ├── role.service.ts
│   │   ├── task.service.ts
│   │   ├── time-entry.service.ts
│   │   ├── leave-request.service.ts
│   │   ├── reporting.service.ts
│   │   └── search.service.ts
│   ├── guards/
│   │   ├── auth.guard.ts
│   │   └── role.guard.ts
│   ├── interceptors/
│   │   ├── auth.interceptor.ts
│   │   └── error.interceptor.ts
│   └── utils/
│       ├── constants.ts
│       └── helpers.ts
├── features/
│   ├── auth/
│   │   ├── login/
│   │   │   ├── login.component.ts
│   │   │   ├── login.component.html
│   │   │   └── login.component.scss
│   │   └── auth-routing.module.ts
│   ├── dashboard/
│   │   ├── dashboard.component.ts
│   │   ├── dashboard.component.html
│   │   └── dashboard.component.scss
│   ├── employees/
│   │   ├── employee-list/
│   │   ├── employee-detail/
│   │   ├── employee-form/
│   │   └── employees-routing.module.ts
│   ├── projects/
│   │   ├── project-list/
│   │   ├── project-detail/
│   │   ├── project-form/
│   │   └── projects-routing.module.ts
│   ├── departments/
│   ├── tasks/
│   ├── time-entries/
│   ├── leave-requests/
│   ├── reporting/
│   └── search/
├── shared/
│   ├── components/
│   │   ├── header/
│   │   ├── sidebar/
│   │   ├── loading-spinner/
│   │   └── error-message/
│   └── pipes/
│       └── date-format.pipe.ts
└── app.component.ts
```

---

## Step 4: Environment Configuration

**src/environments/environment.ts:**
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  apiVersion: 'v1',
  jwtSecret: 'your-jwt-secret' // Should match backend
};
```

**src/environments/environment.prod.ts:**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.yourdomain.com',
  apiVersion: 'v1',
  jwtSecret: 'your-production-jwt-secret'
};
```

---

## Step 5: Core Models

Create models matching your backend DTOs:

**src/app/core/models/user.model.ts:**
```typescript
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId: string;
  roles: string[];
  active: boolean;
}

export interface AuthRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId: string;
  roles: string[];
}
```

**src/app/core/models/project.model.ts:**
```typescript
export enum ProjectStatus {
  PLANNING = 'PLANNING',
  ACTIVE = 'ACTIVE',
  ON_HOLD = 'ON_HOLD',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface Project {
  id: string;
  name: string;
  description?: string;
  status: ProjectStatus;
  startDate?: string;
  endDate?: string;
  deadline?: string;
  budget?: number;
  cost?: number;
  managerName?: string;
  membersCount: number;
}

export interface ProjectCreateDTO {
  name: string;
  description?: string;
  status?: ProjectStatus;
  startDate?: string;
  deadline?: string;
  budget?: number;
  managerId?: string;
  memberIds?: string[];
}

export interface ProjectUpdateDTO {
  name?: string;
  description?: string;
  status?: ProjectStatus;
  endDate?: string;
  cost?: number;
}
```

**src/app/core/models/employee.model.ts:**
```typescript
export interface Employee {
  id: string;
  userId?: string;
  employeeCode?: string;
  jobTitle?: string;
  active: boolean;
  email?: string;
  firstName?: string;
  lastName?: string;
  departmentId?: string;
  departmentName?: string;
  organizationId: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface EmployeeCreateDTO {
  userId: string;
  employeeCode?: string;
  jobTitle?: string;
  departmentId?: string;
}

export interface EmployeeUpdateDTO {
  employeeCode?: string;
  jobTitle?: string;
  departmentId?: string;
  active?: boolean;
}
```

---

## Step 6: Constants

**src/app/core/utils/constants.ts:**
```typescript
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login'
  },
  ORGANIZATIONS: (orgId: string) => `/api/v1/organizations/${orgId}`,
  EMPLOYEES: (orgId: string) => `/api/v1/organizations/${orgId}/employees`,
  PROJECTS: (orgId: string) => `/api/v1/organizations/${orgId}/projects`,
  DEPARTMENTS: (orgId: string) => `/api/v1/organizations/${orgId}/departments`,
  ROLES: (orgId: string) => `/api/v1/organizations/${orgId}/roles`,
  TASKS: (orgId: string, projectId: string) => 
    `/api/v1/organizations/${orgId}/projects/${projectId}/tasks`,
  TIME_ENTRIES: (orgId: string) => `/api/v1/organizations/${orgId}/time-entries`,
  LEAVE_REQUESTS: (orgId: string) => `/api/v1/organizations/${orgId}/leave-requests`,
  REPORTS: (orgId: string) => `/api/v1/organizations/${orgId}/reports`,
  SEARCH: (orgId: string) => `/api/v1/organizations/${orgId}/search`
};

export const STORAGE_KEYS = {
  TOKEN: 'access_token',
  USER: 'current_user',
  ORGANIZATION_ID: 'organization_id'
};

export const ROLES = {
  ORG_ADMIN: 'ORG_ADMIN',
  HR_ADMIN: 'HR_ADMIN',
  PROJECT_MANAGER: 'PROJECT_MANAGER',
  DEPARTMENT_MANAGER: 'DEPARTMENT_MANAGER',
  EMPLOYEE: 'EMPLOYEE'
};
```

---

## Step 7: Auth Service

**src/app/core/services/auth.service.ts:**
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthRequest, AuthResponse, User } from '../models/user.model';
import { STORAGE_KEYS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}${API_ENDPOINTS.AUTH.LOGIN}`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadUserFromStorage();
  }

  login(credentials: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl, credentials).pipe(
      tap(response => {
        this.setSession(response);
        this.setCurrentUser(response);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    localStorage.removeItem(STORAGE_KEYS.ORGANIZATION_ID);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(STORAGE_KEYS.TOKEN);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getOrganizationId(): string | null {
    return localStorage.getItem(STORAGE_KEYS.ORGANIZATION_ID);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.roles?.includes(role) ?? false;
  }

  hasAnyRole(...roles: string[]): boolean {
    const user = this.getCurrentUser();
    if (!user?.roles) return false;
    return roles.some(role => user.roles.includes(role));
  }

  private setSession(response: AuthResponse): void {
    localStorage.setItem(STORAGE_KEYS.TOKEN, response.accessToken);
    localStorage.setItem(STORAGE_KEYS.ORGANIZATION_ID, response.organizationId);
  }

  private setCurrentUser(response: AuthResponse): void {
    const user: User = {
      id: response.id,
      email: response.email,
      firstName: response.firstName,
      lastName: response.lastName,
      organizationId: response.organizationId,
      roles: response.roles,
      active: true
    };
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private loadUserFromStorage(): void {
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    if (userStr) {
      this.currentUserSubject.next(JSON.parse(userStr));
    }
  }
}
```

---

## Step 8: API Base Service

**src/app/core/services/api.service.ts:**
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = `${environment.apiUrl}/api/${environment.apiVersion}`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    
    return headers;
  }

  get<T>(url: string, params?: HttpParams): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}${url}`, {
      headers: this.getHeaders(),
      params
    });
  }

  post<T>(url: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${url}`, body, {
      headers: this.getHeaders()
    });
  }

  put<T>(url: string, body: any): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${url}`, body, {
      headers: this.getHeaders()
    });
  }

  patch<T>(url: string, body: any): Observable<T> {
    return this.http.patch<T>(`${this.baseUrl}${url}`, body, {
      headers: this.getHeaders()
    });
  }

  delete<T>(url: string): Observable<T> {
    return this.http.delete<T>(`${this.baseUrl}${url}`, {
      headers: this.getHeaders()
    });
  }
}
```

---

## Step 9: Auth Interceptor

**src/app/core/interceptors/auth.interceptor.ts:**
```typescript
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();
    
    if (token && !request.url.includes('/auth/login')) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request);
  }
}
```

**app.config.ts (or app.module.ts):**
```typescript
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor])),
    // ... other providers
  ]
};
```

---

## Step 10: Auth Guard

**src/app/core/guards/auth.guard.ts:**
```typescript
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};
```

**src/app/core/guards/role.guard.ts:**
```typescript
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (...allowedRoles: string[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isAuthenticated() && authService.hasAnyRole(...allowedRoles)) {
      return true;
    }

    router.navigate(['/unauthorized']);
    return false;
  };
};
```

---

## Step 11: Project Service Example

**src/app/core/services/project.service.ts:**
```typescript
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Project, ProjectCreateDTO, ProjectUpdateDTO } from '../models/project.model';
import { API_ENDPOINTS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  constructor(
    private api: ApiService,
    private authService: AuthService
  ) {}

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found');
    }
    return orgId;
  }

  getProjects(): Observable<Project[]> {
    const orgId = this.getOrgId();
    return this.api.get<Project[]>(API_ENDPOINTS.PROJECTS(orgId));
  }

  getProject(projectId: string): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.get<Project>(`${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}`);
  }

  createProject(dto: ProjectCreateDTO): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.post<Project>(API_ENDPOINTS.PROJECTS(orgId), dto);
  }

  updateProject(projectId: string, dto: ProjectUpdateDTO): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.put<Project>(`${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}`, dto);
  }

  archiveProject(projectId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}`);
  }

  deleteProject(projectId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}/hard`);
  }

  addMember(projectId: string, employeeId: string): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.post<Project>(
      `${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}/members/${employeeId}`,
      {}
    );
  }

  removeMember(projectId: string, employeeId: string): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.delete<Project>(
      `${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}/members/${employeeId}`
    );
  }
}
```

---

## Step 12: Routing Configuration

**src/app/app.routes.ts:**
```typescript
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'employees',
        canActivate: [roleGuard('HR_ADMIN', 'ORG_ADMIN', 'DEPARTMENT_MANAGER')],
        loadChildren: () => import('./features/employees/employees.routes').then(m => m.routes)
      },
      {
        path: 'projects',
        canActivate: [roleGuard('ORG_ADMIN', 'PROJECT_MANAGER')],
        loadChildren: () => import('./features/projects/projects.routes').then(m => m.routes)
      },
      {
        path: 'departments',
        canActivate: [roleGuard('HR_ADMIN', 'ORG_ADMIN')],
        loadChildren: () => import('./features/departments/departments.routes').then(m => m.routes)
      },
      {
        path: 'reports',
        canActivate: [roleGuard('ORG_ADMIN', 'PROJECT_MANAGER', 'HR_ADMIN')],
        loadChildren: () => import('./features/reporting/reporting.routes').then(m => m.routes)
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
```

---

## Step 13: Login Component

**src/app/features/auth/login/login.component.ts:**
```typescript
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  loading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.errorMessage = error.error?.details?.[0] || 'Login failed. Please try again.';
          this.loading = false;
        }
      });
    }
  }
}
```

**src/app/features/auth/login/login.component.html:**
```html
<div class="login-container">
  <div class="login-card">
    <h2>Enterprise Resource Management</h2>
    <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
      <div class="form-group">
        <label for="email">Email</label>
        <input 
          type="email" 
          id="email" 
          formControlName="email"
          [class.error]="loginForm.get('email')?.invalid && loginForm.get('email')?.touched">
        <div *ngIf="loginForm.get('email')?.invalid && loginForm.get('email')?.touched" 
             class="error-message">
          Valid email is required
        </div>
      </div>

      <div class="form-group">
        <label for="password">Password</label>
        <input 
          type="password" 
          id="password" 
          formControlName="password"
          [class.error]="loginForm.get('password')?.invalid && loginForm.get('password')?.touched">
        <div *ngIf="loginForm.get('password')?.invalid && loginForm.get('password')?.touched" 
             class="error-message">
          Password is required (min 6 characters)
        </div>
      </div>

      <div *ngIf="errorMessage" class="alert alert-error">
        {{ errorMessage }}
      </div>

      <button 
        type="submit" 
        [disabled]="loginForm.invalid || loading"
        class="btn btn-primary">
        <span *ngIf="!loading">Login</span>
        <span *ngIf="loading">Logging in...</span>
      </button>
    </form>
  </div>
</div>
```

---

## Step 14: Update app.config.ts

**src/app/app.config.ts:**
```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimations()
  ]
};
```

---

## Step 15: Quick Start Commands

```bash
# Generate components
ng generate component features/dashboard/dashboard
ng generate component features/auth/login/login
ng generate component features/projects/project-list
ng generate component features/projects/project-detail
ng generate component features/employees/employee-list

# Generate services (if not using manual creation)
ng generate service core/services/employee
ng generate service core/services/department
ng generate service core/services/reporting

# Run development server
ng serve

# Build for production
ng build --configuration production
```

---

## Step 16: Testing the Integration

1. **Start Backend:**
   ```bash
   cd backend
   docker compose -f docker/docker-compose.dev.yml up
   ```

2. **Start Frontend:**
   ```bash
   cd frontend
   ng serve
   ```

3. **Test Login:**
   - Navigate to `http://localhost:4200/login`
   - Use credentials: `admin@demo.com` / `password123`
   - Should redirect to dashboard

4. **Test API Calls:**
   - Check browser DevTools → Network tab
   - Verify JWT token is sent in Authorization header
   - Test fetching projects, employees, etc.

---

## Next Steps

1. Implement remaining services (Employee, Department, Task, etc.)
2. Create UI components for each feature
3. Add error handling and loading states
4. Implement search functionality
5. Add reporting dashboards
6. Style with your chosen UI library

---

## Common Issues & Solutions

**CORS Errors:**
- Ensure backend has CORS configured for `http://localhost:4200`

**401 Unauthorized:**
- Check token is being sent in headers
- Verify token hasn't expired
- Check backend JWT_SECRET matches

**404 Not Found:**
- Verify API endpoints match backend routes
- Check organization ID is correct

---

**Your Angular frontend is now ready to integrate with the backend!** 🚀
