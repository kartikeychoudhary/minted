import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { CategoryRequest, CategoryResponse } from '../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<CategoryResponse[]> {
    return this.http.get<{ success: boolean; data: CategoryResponse[] }>(this.apiUrl)
      .pipe(map(response => response.data));
  }

  getById(id: number): Observable<CategoryResponse> {
    return this.http.get<{ success: boolean; data: CategoryResponse }>(`${this.apiUrl}/${id}`)
      .pipe(map(response => response.data));
  }

  create(request: CategoryRequest): Observable<CategoryResponse> {
    return this.http.post<{ success: boolean; data: CategoryResponse }>(this.apiUrl, request)
      .pipe(map(response => response.data));
  }

  update(id: number, request: CategoryRequest): Observable<CategoryResponse> {
    return this.http.put<{ success: boolean; data: CategoryResponse }>(`${this.apiUrl}/${id}`, request)
      .pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
