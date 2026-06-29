import {
  ChangeDetectionStrategy,
  Component,
  computed,
  forwardRef,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { format, isValid, parseISO } from 'date-fns';
import { Calendar as CalendarIcon, LucideAngularModule } from 'lucide-angular';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import { mergeClasses } from '@/shared/utils/merge-classes';
import { CalendarComponent } from '@/shared/components/ui/calendar/calendar.component';
import {
  PopoverComponent,
  PopoverTriggerDirective,
} from '@/shared/components/ui/popover/popover.component';

@Component({
  selector: 'app-date-picker',
  imports: [
    CommonModule,
    LucideAngularModule,
    ButtonDirective,
    CalendarComponent,
    PopoverComponent,
    PopoverTriggerDirective,
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DatePickerComponent),
      multi: true,
    },
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-popover #popover [side]="'bottom'" [align]="'start'" contentClass="w-auto">
      <button
        popoverTrigger
        app-button
        variant="outline"
        type="button"
        [disabled]="disabled()"
        [class]="triggerClasses()"
        [attr.data-empty]="!value()">
        <lucide-icon [img]="CalendarIcon" [size]="16" class="opacity-70" />
        <span class="truncate">{{ displayLabel() }}</span>
      </button>
      <div popoverContent>
        <app-calendar [selected]="selectedDate()" (selectedChange)="onDateSelect($event)" />
      </div>
    </app-popover>
  `,
})
export class DatePickerComponent implements ControlValueAccessor {
  readonly placeholder = input('Pick a date');
  readonly className = input('', { alias: 'class' });
  readonly id = input('');

  readonly value = signal('');
  readonly disabled = signal(false);

  readonly CalendarIcon = CalendarIcon;

  private readonly popover = viewChild(PopoverComponent);

  readonly selectedDate = computed(() => {
    const raw = this.value();
    if (!raw) return null;
    const parsed = parseISO(raw);
    return isValid(parsed) ? parsed : null;
  });

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  triggerClasses(): string {
    return mergeClasses(
      'h-10 w-full justify-start px-3 font-normal',
      !this.value() && 'text-muted-foreground',
      this.className()
    );
  }

  displayLabel(): string {
    const date = this.selectedDate();
    if (!date) return this.placeholder();
    return format(date, 'PPP');
  }

  onDateSelect(date: Date): void {
    const next = format(date, 'yyyy-MM-dd');
    this.value.set(next);
    this.onChange(next);
    this.onTouched();
    this.popover()?.close();
  }

  writeValue(value: string | null): void {
    this.value.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled.set(isDisabled);
  }
}
