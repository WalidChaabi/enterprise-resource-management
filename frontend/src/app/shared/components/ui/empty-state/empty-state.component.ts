import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div [class]="mergeClasses('flex flex-col items-center justify-center py-12 px-4 text-center', containerClass())">
      @if (iconName()) {
        <div class="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-muted">
          <lucide-icon [name]="iconName()!" [size]="iconSize()" class="text-muted-foreground" />
        </div>
      }

      @if (title()) {
        <h3 [class]="mergeClasses('mb-1 text-base font-semibold text-foreground', titleClass())">
          {{ title() }}
        </h3>
      } @else {
        <div [class]="mergeClasses('mb-1 text-base font-semibold text-foreground', titleClass())">
          <ng-content select="[title]" />
        </div>
      }

      @if (description()) {
        <p [class]="mergeClasses('max-w-sm text-sm text-muted-foreground', descriptionClass())">
          {{ description() }}
        </p>
      } @else {
        <div [class]="mergeClasses('max-w-sm text-sm text-muted-foreground', descriptionClass())">
          <ng-content select="[description]" />
        </div>
      }

      <div class="mt-6">
        <ng-content select="[action]" />
      </div>
    </div>
  `,
})
export class EmptyStateComponent {
  title = input<string>('');
  description = input<string>('');
  iconName = input<string | null>(null);
  iconSize = input<number>(24);
  containerClass = input<string>('');
  titleClass = input<string>('');
  descriptionClass = input<string>('');
  mergeClasses = mergeClasses;

}
