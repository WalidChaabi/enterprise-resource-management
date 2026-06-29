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
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import {
  CellContext,
  ColumnDef,
  ColumnFiltersState,
  FlexRenderDirective,
  HeaderContext,
  SortingState,
  VisibilityState,
  createAngularTable,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
} from '@tanstack/angular-table';
import { FormsModule } from '@angular/forms';
import { TimeEntry, TimeEntryStatus } from '@/core/models/time-entry.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { BadgeComponent, BadgeVariant } from '@/shared/components/ui/badge/badge.component';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { DataTablePaginationComponent } from '@/shared/components/data-table/data-table-pagination.component';
import { DataTableViewOptionsComponent } from '@/shared/components/data-table/data-table-view-options.component';
import { TimeEntryRowActionsComponent } from './time-entry-row-actions.component';

@Component({
  selector: 'app-time-entry-data-table',
  imports: [
    CommonModule,
    FormsModule,
    FlexRenderDirective,
    TranslatePipe,
    DatePipe,
    DecimalPipe,
    BadgeComponent,
    DataTableColumnHeaderComponent,
    DataTablePaginationComponent,
    DataTableViewOptionsComponent,
    TimeEntryRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './time-entry-data-table.component.html',
})
export class TimeEntryDataTableComponent {
  readonly data = input.required<TimeEntry[]>();
  readonly canApprove = input(false);
  readonly processingId = input<string | null>(null);

  readonly submit = output<TimeEntry>();
  readonly approve = output<TimeEntry>();
  readonly reject = output<TimeEntry>();

  readonly dateHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('dateHeader');
  readonly projectHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('projectHeader');
  readonly hoursHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('hoursHeader');
  readonly statusHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('statusHeader');
  readonly actionsHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('actionsHeader');

  readonly dateCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('dateCell');
  readonly projectCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('projectCell');
  readonly hoursCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('hoursCell');
  readonly statusCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('statusCell');
  readonly actionsCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('actionsCell');

  readonly filterValue = signal('');
  readonly sorting = signal<SortingState>([]);
  readonly columnFilters = signal<ColumnFiltersState>([]);
  readonly columnVisibility = signal<VisibilityState>({});

  readonly columns = computed<ColumnDef<TimeEntry, unknown>[]>(() => [
    {
      accessorKey: 'date',
      header: () => this.dateHeader(),
      cell: () => this.dateCell(),
      enableSorting: true,
    },
    {
      id: 'project',
      accessorFn: (row) => row.projectName ?? '',
      header: () => this.projectHeader(),
      cell: () => this.projectCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'hours',
      header: () => this.hoursHeader(),
      cell: () => this.hoursCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'status',
      header: () => this.statusHeader(),
      cell: () => this.statusCell(),
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

  readonly table = createAngularTable<TimeEntry>(() => ({
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
    this.table.getColumn('project')?.setFilterValue(value);
  }

  statusVariant(status: TimeEntryStatus): BadgeVariant {
    switch (status) {
      case TimeEntryStatus.APPROVED:
        return 'default';
      case TimeEntryStatus.SUBMITTED:
        return 'secondary';
      case TimeEntryStatus.REJECTED:
        return 'destructive';
      default:
        return 'outline';
    }
  }

  canSubmitEntry(entry: TimeEntry): boolean {
    return entry.status === TimeEntryStatus.DRAFT;
  }

  canApproveEntry(entry: TimeEntry): boolean {
    return this.canApprove() && entry.status === TimeEntryStatus.SUBMITTED;
  }

  canEditEntry(entry: TimeEntry): boolean {
    return entry.status === TimeEntryStatus.DRAFT || entry.status === TimeEntryStatus.REJECTED;
  }
}
