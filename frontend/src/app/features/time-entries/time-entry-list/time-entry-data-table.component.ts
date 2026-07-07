import {
  ChangeDetectionStrategy,
  Component,
  TemplateRef,
  computed,
  input,
  output,
  viewChild,
} from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import {
  CellContext,
  ColumnDef,
  HeaderContext,
  SortingState,
} from '@tanstack/angular-table';
import { TimeEntry, TimeEntryStatus, TimeEntryStatusFilter } from '@/core/models/time-entry.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { BadgeComponent, BadgeVariant } from '@/shared/components/ui/badge/badge.component';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { EntityDataTableComponent } from '@/shared/components/entity-data-table/entity-data-table.component';
import { createEntityTableState } from '@/shared/components/entity-data-table/entity-table-state';
import { TimeEntryRowActionsComponent } from './time-entry-row-actions.component';

@Component({
  selector: 'app-time-entry-data-table',
  imports: [
    CommonModule,
    TranslatePipe,
    DatePipe,
    DecimalPipe,
    BadgeComponent,
    EntityDataTableComponent,
    DataTableColumnHeaderComponent,
    TimeEntryRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './time-entry-data-table.component.html',
})
export class TimeEntryDataTableComponent {
  readonly data = input.required<TimeEntry[]>();
  readonly canApprove = input(false);
  readonly processingId = input<string | null>(null);
  readonly statusFilter = input<TimeEntryStatusFilter>('ALL');

  readonly submitEntry = output<TimeEntry>();
  readonly approve = output<TimeEntry>();
  readonly reject = output<TimeEntry>();

  readonly dateHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('dateHeader');
  readonly projectHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('projectHeader');
  readonly timeHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('timeHeader');
  readonly hoursHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('hoursHeader');
  readonly employeeHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('employeeHeader');
  readonly statusHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('statusHeader');
  readonly actionsHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<TimeEntry, unknown> }>>('actionsHeader');

  readonly dateCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('dateCell');
  readonly projectCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('projectCell');
  readonly timeCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('timeCell');
  readonly hoursCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('hoursCell');
  readonly employeeCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('employeeCell');
  readonly statusCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('statusCell');
  readonly actionsCell = viewChild.required<TemplateRef<{ $implicit: CellContext<TimeEntry, unknown> }>>('actionsCell');

  readonly filteredData = computed(() => {
    const filter = this.statusFilter();
    if (filter === 'ALL') return this.data();
    return this.data().filter((entry) => entry.status === filter);
  });

  readonly columns = computed<ColumnDef<TimeEntry, unknown>[]>(() => {
    const cols: ColumnDef<TimeEntry, unknown>[] = [
      {
        accessorKey: 'date',
        header: () => this.dateHeader(),
        cell: () => this.dateCell(),
        enableSorting: true,
        sortingFn: 'datetime',
      },
      {
        id: 'project',
        accessorFn: (row) => `${row.projectName ?? ''} ${row.taskTitle ?? ''} ${row.description ?? ''}`,
        header: () => this.projectHeader(),
        cell: () => this.projectCell(),
        enableSorting: true,
      },
      {
        id: 'timeRange',
        accessorFn: (row) => `${row.startTime ?? ''}${row.endTime ?? ''}`,
        header: () => this.timeHeader(),
        cell: () => this.timeCell(),
        enableSorting: false,
      },
      {
        accessorKey: 'hours',
        header: () => this.hoursHeader(),
        cell: () => this.hoursCell(),
        enableSorting: true,
      },
    ];

    if (this.canApprove()) {
      cols.push({
        id: 'employee',
        accessorFn: (row) => row.employeeName ?? '',
        header: () => this.employeeHeader(),
        cell: () => this.employeeCell(),
        enableSorting: true,
      });
    }

    cols.push(
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
      }
    );

    return cols;
  });

  private readonly tableState = createEntityTableState<TimeEntry>({
    data: () => this.filteredData(),
    columns: () => this.columns(),
    initialSorting: [{ id: 'date', desc: true }] as SortingState,
    matchesFilter: (entry, query) =>
      [
        entry.projectName,
        entry.taskTitle,
        entry.description,
        entry.employeeName,
        entry.status,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(query),
  });

  readonly filterValue = this.tableState.filterValue;
  readonly table = this.tableState.table;
  readonly onFilterChange = this.tableState.onFilterChange;

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

  statusLabelKey(status: TimeEntryStatus): string {
    return `timeEntries.statuses.${status.toLowerCase()}`;
  }

  formatTimeRange(entry: TimeEntry): string {
    if (entry.startTime && entry.endTime) {
      return `${entry.startTime} – ${entry.endTime}`;
    }
    return '—';
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

  hasRowActions(entry: TimeEntry): boolean {
    return this.canEditEntry(entry) || this.canSubmitEntry(entry) || this.canApproveEntry(entry);
  }
}
