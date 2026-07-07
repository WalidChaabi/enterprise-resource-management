import { AbstractControl, ValidationErrors } from '@angular/forms';

type ErrorResolver = string | ((error: unknown) => string);
type ErrorMap = Record<string, ErrorResolver>;

function resolveErrorValue(resolver: ErrorResolver, error: unknown): string {
  return typeof resolver === 'function' ? resolver(error) : resolver;
}

export function getFieldErrorKey(
  control: AbstractControl | null,
  errorMap: ErrorMap,
  options?: { showWhenDirty?: boolean },
): string {
  if (!control || !control.errors) {
    return '';
  }

  const canShow = options?.showWhenDirty ? control.touched || control.dirty : control.touched;
  if (!canShow) {
    return '';
  }

  const errors: ValidationErrors = control.errors;
  for (const [validator, value] of Object.entries(errors)) {
    const resolver = errorMap[validator];
    if (resolver) {
      return resolveErrorValue(resolver, value);
    }
  }

  return '';
}
