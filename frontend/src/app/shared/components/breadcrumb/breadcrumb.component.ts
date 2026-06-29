import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { mergeClasses } from '@/shared/utils/merge-classes';

export interface BreadcrumbItem {
  label: string;
  route?: string | string[];
}

@Component({
    selector: 'app-breadcrumb',
    imports: [CommonModule, RouterModule, LucideAngularModule],
    template: `
    <nav class="flex items-center space-x-2 text-sm text-muted-foreground" aria-label="Breadcrumb">
      <ol class="flex items-center space-x-2">
        @for (item of items(); track $index) {
          <li class="flex items-center">
            @if ($index > 0) {
              <lucide-icon name="chevron-right" [size]="16" class="mx-2" />
            }
            @if (item.route && $index < items().length - 1) {
              <a 
                [routerLink]="item.route"
                class="hover:text-foreground transition-colors">
                {{ item.label }}
              </a>
            } @else {
              <span [class]="mergeClasses($index === items().length - 1 ? 'text-foreground font-medium' : '', '')">
                {{ item.label }}
              </span>
            }
          </li>
        }
      </ol>
    </nav>
  `
})
export class BreadcrumbComponent {
  items = input.required<BreadcrumbItem[]>();
  mergeClasses = mergeClasses;
}
