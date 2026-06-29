import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
    selector: 'app-card',
    imports: [CommonModule],
    template: `
    <div [class]="mergeClasses('rounded-xl border border-subtle bg-card text-card-foreground shadow-card transition-[box-shadow] duration-150 hover:shadow-card-hover', class())">
      <ng-content />
    </div>
  `
})
export class CardComponent {
  class = input<string>('');
  mergeClasses = mergeClasses;
}

@Component({
    selector: 'app-card-header',
    imports: [CommonModule],
    template: `
    <div [class]="mergeClasses('flex w-full flex-col space-y-1.5 p-6 pb-4', className())">
      <ng-content />
    </div>
  `
})
export class CardHeaderComponent {
  className = input('', { alias: 'class' });
  mergeClasses = mergeClasses;
}

@Component({
    selector: 'app-card-title',
    imports: [CommonModule],
    template: `
    <h3 [class]="mergeClasses('card-title-premium leading-none tracking-tight', class())">
      <ng-content />
    </h3>
  `
})
export class CardTitleComponent {
  class = input<string>('');
  mergeClasses = mergeClasses;
}

@Component({
    selector: 'app-card-description',
    imports: [CommonModule],
    template: `
    <p [class]="mergeClasses('text-sm text-muted-foreground', class())">
      <ng-content />
    </p>
  `
})
export class CardDescriptionComponent {
  class = input<string>('');
  mergeClasses = mergeClasses;
}

@Component({
    selector: 'app-card-content',
    imports: [CommonModule],
    template: `
    <div [class]="mergeClasses('p-6 pt-0', class())">
      <ng-content />
    </div>
  `
})
export class CardContentComponent {
  class = input<string>('');
  mergeClasses = mergeClasses;
}

@Component({
    selector: 'app-card-footer',
    imports: [CommonModule],
    template: `
    <div [class]="mergeClasses('flex items-center p-6 pt-4 border-t border-subtle', class())">
      <ng-content />
    </div>
  `
})
export class CardFooterComponent {
  class = input<string>('');
  mergeClasses = mergeClasses;
}
