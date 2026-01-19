import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [class]="mergeClasses('flex flex-col items-center justify-center py-12 px-4 text-center', containerClass())">
      @if (icon()) {
        <div class="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-muted">
          <span [innerHTML]="icon()" class="h-6 w-6 text-muted-foreground"></span>
        </div>
      }
      <h3 [class]="mergeClasses('text-base font-semibold text-foreground mb-1', titleClass())">
        <ng-content select="[title]"></ng-content>
        @if (!hasTitleContent()) {
          {{ title() }}
        }
      </h3>
      <p [class]="mergeClasses('text-sm text-muted-foreground max-w-sm', descriptionClass())">
        <ng-content select="[description]"></ng-content>
        @if (!hasDescriptionContent()) {
          {{ description() }}
        }
      </p>
      @if (actionLabel()) {
        <div class="mt-6">
          <ng-content select="[action]"></ng-content>
        </div>
      }
    </div>
  `,
})
export class EmptyStateComponent {
  title = input<string>('');
  description = input<string>('');
  icon = input<string | null>(null);
  actionLabel = input<string>('');
  containerClass = input<string>('');
  titleClass = input<string>('');
  descriptionClass = input<string>('');
  mergeClasses = mergeClasses;

  hasTitleContent(): boolean {
    // This is a simple check - in a real scenario, you might use ContentChild
    return false;
  }

  hasDescriptionContent(): boolean {
    // This is a simple check - in a real scenario, you might use ContentChild
    return false;
  }
}
