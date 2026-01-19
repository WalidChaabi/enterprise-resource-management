import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DepartmentService } from '../../../core/services/department.service';
import { Department } from '../../../core/models/department.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { DepartmentFormComponent } from '../department-form/department-form.component';

@Component({
  selector: 'app-department-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslatePipe, ButtonComponent, CardComponent, CardContentComponent, BadgeComponent, SkeletonTableComponent, DialogComponent, DepartmentFormComponent],
  templateUrl: './department-list.component.html'
})
export class DepartmentListComponent implements OnInit {
  private readonly departmentService = inject(DepartmentService);

  readonly departments = signal<Department[]>([]);
  readonly departmentList = computed<Department[]>(() => {
    const value = this.departments() as unknown;
    if (Array.isArray(value)) {
      return value;
    }
    const fallback = (value as { content?: Department[]; items?: Department[]; data?: Department[] }) || {};
    const list = fallback.content || fallback.items || fallback.data || [];
    return Array.isArray(list) ? list : [];
  });
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);

  ngOnInit(): void {
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.loading.set(true);
    this.error.set('');

    this.departmentService.getDepartments().subscribe({
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
      }
    });
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onDepartmentCreated(): void {
    this.closeCreateModal();
    this.loadDepartments();
  }
}
