import { Injectable, inject } from '@angular/core';
import { HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { SearchRequest, SearchResult, ExportRequest } from '../models/search.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * Search service - handles search and export functionality
 */
@Injectable({
  providedIn: 'root'
})
export class SearchService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  search(request: SearchRequest): Observable<SearchResult> {
    const orgId = this.getOrgId();
    let httpParams = new HttpParams();
    
    httpParams = httpParams.set('query', request.query);
    
    if (request.entityTypes && request.entityTypes.length > 0) {
      request.entityTypes.forEach(type => {
        httpParams = httpParams.append('entityTypes', type);
      });
    }
    
    if (request.filters) {
      Object.keys(request.filters).forEach(key => {
        httpParams = httpParams.set(`filters.${key}`, request.filters![key]);
      });
    }
    
    if (request.page !== undefined) {
      httpParams = httpParams.set('page', request.page.toString());
    }
    
    if (request.size !== undefined) {
      httpParams = httpParams.set('size', request.size.toString());
    }

    return this.api.get<SearchResult>(API_ENDPOINTS.SEARCH(orgId), httpParams);
  }

  exportToCSV(request: ExportRequest): Observable<Blob> {
    const orgId = this.getOrgId();
    let httpParams = new HttpParams();
    
    if (request.query) {
      httpParams = httpParams.set('query', request.query);
    }
    
    if (request.entityTypes && request.entityTypes.length > 0) {
      request.entityTypes.forEach(type => {
        httpParams = httpParams.append('entityTypes', type);
      });
    }
    
    if (request.filters) {
      Object.keys(request.filters).forEach(key => {
        httpParams = httpParams.set(`filters.${key}`, request.filters![key]);
      });
    }
    
    httpParams = httpParams.set('format', 'CSV');

    return this.api.getBlob(`${API_ENDPOINTS.SEARCH(orgId)}/export`, httpParams);
  }

  exportToPDF(request: ExportRequest): Observable<Blob> {
    const orgId = this.getOrgId();
    let httpParams = new HttpParams();
    
    if (request.query) {
      httpParams = httpParams.set('query', request.query);
    }
    
    if (request.entityTypes && request.entityTypes.length > 0) {
      request.entityTypes.forEach(type => {
        httpParams = httpParams.append('entityTypes', type);
      });
    }
    
    if (request.filters) {
      Object.keys(request.filters).forEach(key => {
        httpParams = httpParams.set(`filters.${key}`, request.filters![key]);
      });
    }
    
    httpParams = httpParams.set('format', 'PDF');

    return this.api.getBlob(`${API_ENDPOINTS.SEARCH(orgId)}/export`, httpParams);
  }

  downloadFile(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }
}
