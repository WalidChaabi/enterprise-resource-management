import { Component, computed, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, Toast, ToastVariant } from './toast.service';
import { mergeClasses } from '@/shared/utils/merge-classes';

const toastVariants: Record<ToastVariant, string> = {
  default: 'bg-background text-foreground border',
  success: 'bg-green-50 dark:bg-green-900/20 text-green-900 dark:text-green-100 border-green-200 dark:border-green-800',
  error: 'bg-red-50 dark:bg-red-900/20 text-red-900 dark:text-red-100 border-red-200 dark:border-red-800',
  warning: 'bg-yellow-50 dark:bg-yellow-900/20 text-yellow-900 dark:text-yellow-100 border-yellow-200 dark:border-yellow-800',
  info: 'bg-blue-50 dark:bg-blue-900/20 text-blue-900 dark:text-blue-100 border-blue-200 dark:border-blue-800'
};

const iconVariants: Record<ToastVariant, string> = {
  default: 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
  success: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
  error: 'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z',
  warning: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
  info: 'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z'
};

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      class="pointer-events-auto w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-lg border shadow-lg transition-all duration-300 animate-in slide-in-from-top-5"
      [class]="toastVariants[toast().variant]">
      <div class="p-4">
        <div class="flex items-start">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" [attr.d]="iconVariants[toast().variant]" />
            </svg>
          </div>
          <div class="ml-3 min-w-0 flex-1">
            <p class="text-sm font-medium break-words">{{ toast().title }}</p>
            @if (toast().description) {
              <p class="mt-1 text-sm opacity-90 break-words">{{ toast().description }}</p>
            }
          </div>
          <div class="ml-4 flex flex-shrink-0">
            <button
              class="inline-flex rounded-md opacity-70 hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-offset-2"
              (click)="dismiss()">
              <span class="sr-only">Close</span>
              <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  host: {
    '[attr.data-toast-id]': 'toast().id'
  }
})
export class ToastComponent {
  toast = input.required<Toast>();
  toastService = inject(ToastService);
  
  readonly toastVariants = toastVariants;
  readonly iconVariants = iconVariants;

  dismiss(): void {
    this.toastService.dismiss(this.toast().id);
  }
}

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule, ToastComponent],
  template: `
    <div class="pointer-events-none fixed inset-0 z-[100] flex flex-col items-end gap-4 p-4 sm:items-end">
      @for (toast of toasts(); track toast.id) {
        <app-toast [toast]="toast" />
      }
    </div>
  `,
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
  toasts = this.toastService.toasts$;
}
