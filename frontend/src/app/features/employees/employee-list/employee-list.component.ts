import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { EmployeeService } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/services/auth.service';
import { Employee, Page } from '../../../core/models/employee.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { EmployeeFormComponent } from '../employee-form/employee-form.component';
import { EmployeeDataTableComponent } from './employee-data-table.component';

@Component({
    selector: 'app-employee-list',
    imports: [
        CommonModule,
        TranslatePipe,
        LucideAngularModule,
        ButtonComponent,
        CardComponent,
        CardContentComponent,
        AlertComponent,
        SkeletonTableComponent,
        DialogComponent,
        EmployeeFormComponent,
        EmployeeDataTableComponent
    ],
    templateUrl: './employee-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EmployeeListComponent {
  private readonly employeeService = inject(EmployeeService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly employees = signal<Employee[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);
  
  readonly canCreateEmployee = computed(() => {
    return this.authService.hasAnyRole(ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER);
  });
  
  constructor() {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.loading.set(true);
    this.error.set('');

    this.employeeService.getEmployees({
      size: 1000,
      active: true
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page: Page<Employee>) => {
        this.employees.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load employees');
        this.loading.set(false);
      }
    });
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onEmployeeCreated(): void {
    this.closeCreateModal();
    this.loadEmployees();
  }
}
