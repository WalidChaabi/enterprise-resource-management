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
import { Department } from '@/core/models/department.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { EntityDataTableComponent } from '@/shared/components/entity-data-table/entity-data-table.component';
import { createEntityTableState } from '@/shared/components/entity-data-table/entity-table-state';
import { DepartmentRowActionsComponent } from './department-row-actions.component';
import { DepartmentFilter } from './department-list.component';

@Component({
  selector: 'app-department-data-table',
  imports: [
    CommonModule,
    RouterModule,
    TranslatePipe,
    EntityDataTableComponent,
    DataTableColumnHeaderComponent,
    DepartmentRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './department-data-table.component.html',
})
export class DepartmentDataTableComponent {
  readonly data = input.required<Department[]>();
  readonly departmentFilter = input<DepartmentFilter>('ALL');

  readonly nameHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Department, unknown> }>>('nameHeader');
  readonly descriptionHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Department, unknown> }>>('descriptionHeader');
  readonly managerHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Department, unknown> }>>('managerHeader');
  readonly employeesHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Department, unknown> }>>('employeesHeader');
  readonly actionsHeader = viewChild.required<TemplateRef<{ $implicit: HeaderContext<Department, unknown> }>>('actionsHeader');

  readonly nameCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Department, unknown> }>>('nameCell');
  readonly descriptionCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Department, unknown> }>>('descriptionCell');
  readonly managerCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Department, unknown> }>>('managerCell');
  readonly employeesCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Department, unknown> }>>('employeesCell');
  readonly actionsCell = viewChild.required<TemplateRef<{ $implicit: CellContext<Department, unknown> }>>('actionsCell');

  readonly filteredData = computed(() => {
    const filter = this.departmentFilter();
    if (filter === 'ALL') return this.data();
    if (filter === 'STAFFED') return this.data().filter((d) => (d.employeeCount ?? 0) > 0);
    return this.data().filter((d) => (d.employeeCount ?? 0) === 0);
  });

  readonly columns = computed<ColumnDef<Department, unknown>[]>(() => [
    {
      accessorKey: 'name',
      header: () => this.nameHeader(),
      cell: () => this.nameCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'description',
      header: () => this.descriptionHeader(),
      cell: () => this.descriptionCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'managerName',
      header: () => this.managerHeader(),
      cell: () => this.managerCell(),
      enableSorting: true,
    },
    {
      accessorKey: 'employeeCount',
      header: () => this.employeesHeader(),
      cell: () => this.employeesCell(),
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

  private readonly tableState = createEntityTableState<Department>({
    data: () => this.filteredData(),
    columns: () => this.columns(),
    initialSorting: [{ id: 'name', desc: false }] as SortingState,
    matchesFilter: (dept, query) =>
      [dept.name, dept.description, dept.managerName]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(query),
  });

  readonly filterValue = this.tableState.filterValue;
  readonly table = this.tableState.table;
  readonly onFilterChange = this.tableState.onFilterChange;
}
