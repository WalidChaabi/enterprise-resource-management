import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SettingsService } from '../../../core/services/settings.service';
import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/models/user-role.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { SkeletonComponent } from '../../../shared/components/ui/skeleton/skeleton.component';

@Component({
  selector: 'app-roles-settings',
  standalone: true,
  imports: [
    CommonModule,
    TranslatePipe,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardContentComponent,
    BadgeComponent,
    SkeletonComponent
  ],
  template: `
    <div class="mt-6 space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-xl font-semibold text-foreground">Roles & Permissions</h2>
          <p class="text-sm text-muted-foreground">
            @if (canManageRoles()) {
              Manage roles and permissions for your organization.
            } @else {
              View roles available in your organization. Role management is restricted to administrators.
            }
          </p>
        </div>
      </div>

      @if (error()) {
        <div class="flex items-start gap-3 rounded-lg border border-destructive/20 bg-destructive/5 p-4">
          <svg class="h-5 w-5 flex-shrink-0 text-destructive mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-foreground">{{ error() }}</p>
          </div>
        </div>
      }

      @if (loading()) {
        <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          @for (i of [1,2,3]; track i) {
            <app-card>
              <app-card-content class="p-6">
                <app-skeleton [width]="'100%'" [height]="'h-6'" [className]="'mb-3'"></app-skeleton>
                <app-skeleton [width]="'80%'" [height]="'h-4'"></app-skeleton>
              </app-card-content>
            </app-card>
          }
        </div>
      } @else {
        @if (isEmptyState()) {
          <app-card>
            <app-card-content class="flex flex-col items-center justify-center py-12">
              <div class="rounded-full bg-muted p-3 mb-4">
                <svg class="h-6 w-6 text-muted-foreground" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
              <h3 class="text-base font-semibold text-foreground mb-1">No roles configured</h3>
              @if (canManageRoles()) {
                <p class="text-sm text-muted-foreground mb-6 max-w-sm text-center">Roles define access levels and permissions. Contact your system administrator to configure roles for your organization.</p>
              } @else {
                <p class="text-sm text-muted-foreground max-w-sm text-center">Roles are managed by organization administrators. Your current roles: <span class="font-medium text-foreground">{{ currentUserRoles().join(', ') || 'None assigned' }}</span></p>
              }
            </app-card-content>
          </app-card>
        } @else {
          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            @for (role of roles(); track role.id) {
              <app-card class="group hover:shadow-md transition-shadow duration-200">
                <app-card-content class="p-6">
                  <div class="flex items-start justify-between mb-4">
                    <div class="flex items-center gap-3">
                      <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary group-hover:bg-primary/20 transition-colors">
                        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                        </svg>
                      </div>
                      <div class="flex-1 min-w-0">
                        <h3 class="text-base font-semibold text-foreground truncate">{{ role.name }}</h3>
                        <app-badge variant="secondary" class="mt-1 text-xs">{{ role.code }}</app-badge>
                      </div>
                    </div>
                  </div>
                  @if (role.description) {
                    <p class="text-sm text-muted-foreground line-clamp-2 leading-relaxed mb-3">{{ role.description }}</p>
                  } @else {
                    <p class="text-sm text-muted-foreground italic mb-3">No description available</p>
                  }
                  <div class="mt-4 pt-4 border-t border-border">
                    <div class="flex items-center justify-between">
                      <div class="flex items-center gap-2 text-xs text-muted-foreground">
                        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                        </svg>
                        <span>Organization Role</span>
                      </div>
                      @if (currentUserRoles().includes(role.code)) {
                        <span class="text-xs font-medium px-2 py-0.5 rounded border border-primary/20 text-primary bg-primary/5">
                          Your role
                        </span>
                      }
                    </div>
                  </div>
                </app-card-content>
              </app-card>
            }
          </div>
        }
      }
    </div>
  `
})
export class RolesSettingsComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly error = signal('');
  readonly roles = signal<Role[]>([]);

  readonly canManageRoles = computed(() => {
    return this.authService.hasAnyRole(ROLES.ORG_ADMIN);
  });

  readonly currentUserRoles = computed(() => {
    return this.authService.currentUser()?.roles || [];
  });

  readonly isEmptyState = computed(() => {
    return !this.loading() && this.roles().length === 0;
  });

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.loading.set(true);
    this.error.set('');

    this.settingsService.getRoles().subscribe({
      next: (roles) => {
        this.roles.set(roles);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load roles');
        this.loading.set(false);
      }
    });
  }
}
