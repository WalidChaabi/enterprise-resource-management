import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { ErrorResponse } from '../../../core/models/auth.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent, CardDescriptionComponent, CardHeaderComponent, CardTitleComponent } from '../../../shared/components/ui/card/card.component';

@Component({
  selector: 'app-mfa-verify',
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
  ],
  templateUrl: './mfa-verify.component.html',
})
export class MfaVerifyComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly errorMessage = signal('');

  readonly form = this.fb.group({
    code: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$|^[A-Z0-9]{4}-[A-Z0-9]{4}$/i)]],
  });

  private readonly mfaToken = this.route.snapshot.queryParams['token'] ?? '';
  private readonly returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';

  onSubmit(): void {
    this.errorMessage.set('');
    if (!this.mfaToken) {
      this.errorMessage.set('auth.mfa.invalidSession');
      return;
    }
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.form.disable();
    this.authService.verifyMfa({ mfaToken: this.mfaToken, code: this.form.value.code! }).subscribe({
      next: () => this.router.navigateByUrl(this.returnUrl),
      error: (error: ErrorResponse) => {
        this.errorMessage.set(error.details?.[0] || 'auth.mfa.invalidCode');
        this.loading.set(false);
        this.form.enable();
      },
    });
  }
}
