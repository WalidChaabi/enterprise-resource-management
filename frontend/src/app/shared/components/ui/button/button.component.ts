import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { cva, type VariantProps } from 'class-variance-authority';
import { mergeClasses } from '@/shared/utils/merge-classes';

const buttonVariants = cva(
  'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1 disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]',
  {
    variants: {
      variant: {
        default: 'bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm hover:shadow transition-shadow duration-150',
        destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90 shadow-sm hover:shadow transition-shadow duration-150',
        outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground hover:border-accent-foreground/20 transition-colors duration-150',
        secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80 shadow-sm hover:shadow transition-shadow duration-150',
        ghost: 'hover:bg-accent hover:text-accent-foreground transition-colors duration-150',
        link: 'text-primary underline-offset-4 hover:underline transition-colors duration-150',
      },
      size: {
        default: 'h-10 px-4 py-2',
        sm: 'h-9 rounded-md px-3',
        lg: 'h-11 rounded-md px-8',
        icon: 'h-10 w-10',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  }
);

export type ButtonVariant = VariantProps<typeof buttonVariants>['variant'];
export type ButtonSize = VariantProps<typeof buttonVariants>['size'];

@Component({
  selector: 'app-button, button[app-button]',
  standalone: true,
  imports: [CommonModule, RouterModule],
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
  host: {
    '[class]': 'classes()',
  },
})
export class ButtonComponent {
  variant = input<ButtonVariant>('default');
  size = input<ButtonSize>('default');
  disabled = input<boolean>(false);
  type = input<'button' | 'submit' | 'reset'>('button');
  routerLink = input<string | string[] | null>(null);
  class = input<string>('');
  ariaLabel = input<string>('');

  classes() {
    return mergeClasses(
      buttonVariants({ variant: this.variant(), size: this.size() }),
      this.class()
    );
  }
}
