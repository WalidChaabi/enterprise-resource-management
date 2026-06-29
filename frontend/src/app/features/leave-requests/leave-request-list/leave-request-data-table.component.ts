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
import { CommonModule, DatePipe } from '@angular/common';
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
import { LeaveRequest, LeaveRequestStatus } from '@/core/models/leave-request.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { BadgeComponent, BadgeVariant } from '@/shared/components/ui/badge/badge.component';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { DataTablePaginationComponent } from '@/shared/components/data-table/data-table-pagination.component';
import { DataTableViewOptionsComponent } from '@/shared/components/data-table/data-table-view-options.component';
import { LeaveRequestRowActionsComponent } from './leave-request-row-actions.component';

@Component({
  selector: 'app-leave-request-data-table',
  imports: [
    CommonModule,
    FormsModule,
    FlexRenderDirective,
    TranslatePipe,
    DatePipe,
    BadgeComponent,
    DataTableColumnHeaderComponent,
    DataTablePaginationComponent,
    DataTableViewOptionsComponent,
    LeaveRequestRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './leave-request-data-table.component.html',
})
export class LeaveRequestDataTableComponent {
  readonly data = input.required<LeaveRequest[]>();
  readonly canApprove = input(false);
  readonly processingId = input<string | null>(null);

  readonly approve = output<LeaveRequest>();
  readonly reject = output<LeaveRequest>();
  readonly cancelRequest = output<LeaveRequest>();

  readonly employeeHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<LeaveRequest, unknown> }>>('employeeHeader');
  readonly typeHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<LeaveRequest, unknown> }>>('typeHeader');
  readonly startDateHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<LeaveRequest, unknown> }>>('startDateHeader');
  readonly endDateHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<LeaveRequest, unknown> }>>('endDateHeader');
  readonly daysHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<LeaveRequest, unknown> }>>('daysHeader');
  readonly statusHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<LeaveRequest, unknown> }>>('statusHeader');
  readonly actionsHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<LeaveRequest, unknown> }>>('actionsHeader');

  readonly employeeCell = viewChild.required<TemplateRef<{ $implicit: CellContext<LeaveRequest, unknown> }>>('employeeCell');
  readonly typeCell = viewChild.required<TemplateRef<{ $implicit: CellContext<LeaveRequest, unknown> }>>('typeCell');
  readonly startDateCell = viewChild.required<TemplateRef<{ $implicit: CellContext<LeaveRequest, unknown> }>>('startDateCell');
  readonly endDateCell = viewChild.required<TemplateRef<{ $implicit: CellContext<LeaveRequest, unknown> }>>('endDateCell');
  readonly daysCell = viewChild.required<TemplateRef<{ $implicit: CellContext<LeaveRequest, unknown> }>>('daysCell');
  readonly statusCell = viewChild.required<TemplateRef<{ $implicit: CellContext<LeaveRequest, unknown> }>>('statusCell');
  readonly actionsCell = viewChild.required<TemplateRef<{ $implicit: CellContext<LeaveRequest, unknown> }>>('actionsCell');

  readonly filterValue = signal('');
  readonly sorting = signal<SortingState>([]);
  readonly columnFilters = signal<ColumnFiltersState>([]);
  readonly columnVisibility = signal<VisibilityState>({});

  readonly columns = computed<ColumnDef<LeaveRequest, unknown>[]>(() => [
    {
      id: 'employee',
      accessorFn: (row) => row.employeeName ?? '',
      header: () => this.employeeHeader(),
      cell: () => this.employeeCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'leaveType',
      header: () => this.typeHeader(),
      cell: () => this.typeCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'startDate',
      header: () => this.startDateHeader(),
      cell: () => this.startDateCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'endDate',
      header: () => this.endDateHeader(),
      cell: () => this.endDateCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'days',
      header: () => this.daysHeader(),
      cell: () => this.daysCell(),
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

  readonly table = createAngularTable<LeaveRequest>(() => ({
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
    this.table.getColumn('employee')?.setFilterValue(value);
  }

  statusVariant(status: LeaveRequestStatus): BadgeVariant {
    switch (status) {
      case LeaveRequestStatus.APPROVED:
        return 'default';
      case LeaveRequestStatus.PENDING:
        return 'secondary';
      case LeaveRequestStatus.REJECTED:
      case LeaveRequestStatus.CANCELLED:
        return 'destructive';
      default:
        return 'outline';
    }
  }

  canApproveRequest(request: LeaveRequest): boolean {
    return this.canApprove() && request.status === LeaveRequestStatus.PENDING;
  }

  canCancelRequest(request: LeaveRequest): boolean {
    return request.status === LeaveRequestStatus.PENDING;
  }
}
