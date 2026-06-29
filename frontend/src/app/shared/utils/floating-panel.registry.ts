import { Injectable } from '@angular/core';

export interface FloatingPanelHandle {
  close: () => void;
}

@Injectable({ providedIn: 'root' })
export class FloatingPanelRegistry {
  private readonly panels = new Set<FloatingPanelHandle>();
  private openCount = 0;

  register(panel: FloatingPanelHandle): void {
    this.panels.add(panel);
  }

  unregister(panel: FloatingPanelHandle): void {
    this.panels.delete(panel);
  }

  notifyOpen(panel: FloatingPanelHandle): void {
    for (const registered of this.panels) {
      if (registered !== panel) {
        registered.close();
      }
    }
    this.openCount++;
  }

  notifyClose(): void {
    this.openCount = Math.max(0, this.openCount - 1);
  }

  hasOpenPanels(): boolean {
    return this.openCount > 0;
  }
}
