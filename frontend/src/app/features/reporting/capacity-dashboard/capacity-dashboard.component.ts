import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import {
  LucideAngularModule,
  ChevronLeft,
  ChevronRight,
  CalendarDays,
  Users,
  UserX,
  Palmtree,
  TrendingDown,
  Search,
  Activity,
  ArrowLeft,
} from 'lucide-angular';
import { CapacityService } from '../../../core/services/capacity.service';
import { EntitlementService } from '../../../core/services/entitlement.service';
import { CapacityDashboard, EmployeeCapacityRow } from '../../../core/models/capacity.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import {
  CardComponent,
  CardHeaderComponent,
  CardTitleComponent,
  CardDescriptionComponent,
  CardContentComponent,
} from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { BadgeComponent, type BadgeVariant } from '../../../shared/components/ui/badge/badge.component';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { ChartComponent } from '../../../shared/components/chart/chart.component';
import { SkeletonComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { EmptyStateComponent } from '../../../shared/components/ui/empty-state/empty-state.component';
import {
  TabsComponent,
  TabsContentComponent,
  TabsListComponent,
  TabsTriggerComponent,
} from '../../../shared/components/ui/tabs';

export type CapacityStatusFilter =
  | 'ALL'
  | 'OVERALLOCATED'
  | 'AVAILABLE'
  | 'ON_LEAVE'
  | 'UNDERUTILIZED'
  | 'NORMAL';

interface StatCard {
  titleKey: string;
  value: number;
  hintKey: string;
  valueClass: string;
  icon: typeof Users;
}

@Component({
  selector: 'app-capacity-dashboard',
  imports: [
    CommonModule,
    RouterModule,
    DatePipe,
    DecimalPipe,
    TranslatePipe,
    LucideAngularModule,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardDescriptionComponent,
    CardContentComponent,
    AlertComponent,
    BadgeComponent,
    ButtonComponent,
    ChartComponent,
    SkeletonComponent,
    EmptyStateComponent,
    TabsComponent,
    TabsContentComponent,
    TabsListComponent,
    TabsTriggerComponent,
  ],
  templateUrl: './capacity-dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CapacityDashboardComponent {
  private readonly capacityService = inject(CapacityService);
  private readonly entitlementService = inject(EntitlementService);
  private readonly destroyRef = inject(DestroyRef);

  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;
  readonly CalendarDays = CalendarDays;
  readonly Search = Search;
  readonly Activity = Activity;
  readonly ArrowLeft = ArrowLeft;

  readonly loading = signal(false);
  readonly error = signal('');
  readonly capacity = signal<CapacityDashboard | null>(null);
  readonly weekStart = signal(this.mondayOf(new Date()));
  readonly statusFilter = signal<CapacityStatusFilter>('ALL');
  readonly searchQuery = signal('');

  readonly upgradeRequired = computed(() => !this.entitlementService.has('REPORTING'));

  readonly isCurrentWeek = computed(() => {
    const currentMonday = this.formatDate(this.mondayOf(new Date()));
    return this.formatDate(this.weekStart()) === currentMonday;
  });

  readonly statusFilters: { value: CapacityStatusFilter; labelKey: string }[] = [
    { value: 'ALL', labelKey: 'capacity.filters.all' },
    { value: 'OVERALLOCATED', labelKey: 'capacity.filters.overallocated' },
    { value: 'AVAILABLE', labelKey: 'capacity.filters.available' },
    { value: 'ON_LEAVE', labelKey: 'capacity.filters.onLeave' },
    { value: 'UNDERUTILIZED', labelKey: 'capacity.filters.underutilized' },
    { value: 'NORMAL', labelKey: 'capacity.filters.normal' },
  ];

  readonly statusCounts = computed(() => {
    const rows = this.capacity()?.employees ?? [];
    return {
      ALL: rows.length,
      OVERALLOCATED: rows.filter((r) => r.allocationStatus === 'OVERALLOCATED').length,
      AVAILABLE: rows.filter((r) => r.allocationStatus === 'AVAILABLE').length,
      ON_LEAVE: rows.filter((r) => r.onLeaveThisWeek).length,
      UNDERUTILIZED: rows.filter((r) => r.allocationStatus === 'UNDERUTILIZED').length,
      NORMAL: rows.filter((r) => r.allocationStatus === 'NORMAL').length,
    };
  });

  readonly statCards = computed<StatCard[]>(() => {
    const data = this.capacity();
    if (!data) return [];
    return [
      {
        titleKey: 'capacity.metrics.overallocated',
        value: data.overallocatedEmployees,
        hintKey: 'capacity.metrics.overallocatedHint',
        valueClass: 'text-destructive',
        icon: UserX,
      },
      {
        titleKey: 'capacity.metrics.available',
        value: data.availableEmployees,
        hintKey: 'capacity.metrics.availableHint',
        valueClass: 'text-emerald-600 dark:text-emerald-400',
        icon: Users,
      },
      {
        titleKey: 'capacity.metrics.onLeave',
        value: data.onLeaveEmployees,
        hintKey: 'capacity.metrics.onLeaveHint',
        valueClass: 'text-blue-600 dark:text-blue-400',
        icon: Palmtree,
      },
      {
        titleKey: 'capacity.metrics.underutilized',
        value: data.underutilizedEmployees,
        hintKey: 'capacity.metrics.underutilizedHint',
        valueClass: 'text-foreground',
        icon: TrendingDown,
      },
    ];
  });

  readonly filteredEmployees = computed(() => {
    const rows = this.capacity()?.employees ?? [];
    const filter = this.statusFilter();
    const query = this.searchQuery().trim().toLowerCase();

    return rows.filter((row) => {
      const matchesFilter =
        filter === 'ALL' ||
        (filter === 'ON_LEAVE' ? row.onLeaveThisWeek : row.allocationStatus === filter);
      if (!matchesFilter) return false;
      if (!query) return true;
      const haystack = [row.employeeName, row.employeeCode, row.jobTitle]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return haystack.includes(query);
    });
  });

  readonly hasChartData = computed(() => (this.capacity()?.employees.length ?? 0) > 0);

  readonly utilizationChartData = computed<ChartConfiguration['data']>(() => {
    const top = (this.capacity()?.employees ?? []).slice(0, 12);
    if (!top.length) {
      return { labels: [], datasets: [] };
    }
    return {
      labels: top.map((e) => e.employeeName.split(' ')[0]),
      datasets: [
        {
          label: 'Utilization %',
          data: top.map((e) => e.utilizationPercentage),
          backgroundColor: top.map((e) => this.utilizationColor(e)),
          borderRadius: 6,
        },
      ],
    };
  });

  readonly chartOptions = computed<ChartConfiguration['options']>(() => ({
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: {
        beginAtZero: true,
        max: 130,
        ticks: { callback: (value) => `${value}%` },
      },
    },
  }));

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.capacityService
      .getCapacityDashboard(this.formatDate(this.weekStart()))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.capacity.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(err.details?.[0] || err.message || 'Failed to load capacity data');
          this.loading.set(false);
        },
      });
  }

  previousWeek(): void {
    const d = new Date(this.weekStart());
    d.setDate(d.getDate() - 7);
    this.weekStart.set(d);
    this.load();
  }

  nextWeek(): void {
    const d = new Date(this.weekStart());
    d.setDate(d.getDate() + 7);
    this.weekStart.set(d);
    this.load();
  }

  goToCurrentWeek(): void {
    this.weekStart.set(this.mondayOf(new Date()));
    this.load();
  }

  onStatusTabChange(value: string): void {
    this.statusFilter.set(value as CapacityStatusFilter);
  }

  isStatusTabActive(value: CapacityStatusFilter): boolean {
    return this.statusFilter() === value;
  }

  statusLabel(status: EmployeeCapacityRow['allocationStatus']): string {
    const keyMap: Record<EmployeeCapacityRow['allocationStatus'], string> = {
      OVERALLOCATED: 'capacity.status.overallocated',
      AVAILABLE: 'capacity.status.available',
      UNDERUTILIZED: 'capacity.status.underutilized',
      NORMAL: 'capacity.status.normal',
    };
    return keyMap[status];
  }

  statusVariant(status: EmployeeCapacityRow['allocationStatus']): BadgeVariant {
    if (status === 'OVERALLOCATED') return 'destructive';
    if (status === 'AVAILABLE') return 'secondary';
    if (status === 'UNDERUTILIZED') return 'outline';
    return 'default';
  }

  utilizationBarClass(row: EmployeeCapacityRow): string {
    if (row.onLeaveThisWeek) return 'bg-blue-500';
    if (row.allocationStatus === 'OVERALLOCATED') return 'bg-destructive';
    if (row.allocationStatus === 'AVAILABLE') return 'bg-emerald-500';
    if (row.allocationStatus === 'UNDERUTILIZED') return 'bg-amber-500';
    return 'bg-primary';
  }

  private utilizationColor(row: EmployeeCapacityRow): string {
    if (row.onLeaveThisWeek) return 'rgba(59, 130, 246, 0.7)';
    if (row.allocationStatus === 'OVERALLOCATED') return 'rgba(239, 68, 68, 0.75)';
    if (row.allocationStatus === 'AVAILABLE') return 'rgba(34, 197, 94, 0.65)';
    return 'rgba(99, 102, 241, 0.7)';
  }

  private mondayOf(date: Date): Date {
    const d = new Date(date);
    const day = d.getDay();
    const diff = day === 0 ? -6 : 1 - day;
    d.setDate(d.getDate() + diff);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  private formatDate(date: Date): string {
    return date.toISOString().slice(0, 10);
  }
}
