import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, signal } from '@angular/core';
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
import { CapacityService } from '../../core/services/capacity.service';
import { EntitlementService } from '../../core/services/entitlement.service';
import { ReportingService } from '../../core/services/reporting.service';
import { CapacityDashboard } from '../../core/models/capacity.model';
import { Dashboard } from '../../core/models/dashboard.model';
import { AlertType, AlertSeverity } from '../../core/models/alert.model';
import { ROLES } from '../../core/utils/constants';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
import {
  CardComponent,
  CardHeaderComponent,
  CardTitleComponent,
  CardDescriptionComponent,
  CardContentComponent,
} from '../../shared/components/ui/card/card.component';
import {
  AlertComponent,
  AlertTitleComponent,
  AlertDescriptionComponent,
} from '../../shared/components/ui/alert/alert.component';
import { BadgeComponent, BadgeVariant } from '../../shared/components/ui/badge/badge.component';
import { SkeletonComponent } from '../../shared/components/ui/skeleton/skeleton.component';
import { ChartComponent } from '../../shared/components/chart/chart.component';

type ActionPriority = 'high' | 'medium' | 'low';

interface DashboardAction {
  priority: ActionPriority;
  label: string;
  description: string;
  route: string[];
  fragment?: string;
  count?: number;
}

interface MetricCard {
  titleKey: string;
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
    AlertComponent,
    AlertTitleComponent,
    AlertDescriptionComponent,
    BadgeComponent,
    SkeletonComponent,
    ChartComponent,
  ],
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly reportingService = inject(ReportingService);
  private readonly capacityService = inject(CapacityService);
  private readonly entitlementService = inject(EntitlementService);
  private readonly destroyRef = inject(DestroyRef);
  private capacityRequested = false;

  readonly currentUser = this.authService.currentUser;
  readonly loading = signal(false);
  readonly capacity = signal<CapacityDashboard | null>(null);
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

  readonly projectStatusChartData = computed<ChartConfiguration['data']>(() => {
    const dashboard = this.dashboard();
    if (!dashboard) {
      return { labels: [], datasets: [{ data: [], backgroundColor: [], borderWidth: 0 }] };
    }

    const planning = Math.max(
      0,
      dashboard.totalProjects -
        dashboard.activeProjects -
        dashboard.completedProjects -
        dashboard.projectsOnHold
    );

    const segments = [
      { label: 'Active', value: dashboard.activeProjects, color: 'hsl(221, 97%, 65%)' },
      { label: 'Planning', value: planning, color: 'hsl(219, 100%, 78%)' },
      { label: 'On Hold', value: dashboard.projectsOnHold, color: 'hsl(38, 92%, 50%)' },
      { label: 'Completed', value: dashboard.completedProjects, color: 'hsl(215, 20%, 65%)' },
    ].filter((segment) => segment.value > 0);

    if (segments.length === 0) {
      return {
        labels: ['No projects'],
        datasets: [{ data: [1], backgroundColor: ['hsl(220, 43%, 18%)'], borderWidth: 0, spacing: 2 }],
      };
    }

    return {
      labels: segments.map((segment) => segment.label),
      datasets: [
        {
          data: segments.map((segment) => segment.value),
          backgroundColor: segments.map((segment) => segment.color),
          borderWidth: 0,
          spacing: 2,
        },
      ],
    };
  });

  readonly hasProjectChartData = computed(() => (this.dashboard()?.totalProjects ?? 0) > 0);

  readonly weekHoursPercent = computed(() => {
    const dashboard = this.dashboard();
    if (!dashboard || dashboard.totalHoursThisMonth <= 0) {
      return 0;
    }
    return Math.min(100, Math.round((dashboard.totalHoursThisWeek / dashboard.totalHoursThisMonth) * 100));
  });

  readonly capacityWidgets = computed(() => {
    const data = this.capacity();
    if (!data) return [];
    return [
      {
        titleKey: 'dashboard.capacity.overallocated',
        value: String(data.overallocatedEmployees),
        hintKey: 'dashboard.capacity.overallocatedHint',
        route: ['/reports', 'capacity'] as string[],
        icon: Users,
      },
      {
        titleKey: 'dashboard.capacity.available',
        value: String(data.availableEmployees),
        hintKey: 'dashboard.capacity.availableHint',
        route: ['/reports', 'capacity'] as string[],
        icon: UserX,
      },
      {
        titleKey: 'dashboard.capacity.onLeave',
        value: String(data.onLeaveEmployees),
        hintKey: 'dashboard.capacity.onLeaveHint',
        route: ['/reports', 'capacity'] as string[],
        icon: Calendar,
      },
    ].filter((widget) => Number(widget.value) > 0);
  });

  readonly showCapacitySection = computed(
    () => this.showCapacityWidgets() && this.capacityWidgets().length > 0
  );

  readonly setupSteps = computed(() => {
    const dashboard = this.dashboard();
    if (!dashboard || !this.isLowDataState()) return [];

    const steps: { labelKey: string; done: boolean; route: string[] }[] = [];

    if (dashboard.totalProjects === 0) {
      steps.push({
        labelKey: 'dashboard.setup.createProject',
        done: false,
        route: ['/projects', 'new'],
      });
    }

    if (dashboard.totalEmployees === 0) {
      steps.push({
        labelKey: 'dashboard.setup.addEmployees',
        done: false,
        route: ['/employees', 'new'],
      });
    }

    if (dashboard.totalHoursThisWeek === 0 && dashboard.totalHoursThisMonth === 0) {
      steps.push({
        labelKey: 'dashboard.setup.logTime',
        done: false,
        route: ['/time-entries'],
      });
    }

    return steps;
  });

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
          padding: 12,
          color: 'hsl(215, 20%, 65%)',
          font: { size: 12 },
          boxWidth: 8,
          boxHeight: 8,
        },
      },
    },
  };

  readonly doughnutChartOptions = computed(
    (): ChartConfiguration['options'] => this._doughnutChartOptions as ChartConfiguration['options']
  );

  readonly showCapacityWidgets = computed(
    () =>
      this.entitlementService.has('REPORTING') &&
      this.authService.hasAnyRole(ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER, ROLES.HR_ADMIN)
  );

  readonly metricCards = computed<MetricCard[]>(() => {
    const d = this.dashboard();
    if (!d) return [];

    return [
      {
        titleKey: 'dashboard.metrics.activeProjects',
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
        titleKey: 'dashboard.metrics.activeEmployees',
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
        titleKey: 'dashboard.metrics.hoursThisWeek',
        value: `${d.totalHoursThisWeek}h`,
        hint:
          d.totalHoursThisWeek === 0 && d.totalHoursThisMonth === 0
            ? 'No time entries recorded yet'
            : `${d.totalHoursThisMonth}h this month`,
        route: ['/time-entries'],
        icon: Clock,
      },
      {
        titleKey: 'dashboard.metrics.pendingLeave',
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
    effect(() => {
      if (this.entitlementService.entitlements() && !this.capacityRequested) {
        this.capacityRequested = true;
        this.loadCapacity();
      }
    });
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

  private loadCapacity(): void {
    if (!this.entitlementService.has('REPORTING')) {
      return;
    }
    if (!this.authService.hasAnyRole(ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER, ROLES.HR_ADMIN)) {
      return;
    }

    this.capacityService
      .getCapacityDashboard()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => this.capacity.set(data),
        error: () => this.capacity.set(null),
      });
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
