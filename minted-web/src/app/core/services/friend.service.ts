import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { FriendRequest, FriendResponse } from '../models/friend.model';

@Injectable({
  providedIn: 'root'
})
export class FriendService {
  private apiUrl = `${environment.apiUrl}/friends`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<FriendResponse[]> {
    return this.http.get<{ success: boolean; data: FriendResponse[] }>(this.apiUrl)
      .pipe(map(response => response.data));
  }

  getById(id: number): Observable<FriendResponse> {
    return this.http.get<{ success: boolean; data: FriendResponse }>(`${this.apiUrl}/${id}`)
      .pipe(map(response => response.data));
  }

  create(request: FriendRequest): Observable<FriendResponse> {
    return this.http.post<{ success: boolean; data: FriendResponse }>(this.apiUrl, request)
      .pipe(map(response => response.data));
  }

  update(id: number, request: FriendRequest): Observable<FriendResponse> {
    return this.http.put<{ success: boolean; data: FriendResponse }>(`${this.apiUrl}/${id}`, request)
      .pipe(map(response => response.data));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
