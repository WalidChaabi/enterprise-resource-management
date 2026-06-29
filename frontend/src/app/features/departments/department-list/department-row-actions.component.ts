import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Eye, LucideAngularModule, MoreHorizontal, Pencil } from 'lucide-angular';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import {
  DropdownMenuComponent,
  DropdownMenuItemComponent,
  DropdownMenuTriggerDirective,
} from '@/shared/components/ui/dropdown-menu/dropdown-menu.component';

@Component({
  selector: 'app-department-row-actions',
  imports: [
    CommonModule,
    LucideAngularModule,
    ButtonDirective,
    DropdownMenuComponent,
    DropdownMenuTriggerDirective,
    DropdownMenuItemComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-dropdown-menu>
      <button
        dropdownTrigger
        app-button
        variant="ghost"
        size="icon"
        type="button"
        class="h-8 w-8"
        aria-label="Open menu">
        <lucide-icon [img]="MoreHorizontal" [size]="16" />
        <span class="sr-only">Open menu</span>
      </button>
      <div dropdownContent class="w-40">
        <app-dropdown-menu-item [routerLink]="['/departments', departmentId()]">
          <lucide-icon [img]="Eye" [size]="16" />
          {{ viewLabel() }}
        </app-dropdown-menu-item>
        <app-dropdown-menu-item [routerLink]="['/departments', departmentId(), 'edit']">
          <lucide-icon [img]="Pencil" [size]="16" />
          {{ editLabel() }}
        </app-dropdown-menu-item>
      </div>
    </app-dropdown-menu>
  `,
})
export class DepartmentRowActionsComponent {
  readonly departmentId = input.required<string>();
  readonly viewLabel = input('View');
  readonly editLabel = input('Edit');

  readonly MoreHorizontal = MoreHorizontal;
  readonly Eye = Eye;
  readonly Pencil = Pencil;
}
