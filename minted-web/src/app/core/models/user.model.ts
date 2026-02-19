export interface User {
  id: number;
  username: string;
  displayName: string;
  email: string | null;
  forcePasswordChange: boolean;
  currency: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    token: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: User;
  };
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
  status?: number;
  path?: string;
  timestamp?: string;
}
