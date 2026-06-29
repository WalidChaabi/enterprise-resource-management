import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ReportingService } from '../../../core/services/reporting.service';
import { ResourceAllocationService } from '../../../core/services/resource-allocation.service';
import { Dashboard } from '../../../core/models/dashboard.model';
import { LeaveBalance, LeaveReport, ReportingTab, TimeReport } from '../../../core/models/report.model';
import { ResourceAllocation, EmployeeAllocation, ProjectAllocation } from '../../../core/models/resource-allocation.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';

@Component({
    selector: 'app-reporting-dashboard',
    imports: [
        CommonModule,
        FormsModule,
        TranslatePipe,
        LucideAngularModule,
        CardComponent,
        CardHeaderComponent,
        CardTitleComponent,
        CardDescriptionComponent,
        CardContentComponent,
        AlertComponent,
        ButtonComponent,
        BadgeComponent
    ],
    templateUrl: './reporting-dashboard.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportingDashboardComponent {
  private readonly reportingService = inject(ReportingService);
  private readonly resourceAllocationService = inject(ResourceAllocationService);
  private readonly destroyRef = inject(DestroyRef);

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

  readonly startDate = signal(this.monthStart());
  readonly endDate = signal(this.today());

  readonly tabs: { id: ReportingTab; labelKey: string }[] = [
    { id: 'overview', labelKey: 'reporting.tabs.overview' },
    { id: 'time', labelKey: 'reporting.tabs.time' },
    { id: 'leave', labelKey: 'reporting.tabs.leave' },
  ];

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
    this.loadTimeReports();
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
        error: (err) => {
          this.error.set(err?.error?.message || err?.message || 'Failed to load reports');
          this.loading.set(false);
        }
      });

    this.resourceAllocationService
      .getResourceAllocation()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (allocation) => {
          this.allocation.set(allocation);
        },
        error: (err) => {
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

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
