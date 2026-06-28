import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BreadcrumbService } from '../../../core/services/breadcrumb.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { UserRoleService } from '../../../core/services/user-role.service';
import { Employee } from '../../../core/models/employee.model';
import { UserRole, Role } from '../../../core/models/user-role.model';
import { TranslationService } from '../../../core/services/translation.service';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { RolePickerComponent } from '../../../shared/components/role-picker/role-picker.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { EmployeeFormComponent } from '../employee-form/employee-form.component';

@Component({
    selector: 'app-employee-detail',
    imports: [CommonModule, RouterModule, TranslatePipe, ButtonComponent, CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent, BadgeComponent, AlertComponent, RolePickerComponent, DialogComponent, EmployeeFormComponent],
    templateUrl: './employee-detail.component.html'
})
export class EmployeeDetailComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly userRoleService = inject(UserRoleService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly translationService = inject(TranslationService);

  readonly employee = signal<Employee | null>(null);
  readonly userRoles = signal<UserRole[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly assigningRole = signal<boolean>(false);
  readonly showEditModal = signal<boolean>(false);

  ngOnInit(): void {
    const employeeId = this.route.snapshot.paramMap.get('id');
    if (employeeId) {
      this.loadEmployee(employeeId);
      this.loadEmployeeRoles(employeeId);
    }
  }

  loadEmployee(employeeId: string): void {
    this.loading.set(true);
    this.error.set('');

    this.employeeService.getEmployee(employeeId).subscribe({
      next: (employee) => {
        this.employee.set(employee);
        this.setBreadcrumbs(employee);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load employee');
        this.loading.set(false);
      }
    });
  }

  openEditModal(): void {
    this.showEditModal.set(true);
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
  }

  onEmployeeUpdated(): void {
    this.closeEditModal();
    const employeeId = this.employee()?.id;
    if (employeeId) {
      this.loadEmployee(employeeId);
      this.loadEmployeeRoles(employeeId);
    }
  }

  loadEmployeeRoles(employeeId: string): void {
    this.userRoleService.getEmployeeRoles(employeeId).subscribe({
      next: (roles) => {
        this.userRoles.set(roles);
      },
      error: (err) => {
        console.error('Error loading employee roles:', err);
        // Don't show error for roles, just log it
      }
    });
  }

  getAssignedRoleIds(): string[] {
    return this.userRoles().map(ur => ur.roleId);
  }

  onRoleSelected(role: Role): void {
    const employee = this.employee();
    if (!employee || !employee.userId) {
      this.error.set('Cannot assign role: Employee user ID not found');
      return;
    }

    this.assigningRole.set(true);
    this.userRoleService.assignRole({
      userId: employee.userId,
      roleId: role.id
    }).subscribe({
      next: (userRole) => {
        this.userRoles.set([...this.userRoles(), userRole]);
        this.assigningRole.set(false);
      },
      error: (err) => {
        console.error('Error assigning role:', err);
        let errorMessage = 'Failed to assign role';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        }
        
        this.error.set(errorMessage);
        this.assigningRole.set(false);
      }
    });
  }

  onRoleRemoved(roleId: string): void {
    const employee = this.employee();
    if (!employee || !employee.userId) {
      this.error.set('Cannot remove role: Employee user ID not found');
      return;
    }

    this.assigningRole.set(true);
    this.userRoleService.removeRole(employee.userId, roleId).subscribe({
      next: () => {
        this.userRoles.set(this.userRoles().filter(ur => ur.roleId !== roleId));
        this.assigningRole.set(false);
      },
      error: (err) => {
        console.error('Error removing role:', err);
        let errorMessage = 'Failed to remove role';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        }
        
        this.error.set(errorMessage);
        this.assigningRole.set(false);
      }
    });
  }

  private setBreadcrumbs(employee: Employee): void {
    const displayName = `${employee.firstName ?? ''} ${employee.lastName ?? ''}`.trim()
      || employee.email
      || employee.employeeCode
      || employee.id;

    this.breadcrumbService.setItems(
      [
        { label: this.translationService.translate('nav.dashboard'), route: '/dashboard' },
        { label: this.translationService.translate('nav.employees'), route: '/employees' },
        { label: displayName }
      ],
      this.router.url
    );
  }
}
