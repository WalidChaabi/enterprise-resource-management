import { Component, ElementRef, EventEmitter, HostListener, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

@Component({
    selector: 'app-help-menu',
    imports: [CommonModule, LucideAngularModule],
    template: `
    <div class="relative">
      <button
        type="button"
        class="toolbar-icon-btn"
        (click)="toggleMenu()"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="'Open help menu'">
        <lucide-icon name="circle-help" [size]="20" />
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
