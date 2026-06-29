import {
  ChangeDetectionStrategy,
  Component,
  computed,
  forwardRef,
  input,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ChevronDown, LucideAngularModule } from 'lucide-angular';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import { mergeClasses } from '@/shared/utils/merge-classes';
import {
  DropdownMenuComponent,
  DropdownMenuRadioItemComponent,
  DropdownMenuTriggerDirective,
} from '@/shared/components/ui/dropdown-menu/dropdown-menu.component';

export interface FormSelectOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-form-select',
  imports: [
    CommonModule,
    LucideAngularModule,
    ButtonDirective,
    DropdownMenuComponent,
    DropdownMenuTriggerDirective,
    DropdownMenuRadioItemComponent,
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormSelectComponent),
      multi: true,
    },
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-dropdown-menu [side]="'bottom'" [align]="'start'" [fullWidth]="true" contentClass="max-h-60 overflow-y-auto">
      <button
        dropdownTrigger
        app-button
        variant="outline"
        type="button"
        [disabled]="disabled()"
        [class]="triggerClasses()"
        [attr.id]="id() || null"
        [attr.aria-haspopup]="'listbox'"
        [attr.aria-label]="placeholder()">
        <span class="truncate">{{ selectedLabel() }}</span>
        <lucide-icon [img]="ChevronDown" [size]="16" class="ml-auto opacity-50" />
      </button>
      <div dropdownContent class="flex flex-col">
        @for (option of options(); track option.value) {
          <app-dropdown-menu-radio-item
            [compact]="compact()"
            [selected]="value() === option.value"
            (selectedChange)="selectValue(option.value)">
            {{ option.label }}
          </app-dropdown-menu-radio-item>
        }
      </div>
    </app-dropdown-menu>
  `,
  host: {
    class: 'block w-full',
  },
})
export class FormSelectComponent implements ControlValueAccessor {
  readonly options = input<FormSelectOption[]>([]);
  readonly placeholder = input('Select an option');
  readonly compact = input(false);
  readonly className = input('', { alias: 'class' });
  readonly id = input('');

  readonly value = signal('');
  readonly disabled = signal(false);

  readonly ChevronDown = ChevronDown;

  readonly selectedLabel = computed(() => {
    const current = this.options().find((option) => option.value === this.value());
    if (current) {
      return current.label;
    }
    return this.placeholder();
  });

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  triggerClasses(): string {
    return mergeClasses(
      'h-10 w-full justify-between px-3 font-normal',
      !this.value() && 'text-muted-foreground',
      this.className()
    );
  }

  selectValue(next: string): void {
    this.value.set(next);
    this.onChange(next);
    this.onTouched();
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
