import { Component, EventEmitter, Output, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { SearchService } from '../../../core/services/search.service';
import { SearchRequest, SearchResult, SearchEntityType } from '../../../core/models/search.model';

@Component({
    selector: 'app-search-bar',
    imports: [CommonModule, FormsModule, RouterModule, TranslatePipe],
    templateUrl: './search-bar.component.html'
})
export class SearchBarComponent {
  private readonly searchService = inject(SearchService);
  private readonly router = inject(Router);

  @Output() searchResults = new EventEmitter<SearchResult>();
  @Output() searchError = new EventEmitter<string>();

  readonly query = signal<string>('');
  readonly searching = signal<boolean>(false);
  readonly showResults = signal<boolean>(false);
  readonly selectedTypes = signal<SearchEntityType[]>([]);

  readonly entityTypes = [
    { value: SearchEntityType.EMPLOYEE, label: 'search.types.employee' },
    { value: SearchEntityType.PROJECT, label: 'search.types.project' },
    { value: SearchEntityType.TASK, label: 'search.types.task' },
    { value: SearchEntityType.DEPARTMENT, label: 'search.types.department' },
    { value: SearchEntityType.TIME_ENTRY, label: 'search.types.timeEntry' },
    { value: SearchEntityType.LEAVE_REQUEST, label: 'search.types.leaveRequest' }
  ];

  onSearch(): void {
    const queryValue = this.query().trim();
    if (!queryValue || queryValue.length < 2) {
      return;
    }

    this.searching.set(true);
    this.showResults.set(false);

    const request: SearchRequest = {
      query: queryValue,
      entityTypes: this.selectedTypes().length > 0 ? this.selectedTypes() : undefined,
      page: 0,
      size: 20
    };

    this.searchService.search(request).subscribe({
      next: (result) => {
        this.searchResults.emit(result);
        this.searching.set(false);
        // Navigate to search results page
        this.router.navigate(['/search'], { 
          queryParams: { q: queryValue },
          state: { searchResult: result }
        });
      },
      error: (err) => {
        console.error('Search error:', err);
        let errorMessage = 'Failed to perform search';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        }
        
        this.searchError.emit(errorMessage);
        this.searching.set(false);
      }
    });
  }

  onQueryChange(value: string): void {
    this.query.set(value);
    if (value.trim().length >= 2) {
      // Optional: debounce and auto-search
      // For now, user clicks search button
    }
  }

  toggleEntityType(type: SearchEntityType): void {
    const current = this.selectedTypes();
    if (current.includes(type)) {
      this.selectedTypes.set(current.filter(t => t !== type));
    } else {
      this.selectedTypes.set([...current, type]);
    }
  }

  clearSearch(): void {
    this.query.set('');
    this.showResults.set(false);
    this.selectedTypes.set([]);
  }
}
