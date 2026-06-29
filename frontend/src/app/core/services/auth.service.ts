import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthRequest, AuthResponse, SignupRequest, User } from '../models/auth.model';
import { EntitlementService } from './entitlement.service';
import { STORAGE_KEYS, type Role } from '../utils/constants';

/**
 * Authentication service using httpOnly cookies for tokens.
 * User profile is stored in sessionStorage (not the JWT).
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly entitlementService = inject(EntitlementService);
  private readonly apiBase = environment.apiUrl;

  private readonly currentUserSignal = signal<User | null>(null);
  public readonly currentUser = this.currentUserSignal.asReadonly();

  public readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  public readonly organizationId = computed(() => this.currentUserSignal()?.organizationId ?? null);

  constructor() {
    this.loadUserFromStorage();
  }

  login(credentials: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiBase}/auth/login`, credentials, {
      withCredentials: true
    }).pipe(
      tap(response => this.setCurrentUser(response))
    );
  }

  signup(request: SignupRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiBase}/auth/signup`, request, {
      withCredentials: true
    }).pipe(
      tap(response => this.setCurrentUser(response))
    );
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/auth/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/auth/reset-password`, { token, newPassword });
  }

  refreshSession(): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/auth/refresh`, {}, {
      withCredentials: true
    });
  }

  logout(): void {
    this.http.post(`${this.apiBase}/auth/logout`, {}, { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe(() => this.clearLocalSession());
  }

  getToken(): string | null {
    return null;
  }

  getCurrentUser(): User | null {
    return this.currentUserSignal();
  }

  getOrganizationId(): string | null {
    return this.organizationId();
  }

  hasRole(role: Role | string): boolean {
    const user = this.currentUserSignal();
    return user?.roles?.includes(role) ?? false;
  }

  hasAnyRole(...roles: Role[]): boolean {
    const user = this.currentUserSignal();
    if (!user?.roles) return false;
    return roles.some(role => user.roles.includes(role));
  }

  clearLocalSession(): void {
    sessionStorage.removeItem(STORAGE_KEYS.USER);
    sessionStorage.removeItem(STORAGE_KEYS.ORGANIZATION_ID);
    this.currentUserSignal.set(null);
    this.entitlementService.clear();
    this.router.navigate(['/login']);
  }

  private setCurrentUser(response: AuthResponse): void {
    const user: User = {
      id: response.userId,
      email: response.email,
      firstName: response.firstName,
      lastName: response.lastName,
      organizationId: response.organizationId,
      roles: response.roles,
      active: true
    };
    sessionStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    sessionStorage.setItem(STORAGE_KEYS.ORGANIZATION_ID, response.organizationId);
    this.currentUserSignal.set(user);
    this.entitlementService.load();
  }

  private loadUserFromStorage(): void {
    const userStr = sessionStorage.getItem(STORAGE_KEYS.USER);
    if (userStr) {
      try {
        const user = JSON.parse(userStr) as User;
        this.currentUserSignal.set(user);
      } catch (error) {
        console.error('Failed to load user from storage', error);
        this.clearLocalSession();
      }
    }
  }
}
