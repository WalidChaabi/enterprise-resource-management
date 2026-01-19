import { Component, signal, computed, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ErrorResponse } from '../../../core/models/auth.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent, CardFooterComponent } from '../../../shared/components/ui/card/card.component';

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
  standalone: true,
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
    CardFooterComponent
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly sanitizer = inject(DomSanitizer);
  private readonly themeService = inject(ThemeService);
  private readonly translationService = inject(TranslationService);

  loginForm: FormGroup;

  // State management with clear separation
  readonly errorMessage = signal<string>('');
  readonly loading = signal<boolean>(false);
  readonly showPassword = signal<boolean>(false);
  readonly showDemoCredentials = signal<boolean>(false); // Collapsible demo section

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

    // Set loading state
    this.loading.set(true);

    // Submit authentication request
    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        // Success: Navigate to return URL or dashboard
        // Enterprise pattern: Respect returnUrl for deep linking
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
        this.router.navigateByUrl(returnUrl);
        // Note: loading state will be cleared on navigation
      },
      error: (error: ErrorResponse) => {
        // Error handling: Extract meaningful message
        // Enterprise pattern: User-friendly errors, no technical jargon
        const message = error.details?.[0] || 'auth.login.error.default';
        this.errorMessage.set(message);
        this.loading.set(false);

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
  fillDemoCredentials(): void {
    this.loginForm.patchValue({
      email: 'admin@demo.com',
      password: 'password'
    });
    this.showDemoCredentials.set(false);
    // Focus password field for quick submission
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
    const field = this.loginForm.get(fieldName);
    if (!field?.errors || !field.touched) return '';

    if (field.errors['required']) {
      return `auth.login.${fieldName}Required`;
    }
    if (field.errors['email']) {
      return 'auth.login.emailInvalid';
    }
    if (field.errors['minlength']) {
      return 'auth.login.passwordMinLength';
    }

    return '';
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

  /**
   * Get flag SVG for language switcher
   * Enterprise pattern: Visual language indicators
   */
  getFlagSvg(code: 'en' | 'fr'): SafeHtml {
    const flags: Record<string, string> = {
      'en': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480" width="20" height="15"><path fill="#012169" d="M0 0h640v480H0z"/><path fill="#FFF" d="M75 0l244 181L562 0h78v62L400 241l240 178v61h-80L320 301 81 480H0v-60l239-178L0 64V0h75z"/><path fill="#C8102E" d="M424 281l216 159v40L369 281h55zm-184 20l6 35L54 480H0l240-179zM640 0v3L391 191l2-44L590 0h50zM0 0l239 176h-60L0 42V0z"/><path fill="#FFF" d="M241 0v480h160V0H241zM0 160v160h640V160H0z"/><path fill="#C8102E" d="M0 193v96h640v-96H0zM273 0v480h96V0h-96z"/></svg>`,
      'fr': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480" width="20" height="15"><path fill="#ED2939" d="M0 0h640v480H0z"/><path fill="#002395" d="M0 0h213.3v480H0z"/><path fill="#FFF" d="M213.3 0h213.4v480H213.3z"/></svg>`
    };
    const svg = flags[code] || '';
    return this.sanitizer.bypassSecurityTrustHtml(svg);
  }

  /**
   * Get sun icon for light mode toggle
   */
  getSunIcon(): SafeHtml {
    const sunSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/></svg>`;
    return this.sanitizer.bypassSecurityTrustHtml(sunSvg);
  }

  /**
   * Get moon icon for dark mode toggle
   */
  getMoonIcon(): SafeHtml {
    const moonSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`;
    return this.sanitizer.bypassSecurityTrustHtml(moonSvg);
  }
}
