import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AccountTypeRequest, AccountTypeResponse } from '../models/account-type.model';

@Injectable({
  providedIn: 'root'
})
export class AccountTypeService {
  private apiUrl = `${environment.apiUrl}/account-types`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<AccountTypeResponse[]> {
    return this.http.get<{ success: boolean; data: AccountTypeResponse[] }>(this.apiUrl)
      .pipe(map(response => response.data));
  }

  getById(id: number): Observable<AccountTypeResponse> {
    return this.http.get<{ success: boolean; data: AccountTypeResponse }>(`${this.apiUrl}/${id}`)
      .pipe(map(response => response.data));
  }

  create(request: AccountTypeRequest): Observable<AccountTypeResponse> {
    return this.http.post<{ success: boolean; data: AccountTypeResponse }>(this.apiUrl, request)
      .pipe(map(response => response.data));
  }

  update(id: number, request: AccountTypeRequest): Observable<AccountTypeResponse> {
    return this.http.put<{ success: boolean; data: AccountTypeResponse }>(`${this.apiUrl}/${id}`, request)
      .pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
