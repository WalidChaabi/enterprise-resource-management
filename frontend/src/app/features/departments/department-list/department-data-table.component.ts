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
import { Department } from '@/core/models/department.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { DataTableColumnHeaderComponent } from '@/shared/components/data-table/data-table-column-header.component';
import { DataTablePaginationComponent } from '@/shared/components/data-table/data-table-pagination.component';
import { DataTableViewOptionsComponent } from '@/shared/components/data-table/data-table-view-options.component';
import { DepartmentRowActionsComponent } from './department-row-actions.component';

@Component({
  selector: 'app-department-data-table',
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    FlexRenderDirective,
    TranslatePipe,
    DataTableColumnHeaderComponent,
    DataTablePaginationComponent,
    DataTableViewOptionsComponent,
    DepartmentRowActionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './department-data-table.component.html',
})
export class DepartmentDataTableComponent {
  readonly data = input.required<Department[]>();

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

  readonly filterValue = signal('');
  readonly sorting = signal<SortingState>([]);
  readonly columnFilters = signal<ColumnFiltersState>([]);
  readonly columnVisibility = signal<VisibilityState>({});

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

  readonly table = createAngularTable<Department>(() => ({
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
}
