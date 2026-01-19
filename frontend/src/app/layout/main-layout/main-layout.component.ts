import { Component, DestroyRef, HostListener, computed, inject, signal } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { NavigationEnd, Router, RouterModule, RouterOutlet } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { filter } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/services/auth.service';
import { BreadcrumbService } from '../../core/services/breadcrumb.service';
import { ThemeService } from '../../core/services/theme.service';
import { TranslationService } from '../../core/services/translation.service';
import { NotificationService } from '../../core/services/notification.service';
import { OrganizationSwitcherService } from '../../core/services/organization-switcher.service';
import { UiConfigService } from '../../core/services/ui-config.service';
import { SettingsService } from '../../core/services/settings.service';
import { Notification } from '../../core/models/notification.model';
import { OrganizationSummary } from '../../core/models/organization.model';
import { ShortcutDefinition } from '../../core/models/settings.model';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../shared/components/ui/button/button.component';
import { SearchBarComponent } from '../../features/search/search-bar/search-bar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb.component';
import { ProfileMenuComponent } from '../../shared/components/profile-menu/profile-menu.component';
import { NotificationMenuComponent } from '../../shared/components/notification-menu/notification-menu.component';
import { QuickActionMenuComponent } from '../../shared/components/quick-action-menu/quick-action-menu.component';
import { HelpMenuComponent } from '../../shared/components/help-menu/help-menu.component';
import { OrgSwitcherComponent } from '../../shared/components/org-switcher/org-switcher.component';
import { KeyboardShortcutsComponent } from '../../shared/components/keyboard-shortcuts/keyboard-shortcuts.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    RouterOutlet,
    TranslatePipe,
    ButtonComponent,
    SearchBarComponent,
    BreadcrumbComponent,
    ProfileMenuComponent,
    NotificationMenuComponent,
    QuickActionMenuComponent,
    HelpMenuComponent,
    OrgSwitcherComponent,
    KeyboardShortcutsComponent,
    SidebarComponent
  ],
  templateUrl: './main-layout.component.html'
})

export class MainLayoutComponent {
  private readonly authService = inject(AuthService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly destroyRef = inject(DestroyRef);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly notificationService = inject(NotificationService);
  private readonly orgSwitcherService = inject(OrganizationSwitcherService);
  private readonly uiConfigService = inject(UiConfigService);
  private readonly settingsService = inject(SettingsService);
  readonly themeService = inject(ThemeService);
  readonly translationService = inject(TranslationService);
  readonly mobileSidebarOpen = signal<boolean>(false);
  readonly breadcrumbs = signal<BreadcrumbItem[]>([]);

  readonly currentUser = this.authService.currentUser;
  readonly userFullName = computed(() => {
    const user = this.currentUser();
    return user ? `${user.firstName} ${user.lastName}` : '';
  });
  readonly userEmail = computed(() => this.currentUser()?.email ?? '');

  readonly theme = this.themeService.theme;
  
  readonly currentLanguage = this.translationService.currentLanguage;
  readonly languages = [
    { code: 'en' as const, label: 'English', flagCode: 'GB' },
    { code: 'fr' as const, label: 'Français', flagCode: 'FR' }
  ];
  readonly notifications = signal<Notification[]>([]);
  readonly shortcutsOpen = signal(false);
  readonly shortcuts = signal<ShortcutDefinition[]>([]);
  readonly organizations = signal<OrganizationSummary[]>([]);
  readonly currentOrganization = signal<string>('');
  readonly uiConfig = signal<{ documentationUrl?: string; supportUrl?: string }>({});

  constructor() {
    this.updateBreadcrumbs(this.router.url);
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((event) => {
        this.updateBreadcrumbs(event.urlAfterRedirects);
      });
    
    this.loadNotifications();
    this.loadOrganizations();
    this.loadUiConfig();
    this.loadShortcuts();
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  setLanguage(language: 'en' | 'fr'): void {
    this.translationService.setLanguage(language);
  }

  toggleMobileSidebar(): void {
    this.mobileSidebarOpen.update((open) => !open);
  }

  closeMobileSidebar(): void {
    this.mobileSidebarOpen.set(false);
  }

  goBack(): void {
    const items = this.breadcrumbs();
    const target = items.length > 1 ? items[items.length - 2] : null;
    if (target?.route) {
      this.router.navigate(Array.isArray(target.route) ? target.route : [target.route]);
      return;
    }

    if (window.history.length > 1) {
      this.location.back();
      return;
    }

    this.router.navigate(['/dashboard']);
  }

  getFlagSvg(code: 'en' | 'fr'): SafeHtml {
    const flags: Record<string, string> = {
      'en': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480" width="20" height="15"><path fill="#012169" d="M0 0h640v480H0z"/><path fill="#FFF" d="M75 0l244 181L562 0h78v62L400 241l240 178v61h-80L320 301 81 480H0v-60l239-178L0 64V0h75z"/><path fill="#C8102E" d="M424 281l216 159v40L369 281h55zm-184 20l6 35L54 480H0l240-179zM640 0v3L391 191l2-44L590 0h50zM0 0l239 176h-60L0 42V0z"/><path fill="#FFF" d="M241 0v480h160V0H241zM0 160v160h640V160H0z"/><path fill="#C8102E" d="M0 193v96h640v-96H0zM273 0v480h96V0h-96z"/></svg>`,
      'fr': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480" width="20" height="15"><path fill="#ED2939" d="M0 0h640v480H0z"/><path fill="#002395" d="M0 0h213.3v480H0z"/><path fill="#FFF" d="M213.3 0h213.4v480H213.3z"/></svg>`
    };
    const svg = flags[code] || '';
    return this.sanitizer.bypassSecurityTrustHtml(svg);
  }

  getSunIcon(): SafeHtml {
    const sunSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/></svg>`;
    return this.sanitizer.bypassSecurityTrustHtml(sunSvg);
  }

  getMoonIcon(): SafeHtml {
    const moonSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/></svg>`;
    return this.sanitizer.bypassSecurityTrustHtml(moonSvg);
  }

  openSettings(): void {
    this.router.navigate(['/settings']);
  }

  logout(): void {
    this.authService.logout();
  }

  markNotificationsRead(): void {
    this.notificationService.markAllRead().subscribe({
      next: () => {
        this.loadNotifications();
      },
      error: (err) => {
        console.error('Failed to mark notifications as read:', err);
      }
    });
  }

  private loadNotifications(): void {
    this.notificationService.getNotifications(0, 20).subscribe({
      next: (page) => {
        this.notifications.set(page.content);
      },
      error: (err) => {
        console.error('Failed to load notifications:', err);
      }
    });
  }

  private loadOrganizations(): void {
    this.orgSwitcherService.listOrganizations().subscribe({
      next: (orgs) => {
        this.organizations.set(orgs);
        const currentOrg = this.authService.getCurrentUser()?.organizationId;
        const activeOrg = orgs.find(org => org.id === currentOrg);
        if (activeOrg) {
          this.currentOrganization.set(activeOrg.name);
        } else if (orgs.length > 0) {
          this.currentOrganization.set(orgs[0].name);
        }
      },
      error: (err) => {
        console.error('Failed to load organizations:', err);
      }
    });
  }

  private loadUiConfig(): void {
    this.uiConfigService.getUiConfig().subscribe({
      next: (config) => {
        this.uiConfig.set({
          documentationUrl: config.documentationUrl,
          supportUrl: config.supportUrl
        });
      },
      error: (err) => {
        console.error('Failed to load UI config:', err);
      }
    });
  }

  private loadShortcuts(): void {
    this.settingsService.getShortcuts().subscribe({
      next: (shortcuts) => {
        this.shortcuts.set(shortcuts);
      },
      error: (err) => {
        console.error('Failed to load shortcuts:', err);
      }
    });
  }

  switchOrganization(orgId: string): void {
    this.orgSwitcherService.switchOrganization(orgId).subscribe({
      next: (response) => {
        this.currentOrganization.set(
          this.organizations().find(org => org.id === response.activeOrgId)?.name || ''
        );
        // Reload page or refresh data
        window.location.reload();
      },
      error: (err) => {
        console.error('Failed to switch organization:', err);
      }
    });
  }

  openShortcuts(): void {
    this.shortcutsOpen.set(true);
  }

  closeShortcuts(): void {
    this.shortcutsOpen.set(false);
  }

  @HostListener('document:keydown', ['$event'])
  handleShortcutKeys(event: KeyboardEvent): void {
    const target = event.target as HTMLElement | null;
    const isEditable = target
      ? target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable
      : false;

    if (isEditable) {
      return;
    }

    if (event.key === '?' || (event.shiftKey && event.key === '/')) {
      event.preventDefault();
      this.openShortcuts();
    }

    if (event.key === '/' && !event.shiftKey) {
      const input = document.querySelector<HTMLInputElement>('[data-search-input]');
      if (input) {
        event.preventDefault();
        input.focus();
      }
    }
  }

  private updateBreadcrumbs(url: string): void {
    const cleanedUrl = this.breadcrumbService.normalizeUrl(url);
    const overrideItems = this.breadcrumbService.getItemsForUrl(cleanedUrl);
    if (overrideItems) {
      this.breadcrumbs.set(overrideItems);
      return;
    }
    this.breadcrumbService.clear();
    const segments = cleanedUrl.split('/').filter(Boolean);
    const items: BreadcrumbItem[] = [];

    if (segments.length === 0) {
      this.breadcrumbs.set([{ label: this.translationService.translate('nav.dashboard') }]);
      return;
    }

    if (segments[0] !== 'dashboard') {
      items.push({ label: this.translationService.translate('nav.dashboard'), route: '/dashboard' });
    }

    segments.forEach((segment, index) => {
      const route = `/${segments.slice(0, index + 1).join('/')}`;
      const label = this.formatBreadcrumbLabel(segment);
      const isLast = index === segments.length - 1;
      items.push({ label, route: isLast ? undefined : route });
    });

    this.breadcrumbs.set(items);
  }

  private formatBreadcrumbLabel(segment: string): string {
    return segment
      .split('-')
      .map((part) => (part ? part[0].toUpperCase() + part.slice(1) : part))
      .join(' ');
  }
}
