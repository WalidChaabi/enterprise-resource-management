import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { cva, type VariantProps } from 'class-variance-authority';
import { mergeClasses } from '@/shared/utils/merge-classes';

const alertVariants = cva(
  'relative w-full rounded-lg border p-4 [&>svg~*]:pl-7 [&>svg+div]:translate-y-[-3px] [&>svg]:absolute [&>svg]:left-4 [&>svg]:top-4 [&>svg]:text-foreground',
  {
    variants: {
      variant: {
        default: 'bg-background text-foreground',
        destructive: 'border-destructive/50 text-destructive dark:border-destructive [&>svg]:text-destructive',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  }
);

export type AlertVariant = VariantProps<typeof alertVariants>['variant'];

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [class]="classes()" [attr.role]="role()">
      <ng-content />
    </div>
  `,
})
export class AlertComponent {
  variant = input<AlertVariant>('default');
  role = input<string>('alert');
  class = input<string>('');

  classes() {
    return mergeClasses(alertVariants({ variant: this.variant() }), this.class());
  }
}

@Component({
  selector: 'app-alert-title',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h5 [class]="mergeClasses('mb-1 font-medium leading-none tracking-tight', class())">
      <ng-content />
    </h5>
  `,
})
export class AlertTitleComponent {
  class = input<string>('');
  mergeClasses = mergeClasses;
}

@Component({
  selector: 'app-alert-description',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [class]="mergeClasses('text-sm [&_p]:leading-relaxed', class())">
      <ng-content />
    </div>
  `,
})
export class AlertDescriptionComponent {
  class = input<string>('');
  mergeClasses = mergeClasses;
}
