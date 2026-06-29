import {
  Component,
  Directive,
  ElementRef,
  HostListener,
  inject,
  input,
  OnDestroy,
  signal,
  viewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { mergeClasses } from '@/shared/utils/merge-classes';
import {
  computeFloatingPanelPosition,
  FloatingPanelPosition,
} from '@/shared/utils/floating-panel.position';
import { FloatingPanelRegistry } from '@/shared/utils/floating-panel.registry';

@Component({
  selector: 'app-popover',
  imports: [CommonModule],
  template: `
    <div [class]="rootClasses()">
      <ng-content select="[popoverTrigger]" />
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
      (click)="$event.stopPropagation()">
      <ng-content select="[popoverContent]" />
    </div>
  `,
})
export class PopoverComponent implements OnDestroy {
  readonly open = signal(false);
  readonly side = input<'top' | 'bottom' | 'left' | 'right'>('bottom');
  readonly align = input<'start' | 'center' | 'end'>('start');
  readonly contentClass = input('', { alias: 'contentClass' });
  readonly fullWidth = input(true);

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
    const alignClasses: Record<string, string> = {
      start: '',
      center: '',
      end: '',
    };

    return mergeClasses(
      'fixed z-[200] rounded-md border bg-popover p-0 text-popover-foreground shadow-lg outline-none animate-in fade-in-0 zoom-in-95',
      alignClasses[this.align()],
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

    this.position.set(
      computeFloatingPanelPosition(
        this.triggerElement,
        this.panel()?.nativeElement ?? null,
        resolvedSide,
        resolvedAlign
      )
    );
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
  selector: '[popoverTrigger]',
  standalone: true,
})
export class PopoverTriggerDirective {
  private readonly popover = inject(PopoverComponent);
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  @HostListener('click', ['$event'])
  onClick(event: MouseEvent): void {
    event.stopPropagation();
    this.popover.toggle(this.elementRef.nativeElement);
  }
}
