export type SplitType = 'EQUAL' | 'UNEQUAL' | 'SHARE';

export interface SplitShareRequest {
  friendId: number | null;
  shareAmount: number;
  sharePercentage?: number;
  isPayer?: boolean;
}

export interface SplitShareResponse {
  id: number;
  friendId: number | null;
  friendName: string;
  friendAvatarColor: string | null;
  shareAmount: number;
  sharePercentage: number | null;
  isPayer: boolean;
  isSettled: boolean;
  settledAt: string | null;
  splitDescription: string;
  splitCategoryName: string;
  splitTransactionDate: string;
}

export interface SplitTransactionRequest {
  sourceTransactionId?: number;
  description: string;
  categoryName: string;
  totalAmount: number;
  splitType: SplitType;
  transactionDate: string;
  shares: SplitShareRequest[];
}

export interface SplitTransactionResponse {
  id: number;
  sourceTransactionId: number | null;
  description: string;
  categoryName: string;
  totalAmount: number;
  splitType: string;
  transactionDate: string;
  isSettled: boolean;
  yourShare: number;
  shares: SplitShareResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface SplitBalanceSummaryResponse {
  youAreOwed: number;
  youOwe: number;
}

export interface FriendBalanceResponse {
  friendId: number;
  friendName: string;
  avatarColor: string;
  balance: number;
}

export interface SettleRequest {
  friendId: number;
}
