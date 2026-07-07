import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { EmployeeService } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/services/auth.service';
import { Employee, Page } from '../../../core/models/employee.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { EmptyStateComponent } from '../../../shared/components/ui/empty-state/empty-state.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { ListPageShellComponent } from '../../../shared/components/list-page-shell/list-page-shell.component';
import { EmployeeFormComponent } from '../employee-form/employee-form.component';
import { EmployeeDataTableComponent } from './employee-data-table.component';
import {
  TabsComponent,
  TabsContentComponent,
  TabsListComponent,
  TabsTriggerComponent,
} from '../../../shared/components/ui/tabs';

export type EmployeeStatusFilter = 'ALL' | 'ACTIVE' | 'INACTIVE';

@Component({
  selector: 'app-employee-list',
  imports: [
    CommonModule,
    TranslatePipe,
    LucideAngularModule,
    ButtonComponent,
    CardComponent,
    CardContentComponent,
    SkeletonTableComponent,
    EmptyStateComponent,
    DialogComponent,
    ListPageShellComponent,
    EmployeeFormComponent,
    EmployeeDataTableComponent,
    TabsComponent,
    TabsListComponent,
    TabsTriggerComponent,
    TabsContentComponent,
  ],
  templateUrl: './employee-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeListComponent {
  private readonly employeeService = inject(EmployeeService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly toastService = inject(ToastService);

  readonly employees = signal<Employee[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);
  readonly statusFilter = signal<EmployeeStatusFilter>('ALL');

  readonly statusFilters: { value: EmployeeStatusFilter; labelKey: string }[] = [
    { value: 'ALL', labelKey: 'employees.filters.all' },
    { value: 'ACTIVE', labelKey: 'common.active' },
    { value: 'INACTIVE', labelKey: 'common.inactive' },
  ];

  readonly canCreateEmployee = computed(() => {
    return this.authService.hasAnyRole(ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER);
  });

  readonly countByStatus = computed(() => {
    const items = this.employees();
    return {
      active: items.filter((e) => e.active).length,
      inactive: items.filter((e) => !e.active).length,
    };
  });

  readonly departmentCount = computed(() => {
    const names = new Set(
      this.employees()
        .map((e) => e.departmentName)
        .filter(Boolean)
    );
    return names.size;
  });

  readonly unassignedCount = computed(() =>
    this.employees().filter((e) => !e.departmentId && !e.departmentName).length
  );

  readonly filteredCount = computed(() => {
    const filter = this.statusFilter();
    if (filter === 'ALL') return this.employees().length;
    if (filter === 'ACTIVE') return this.countByStatus().active;
    return this.countByStatus().inactive;
  });

  constructor() {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.loading.set(true);
    this.error.set('');

    this.employeeService.getEmployees({ size: 1000 }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page: Page<Employee>) => {
        this.employees.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load employees');
        this.loading.set(false);
      },
    });
  }

  onStatusTabChange(value: string): void {
    this.statusFilter.set(value as EmployeeStatusFilter);
  }

  isStatusTabActive(filter: EmployeeStatusFilter): boolean {
    return this.statusFilter() === filter;
  }

  statusCount(filter: EmployeeStatusFilter): number {
    if (filter === 'ALL') return this.employees().length;
    if (filter === 'ACTIVE') return this.countByStatus().active;
    return this.countByStatus().inactive;
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onEmployeeCreated(): void {
    this.closeCreateModal();
    this.toastService.successI18n('employees.toast.createdTitle', 'employees.toast.createdDescription');
    this.loadEmployees();
  }
}
