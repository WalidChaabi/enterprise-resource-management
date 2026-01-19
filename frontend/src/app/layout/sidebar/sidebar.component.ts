import { Component, EventEmitter, Input, Output, inject, signal, computed, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ReportingService } from '../../core/services/reporting.service';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
import { BadgeComponent } from '../../shared/components/ui/badge/badge.component';
import { ROLES, type Role } from '../../core/utils/constants';

/**
 * Navigation item interface for type safety and extensibility
 * Supports role-based visibility and contextual indicators
 */
interface NavItem {
  label: string;
  route: string[];
  icon: string; // SVG path
  section: 'core' | 'people' | 'insights';
  requiredRoles?: Role[]; // If undefined, visible to all authenticated users
  indicator?: {
    count: number;
    priority: 'high' | 'medium' | 'low';
  };
}

/**
 * Section configuration for logical grouping
 */
interface NavSection {
  id: 'core' | 'people' | 'insights';
  label: string;
  items: NavItem[];
}

/**
 * Contextual create action configuration
 * Adapts based on current route to provide relevant creation options
 */
interface CreateAction {
  label: string;
  route: string[];
  icon: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslatePipe, BadgeComponent],
  template: `
    <aside
      class="flex flex-col bg-background border-r transition-all duration-300"
      [ngClass]="{
        'w-16': isCollapsed() && !isMobile,
        'w-64': !isCollapsed() || isMobile,
        'h-full': isMobile,
        'sticky top-0 h-screen': !isMobile,
        'px-2': isCollapsed() && !isMobile,
        'px-4': !isCollapsed() || isMobile,
        'py-4': isCollapsed() && !isMobile,
        'py-6': !isCollapsed() || isMobile
      }">
      
      <!-- Logo Section -->
      <div class="mb-6 flex items-center" [ngClass]="{'justify-center': isCollapsed() && !isMobile, 'justify-start': !isCollapsed() || isMobile}">
        @if (isCollapsed() && !isMobile) {
          <a 
            routerLink="/dashboard" 
            class="flex items-center justify-center transition-opacity duration-200 hover:opacity-80" 
            (click)="handleNavigate()"
            [attr.aria-label]="'app.logo' | translate">
            <img
              [src]="theme === 'dark' ? 'assets/logos/logo-symbol-white.svg' : 'assets/logos/logo-symbol-black.svg'"
              alt="{{ 'app.logo' | translate }}"
              class="h-10 w-10" />
          </a>
        } @else {
          <a routerLink="/dashboard" class="flex items-center transition-opacity duration-200 hover:opacity-80" (click)="handleNavigate()">
            <img
              [src]="theme === 'dark' ? 'assets/logos/logo-full-white.svg' : 'assets/logos/logo-full-black.svg'"
              alt="{{ 'app.logo' | translate }}"
              class="h-6 w-auto" />
          </a>
        }
      </div>

      <!-- Contextual Create Action -->
      <!-- Enterprise pattern: Single, elegant entry point that adapts to current context -->
      @if (currentCreateAction() && !isCollapsed()) {
        <div class="mb-4">
          <a
            [routerLink]="currentCreateAction()!.route"
            class="flex w-full items-center justify-center gap-2 rounded-lg border border-primary/20 bg-primary/5 px-3 py-2 text-sm font-medium text-primary transition-all duration-150 hover:bg-primary/10 hover:border-primary/30 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
            (click)="handleNavigate()">
            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            <span>{{ currentCreateAction()!.label }}</span>
          </a>
        </div>
      }

      <!-- Navigation Sections -->
      <!-- Grouped by logical business domains for better scanability -->
      <nav class="flex flex-1 flex-col gap-6 overflow-y-auto">
        @for (section of visibleSections(); track section.id) {
          <div class="space-y-1">
            <!-- Section Label (hidden when collapsed) -->
            @if ((!isCollapsed() || isMobile) && section.items.length > 0) {
              <div class="px-3 pb-1">
                <h3 class="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  {{ section.label }}
                </h3>
              </div>
            }

            <!-- Navigation Items -->
            @for (item of section.items; track item.route.join('/')) {
              <a
                [routerLink]="item.route"
                (click)="handleNavigate()"
                routerLinkActive="bg-primary text-primary-foreground"
                [routerLinkActiveOptions]="item.route[0] === '/dashboard' ? {exact: true} : {exact: false}"
                class="group relative flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                [ngClass]="{
                  'justify-center': isCollapsed() && !isMobile,
                  'justify-start': !isCollapsed() || isMobile
                }"
                [attr.aria-label]="isCollapsed() && !isMobile ? (item.label | translate) : undefined"
                [title]="isCollapsed() && !isMobile ? (item.label | translate) : undefined">
                
                <!-- Icon -->
                <svg class="h-5 w-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path [attr.d]="item.icon" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" />
                </svg>

                <!-- Label (hidden when collapsed) -->
                @if (!isCollapsed() || isMobile) {
                  <span class="flex-1 truncate">{{ item.label | translate }}</span>
                }

                <!-- Contextual Indicator -->
                <!-- Subtle, non-dominant signal for business-relevant information -->
                @if (item.indicator && item.indicator.count > 0 && (!isCollapsed() || isMobile)) {
                  <span 
                    class="flex-shrink-0 text-xs font-medium px-1.5 py-0.5 rounded"
                    [ngClass]="{
                      'bg-destructive/10 text-destructive border border-destructive/20': item.indicator.priority === 'high',
                      'bg-yellow-500/10 text-yellow-600 dark:text-yellow-500 border border-yellow-500/20': item.indicator.priority === 'medium',
                      'bg-muted text-muted-foreground border border-border': item.indicator.priority === 'low'
                    }">
                    {{ item.indicator.count > 99 ? '99+' : item.indicator.count }}
                  </span>
                }

                <!-- Collapsed Indicator Dot -->
                @if (item.indicator && item.indicator.count > 0 && isCollapsed() && !isMobile) {
                  <span 
                    class="absolute right-2 top-1/2 -translate-y-1/2 h-2 w-2 rounded-full"
                    [ngClass]="{
                      'bg-destructive': item.indicator.priority === 'high',
                      'bg-yellow-500': item.indicator.priority === 'medium',
                      'bg-muted-foreground/60': item.indicator.priority === 'low'
                    }"
                    [attr.aria-label]="item.indicator.count + ' ' + (item.label | translate)">
                  </span>
                }
              </a>
            }
          </div>
        }
      </nav>

      <!-- Bottom Actions -->
      <div class="mt-auto space-y-2 border-t border-border pt-4">
        @if (!isMobile) {
          <button
            type="button"
            class="flex w-full items-center justify-center gap-3 rounded-lg p-2 text-muted-foreground transition-colors duration-150 hover:bg-accent hover:text-accent-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
            (click)="toggleCollapse()"
            [attr.aria-label]="isCollapsed() ? 'Expand sidebar' : 'Collapse sidebar'"
            [title]="isCollapsed() ? 'Expand sidebar' : 'Collapse sidebar'">
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              @if (isCollapsed()) {
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              } @else {
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              }
            </svg>
            @if (!isCollapsed()) {
              <span class="text-sm">{{ isCollapsed() ? 'Expand' : 'Collapse' }}</span>
            }
          </button>
        }
        <button
          type="button"
          class="flex w-full items-center justify-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-destructive transition-all duration-150 hover:bg-destructive/20 hover:text-destructive active:bg-destructive/50 active:text-destructive-foreground active:scale-[0.98] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
          (click)="logout()"
          [attr.aria-label]="'auth.logout' | translate"
          [title]="isCollapsed() && !isMobile ? ('auth.logout' | translate) : undefined">
          <svg class="h-5 w-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          @if (!isCollapsed() || isMobile) {
            <span>{{ 'auth.logout' | translate }}</span>
          }
        </button>
      </div>
    </aside>
  `,
})
export class SidebarComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly reportingService = inject(ReportingService);
  readonly router = inject(Router);

  @Input({ required: true }) theme!: 'dark' | 'light';
  @Input() isMobile = false;
  @Output() navigated = new EventEmitter<void>();

  readonly isCollapsed = signal(false);
  readonly currentRoute = signal<string>('');
  readonly dashboardData = signal<any>(null);

  private routeSubscription?: Subscription;
  private dashboardSubscription?: Subscription;

  // Feature flag: Enable/disable contextual indicators
  // In production, this would come from a feature flag service
  private readonly ENABLE_INDICATORS = true;

  /**
   * All navigation items with their configuration
   * Role-based visibility is handled in visibleSections computed
   */
  private readonly allNavItems: NavItem[] = [
    {
      label: 'nav.dashboard',
      route: ['/dashboard'],
      icon: 'M3 10.5L12 3l9 7.5V21a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1v-10.5z',
      section: 'core',
    },
    {
      label: 'nav.projects',
      route: ['/projects'],
      icon: 'M3 7a2 2 0 0 1 2-2h5l2 2h7a2 2 0 0 1 2 2v7a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7z',
      section: 'core',
      requiredRoles: [ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER],
    },
    {
      label: 'nav.employees',
      route: ['/employees'],
      icon: 'M16 11a4 4 0 1 1-8 0 4 4 0 0 1 8 0zM4 20a6 6 0 0 1 16 0',
      section: 'people',
      requiredRoles: [ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER],
    },
    {
      label: 'nav.departments',
      route: ['/departments'],
      icon: 'M4 4h7v7H4V4zm9 0h7v7h-7V4zM4 13h7v7H4v-7zm9 0h7v7h-7v-7z',
      section: 'people',
      requiredRoles: [ROLES.HR_ADMIN, ROLES.ORG_ADMIN],
    },
    {
      label: 'nav.timeEntries',
      route: ['/time-entries'],
      icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
      section: 'core',
      requiredRoles: [ROLES.EMPLOYEE, ROLES.PROJECT_MANAGER, ROLES.ORG_ADMIN],
    },
    {
      label: 'nav.leaveRequests',
      route: ['/leave-requests'],
      icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z',
      section: 'people',
    },
    {
      label: 'nav.reports',
      route: ['/reports'],
      icon: 'M4 4h16v16H4V4zm4 10v2m4-6v6m4-4v4',
      section: 'insights',
      requiredRoles: [ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER, ROLES.HR_ADMIN],
    },
  ];

  /**
   * Navigation sections configuration
   * Groups items logically for better information hierarchy
   */
  private readonly navSections: NavSection[] = [
    {
      id: 'core',
      label: 'nav.sections.core',
      items: [],
    },
    {
      id: 'people',
      label: 'nav.sections.people',
      items: [],
    },
    {
      id: 'insights',
      label: 'nav.sections.insights',
      items: [],
    },
  ];

  /**
   * Computed: Filter navigation items based on user roles
   * Respects ERP security model - unauthorized sections are completely hidden
   */
  readonly visibleSections = computed(() => {
    const user = this.authService.currentUser();
    if (!user) return [];

    // Filter items by role
    const visibleItems = this.allNavItems.filter(item => {
      if (!item.requiredRoles) return true; // Public to all authenticated users
      return this.authService.hasAnyRole(...item.requiredRoles);
    });

    // Enrich items with contextual indicators if enabled
    const enrichedItems = visibleItems.map(item => {
      if (!this.ENABLE_INDICATORS || !this.dashboardData()) {
        return item;
      }

      const dashboard = this.dashboardData();
      let indicator: NavItem['indicator'] | undefined;

      // Map dashboard data to navigation indicators
      // Priority logic: high = critical, medium = attention needed, low = informational
      switch (item.route[0]) {
        case '/time-entries':
          if (dashboard.pendingTimeEntries > 0) {
            indicator = {
              count: dashboard.pendingTimeEntries,
              priority: 'medium', // Pending approvals need attention but aren't critical
            };
          }
          break;
        case '/leave-requests':
          if (dashboard.pendingLeaveRequests > 0) {
            indicator = {
              count: dashboard.pendingLeaveRequests,
              priority: 'high', // Leave requests often have deadlines
            };
          }
          break;
        case '/projects':
          // Could add active projects count or alerts count here
          const projectAlerts = dashboard.alerts?.filter((a: any) => a.type === 'PROJECT_OVERBUDGET' || a.type === 'PROJECT_DELAYED') || [];
          if (projectAlerts.length > 0) {
            indicator = {
              count: projectAlerts.length,
              priority: 'high',
            };
          }
          break;
      }

      return { ...item, indicator };
    });

    // Group items into sections
    return this.navSections.map(section => ({
      ...section,
      items: enrichedItems.filter(item => item.section === section.id),
    })).filter(section => section.items.length > 0); // Only show sections with visible items
  });

  /**
   * Computed: Contextual create action based on current route
   * Provides single, elegant entry point that adapts to user context
   */
  readonly currentCreateAction = computed<CreateAction | null>(() => {
    const route = this.currentRoute();
    if (!route) return null;

    // Map routes to appropriate create actions
    // Only show if user has permission to create in that section
    const createActions: Record<string, CreateAction> = {
      '/projects': {
        label: 'projects.newProject',
        route: ['/projects', 'new'],
        icon: 'M12 4v16m8-8H4',
      },
      '/employees': {
        label: 'employees.newEmployee',
        route: ['/employees', 'new'],
        icon: 'M12 4v16m8-8H4',
      },
      '/time-entries': {
        label: 'timeEntries.newTimeEntry',
        route: ['/time-entries', 'new'],
        icon: 'M12 4v16m8-8H4',
      },
      '/leave-requests': {
        label: 'leaveRequests.newLeaveRequest',
        route: ['/leave-requests', 'new'],
        icon: 'M12 4v16m8-8H4',
      },
    };

    const action = createActions[route];
    if (!action) return null;

    // Verify user has permission to create
    const navItem = this.allNavItems.find(item => item.route[0] === route);
    if (navItem?.requiredRoles && !this.authService.hasAnyRole(...navItem.requiredRoles)) {
      return null;
    }

    return action;
  });

  ngOnInit(): void {
    // Track current route for contextual actions
    this.currentRoute.set(this.router.url.split('?')[0]);
    this.routeSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.currentRoute.set(event.urlAfterRedirects.split('?')[0]);
      });

    // Load dashboard data for contextual indicators
    // In production, this could be cached or loaded on-demand
    if (this.ENABLE_INDICATORS) {
      this.dashboardSubscription = this.reportingService.getDashboard().subscribe({
        next: (data) => this.dashboardData.set(data),
        error: () => {
          // Silently fail - indicators are non-critical
          this.dashboardData.set(null);
        },
      });
    }
  }

  ngOnDestroy(): void {
    this.routeSubscription?.unsubscribe();
    this.dashboardSubscription?.unsubscribe();
  }

  toggleCollapse(): void {
    this.isCollapsed.update(value => !value);
  }

  handleNavigate(): void {
    if (this.isMobile) {
      this.navigated.emit();
    }
  }

  logout(): void {
    this.authService.logout();
    if (this.isMobile) {
      this.navigated.emit();
    }
  }
}
