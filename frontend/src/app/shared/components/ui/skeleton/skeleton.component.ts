import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
    selector: 'app-skeleton',
    imports: [CommonModule],
    template: `
    <div 
      [class]="mergeClasses(
        'animate-pulse rounded-md bg-muted',
        className()
      )"
      [style.width]="width()"
      [style.height]="height()">
    </div>
  `
})
export class SkeletonComponent {
  className = input<string>('');
  width = input<string>('');
  height = input<string>('');
  mergeClasses = mergeClasses;
}

@Component({
    selector: 'app-skeleton-text',
    imports: [CommonModule, SkeletonComponent],
    template: `
    <div [class]="mergeClasses('space-y-2', containerClass())">
      @for (line of getLinesArray(); track $index) {
        <app-skeleton 
          [className]="lineClass()"
          [width]="getLineWidth($index)"
          [height]="height()" />
      }
    </div>
  `
})
export class SkeletonTextComponent {
  lines = input<number>(3);
  height = input<string>('h-4');
  containerClass = input<string>('');
  lineClass = input<string>('');
  fullWidth = input<boolean>(false);
  mergeClasses = mergeClasses;

  getLineWidth(index: number): string {
    if (this.fullWidth()) return '100%';
    const widths = ['100%', '90%', '95%', '85%'];
    return widths[index % widths.length];
  }

  getLinesArray(): number[] {
    return Array.from({ length: this.lines() }, (_, i) => i);
  }
}

@Component({
    selector: 'app-skeleton-table',
    imports: [CommonModule, SkeletonComponent],
    template: `
    <div class="space-y-3">
      <!-- Header -->
      <div class="flex gap-4">
        @for (col of columns(); track $index) {
          <app-skeleton 
            [className]="'h-12'"
            [width]="col.width || '100%'"
            [height]="'h-12'" />
        }
      </div>
      <!-- Rows -->
      @for (row of getRowsArray(); track $index) {
        <div class="flex gap-4">
          @for (col of columns(); track $index) {
            <app-skeleton 
              [className]="'h-12'"
              [width]="col.width || '100%'"
              [height]="'h-12'" />
          }
        </div>
      }
    </div>
  `
})
export class SkeletonTableComponent {
  columns = input<Array<{ width?: string }>>([
    { width: '25%' },
    { width: '25%' },
    { width: '25%' },
    { width: '25%' }
  ]);
  rows = input<number>(5);

  getRowsArray(): number[] {
    return Array.from({ length: this.rows() }, (_, i) => i);
  }
}
