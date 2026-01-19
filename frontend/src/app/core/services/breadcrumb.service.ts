import { Injectable, signal } from '@angular/core';
import { BreadcrumbItem } from '../../shared/components/breadcrumb/breadcrumb.component';

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {
  private readonly items = signal<BreadcrumbItem[]>([]);
  private readonly sourceUrl = signal<string>('');

  setItems(items: BreadcrumbItem[], sourceUrl: string): void {
    this.items.set(items);
    this.sourceUrl.set(this.normalizeUrl(sourceUrl));
  }

  getItemsForUrl(url: string): BreadcrumbItem[] | null {
    const normalized = this.normalizeUrl(url);
    if (!this.sourceUrl() || this.sourceUrl() !== normalized) {
      return null;
    }
    return this.items();
  }

  clear(): void {
    this.items.set([]);
    this.sourceUrl.set('');
  }

  normalizeUrl(url: string): string {
    return url.split('?')[0].split('#')[0];
  }
}
