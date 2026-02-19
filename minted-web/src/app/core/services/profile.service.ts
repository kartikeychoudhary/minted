import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, User } from '../models/user.model';
import { environment } from '../../../environments/environment';

export interface UserProfileUpdateRequest {
  displayName?: string;
  email?: string;
  currency?: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  constructor(private http: HttpClient) {}

  getProfile(): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${environment.apiUrl}/profile`);
  }

  updateProfile(request: UserProfileUpdateRequest): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${environment.apiUrl}/profile`, request);
  }
}
