import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ErrorResponse } from '../models/auth.model';

/**
 * Base API service for all HTTP operations.
 * Auth headers are added by authInterceptor.
 */
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  private readonly jsonHeaders = new HttpHeaders({
    'Content-Type': 'application/json'
  });

  get<T>(url: string, params?: HttpParams): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}${url}`, {
      headers: this.jsonHeaders,
      params
    }).pipe(
      catchError(this.handleError.bind(this))
    );
  }

  post<T>(url: string, body: unknown): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${url}`, body, {
      headers: this.jsonHeaders
    }).pipe(
      catchError(this.handleError.bind(this))
    );
  }

  put<T>(url: string, body: unknown): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${url}`, body, {
      headers: this.jsonHeaders
    }).pipe(
      catchError(this.handleError.bind(this))
    );
  }

  patch<T>(url: string, body: unknown): Observable<T> {
    return this.http.patch<T>(`${this.baseUrl}${url}`, body, {
      headers: this.jsonHeaders
    }).pipe(
      catchError(this.handleError.bind(this))
    );
  }

  delete<T>(url: string): Observable<T> {
    return this.http.delete<T>(`${this.baseUrl}${url}`, {
      headers: this.jsonHeaders
    }).pipe(
      catchError(this.handleError.bind(this))
    );
  }

  getBlob(url: string, params?: HttpParams): Observable<Blob> {
    return this.http.get(`${this.baseUrl}${url}`, {
      headers: this.jsonHeaders,
      params,
      responseType: 'blob'
    }).pipe(
      catchError(this.handleError.bind(this))
    );
  }

  postBlob(url: string, body: unknown = {}): Observable<Blob> {
    return this.http.post(`${this.baseUrl}${url}`, body, {
      headers: this.jsonHeaders,
      responseType: 'blob'
    }).pipe(
      catchError(this.handleError.bind(this))
    );
  }

  getOptional<T>(url: string): Observable<T | null> {
    return this.http.get<T>(`${this.baseUrl}${url}`, {
      headers: this.jsonHeaders,
      observe: 'response'
    }).pipe(
      map(response => (response.status === 204 ? null : response.body)),
      catchError(this.handleError.bind(this))
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorResponse: ErrorResponse;

    if (error.error instanceof ErrorEvent) {
      errorResponse = {
        code: 'CLIENT_ERROR',
        details: [error.error.message]
      };
    } else {
      errorResponse = error.error || {
        code: 'UNEXPECTED_ERROR',
        details: ['An unexpected error occurred']
      };
    }

    return throwError(() => errorResponse);
  }
}
