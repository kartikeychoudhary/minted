---
title: Authentication — Web
feature: auth
layer: web
module: AuthModule (lazy, /auth)
components:
  - LoginComponent
  - SignupComponent
  - ChangePasswordComponent
related:
  - docs/features/api/auth.md
  - docs/features/web/layout.md
---

# Authentication — Web

## Overview

JWT-based auth with login, optional public signup, and forced password change on first login.

---

## Module

`AuthModule` — lazy-loaded at `/auth`. Non-standalone components only.

---

## Components

### LoginComponent (`/auth/login`)
- PrimeNG `p-card` centered on screen with Minted logo
- Fields: Username (`pInputText`), Password (`p-password`)
- "Login" button (`p-button`)
- On success: if `forcePasswordChange=true` → navigate to `/auth/change-password`; else → `/dashboard`
- "Create Account" link shown conditionally when `authService.isSignupEnabled()` is true → routes to `/signup`

### SignupComponent (`/signup`)
- Same card layout + decorative blurs as login
- Fields: Display Name, Email, Username, Password (`p-password`), Confirm Password (`p-password`)
- Cross-field password match validator
- On init: calls `isSignupEnabled()` — if disabled, shows amber warning banner and disables submit
- On success: auto-login (API returns tokens), navigate to `/dashboard`
- Footer: "Already have an account? Sign In" link

### ChangePasswordComponent (`/auth/change-password`)
- Fields: Current Password, New Password, Confirm Password
- Validation: min 8 chars, at least one uppercase, one number, passwords must match
- `p-password` with strength indicator
- On success: navigate to `/dashboard`

---

## Services & State

**`AuthService` (`core/services/auth.service.ts`)**
- `isAuthenticated$` — `BehaviorSubject<boolean>`
- `currentUser$` — `BehaviorSubject<User | null>`
- Tokens stored in `localStorage`
- `signup(request)` — POST + auto-login
- `isSignupEnabled()` — GET `/auth/signup-enabled`

**`JwtInterceptor`** — Attaches `Authorization: Bearer <token>` to all API requests
**`ErrorInterceptor`** — On 401: clears auth + redirects to login. On 403 forcePasswordChange: redirects to change-password. On 400/409/500: shows PrimeNG toast.

---

## Guards

- `AuthGuard` — Redirects to `/auth/login` if not authenticated
- `ForcePasswordChangeGuard` — Redirects to `/auth/change-password` if `forcePasswordChange=true`
