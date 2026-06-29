import {

  ChangeDetectionStrategy,

  Component,

  input,

  output,

} from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { Eye, Archive, LucideAngularModule, MoreHorizontal, Pencil } from 'lucide-angular';

import { ButtonDirective } from '@/shared/components/ui/button/button.directive';

import {

  DropdownMenuComponent,

  DropdownMenuItemComponent,

  DropdownMenuTriggerDirective,

} from '@/shared/components/ui/dropdown-menu/dropdown-menu.component';



@Component({

  selector: 'app-project-row-actions',

  imports: [

    CommonModule,
    RouterModule,

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

      <div dropdownContent class="w-44">

        <app-dropdown-menu-item [routerLink]="['/projects', projectId()]">

          <lucide-icon [img]="Eye" [size]="16" />

          {{ viewLabel() }}

        </app-dropdown-menu-item>

        <app-dropdown-menu-item [routerLink]="['/projects', projectId(), 'edit']">

          <lucide-icon [img]="Pencil" [size]="16" />

          {{ editLabel() }}

        </app-dropdown-menu-item>

        @if (showArchive()) {

          <app-dropdown-menu-item (click)="archive.emit(projectId())">

            <lucide-icon [img]="Archive" [size]="16" />

            {{ archiveLabel() }}

          </app-dropdown-menu-item>

        }

      </div>

    </app-dropdown-menu>

  `,

})

export class ProjectRowActionsComponent {

  readonly projectId = input.required<string>();

  readonly viewLabel = input('View');

  readonly editLabel = input('Edit');

  readonly archiveLabel = input('Archive');

  readonly showArchive = input(false);



  readonly archive = output<string>();



  readonly MoreHorizontal = MoreHorizontal;

  readonly Eye = Eye;

  readonly Pencil = Pencil;

  readonly Archive = Archive;

}


