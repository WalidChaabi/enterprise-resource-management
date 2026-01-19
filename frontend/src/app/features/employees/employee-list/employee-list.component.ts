import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { EmployeeService } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/services/auth.service';
import { Employee, Page } from '../../../core/models/employee.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent, CardHeaderComponent, CardTitleComponent } from '../../../shared/components/ui/card/card.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { EmployeeFormComponent } from '../employee-form/employee-form.component';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    TranslatePipe, 
    ButtonComponent, 
    CardComponent, 
    CardContentComponent, 
    CardHeaderComponent,
    CardTitleComponent,
    BadgeComponent, 
    AlertComponent, 
    SkeletonTableComponent,
    DialogComponent,
    EmployeeFormComponent
  ],
  templateUrl: './employee-list.component.html'
})
export class EmployeeListComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly authService = inject(AuthService);

  readonly employees = signal<Employee[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly totalElements = signal<number>(0);
  readonly currentPage = signal<number>(0);
  readonly pageSize = signal<number>(20);
  readonly showCreateModal = signal<boolean>(false);
  
  readonly canCreateEmployee = computed(() => {
    return this.authService.hasAnyRole(ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER);
  });
  
  // Expose Math to template
  readonly Math = Math;

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(page: number = 0): void {
    this.loading.set(true);
    this.error.set('');

    this.employeeService.getEmployees({
      page,
      size: this.pageSize(),
      active: true
    }).subscribe({
      next: (page: Page<Employee>) => {
        this.employees.set(page.content);
        this.totalElements.set(page.totalElements);
        this.currentPage.set(page.number);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load employees');
        this.loading.set(false);
      }
    });
  }

  onPageChange(page: number): void {
    this.loadEmployees(page);
  }

  openCreateModal(): void {
    console.log('Opening create modal');
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onEmployeeCreated(): void {
    this.closeCreateModal();
    this.loadEmployees(this.currentPage());
  }
}
