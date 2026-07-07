import { LucideAngularModule, LucideIconData, FolderKanban, Users, Clock, Calendar, DollarSign, Activity, UserX, TrendingDown, AlertTriangle, ArrowRight, CheckCircle2, PauseCircle, CalendarDays, Timer, ListOrdered, BarChart3, Plane } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { format, isValid, parseISO } from 'date-fns';
import { forkJoin } from 'rxjs';
import { ReportingService } from '../../../core/services/reporting.service';
import { EntitlementService } from '../../../core/services/entitlement.service';
import { ResourceAllocationService } from '../../../core/services/resource-allocation.service';
import { Dashboard } from '../../../core/models/dashboard.model';
import { LeaveBalance, LeaveReport, ReportingTab, TimeReport } from '../../../core/models/report.model';
import { ResourceAllocation, EmployeeAllocation, ProjectAllocation } from '../../../core/models/resource-allocation.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { TranslationService } from '../../../core/services/translation.service';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { BadgeComponent, type BadgeVariant } from '../../../shared/components/ui/badge/badge.component';
import { ChartComponent } from '../../../shared/components/chart/chart.component';
import { SkeletonComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { ButtonDirective } from '../../../shared/components/ui/button/button.directive';
import { DatePickerComponent } from '../../../shared/components/ui/date-picker/date-picker.component';
import {
  TabsComponent,
  TabsContentComponent,
  TabsListComponent,
  TabsTriggerComponent,
} from '../../../shared/components/ui/tabs';
import {
  TableComponent,
  TableHeaderComponent,
  TableBodyComponent,
  TableRowComponent,
  TableHeadComponent,
  TableCellComponent,
} from '../../../shared/components/ui/table/table.component';

interface OverviewMetricCard {
  titleKey: string;
  value: string;
  hintKey: string;
  hintParams?: Record<string, string | number>;
  route: string[];
  icon: LucideIconData;
  valueClass?: string;
}

interface OperationalItem {
  labelKey: string;
  descriptionKey: string;
  count: number;
  route: string[];
  priority: 'high' | 'medium' | 'low';
}

interface AllocationStat {
  labelKey: string;
  value: number;
  icon: LucideIconData;
  valueClass: string;
}

interface ReportMetricCard {
  titleKey: string;
  value: string;
  hintKey: string;
  icon: LucideIconData;
  valueClass?: string;
}

type DateRangePreset = 'thisWeek' | 'thisMonth' | 'lastMonth' | null;

@Component({
    selector: 'app-reporting-dashboard',
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        TranslatePipe,
        LucideAngularModule,
        CardComponent,
        CardHeaderComponent,
        CardTitleComponent,
        CardDescriptionComponent,
        CardContentComponent,
        AlertComponent,
        BadgeComponent,
        ChartComponent,
        SkeletonComponent,
        ButtonDirective,
        DatePickerComponent,
        TabsComponent,
        TabsContentComponent,
        TabsListComponent,
        TabsTriggerComponent,
        TableComponent,
        TableHeaderComponent,
        TableBodyComponent,
        TableRowComponent,
        TableHeadComponent,
        TableCellComponent,
    ],
    templateUrl: './reporting-dashboard.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportingDashboardComponent {
  private readonly reportingService = inject(ReportingService);
  private readonly entitlementService = inject(EntitlementService);
  private readonly resourceAllocationService = inject(ResourceAllocationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly i18n = inject(TranslationService);

  readonly activeTab = signal<ReportingTab>('overview');
  readonly dashboard = signal<Dashboard | null>(null);
  readonly allocation = signal<ResourceAllocation | null>(null);
  readonly timeByProject = signal<TimeReport[]>([]);
  readonly timeByEmployee = signal<TimeReport[]>([]);
  readonly timeSummary = signal<TimeReport | null>(null);
  readonly leaveBalances = signal<LeaveBalance[]>([]);
  readonly upcomingLeave = signal<LeaveReport[]>([]);

  readonly loading = signal<boolean>(false);
  readonly timeLoading = signal<boolean>(false);
  readonly leaveLoading = signal<boolean>(false);
  readonly error = signal<string>('');

  readonly upgradeRequired = computed(
    () => !this.entitlementService.has('REPORTING')
  );

  readonly startDate = signal(this.monthStart());
  readonly endDate = signal(this.today());
  readonly activeDatePreset = signal<DateRangePreset>('thisMonth');

  readonly tabs: { id: ReportingTab; labelKey: string }[] = [
    { id: 'overview', labelKey: 'reporting.tabs.overview' },
    { id: 'time', labelKey: 'reporting.tabs.time' },
    { id: 'leave', labelKey: 'reporting.tabs.leave' },
  ];

  readonly FolderKanban = FolderKanban;
  readonly Users = Users;
  readonly Clock = Clock;
  readonly Calendar = Calendar;
  readonly Activity = Activity;
  readonly ArrowRight = ArrowRight;
  readonly CheckCircle2 = CheckCircle2;
  readonly AlertTriangle = AlertTriangle;
  readonly CalendarDays = CalendarDays;
  readonly BarChart3 = BarChart3;
  readonly Plane = Plane;
  readonly ListOrdered = ListOrdered;
  readonly Timer = Timer;

  readonly timeMetricCards = computed<ReportMetricCard[]>(() => {
    const summary = this.timeSummary();
    if (!summary) return [];

    return [
      {
        titleKey: 'reporting.time.totalHours',
        value: `${summary.totalHours.toFixed(1)}h`,
        hintKey: 'reporting.time.totalHoursHint',
        icon: Clock,
      },
      {
        titleKey: 'reporting.time.entriesCount',
        value: String(summary.entriesCount),
        hintKey: 'reporting.time.entriesHint',
        icon: ListOrdered,
      },
      {
        titleKey: 'reporting.time.avgPerEntry',
        value: `${summary.averageHoursPerEntry.toFixed(1)}h`,
        hintKey: 'reporting.time.avgHint',
        icon: Timer,
      },
    ];
  });

  readonly leaveMetricCards = computed<ReportMetricCard[]>(() => {
    const balances = this.leaveBalances();
    const upcoming = this.upcomingLeave();
    const pendingDays = balances.reduce((sum, row) => sum + row.totalDaysPending, 0);
    const approvedYear = balances.reduce((sum, row) => sum + row.totalDaysThisYear, 0);

    return [
      {
        titleKey: 'reporting.leave.summaryEmployees',
        value: String(balances.length),
        hintKey: 'reporting.leave.summaryEmployeesHint',
        icon: Users,
      },
      {
        titleKey: 'reporting.leave.summaryPending',
        value: String(pendingDays),
        hintKey: 'reporting.leave.summaryPendingHint',
        icon: Calendar,
        valueClass: pendingDays > 0 ? 'text-amber-600 dark:text-amber-400' : undefined,
      },
      {
        titleKey: 'reporting.leave.summaryUpcoming',
        value: String(upcoming.length),
        hintKey: 'reporting.leave.summaryUpcomingHint',
        icon: Plane,
      },
      {
        titleKey: 'reporting.leave.summaryApprovedYear',
        value: String(approvedYear),
        hintKey: 'reporting.leave.summaryApprovedYearHint',
        icon: CheckCircle2,
      },
    ];
  });

  readonly metricCards = computed<OverviewMetricCard[]>(() => {
    const d = this.dashboard();
    if (!d) return [];

    return [
      {
        titleKey: 'reporting.metrics.activeProjects',
        value: String(d.activeProjects),
        hintKey: 'reporting.metrics.projectsHint',
        hintParams: { total: d.totalProjects },
        route: ['/projects'],
        icon: FolderKanban,
      },
      {
        titleKey: 'reporting.metrics.activeEmployees',
        value: String(d.activeEmployees),
        hintKey: 'reporting.metrics.employeesHint',
        hintParams: { total: d.totalEmployees },
        route: ['/employees'],
        icon: Users,
      },
      {
        titleKey: 'reporting.metrics.hoursThisWeek',
        value: `${d.totalHoursThisWeek}h`,
        hintKey: 'reporting.metrics.hoursHint',
        hintParams: { month: d.totalHoursThisMonth },
        route: ['/time-entries'],
        icon: Clock,
      },
      {
        titleKey: 'reporting.metrics.budgetUtilization',
        value: `${Math.round(d.budgetUtilization)}%`,
        hintKey: 'reporting.metrics.budgetHint',
        hintParams: { cost: d.totalCost, budget: d.totalBudget },
        route: ['/projects'],
        icon: DollarSign,
        valueClass:
          d.budgetUtilization > 100
            ? 'text-destructive'
            : d.budgetUtilization > 85
              ? 'text-amber-600 dark:text-amber-400'
              : 'text-foreground',
      },
    ] as OverviewMetricCard[];
  });

  readonly projectStatusChartData = computed<ChartConfiguration['data']>(() => {
    this.i18n.languageTick();
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
      { labelKey: 'reporting.charts.active', value: dashboard.activeProjects, color: 'hsl(221, 97%, 65%)' },
      { labelKey: 'reporting.charts.planning', value: planning, color: 'hsl(219, 100%, 78%)' },
      { labelKey: 'reporting.charts.onHold', value: dashboard.projectsOnHold, color: 'hsl(38, 92%, 50%)' },
      { labelKey: 'reporting.charts.completed', value: dashboard.completedProjects, color: 'hsl(215, 20%, 65%)' },
    ].filter((segment) => segment.value > 0);

    if (segments.length === 0) {
      return {
        labels: ['—'],
        datasets: [{ data: [1], backgroundColor: ['hsl(220, 43%, 18%)'], borderWidth: 0, spacing: 2 }],
      };
    }

    return {
      labels: segments.map((s) => this.i18n.translate(s.labelKey)),
      datasets: [
        {
          data: segments.map((s) => s.value),
          backgroundColor: segments.map((s) => s.color),
          borderWidth: 0,
          spacing: 2,
        },
      ],
    };
  });

  readonly hasProjectChartData = computed(() => (this.dashboard()?.totalProjects ?? 0) > 0);

  readonly operationalItems = computed<OperationalItem[]>(() => {
    const d = this.dashboard();
    if (!d) return [];

    const items: OperationalItem[] = [];

    if (d.pendingLeaveRequests > 0) {
      items.push({
        labelKey: 'reporting.actions.pendingLeave',
        descriptionKey: 'reporting.actions.pendingLeaveHint',
        count: d.pendingLeaveRequests,
        route: ['/leave-requests'],
        priority: 'high',
      });
    }

    if (d.pendingTimeEntries > 0) {
      items.push({
        labelKey: 'reporting.actions.pendingTime',
        descriptionKey: 'reporting.actions.pendingTimeHint',
        count: d.pendingTimeEntries,
        route: ['/time-entries'],
        priority: 'medium',
      });
    }

    if (d.projectsOnHold > 0) {
      items.push({
        labelKey: 'reporting.actions.projectsOnHold',
        descriptionKey: 'reporting.actions.projectsOnHoldHint',
        count: d.projectsOnHold,
        route: ['/projects'],
        priority: 'low',
      });
    }

    return items;
  });

  readonly allocationStats = computed<AllocationStat[]>(() => {
    const a = this.allocation();
    return [
      {
        labelKey: 'reporting.allocation.available',
        value: a?.availableEmployees ?? 0,
        icon: Users,
        valueClass: 'text-emerald-600 dark:text-emerald-400',
      },
      {
        labelKey: 'reporting.allocation.overallocated',
        value: a?.overallocatedEmployees ?? 0,
        icon: UserX,
        valueClass: 'text-destructive',
      },
      {
        labelKey: 'reporting.allocation.underutilized',
        value: a?.underutilizedEmployees ?? 0,
        icon: TrendingDown,
        valueClass: 'text-foreground',
      },
      {
        labelKey: 'reporting.allocation.understaffedProjects',
        value: a?.understaffedProjects ?? 0,
        icon: PauseCircle,
        valueClass: 'text-amber-600 dark:text-amber-400',
      },
    ];
  });

  readonly pendingActionsCount = computed(
    () => (this.dashboard()?.pendingLeaveRequests ?? 0) + (this.dashboard()?.pendingTimeEntries ?? 0)
  );

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

  readonly topOverallocatedEmployees = computed<EmployeeAllocation[]>(() => {
    const allocation = this.allocation();
    if (!allocation) {
      return [];
    }
    return [...allocation.employees]
      .filter((employee) => employee.allocationStatus === 'OVERALLOCATED')
      .sort((a, b) => b.utilizationPercentage - a.utilizationPercentage)
      .slice(0, 5);
  });

  readonly topUnderstaffedProjects = computed<ProjectAllocation[]>(() => {
    const allocation = this.allocation();
    if (!allocation) {
      return [];
    }
    return [...allocation.projects]
      .filter((project) => project.allocationStatus === 'UNDERSTAFFED')
      .sort((a, b) => b.gapHours - a.gapHours)
      .slice(0, 5);
  });

  constructor() {
    this.loadOverview();
  }

  onTabChange(value: string): void {
    this.setTab(value as ReportingTab);
  }

  onStartDateChange(date: string | Date | null): void {
    const normalized = this.normalizeDateValue(date);
    if (!normalized) {
      return;
    }
    this.startDate.set(normalized);
    this.activeDatePreset.set(null);
  }

  onEndDateChange(date: string | Date | null): void {
    const normalized = this.normalizeDateValue(date);
    if (!normalized) {
      return;
    }
    this.endDate.set(normalized);
    this.activeDatePreset.set(null);
  }

  setTab(tab: ReportingTab): void {
    this.activeTab.set(tab);
    if (tab === 'time' && this.timeByProject().length === 0 && !this.timeLoading()) {
      this.loadTimeReports();
    }
    if (tab === 'leave' && this.leaveBalances().length === 0 && this.upcomingLeave().length === 0 && !this.leaveLoading()) {
      this.loadLeaveReports();
    }
  }

  applyTimeFilter(): void {
    this.activeDatePreset.set(null);
    this.loadTimeReports();
  }

  applyDatePreset(preset: Exclude<DateRangePreset, null>): void {
    const now = new Date();

    if (preset === 'thisMonth') {
      this.startDate.set(this.monthStart());
      this.endDate.set(this.today());
    } else if (preset === 'lastMonth') {
      const first = new Date(now.getFullYear(), now.getMonth() - 1, 1);
      const last = new Date(now.getFullYear(), now.getMonth(), 0);
      this.startDate.set(this.formatDate(first));
      this.endDate.set(this.formatDate(last));
    } else {
      const day = now.getDay();
      const diff = day === 0 ? 6 : day - 1;
      const monday = new Date(now);
      monday.setDate(now.getDate() - diff);
      this.startDate.set(this.formatDate(monday));
      this.endDate.set(this.today());
    }

    this.activeDatePreset.set(preset);
    this.loadTimeReports();
  }

  timeHoursBarWidth(row: TimeReport, rows: TimeReport[]): number {
    const maxHours = Math.max(...rows.map((item) => item.totalHours), 1);
    return (row.totalHours / maxHours) * 100;
  }

  leaveStatusVariant(status: string): BadgeVariant {
    switch (status.toUpperCase()) {
      case 'APPROVED':
        return 'default';
      case 'PENDING':
        return 'secondary';
      case 'REJECTED':
      case 'CANCELLED':
        return 'destructive';
      default:
        return 'outline';
    }
  }

  loadOverview(): void {
    this.loading.set(true);
    this.error.set('');

    this.reportingService
      .getDashboard()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (dashboard) => {
          this.dashboard.set(dashboard);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
        }
      });

    if (this.upgradeRequired()) {
      return;
    }

    this.resourceAllocationService
      .getResourceAllocation()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (allocation) => {
          this.allocation.set(allocation);
        },
        error: (err) => {
          if (err?.code === 'UPGRADE_REQUIRED') {
            return;
          }
          const currentError = this.error();
          const nextError = err?.error?.message || err?.message || 'Failed to load resource allocation';
          this.error.set(currentError ? `${currentError}. ${nextError}` : nextError);
        }
      });
  }

  loadTimeReports(): void {
    this.timeLoading.set(true);
    this.error.set('');

    forkJoin({
      byProject: this.reportingService.getTimeReportByProject(this.startDate(), this.endDate()),
      byEmployee: this.reportingService.getTimeReportByEmployee(this.startDate(), this.endDate()),
      summary: this.reportingService.getTimeSummary(this.startDate(), this.endDate()),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ byProject, byEmployee, summary }) => {
          this.timeByProject.set(byProject);
          this.timeByEmployee.set(byEmployee);
          this.timeSummary.set(summary);
          this.timeLoading.set(false);
        },
        error: (err) => {
          this.error.set(err?.error?.message || err?.message || 'Failed to load time reports');
          this.timeLoading.set(false);
        }
      });
  }

  loadLeaveReports(): void {
    this.leaveLoading.set(true);
    this.error.set('');

    forkJoin({
      balances: this.reportingService.getLeaveBalance(),
      upcoming: this.reportingService.getUpcomingLeave(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ balances, upcoming }) => {
          this.leaveBalances.set(balances);
          this.upcomingLeave.set(upcoming);
          this.leaveLoading.set(false);
        },
        error: (err) => {
          this.error.set(err?.error?.message || err?.message || 'Failed to load leave reports');
          this.leaveLoading.set(false);
        }
      });
  }

  private monthStart(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    return `${now.getFullYear()}-${month}-01`;
  }

  private formatDate(date: Date): string {
    return date.toISOString().slice(0, 10);
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private normalizeDateValue(value: string | Date | null): string | null {
    if (!value) {
      return null;
    }
    if (typeof value === 'string') {
      const parsed = parseISO(value);
      return isValid(parsed) ? value : null;
    }
    return isValid(value) ? format(value, 'yyyy-MM-dd') : null;
  }

  priorityBadgeVariant(priority: OperationalItem['priority']): BadgeVariant {
    switch (priority) {
      case 'high':
        return 'destructive';
      case 'medium':
        return 'secondary';
      default:
        return 'outline';
    }
  }

  priorityLabelKey(priority: OperationalItem['priority']): string {
    return `reporting.priority.${priority}`;
  }

  utilizationBarWidth(percent: number): number {
    return Math.min(100, Math.max(0, percent));
  }

  gapBarWidth(project: ProjectAllocation): number {
    const projects = this.topUnderstaffedProjects();
    const maxGap = Math.max(...projects.map((p) => p.gapHours), 1);
    return (project.gapHours / maxGap) * 100;
  }
}
