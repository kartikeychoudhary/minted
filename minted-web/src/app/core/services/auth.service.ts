import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { User, LoginRequest, LoginResponse, ChangePasswordRequest, ApiResponse } from '../models/user.model';
import { environment } from '../../../environments/environment';
import { CurrencyService } from './currency.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;

  private readonly TOKEN_KEY = 'minted_token';
  private readonly REFRESH_TOKEN_KEY = 'minted_refresh_token';
  private readonly USER_KEY = 'minted_user';

  constructor(
    private http: HttpClient,
    private router: Router,
    private currencyService: CurrencyService
  ) {
    const storedUser = localStorage.getItem(this.USER_KEY);
    this.currentUserSubject = new BehaviorSubject<User | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public get isAuthenticated(): boolean {
    return !!this.getToken();
  }

  login(username: string, password: string): Observable<LoginResponse> {
    const request: LoginRequest = { username, password };

    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request)
      .pipe(
        tap(response => {
          if (response.success && response.data) {
            // Store tokens
            localStorage.setItem(this.TOKEN_KEY, response.data.token);
            localStorage.setItem(this.REFRESH_TOKEN_KEY, response.data.refreshToken);
            localStorage.setItem(this.USER_KEY, JSON.stringify(response.data.user));

            // Sync currency preference from server
            if (response.data.user.currency) {
              this.currencyService.setCurrency(response.data.user.currency);
            }

            // Update current user
            this.currentUserSubject.next(response.data.user);
          }
        })
      );
  }

  logout(): void {
    // Clear local storage
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);

    // Clear current user
    this.currentUserSubject.next(null);

    // Navigate to login
    this.router.navigate(['/login']);
  }

  changePassword(request: ChangePasswordRequest): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${environment.apiUrl}/auth/change-password`, request)
      .pipe(
        tap(response => {
          if (response.success && this.currentUserValue) {
            // Update user's forcePasswordChange flag
            const updatedUser = { ...this.currentUserValue, forcePasswordChange: false };
            localStorage.setItem(this.USER_KEY, JSON.stringify(updatedUser));
            this.currentUserSubject.next(updatedUser);
          }
        })
      );
  }

  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken })
      .pipe(
        tap(response => {
          if (response.success && response.data) {
            localStorage.setItem(this.TOKEN_KEY, response.data.token);
            localStorage.setItem(this.REFRESH_TOKEN_KEY, response.data.refreshToken);
            localStorage.setItem(this.USER_KEY, JSON.stringify(response.data.user));
            if (response.data.user.currency) {
              this.currencyService.setCurrency(response.data.user.currency);
            }
            this.currentUserSubject.next(response.data.user);
          }
        })
      );
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }
}
