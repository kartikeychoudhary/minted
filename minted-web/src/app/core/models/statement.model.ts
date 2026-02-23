export type StatementStatus = 'UPLOADED' | 'TEXT_EXTRACTED' | 'LLM_PARSED'
  | 'CONFIRMING' | 'COMPLETED' | 'FAILED';

export interface ParsedTransactionRow {
  tempId: string;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  description: string;
  transactionDate: string;
  categoryName: string;
  matchedCategoryId: number | null;
  notes: string;
  tags: string;
  isDuplicate: boolean;
  duplicateReason: string;
  mappedByRule: boolean;
}

export interface CreditCardStatement {
  id: number;
  accountId: number;
  accountName: string;
  fileName: string;
  fileSize: number;
  status: StatementStatus;
  currentStep: number;
  extractedText: string | null;
  parsedCount: number;
  duplicateCount: number;
  importedCount: number;
  errorMessage: string | null;
  jobExecutionId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface ConfirmStatementRequest {
  statementId: number;
  skipDuplicates: boolean;
}
