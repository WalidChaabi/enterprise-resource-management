import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ProjectService } from '../../../core/services/project.service';
import { AuthService } from '../../../core/services/auth.service';
import { Project, ProjectStatus } from '../../../core/models/project.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { TranslationService } from '../../../core/services/translation.service';
import { ConfirmDialogService } from '../../../shared/components/ui/alert-dialog';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { EmptyStateComponent } from '../../../shared/components/ui/empty-state/empty-state.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { ListPageShellComponent } from '../../../shared/components/list-page-shell/list-page-shell.component';
import { ProjectFormComponent } from '../project-form/project-form.component';
import { ProjectDataTableComponent } from './project-data-table.component';
import {
  TabsComponent,
  TabsContentComponent,
  TabsListComponent,
  TabsTriggerComponent,
} from '../../../shared/components/ui/tabs';

export type ProjectStatusFilter = 'ALL' | ProjectStatus;

@Component({
  selector: 'app-project-list',
  imports: [
    CommonModule,
    CurrencyPipe,
    TranslatePipe,
    LucideAngularModule,
    ButtonComponent,
    CardComponent,
    CardContentComponent,
    SkeletonTableComponent,
    EmptyStateComponent,
    DialogComponent,
    ListPageShellComponent,
    ProjectFormComponent,
    ProjectDataTableComponent,
    TabsComponent,
    TabsListComponent,
    TabsTriggerComponent,
    TabsContentComponent,
  ],
  templateUrl: './project-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectListComponent {
  private readonly projectService = inject(ProjectService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly toastService = inject(ToastService);
  private readonly translation = inject(TranslationService);

  readonly projects = signal<Project[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);
  readonly processing = signal(false);
  readonly statusFilter = signal<ProjectStatusFilter>('ALL');

  readonly statusFilters: { value: ProjectStatusFilter; labelKey: string }[] = [
    { value: 'ALL', labelKey: 'projects.filters.all' },
    { value: ProjectStatus.ACTIVE, labelKey: 'projects.status.active' },
    { value: ProjectStatus.PLANNING, labelKey: 'projects.status.planning' },
    { value: ProjectStatus.ON_HOLD, labelKey: 'projects.status.on-hold' },
    { value: ProjectStatus.COMPLETED, labelKey: 'projects.status.completed' },
  ];

  readonly canCreateProject = computed(() => {
    return this.authService.hasAnyRole(ROLES.ORG_ADMIN, ROLES.PROJECT_MANAGER);
  });

  readonly canArchiveProject = computed(() => {
    return this.authService.hasAnyRole(ROLES.ORG_ADMIN);
  });

  readonly countByStatus = computed(() => {
    const items = this.projects();
    return {
      active: items.filter((p) => p.status === ProjectStatus.ACTIVE).length,
      planning: items.filter((p) => p.status === ProjectStatus.PLANNING).length,
      onHold: items.filter((p) => p.status === ProjectStatus.ON_HOLD).length,
      completed: items.filter((p) => p.status === ProjectStatus.COMPLETED).length,
    };
  });

  readonly totalBudget = computed(() =>
    this.projects().reduce((sum, project) => sum + (project.budget ?? 0), 0)
  );

  readonly filteredCount = computed(() => {
    const filter = this.statusFilter();
    if (filter === 'ALL') return this.projects().length;
    return this.projects().filter((project) => project.status === filter).length;
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

  onStatusTabChange(value: string): void {
    this.statusFilter.set(value as ProjectStatusFilter);
  }

  isStatusTabActive(filter: ProjectStatusFilter): boolean {
    return this.statusFilter() === filter;
  }

  statusCount(filter: ProjectStatusFilter): number {
    if (filter === 'ALL') return this.projects().length;
    return this.projects().filter((project) => project.status === filter).length;
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onProjectCreated(): void {
    this.closeCreateModal();
    this.toastService.successI18n('projects.toast.createdTitle', 'projects.toast.createdDescription');
    this.loadProjects();
  }

  async startArchive(projectId: string): Promise<void> {
    const confirmed = await this.confirmDialog.open({
      title: this.translation.translate('projects.archive'),
      description: this.translation.translate('projects.archiveConfirm'),
      confirmLabel: this.translation.translate('projects.confirmArchive'),
      cancelLabel: this.translation.translate('common.cancel'),
      variant: 'destructive',
    });
    if (!confirmed || this.processing()) {
      return;
    }

    this.processing.set(true);
    this.projectService.archiveProject(projectId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toastService.successI18n('projects.toast.archivedTitle', 'projects.toast.archivedDescription');
        this.loadProjects();
        this.processing.set(false);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to archive project');
        this.processing.set(false);
      },
    });
  }
}
