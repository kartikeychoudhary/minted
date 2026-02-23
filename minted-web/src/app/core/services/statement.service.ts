import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  CreditCardStatement,
  ParsedTransactionRow,
  ConfirmStatementRequest
} from '../models/statement.model';

@Injectable({ providedIn: 'root' })
export class StatementService {
  private apiUrl = `${environment.apiUrl}/statements`;

  constructor(private http: HttpClient) {}

  upload(file: File, accountId: number, pdfPassword?: string): Observable<CreditCardStatement> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('accountId', accountId.toString());
    if (pdfPassword) {
      formData.append('pdfPassword', pdfPassword);
    }
    return this.http.post<{ success: boolean; data: CreditCardStatement }>(
      `${this.apiUrl}/upload`, formData
    ).pipe(map(r => r.data));
  }

  triggerParse(statementId: number): Observable<CreditCardStatement> {
    return this.http.post<{ success: boolean; data: CreditCardStatement }>(
      `${this.apiUrl}/${statementId}/parse`, {}
    ).pipe(map(r => r.data));
  }

  getParsedRows(statementId: number): Observable<ParsedTransactionRow[]> {
    return this.http.get<{ success: boolean; data: ParsedTransactionRow[] }>(
      `${this.apiUrl}/${statementId}/parsed-rows`
    ).pipe(map(r => r.data));
  }

  confirmImport(request: ConfirmStatementRequest): Observable<void> {
    return this.http.post<{ success: boolean; message: string }>(
      `${this.apiUrl}/confirm`, request
    ).pipe(map(() => void 0));
  }

  getStatements(): Observable<CreditCardStatement[]> {
    return this.http.get<{ success: boolean; data: CreditCardStatement[] }>(this.apiUrl)
      .pipe(map(r => r.data));
  }

  getStatement(id: number): Observable<CreditCardStatement> {
    return this.http.get<{ success: boolean; data: CreditCardStatement }>(`${this.apiUrl}/${id}`)
      .pipe(map(r => r.data));
  }
}
