import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  IntegrationStatusResponse,
  SplitwiseAdminConfigResponse,
  SplitwiseAuthUrlResponse,
  SplitwiseFriend,
  FriendLinkResponse,
  PushResult,
  BulkPushResponse
} from '../models/splitwise.model';

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class SplitwiseService {
  private apiUrl = `${environment.apiUrl}/integrations/splitwise`;

  constructor(private http: HttpClient) { }

  // --- Status & Config ---
  
  getStatus(): Observable<IntegrationStatusResponse> {
    return this.http.get<ApiResponse<IntegrationStatusResponse>>(`${this.apiUrl}/status`).pipe(map(r => r.data!));
  }

  getAdminConfig(): Observable<SplitwiseAdminConfigResponse> {
    return this.http.get<ApiResponse<SplitwiseAdminConfigResponse>>(`${this.apiUrl}/admin-config`).pipe(map(r => r.data!));
  }

  // --- OAuth ---

  getAuthUrl(): Observable<SplitwiseAuthUrlResponse> {
    return this.http.get<ApiResponse<SplitwiseAuthUrlResponse>>(`${this.apiUrl}/auth-url`).pipe(map(r => r.data!));
  }

  handleCallback(code: string): Observable<ApiResponse<IntegrationStatusResponse>> {
    return this.http.post<ApiResponse<IntegrationStatusResponse>>(`${this.apiUrl}/callback`, { code });
  }

  disconnect(): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/disconnect`);
  }

  // --- Friends ---

  getFriends(): Observable<SplitwiseFriend[]> {
    return this.http.get<ApiResponse<SplitwiseFriend[]>>(`${this.apiUrl}/friends`).pipe(map(r => r.data || []));
  }

  getLinkedFriends(): Observable<FriendLinkResponse[]> {
    return this.http.get<ApiResponse<FriendLinkResponse[]>>(`${this.apiUrl}/linked-friends`).pipe(map(r => r.data || []));
  }

  linkFriend(friendId: number, splitwiseFriendId: number): Observable<ApiResponse<FriendLinkResponse>> {
    return this.http.post<ApiResponse<FriendLinkResponse>>(`${this.apiUrl}/link-friend`, { friendId, splitwiseFriendId });
  }

  unlinkFriend(friendId: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/link-friend/${friendId}`);
  }

  // --- Push ---

  pushSplit(splitId: number, forcePush: boolean = false): Observable<ApiResponse<PushResult>> {
    return this.http.post<ApiResponse<PushResult>>(`${this.apiUrl}/push-split/${splitId}`, { forcePush });
  }

  bulkPushSplits(splitTransactionIds: number[], forcePush: boolean = false): Observable<ApiResponse<BulkPushResponse>> {
    return this.http.post<ApiResponse<BulkPushResponse>>(`${this.apiUrl}/push-splits`, { splitTransactionIds, forcePush });
  }
}
