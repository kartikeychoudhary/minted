import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AccountRequest, AccountResponse } from '../models/account.model';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private apiUrl = `${environment.apiUrl}/accounts`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<AccountResponse[]> {
    return this.http.get<{ success: boolean; data: AccountResponse[] }>(this.apiUrl)
      .pipe(map(response => response.data));
  }

  getById(id: number): Observable<AccountResponse> {
    return this.http.get<{ success: boolean; data: AccountResponse }>(`${this.apiUrl}/${id}`)
      .pipe(map(response => response.data));
  }

  create(request: AccountRequest): Observable<AccountResponse> {
    return this.http.post<{ success: boolean; data: AccountResponse }>(this.apiUrl, request)
      .pipe(map(response => response.data));
  }

  update(id: number, request: AccountRequest): Observable<AccountResponse> {
    return this.http.put<{ success: boolean; data: AccountResponse }>(`${this.apiUrl}/${id}`, request)
      .pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
