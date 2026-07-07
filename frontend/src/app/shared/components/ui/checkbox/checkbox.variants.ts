import { cva, type VariantProps } from 'class-variance-authority';

export const checkboxVariants = cva(
  'peer inline-flex shrink-0 cursor-pointer items-center justify-center rounded-[4px] border border-input bg-background shadow-xs transition-shadow outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 dark:bg-input/30 data-[state=checked]:border-primary data-[state=checked]:bg-primary data-[state=checked]:text-primary-foreground dark:data-[state=checked]:bg-primary aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40',
  {
    variants: {
      size: {
        default: 'size-4',
        sm: 'size-3.5',
      },
    },
    defaultVariants: {
      size: 'default',
    },
  },
);

export type CheckboxSize = NonNullable<VariantProps<typeof checkboxVariants>['size']>;
