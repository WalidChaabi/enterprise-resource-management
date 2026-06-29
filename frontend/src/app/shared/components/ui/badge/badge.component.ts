import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { cva, type VariantProps } from 'class-variance-authority';
import { mergeClasses } from '@/shared/utils/merge-classes';

const badgeVariants = cva(
  'inline-flex items-center rounded-[6px] border px-2 py-0.5 text-xs font-medium transition-colors duration-150',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary/15 text-primary',
        secondary: 'border-transparent bg-muted text-muted-foreground',
        destructive: 'border-transparent bg-destructive/15 text-destructive',
        outline: 'border-subtle text-muted-foreground bg-transparent',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  }
);

export type BadgeVariant = VariantProps<typeof badgeVariants>['variant'];

@Component({
    selector: 'app-badge',
    imports: [CommonModule],
    template: `
    <div [class]="classes()">
      <ng-content />
    </div>
  `
})
export class BadgeComponent {
  variant = input<BadgeVariant>('default');
  class = input<string>('');

  classes() {
    return mergeClasses(badgeVariants({ variant: this.variant() }), this.class());
  }
}
