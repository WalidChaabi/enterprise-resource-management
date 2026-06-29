import { Component, input, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';
import { mergeClasses } from '@/shared/utils/merge-classes';

@Component({
    selector: 'app-input',
    imports: [CommonModule, FormsModule],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => InputComponent),
            multi: true,
        },
    ],
    template: `
    <input
      [type]="type()"
      [class]="classes()"
      [placeholder]="placeholder()"
      [disabled]="disabled()"
      [value]="value"
      (input)="onInput($event)"
      (blur)="onBlur()"
      [attr.id]="id()"
      [attr.name]="name()" />
  `
})
export class InputComponent implements ControlValueAccessor {
  type = input<string>('text');
  placeholder = input<string>('');
  disabled = input<boolean>(false);
  id = input<string>('');
  name = input<string>('');
  class = input<string>('');

  value: string = '';
  onChange = (_value: string) => {};
  onTouched = () => {};

  classes() {
    return mergeClasses(
      'flex h-10 w-full rounded-[10px] border border-subtle bg-transparent px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 transition-colors duration-150',
      this.class()
    );
  }

  onInput(event: Event) {
    const target = event.target as HTMLInputElement;
    this.value = target.value;
    this.onChange(this.value);
  }

  onBlur() {
    this.onTouched();
  }

  writeValue(value: string): void {
    this.value = value || '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(_isDisabled: boolean): void {
    // Handled by input binding
  }
}
