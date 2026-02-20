export interface BulkImportResponse {
  id: number;
  accountId: number;
  accountName: string;
  importType: string;
  fileName: string;
  fileSize: number;
  totalRows: number;
  validRows: number;
  duplicateRows: number;
  errorRows: number;
  importedRows: number;
  status: string;
  jobExecutionId: number | null;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CsvUploadResponse {
  importId: number;
  totalRows: number;
  validRows: number;
  errorRows: number;
  duplicateRows: number;
  rows: CsvRowPreview[];
}

export interface CsvRowPreview {
  rowNumber: number;
  date: string;
  amount: string;
  type: string;
  description: string;
  categoryName: string;
  notes: string;
  tags: string;
  status: string;
  errorMessage: string | null;
  matchedCategoryId: number | null;
  isDuplicate: boolean;
}

export interface BulkImportConfirmRequest {
  importId: number;
  skipDuplicates: boolean;
}

export type ImportStatus = 'PENDING' | 'VALIDATING' | 'VALIDATED' | 'IMPORTING' | 'COMPLETED' | 'FAILED';
