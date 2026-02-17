import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { RecurringTransaction, RecurringTransactionRequest, RecurringSummary } from '../models/recurring.model';

@Injectable({
    providedIn: 'root'
})
export class RecurringTransactionService {
    private apiUrl = `${environment.apiUrl}/recurring-transactions`;

    constructor(private http: HttpClient) { }

    getAll(): Observable<RecurringTransaction[]> {
        return this.http.get<{ success: boolean; data: RecurringTransaction[] }>(this.apiUrl)
            .pipe(map(res => res.data));
    }

    getById(id: number): Observable<RecurringTransaction> {
        return this.http.get<{ success: boolean; data: RecurringTransaction }>(`${this.apiUrl}/${id}`)
            .pipe(map(res => res.data));
    }

    create(request: RecurringTransactionRequest): Observable<RecurringTransaction> {
        return this.http.post<{ success: boolean; data: RecurringTransaction }>(this.apiUrl, request)
            .pipe(map(res => res.data));
    }

    update(id: number, request: RecurringTransactionRequest): Observable<RecurringTransaction> {
        return this.http.put<{ success: boolean; data: RecurringTransaction }>(`${this.apiUrl}/${id}`, request)
            .pipe(map(res => res.data));
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    toggleStatus(id: number): Observable<void> {
        return this.http.patch<void>(`${this.apiUrl}/${id}/toggle`, {});
    }

    getSummary(): Observable<RecurringSummary> {
        return this.http.get<{ success: boolean; data: RecurringSummary }>(`${this.apiUrl}/summary`)
            .pipe(map(res => res.data));
    }

    search(query: string): Observable<RecurringTransaction[]> {
        const params = new HttpParams().set('q', query);
        return this.http.get<{ success: boolean; data: RecurringTransaction[] }>(`${this.apiUrl}/search`, { params })
            .pipe(map(res => res.data));
    }
}
