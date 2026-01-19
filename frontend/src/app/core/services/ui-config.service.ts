import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { UiConfig } from '../models/ui-config.model';
import { API_ENDPOINTS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class UiConfigService {
  private readonly api = inject(ApiService);

  getUiConfig(): Observable<UiConfig> {
    return this.api.get<UiConfig>(API_ENDPOINTS.UI_CONFIG);
  }
}
