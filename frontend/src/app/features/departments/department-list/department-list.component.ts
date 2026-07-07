import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { DepartmentService } from '../../../core/services/department.service';
import { Department } from '../../../core/models/department.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { EmptyStateComponent } from '../../../shared/components/ui/empty-state/empty-state.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { ListPageShellComponent } from '../../../shared/components/list-page-shell/list-page-shell.component';
import { DepartmentFormComponent } from '../department-form/department-form.component';
import { DepartmentDataTableComponent } from './department-data-table.component';
import {
  TabsComponent,
  TabsContentComponent,
  TabsListComponent,
  TabsTriggerComponent,
} from '../../../shared/components/ui/tabs';

export type DepartmentFilter = 'ALL' | 'STAFFED' | 'EMPTY';

@Component({
  selector: 'app-department-list',
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
    DepartmentFormComponent,
    DepartmentDataTableComponent,
    TabsComponent,
    TabsListComponent,
    TabsTriggerComponent,
    TabsContentComponent,
  ],
  templateUrl: './department-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DepartmentListComponent {
  private readonly departmentService = inject(DepartmentService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly toastService = inject(ToastService);

  readonly departments = signal<Department[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);
  readonly departmentFilter = signal<DepartmentFilter>('ALL');

  readonly departmentFilters: { value: DepartmentFilter; labelKey: string }[] = [
    { value: 'ALL', labelKey: 'departments.filters.all' },
    { value: 'STAFFED', labelKey: 'departments.filters.staffed' },
    { value: 'EMPTY', labelKey: 'departments.filters.empty' },
  ];

  readonly totalEmployees = computed(() =>
    this.departments().reduce((sum, dept) => sum + (dept.employeeCount ?? 0), 0)
  );

  readonly withManagerCount = computed(() =>
    this.departments().filter((d) => d.managerId || d.managerName).length
  );

  readonly emptyCount = computed(() =>
    this.departments().filter((d) => (d.employeeCount ?? 0) === 0).length
  );

  readonly staffedCount = computed(() =>
    this.departments().filter((d) => (d.employeeCount ?? 0) > 0).length
  );

  readonly filteredCount = computed(() => {
    const filter = this.departmentFilter();
    if (filter === 'ALL') return this.departments().length;
    if (filter === 'STAFFED') return this.staffedCount();
    return this.emptyCount();
  });

  constructor() {
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.loading.set(true);
    this.error.set('');

    this.departmentService.getDepartments().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (departments) => {
        const departmentList = Array.isArray(departments)
          ? departments
          : ((departments as { content?: Department[] }).content || []);
        this.departments.set(departmentList);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading departments:', err);
        let errorMessage = 'Failed to load departments';

        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        } else if (typeof err === 'string') {
          errorMessage = err;
        } else if (err?.error?.message) {
          errorMessage = err.error.message;
        } else if (err?.error?.details && Array.isArray(err.error.details) && err.error.details.length > 0) {
          errorMessage = err.error.details[0];
        }

        this.error.set(errorMessage);
        this.loading.set(false);
      },
    });
  }

  onDepartmentTabChange(value: string): void {
    this.departmentFilter.set(value as DepartmentFilter);
  }

  isDepartmentTabActive(filter: DepartmentFilter): boolean {
    return this.departmentFilter() === filter;
  }

  filterCount(filter: DepartmentFilter): number {
    if (filter === 'ALL') return this.departments().length;
    if (filter === 'STAFFED') return this.staffedCount();
    return this.emptyCount();
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onDepartmentCreated(): void {
    this.closeCreateModal();
    this.toastService.successI18n('departments.toast.createdTitle', 'departments.toast.createdDescription');
    this.loadDepartments();
  }
}
