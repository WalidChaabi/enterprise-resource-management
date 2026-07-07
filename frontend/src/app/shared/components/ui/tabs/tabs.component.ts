import {
  ChangeDetectionStrategy,
  Component,
  Injectable,
  OnInit,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { cva } from 'class-variance-authority';
import { mergeClasses } from '@/shared/utils/merge-classes';

export type TabsVariant = 'default' | 'line';

export const tabsListVariants = cva('', {
  variants: {
    variant: {
      default:
        'inline-flex h-9 w-fit max-w-full items-center justify-center rounded-lg bg-muted p-1 text-muted-foreground',
      line: 'inline-flex h-9 w-full items-center justify-start gap-0 overflow-x-auto rounded-none border-b border-border bg-transparent p-0 text-muted-foreground',
    },
  },
  defaultVariants: {
    variant: 'default',
  },
});

export const tabsTriggerVariants = cva(
  'inline-flex shrink-0 items-center justify-center gap-1.5 whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-0 disabled:pointer-events-none disabled:opacity-50',
  {
    variants: {
      variant: {
        default: '',
        line: 'rounded-none border-b-2 border-transparent px-3 py-2 -mb-px shadow-none',
      },
      active: {
        true: '',
        false: '',
      },
    },
    compoundVariants: [
      {
        variant: 'default',
        active: true,
        class: 'bg-background text-foreground shadow-sm',
      },
      {
        variant: 'default',
        active: false,
        class: 'text-muted-foreground hover:text-foreground',
      },
      {
        variant: 'line',
        active: true,
        class: 'border-primary text-foreground',
      },
      {
        variant: 'line',
        active: false,
        class: 'text-muted-foreground hover:text-foreground',
      },
    ],
    defaultVariants: {
      variant: 'default',
      active: false,
    },
  }
);

@Injectable()
export class TabsContext {
  readonly value = signal<string>('');
  valueChange?: (value: string) => void;

  select(value: string): void {
    this.value.set(value);
    this.valueChange?.(value);
  }
}

@Component({
  selector: 'app-tabs',
  imports: [CommonModule],
  providers: [TabsContext],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[class]': 'hostClasses()',
  },
  template: `<ng-content />`,
})
export class TabsComponent implements OnInit {
  private readonly ctx = inject(TabsContext);

  readonly value = input.required<string>();
  readonly class = input<string>('');
  readonly valueChange = output<string>();

  readonly hostClasses = computed(() =>
    mergeClasses('flex flex-col gap-2', this.class())
  );

  constructor() {
    effect(() => {
      this.ctx.value.set(this.value());
    });
  }

  ngOnInit(): void {
    this.ctx.valueChange = (value) => this.valueChange.emit(value);
  }
}

@Component({
  selector: 'app-tabs-list',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<div [class]="classes()" role="tablist"><ng-content /></div>`,
})
export class TabsListComponent {
  readonly variant = input<TabsVariant>('default');
  readonly class = input<string>('');

  readonly classes = computed(() =>
    mergeClasses(tabsListVariants({ variant: this.variant() }), this.class())
  );
}

@Component({
  selector: 'app-tabs-trigger',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button
      type="button"
      role="tab"
      [attr.aria-selected]="isActive()"
      [attr.data-state]="isActive() ? 'active' : 'inactive'"
      [disabled]="disabled()"
      [class]="classes()"
      (click)="select()">
      <ng-content />
    </button>
  `,
})
export class TabsTriggerComponent {
  private readonly ctx = inject(TabsContext);

  readonly value = input.required<string>();
  readonly variant = input<TabsVariant>('default');
  readonly disabled = input(false);
  readonly class = input<string>('');

  readonly isActive = computed(() => this.ctx.value() === this.value());

  readonly classes = computed(() =>
    mergeClasses(
      tabsTriggerVariants({
        variant: this.variant(),
        active: this.isActive(),
      }),
      this.class()
    )
  );

  select(): void {
    if (!this.disabled()) {
      this.ctx.select(this.value());
    }
  }
}

@Component({
  selector: 'app-tabs-content',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (isActive()) {
      <div
        role="tabpanel"
        [attr.data-state]="'active'"
        [attr.aria-labelledby]="value()"
        [class]="classes()">
        <ng-content />
      </div>
    }
  `,
})
export class TabsContentComponent {
  private readonly ctx = inject(TabsContext);

  readonly value = input.required<string>();
  readonly class = input<string>('');

  readonly isActive = computed(() => this.ctx.value() === this.value());

  readonly classes = computed(() =>
    mergeClasses(
      'mt-0 ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
      this.class()
    )
  );
}
