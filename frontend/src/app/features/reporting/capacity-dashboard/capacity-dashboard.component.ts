import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { LucideAngularModule, ChevronLeft, ChevronRight, CalendarDays } from 'lucide-angular';
import { CapacityService } from '../../../core/services/capacity.service';
import { EntitlementService } from '../../../core/services/entitlement.service';
import { CapacityDashboard, EmployeeCapacityRow } from '../../../core/models/capacity.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { ChartComponent } from '../../../shared/components/chart/chart.component';

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

  readonly loading = signal(false);
  readonly error = signal('');
  readonly capacity = signal<CapacityDashboard | null>(null);
  readonly weekStart = signal(this.mondayOf(new Date()));

  readonly upgradeRequired = computed(() => !this.entitlementService.has('REPORTING'));

  readonly utilizationChartData = computed<ChartConfiguration['data']>(() => {
    const data = this.capacity();
    if (!data?.employees.length) {
      return { labels: [], datasets: [] };
    }
    const top = data.employees.slice(0, 12);
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

  statusVariant(status: EmployeeCapacityRow['allocationStatus']): 'default' | 'secondary' | 'destructive' | 'outline' {
    if (status === 'OVERALLOCATED') return 'destructive';
    if (status === 'AVAILABLE') return 'secondary';
    if (status === 'UNDERUTILIZED') return 'outline';
    return 'default';
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
