import {
  ChangeDetectionStrategy,
  Component,
  TemplateRef,
  computed,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
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
import { Employee } from '@/core/models/employee.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { BadgeComponent } from '@/shared/components/ui/badge/badge.component';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { DataTablePaginationComponent } from '@/shared/components/data-table/data-table-pagination.component';
import { DataTableViewOptionsComponent } from '@/shared/components/data-table/data-table-view-options.component';
import { EmployeeRowActionsComponent } from './employee-row-actions.component';

@Component({
  selector: 'app-employee-data-table',
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    FlexRenderDirective,
    TranslatePipe,
    BadgeComponent,
    DataTableColumnHeaderComponent,
    DataTablePaginationComponent,
    DataTableViewOptionsComponent,
    EmployeeRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './employee-data-table.component.html',
})
export class EmployeeDataTableComponent {
  readonly data = input.required<Employee[]>();

  readonly nameHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Employee, unknown> }>>('nameHeader');
  readonly emailHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Employee, unknown> }>>('emailHeader');
  readonly jobTitleHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Employee, unknown> }>>('jobTitleHeader');
  readonly departmentHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Employee, unknown> }>>('departmentHeader');
  readonly statusHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Employee, unknown> }>>('statusHeader');
  readonly actionsHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Employee, unknown> }>>('actionsHeader');

  readonly nameCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Employee, unknown> }>>('nameCell');
  readonly emailCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Employee, unknown> }>>('emailCell');
  readonly jobTitleCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Employee, unknown> }>>('jobTitleCell');
  readonly departmentCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Employee, unknown> }>>('departmentCell');
  readonly statusCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Employee, unknown> }>>('statusCell');
  readonly actionsCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Employee, unknown> }>>('actionsCell');

  readonly filterValue = signal('');
  readonly sorting = signal<SortingState>([]);
  readonly columnFilters = signal<ColumnFiltersState>([]);
  readonly columnVisibility = signal<VisibilityState>({});

  readonly columns = computed<ColumnDef<Employee, unknown>[]>(() => [
    {
      id: 'name',
      accessorFn: (row) => `${row.firstName ?? ''} ${row.lastName ?? ''}`.trim(),
      header: () => this.nameHeader(),
      cell: () => this.nameCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'email',
      header: () => this.emailHeader(),
      cell: () => this.emailCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'jobTitle',
      header: () => this.jobTitleHeader(),
      cell: () => this.jobTitleCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'departmentName',
      header: () => this.departmentHeader(),
      cell: () => this.departmentCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'active',
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

  readonly table = createAngularTable<Employee>(() => ({
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

  initials(employee: Employee): string {
    const first = employee.firstName?.charAt(0) ?? '';
    const last = employee.lastName?.charAt(0) ?? '';
    return `${first}${last}`.toUpperCase();
  }
}
