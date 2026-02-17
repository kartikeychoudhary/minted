import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { BudgetRequest, BudgetResponse } from '../models/budget.model';

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private apiUrl = `${environment.apiUrl}/budgets`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<BudgetResponse[]> {
    return this.http.get<{ success: boolean; data: BudgetResponse[] }>(this.apiUrl)
      .pipe(map(response => response.data));
  }

  getById(id: number): Observable<BudgetResponse> {
    return this.http.get<{ success: boolean; data: BudgetResponse }>(`${this.apiUrl}/${id}`)
      .pipe(map(response => response.data));
  }

  create(request: BudgetRequest): Observable<BudgetResponse> {
    return this.http.post<{ success: boolean; data: BudgetResponse }>(this.apiUrl, request)
      .pipe(map(response => response.data));
  }

  update(id: number, request: BudgetRequest): Observable<BudgetResponse> {
    return this.http.put<{ success: boolean; data: BudgetResponse }>(`${this.apiUrl}/${id}`, request)
      .pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
