import { ChangeDetectionStrategy, Component, DestroyRef, EventEmitter, Input, Output, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/services/auth.service';
import { ReportingService } from '../../core/services/reporting.service';
import { Dashboard } from '../../core/models/dashboard.model';
import { AlertType } from '../../core/models/alert.model';
import { NavItem, SIDEBAR_NAV_ITEMS, SIDEBAR_NAV_SECTIONS } from './sidebar-nav.config';
import { SidebarNavComponent } from './sidebar-nav.component';

@Component({
  selector: 'app-sidebar',
  imports: [SidebarNavComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-sidebar-nav
      [theme]="theme"
      [sections]="visibleSections()"
      [userName]="userFullName()"
      [userEmail]="userEmail()"
      (navigated)="handleNavigate()"
      (logout)="onLogout()" />
  `,
})
export class SidebarComponent {
  private readonly authService = inject(AuthService);
  private readonly reportingService = inject(ReportingService);
  private readonly destroyRef = inject(DestroyRef);

  @Input({ required: true }) theme!: 'dark' | 'light';
  @Output() navigated = new EventEmitter<void>();

  readonly dashboardData = signal<Dashboard | null>(null);
  readonly userFullName = computed(() => {
    const user = this.authService.currentUser();
    return user ? `${user.firstName} ${user.lastName}` : '';
  });
  readonly userEmail = computed(() => this.authService.currentUser()?.email ?? '');

  private readonly enableIndicators = true;

  readonly visibleSections = computed(() => {
    const user = this.authService.currentUser();
    if (!user) {
      return [];
    }

    const visibleItems = SIDEBAR_NAV_ITEMS.filter((item) => {
      if (!item.requiredRoles) {
        return true;
      }
      return this.authService.hasAnyRole(...item.requiredRoles);
    });

    const enrichedItems = visibleItems.map((item) => {
      if (!this.enableIndicators || !this.dashboardData()) {
        return item;
      }

      const dashboard = this.dashboardData()!;
      let indicator: NavItem['indicator'];

      switch (item.route[0]) {
        case '/time-entries':
          if (dashboard.pendingTimeEntries > 0) {
            indicator = { count: dashboard.pendingTimeEntries, priority: 'medium' };
          }
          break;
        case '/leave-requests':
          if (dashboard.pendingLeaveRequests > 0) {
            indicator = { count: dashboard.pendingLeaveRequests, priority: 'high' };
          }
          break;
        case '/projects': {
          const projectAlerts = dashboard.alerts?.filter(
            (alert) => alert.type === AlertType.PROJECT_OVER_BUDGET || alert.type === AlertType.PROJECT_AT_RISK
          ) ?? [];
          if (projectAlerts.length > 0) {
            indicator = { count: projectAlerts.length, priority: 'high' };
          }
          break;
        }
      }

      return { ...item, indicator };
    });

    return SIDEBAR_NAV_SECTIONS.map((section) => ({
      ...section,
      items: enrichedItems.filter((item) => item.section === section.id),
    })).filter((section) => section.items.length > 0);
  });

  constructor() {
    if (this.enableIndicators) {
      this.reportingService
        .getDashboard()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (data) => this.dashboardData.set(data),
          error: () => this.dashboardData.set(null),
        });
    }
  }

  handleNavigate(): void {
    this.navigated.emit();
  }

  onLogout(): void {
    this.authService.logout();
    this.navigated.emit();
  }
}
