import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  BulkImportResponse,
  BulkImportConfirmRequest,
  CsvUploadResponse
} from '../models/import.model';

@Injectable({ providedIn: 'root' })
export class ImportService {
  private apiUrl = `${environment.apiUrl}/imports`;

  constructor(private http: HttpClient) {}

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/template`, { responseType: 'blob' });
  }

  uploadCsv(file: File, accountId: number): Observable<CsvUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('accountId', accountId.toString());
    return this.http.post<{ success: boolean; data: CsvUploadResponse }>(
      `${this.apiUrl}/upload`, formData
    ).pipe(map(r => r.data));
  }

  confirmImport(request: BulkImportConfirmRequest): Observable<BulkImportResponse> {
    return this.http.post<{ success: boolean; data: BulkImportResponse }>(
      `${this.apiUrl}/confirm`, request
    ).pipe(map(r => r.data));
  }

  getUserImports(): Observable<BulkImportResponse[]> {
    return this.http.get<{ success: boolean; data: BulkImportResponse[] }>(this.apiUrl)
      .pipe(map(r => r.data));
  }

  getImportById(id: number): Observable<BulkImportResponse> {
    return this.http.get<{ success: boolean; data: BulkImportResponse }>(`${this.apiUrl}/${id}`)
      .pipe(map(r => r.data));
  }

  getImportJobDetails(id: number): Observable<any> {
    return this.http.get<{ success: boolean; data: any }>(`${this.apiUrl}/${id}/job`)
      .pipe(map(r => r.data));
  }
}
