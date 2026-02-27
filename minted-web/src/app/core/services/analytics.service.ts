import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AnalyticsSummary, BudgetSummary, CategoryWise, TrendData, SpendingActivity, TotalBalance } from '../models/dashboard.model';

@Injectable({
    providedIn: 'root'
})
export class AnalyticsService {
    private apiUrl = `${environment.apiUrl}/analytics`;

    constructor(private http: HttpClient) { }

    getSummary(startDate: string, endDate: string): Observable<AnalyticsSummary> {
        const params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate);

        return this.http.get<{ success: boolean; data: AnalyticsSummary }>(`${this.apiUrl}/summary`, { params })
            .pipe(map(response => response.data));
    }

    getCategoryWise(startDate: string, endDate: string, type: string = 'EXPENSE'): Observable<CategoryWise[]> {
        const params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate)
            .set('type', type);

        return this.http.get<{ success: boolean; data: CategoryWise[] }>(`${this.apiUrl}/category-wise`, { params })
            .pipe(map(response => response.data));
    }

    getTrend(months: number = 6): Observable<TrendData[]> {
        const params = new HttpParams().set('months', months.toString());

        return this.http.get<{ success: boolean; data: TrendData[] }>(`${this.apiUrl}/trend`, { params })
            .pipe(map(response => response.data));
    }

    getSpendingActivity(startDate: string, endDate: string): Observable<SpendingActivity[]> {
        const params = new HttpParams()
            .set('startDate', startDate)
            .set('endDate', endDate);

        return this.http.get<{ success: boolean; data: SpendingActivity[] }>(`${this.apiUrl}/spending-activity`, { params })
            .pipe(map(response => response.data));
    }

    getTotalBalance(): Observable<TotalBalance> {
        return this.http.get<{ success: boolean; data: TotalBalance }>(`${this.apiUrl}/total-balance`)
            .pipe(map(response => response.data));
    }

    getBudgetSummary(): Observable<BudgetSummary[]> {
        return this.http.get<{ success: boolean; data: BudgetSummary[] }>(`${this.apiUrl}/budget-summary`)
            .pipe(map(response => response.data));
    }
}
