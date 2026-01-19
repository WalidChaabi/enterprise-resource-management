import { Component, ElementRef, EventEmitter, HostListener, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-quick-action-menu',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="relative">
      <button
        type="button"
        class="inline-flex h-10 w-10 items-center justify-center rounded-md border border-input bg-background hover:bg-accent hover:text-accent-foreground"
        (click)="toggleMenu()"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="'Open quick actions'">
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 5v14M5 12h14" />
        </svg>
      </button>

      @if (open()) {
        <div class="absolute right-0 mt-2 w-56 rounded-md border border-border bg-background shadow-lg">
          <div class="px-4 py-3">
            <p class="text-sm font-medium text-foreground">Quick actions</p>
          </div>
          <div class="border-t border-border"></div>
          <a
            routerLink="/employees/new"
            class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent"
            (click)="closeMenu()">
            New employee
          </a>
          <a
            routerLink="/projects/new"
            class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent"
            (click)="closeMenu()">
            New project
          </a>
          <a
            routerLink="/departments/new"
            class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent"
            (click)="closeMenu()">
            New department
          </a>
          <a
            routerLink="/time-entries/new"
            class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent"
            (click)="closeMenu()">
            New time entry
          </a>
          <a
            routerLink="/leave-requests/new"
            class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent"
            (click)="closeMenu()">
            New leave request
          </a>
        </div>
      }
    </div>
  `
})
export class QuickActionMenuComponent {
  readonly open = signal(false);
  @Output() opened = new EventEmitter<void>();

  constructor(private readonly elementRef: ElementRef<HTMLElement>) {}

  toggleMenu(): void {
    this.open.update((value) => !value);
    if (this.open()) {
      this.opened.emit();
    }
  }

  closeMenu(): void {
    this.open.set(false);
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
