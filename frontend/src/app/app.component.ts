import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/services/theme.service';
import { TranslationService } from './core/services/translation.service';
import { ToastContainerComponent } from './shared/components/ui/toast/toast.component';
import { ConfirmDialogHostComponent } from './shared/components/ui/alert-dialog';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, ToastContainerComponent, ConfirmDialogHostComponent],
    template: `
    <router-outlet></router-outlet>
    <app-toast-container />
    <app-confirm-dialog-host />
  `,
    styles: []
})
export class AppComponent {
  title = 'Enterprise Resource Management';
  
  // Initialize services to apply theme and language on app start
  private readonly themeService = inject(ThemeService);
  private readonly translationService = inject(TranslationService);
}
