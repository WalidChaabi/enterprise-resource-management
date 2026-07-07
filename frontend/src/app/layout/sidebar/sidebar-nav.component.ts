import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
import { TranslationService } from '../../core/services/translation.service';
import { mergeClasses } from '@/shared/utils/merge-classes';
import {
  SidebarPanelComponent,
  SidebarContentComponent,
  SidebarFooterComponent,
  SidebarGroupComponent,
  SidebarGroupContentComponent,
  SidebarGroupLabelComponent,
  SidebarHeaderComponent,
  SidebarMenuComponent,
  SidebarMenuItemComponent,
  SidebarService,
} from '@/shared/components/ui/sidebar';
import { NavItem, NavItemIndicator, NavSection } from './sidebar-nav.config';
import { UserMenuComponent } from '@/shared/components/user-menu/user-menu.component';

@Component({
  selector: 'app-sidebar-nav',
  imports: [
    CommonModule,
    RouterModule,
    TranslatePipe,
    LucideAngularModule,
    SidebarPanelComponent,
    SidebarHeaderComponent,
    SidebarContentComponent,
    SidebarFooterComponent,
    SidebarGroupComponent,
    SidebarGroupLabelComponent,
    SidebarGroupContentComponent,
    SidebarMenuComponent,
    SidebarMenuItemComponent,
    UserMenuComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sidebar-nav.component.html',
})
export class SidebarNavComponent {
  protected readonly sidebar = inject(SidebarService);
  private readonly translation = inject(TranslationService);

  @Input({ required: true }) theme!: 'dark' | 'light';
  @Input() sections: NavSection[] = [];
  @Input() userName = '';
  @Input() userEmail = '';

  @Output() navigated = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  readonly isCollapsed = computed(() => this.sidebar.state() === 'collapsed');

  navItemClass(active: boolean, collapsed = false): string {
    return mergeClasses(
      'sidebar-nav-link',
      active ? 'is-active' : '',
      collapsed ? 'justify-center px-2' : '',
    );
  }

  handleNavigate(): void {
    this.navigated.emit();
    if (this.sidebar.isMobile()) {
      this.sidebar.setOpenMobile(false);
    }
  }

  onLogout(): void {
    this.logout.emit();
  }

  indicatorTooltip(indicator: NavItemIndicator): string {
    return this.translation.translate(indicator.tooltipKey, { count: indicator.count });
  }

  linkTitle(labelKey: string, indicator?: NavItemIndicator): string {
    const label = this.translation.translate(labelKey);
    if (!indicator || indicator.count <= 0) {
      return label;
    }
    return `${label} — ${this.indicatorTooltip(indicator)}`;
  }

  badgeClass(indicator: NavItemIndicator): string {
    if (indicator.kind === 'alert') {
      return 'bg-amber-500/15 text-amber-700 dark:text-amber-400';
    }
    switch (indicator.priority) {
      case 'high':
        return 'bg-destructive/15 text-destructive';
      case 'medium':
        return 'bg-yellow-500/15 text-yellow-600 dark:text-yellow-400';
      default:
        return 'bg-muted text-muted-foreground';
    }
  }

  dotClass(indicator: NavItemIndicator): string {
    if (indicator.kind === 'alert') {
      return 'bg-amber-500 ring-2 ring-sidebar';
    }
    switch (indicator.priority) {
      case 'high':
        return 'bg-destructive ring-2 ring-sidebar';
      case 'medium':
        return 'bg-yellow-500 ring-2 ring-sidebar';
      default:
        return 'bg-muted-foreground ring-2 ring-sidebar';
    }
  }

  showIndicator(indicator: NavItemIndicator | undefined): indicator is NavItemIndicator {
    return !!indicator && indicator.count > 0;
  }
}
