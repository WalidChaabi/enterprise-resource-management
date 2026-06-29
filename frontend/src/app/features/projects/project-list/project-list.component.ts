import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { Project } from '../../../core/models/project.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { SkeletonComponent, SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { ProjectFormComponent } from '../project-form/project-form.component';
import { ProjectDataTableComponent } from './project-data-table.component';

@Component({
  selector: 'app-project-list',
  imports: [
    CommonModule,
    TranslatePipe,
    LucideAngularModule,
    ButtonComponent,
    CardComponent,
    CardContentComponent,
    SkeletonComponent,
    SkeletonTableComponent,
    DialogComponent,
    ProjectFormComponent,
    ProjectDataTableComponent,
  ],
  templateUrl: './project-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectListComponent {
  private readonly projectService = inject(ProjectService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly projects = signal<Project[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);
  readonly archivingProjectId = signal<string | null>(null);
  readonly processing = signal(false);

  readonly canCreateProject = computed(() => {
    return this.authService.hasAnyRole(ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER);
  });

  readonly canArchiveProject = computed(() => {
    return this.authService.hasAnyRole(ROLES.ORG_ADMIN);
  });

  constructor() {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading.set(true);
    this.error.set('');

    this.projectService.getProjects().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (projects) => {
        this.projects.set(projects);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading projects:', err);
        let errorMessage = 'Failed to load projects';

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

  onProjectCreated(): void {
    this.closeCreateModal();
    this.loadProjects();
  }

  startArchive(projectId: string): void {
    this.archivingProjectId.set(projectId);
  }

  closeArchiveDialog(): void {
    this.archivingProjectId.set(null);
  }

  confirmArchive(): void {
    const projectId = this.archivingProjectId();
    if (!projectId || this.processing()) return;

    this.processing.set(true);
    this.projectService.archiveProject(projectId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.closeArchiveDialog();
        this.loadProjects();
        this.processing.set(false);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to archive project');
        this.processing.set(false);
      }
    });
  }
}
