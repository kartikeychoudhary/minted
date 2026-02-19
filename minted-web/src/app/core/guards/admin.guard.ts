import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isAuthenticated) {
        const user = authService.currentUserValue;
        if (user && user.role === 'ADMIN') {
            return true;
        }
    }

    // Not logged in or not an admin, redirect to home or login
    router.navigate(['/dashboard']);
    return false;
};
