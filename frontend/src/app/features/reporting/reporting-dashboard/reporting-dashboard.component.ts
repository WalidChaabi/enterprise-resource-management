import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReportingService } from '../../../core/services/reporting.service';
import { ResourceAllocationService } from '../../../core/services/resource-allocation.service';
import { Dashboard } from '../../../core/models/dashboard.model';
import { ResourceAllocation, EmployeeAllocation, ProjectAllocation } from '../../../core/models/resource-allocation.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
  selector: 'app-reporting-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    TranslatePipe,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardDescriptionComponent,
    CardContentComponent,
    AlertComponent
  ],
  templateUrl: './reporting-dashboard.component.html'
})
export class ReportingDashboardComponent implements OnInit {
  private readonly reportingService = inject(ReportingService);
  private readonly resourceAllocationService = inject(ResourceAllocationService);

  readonly dashboard = signal<Dashboard | null>(null);
  readonly allocation = signal<ResourceAllocation | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');

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

  ngOnInit(): void {
    this.loadReportingData();
  }

  loadReportingData(): void {
    this.loading.set(true);
    this.error.set('');

    this.reportingService.getDashboard().subscribe({
      next: (dashboard) => {
        this.dashboard.set(dashboard);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || err?.message || 'Failed to load reports');
        this.loading.set(false);
      }
    });

    this.resourceAllocationService.getResourceAllocation().subscribe({
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
}
