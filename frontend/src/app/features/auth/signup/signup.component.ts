import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { ErrorResponse } from '../../../core/models/auth.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import {
  CardComponent,
  CardHeaderComponent,
  CardTitleComponent,
  CardDescriptionComponent,
  CardContentComponent,
} from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
  selector: 'app-signup',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    LucideAngularModule,
    TranslatePipe,
    ButtonComponent,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardDescriptionComponent,
    CardContentComponent,
    AlertComponent,
  ],
  templateUrl: './signup.component.html',
})
export class SignupComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly step = signal(1);
  readonly loading = signal(false);
  readonly errorMessage = signal('');

  readonly totalSteps = 3;

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    organizationName: ['', [Validators.required, Validators.maxLength(255)]],
    acceptTerms: [false, [Validators.requiredTrue]],
  });

  readonly stepTitle = computed(() => {
    switch (this.step()) {
      case 1:
        return 'auth.signup.step1Title';
      case 2:
        return 'auth.signup.step2Title';
      default:
        return 'auth.signup.step3Title';
    }
  });

  readonly stepSubtitle = computed(() => {
    switch (this.step()) {
      case 1:
        return 'auth.signup.step1Subtitle';
      case 2:
        return 'auth.signup.step2Subtitle';
      default:
        return 'auth.signup.step3Subtitle';
    }
  });

  nextStep(): void {
    this.errorMessage.set('');
    const fields = this.fieldsForStep(this.step());
    fields.forEach((name) => this.form.get(name)?.markAsTouched());
    if (fields.some((name) => this.form.get(name)?.invalid)) {
      return;
    }
    if (this.step() < this.totalSteps) {
      this.step.update((s) => s + 1);
    }
  }

  previousStep(): void {
    this.errorMessage.set('');
    if (this.step() > 1) {
      this.step.update((s) => s - 1);
    }
  }

  onSubmit(): void {
    this.errorMessage.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const value = this.form.getRawValue();

    this.authService.signup({
      email: value.email!,
      password: value.password!,
      firstName: value.firstName!,
      lastName: value.lastName!,
      organizationName: value.organizationName!,
      acceptTerms: value.acceptTerms!,
    }).subscribe({
      next: () => {
        this.router.navigateByUrl('/dashboard');
      },
      error: (error: ErrorResponse) => {
        const codeMap: Record<string, string> = {
          EMAIL_ALREADY_IN_USE: 'auth.signup.error.emailInUse',
          SIGNUP_DISABLED: 'auth.signup.error.disabled',
          TERMS_REQUIRED: 'auth.signup.error.terms',
        };
        this.errorMessage.set(codeMap[error.code] ?? 'auth.signup.error.default');
        this.loading.set(false);
      },
    });
  }

  private fieldsForStep(step: number): string[] {
    switch (step) {
      case 1:
        return ['firstName', 'lastName', 'email', 'password'];
      case 2:
        return ['organizationName'];
      default:
        return ['acceptTerms'];
    }
  }
}
