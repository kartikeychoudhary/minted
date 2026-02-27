import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { DashboardConfigRequest, DashboardConfigResponse } from '../models/dashboard-config.model';

const DEFAULT_CHART_COLORS = ['#c48821', '#22c55e', '#3b82f6', '#a855f7', '#ec4899', '#14b8a6', '#f97316', '#6366f1'];
const CHART_COLORS_KEY = 'minted-chart-colors';

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

  getChartColors(): string[] {
    const stored = localStorage.getItem(CHART_COLORS_KEY);
    if (stored) {
      try { return JSON.parse(stored); } catch { /* fall through */ }
    }
    return [...DEFAULT_CHART_COLORS];
  }

  saveChartColors(colors: string[]): void {
    localStorage.setItem(CHART_COLORS_KEY, JSON.stringify(colors));
  }

  getDefaultChartColors(): string[] {
    return [...DEFAULT_CHART_COLORS];
  }
}
