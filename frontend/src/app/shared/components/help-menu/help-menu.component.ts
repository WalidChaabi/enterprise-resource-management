import { Component, ElementRef, EventEmitter, HostListener, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-help-menu',
    imports: [CommonModule],
    template: `
    <div class="relative">
      <button
        type="button"
        class="inline-flex h-10 w-10 items-center justify-center rounded-md border border-input bg-background hover:bg-accent hover:text-accent-foreground"
        (click)="toggleMenu()"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="'Open help menu'">
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10a4 4 0 1 1 8 0c0 2-2 3-2 3m-2 4h.01M12 3a9 9 0 1 1 0 18 9 9 0 0 1 0-18z" />
        </svg>
      </button>

      @if (open()) {
        <div class="absolute right-0 mt-2 w-60 rounded-md border border-border bg-background shadow-lg">
          <div class="px-4 py-3">
            <p class="text-sm font-medium text-foreground">Help & support</p>
          </div>
          <div class="border-t border-border"></div>
          <button
            type="button"
            class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent"
            (click)="openShortcuts()">
            Keyboard shortcuts
          </button>
          <a
            [href]="documentationUrl || '#'"
            [target]="documentationUrl ? '_blank' : undefined"
            class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent">
            Documentation
          </a>
          <a
            [href]="supportUrl || '#'"
            [target]="supportUrl ? '_blank' : undefined"
            class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent">
            Contact support
          </a>
        </div>
      }
    </div>
  `
})
export class HelpMenuComponent {
  readonly open = signal(false);
  @Input() documentationUrl?: string;
  @Input() supportUrl?: string;
  @Output() shortcuts = new EventEmitter<void>();

  constructor(private readonly elementRef: ElementRef<HTMLElement>) {}

  toggleMenu(): void {
    this.open.update((value) => !value);
  }

  closeMenu(): void {
    this.open.set(false);
  }

  openShortcuts(): void {
    this.shortcuts.emit();
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
