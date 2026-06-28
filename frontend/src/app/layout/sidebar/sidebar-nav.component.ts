import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslatePipe } from '../../core/pipes/translate.pipe';
import { CreateAction, NavSection } from './sidebar-nav.config';

@Component({
    selector: 'app-sidebar-nav',
    imports: [CommonModule, RouterModule, TranslatePipe],
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './sidebar-nav.component.html'
})
export class SidebarNavComponent {
  @Input({ required: true }) theme!: 'dark' | 'light';
  @Input() isMobile = false;
  @Input() isCollapsed = false;
  @Input() sections: NavSection[] = [];
  @Input() currentCreateAction: CreateAction | null = null;

  @Output() navigated = new EventEmitter<void>();
  @Output() toggleCollapse = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  handleNavigate(): void {
    this.navigated.emit();
  }

  onToggleCollapse(): void {
    this.toggleCollapse.emit();
  }

  onLogout(): void {
    this.logout.emit();
  }
}
