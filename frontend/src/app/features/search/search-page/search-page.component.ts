import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchService } from '../../../core/services/search.service';
import { SearchResult } from '../../../core/models/search.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { SearchBarComponent } from '../search-bar/search-bar.component';
import { SearchResultsComponent } from '../search-results/search-results.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
    selector: 'app-search-page',
    imports: [CommonModule, TranslatePipe, SearchBarComponent, SearchResultsComponent, AlertComponent],
    templateUrl: './search-page.component.html'
})
export class SearchPageComponent implements OnInit {
  private readonly searchService = inject(SearchService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly searchResult = signal<SearchResult | null>(null);
  readonly searchQuery = signal<string>('');
  readonly error = signal<string>('');

  ngOnInit(): void {
    const query = this.route.snapshot.queryParams['q'];
    if (query) {
      this.searchQuery.set(query);
      // Could auto-search here if desired
    }
  }

  onSearchResults(result: SearchResult): void {
    this.searchResult.set(result);
    this.error.set('');
  }

  onSearchError(errorMessage: string): void {
    this.error.set(errorMessage);
    this.searchResult.set(null);
  }
}
