import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AuthService } from '../../core/services/auth.service';
import { ReportingService } from '../../core/services/reporting.service';
import { Dashboard } from '../../core/models/dashboard.model';
import { Alert, AlertType, AlertSeverity } from '../../core/models/alert.model';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent } from '../../shared/components/ui/card/card.component';
import { ButtonComponent } from '../../shared/components/ui/button/button.component';
import { SkeletonComponent } from '../../shared/components/ui/skeleton/skeleton.component';
import { ChartComponent } from '../../shared/components/chart/chart.component';
import { ChartConfiguration } from 'chart.js';

@Component({
    selector: 'app-dashboard',
    imports: [CommonModule, RouterModule, TranslatePipe, CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent, ButtonComponent, SkeletonComponent, ChartComponent],
    templateUrl: './dashboard.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly reportingService = inject(ReportingService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly destroyRef = inject(DestroyRef);

  readonly currentUser = this.authService.currentUser;
  readonly loading = signal<boolean>(false);
  readonly dashboard = signal<Dashboard | null>(null);
  readonly error = signal<string | null>(null);
  readonly AlertType = AlertType;

  private readonly _weeklyHoursChartData: ChartConfiguration['data'] = {
    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    datasets: [{
      label: 'Hours',
      data: [8, 7.5, 8.5, 7, 8, 4, 0],
      backgroundColor: 'hsl(var(--primary))',
      borderColor: 'hsl(var(--primary))',
      borderWidth: 1
    }]
  };

  readonly weeklyHoursChartData = computed<ChartConfiguration['data']>(() => this._weeklyHoursChartData);

  readonly projectStatusChartData = computed<ChartConfiguration['data']>(() => {
    const dashboard = this.dashboard();
    const activeProjects = dashboard?.activeProjects || 0;

    return {
      labels: ['Active', 'Planning', 'On Hold', 'Completed'],
      datasets: [{
        data: [activeProjects, 5, 2, 10],
        backgroundColor: [
          'hsl(var(--chart-1))',
          'hsl(var(--chart-2))',
          'hsl(var(--chart-3))',
          'hsl(var(--chart-4))'
        ]
      }]
    };
  });

  private readonly _chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          usePointStyle: true,
          padding: 15
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: { color: 'hsl(var(--border))' }
      },
      x: {
        grid: { color: 'hsl(var(--border))' }
      }
    }
  };

  readonly chartOptions = computed<ChartConfiguration['options']>(() => this._chartOptions);

  readonly nextActions = computed(() => {
    const dashboard = this.dashboard();
    if (!dashboard) return [];

    const actions: Array<{
      priority: 'high' | 'medium' | 'low';
      label: string;
      description: string;
      route: string[];
      count?: number;
    }> = [];

    const highAlerts = dashboard.alerts?.filter(a => a.severity === 'HIGH') || [];
    if (highAlerts.length > 0) {
      actions.push({
        priority: 'high',
        label: `Review ${highAlerts.length} critical ${highAlerts.length === 1 ? 'alert' : 'alerts'}`,
        description: 'Immediate attention required',
        route: ['/dashboard'],
        count: highAlerts.length
      });
    }

    if (dashboard.pendingLeaveRequests > 0) {
      actions.push({
        priority: 'high',
        label: `Review ${dashboard.pendingLeaveRequests} pending leave ${dashboard.pendingLeaveRequests === 1 ? 'request' : 'requests'}`,
        description: 'Awaiting your approval',
        route: ['/leave-requests'],
        count: dashboard.pendingLeaveRequests
      });
    }

    if (dashboard.pendingTimeEntries > 0) {
      actions.push({
        priority: 'medium',
        label: `Approve ${dashboard.pendingTimeEntries} time ${dashboard.pendingTimeEntries === 1 ? 'entry' : 'entries'}`,
        description: 'Pending approval',
        route: ['/time-entries'],
        count: dashboard.pendingTimeEntries
      });
    }

    const mediumAlerts = dashboard.alerts?.filter(a => a.severity === 'MEDIUM') || [];
    if (mediumAlerts.length > 0) {
      actions.push({
        priority: 'medium',
        label: `Review ${mediumAlerts.length} ${mediumAlerts.length === 1 ? 'alert' : 'alerts'}`,
        description: 'Needs attention soon',
        route: ['/dashboard'],
        count: mediumAlerts.length
      });
    }

    if (dashboard.activeProjects === 0 && dashboard.totalProjects === 0) {
      actions.push({
        priority: 'low',
        label: 'Create your first project',
        description: 'Get started by setting up a project',
        route: ['/projects']
      });
    } else if (dashboard.activeEmployees === 0 && dashboard.totalEmployees === 0) {
      actions.push({
        priority: 'low',
        label: 'Add employees to your organization',
        description: 'Start building your team',
        route: ['/employees']
      });
    } else if (dashboard.totalHoursThisWeek === 0 && dashboard.totalHoursThisMonth === 0) {
      actions.push({
        priority: 'low',
        label: 'Start tracking time',
        description: 'Record your first time entry',
        route: ['/time-entries']
      });
    }

    return actions.slice(0, 5);
  });

  readonly isLowDataState = computed(() => {
    const dashboard = this.dashboard();
    if (!dashboard) return false;

    return (
      dashboard.totalProjects === 0 ||
      dashboard.totalEmployees === 0 ||
      (dashboard.totalHoursThisWeek === 0 && dashboard.totalHoursThisMonth === 0)
    );
  });

  readonly recentActivity = computed(() => {
    const dashboard = this.dashboard();
    if (!dashboard) return [];

    const activities: Array<{
      type: 'approval' | 'alert' | 'milestone' | 'update';
      message: string;
      route?: string[];
      timestamp?: string;
    }> = [];

    if (dashboard.approvedLeaveRequests > 0) {
      activities.push({
        type: 'approval',
        message: `${dashboard.approvedLeaveRequests} leave ${dashboard.approvedLeaveRequests === 1 ? 'request' : 'requests'} approved`,
        route: ['/leave-requests']
      });
    }

    const recentAlerts = dashboard.alerts?.slice(0, 2) || [];
    recentAlerts.forEach(alert => {
      activities.push({
        type: 'alert',
        message: alert.message,
        route: alert.projectId ? ['/projects', alert.projectId] : undefined
      });
    });

    if (dashboard.completedProjects > 0) {
      activities.push({
        type: 'milestone',
        message: `${dashboard.completedProjects} ${dashboard.completedProjects === 1 ? 'project' : 'projects'} completed`,
        route: ['/projects']
      });
    }

    if (dashboard.approvedTimeEntries > 0) {
      activities.push({
        type: 'update',
        message: `${dashboard.approvedTimeEntries} time ${dashboard.approvedTimeEntries === 1 ? 'entry' : 'entries'} approved`,
        route: ['/time-entries']
      });
    }

    return activities.slice(0, 4);
  });

  private readonly alertIconCache = new Map<AlertType, SafeHtml>();

  constructor() {
    this.loadDashboard();
  }

  private loadDashboard(): void {
    this.loading.set(true);
    this.error.set(null);

    this.reportingService
      .getDashboard()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.dashboard.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set('Failed to load dashboard data');
          this.loading.set(false);
          console.error('Dashboard load error:', err);
        }
      });
  }

  getAlertsBySeverity(severity: AlertSeverity): Alert[] {
    const dashboard = this.dashboard();
    if (!dashboard || !dashboard.alerts) {
      return [];
    }
    return dashboard.alerts.filter(alert => alert.severity === severity);
  }

  getAlertIcon(type: AlertType): SafeHtml {
    if (this.alertIconCache.has(type)) {
      return this.alertIconCache.get(type)!;
    }

    const icons: Record<string, string> = {
      [AlertType.PROJECT_WITHOUT_RESOURCES]: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/></svg>`,
      [AlertType.EMPLOYEE_OVERALLOCATED]: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/></svg>`,
      [AlertType.PROJECT_AT_RISK]: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/></svg>`,
      [AlertType.PROJECT_UNDERSTAFFED]: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 17 13.5 8.5 8.5 13.5 2 7"/><polyline points="16 17 22 17 22 11"/></svg>`,
      [AlertType.PROJECT_DEADLINE_APPROACHING]: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`,
      [AlertType.PROJECT_OVER_BUDGET]: `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" x2="12" y1="2" y2="22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
    };
    const defaultIcon = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="16" y2="12"/><line x1="12" x2="12.01" y1="8" y2="8"/></svg>`;
    const svg = icons[type] || defaultIcon;
    const safeHtml = this.sanitizer.bypassSecurityTrustHtml(svg);
    this.alertIconCache.set(type, safeHtml);
    return safeHtml;
  }

  getSeverityColor(severity: AlertSeverity): string {
    switch (severity) {
      case 'HIGH':
        return 'text-red-400 bg-red-950/50 border-red-800 dark:text-red-300 dark:bg-red-900/20 dark:border-red-700';
      case 'MEDIUM':
        return 'text-yellow-400 bg-yellow-950/50 border-yellow-800 dark:text-yellow-300 dark:bg-yellow-900/20 dark:border-yellow-700';
      case 'LOW':
        return 'text-blue-400 bg-blue-950/50 border-blue-800 dark:text-blue-300 dark:bg-blue-900/20 dark:border-blue-700';
      default:
        return 'text-gray-400 bg-gray-950/50 border-gray-800 dark:text-gray-300 dark:bg-gray-900/20 dark:border-gray-700';
    }
  }
}
