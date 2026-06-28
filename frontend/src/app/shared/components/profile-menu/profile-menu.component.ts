import { Component, ElementRef, EventEmitter, HostListener, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-profile-menu',
    imports: [CommonModule],
    template: `
    <div class="relative">
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-md border border-input bg-background px-2 py-1.5 text-sm font-medium text-foreground hover:bg-accent hover:text-accent-foreground"
        (click)="toggleMenu()"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="'Open user menu'">
        <span class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-accent text-accent-foreground text-xs font-semibold">
          {{ initials() }}
        </span>
        <span class="hidden sm:inline">{{ name || 'User' }}</span>
        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 9l6 6 6-6" />
        </svg>
      </button>

      @if (open()) {
        <div class="absolute right-0 mt-2 w-60 rounded-md border border-border bg-background shadow-lg">
          <div class="px-4 py-3">
            <p class="text-sm font-medium text-foreground">{{ name || 'User' }}</p>
            <p class="text-xs text-muted-foreground truncate">{{ email || '-' }}</p>
          </div>
          <div class="border-t border-border"></div>
          <button
            type="button"
            class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent"
            (click)="onSettings()">
            Settings
          </button>
          <button
            type="button"
            class="flex w-full items-center px-4 py-2 text-sm text-destructive transition-all duration-150 hover:bg-destructive/20 hover:text-destructive active:bg-destructive/50 active:text-destructive-foreground">
            Logout
          </button>
        </div>
      }
    </div>
  `
})
export class ProfileMenuComponent {
  readonly open = signal(false);

  @Input() name = '';
  @Input() email = '';
  @Output() settings = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  constructor(private readonly elementRef: ElementRef<HTMLElement>) {}

  toggleMenu(): void {
    this.open.update((value) => !value);
  }

  closeMenu(): void {
    this.open.set(false);
  }

  initials(): string {
    const parts = this.name.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return 'U';
    const first = parts[0]?.[0] ?? '';
    const last = parts.length > 1 ? parts[parts.length - 1]?.[0] ?? '' : '';
    return (first + last).toUpperCase();
  }

  onSettings(): void {
    this.settings.emit();
    this.closeMenu();
  }

  onLogout(): void {
    this.logout.emit();
    this.closeMenu();
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
}
