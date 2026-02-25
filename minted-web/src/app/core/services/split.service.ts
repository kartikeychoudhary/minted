import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  SplitTransactionRequest,
  SplitTransactionResponse,
  SplitBalanceSummaryResponse,
  FriendBalanceResponse,
  SettleRequest,
  SplitShareResponse
} from '../models/split.model';

@Injectable({
  providedIn: 'root'
})
export class SplitService {
  private apiUrl = `${environment.apiUrl}/splits`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<SplitTransactionResponse[]> {
    return this.http.get<{ success: boolean; data: SplitTransactionResponse[] }>(this.apiUrl)
      .pipe(map(response => response.data));
  }

  getById(id: number): Observable<SplitTransactionResponse> {
    return this.http.get<{ success: boolean; data: SplitTransactionResponse }>(`${this.apiUrl}/${id}`)
      .pipe(map(response => response.data));
  }

  create(request: SplitTransactionRequest): Observable<SplitTransactionResponse> {
    return this.http.post<{ success: boolean; data: SplitTransactionResponse }>(this.apiUrl, request)
      .pipe(map(response => response.data));
  }

  update(id: number, request: SplitTransactionRequest): Observable<SplitTransactionResponse> {
    return this.http.put<{ success: boolean; data: SplitTransactionResponse }>(`${this.apiUrl}/${id}`, request)
      .pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getBalanceSummary(): Observable<SplitBalanceSummaryResponse> {
    return this.http.get<{ success: boolean; data: SplitBalanceSummaryResponse }>(`${this.apiUrl}/summary`)
      .pipe(map(response => response.data));
  }

  getFriendBalances(): Observable<FriendBalanceResponse[]> {
    return this.http.get<{ success: boolean; data: FriendBalanceResponse[] }>(`${this.apiUrl}/balances`)
      .pipe(map(response => response.data));
  }

  settle(request: SettleRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/settle`, request);
  }

  getSharesByFriend(friendId: number): Observable<SplitShareResponse[]> {
    return this.http.get<{ success: boolean; data: SplitShareResponse[] }>(`${this.apiUrl}/friend/${friendId}/shares`)
      .pipe(map(response => response.data));
  }

  exportFriendShares(shares: SplitShareResponse[], friendName: string): void {
    const headers = ['Date', 'Description', 'Category', 'Share Amount', 'Payer', 'Settled'];
    const rows = shares.map(s => [
      s.splitTransactionDate,
      s.splitDescription,
      s.splitCategoryName,
      s.shareAmount.toFixed(2),
      s.isPayer ? 'Yes' : 'No',
      s.isSettled ? 'Yes' : 'No'
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map(r => r.map(v => `"${v}"`).join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `splits-${friendName.toLowerCase().replace(/\s+/g, '-')}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
}
