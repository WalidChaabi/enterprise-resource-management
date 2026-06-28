import { ChangeDetectionStrategy, Component, DestroyRef, EventEmitter, Input, Output, computed, inject, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/services/auth.service';
import { ReportingService } from '../../core/services/reporting.service';
import { Dashboard } from '../../core/models/dashboard.model';
import { AlertType } from '../../core/models/alert.model';
import {
  CreateAction,
  NavItem,
  SIDEBAR_CREATE_ACTIONS,
  SIDEBAR_NAV_ITEMS,
  SIDEBAR_NAV_SECTIONS,
} from './sidebar-nav.config';
import { SidebarNavComponent } from './sidebar-nav.component';

@Component({
    selector: 'app-sidebar',
    imports: [SidebarNavComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
    <app-sidebar-nav
      [theme]="theme"
      [isMobile]="isMobile"
      [isCollapsed]="isCollapsed()"
      [sections]="visibleSections()"
      [currentCreateAction]="currentCreateAction()"
      (navigated)="handleNavigate()"
      (toggleCollapse)="toggleCollapse()"
      (logout)="onLogout()" />
  `
})
export class SidebarComponent {
  private readonly authService = inject(AuthService);
  private readonly reportingService = inject(ReportingService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  @Input({ required: true }) theme!: 'dark' | 'light';
  @Input() isMobile = false;
  @Output() navigated = new EventEmitter<void>();

  readonly isCollapsed = signal(false);
  readonly currentRoute = signal('');
  readonly dashboardData = signal<Dashboard | null>(null);

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

  readonly currentCreateAction = computed<CreateAction | null>(() => {
    const route = this.currentRoute();
    if (!route) {
      return null;
    }

    const action = SIDEBAR_CREATE_ACTIONS[route];
    if (!action) {
      return null;
    }

    const navItem = SIDEBAR_NAV_ITEMS.find((item) => item.route[0] === route);
    if (navItem?.requiredRoles && !this.authService.hasAnyRole(...navItem.requiredRoles)) {
      return null;
    }

    return action;
  });

  constructor() {
    this.currentRoute.set(this.router.url.split('?')[0]);

    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((event) => {
        this.currentRoute.set(event.urlAfterRedirects.split('?')[0]);
      });

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

  toggleCollapse(): void {
    this.isCollapsed.update((value) => !value);
  }

  handleNavigate(): void {
    if (this.isMobile) {
      this.navigated.emit();
    }
  }

  onLogout(): void {
    this.authService.logout();
    if (this.isMobile) {
      this.navigated.emit();
    }
  }
}
