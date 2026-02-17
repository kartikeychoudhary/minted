export interface Budget {
  id?: number;
  name: string;
  amount: number;
  month: number;
  year: number;
  categoryId?: number;
  categoryName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface BudgetRequest {
  name: string;
  amount: number;
  month: number;
  year: number;
  categoryId?: number;
}

export interface BudgetResponse {
  id: number;
  name: string;
  amount: number;
  month: number;
  year: number;
  categoryId: number | null;
  categoryName: string | null;
  createdAt: string;
  updatedAt: string;
}
