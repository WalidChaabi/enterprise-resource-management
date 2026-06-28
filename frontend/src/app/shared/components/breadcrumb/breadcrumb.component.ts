import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { mergeClasses } from '@/shared/utils/merge-classes';

export interface BreadcrumbItem {
  label: string;
  route?: string | string[];
}

@Component({
    selector: 'app-breadcrumb',
    imports: [CommonModule, RouterModule],
    template: `
    <nav class="flex items-center space-x-2 text-sm text-muted-foreground" aria-label="Breadcrumb">
      <ol class="flex items-center space-x-2">
        @for (item of items(); track $index) {
          <li class="flex items-center">
            @if ($index > 0) {
              <svg class="h-4 w-4 mx-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
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
