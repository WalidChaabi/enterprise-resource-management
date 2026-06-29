import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const SIDEBAR_COOKIE_NAME = 'sidebar_state';
const SIDEBAR_KEYBOARD_SHORTCUT = 'b';
const MOBILE_BREAKPOINT = 768;

@Injectable()
export class SidebarService {
  private readonly platformId = inject(PLATFORM_ID);

  readonly open = signal(true);
  readonly openMobile = signal(false);
  readonly isMobile = signal(false);

  readonly state = computed<'expanded' | 'collapsed'>(() => (this.open() ? 'expanded' : 'collapsed'));

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.open.set(this.readPersistedState());
      this.isMobile.set(window.innerWidth < MOBILE_BREAKPOINT);
      window.addEventListener('resize', this.onResize);
    }
  }

  dispose(): void {
    if (isPlatformBrowser(this.platformId)) {
      window.removeEventListener('resize', this.onResize);
    }
  }

  setOpen(value: boolean): void {
    this.open.set(value);
    this.persistState(value);
  }

  setOpenMobile(value: boolean): void {
    this.openMobile.set(value);
  }

  toggleSidebar(): void {
    if (this.isMobile()) {
      this.openMobile.update((value) => !value);
      return;
    }

    this.setOpen(!this.open());
  }

  handleKeyboardShortcut(event: KeyboardEvent): void {
    if (event.key.toLowerCase() !== SIDEBAR_KEYBOARD_SHORTCUT) {
      return;
    }

    if (!(event.metaKey || event.ctrlKey)) {
      return;
    }

    const target = event.target as HTMLElement | null;
    if (target?.tagName === 'INPUT' || target?.tagName === 'TEXTAREA' || target?.isContentEditable) {
      return;
    }

    event.preventDefault();
    this.toggleSidebar();
  }

  private readonly onResize = (): void => {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.isMobile.set(window.innerWidth < MOBILE_BREAKPOINT);
    if (!this.isMobile()) {
      this.openMobile.set(false);
    }
  };

  private readPersistedState(): boolean {
    if (!isPlatformBrowser(this.platformId)) {
      return true;
    }

    const match = document.cookie.match(new RegExp(`(?:^|; )${SIDEBAR_COOKIE_NAME}=([^;]*)`));
    if (!match) {
      return true;
    }

    return match[1] === 'true';
  }

  private persistState(value: boolean): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    document.cookie = `${SIDEBAR_COOKIE_NAME}=${value}; path=/; max-age=${60 * 60 * 24 * 7}`;
  }
}
