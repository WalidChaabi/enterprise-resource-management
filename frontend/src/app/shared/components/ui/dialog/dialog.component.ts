import { Component, input, output, ElementRef, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
  selector: 'app-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (open()) {
      <div 
        class="fixed inset-0 z-50 flex items-center justify-center"
        (click)="onBackdropClick()">
        <div 
          class="fixed inset-0 bg-black/50 backdrop-blur-sm"
          (click)="onBackdropClick()">
        </div>
        <div 
          #dialogContent
          class="relative z-50 w-full max-w-lg rounded-lg border bg-background p-6 shadow-lg"
          [class]="mergeClasses('max-h-[90vh] overflow-y-auto', contentClass())"
          (click)="$event.stopPropagation()">
          <div class="flex items-center justify-between mb-4">
            @if (title()) {
              <h2 class="text-lg font-semibold">{{ title() }}</h2>
            }
            <button
              (click)="close()"
              class="ml-auto rounded-sm opacity-70 hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-ring"
              type="button">
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
              <span class="sr-only">Close</span>
            </button>
          </div>
          <div class="dialog-content">
            <ng-content />
          </div>
        </div>
      </div>
    }
  `,
})
export class DialogComponent {
  open = input<boolean>(false);
  title = input<string>('');
  contentClass = input<string>('');
  closeOnBackdrop = input<boolean>(true);
  
  closed = output<void>();
  
  dialogContent = viewChild<ElementRef<HTMLDivElement>>('dialogContent');
  mergeClasses = mergeClasses;

  close(): void {
    this.closed.emit();
  }

  onBackdropClick(): void {
    if (this.closeOnBackdrop()) {
      this.close();
    }
  }
}

@Component({
  selector: 'app-dialog-trigger',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      [type]="type()"
      [class]="mergeClasses('inline-flex items-center justify-center', buttonClass())"
      (click)="onClick.emit()">
      <ng-content />
    </button>
  `,
})
export class DialogTriggerComponent {
  onClick = output<void>();
  type = input<'button' | 'submit' | 'reset'>('button');
  buttonClass = input<string>('');
  mergeClasses = mergeClasses;
}
