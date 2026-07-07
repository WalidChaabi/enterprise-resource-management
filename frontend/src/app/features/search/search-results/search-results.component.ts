import { Component, Input, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SearchResult, SearchItem, SearchEntityType } from '../../../core/models/search.model';
import { SearchService } from '../../../core/services/search.service';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent } from '../../../shared/components/ui/card/card.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { EmptyStateComponent } from '../../../shared/components/ui/empty-state/empty-state.component';

@Component({
    selector: 'app-search-results',
    imports: [CommonModule, RouterModule, TranslatePipe, ButtonComponent, CardComponent, BadgeComponent, EmptyStateComponent],
    templateUrl: './search-results.component.html'
})
export class SearchResultsComponent implements OnInit {
  private readonly searchService = inject(SearchService);

  @Input() result!: SearchResult;
  @Input() query?: string;

  readonly exporting = signal<boolean>(false);
  readonly Object = Object; // Expose Object for template use

  ngOnInit(): void {
    if (!this.result) {
      console.warn('SearchResultsComponent: No result provided');
    }
  }

  getEntityTypeLabel(type: SearchEntityType): string {
    return `search.types.${type.toLowerCase()}`;
  }

  getEntityRoute(item: SearchItem): string[] {
    switch (item.type) {
      case SearchEntityType.EMPLOYEE:
        return ['/employees', item.id];
      case SearchEntityType.PROJECT:
        return ['/projects', item.id];
      case SearchEntityType.DEPARTMENT:
        return ['/departments', item.id];
      case SearchEntityType.TASK:
        const projectId = item.metadata?.['projectId'];
        if (projectId) {
          return ['/projects', projectId, 'tasks', item.id];
        }
        return ['/projects'];
      case SearchEntityType.TIME_ENTRY:
        return ['/time-entries'];
      case SearchEntityType.LEAVE_REQUEST:
        return ['/leave-requests'];
      default:
        return ['/dashboard'];
    }
  }

  exportToCSV(): void {
    if (!this.query) return;

    this.exporting.set(true);
    this.searchService.exportToCSV({
      query: this.query,
      format: 'CSV'
    }).subscribe({
      next: (blob) => {
        const filename = `search-results-${new Date().toISOString().split('T')[0]}.csv`;
        this.searchService.downloadFile(blob, filename);
        this.exporting.set(false);
      },
      error: (err) => {
        console.error('Export error:', err);
        this.exporting.set(false);
      }
    });
  }

  exportToPDF(): void {
    if (!this.query) return;

    this.exporting.set(true);
    this.searchService.exportToPDF({
      query: this.query,
      format: 'PDF'
    }).subscribe({
      next: (blob) => {
        const filename = `search-results-${new Date().toISOString().split('T')[0]}.pdf`;
        this.searchService.downloadFile(blob, filename);
        this.exporting.set(false);
      },
      error: (err) => {
        console.error('Export error:', err);
        this.exporting.set(false);
      }
    });
  }
}
