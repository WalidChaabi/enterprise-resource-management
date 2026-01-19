import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/services/theme.service';
import { TranslationService } from './core/services/translation.service';
import { ToastContainerComponent } from './shared/components/ui/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ToastContainerComponent],
  template: `
    <router-outlet></router-outlet>
    <app-toast-container />
  `,
  styles: []
})
export class AppComponent {
  title = 'Enterprise Resource Management';
  
  // Initialize services to apply theme and language on app start
  private readonly themeService = inject(ThemeService);
  private readonly translationService = inject(TranslationService);
}
