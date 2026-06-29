import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  addMonths,
  eachDayOfInterval,
  endOfMonth,
  endOfWeek,
  format,
  isSameDay,
  isSameMonth,
  isToday,
  startOfMonth,
  startOfWeek,
  subMonths,
} from 'date-fns';
import { ChevronLeft, ChevronRight, LucideAngularModule } from 'lucide-angular';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
  selector: 'app-calendar',
  imports: [CommonModule, LucideAngularModule, ButtonDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="p-3">
      <div class="flex items-center justify-between pb-2">
        <button
          app-button
          variant="outline"
          size="icon"
          type="button"
          class="h-7 w-7"
          (click)="previousMonth()"
          aria-label="Previous month">
          <lucide-icon [img]="ChevronLeft" [size]="16" />
        </button>
        <span class="text-sm font-medium">{{ monthLabel() }}</span>
        <button
          app-button
          variant="outline"
          size="icon"
          type="button"
          class="h-7 w-7"
          (click)="nextMonth()"
          aria-label="Next month">
          <lucide-icon [img]="ChevronRight" [size]="16" />
        </button>
      </div>
      <div class="grid grid-cols-7 text-center text-[0.8rem] text-muted-foreground">
        @for (weekday of weekdays; track weekday) {
          <div class="py-1 font-normal">{{ weekday }}</div>
        }
      </div>
      <div class="grid grid-cols-7 gap-1 pt-1">
        @for (day of calendarDays(); track day.toISOString()) {
          <button
            type="button"
            [class]="dayClasses(day)"
            (click)="selectDay(day)">
            {{ format(day, 'd') }}
          </button>
        }
      </div>
    </div>
  `,
})
export class CalendarComponent {
  readonly selected = input<Date | null>(null);
  readonly selectedChange = output<Date>();

  readonly month = signal(new Date());

  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;
  readonly weekdays = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];

  readonly monthLabel = computed(() => format(this.month(), 'MMMM yyyy'));

  readonly calendarDays = computed(() => {
    const monthStart = startOfMonth(this.month());
    const monthEnd = endOfMonth(this.month());
    const gridStart = startOfWeek(monthStart);
    const gridEnd = endOfWeek(monthEnd);
    return eachDayOfInterval({ start: gridStart, end: gridEnd });
  });

  format = format;

  dayClasses(day: Date): string {
    const selectedDate = this.selected();
    return mergeClasses(
      'inline-flex h-8 w-8 items-center justify-center rounded-md p-0 text-sm font-normal transition-colors hover:bg-accent hover:text-accent-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
      !isSameMonth(day, this.month()) && 'text-muted-foreground opacity-50',
      selectedDate && isSameDay(day, selectedDate) && 'bg-primary text-primary-foreground hover:bg-primary hover:text-primary-foreground',
      isToday(day) && !(selectedDate && isSameDay(day, selectedDate)) && 'bg-accent text-accent-foreground'
    );
  }

  selectDay(day: Date): void {
    this.selectedChange.emit(day);
  }

  previousMonth(): void {
    this.month.update((current) => subMonths(current, 1));
  }

  nextMonth(): void {
    this.month.update((current) => addMonths(current, 1));
  }
}
