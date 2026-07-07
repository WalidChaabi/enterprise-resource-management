import { ChangeDetectionStrategy, Component, DestroyRef, EventEmitter, Input, Output, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/services/auth.service';
import { EntitlementService } from '../../core/services/entitlement.service';
import { ReportingService } from '../../core/services/reporting.service';
import { Dashboard } from '../../core/models/dashboard.model';
import { AlertType } from '../../core/models/alert.model';
import { ROLES } from '../../core/utils/constants';
import { NavItem, NavItemIndicator, SIDEBAR_NAV_ITEMS, SIDEBAR_NAV_SECTIONS } from './sidebar-nav.config';
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
  private readonly entitlementService = inject(EntitlementService);
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

  private readonly canApproveTimeEntries = computed(() =>
    this.authService.hasAnyRole(ROLES.PROJECT_MANAGER, ROLES.ORG_ADMIN, ROLES.HR_ADMIN),
  );

  private readonly canApproveLeaveRequests = computed(() =>
    this.authService.hasAnyRole(ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER),
  );

  private readonly canViewProjectAlerts = computed(() =>
    this.authService.hasAnyRole(ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER),
  );

  readonly visibleSections = computed(() => {
    const user = this.authService.currentUser();
    if (!user) {
      return [];
    }

    const visibleItems = SIDEBAR_NAV_ITEMS.filter((item) => {
      if (item.requiredEntitlements?.length) {
        const hasEntitlement = item.requiredEntitlements.some((e) => this.entitlementService.has(e));
        if (!hasEntitlement) {
          return false;
        }
      }
      if (!item.requiredRoles) {
        return true;
      }
      return this.authService.hasAnyRole(...item.requiredRoles);
    });

    const dashboard = this.dashboardData();
    const enrichedItems = visibleItems.map((item) => {
      const indicator = dashboard ? this.resolveIndicator(item, dashboard) : undefined;
      return indicator ? { ...item, indicator } : item;
    });

    return SIDEBAR_NAV_SECTIONS.map((section) => ({
      ...section,
      items: enrichedItems.filter((item) => item.section === section.id),
    })).filter((section) => section.items.length > 0);
  });

  constructor() {
    this.reportingService
      .getDashboard()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => this.dashboardData.set(data),
        error: () => this.dashboardData.set(null),
      });
  }

  private resolveIndicator(item: NavItem, dashboard: Dashboard): NavItemIndicator | undefined {
    switch (item.route[0]) {
      case '/time-entries':
        if (this.canApproveTimeEntries() && dashboard.pendingTimeEntries > 0) {
          return {
            kind: 'approval',
            count: dashboard.pendingTimeEntries,
            priority: 'medium',
            tooltipKey: 'nav.indicators.pendingTimeEntries',
          };
        }
        return undefined;
      case '/leave-requests':
        if (this.canApproveLeaveRequests() && dashboard.pendingLeaveRequests > 0) {
          return {
            kind: 'approval',
            count: dashboard.pendingLeaveRequests,
            priority: 'high',
            tooltipKey: 'nav.indicators.pendingLeaveRequests',
          };
        }
        return undefined;
      case '/projects': {
        if (!this.canViewProjectAlerts()) {
          return undefined;
        }
        const projectAlerts =
          dashboard.alerts?.filter(
            (alert) => alert.type === AlertType.PROJECT_OVER_BUDGET || alert.type === AlertType.PROJECT_AT_RISK,
          ) ?? [];
        if (projectAlerts.length > 0) {
          return {
            kind: 'alert',
            count: projectAlerts.length,
            priority: 'medium',
            tooltipKey: 'nav.indicators.projectAlerts',
          };
        }
        return undefined;
      }
      default:
        return undefined;
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
