import {
  Component,
  Directive,
  ElementRef,
  HostListener,
  inject,
  input,
  OnDestroy,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Check, LucideAngularModule } from 'lucide-angular';
import { mergeClasses } from '@/shared/utils/merge-classes';
import {
  computeFloatingPanelPosition,
  FloatingPanelPosition,
} from '@/shared/utils/floating-panel.position';
import { FloatingPanelRegistry } from '@/shared/utils/floating-panel.registry';

@Component({
  selector: 'app-dropdown-menu',
  imports: [CommonModule],
  template: `
    <div [class]="rootClasses()">
      <ng-content select="[dropdownTrigger]" />
    </div>

    <div
      class="fixed inset-0 z-[150]"
      [class.hidden]="!open()"
      aria-hidden="true"
      (click)="close()"></div>
    <div
      #panel
      [class]="panelClasses()"
      [class.hidden]="!open()"
      [attr.aria-hidden]="!open()"
      [style.top.px]="position().top"
      [style.left.px]="position().width ? position().left : null"
      [style.width.px]="position().width || null"
      (click)="$event.stopPropagation()">
      <ng-content select="[dropdownContent]" />
    </div>
  `,
})
export class DropdownMenuComponent implements OnDestroy {
  readonly open = signal(false);
  readonly side = input<'top' | 'bottom' | 'left' | 'right'>('bottom');
  readonly align = input<'start' | 'center' | 'end'>('end');
  readonly contentClass = input('', { alias: 'contentClass' });
  readonly fullWidth = input(false);

  readonly position = signal<FloatingPanelPosition>({ top: 0, left: 0, width: 0 });

  private readonly panel = viewChild<ElementRef<HTMLElement>>('panel');
  private readonly registry = inject(FloatingPanelRegistry);
  private triggerElement: HTMLElement | null = null;
  private readonly handle = { close: () => this.close() };

  rootClasses(): string {
    return mergeClasses(
      'relative text-left',
      this.fullWidth() ? 'block w-full' : 'inline-block'
    );
  }

  panelClasses(): string {
    return mergeClasses(
      'fixed z-[200] flex flex-col overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-lg animate-in fade-in-0 zoom-in-95',
      this.contentClass() ? '' : 'min-w-[8rem]',
      this.contentClass()
    );
  }

  toggle(trigger?: HTMLElement): void {
    if (this.open()) {
      this.close();
      return;
    }

    if (trigger) {
      this.triggerElement = trigger;
    }

    this.registry.notifyOpen(this.handle);
    this.open.set(true);
    this.scheduleReposition();
  }

  close(): void {
    if (this.open()) {
      this.registry.notifyClose();
    }
    this.open.set(false);
    this.triggerElement = null;
  }

  ngOnDestroy(): void {
    if (this.open()) {
      this.registry.notifyClose();
    }
    this.registry.unregister(this.handle);
    document.removeEventListener('scroll', this.onScroll, true);
  }

  constructor() {
    this.registry.register(this.handle);
    document.addEventListener('scroll', this.onScroll, true);
  }

  private readonly onScroll = (): void => {
    this.updatePosition();
  };

  private scheduleReposition(): void {
    requestAnimationFrame(() => {
      this.updatePosition();
      requestAnimationFrame(() => this.updatePosition());
    });
  }

  private updatePosition(): void {
    if (!this.open() || !this.triggerElement) {
      return;
    }

    const resolvedSide = this.side() === 'top' ? 'top' : 'bottom';
    const resolvedAlign =
      this.align() === 'center' ? 'center' : this.align() === 'start' ? 'start' : 'end';

    const next = computeFloatingPanelPosition(
      this.triggerElement,
      this.panel()?.nativeElement ?? null,
      resolvedSide,
      resolvedAlign
    );

    if (this.fullWidth()) {
      next.width = Math.max(next.width, 224);
    }

    this.position.set(next);
  }

  @HostListener('window:resize')
  onResize(): void {
    this.updatePosition();
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscape(event: KeyboardEvent): void {
    if (!this.open()) {
      return;
    }
    event.stopPropagation();
    this.close();
  }
}

@Directive({
  selector: '[dropdownTrigger]',
  standalone: true,
})
export class DropdownMenuTriggerDirective {
  private readonly menu = inject(DropdownMenuComponent);
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  @HostListener('click', ['$event'])
  onClick(event: MouseEvent): void {
    event.stopPropagation();
    this.menu.toggle(this.elementRef.nativeElement);
  }
}

@Component({
  selector: 'app-dropdown-menu-label',
  imports: [CommonModule],
  template: `
    <div class="px-2 py-1.5 text-sm font-semibold">
      <ng-content />
    </div>
  `,
})
export class DropdownMenuLabelComponent {}

@Component({
  selector: 'app-dropdown-menu-separator',
  imports: [CommonModule],
  template: `<div class="-mx-1 my-1 h-px bg-muted"></div>`,
})
export class DropdownMenuSeparatorComponent {}

@Component({
  selector: 'app-dropdown-menu-item',
  imports: [CommonModule, RouterModule],
  host: {
    class: 'block w-full',
  },
  template: `
    @if (routerLink()) {
      <a
        [routerLink]="routerLink()!"
        class="relative flex w-full cursor-default select-none items-center gap-2 rounded-sm px-2 py-1.5 text-sm text-popover-foreground outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground"
        (click)="handleClick($event)">
        <ng-content />
      </a>
    } @else {
      <button
        type="button"
        class="relative flex w-full cursor-default select-none items-center gap-2 rounded-sm px-2 py-1.5 text-sm text-popover-foreground outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground"
        (click)="handleClick($event)">
        <ng-content />
      </button>
    }
  `,
})
export class DropdownMenuItemComponent {
  routerLink = input<string | string[] | null>(null);
  activated = output<MouseEvent>();

  private readonly menu = inject(DropdownMenuComponent, { optional: true });

  handleClick(event: MouseEvent): void {
    this.activated.emit(event);
    this.menu?.close();
  }
}

@Component({
  selector: 'app-dropdown-menu-checkbox-item',
  imports: [CommonModule],
  template: `
    <label
      class="relative flex w-full cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none transition-colors hover:bg-accent hover:text-accent-foreground">
      <span class="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
        <input
          type="checkbox"
          class="h-4 w-4 rounded border border-primary accent-primary"
          [checked]="checked()"
          (change)="onChange($event)" />
      </span>
      <span class="capitalize"><ng-content /></span>
    </label>
  `,
})
export class DropdownMenuCheckboxItemComponent {
  checked = input(false);
  checkedChange = output<boolean>();

  onChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.checkedChange.emit(target.checked);
  }
}

@Component({
  selector: 'app-dropdown-menu-radio-item',
  imports: [CommonModule, LucideAngularModule],
  template: `
    <button
      type="button"
      [class]="itemClasses()"
      (click)="handleClick($event)">
      <span [class]="indicatorClasses()">
        @if (selected()) {
          <lucide-icon [img]="Check" [size]="14" />
        }
      </span>
      <ng-content />
    </button>
  `,
})
export class DropdownMenuRadioItemComponent {
  selected = input(false);
  selectedChange = output<void>();
  compact = input(false);
  className = input('', { alias: 'class' });

  readonly Check = Check;

  private readonly menu = inject(DropdownMenuComponent, { optional: true });

  itemClasses(): string {
    return mergeClasses(
      'relative flex w-full cursor-default select-none items-center rounded-sm text-sm outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground',
      this.compact() ? 'py-1 pl-6 pr-2' : 'py-1.5 pl-8 pr-2',
      this.className()
    );
  }

  indicatorClasses(): string {
    return this.compact()
      ? 'absolute left-1.5 flex h-3.5 w-3.5 items-center justify-center'
      : 'absolute left-2 flex h-3.5 w-3.5 items-center justify-center';
  }

  handleClick(event: MouseEvent): void {
    event.stopPropagation();
    this.selectedChange.emit();
    this.menu?.close();
  }
}
