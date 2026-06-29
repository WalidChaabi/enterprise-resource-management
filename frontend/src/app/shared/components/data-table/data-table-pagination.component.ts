import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Table } from '@tanstack/angular-table';
import {
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  LucideAngularModule,
} from 'lucide-angular';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import {
  DropdownMenuComponent,
  DropdownMenuRadioItemComponent,
  DropdownMenuTriggerDirective,
} from '@/shared/components/ui/dropdown-menu/dropdown-menu.component';

@Component({
  selector: 'app-data-table-pagination',
  imports: [
    CommonModule,
    LucideAngularModule,
    ButtonDirective,
    DropdownMenuComponent,
    DropdownMenuTriggerDirective,
    DropdownMenuRadioItemComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex items-center justify-between gap-4 px-2">
      <div class="flex-1 text-sm text-muted-foreground">
        {{ table().getFilteredRowModel().rows.length }} of
        {{ table().getFilteredRowModel().rows.length }} row(s) visible.
      </div>
      <div class="flex flex-wrap items-center justify-end gap-4 sm:gap-6 lg:gap-8">
        <div class="flex items-center gap-2">
          <p class="text-sm font-medium whitespace-nowrap">Rows per page</p>
          <app-dropdown-menu [side]="'bottom'" [align]="'start'" contentClass="w-[70px] min-w-0 p-0.5">
            <button
              dropdownTrigger
              app-button
              variant="outline"
              size="sm"
              type="button"
              class="h-8 w-[70px] justify-between px-2 font-normal"
              aria-label="Rows per page">
              <span>{{ table().getState().pagination.pageSize }}</span>
              <lucide-icon [img]="ChevronDown" [size]="16" class="opacity-50" />
            </button>
            <div dropdownContent>
              @for (size of pageSizes; track size) {
                <app-dropdown-menu-radio-item
                  [compact]="true"
                  [selected]="table().getState().pagination.pageSize === size"
                  (selectedChange)="setPageSize(size)">
                  {{ size }}
                </app-dropdown-menu-radio-item>
              }
            </div>
          </app-dropdown-menu>
        </div>
        <div class="flex w-[100px] items-center justify-center text-sm font-medium whitespace-nowrap">
          Page {{ table().getState().pagination.pageIndex + 1 }} of
          {{ table().getPageCount() || 1 }}
        </div>
        <div class="flex items-center gap-1 pr-1">
          <button
            app-button
            variant="outline"
            size="icon"
            type="button"
            class="hidden h-8 w-8 lg:inline-flex"
            [disabled]="!table().getCanPreviousPage()"
            (click)="table().setPageIndex(0)"
            aria-label="Go to first page">
            <lucide-icon [img]="ChevronsLeft" [size]="16" />
          </button>
          <button
            app-button
            variant="outline"
            size="icon"
            type="button"
            class="h-8 w-8"
            [disabled]="!table().getCanPreviousPage()"
            (click)="table().previousPage()"
            aria-label="Go to previous page">
            <lucide-icon [img]="ChevronLeft" [size]="16" />
          </button>
          <button
            app-button
            variant="outline"
            size="icon"
            type="button"
            class="h-8 w-8"
            [disabled]="!table().getCanNextPage()"
            (click)="table().nextPage()"
            aria-label="Go to next page">
            <lucide-icon [img]="ChevronRight" [size]="16" />
          </button>
          <button
            app-button
            variant="outline"
            size="icon"
            type="button"
            class="hidden h-8 w-8 lg:inline-flex"
            [disabled]="!table().getCanNextPage()"
            (click)="table().setPageIndex(table().getPageCount() - 1)"
            aria-label="Go to last page">
            <lucide-icon [img]="ChevronsRight" [size]="16" />
          </button>
        </div>
      </div>
    </div>
  `,
})
export class DataTablePaginationComponent<TData> {
  readonly table = input.required<Table<TData>>();

  readonly pageSizes = [10, 20, 30, 40, 50];

  readonly ChevronsLeft = ChevronsLeft;
  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;
  readonly ChevronsRight = ChevronsRight;
  readonly ChevronDown = ChevronDown;

  setPageSize(size: number): void {
    this.table().setPageSize(size);
  }
}
