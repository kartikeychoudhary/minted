import { TransactionType } from './category.model';

export interface TransactionRequest {
  amount: number;
  type: TransactionType;
  description: string;
  notes?: string;
  transactionDate: string; // ISO date string
  accountId: number;
  toAccountId?: number;
  categoryId: number;
  isRecurring?: boolean;
  tags?: string;
  excludeFromAnalysis?: boolean;
}

export interface TransactionResponse {
  id: number;
  amount: number;
  type: TransactionType;
  description: string;
  notes: string | null;
  transactionDate: string;
  accountId: number;
  accountName: string;
  toAccountId: number | null;
  toAccountName: string | null;
  categoryId: number;
  categoryName: string;
  categoryIcon: string;
  categoryColor: string;
  isRecurring: boolean;
  tags: string | null;
  isSplit: boolean;
  excludeFromAnalysis: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionFilters {
  accountId?: number;
  categoryId?: number;
  type?: TransactionType;
  startDate: string;
  endDate: string;
  searchTerm?: string;
}

export enum DateFilterOption {
  THIS_MONTH = 'THIS_MONTH',
  LAST_MONTH = 'LAST_MONTH',
  LAST_3_MONTHS = 'LAST_3_MONTHS',
  LAST_6_MONTHS = 'LAST_6_MONTHS',
  LAST_YEAR = 'LAST_YEAR',
  CUSTOM = 'CUSTOM'
}
