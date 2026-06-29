import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { ShortcutDefinition } from '../../../core/models/settings.model';

@Component({
    selector: 'app-keyboard-shortcuts',
    imports: [CommonModule, LucideAngularModule],
    template: `
    @if (open) {
      <div class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" (click)="close()"></div>
        <div class="relative w-full max-w-md rounded-lg border border-border bg-background p-6 shadow-lg">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold text-foreground">Keyboard shortcuts</h2>
            <button
              type="button"
              class="inline-flex h-8 w-8 items-center justify-center rounded-md border border-input hover:bg-accent"
              (click)="close()"
              [attr.aria-label]="'Close shortcuts'">
              <lucide-icon name="x" [size]="16" />
            </button>
          </div>
          <div class="mt-4 space-y-3 text-sm text-foreground">
            @for (shortcut of getDisplayShortcuts(); track shortcut.key) {
              <div class="flex items-center justify-between">
                <span>{{ shortcut.action }}</span>
                <span class="rounded border px-2 py-0.5 text-xs">{{ shortcut.key }}</span>
              </div>
            }
          </div>
        </div>
      </div>
    }
  `
})
export class KeyboardShortcutsComponent {
  @Input() open = false;
  @Input() shortcuts: ShortcutDefinition[] = [];
  @Output() closed = new EventEmitter<void>();

  close(): void {
    this.closed.emit();
  }

  getDefaultShortcuts(): ShortcutDefinition[] {
    return [
      { key: 'Shift + /', action: 'Open shortcuts' },
      { key: '/', action: 'Global search' },
      { key: 'A', action: 'Quick actions' },
      { key: 'N', action: 'Notifications' }
    ];
  }

  getDisplayShortcuts(): ShortcutDefinition[] {
    return this.shortcuts.length > 0 ? this.shortcuts : this.getDefaultShortcuts();
  }
}
