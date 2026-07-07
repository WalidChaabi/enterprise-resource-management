import { signal } from '@angular/core';
import {
  type ColumnDef,
  type ColumnFiltersState,
  type SortingState,
  type VisibilityState,
  createAngularTable,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
} from '@tanstack/angular-table';

type Getter<T> = () => T;

interface EntityTableStateOptions<T> {
  data: Getter<T[]>;
  columns: Getter<ColumnDef<T, unknown>[]>;
  initialSorting: SortingState;
  matchesFilter: (item: T, query: string) => boolean;
}

export function createEntityTableState<T>(options: EntityTableStateOptions<T>) {
  const filterValue = signal('');
  const globalFilter = signal('');
  const sorting = signal<SortingState>(options.initialSorting);
  const columnFilters = signal<ColumnFiltersState>([]);
  const columnVisibility = signal<VisibilityState>({});

  const table = createAngularTable<T>(() => ({
    data: options.data(),
    columns: options.columns(),
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    onSortingChange: (updater) => {
      const next = typeof updater === 'function' ? updater(sorting()) : updater;
      sorting.set(next);
    },
    onColumnFiltersChange: (updater) => {
      const next = typeof updater === 'function' ? updater(columnFilters()) : updater;
      columnFilters.set(next);
    },
    onColumnVisibilityChange: (updater) => {
      const next = typeof updater === 'function' ? updater(columnVisibility()) : updater;
      columnVisibility.set(next);
    },
    globalFilterFn: (row, _columnId, filter) => {
      const query = String(filter).toLowerCase();
      if (!query) return true;
      return options.matchesFilter(row.original, query);
    },
    onGlobalFilterChange: (updater) => {
      const next = typeof updater === 'function' ? updater(globalFilter()) : updater;
      globalFilter.set(String(next ?? ''));
    },
    state: {
      sorting: sorting(),
      columnFilters: columnFilters(),
      columnVisibility: columnVisibility(),
      globalFilter: globalFilter(),
    },
  }));

  const onFilterChange = (value: string): void => {
    filterValue.set(value);
    globalFilter.set(value);
  };

  return {
    filterValue,
    globalFilter,
    sorting,
    columnFilters,
    columnVisibility,
    table,
    onFilterChange,
  };
}
