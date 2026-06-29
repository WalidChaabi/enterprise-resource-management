import { Component, ElementRef, EventEmitter, HostListener, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { OrganizationSummary } from '../../../core/models/organization.model';

@Component({
    selector: 'app-org-switcher',
    imports: [CommonModule, LucideAngularModule],
    template: `
    <div class="relative">
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-[10px] border border-subtle bg-transparent px-2.5 py-1.5 text-sm text-foreground transition-colors duration-150 hover:bg-accent"
        (click)="toggleMenu()"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="'Switch organization'">
        <span class="truncate max-w-[140px]">{{ currentName || 'Organization' }}</span>
        <lucide-icon name="chevron-down" [size]="16" />
      </button>

      @if (open()) {
        <div class="absolute left-0 mt-2 w-60 rounded-md border border-border bg-background shadow-lg">
          <div class="px-4 py-2 text-xs text-muted-foreground">Organizations</div>
          <div class="border-t border-border"></div>
          @for (org of organizations; track org.id) {
            <button
              type="button"
              class="flex w-full items-center px-4 py-2 text-sm text-foreground hover:bg-accent"
              (click)="selectOrg(org)">
              {{ org.name }}
            </button>
          }
        </div>
      }
    </div>
  `
})
export class OrgSwitcherComponent {
  readonly open = signal(false);

  @Input() organizations: OrganizationSummary[] = [];
  @Input() currentName = '';
  @Output() orgSelected = new EventEmitter<string>();

  constructor(private readonly elementRef: ElementRef<HTMLElement>) {}

  toggleMenu(): void {
    this.open.update((value) => !value);
  }

  selectOrg(org: OrganizationSummary): void {
    this.orgSelected.emit(org.id);
    this.open.set(false);
  }

  @HostListener('document:click', ['$event'])
  handleDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.open.set(false);
    }
  }

  @HostListener('document:keydown.escape')
  handleEscape(): void {
    this.open.set(false);
  }
}
