import {
  ChangeDetectionStrategy,
  Component,
  TemplateRef,
  computed,
  input,
  output,
  viewChild,
} from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  CellContext,
  ColumnDef,
  HeaderContext,
  SortingState,
} from '@tanstack/angular-table';
import { Project, ProjectStatus } from '@/core/models/project.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { BadgeComponent, BadgeVariant } from '@/shared/components/ui/badge/badge.component';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { EntityDataTableComponent } from '@/shared/components/entity-data-table/entity-data-table.component';
import { createEntityTableState } from '@/shared/components/entity-data-table/entity-table-state';
import { ProjectRowActionsComponent } from './project-row-actions.component';
import { ProjectStatusFilter } from './project-list.component';

@Component({
  selector: 'app-project-data-table',
  imports: [
    CommonModule,
    RouterModule,
    TranslatePipe,
    CurrencyPipe,
    DatePipe,
    BadgeComponent,
    EntityDataTableComponent,
    DataTableColumnHeaderComponent,
    ProjectRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './project-data-table.component.html',
})
export class ProjectDataTableComponent {
  readonly data = input.required<Project[]>();
  readonly statusFilter = input<ProjectStatusFilter>('ALL');
  readonly canArchive = input(false);
  readonly archiveRequested = output<string>();

  readonly nameHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Project, unknown> }>>('nameHeader');
  readonly statusHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Project, unknown> }>>('statusHeader');
  readonly membersHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Project, unknown> }>>('membersHeader');
  readonly startDateHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Project, unknown> }>>('startDateHeader');
  readonly deadlineHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Project, unknown> }>>('deadlineHeader');
  readonly budgetHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Project, unknown> }>>('budgetHeader');
  readonly actionsHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Project, unknown> }>>('actionsHeader');

  readonly nameCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Project, unknown> }>>('nameCell');
  readonly statusCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Project, unknown> }>>('statusCell');
  readonly membersCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Project, unknown> }>>('membersCell');
  readonly startDateCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Project, unknown> }>>('startDateCell');
  readonly deadlineCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Project, unknown> }>>('deadlineCell');
  readonly budgetCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Project, unknown> }>>('budgetCell');
  readonly actionsCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Project, unknown> }>>('actionsCell');

  readonly filteredData = computed(() => {
    const filter = this.statusFilter();
    if (filter === 'ALL') return this.data();
    return this.data().filter((project) => project.status === filter);
  });

  readonly columns = computed<ColumnDef<Project, unknown>[]>(() => [
    {
      accessorKey: 'name',
      header: () => this.nameHeader(),
      cell: () => this.nameCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'status',
      header: () => this.statusHeader(),
      cell: () => this.statusCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'membersCount',
      header: () => this.membersHeader(),
      cell: () => this.membersCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'startDate',
      header: () => this.startDateHeader(),
      cell: () => this.startDateCell(),
      enableSorting: true,
      sortingFn: 'datetime',
    },
    {
      accessorKey: 'deadline',
      header: () => this.deadlineHeader(),
      cell: () => this.deadlineCell(),
      enableSorting: true,
      sortingFn: 'datetime',
    },
    {
      accessorKey: 'budget',
      header: () => this.budgetHeader(),
      cell: () => this.budgetCell(),
      enableSorting: true,
    },
    {
      id: 'actions',
      header: () => this.actionsHeader(),
      cell: () => this.actionsCell(),
      enableSorting: false,
      enableHiding: false,
    },
  ]);

  private readonly tableState = createEntityTableState<Project>({
    data: () => this.filteredData(),
    columns: () => this.columns(),
    initialSorting: [{ id: 'name', desc: false }] as SortingState,
    matchesFilter: (project, query) =>
      [project.name, project.description, project.managerName, project.status]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(query),
  });

  readonly filterValue = this.tableState.filterValue;
  readonly table = this.tableState.table;
  readonly onFilterChange = this.tableState.onFilterChange;

  getStatusVariant(status: ProjectStatus): BadgeVariant {
    switch (status) {
      case ProjectStatus.ACTIVE:
        return 'default';
      case ProjectStatus.COMPLETED:
        return 'secondary';
      case ProjectStatus.CANCELLED:
      case ProjectStatus.ON_HOLD:
        return 'destructive';
      default:
        return 'outline';
    }
  }

  statusKey(status: ProjectStatus): string {
    return status.toLowerCase().replace('_', '-');
  }
}
