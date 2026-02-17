export interface Account {
  id?: number;
  name: string;
  accountTypeId: number;
  accountTypeName?: string;
  balance: number;
  currency?: string;
  color?: string;
  icon?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface AccountRequest {
  name: string;
  accountTypeId: number;
  balance?: number;
  currency?: string;
  color?: string;
  icon?: string;
}

export interface AccountResponse {
  id: number;
  name: string;
  accountTypeId: number;
  accountTypeName: string;
  balance: number;
  currency: string;
  color: string;
  icon: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}
