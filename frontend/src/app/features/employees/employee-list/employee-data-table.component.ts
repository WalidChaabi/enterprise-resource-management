import {
  ChangeDetectionStrategy,
  Component,
  TemplateRef,
  computed,
  input,
  viewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  CellContext,
  ColumnDef,
  HeaderContext,
  SortingState,
} from '@tanstack/angular-table';
import { Employee } from '@/core/models/employee.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { BadgeComponent } from '@/shared/components/ui/badge/badge.component';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { EntityDataTableComponent } from '@/shared/components/entity-data-table/entity-data-table.component';
import { createEntityTableState } from '@/shared/components/entity-data-table/entity-table-state';
import { EmployeeRowActionsComponent } from './employee-row-actions.component';
import { EmployeeStatusFilter } from './employee-list.component';

@Component({
  selector: 'app-employee-data-table',
  imports: [
    CommonModule,
    RouterModule,
    TranslatePipe,
    BadgeComponent,
    EntityDataTableComponent,
    DataTableColumnHeaderComponent,
    EmployeeRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './employee-data-table.component.html',
})
export class EmployeeDataTableComponent {
  readonly data = input.required<Employee[]>();
  readonly statusFilter = input<EmployeeStatusFilter>('ALL');

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

  readonly filteredData = computed(() => {
    const filter = this.statusFilter();
    if (filter === 'ALL') return this.data();
    if (filter === 'ACTIVE') return this.data().filter((e) => e.active);
    return this.data().filter((e) => !e.active);
  });

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

  private readonly tableState = createEntityTableState<Employee>({
    data: () => this.filteredData(),
    columns: () => this.columns(),
    initialSorting: [{ id: 'name', desc: false }] as SortingState,
    matchesFilter: (employee, query) =>
      [
        employee.firstName,
        employee.lastName,
        employee.email,
        employee.jobTitle,
        employee.departmentName,
        employee.employeeCode,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(query),
  });

  readonly filterValue = this.tableState.filterValue;
  readonly table = this.tableState.table;
  readonly onFilterChange = this.tableState.onFilterChange;

  initials(employee: Employee): string {
    const first = employee.firstName?.charAt(0) ?? '';
    const last = employee.lastName?.charAt(0) ?? '';
    return `${first}${last}`.toUpperCase() || '?';
  }
}
