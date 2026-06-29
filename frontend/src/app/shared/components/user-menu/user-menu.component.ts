import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  Bell,
  ChevronsUpDown,
  CreditCard,
  LogOut,
  LucideAngularModule,
  ShieldCheck,
} from 'lucide-angular';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { mergeClasses } from '@/shared/utils/merge-classes';
import {
  SidebarMenuComponent,
  SidebarMenuItemComponent,
  SidebarService,
} from '@/shared/components/ui/sidebar';

type UserMenuVariant = 'sidebar' | 'toolbar';

@Component({
  selector: 'app-user-menu',
  imports: [
    CommonModule,
    RouterModule,
    TranslatePipe,
    LucideAngularModule,
    SidebarMenuComponent,
    SidebarMenuItemComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (variant() === 'sidebar') {
      <app-sidebar-menu>
        <app-sidebar-menu-item>
          <ng-container *ngTemplateOutlet="menuShell" />
        </app-sidebar-menu-item>
      </app-sidebar-menu>
    } @else {
      <ng-container *ngTemplateOutlet="menuShell" />
    }

    <ng-template #menuShell>
      <div class="relative w-full">
        @if (open()) {
          <div
            [class]="panelClass()"
            (click)="$event.stopPropagation()">
            <ng-container *ngTemplateOutlet="menuPanel" />
          </div>
        }

        @if (variant() === 'sidebar') {
          <button
            type="button"
            data-sidebar="menu-button"
            data-size="lg"
            [class]="sidebarTriggerClass()"
            [attr.aria-expanded]="open()"
            [attr.aria-label]="'nav.userMenu.open' | translate"
            [title]="isCollapsed() && !sidebar.isMobile() ? userName : undefined"
            (click)="toggleMenu($event)">
            <ng-container *ngTemplateOutlet="sidebarTriggerContent" />
          </button>
        } @else {
          <button
            type="button"
            class="toolbar-icon-btn"
            [attr.aria-expanded]="open()"
            [attr.aria-label]="'nav.userMenu.open' | translate"
            (click)="toggleMenu($event)">
            <span
              class="inline-flex size-8 items-center justify-center rounded-[10px] bg-icon-container text-xs font-semibold text-icon-accent">
              {{ userInitials() }}
            </span>
          </button>
        }
      </div>
    </ng-template>

    <ng-template #menuPanel>
      <div class="flex items-center gap-2 px-2 py-1.5 text-left text-sm">
        <span
          class="inline-flex size-8 shrink-0 items-center justify-center rounded-lg bg-sidebar-primary text-xs font-semibold text-sidebar-primary-foreground">
          {{ userInitials() }}
        </span>
        <div class="grid min-w-0 flex-1 text-left text-sm leading-tight">
          <span class="truncate font-medium text-popover-foreground">{{ userName }}</span>
          <span class="truncate text-xs text-muted-foreground">{{ userEmail }}</span>
        </div>
      </div>
      <div class="-mx-1 my-1 h-px bg-muted"></div>
      <a
        routerLink="/settings/profile"
        class="relative flex w-full cursor-default select-none items-center gap-2 rounded-sm px-2 py-1.5 text-sm text-popover-foreground outline-none transition-colors hover:bg-accent hover:text-accent-foreground"
        (click)="onMenuNavigate()">
        <lucide-icon [img]="ShieldCheck" [size]="16" class="text-muted-foreground" />
        {{ 'nav.userMenu.account' | translate }}
      </a>
      <a
        routerLink="/settings/billing"
        class="relative flex w-full cursor-default select-none items-center gap-2 rounded-sm px-2 py-1.5 text-sm text-popover-foreground outline-none transition-colors hover:bg-accent hover:text-accent-foreground"
        (click)="onMenuNavigate()">
        <lucide-icon [img]="CreditCard" [size]="16" class="text-muted-foreground" />
        {{ 'nav.userMenu.billing' | translate }}
      </a>
      <a
        routerLink="/settings/notifications"
        class="relative flex w-full cursor-default select-none items-center gap-2 rounded-sm px-2 py-1.5 text-sm text-popover-foreground outline-none transition-colors hover:bg-accent hover:text-accent-foreground"
        (click)="onMenuNavigate()">
        <lucide-icon [img]="Bell" [size]="16" class="text-muted-foreground" />
        {{ 'nav.userMenu.notifications' | translate }}
      </a>
      <div class="-mx-1 my-1 h-px bg-muted"></div>
      <button
        type="button"
        class="relative flex w-full cursor-default select-none items-center gap-2 rounded-sm px-2 py-1.5 text-sm text-popover-foreground outline-none transition-colors hover:bg-accent hover:text-accent-foreground"
        (click)="onLogout()">
        <lucide-icon [img]="LogOut" [size]="16" class="text-muted-foreground" />
        {{ 'auth.logout' | translate }}
      </button>
    </ng-template>

    <ng-template #sidebarTriggerContent>
      <span
        class="inline-flex size-8 shrink-0 items-center justify-center rounded-lg bg-sidebar-primary text-xs font-semibold text-sidebar-primary-foreground">
        {{ userInitials() }}
      </span>
      <span class="user-details grid min-w-0 flex-1 text-left text-sm leading-tight">
        <span class="truncate font-medium">{{ userName }}</span>
        <span class="truncate text-xs text-sidebar-foreground/70">{{ userEmail }}</span>
      </span>
      <lucide-icon
        [img]="ChevronsUpDown"
        [size]="16"
        class="user-chevron ml-auto shrink-0 text-sidebar-foreground/70" />
    </ng-template>
  `,
})
export class UserMenuComponent {
  protected readonly sidebar = inject(SidebarService);

  readonly variant = input<UserMenuVariant>('sidebar');
  readonly open = signal(false);

  @Input() userName = '';
  @Input() userEmail = '';

  @Output() navigated = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  readonly isCollapsed = computed(() => this.sidebar.state() === 'collapsed');
  readonly ChevronsUpDown = ChevronsUpDown;
  readonly ShieldCheck = ShieldCheck;
  readonly CreditCard = CreditCard;
  readonly Bell = Bell;
  readonly LogOut = LogOut;

  constructor(private readonly elementRef: ElementRef<HTMLElement>) {}

  panelClass(): string {
    const base =
      'absolute z-50 min-w-56 rounded-md border border-border bg-popover p-1 text-popover-foreground shadow-lg';
    if (this.variant() === 'toolbar') {
      return `${base} top-full right-0 mt-2 w-56`;
    }
    return `${base} bottom-full left-0 mb-2 w-full`;
  }

  sidebarTriggerClass(): string {
    return mergeClasses(
      'peer/menu-button flex w-full items-center gap-2 overflow-hidden rounded-md p-2 text-left text-sm text-sidebar-foreground outline-none ring-sidebar-ring transition-[width,height,padding] hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:ring-2 active:bg-sidebar-accent active:text-sidebar-accent-foreground h-12 group-data-[collapsible=icon]:!size-8 group-data-[collapsible=icon]:!p-2 group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:[&_.user-details]:hidden group-data-[collapsible=icon]:[&_.user-chevron]:hidden',
      this.open() ? 'bg-sidebar-accent text-sidebar-accent-foreground' : ''
    );
  }

  userInitials(): string {
    const parts = this.userName.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) {
      return 'U';
    }
    if (parts.length === 1) {
      return parts[0].slice(0, 2).toUpperCase();
    }
    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
  }

  toggleMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.open.update((value) => !value);
  }

  closeMenu(): void {
    this.open.set(false);
  }

  onMenuNavigate(): void {
    this.closeMenu();
    this.navigated.emit();
    if (this.sidebar.isMobile()) {
      this.sidebar.setOpenMobile(false);
    }
  }

  onLogout(): void {
    this.closeMenu();
    if (this.sidebar.isMobile()) {
      this.sidebar.setOpenMobile(false);
    }
    this.logout.emit();
  }

  @HostListener('document:click', ['$event'])
  handleDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.closeMenu();
    }
  }

  @HostListener('document:keydown.escape')
  handleEscape(): void {
    this.closeMenu();
  }
}
