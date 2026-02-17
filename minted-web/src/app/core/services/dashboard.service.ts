import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { DashboardCard, ChartDataResponse } from '../models/dashboard.model';

@Injectable({
    providedIn: 'root'
})
export class DashboardService {
    private apiUrl = `${environment.apiUrl}/dashboard-cards`;

    constructor(private http: HttpClient) { }

    getActiveCards(): Observable<DashboardCard[]> {
        return this.http.get<{ success: boolean; data: DashboardCard[] }>(`${this.apiUrl}/active`)
            .pipe(map(response => response.data));
    }

    getAllCards(): Observable<DashboardCard[]> {
        return this.http.get<{ success: boolean; data: DashboardCard[] }>(this.apiUrl)
            .pipe(map(response => response.data));
    }

    getCardData(cardId: number, startDate: string, endDate: string): Observable<ChartDataResponse> {
        const params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate);

        return this.http.get<{ success: boolean; data: ChartDataResponse }>(`${this.apiUrl}/${cardId}/data`, { params })
            .pipe(map(response => response.data));
    }

    reorderCards(cardIds: number[]): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/reorder`, cardIds);
    }
}
