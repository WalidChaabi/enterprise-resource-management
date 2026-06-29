import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
  selector: 'app-reset-password',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    TranslatePipe,
    ButtonComponent,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardDescriptionComponent,
    CardContentComponent,
    AlertComponent
  ],
  template: `
    <div class="flex min-h-screen items-center justify-center bg-muted/30 p-4">
      <div class="w-full max-w-md">
        <app-card>
          <app-card-header class="text-center">
            <app-card-title>{{ 'auth.resetPassword.title' | translate }}</app-card-title>
            <app-card-description>{{ 'auth.resetPassword.subtitle' | translate }}</app-card-description>
          </app-card-header>
          <app-card-content>
            @if (!token()) {
              <app-alert variant="destructive">{{ 'auth.resetPassword.invalidToken' | translate }}</app-alert>
            } @else if (success()) {
              <app-alert>{{ 'auth.resetPassword.success' | translate }}</app-alert>
              <div class="mt-4 text-center">
                <a routerLink="/login" class="text-sm text-primary hover:underline">{{ 'auth.forgotPassword.backToLogin' | translate }}</a>
              </div>
            } @else {
              @if (error()) {
                <app-alert variant="destructive" class="mb-4">{{ error() }}</app-alert>
              }
              <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-4">
                <div>
                  <label for="password" class="text-sm font-medium">{{ 'auth.resetPassword.newPassword' | translate }}</label>
                  <input id="password" type="password" formControlName="password" autocomplete="new-password"
                    class="mt-1 flex h-11 w-full rounded-md border border-input bg-background px-3 text-sm" />
                </div>
                <div>
                  <label for="confirm" class="text-sm font-medium">{{ 'auth.resetPassword.confirmPassword' | translate }}</label>
                  <input id="confirm" type="password" formControlName="confirm" autocomplete="new-password"
                    class="mt-1 flex h-11 w-full rounded-md border border-input bg-background px-3 text-sm" />
                </div>
                <app-button type="submit" class="w-full" [disabled]="loading() || form.invalid">
                  {{ loading() ? ('auth.resetPassword.saving' | translate) : ('auth.resetPassword.submit' | translate) }}
                </app-button>
              </form>
            }
          </app-card-content>
        </app-card>
      </div>
    </div>
  `
})
export class ResetPasswordComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  readonly token = signal<string | null>(null);
  readonly loading = signal(false);
  readonly success = signal(false);
  readonly error = signal('');

  readonly form = this.fb.group({
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirm: ['', [Validators.required]]
  });

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    this.token.set(token);
  }

  onSubmit(): void {
    const token = this.token();
    if (!token || this.form.invalid) return;

    const password = this.form.value.password!;
    const confirm = this.form.value.confirm!;
    if (password !== confirm) {
      this.error.set('auth.resetPassword.passwordMismatch');
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.authService.resetPassword(token, password).subscribe({
      next: () => {
        this.success.set(true);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || 'auth.resetPassword.error');
        this.loading.set(false);
      }
    });
  }
}
