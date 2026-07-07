import { Injectable, inject, signal, computed } from '@angular/core';
import { TranslationService } from '@/core/services/translation.service';

export type ToastVariant = 'default' | 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  title: string;
  description?: string;
  variant: ToastVariant;
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private readonly translation = inject(TranslationService);
  private readonly toasts = signal<Toast[]>([]);

  readonly toasts$ = computed(() => this.toasts());

  show(toast: Omit<Toast, 'id'>): string {
    const id = this.generateId();
    const duration = toast.duration ?? 5000;
    
    const newToast: Toast = {
      ...toast,
      id,
      duration
    };

    this.toasts.update(toasts => [...toasts, newToast]);

    if (duration > 0) {
      setTimeout(() => this.dismiss(id), duration);
    }

    return id;
  }

  success(title: string, description?: string, duration?: number): string {
    return this.show({ title, description, variant: 'success', duration });
  }

  error(title: string, description?: string, duration?: number): string {
    return this.show({ title, description, variant: 'error', duration });
  }

  warning(title: string, description?: string, duration?: number): string {
    return this.show({ title, description, variant: 'warning', duration });
  }

  info(title: string, description?: string, duration?: number): string {
    return this.show({ title, description, variant: 'info', duration });
  }

  successI18n(titleKey: string, descriptionKey?: string, duration?: number): string {
    return this.success(
      this.translation.translate(titleKey),
      descriptionKey ? this.translation.translate(descriptionKey) : undefined,
      duration
    );
  }

  errorI18n(titleKey: string, descriptionKey?: string, duration?: number): string {
    return this.error(
      this.translation.translate(titleKey),
      descriptionKey ? this.translation.translate(descriptionKey) : undefined,
      duration
    );
  }

  dismiss(id: string): void {
    this.toasts.update(toasts => toasts.filter(t => t.id !== id));
  }

  dismissAll(): void {
    this.toasts.set([]);
  }

  private generateId(): string {
    return `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }
}
