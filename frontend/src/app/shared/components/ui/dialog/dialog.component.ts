import {
  Component,
  effect,
  ElementRef,
  HostListener,
  inject,
  input,
  output,
  viewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { mergeClasses } from '@/shared/utils/merge-classes';
import { FloatingPanelRegistry } from '@/shared/utils/floating-panel.registry';

let dialogIdCounter = 0;

@Component({
  selector: 'app-dialog',
  imports: [CommonModule, LucideAngularModule],
  template: `
    @if (open()) {
      <div
        class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6"
        role="dialog"
        aria-modal="true"
        [attr.aria-labelledby]="title() ? titleId : null"
        (click)="onBackdropClick()">
        <div
          class="fixed inset-0 bg-black/60 backdrop-blur-sm"
          aria-hidden="true">
        </div>
        <div
          #dialogContent
          tabindex="-1"
          class="relative z-50 flex w-full max-w-xl flex-col overflow-hidden rounded-xl border bg-background shadow-2xl"
          [class]="contentClass()"
          (click)="$event.stopPropagation()">
          <div class="flex shrink-0 items-center justify-between gap-4 border-b px-6 py-4">
            @if (title()) {
              <h2 [id]="titleId" class="text-lg font-semibold tracking-tight">{{ title() }}</h2>
            }
            <button
              (click)="close()"
              class="ml-auto rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
              type="button"
              aria-label="Close">
              <lucide-icon name="x" [size]="16" />
            </button>
          </div>
          <div class="dialog-body min-h-0 max-h-[min(85vh,720px)] overflow-y-auto overflow-x-visible px-6 py-5">
            <ng-content />
          </div>
        </div>
      </div>
    }
  `,
})
export class DialogComponent {
  open = input<boolean>(false);
  title = input<string>('');
  contentClass = input<string>('');
  closeOnBackdrop = input<boolean>(true);

  closed = output<void>();

  dialogContent = viewChild<ElementRef<HTMLDivElement>>('dialogContent');
  mergeClasses = mergeClasses;

  readonly titleId = `dialog-title-${++dialogIdCounter}`;

  private readonly floatingPanels = inject(FloatingPanelRegistry);
  private previousBodyOverflow = '';

  constructor() {
    effect(() => {
      if (this.open()) {
        this.lockBodyScroll();
        queueMicrotask(() => this.focusDialog());
      } else {
        this.unlockBodyScroll();
      }
    });
  }

  @HostListener('keydown', ['$event'])
  onDialogKeydown(event: KeyboardEvent): void {
    if (!this.open() || event.key !== 'Tab') {
      return;
    }

    const root = this.dialogContent()?.nativeElement;
    if (!root) {
      return;
    }

    const focusable = this.getFocusableElements(root);
    if (focusable.length === 0) {
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    const active = document.activeElement;

    if (event.shiftKey && active === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && active === last) {
      event.preventDefault();
      first.focus();
    }
  }

  private focusDialog(): void {
    const root = this.dialogContent()?.nativeElement;
    if (!root) {
      return;
    }

    const focusable = this.getFocusableElements(root);
    if (focusable.length > 0) {
      focusable[0].focus();
    } else {
      root.focus();
    }
  }

  private getFocusableElements(root: HTMLElement): HTMLElement[] {
    return Array.from(
      root.querySelectorAll<HTMLElement>(
        'a[href], button:not([disabled]), textarea, input, select, [tabindex]:not([tabindex="-1"])'
      )
    ).filter((el) => !el.hasAttribute('disabled') && el.offsetParent !== null);
  }

  close(): void {
    this.closed.emit();
  }

  onBackdropClick(): void {
    if (this.closeOnBackdrop()) {
      this.close();
    }
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscape(event: KeyboardEvent): void {
    if (!this.open()) {
      return;
    }

    if (this.floatingPanels.hasOpenPanels()) {
      return;
    }

    event.preventDefault();
    this.close();
  }

  private lockBodyScroll(): void {
    this.previousBodyOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
  }

  private unlockBodyScroll(): void {
    document.body.style.overflow = this.previousBodyOverflow;
  }
}

@Component({
  selector: 'app-dialog-trigger',
  imports: [CommonModule],
  template: `
    <button
      [type]="type()"
      [class]="mergeClasses('inline-flex items-center justify-center', buttonClass())"
      (click)="clicked.emit()">
      <ng-content />
    </button>
  `,
})
export class DialogTriggerComponent {
  clicked = output<void>();
  type = input<'button' | 'submit' | 'reset'>('button');
  buttonClass = input<string>('');
  mergeClasses = mergeClasses;
}
