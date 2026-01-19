import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Notification, Page } from '../models/notification.model';
import { API_ENDPOINTS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly api = inject(ApiService);

  getNotifications(page: number = 0, size: number = 20): Observable<Page<Notification>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.api.get<Page<Notification>>(API_ENDPOINTS.NOTIFICATIONS, params);
  }

  markAllRead(): Observable<void> {
    return this.api.post<void>(`${API_ENDPOINTS.NOTIFICATIONS}/mark-all-read`, {});
  }
}
