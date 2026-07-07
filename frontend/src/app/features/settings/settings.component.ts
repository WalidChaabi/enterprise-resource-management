import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
import { SettingsTabsNavComponent } from './components/settings-tabs-nav.component';

@Component({
    selector: 'app-settings',
    imports: [RouterOutlet, TranslatePipe, SettingsTabsNavComponent],
    template: `
    <div class="flex w-full flex-col gap-6">
      <div class="page-header">
        <h1 class="page-title">{{ 'settings.title' | translate }}</h1>
        <p class="page-description">{{ 'settings.subtitle' | translate }}</p>
      </div>

      <app-settings-tabs-nav />

      <div class="w-full">
        <router-outlet />
      </div>
    </div>
  `
})
export class SettingsComponent {}
