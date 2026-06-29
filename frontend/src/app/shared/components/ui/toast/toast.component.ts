import { Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { ToastService, Toast, ToastVariant } from './toast.service';

const toastVariants: Record<ToastVariant, string> = {
  default: 'bg-background text-foreground border',
  success: 'bg-green-50 dark:bg-green-900/20 text-green-900 dark:text-green-100 border-green-200 dark:border-green-800',
  error: 'bg-red-50 dark:bg-red-900/20 text-red-900 dark:text-red-100 border-red-200 dark:border-red-800',
  warning: 'bg-yellow-50 dark:bg-yellow-900/20 text-yellow-900 dark:text-yellow-100 border-yellow-200 dark:border-yellow-800',
  info: 'bg-blue-50 dark:bg-blue-900/20 text-blue-900 dark:text-blue-100 border-blue-200 dark:border-blue-800'
};

const toastIconNames: Record<ToastVariant, string> = {
  default: 'circle-help',
  success: 'check-circle-2',
  error: 'x-circle',
  warning: 'alert-triangle',
  info: 'alert-circle',
};

@Component({
    selector: 'app-toast',
    imports: [CommonModule, LucideAngularModule],
    template: `
    <div 
      class="pointer-events-auto w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-lg border shadow-lg transition-all duration-300 animate-in slide-in-from-top-5"
      [class]="toastVariants[toast().variant]">
      <div class="p-4">
        <div class="flex items-start">
          <div class="flex-shrink-0">
            <lucide-icon [name]="toastIconNames[toast().variant]" [size]="20" />
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
              <lucide-icon name="x" [size]="20" />
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
  readonly toastIconNames = toastIconNames;

  dismiss(): void {
    this.toastService.dismiss(this.toast().id);
  }
}

@Component({
    selector: 'app-toast-container',
    imports: [CommonModule, ToastComponent],
    template: `
    <div class="pointer-events-none fixed inset-0 z-[100] flex flex-col items-end gap-4 p-4 sm:items-end">
      @for (toast of toasts(); track toast.id) {
        <app-toast [toast]="toast" />
      }
    </div>
  `
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
  toasts = this.toastService.toasts$;
}
