export interface AccountType {
  id?: number;
  name: string;
  description: string;
  icon: string;
  isDefault?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface AccountTypeRequest {
  name: string;
  description: string;
  icon: string;
}

export interface AccountTypeResponse {
  id: number;
  name: string;
  description: string;
  icon: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}
