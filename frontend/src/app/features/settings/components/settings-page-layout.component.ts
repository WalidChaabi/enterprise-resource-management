import { Component, computed, input } from '@angular/core';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
  selector: 'app-settings-page-layout',
  template: `
    <div [class]="classes()">
      <ng-content />
    </div>
  `,
})
export class SettingsPageLayoutComponent {
  /** Use wider constraint for tables and multi-column layouts. */
  readonly wide = input(false);

  readonly classes = computed(() =>
    mergeClasses(
      'flex w-full flex-col gap-6',
      this.wide() ? 'max-w-5xl' : 'max-w-[900px]',
    ),
  );
}
