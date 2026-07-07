import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FlexRenderDirective } from '@tanstack/angular-table';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { DataTablePaginationComponent } from '@/shared/components/data-table/data-table-pagination.component';
import { DataTableViewOptionsComponent } from '@/shared/components/data-table/data-table-view-options.component';

@Component({
  selector: 'app-entity-data-table',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    FlexRenderDirective,
    TranslatePipe,
    DataTablePaginationComponent,
    DataTableViewOptionsComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="w-full">
      <div class="flex items-center justify-between gap-4">
        <input
          type="search"
          class="flex h-9 w-full max-w-sm rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          [placeholder]="filterPlaceholderKey() | translate"
          [ngModel]="filterValue()"
          (ngModelChange)="filterChange.emit($event)" />
        <app-data-table-view-options [table]="table()" />
      </div>

      <div class="mt-4 overflow-hidden rounded-md border">
        <div class="relative w-full overflow-auto">
          <table class="w-full caption-bottom text-sm">
            <thead class="[&_tr]:border-b">
              @for (headerGroup of table().getHeaderGroups(); track headerGroup.id) {
                <tr class="border-b transition-colors">
                  @for (header of headerGroup.headers; track header.id) {
                    <th
                      class="h-9 px-3 text-left align-middle text-xs font-medium text-muted-foreground"
                      [class.text-right]="isRightAligned(header.column.id)">
                      @if (!header.isPlaceholder) {
                        <ng-container *flexRender="header.column.columnDef.header; props: header.getContext()" />
                      }
                    </th>
                  }
                </tr>
              }
            </thead>
            <tbody class="[&_tr:last-child]:border-0">
              @if (table().getRowModel().rows.length) {
                @for (row of table().getRowModel().rows; track row.id) {
                  <tr class="border-b transition-colors hover:bg-muted/50">
                    @for (cell of row.getVisibleCells(); track cell.id) {
                      <td
                        class="px-3 py-2.5 align-middle"
                        [class.text-right]="isRightAligned(cell.column.id)">
                        <ng-container *flexRender="cell.column.columnDef.cell; props: cell.getContext()" />
                      </td>
                    }
                  </tr>
                }
              } @else {
                <tr>
                  <td
                    class="h-20 px-3 py-2.5 text-center align-middle text-sm text-foreground/60"
                    [attr.colspan]="table().getAllColumns().length">
                    {{ noResultsKey() | translate }}
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>

      <div class="mt-4">
        <app-data-table-pagination [table]="table()" />
      </div>
    </div>
  `,
})
export class EntityDataTableComponent {
  readonly table = input.required<any>();
  readonly filterValue = input<string>('');
  readonly filterPlaceholderKey = input.required<string>();
  readonly noResultsKey = input.required<string>();
  readonly rightAlignColumnIds = input<string[]>(['actions']);
  readonly filterChange = output<string>();

  isRightAligned(columnId: string): boolean {
    return this.rightAlignColumnIds().includes(columnId);
  }
}
