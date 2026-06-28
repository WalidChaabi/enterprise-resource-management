import { Component, ElementRef, EventEmitter, HostListener, Input, Output, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Notification } from '../../../core/models/notification.model';

@Component({
    selector: 'app-notification-menu',
    imports: [CommonModule, DatePipe],
    template: `
    <div class="relative">
      <button
        type="button"
        class="relative inline-flex h-10 w-10 items-center justify-center rounded-md border border-input bg-background hover:bg-accent hover:text-accent-foreground"
        (click)="toggleMenu()"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="'Open notifications'">
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.4-1.4A2 2 0 0 1 18 14.2V11a6 6 0 1 0-12 0v3.2a2 2 0 0 1-.6 1.4L4 17h5m6 0a3 3 0 1 1-6 0h6z" />
        </svg>
        @if (unreadCount() > 0) {
          <span class="absolute -right-1 -top-1 inline-flex h-4 min-w-[16px] items-center justify-center rounded-full bg-destructive px-1 text-[10px] font-semibold text-destructive-foreground">
            {{ unreadCount() }}
          </span>
        }
      </button>

      @if (open()) {
        <div class="absolute right-0 mt-2 w-80 rounded-md border border-border bg-background shadow-lg">
          <div class="flex items-center justify-between px-4 py-3">
            <p class="text-sm font-medium text-foreground">Notifications</p>
            <button
              type="button"
              class="text-xs text-muted-foreground hover:text-foreground"
              (click)="markAllRead()">
              Mark all as read
            </button>
          </div>
          <div class="border-t border-border"></div>
          <div class="max-h-72 overflow-y-auto">
            @if (items.length === 0) {
              <div class="px-4 py-6 text-sm text-muted-foreground">No notifications yet.</div>
            } @else {
              @for (item of items; track item.id) {
                <div class="px-4 py-3 hover:bg-accent">
                  <div class="flex items-start gap-2">
                    <span class="mt-1 h-2 w-2 rounded-full" [class.bg-primary]="item.unread" [class.bg-transparent]="!item.unread"></span>
                    <div class="min-w-0">
                      <p class="text-sm font-medium text-foreground">{{ item.title }}</p>
                      @if (item.description) {
                        <p class="text-xs text-muted-foreground">{{ item.description }}</p>
                      }
                      @if (item.time) {
                        <p class="text-[10px] text-muted-foreground mt-1">{{ item.time }}</p>
                      }
                    </div>
                  </div>
                </div>
              }
            }
          </div>
        </div>
      }
    </div>
  `
})
export class NotificationMenuComponent {
  readonly open = signal(false);

  @Input() items: Notification[] = [];
  @Output() markAll = new EventEmitter<void>();

  constructor(private readonly elementRef: ElementRef<HTMLElement>) {}

  unreadCount(): number {
    return this.items.filter((item) => item.unread).length;
  }

  toggleMenu(): void {
    this.open.update((value) => !value);
  }

  closeMenu(): void {
    this.open.set(false);
  }

  markAllRead(): void {
    this.markAll.emit();
  }

  @HostListener('document:click', ['$event'])
  handleDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.closeMenu();
    }
  }

  @HostListener('document:keydown.escape')
  handleEscape(): void {
    this.closeMenu();
  }

  formatTime(time: string): string {
    const date = new Date(time);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  }
}
