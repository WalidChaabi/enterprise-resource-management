import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
  selector: 'app-forgot-password',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    TranslatePipe,
    LucideAngularModule,
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
            <app-card-title>{{ 'auth.forgotPassword.title' | translate }}</app-card-title>
            <app-card-description>{{ 'auth.forgotPassword.subtitle' | translate }}</app-card-description>
          </app-card-header>
          <app-card-content>
            @if (success()) {
              <app-alert>{{ 'auth.forgotPassword.success' | translate }}</app-alert>
              <div class="mt-4 text-center">
                <a routerLink="/login" class="text-sm text-primary hover:underline">{{ 'auth.forgotPassword.backToLogin' | translate }}</a>
              </div>
            } @else {
              @if (error()) {
                <app-alert variant="destructive" class="mb-4">{{ error() }}</app-alert>
              }
              <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-4">
                <div>
                  <label for="email" class="text-sm font-medium">{{ 'auth.login.email' | translate }}</label>
                  <input id="email" type="email" formControlName="email" autocomplete="email"
                    class="mt-1 flex h-11 w-full rounded-md border border-input bg-background px-3 text-sm" />
                </div>
                <app-button type="submit" class="w-full" [disabled]="loading() || form.invalid">
                  {{ loading() ? ('auth.forgotPassword.sending' | translate) : ('auth.forgotPassword.submit' | translate) }}
                </app-button>
                <div class="text-center">
                  <a routerLink="/login" class="text-sm text-muted-foreground hover:text-foreground">{{ 'auth.forgotPassword.backToLogin' | translate }}</a>
                </div>
              </form>
            }
          </app-card-content>
        </app-card>
      </div>
    </div>
  `
})
export class ForgotPasswordComponent {
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly success = signal(false);
  readonly error = signal('');

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.error.set('');

    this.authService.forgotPassword(this.form.value.email!).subscribe({
      next: () => {
        this.success.set(true);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || 'auth.forgotPassword.error');
        this.loading.set(false);
      }
    });
  }
}
