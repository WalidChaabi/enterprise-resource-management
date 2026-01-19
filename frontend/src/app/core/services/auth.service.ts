import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthRequest, AuthResponse, User } from '../models/auth.model';
import { STORAGE_KEYS, ROLES, type Role } from '../utils/constants';

/**
 * Authentication service
 * Manages user authentication state using Angular Signals
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/auth/login`;

  // Reactive state using Signals
  private readonly currentUserSignal = signal<User | null>(null);
  public readonly currentUser = this.currentUserSignal.asReadonly();
  
  public readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  public readonly organizationId = computed(() => this.currentUserSignal()?.organizationId ?? null);

  constructor() {
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
    this.currentUserSignal.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(STORAGE_KEYS.TOKEN);
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

  private setSession(response: AuthResponse): void {
    localStorage.setItem(STORAGE_KEYS.TOKEN, response.accessToken);
    localStorage.setItem(STORAGE_KEYS.ORGANIZATION_ID, response.organizationId);
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
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  private loadUserFromStorage(): void {
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    if (userStr) {
      try {
        const user = JSON.parse(userStr) as User;
        this.currentUserSignal.set(user);
      } catch (error) {
        console.error('Failed to load user from storage', error);
        this.logout();
      }
    }
  }
}
