import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  AlertTriangle,
  ArrowRight,
  Calendar,
  CheckCircle2,
  Clock,
  DollarSign,
  FolderKanban,
  LucideIconData,
  LucideAngularModule,
  TrendingDown,
  Users,
  UserX,
} from 'lucide-angular';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { AuthService } from '../../core/services/auth.service';
import { ReportingService } from '../../core/services/reporting.service';
import { Dashboard } from '../../core/models/dashboard.model';
import { AlertType, AlertSeverity } from '../../core/models/alert.model';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
import {
  CardComponent,
  CardHeaderComponent,
  CardTitleComponent,
  CardDescriptionComponent,
  CardContentComponent,
  CardFooterComponent,
} from '../../shared/components/ui/card/card.component';
import {
  AlertComponent,
  AlertTitleComponent,
  AlertDescriptionComponent,
} from '../../shared/components/ui/alert/alert.component';
import { BadgeComponent, BadgeVariant } from '../../shared/components/ui/badge/badge.component';
import { ButtonComponent } from '../../shared/components/ui/button/button.component';
import { SkeletonComponent } from '../../shared/components/ui/skeleton/skeleton.component';
import { ChartComponent } from '../../shared/components/chart/chart.component';

type ActionPriority = 'high' | 'medium' | 'low';

interface DashboardAction {
  priority: ActionPriority;
  label: string;
  description: string;
  route: string[];
  count?: number;
}

interface MetricCard {
  title: string;
  value: string;
  hint: string;
  route: string[];
  icon: LucideIconData;
}

interface ActivityItem {
  type: 'approval' | 'alert' | 'milestone' | 'update';
  message: string;
  route?: string[];
}

@Component({
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    RouterModule,
    TranslatePipe,
    LucideAngularModule,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardDescriptionComponent,
    CardContentComponent,
    CardFooterComponent,
    AlertComponent,
    AlertTitleComponent,
    AlertDescriptionComponent,
    BadgeComponent,
    ButtonComponent,
    SkeletonComponent,
    ChartComponent,
  ],
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly reportingService = inject(ReportingService);
  private readonly destroyRef = inject(DestroyRef);

  readonly currentUser = this.authService.currentUser;
  readonly loading = signal(false);
  readonly dashboard = signal<Dashboard | null>(null);
  readonly error = signal<string | null>(null);

  readonly FolderKanban = FolderKanban;
  readonly Users = Users;
  readonly Clock = Clock;
  readonly Calendar = Calendar;
  readonly ArrowRight = ArrowRight;
  readonly CheckCircle2 = CheckCircle2;
  readonly AlertTriangle = AlertTriangle;

  private readonly alertIcons: Record<AlertType, LucideIconData> = {
    [AlertType.PROJECT_WITHOUT_RESOURCES]: UserX,
    [AlertType.EMPLOYEE_OVERALLOCATED]: Users,
    [AlertType.PROJECT_AT_RISK]: AlertTriangle,
    [AlertType.PROJECT_UNDERSTAFFED]: TrendingDown,
    [AlertType.PROJECT_DEADLINE_APPROACHING]: Clock,
    [AlertType.PROJECT_OVER_BUDGET]: DollarSign,
  };

  private readonly _weeklyHoursChartData: ChartConfiguration['data'] = {
    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    datasets: [
      {
        label: 'Hours',
        data: [8, 7.5, 8.5, 7, 8, 4, 0],
        backgroundColor: 'hsl(221, 97%, 65%)',
        borderColor: 'transparent',
        borderWidth: 0,
        borderRadius: 6,
        borderSkipped: false,
        barThickness: 28,
      },
    ],
  };

  readonly weeklyHoursChartData = computed<ChartConfiguration['data']>(() => this._weeklyHoursChartData);

  readonly projectStatusChartData = computed<ChartConfiguration['data']>(() => {
    const dashboard = this.dashboard();
    const activeProjects = dashboard?.activeProjects || 0;

    return {
      labels: ['Active', 'Planning', 'On Hold', 'Completed'],
      datasets: [
        {
          data: [activeProjects, 5, 2, 10],
          backgroundColor: [
            'hsl(221, 97%, 65%)',
            'hsl(219, 100%, 78%)',
            'hsl(38, 92%, 50%)',
            'hsl(215, 20%, 65%)',
          ],
          borderWidth: 0,
          spacing: 2,
        },
      ],
    };
  });

  private readonly _barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    plugins: { legend: { display: false } },
    scales: {
      y: {
        beginAtZero: true,
        border: { display: false },
        grid: { color: 'hsl(0 0% 100% / 0.04)', drawTicks: false },
        ticks: {
          color: 'hsl(215, 20%, 65%)',
          font: { size: 11 },
          padding: 8,
        },
      },
      x: {
        border: { display: false },
        grid: { display: false },
        ticks: {
          color: 'hsl(215, 20%, 65%)',
          font: { size: 11 },
          padding: 8,
        },
      },
    },
  };

  private readonly _doughnutChartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    cutout: '68%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          usePointStyle: true,
          pointStyle: 'circle',
          padding: 20,
          color: 'hsl(215, 20%, 65%)',
          font: { size: 12 },
          boxWidth: 8,
          boxHeight: 8,
        },
      },
    },
  };

  readonly barChartOptions = computed((): ChartConfiguration['options'] => this._barChartOptions);
  readonly doughnutChartOptions = computed(
    (): ChartConfiguration['options'] => this._doughnutChartOptions as ChartConfiguration['options']
  );

  readonly metricCards = computed<MetricCard[]>(() => {
    const d = this.dashboard();
    if (!d) return [];

    return [
      {
        title: 'Active Projects',
        value: String(d.activeProjects),
        hint:
          d.activeProjects === 0 && d.totalProjects === 0
            ? 'Create your first project'
            : d.activeProjects === 0
              ? `All ${d.totalProjects} projects are inactive`
              : `of ${d.totalProjects} total`,
        route: ['/projects'],
        icon: FolderKanban,
      },
      {
        title: 'Active Employees',
        value: String(d.activeEmployees),
        hint:
          d.activeEmployees === 0 && d.totalEmployees === 0
            ? 'Add employees to get started'
            : d.activeEmployees === 0
              ? `All ${d.totalEmployees} employees are inactive`
              : `of ${d.totalEmployees} total`,
        route: ['/employees'],
        icon: Users,
      },
      {
        title: 'Hours This Week',
        value: String(d.totalHoursThisWeek),
        hint:
          d.totalHoursThisWeek === 0 && d.totalHoursThisMonth === 0
            ? 'No time entries recorded yet'
            : `${d.totalHoursThisMonth}h this month`,
        route: ['/reports'],
        icon: Clock,
      },
      {
        title: 'Pending Leave',
        value: String(d.pendingLeaveRequests),
        hint: d.pendingLeaveRequests === 0 ? 'All requests processed' : 'Requires attention',
        route: ['/leave-requests'],
        icon: Calendar,
      },
    ];
  });

  readonly nextActions = computed<DashboardAction[]>(() => {
    const dashboard = this.dashboard();
    if (!dashboard) return [];

    const actions: DashboardAction[] = [];

    const highAlerts = dashboard.alerts?.filter((a) => a.severity === 'HIGH') || [];
    if (highAlerts.length > 0) {
      actions.push({
        priority: 'high',
        label: `Review ${highAlerts.length} critical ${highAlerts.length === 1 ? 'alert' : 'alerts'}`,
        description: 'Immediate attention required',
        route: ['/dashboard'],
        count: highAlerts.length,
      });
    }

    if (dashboard.pendingLeaveRequests > 0) {
      actions.push({
        priority: 'high',
        label: `Review ${dashboard.pendingLeaveRequests} pending leave ${dashboard.pendingLeaveRequests === 1 ? 'request' : 'requests'}`,
        description: 'Awaiting your approval',
        route: ['/leave-requests'],
        count: dashboard.pendingLeaveRequests,
      });
    }

    if (dashboard.pendingTimeEntries > 0) {
      actions.push({
        priority: 'medium',
        label: `Approve ${dashboard.pendingTimeEntries} time ${dashboard.pendingTimeEntries === 1 ? 'entry' : 'entries'}`,
        description: 'Pending approval',
        route: ['/time-entries'],
        count: dashboard.pendingTimeEntries,
      });
    }

    const mediumAlerts = dashboard.alerts?.filter((a) => a.severity === 'MEDIUM') || [];
    if (mediumAlerts.length > 0) {
      actions.push({
        priority: 'medium',
        label: `Review ${mediumAlerts.length} ${mediumAlerts.length === 1 ? 'alert' : 'alerts'}`,
        description: 'Needs attention soon',
        route: ['/dashboard'],
        count: mediumAlerts.length,
      });
    }

    if (dashboard.activeProjects === 0 && dashboard.totalProjects === 0) {
      actions.push({
        priority: 'low',
        label: 'Create your first project',
        description: 'Get started by setting up a project',
        route: ['/projects', 'new'],
      });
    } else if (dashboard.activeEmployees === 0 && dashboard.totalEmployees === 0) {
      actions.push({
        priority: 'low',
        label: 'Add employees to your organization',
        description: 'Start building your team',
        route: ['/employees', 'new'],
      });
    } else if (dashboard.totalHoursThisWeek === 0 && dashboard.totalHoursThisMonth === 0) {
      actions.push({
        priority: 'low',
        label: 'Start tracking time',
        description: 'Record your first time entry',
        route: ['/time-entries', 'new'],
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

  readonly recentActivity = computed<ActivityItem[]>(() => {
    const dashboard = this.dashboard();
    if (!dashboard) return [];

    const activities: ActivityItem[] = [];

    if (dashboard.approvedLeaveRequests > 0) {
      activities.push({
        type: 'approval',
        message: `${dashboard.approvedLeaveRequests} leave ${dashboard.approvedLeaveRequests === 1 ? 'request' : 'requests'} approved`,
        route: ['/leave-requests'],
      });
    }

    dashboard.alerts?.slice(0, 2).forEach((alert) => {
      activities.push({
        type: 'alert',
        message: alert.message,
        route: alert.projectId ? ['/projects', alert.projectId] : undefined,
      });
    });

    if (dashboard.completedProjects > 0) {
      activities.push({
        type: 'milestone',
        message: `${dashboard.completedProjects} ${dashboard.completedProjects === 1 ? 'project' : 'projects'} completed`,
        route: ['/projects'],
      });
    }

    if (dashboard.approvedTimeEntries > 0) {
      activities.push({
        type: 'update',
        message: `${dashboard.approvedTimeEntries} time ${dashboard.approvedTimeEntries === 1 ? 'entry' : 'entries'} approved`,
        route: ['/time-entries'],
      });
    }

    return activities.slice(0, 4);
  });

  constructor() {
    this.loadDashboard();
  }

  alertIcon(type: AlertType): LucideIconData {
    return this.alertIcons[type] ?? AlertTriangle;
  }

  priorityBadgeVariant(priority: ActionPriority): BadgeVariant {
    switch (priority) {
      case 'high':
        return 'destructive';
      case 'medium':
        return 'secondary';
      default:
        return 'outline';
    }
  }

  severityBadgeVariant(severity: AlertSeverity): BadgeVariant {
    switch (severity) {
      case 'HIGH':
        return 'destructive';
      case 'MEDIUM':
        return 'secondary';
      default:
        return 'outline';
    }
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
        },
      });
  }
}
