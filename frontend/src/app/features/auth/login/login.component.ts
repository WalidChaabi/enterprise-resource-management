import { Component, signal, computed, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ErrorResponse } from '../../../core/models/auth.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent, CardFooterComponent } from '../../../shared/components/ui/card/card.component';
import { environment } from '../../../../environments/environment';
import { DEMO_ACCOUNTS, DEMO_PASSWORD } from '../../../core/config/demo-accounts';
import { getFieldErrorKey } from '../../../shared/utils/form-errors';

/**
 * Enterprise-grade login component
 * 
 * UX Philosophy:
 * - Trust first: Security signals without technical jargon
 * - Clarity always: Clear states, helpful errors, logical flow
 * - Elegance through restraint: Minimal, professional, distraction-free
 */
@Component({
    selector: 'app-login',
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
        CardFooterComponent
    ],
    templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly themeService = inject(ThemeService);
  private readonly translationService = inject(TranslationService);

  loginForm: FormGroup;

  // State management with clear separation
  readonly errorMessage = signal<string>('');
  readonly loading = signal<boolean>(false);
  readonly showPassword = signal<boolean>(false);
  readonly showDemoCredentials = signal<boolean>(false);
  readonly ssoProviders = signal<string[]>([]);
  readonly demoAccessEnabled = environment.showDemoAccess;
  readonly demoAccounts = DEMO_ACCOUNTS;
  readonly demoPassword = DEMO_PASSWORD;

  // Computed states for better UX
  readonly hasError = computed(() => this.errorMessage().length > 0);

  // Methods instead of computed for form validation (better change detection)
  get isFormValid(): boolean {
    return this.loginForm.valid;
  }

  get canSubmit(): boolean {
    return this.isFormValid && !this.loading();
  }

  // Theme and language state
  readonly theme = this.themeService.theme;
  readonly currentLanguage = this.translationService.currentLanguage;

  // Language options
  readonly languages = [
    { code: 'en' as const, label: 'English' },
    { code: 'fr' as const, label: 'Français' }
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Form initialization with enterprise-grade validation
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.loginForm.get('email')?.valueChanges.subscribe((email) => {
      if (email && this.loginForm.get('email')?.valid) {
        this.loadSsoProviders(email);
      } else {
        this.ssoProviders.set([]);
      }
    });
  }

  loadSsoProviders(email: string): void {
    this.authService.getSsoProviders(email).subscribe({
      next: (response) => this.ssoProviders.set(response.providers ?? []),
      error: () => this.ssoProviders.set([]),
    });
  }

  startSso(provider: string): void {
    this.authService.startSsoLogin(provider);
  }

  /**
   * Form submission handler
   * Enterprise pattern: Clear state management, proper error handling, non-blocking feedback
   */
  onSubmit(): void {
    // Clear previous errors
    this.errorMessage.set('');

    // Validate form
    if (!this.loginForm.valid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const credentials = this.loginForm.value;

    // Set loading state and disable form controls via reactive forms API
    this.loading.set(true);
    this.loginForm.disable();

    // Submit authentication request
    this.authService.login(credentials).subscribe({
      next: (response) => {
        if (response.mfaRequired && response.mfaToken) {
          this.loading.set(false);
          this.loginForm.enable();
          void this.router.navigate(['/login/mfa'], {
            queryParams: {
              token: response.mfaToken,
              returnUrl: this.route.snapshot.queryParams['returnUrl'] || '/dashboard',
            },
          });
          return;
        }
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
        this.router.navigateByUrl(returnUrl);
      },
      error: (error: ErrorResponse) => {
        // Error handling: Extract meaningful message
        // Enterprise pattern: User-friendly errors, no technical jargon
        const message = error.details?.[0] || 'auth.login.error.default';
        this.errorMessage.set(message);
        this.loading.set(false);
        this.loginForm.enable();

        // UX enhancement: Focus first invalid field after error
        // This helps keyboard users recover quickly
        if (this.email?.invalid) {
          document.getElementById('email')?.focus();
        } else if (this.password?.invalid) {
          document.getElementById('password')?.focus();
        }
      }
    });
  }

  /**
   * Toggle password visibility
   * Enterprise pattern: Accessible, intuitive, keyboard-friendly
   */
  togglePasswordVisibility(): void {
    this.showPassword.update(value => !value);
  }

  /**
   * Toggle demo credentials display
   * Enterprise pattern: De-emphasized, collapsible, clearly labeled
   */
  toggleDemoCredentials(): void {
    this.showDemoCredentials.update(value => !value);
  }

  /**
   * Fill demo credentials (for quick testing)
   * Enterprise pattern: Convenient but not prominent
   */
  fillDemoCredentials(email: string = 'admin@demo.com'): void {
    this.loginForm.patchValue({
      email,
      password: DEMO_PASSWORD
    });
    this.showDemoCredentials.set(false);
    setTimeout(() => document.getElementById('password')?.focus(), 100);
  }

  // Form control getters for template access
  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }

  /**
   * Get field error message
   * Enterprise pattern: Clear, actionable, non-aggressive
   */
  getFieldError(fieldName: string): string {
    return getFieldErrorKey(this.loginForm.get(fieldName), {
      required: `auth.login.${fieldName}Required`,
      email: 'auth.login.emailInvalid',
      minlength: 'auth.login.passwordMinLength',
    });
  }


  /**
   * Theme toggle handler
   * Enterprise pattern: Accessible, smooth transitions
   */
  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  /**
   * Language switcher handler
   * Enterprise pattern: Instant language switching without page reload
   */
  setLanguage(language: 'en' | 'fr'): void {
    this.translationService.setLanguage(language);
  }
}
