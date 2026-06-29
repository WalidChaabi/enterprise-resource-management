import { Component, DestroyRef, HostListener, ChangeDetectionStrategy, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, Location } from '@angular/common';
import { NavigationEnd, Router, RouterModule, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { LucideAngularModule, ChevronLeft, Moon, PanelLeft, Search, Sun } from 'lucide-angular';
import { AuthService } from '../../core/services/auth.service';
import { BreadcrumbService } from '../../core/services/breadcrumb.service';
import { ThemeService } from '../../core/services/theme.service';
import { TranslationService } from '../../core/services/translation.service';
import { NotificationService } from '../../core/services/notification.service';
import { OrganizationSwitcherService } from '../../core/services/organization-switcher.service';
import { UiConfigService } from '../../core/services/ui-config.service';
import { EntitlementService } from '../../core/services/entitlement.service';
import { SettingsService } from '../../core/services/settings.service';
import { Notification } from '../../core/models/notification.model';
import { OrganizationSummary } from '../../core/models/organization.model';
import { ShortcutDefinition } from '../../core/models/settings.model';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../shared/components/ui/button/button.component';
import { SearchBarComponent } from '../../features/search/search-bar/search-bar.component';
import { BreadcrumbComponent, BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb.component';
import { UserMenuComponent } from '../../shared/components/user-menu/user-menu.component';
import { NotificationMenuComponent } from '../../shared/components/notification-menu/notification-menu.component';
import { QuickActionMenuComponent } from '../../shared/components/quick-action-menu/quick-action-menu.component';
import { HelpMenuComponent } from '../../shared/components/help-menu/help-menu.component';
import { OrgSwitcherComponent } from '../../shared/components/org-switcher/org-switcher.component';
import { KeyboardShortcutsComponent } from '../../shared/components/keyboard-shortcuts/keyboard-shortcuts.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { SidebarInsetComponent, SidebarProviderComponent, SidebarTriggerComponent } from '@/shared/components/ui/sidebar';

@Component({
    selector: 'app-main-layout',
    imports: [
        CommonModule,
        RouterModule,
        RouterOutlet,
        TranslatePipe,
        LucideAngularModule,
        ButtonComponent,
        SearchBarComponent,
        BreadcrumbComponent,
        UserMenuComponent,
        NotificationMenuComponent,
        QuickActionMenuComponent,
        HelpMenuComponent,
        OrgSwitcherComponent,
        KeyboardShortcutsComponent,
        SidebarComponent,
        SidebarProviderComponent,
        SidebarInsetComponent,
        SidebarTriggerComponent,
    ],
    templateUrl: './main-layout.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainLayoutComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly destroyRef = inject(DestroyRef);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly notificationService = inject(NotificationService);
  private readonly orgSwitcherService = inject(OrganizationSwitcherService);
  private readonly uiConfigService = inject(UiConfigService);
  private readonly entitlementService = inject(EntitlementService);
  private readonly settingsService = inject(SettingsService);
  readonly themeService = inject(ThemeService);
  readonly translationService = inject(TranslationService);
  readonly breadcrumbs = signal<BreadcrumbItem[]>([]);

  readonly PanelLeft = PanelLeft;
  readonly ChevronLeft = ChevronLeft;
  readonly Sun = Sun;
  readonly Moon = Moon;
  readonly Search = Search;

  readonly currentUser = this.authService.currentUser;
  readonly userFullName = computed(() => {
    const user = this.currentUser();
    return user ? `${user.firstName} ${user.lastName}` : '';
  });
  readonly userEmail = computed(() => this.currentUser()?.email ?? '');

  readonly theme = this.themeService.theme;
  
  readonly currentLanguage = this.translationService.currentLanguage;
  readonly languages = [
    { code: 'en' as const, label: 'English' },
    { code: 'fr' as const, label: 'Français' }
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
    this.entitlementService.load();
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  setLanguage(language: 'en' | 'fr'): void {
    this.translationService.setLanguage(language);
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

  logout(): void {
    this.authService.logout();
  }

  markNotificationsRead(): void {
    this.notificationService.markAllRead().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.loadNotifications();
      },
      error: (err) => {
        console.error('Failed to mark notifications as read:', err);
      }
    });
  }

  private loadNotifications(): void {
    this.notificationService.getNotifications(0, 20).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page) => {
        this.notifications.set(page.content);
      },
      error: (err) => {
        console.error('Failed to load notifications:', err);
      }
    });
  }

  private loadOrganizations(): void {
    this.orgSwitcherService.listOrganizations().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
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
    this.uiConfigService.getUiConfig().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
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
    this.settingsService.getShortcuts().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (shortcuts) => {
        this.shortcuts.set(shortcuts);
      },
      error: (err) => {
        console.error('Failed to load shortcuts:', err);
      }
    });
  }

  switchOrganization(orgId: string): void {
    this.orgSwitcherService.switchOrganization(orgId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (response) => {
        this.currentOrganization.set(
          this.organizations().find(org => org.id === response.activeOrgId)?.name || ''
        );
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