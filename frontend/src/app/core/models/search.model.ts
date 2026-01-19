/**
 * Search models matching backend DTOs
 */

export enum SearchEntityType {
  EMPLOYEE = 'EMPLOYEE',
  PROJECT = 'PROJECT',
  TASK = 'TASK',
  DEPARTMENT = 'DEPARTMENT',
  TIME_ENTRY = 'TIME_ENTRY',
  LEAVE_REQUEST = 'LEAVE_REQUEST'
}

export interface SearchRequest {
  query: string;
  entityTypes?: SearchEntityType[];
  filters?: Record<string, any>;
  page?: number;
  size?: number;
}

export interface SearchItem {
  id: string;
  type: SearchEntityType;
  title: string;
  description?: string;
  metadata?: Record<string, any>;
  url?: string;
}

export interface SearchResult {
  items: SearchItem[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface ExportRequest {
  query?: string;
  entityTypes?: SearchEntityType[];
  filters?: Record<string, any>;
  format: 'CSV' | 'PDF';
}
