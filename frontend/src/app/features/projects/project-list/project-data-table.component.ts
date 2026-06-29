import {
  ChangeDetectionStrategy,
  Component,
  TemplateRef,
  computed,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  CellContext,
  ColumnDef,
  ColumnFiltersState,
  HeaderContext,
  SortingState,
  VisibilityState,
  createAngularTable,
  FlexRenderDirective,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
} from '@tanstack/angular-table';
import { FormsModule } from '@angular/forms';
import { Project, ProjectStatus } from '@/core/models/project.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { BadgeComponent, BadgeVariant } from '@/shared/components/ui/badge/badge.component';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { DataTablePaginationComponent } from '@/shared/components/data-table/data-table-pagination.component';
import { DataTableViewOptionsComponent } from '@/shared/components/data-table/data-table-view-options.component';
import { ProjectRowActionsComponent } from './project-row-actions.component';

@Component({
  selector: 'app-project-data-table',
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    FlexRenderDirective,
    TranslatePipe,
    CurrencyPipe,
    DatePipe,
    BadgeComponent,
    DataTableColumnHeaderComponent,
    DataTablePaginationComponent,
    DataTableViewOptionsComponent,
    ProjectRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './project-data-table.component.html',
})
export class ProjectDataTableComponent {
  readonly data = input.required<Project[]>();
  readonly canCreate = input(false);
  readonly canArchive = input(false);
  readonly createRequested = output<void>();
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

  readonly filterValue = signal('');
  readonly sorting = signal<SortingState>([]);
  readonly columnFilters = signal<ColumnFiltersState>([]);
  readonly columnVisibility = signal<VisibilityState>({});

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
    },
    {
      accessorKey: 'deadline',
      header: () => this.deadlineHeader(),
      cell: () => this.deadlineCell(),
      enableSorting: true,
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

  readonly table = createAngularTable<Project>(() => ({
    data: this.data(),
    columns: this.columns(),
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    onSortingChange: (updater) => {
      const next = typeof updater === 'function' ? updater(this.sorting()) : updater;
      this.sorting.set(next);
    },
    onColumnFiltersChange: (updater) => {
      const next = typeof updater === 'function' ? updater(this.columnFilters()) : updater;
      this.columnFilters.set(next);
    },
    onColumnVisibilityChange: (updater) => {
      const next = typeof updater === 'function' ? updater(this.columnVisibility()) : updater;
      this.columnVisibility.set(next);
    },
    state: {
      sorting: this.sorting(),
      columnFilters: this.columnFilters(),
      columnVisibility: this.columnVisibility(),
    },
  }));

  onFilterChange(value: string): void {
    this.filterValue.set(value);
    this.table.getColumn('name')?.setFilterValue(value);
  }

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
