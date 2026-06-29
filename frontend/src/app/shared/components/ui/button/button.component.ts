import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { mergeClasses } from '@/shared/utils/merge-classes';
import { buttonVariants, ButtonSize, ButtonVariant } from './button.directive';

@Component({
  selector: 'app-button',
  imports: [CommonModule, RouterModule],
  host: {
    class: 'inline-flex',
  },
  template: `
    @if (routerLink()) {
      <a
        [routerLink]="routerLink()"
        [class]="classes()"
        [attr.aria-disabled]="disabled()"
        [attr.tabindex]="disabled() ? -1 : 0">
        <ng-content />
      </a>
    } @else {
      <button
        [type]="type()"
        [class]="classes()"
        [disabled]="disabled()"
        [attr.aria-disabled]="disabled()"
        [attr.aria-label]="ariaLabel() || undefined">
        <ng-content />
      </button>
    }
  `,
})
export class ButtonComponent {
  variant = input<ButtonVariant>('default');
  size = input<ButtonSize>('default');
  disabled = input<boolean>(false);
  type = input<'button' | 'submit' | 'reset'>('button');
  routerLink = input<string | string[] | null>(null);
  class = input<string>('');
  ariaLabel = input<string>('');

  classes(): string {
    return mergeClasses(
      buttonVariants({ variant: this.variant(), size: this.size() }),
      this.class()
    );
  }
}

export { ButtonDirective, buttonVariants } from './button.directive';
export type { ButtonVariant, ButtonSize } from './button.directive';
