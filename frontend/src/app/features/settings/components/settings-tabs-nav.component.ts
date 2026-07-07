import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import {
  TabsComponent,
  TabsListComponent,
  TabsTriggerComponent,
} from '../../../shared/components/ui/tabs/tabs.component';
import { SETTINGS_NAV_ITEMS, settingsTabIdFromUrl } from '../settings-nav.config';

@Component({
  selector: 'app-settings-tabs-nav',
  imports: [TabsComponent, TabsListComponent, TabsTriggerComponent, TranslatePipe],
  template: `
    <app-tabs class="gap-6" [value]="activeTab()" (valueChange)="onTabChange($event)">
      <app-tabs-list class="max-w-full overflow-x-auto">
        @for (item of navItems; track item.id) {
          <app-tabs-trigger [value]="item.id">
            {{ item.labelKey | translate }}
          </app-tabs-trigger>
        }
      </app-tabs-list>
    </app-tabs>
  `,
})
export class SettingsTabsNavComponent {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly navItems = SETTINGS_NAV_ITEMS;
  readonly activeTab = signal(settingsTabIdFromUrl(this.router.url));

  constructor() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.activeTab.set(settingsTabIdFromUrl(this.router.url)));
  }

  onTabChange(id: string): void {
    const item = SETTINGS_NAV_ITEMS.find((entry) => entry.id === id);
    if (item && item.route !== this.router.url) {
      void this.router.navigateByUrl(item.route);
    }
  }
}
