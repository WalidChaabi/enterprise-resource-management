import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Table } from '@tanstack/angular-table';
import { LucideAngularModule, Settings2 } from 'lucide-angular';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import {
  DropdownMenuComponent,
  DropdownMenuCheckboxItemComponent,
  DropdownMenuLabelComponent,
  DropdownMenuSeparatorComponent,
  DropdownMenuTriggerDirective,
} from '@/shared/components/ui/dropdown-menu/dropdown-menu.component';

@Component({
  selector: 'app-data-table-view-options',
  imports: [
    CommonModule,
    LucideAngularModule,
    ButtonDirective,
    DropdownMenuComponent,
    DropdownMenuTriggerDirective,
    DropdownMenuLabelComponent,
    DropdownMenuSeparatorComponent,
    DropdownMenuCheckboxItemComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-dropdown-menu>
      <button
        dropdownTrigger
        app-button
        variant="outline"
        size="sm"
        type="button"
        class="ml-auto hidden h-8 gap-2 lg:flex">
        <lucide-icon [img]="Settings2" [size]="16" />
        View
      </button>
      <div dropdownContent class="w-[150px]">
        <app-dropdown-menu-label>Toggle columns</app-dropdown-menu-label>
        <app-dropdown-menu-separator />
        @for (column of hideableColumns(); track column.id) {
          <app-dropdown-menu-checkbox-item
            [checked]="column.getIsVisible()"
            (checkedChange)="column.toggleVisibility($event)">
            {{ column.id }}
          </app-dropdown-menu-checkbox-item>
        }
      </div>
    </app-dropdown-menu>
  `,
})
export class DataTableViewOptionsComponent<TData> {
  readonly table = input.required<Table<TData>>();
  readonly Settings2 = Settings2;

  readonly hideableColumns = computed(() =>
    this.table()
      .getAllColumns()
      .filter((column) => typeof column.accessorFn !== 'undefined' && column.getCanHide())
  );
}
