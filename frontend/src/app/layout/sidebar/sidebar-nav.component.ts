import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
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
import { NavSection } from './sidebar-nav.config';
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
      collapsed ? 'justify-center px-2' : ''
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

  badgeClass(priority: 'high' | 'medium' | 'low'): string {
    switch (priority) {
      case 'high':
        return 'bg-destructive/15 text-destructive';
      case 'medium':
        return 'bg-yellow-500/15 text-yellow-600 dark:text-yellow-400';
      default:
        return 'bg-muted text-muted-foreground';
    }
  }
}
