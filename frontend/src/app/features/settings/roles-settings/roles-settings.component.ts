import { LucideAngularModule } from 'lucide-angular';
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

import {
  SettingsPageLayoutComponent,
} from '../components';

@Component({
    selector: 'app-roles-settings',
    imports: [
        CommonModule,
        TranslatePipe,
        LucideAngularModule,
        CardComponent,
        CardHeaderComponent,
        CardTitleComponent,
        CardContentComponent,
        BadgeComponent,
        SkeletonComponent,
        SettingsPageLayoutComponent,
    ],
    template: `
    <app-settings-page-layout [wide]="true">
      @if (error()) {
        <div class="flex items-start gap-3 rounded-lg border border-destructive/20 bg-destructive/5 p-4">
          <lucide-icon name="alert-circle" [size]="20" class="flex-shrink-0 text-destructive mt-0.5" />
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
                <lucide-icon name="users" [size]="24" class="text-muted-foreground" />
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
                      <div class="icon-container h-10 w-10">
                        <lucide-icon name="shield-check" [size]="20" />
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
                        <lucide-icon name="building-2" [size]="16" />
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
    </app-settings-page-layout>
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

  readonly subtitleKey = computed(() =>
    this.canManageRoles() ? 'settings.roles.subtitleAdmin' : 'settings.roles.subtitleView',
  );

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
