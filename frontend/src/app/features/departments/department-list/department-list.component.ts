import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { DepartmentService } from '../../../core/services/department.service';
import { Department } from '../../../core/models/department.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { DepartmentFormComponent } from '../department-form/department-form.component';
import { DepartmentDataTableComponent } from './department-data-table.component';

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
    DialogComponent,
    DepartmentFormComponent,
    DepartmentDataTableComponent,
  ],
  templateUrl: './department-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DepartmentListComponent {
  private readonly departmentService = inject(DepartmentService);
  private readonly destroyRef = inject(DestroyRef);

  readonly departments = signal<Department[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);

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
