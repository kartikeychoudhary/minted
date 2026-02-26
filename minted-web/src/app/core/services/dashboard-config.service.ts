import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { DashboardConfigRequest, DashboardConfigResponse } from '../models/dashboard-config.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardConfigService {
  private apiUrl = `${environment.apiUrl}/dashboard-config`;

  constructor(private http: HttpClient) {}

  getConfig(): Observable<DashboardConfigResponse> {
    return this.http.get<{ success: boolean; data: DashboardConfigResponse }>(this.apiUrl)
      .pipe(map(response => response.data));
  }

  saveConfig(request: DashboardConfigRequest): Observable<DashboardConfigResponse> {
    return this.http.put<{ success: boolean; data: DashboardConfigResponse }>(this.apiUrl, request)
      .pipe(map(response => response.data));
  }
}
