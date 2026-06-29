import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Column } from '@tanstack/angular-table';
import {
  ArrowDown,
  ArrowUp,
  ArrowUpDown,
  ChevronsUpDown,
  EyeOff,
  LucideAngularModule,
} from 'lucide-angular';
import { mergeClasses } from '@/shared/utils/merge-classes';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import {
  DropdownMenuComponent,
  DropdownMenuItemComponent,
  DropdownMenuSeparatorComponent,
  DropdownMenuTriggerDirective,
} from '@/shared/components/ui/dropdown-menu/dropdown-menu.component';

@Component({
  selector: 'app-data-table-column-header',
  imports: [
    CommonModule,
    LucideAngularModule,
    ButtonDirective,
    DropdownMenuComponent,
    DropdownMenuTriggerDirective,
    DropdownMenuItemComponent,
    DropdownMenuSeparatorComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (!column().getCanSort()) {
      <div [class]="mergeClasses('flex items-center', className())">{{ title() }}</div>
    } @else if (variant() === 'toggle') {
      <button
        app-button
        variant="ghost"
        size="sm"
        type="button"
        class="-ml-3 h-8 px-2 lg:h-8"
        (click)="column().toggleSorting(column().getIsSorted() === 'asc')">
        <span>{{ title() }}</span>
        <lucide-icon [img]="ArrowUpDown" [size]="16" class="ml-2" />
      </button>
    } @else {
      <app-dropdown-menu>
        <button
          dropdownTrigger
          app-button
          variant="ghost"
          size="sm"
          type="button"
          class="-ml-3 h-8 px-2 lg:h-8">
          <span>{{ title() }}</span>
          @if (column().getIsSorted() === 'desc') {
            <lucide-icon [img]="ArrowDown" [size]="16" class="ml-2" />
          } @else if (column().getIsSorted() === 'asc') {
            <lucide-icon [img]="ArrowUp" [size]="16" class="ml-2" />
          } @else {
            <lucide-icon [img]="ChevronsUpDown" [size]="16" class="ml-2 opacity-50" />
          }
        </button>
        <div dropdownContent>
          <app-dropdown-menu-item (activated)="sortAsc($event)">
            <lucide-icon [img]="ArrowUp" [size]="16" />
            Asc
          </app-dropdown-menu-item>
          <app-dropdown-menu-item (activated)="sortDesc($event)">
            <lucide-icon [img]="ArrowDown" [size]="16" />
            Desc
          </app-dropdown-menu-item>
          @if (column().getCanHide()) {
            <app-dropdown-menu-separator />
            <app-dropdown-menu-item (activated)="hideColumn($event)">
              <lucide-icon [img]="EyeOff" [size]="16" />
              Hide
            </app-dropdown-menu-item>
          }
        </div>
      </app-dropdown-menu>
    }
  `,
})
export class DataTableColumnHeaderComponent<TData, TValue> {
  readonly column = input.required<Column<TData, TValue>>();
  readonly title = input.required<string>();
  readonly variant = input<'toggle' | 'menu'>('menu');
  readonly className = input('', { alias: 'class' });

  readonly ArrowDown = ArrowDown;
  readonly ArrowUp = ArrowUp;
  readonly ArrowUpDown = ArrowUpDown;
  readonly ChevronsUpDown = ChevronsUpDown;
  readonly EyeOff = EyeOff;

  mergeClasses = mergeClasses;

  sortAsc(event: MouseEvent): void {
    event.stopPropagation();
    this.column().toggleSorting(false);
  }

  sortDesc(event: MouseEvent): void {
    event.stopPropagation();
    this.column().toggleSorting(true);
  }

  hideColumn(event: MouseEvent): void {
    event.stopPropagation();
    this.column().toggleVisibility(false);
  }
}
