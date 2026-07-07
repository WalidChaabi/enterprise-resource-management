import {
  Component,
  effect,
  ElementRef,
  HostListener,
  inject,
  viewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { ButtonComponent } from '@/shared/components/ui/button/button.component';
import { ConfirmDialogService } from './confirm-dialog.service';
import { TranslationService } from '@/core/services/translation.service';

@Component({
  selector: 'app-confirm-dialog-host',
  imports: [CommonModule, LucideAngularModule, ButtonComponent],
  template: `
    @if (state(); as dialog) {
      <div
        class="fixed inset-0 z-[110] flex items-center justify-center p-4 sm:p-6"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        [attr.aria-describedby]="dialog.description ? 'confirm-dialog-description' : null">
        <div
          class="fixed inset-0 bg-black/60 backdrop-blur-sm"
          aria-hidden="true"
          (click)="cancel()">
        </div>
        <div
          #dialogContent
          tabindex="-1"
          class="relative z-50 w-full max-w-lg overflow-hidden rounded-xl border bg-background p-6 shadow-2xl"
          (click)="$event.stopPropagation()">
          <div class="flex items-start gap-4">
            @if (dialog.variant === 'destructive') {
              <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-destructive/10 text-destructive">
                <lucide-icon name="alert-triangle" [size]="20" />
              </div>
            }
            <div class="min-w-0 flex-1 space-y-2">
              <h2 id="confirm-dialog-title" class="text-lg font-semibold tracking-tight text-foreground">
                {{ dialog.title }}
              </h2>
              @if (dialog.description) {
                <p id="confirm-dialog-description" class="text-sm text-muted-foreground">
                  {{ dialog.description }}
                </p>
              }
            </div>
          </div>
          <div class="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            <app-button type="button" variant="outline" (click)="cancel()">
              {{ dialog.cancelLabel ?? defaultCancelLabel }}
            </app-button>
            <app-button
              type="button"
              [variant]="dialog.variant === 'destructive' ? 'destructive' : 'default'"
              (click)="confirm()">
              {{ dialog.confirmLabel ?? defaultConfirmLabel }}
            </app-button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ConfirmDialogHostComponent {
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly translation = inject(TranslationService);

  readonly state = this.confirmDialog.state;
  dialogContent = viewChild<ElementRef<HTMLDivElement>>('dialogContent');

  private previousBodyOverflow = '';

  readonly defaultCancelLabel = this.translation.translate('common.cancel');
  readonly defaultConfirmLabel = this.translation.translate('common.continue');

  constructor() {
    effect(() => {
      if (this.state()) {
        this.lockBodyScroll();
        queueMicrotask(() => this.focusDialog());
      } else {
        this.unlockBodyScroll();
      }
    });
  }

  confirm(): void {
    this.confirmDialog.confirm();
  }

  cancel(): void {
    this.confirmDialog.cancel();
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscape(event: KeyboardEvent): void {
    if (!this.state()) {
      return;
    }
    event.preventDefault();
    this.cancel();
  }

  private focusDialog(): void {
    this.dialogContent()?.nativeElement.focus();
  }

  private lockBodyScroll(): void {
    this.previousBodyOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
  }

  private unlockBodyScroll(): void {
    document.body.style.overflow = this.previousBodyOverflow;
  }
}
