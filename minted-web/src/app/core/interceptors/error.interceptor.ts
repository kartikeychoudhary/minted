import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('HTTP Error:', error);

        if (error.status === 401) {
          // Auto logout if 401 response returned from API
          this.authService.logout();
        }

        if (error.status === 403) {
          // Forbidden - token expired or lacking permissions
          console.error('Forbidden access - redirecting to login');
          this.authService.logout();
        }

        // Extract error message from API response
        let errorMessage = 'An error occurred';
        if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.error?.error) {
          errorMessage = error.error.error;
        } else if (error.message) {
          errorMessage = error.message;
        } else if (error.statusText) {
          errorMessage = error.statusText;
        }

        return throwError(() => new Error(errorMessage));
      })
    );
  }
}
