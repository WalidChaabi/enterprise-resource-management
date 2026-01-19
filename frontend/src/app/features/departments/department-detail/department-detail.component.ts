import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { DepartmentService } from '../../../core/services/department.service';
import { Department } from '../../../core/models/department.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { DepartmentFormComponent } from '../department-form/department-form.component';

@Component({
  selector: 'app-department-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslatePipe, ButtonComponent, CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent, AlertComponent, DialogComponent, DepartmentFormComponent],
  templateUrl: './department-detail.component.html'
})
export class DepartmentDetailComponent implements OnInit {
  private readonly departmentService = inject(DepartmentService);
  private readonly route = inject(ActivatedRoute);

  readonly department = signal<Department | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showEditModal = signal<boolean>(false);

  ngOnInit(): void {
    const departmentId = this.route.snapshot.paramMap.get('id');
    if (departmentId) {
      this.loadDepartment(departmentId);
    }
  }

  loadDepartment(departmentId: string): void {
    this.loading.set(true);
    this.error.set('');

    this.departmentService.getDepartment(departmentId).subscribe({
      next: (department) => {
        this.department.set(department);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading department:', err);
        let errorMessage = 'Failed to load department';
        
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
      }
    });
  }

  openEditModal(): void {
    this.showEditModal.set(true);
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
  }

  onDepartmentUpdated(): void {
    this.closeEditModal();
    const departmentId = this.department()?.id;
    if (departmentId) {
      this.loadDepartment(departmentId);
    }
  }
}
