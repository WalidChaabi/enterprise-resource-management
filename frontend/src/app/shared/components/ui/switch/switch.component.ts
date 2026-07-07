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
import { mergeClasses } from '@/shared/utils/merge-classes';
import {
  switchThumbVariants,
  switchTrackVariants,
  type SwitchSize,
} from './switch.variants';

@Component({
  selector: 'app-switch',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'inline-flex',
    '[attr.data-size]': 'size()',
  },
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SwitchComponent),
      multi: true,
    },
  ],
  template: `
    <button
      type="button"
      role="switch"
      data-slot="switch"
      [class]="trackClasses()"
      [attr.data-state]="isChecked() ? 'checked' : 'unchecked'"
      [attr.aria-checked]="isChecked()"
      [attr.aria-label]="ariaLabel() || null"
      [disabled]="isDisabled()"
      (click)="toggle()">
      <span
        data-slot="switch-thumb"
        [class]="thumbClasses()"
        [attr.data-state]="isChecked() ? 'checked' : 'unchecked'"></span>
    </button>
  `,
})
export class SwitchComponent implements ControlValueAccessor {
  readonly checked = input(false);
  readonly checkedChange = output<boolean>();
  readonly ariaLabel = input<string>('');
  readonly disabled = input(false);
  readonly size = input<SwitchSize>('default');
  readonly class = input<string>('');

  private readonly formDisabled = signal(false);
  private readonly formValue = signal<boolean | null>(null);

  private onChange: (value: boolean) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  readonly trackClasses = computed(() =>
    mergeClasses(
      switchTrackVariants({
        size: this.size(),
        checked: this.isChecked(),
      }),
      this.class(),
    ),
  );

  readonly thumbClasses = computed(() =>
    switchThumbVariants({
      size: this.size(),
      checked: this.isChecked(),
    }),
  );

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
