import { Component, input, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
  selector: 'app-input-group',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [class]="mergeClasses('flex items-center rounded-md border border-input bg-background overflow-hidden', class())">
      @if (addonBefore()) {
        <div class="flex items-center px-3 py-2 text-sm text-muted-foreground border-r border-input bg-muted/50">
          <ng-container *ngTemplateOutlet="addonBefore()!" />
        </div>
      }
      <div class="flex-1 min-w-0">
        <ng-content />
      </div>
      @if (addonAfter()) {
        <div class="flex items-center px-3 py-2 text-sm text-muted-foreground border-l border-input bg-muted/50">
          <ng-container *ngTemplateOutlet="addonAfter()!" />
        </div>
      }
    </div>
  `,
})
export class InputGroupComponent {
  addonBefore = input<TemplateRef<any>>();
  addonAfter = input<TemplateRef<any>>();
  class = input<string>('');
  mergeClasses = mergeClasses;
}
