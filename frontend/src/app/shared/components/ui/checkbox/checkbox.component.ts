import {
  ChangeDetectionStrategy,
  Component,
  computed,
  forwardRef,
  input,
  output,
  signal,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { mergeClasses } from '@/shared/utils/merge-classes';
import { checkboxVariants, type CheckboxSize } from './checkbox.variants';

@Component({
  selector: 'app-checkbox',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'inline-flex',
    '[attr.data-size]': 'size()',
  },
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CheckboxComponent),
      multi: true,
    },
  ],
  imports: [LucideAngularModule],
  template: `
    <button
      type="button"
      role="checkbox"
      data-slot="checkbox"
      [id]="id() || null"
      [class]="classes()"
      [attr.data-state]="isChecked() ? 'checked' : 'unchecked'"
      [attr.aria-checked]="isChecked()"
      [attr.aria-label]="ariaLabel() || null"
      [attr.aria-invalid]="invalid() ? true : null"
      [disabled]="isDisabled()"
      (click)="toggle()">
      @if (isChecked()) {
        <lucide-icon name="check" [size]="iconSize()" [strokeWidth]="3" class="text-current" />
      }
    </button>
  `,
})
export class CheckboxComponent implements ControlValueAccessor {
  readonly id = input<string>('');
  readonly checked = input(false);
  readonly checkedChange = output<boolean>();
  readonly ariaLabel = input<string>('');
  readonly disabled = input(false);
  readonly invalid = input(false);
  readonly size = input<CheckboxSize>('default');
  readonly class = input<string>('');

  private readonly formDisabled = signal(false);
  private readonly formValue = signal<boolean | null>(null);

  private onChange: (value: boolean) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  readonly classes = computed(() =>
    mergeClasses(
      checkboxVariants({
        size: this.size(),
      }),
      this.class(),
    ),
  );

  readonly iconSize = computed(() => (this.size() === 'sm' ? 12 : 14));

  isDisabled(): boolean {
    return this.disabled() || this.formDisabled();
  }

  isChecked(): boolean {
    return this.formValue() ?? this.checked();
  }

  toggle(): void {
    if (this.isDisabled()) {
      return;
    }
    const next = !this.isChecked();
    this.formValue.set(next);
    this.checkedChange.emit(next);
    this.onChange(next);
    this.onTouched();
  }

  writeValue(value: boolean): void {
    this.formValue.set(!!value);
  }

  registerOnChange(fn: (value: boolean) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.formDisabled.set(isDisabled);
  }
}
