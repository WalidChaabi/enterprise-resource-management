import { cva, type VariantProps } from 'class-variance-authority';

export const switchTrackVariants = cva(
  'peer inline-flex shrink-0 cursor-pointer items-center rounded-full border border-transparent shadow-xs transition-all outline-none focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50',
  {
    variants: {
      size: {
        default: 'h-[1.15rem] w-8',
        sm: 'h-3.5 w-6',
      },
      checked: {
        true: 'bg-primary',
        false: 'bg-input dark:bg-input/80',
      },
    },
    defaultVariants: {
      size: 'default',
      checked: false,
    },
  },
);

export const switchThumbVariants = cva(
  'pointer-events-none block rounded-full bg-background ring-0 transition-transform dark:data-[state=checked]:bg-primary-foreground dark:data-[state=unchecked]:bg-foreground',
  {
    variants: {
      size: {
        default: 'size-4',
        sm: 'size-3',
      },
      checked: {
        true: 'translate-x-[calc(100%-2px)]',
        false: 'translate-x-0',
      },
    },
    defaultVariants: {
      size: 'default',
      checked: false,
    },
  },
);

export type SwitchSize = NonNullable<VariantProps<typeof switchTrackVariants>['size']>;
