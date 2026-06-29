import { ChangeDetectionStrategy, Component, HostBinding, HostListener, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { cva, type VariantProps } from 'class-variance-authority';
import { mergeClasses } from '@/shared/utils/merge-classes';
import { ButtonComponent } from '@/shared/components/ui/button/button.component';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import { SidebarService } from './sidebar.service';

const sidebarMenuButtonVariants = cva(
  'peer/menu-button flex w-full items-center gap-2 overflow-hidden rounded-md p-2 text-left text-sm text-sidebar-foreground outline-none ring-sidebar-ring transition-[width,height,padding] hover:bg-sidebar-accent hover:text-sidebar-accent-foreground focus-visible:ring-2 active:bg-sidebar-accent active:text-sidebar-accent-foreground disabled:pointer-events-none disabled:opacity-50 aria-disabled:pointer-events-none aria-disabled:opacity-50 data-[active=true]:bg-sidebar-accent data-[active=true]:font-medium data-[active=true]:text-sidebar-accent-foreground group-data-[collapsible=icon]:!size-8 group-data-[collapsible=icon]:!p-2 group-data-[collapsible=icon]:justify-center [&_svg]:size-4 [&_svg]:shrink-0 [&>span:last-child]:truncate group-data-[collapsible=icon]:[&>span:last-child]:hidden',
  {
    variants: {
      variant: {
        default: 'hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
        outline:
          'bg-background shadow-[0_0_0_1px_hsl(var(--sidebar-border))] hover:bg-sidebar-accent hover:text-sidebar-accent-foreground',
      },
      size: {
        default: 'h-8 text-sm',
        sm: 'h-7 text-xs',
        lg: 'h-12 text-sm group-data-[collapsible=icon]:!p-0',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  }
);

type SidebarMenuButtonVariants = VariantProps<typeof sidebarMenuButtonVariants>;

@Component({
  selector: 'app-sidebar-provider',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [SidebarService],
  template: `
    <div
      class="group/sidebar-wrapper flex min-h-svh w-full has-[[data-variant=inset]]:bg-sidebar"
      [style.--sidebar-width]="'16rem'"
      [style.--sidebar-width-icon]="'3rem'">
      <ng-content />
    </div>
  `,
})
export class SidebarProviderComponent {
  private readonly sidebar = inject(SidebarService);

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    this.sidebar.handleKeyboardShortcut(event);
  }
}

@Component({
  selector: 'app-sidebar-inset',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'flex min-h-svh min-w-0 w-full flex-1 flex-col',
  },
  template: `
    <main
      class="relative flex min-h-0 min-w-0 w-full flex-1 flex-col bg-background"
      [class]="className()">
      <ng-content />
    </main>
  `,
})
export class SidebarInsetComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-panel',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (sidebar.isMobile()) {
      @if (sidebar.openMobile()) {
        <div class="fixed inset-0 z-50 md:hidden" (click)="closeMobile()">
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <aside
            class="absolute inset-y-0 left-0 flex h-full w-[var(--sidebar-width)] max-w-[85vw] flex-col border-r border-sidebar-border bg-sidebar text-sidebar-foreground shadow-lg"
            (click)="$event.stopPropagation()"
            data-sidebar="sidebar"
            data-mobile="true">
            <ng-content />
          </aside>
        </div>
      }
    } @else {
      <aside
        class="group peer hidden h-svh shrink-0 flex flex-col border-r border-sidebar-border/80 bg-sidebar text-sidebar-foreground transition-[width] duration-150 ease-out md:flex"
        [ngClass]="{
          'w-[var(--sidebar-width)]': sidebar.state() === 'expanded',
          'w-[var(--sidebar-width-icon)]': sidebar.state() === 'collapsed' && collapsible() === 'icon'
        }"
        [attr.data-state]="sidebar.state()"
        [attr.data-collapsible]="sidebar.state() === 'collapsed' ? collapsible() : null"
        [attr.data-variant]="variant()"
        [attr.data-side]="side()">
        <ng-content />
      </aside>
    }
  `,
})
export class SidebarComponent {
  readonly sidebar = inject(SidebarService);

  side = input<'left' | 'right'>('left');
  variant = input<'sidebar' | 'floating' | 'inset'>('sidebar');
  collapsible = input<'offcanvas' | 'icon' | 'none'>('icon');

  closeMobile(): void {
    this.sidebar.setOpenMobile(false);
  }
}

@Component({
  selector: 'app-sidebar-header',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'shrink-0',
  },
  template: `
    <div class="flex flex-col gap-2 px-3 py-3" data-sidebar="header" [class]="className()">
      <ng-content />
    </div>
  `,
})
export class SidebarHeaderComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-footer',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'mt-auto shrink-0 overflow-visible',
  },
  template: `
    <div class="flex flex-col gap-2 px-3 py-3" data-sidebar="footer" [class]="className()">
      <ng-content />
    </div>
  `,
})
export class SidebarFooterComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-content',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'flex min-h-0 flex-1 flex-col overflow-hidden',
  },
  template: `
    <div
      class="flex min-h-0 flex-1 flex-col gap-6 overflow-y-auto group-data-[collapsible=icon]:overflow-hidden px-2 py-2"
      data-sidebar="content"
      [class]="className()">
      <ng-content />
    </div>
  `,
})
export class SidebarContentComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-group',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="relative flex w-full min-w-0 flex-col px-1" data-sidebar="group" [class]="className()">
      <ng-content />
    </div>
  `,
})
export class SidebarGroupComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-group-label',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="flex h-7 shrink-0 items-center rounded-md px-2.5 text-[11px] font-medium uppercase tracking-wider text-sidebar-foreground/60 outline-none ring-sidebar-ring transition-[margin,opacity] duration-200 ease-linear focus-visible:ring-2 group-data-[collapsible=icon]:-mt-8 group-data-[collapsible=icon]:opacity-0"
      data-sidebar="group-label"
      [class]="className()">
      <ng-content />
    </div>
  `,
})
export class SidebarGroupLabelComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-group-content',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="w-full text-sm" data-sidebar="group-content" [class]="className()">
      <ng-content />
    </div>
  `,
})
export class SidebarGroupContentComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-menu',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="flex w-full min-w-0 flex-col gap-0.5" data-sidebar="menu" [class]="className()">
      <ng-content />
    </ul>
  `,
})
export class SidebarMenuComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-menu-item',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <li class="group/menu-item relative" data-sidebar="menu-item" [class]="className()">
      <ng-content />
    </li>
  `,
})
export class SidebarMenuItemComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-menu-button',
  imports: [CommonModule, RouterModule],
  host: {
    class: 'block w-full',
  },
  template: `
    @if (routerLink()) {
      <a
        [routerLink]="routerLink()"
        routerLinkActive
        #rla="routerLinkActive"
        [routerLinkActiveOptions]="routerLinkActiveOptions()"
        [attr.data-active]="isActive() || rla.isActive ? 'true' : null"
        data-sidebar="menu-button"
        [attr.data-size]="size()"
        [class]="classes()"
        [attr.aria-label]="tooltip() || undefined"
        [title]="tooltip() || undefined">
        <ng-content />
      </a>
    } @else {
      <button
        type="button"
        [attr.data-active]="isActive() ? 'true' : null"
        data-sidebar="menu-button"
        [attr.data-size]="size()"
        [class]="classes()"
        [attr.aria-label]="tooltip() || undefined"
        [title]="tooltip() || undefined">
        <ng-content />
      </button>
    }
  `,
})
export class SidebarMenuButtonComponent {
  variant = input<SidebarMenuButtonVariants['variant']>('default');
  size = input<SidebarMenuButtonVariants['size']>('default');
  isActive = input(false);
  routerLink = input<string | string[] | null>(null);
  routerLinkActiveOptions = input<{ exact: boolean }>({ exact: false });
  tooltip = input<string | undefined>(undefined);
  className = input('', { alias: 'class' });

  classes(): string {
    return mergeClasses(
      sidebarMenuButtonVariants({ variant: this.variant(), size: this.size() }),
      this.className()
    );
  }
}

@Component({
  selector: 'app-sidebar-menu-badge',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="pointer-events-none absolute right-1 flex h-5 min-w-5 select-none items-center justify-center rounded-md px-1 text-xs font-medium tabular-nums text-sidebar-foreground peer-hover/menu-button:text-sidebar-accent-foreground group-data-[collapsible=icon]:hidden"
      data-sidebar="menu-badge"
      [class]="className()">
      <ng-content />
    </div>
  `,
})
export class SidebarMenuBadgeComponent {
  className = input('', { alias: 'class' });
}

@Component({
  selector: 'app-sidebar-rail',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button
      type="button"
      class="absolute inset-y-0 z-20 hidden w-4 -translate-x-1/2 transition-all ease-linear after:absolute after:inset-y-0 after:left-1/2 after:w-[2px] hover:after:bg-sidebar-border group-data-[side=left]:-right-4 group-data-[side=right]:left-0 sm:flex"
      data-sidebar="rail"
      [attr.aria-label]="'Toggle sidebar'"
      [title]="'Toggle sidebar'"
      (click)="toggle()"
      tabindex="-1">
    </button>
  `,
})
export class SidebarRailComponent {
  private readonly sidebar = inject(SidebarService);

  toggle(): void {
    this.sidebar.toggleSidebar();
  }
}

@Component({
  selector: 'app-sidebar-trigger',
  imports: [CommonModule, ButtonComponent, ButtonDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button
      app-button
      variant="ghost"
      size="icon"
      type="button"
      class="toolbar-icon-btn h-9 w-9"
      [class]="className()"
      (click)="toggle()"
      [attr.aria-label]="'Toggle sidebar'">
      <ng-content />
    </button>
  `,
})
export class SidebarTriggerComponent {
  private readonly sidebar = inject(SidebarService);
  className = input('', { alias: 'class' });

  toggle(): void {
    this.sidebar.toggleSidebar();
  }
}
