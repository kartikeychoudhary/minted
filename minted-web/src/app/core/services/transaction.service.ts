import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TransactionRequest, TransactionResponse, TransactionFilters } from '../models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private apiUrl = `${environment.apiUrl}/transactions`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<TransactionResponse[]> {
    return this.http.get<{ success: boolean; data: TransactionResponse[] }>(this.apiUrl)
      .pipe(map(response => response.data));
  }

  getByDateRange(startDate: string, endDate: string): Observable<TransactionResponse[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<{ success: boolean; data: TransactionResponse[] }>(`${this.apiUrl}/date-range`, { params })
      .pipe(map(response => response.data));
  }

  getByFilters(filters: TransactionFilters): Observable<TransactionResponse[]> {
    let params = new HttpParams()
      .set('startDate', filters.startDate)
      .set('endDate', filters.endDate);

    if (filters.accountId) {
      params = params.set('accountId', filters.accountId.toString());
    }
    if (filters.categoryId) {
      params = params.set('categoryId', filters.categoryId.toString());
    }
    if (filters.type) {
      params = params.set('type', filters.type);
    }

    return this.http.get<{ success: boolean; data: TransactionResponse[] }>(`${this.apiUrl}/filter`, { params })
      .pipe(map(response => response.data));
  }

  getById(id: number): Observable<TransactionResponse> {
    return this.http.get<{ success: boolean; data: TransactionResponse }>(`${this.apiUrl}/${id}`)
      .pipe(map(response => response.data));
  }

  create(request: TransactionRequest): Observable<TransactionResponse> {
    return this.http.post<{ success: boolean; data: TransactionResponse }>(this.apiUrl, request)
      .pipe(map(response => response.data));
  }

  update(id: number, request: TransactionRequest): Observable<TransactionResponse> {
    return this.http.put<{ success: boolean; data: TransactionResponse }>(`${this.apiUrl}/${id}`, request)
      .pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Utility method to export transactions (basic CSV export)
  exportTransactions(transactions: TransactionResponse[]): void {
    const headers = ['Date', 'Category', 'Description', 'Account', 'Amount', 'Type'];
    const csvData = transactions.map(t => [
      t.transactionDate,
      t.categoryName,
      t.description,
      t.accountName,
      t.amount.toString(),
      t.type
    ]);

    const csv = [headers, ...csvData]
      .map(row => row.map(cell => `"${cell}"`).join(','))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `transactions_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
    window.URL.revokeObjectURL(url);
  }
}
