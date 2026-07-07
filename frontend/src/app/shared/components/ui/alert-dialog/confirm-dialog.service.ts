import { Injectable, signal } from '@angular/core';

export type ConfirmDialogVariant = 'default' | 'destructive';

export interface ConfirmDialogOptions {
  title: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: ConfirmDialogVariant;
}

export interface ConfirmDialogState extends ConfirmDialogOptions {
  open: boolean;
  variant: ConfirmDialogVariant;
}

/**
 * Promise-based confirmation dialogs for destructive or irreversible actions.
 * Use ToastService for action feedback; use app-dialog for forms and multi-field flows.
 */
@Injectable({
  providedIn: 'root',
})
export class ConfirmDialogService {
  private resolvePending: ((confirmed: boolean) => void) | null = null;

  readonly state = signal<ConfirmDialogState | null>(null);

  open(options: ConfirmDialogOptions): Promise<boolean> {
    this.closePending(false);

    return new Promise<boolean>((resolve) => {
      this.resolvePending = resolve;
      this.state.set({
        open: true,
        title: options.title,
        description: options.description,
        confirmLabel: options.confirmLabel,
        cancelLabel: options.cancelLabel,
        variant: options.variant ?? 'default',
      });
    });
  }

  confirm(): void {
    this.closePending(true);
  }

  cancel(): void {
    this.closePending(false);
  }

  private closePending(confirmed: boolean): void {
    if (this.resolvePending) {
      this.resolvePending(confirmed);
      this.resolvePending = null;
    }
    this.state.set(null);
  }
}
