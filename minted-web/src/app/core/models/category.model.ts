export enum TransactionType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE',
  TRANSFER = 'TRANSFER'
}

export interface Category {
  id?: number;
  name: string;
  type: TransactionType;
  icon?: string;
  color?: string;
  parentId?: number;
  parentName?: string;
  isActive?: boolean;
  isDefault?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CategoryRequest {
  name: string;
  type: TransactionType;
  icon?: string;
  color?: string;
  parentId?: number;
}

export interface CategoryResponse {
  id: number;
  name: string;
  type: TransactionType;
  icon: string;
  color: string;
  parentId: number | null;
  parentName: string | null;
  isActive: boolean;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}
