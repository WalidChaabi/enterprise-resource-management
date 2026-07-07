import {
  ChangeDetectionStrategy,
  Component,
  TemplateRef,
  computed,
  input,
  output,
  viewChild,
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import {
  CellContext,
  ColumnDef,
  HeaderContext,
  SortingState,
} from '@tanstack/angular-table';
import {
  LeaveRequest,
  LeaveRequestStatus,
  LeaveRequestStatusFilter,
  LeaveType,
} from '@/core/models/leave-request.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { BadgeComponent, BadgeVariant } from '@/shared/components/ui/badge/badge.component';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { EntityDataTableComponent } from '@/shared/components/entity-data-table/entity-data-table.component';
import { createEntityTableState } from '@/shared/components/entity-data-table/entity-table-state';
import { LeaveRequestRowActionsComponent } from './leave-request-row-actions.component';

@Component({
  selector: 'app-leave-request-data-table',
  imports: [
    CommonModule,
    TranslatePipe,
    DatePipe,
    BadgeComponent,
    EntityDataTableComponent,
    DataTableColumnHeaderComponent,
    LeaveRequestRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './leave-request-data-table.component.html',
})
export class LeaveRequestDataTableComponent {
  readonly data = input.required<LeaveRequest[]>();
  readonly canApprove = input(false);
  readonly processingId = input<string | null>(null);
  readonly statusFilter = input<LeaveRequestStatusFilter>('ALL');

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

  readonly filteredData = computed(() => {
    const filter = this.statusFilter();
    if (filter === 'ALL') return this.data();
    return this.data().filter((request) => request.status === filter);
  });

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
      sortingFn: 'datetime',
    },
    {
      accessorKey: 'endDate',
      header: () => this.endDateHeader(),
      cell: () => this.endDateCell(),
      enableSorting: true,
      sortingFn: 'datetime',
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

  private readonly tableState = createEntityTableState<LeaveRequest>({
    data: () => this.filteredData(),
    columns: () => this.columns(),
    initialSorting: [{ id: 'startDate', desc: true }] as SortingState,
    matchesFilter: (request, query) =>
      [
        request.employeeName,
        request.leaveType,
        request.status,
        request.reason,
        request.startDate,
        request.endDate,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(query),
  });

  readonly filterValue = this.tableState.filterValue;
  readonly table = this.tableState.table;
  readonly onFilterChange = this.tableState.onFilterChange;

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

  statusLabelKey(status: LeaveRequestStatus): string {
    return `leaveRequests.statuses.${status.toLowerCase()}`;
  }

  leaveTypeLabelKey(type: LeaveType): string {
    return `leaveRequests.types.${type.toLowerCase()}`;
  }

  canApproveRequest(request: LeaveRequest): boolean {
    return this.canApprove() && request.status === LeaveRequestStatus.PENDING;
  }

  canEditRequest(request: LeaveRequest): boolean {
    return request.status === LeaveRequestStatus.PENDING;
  }

  canCancelRequest(request: LeaveRequest): boolean {
    return request.status === LeaveRequestStatus.PENDING;
  }

  hasRowActions(request: LeaveRequest): boolean {
    return this.canEditRequest(request) || this.canApproveRequest(request) || this.canCancelRequest(request);
  }
}
